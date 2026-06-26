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

import static jakarta.ejb.LockType.READ;

import org.tomitribe.microscoped.method.MethodScopeEnabled;

import jakarta.ejb.Lock;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Lock(READ)
@Singleton
@Path("/color")
@MethodScopeEnabled
public class ColorService {

    @Inject
    private Count count;

    @GET
    @Path("/red")
    public String red() {
        return String.format("red, %s invocations", count.add());
    }

    @GET
    @Path("/green")
    public String green() {
        return String.format("green, %s invocations", count.add());
    }

    @GET
    @Path("/blue")
    public String blue() {
        return String.format("blue, %s invocations", count.add());
    }

}
