/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.microscoped.core;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

class Scope<Key> {
    private final Map<Contextual<?>, CompletableFuture<Instance>> instances = new ConcurrentHashMap<>();
    private volatile boolean closed = false;
    private final Key key;

    public Scope(final Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    /**
     * Returns an instance of the Bean (Contextual), creating it if necessary
     * @param contextual the Bean type to create
     * @param <T> the Java type of the bean instance itself
     * @return existing or newly created bean instance, never null
     */
    public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
        if (closed) {
            throw new ContextNotActiveException(String.format("Context not active [key=%s, contextual=%s, creationalContext=%s]",
                    String.valueOf(key),
                    (contextual instanceof Bean) ? ((Bean<?>) contextual).getBeanClass().getName() : String.valueOf(contextual),
                    String.valueOf(creationalContext)));
        } else {
            CompletableFuture<Instance> future = instances.get(contextual);
            if (future == null) {
                final CompletableFuture<Instance> newFuture = new CompletableFuture<>();
                // Atomically place the new future in the map.
                future = instances.putIfAbsent(contextual, newFuture);
                if (future == null) {
                    // We won the race. Our future is now in the map. We are responsible for creating the bean.
                    future = newFuture;
                    try {
                        // The creation logic is now safely executed by only one thread.
                        final Instance<T> createdInstance = new Instance<>(contextual, creationalContext);
                        future.complete(createdInstance); // Publish the result for other threads.
                    } catch (final Throwable e) {
                        // If creation fails, complete the future exceptionally and remove it from the map
                        // so that subsequent requests can try again.
                        future.completeExceptionally(e);
                        instances.remove(contextual, future);
                        // Re-throw the original exception.
                        throw e;
                    }
                }
            }
            // All threads (the winner and the waiters) wait here for the result.
            return fastWait(future);
        }
    }

    /**
     * Returns the existing instance of the Bean or null if none exists yet
     * @param contextual the Bean type to create
     * @param <T> the Java type of the bean instance itself
     * @return existing the bean instance or null
     */
    public <T> T get(final Contextual<T> contextual) {
        if (closed) {
            throw new ContextNotActiveException(String.format("Context not active [key=%s, contextual=%s]", String.valueOf(key),
                    (contextual instanceof Bean) ? ((Bean<?>) contextual).getBeanClass().getName() : String.valueOf(contextual)));
        } else {
            final CompletableFuture<Instance> future = instances.get(contextual);
            return fastWait(future);
        }
    }

    private static <T> T fastWait(final CompletableFuture<Instance> future) throws Error {
        final T value;
        if (future == null) {
            value = null;
        } else if (future.isDone() && !future.isCompletedExceptionally()) {
            // Completed normally: read the result without join()
            final Instance<T> instance = future.getNow(null); // never null in this branch
            value = instance.get();
        } else {
            try {
                final Instance<T> instance = future.get();
                value = instance.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (final ExecutionException ee) {
                final Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        }
        return value;
    }

    /**
     * Destroy all the instances in this scope
     */
    public void destroy() {
        closed = true;
        instances.values().forEach((final CompletableFuture<Instance> future) -> {
            // There's a small chance
            if (!future.isCompletedExceptionally()) {
                future.join().destroy();
            }
        });
        instances.clear();
    }

    private static class Instance<T> {
        private final T instance;
        private final CreationalContext<T> creationalContext;
        private final Contextual<T> contextual;

        public Instance(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
            this(contextual, creationalContext, contextual.create(creationalContext));
        }

        public Instance(final Contextual<T> contextual, final CreationalContext<T> creationalContext, final T instance) {
            this.instance = instance;
            this.creationalContext = creationalContext;
            this.contextual = contextual;
        }

        public T get() {
            return instance;
        }

        public void destroy() {
            if (contextual != null) {
                contextual.destroy(instance, creationalContext);
            }
        }
    }
}
