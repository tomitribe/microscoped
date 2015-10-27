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
package org.tomitribe.microscoped.timer;

import org.tomitribe.microscoped.core.ScopeContext;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;

@Interceptor
@TimerScopeEnabled
public class TimerScopedInterceptor {

    @Inject
    private BeanManager beanManager;

    @AroundTimeout
    public Object invoke(final InvocationContext invocation) throws Exception {
        final ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(TimerScoped.class);

        final String key = getKey(invocation);
        final String previous = context.enter(key);
        try {
            return invocation.proceed();
        } finally {
            context.exit(previous);
        }
    }

    private String getKey(InvocationContext invocation) {
        final String method = invocation.getMethod().toString();

        final Object object = invocation.getTimer();
        if (!(object instanceof Timer)) {
            return method;
        }

        final Timer timer = (Timer) object;
        final Serializable info = timer.getInfo();
        if (info == null || info instanceof ScheduleExpression) {
            return method;
        } else {
            return info.toString();
        }
    }
}
