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

import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.microscoped.core.ScopeContext;
import org.tomitribe.microscoped.method.MethodScopedExtension;

import javax.enterprise.inject.spi.Extension;

@RunWith(Arquillian.class)
public class MethodScopedTest extends Assert {

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class)
                .addPackage(ScopeContext.class.getPackage())
                .addPackage(MethodScopedExtension.class.getPackage())
                .addPackage(ColorService.class.getPackage())
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/beans.xml"), "classes/META-INF/beans.xml")
                .addAsWebInfResource(new StringAsset(MethodScopedExtension.class.getName()),
                        "classes/META-INF/services/" + Extension.class.getName()
                );
    }

    @Inject
    private ColorService colorService;

    @Test
    public void test() throws Exception {
        assertRequest("color/red", "red, 1 invocations");
        assertRequest("color/red", "red, 2 invocations");
        assertRequest("color/red", "red, 3 invocations");
        assertRequest("color/green", "green, 1 invocations");
        assertRequest("color/green", "green, 2 invocations");
        assertRequest("color/green", "green, 3 invocations");
        assertRequest("color/red", "red, 4 invocations");
        assertRequest("color/blue", "blue, 1 invocations");
        assertRequest("color/blue", "blue, 2 invocations");
    }

    private void assertRequest(String path, String expected) {
        String actualMessage = invoke(path);
        assertEquals(expected, actualMessage);
    }

    private String invoke(String path) {
        if(path.endsWith("red")) {
            return colorService.red();
        } else if(path.endsWith("green")) {
            return colorService.green();
        } else {
            return colorService.blue();
        }
    }
}
