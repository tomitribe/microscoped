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
package org.supertribe.header;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.microscoped.core.ScopeContext;
import org.tomitribe.microscoped.header.HeaderScopedExtension;

import javax.enterprise.inject.spi.Extension;
import java.net.URL;

@RunWith(Arquillian.class)
public class HeaderScopedTest extends Assert {

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class)
                .addPackage(ScopeContext.class.getPackage())
                .addPackage(HeaderScopedExtension.class.getPackage())
                .addPackage(SimpleService.class.getPackage())
                .addPackages(true, "org.apache.http")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/beans.xml"), "classes/META-INF/beans.xml")
                .addAsServiceProviderAndClasses(Extension.class, HeaderScopedConfigExtension.class, HeaderScopedExtension.class)
                ;
    }

    @ArquillianResource
    private URL webappUrl;

    @Test
    public void test() throws Exception {
        assertVersion("1.0", 1);
        assertVersion("1.0", 2);
        assertVersion("1.1", 1);
        assertVersion("1.1", 2);
        assertVersion("1.1", 3);
        assertVersion("1.1", 4);
        assertVersion("1.0", 3);
        assertVersion("1.0", 4);
    }

    private void assertVersion(String version, int i) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final String uri = webappUrl.toURI() + "domain";
        HttpGet get = new HttpGet(uri);
        get.addHeader("version", version);
        CloseableHttpResponse response = httpclient.execute(get);
        HttpEntity entity = response.getEntity();
        String s = EntityUtils.toString(entity);

        final String expected = String.format("version=%s , %s invocations", version, i);

        assertEquals(expected, s);
    }
}
