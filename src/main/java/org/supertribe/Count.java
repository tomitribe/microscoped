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
package org.supertribe;

import java.util.concurrent.atomic.AtomicInteger;

@MethodScoped
public class Count {

    private final AtomicInteger count = new AtomicInteger();

    public int get() {
        return count.get();
    }

    public int add() {
        return count.incrementAndGet();
    }

    public boolean compareAndSet(int expect, int update) {
        return count.compareAndSet(expect, update);
    }

    public int remove() {
        return count.decrementAndGet();
    }
}
