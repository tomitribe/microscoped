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

import org.tomitribe.microscoped.timer.TimerScopedInterceptor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.Schedule;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static javax.ejb.LockType.READ;

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
