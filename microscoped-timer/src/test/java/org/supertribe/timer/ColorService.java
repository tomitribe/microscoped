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
package org.supertribe.timer;

import static jakarta.ejb.LockType.READ;

import org.tomitribe.microscoped.timer.TimerScopedInterceptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Lock;
import jakarta.ejb.Schedule;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptors;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Lock(READ)
@Singleton
@Path("/color")
//@TimerScopeEnabled
@Interceptors(TimerScopedInterceptor.class)
public class ColorService {

    @Inject
    private Count count;

    @Resource
    private TimerService timerService;

    private final Log log = new Log();

    @PostConstruct
    public void construct() {
        {
            final TimerConfig timerConfig = new TimerConfig("red", false);
            timerService.createCalendarTimer(new ScheduleExpression().second("*").minute("*").hour("*"), timerConfig);
        }
        {
            final TimerConfig timerConfig = new TimerConfig("green", false);
            timerService.createCalendarTimer(new ScheduleExpression().second("*").minute("*").hour("*"), timerConfig);
        }
    }

    @GET
    public Log getLog() {
        return log;
    }

    @Timeout
    public void timeout(Timer timer) {
        log.add(String.format("%s, %s", timer.getInfo(), count.add()));
    }

    @Schedule(second = "*", minute = "*", hour = "*")
    public void blue() {
        log.add(String.format("blue, %s", count.add()));
    }

    @Schedule(second = "*", minute = "*", hour = "*")
    public void orange() {
        log.add(String.format("orange, %s", count.add()));
    }
}
