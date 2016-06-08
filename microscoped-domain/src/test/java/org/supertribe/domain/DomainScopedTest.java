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
package org.supertribe.domain;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tomitribe.microscoped.core.ScopeContext;
import org.tomitribe.microscoped.domain.DomainScopedExtension;

import javax.enterprise.inject.spi.Extension;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * This test requires some DNS hacking.
 * Edit your /etc/hosts to contain the following entries:
 *
 * <pre>
 * $ cat /etc/hosts | egrep 'orange|red'
 * 127.0.0.1	orange
 * 127.0.0.1	red
 * </pre>
 *
 * Then comment out the @Ignore
 */
@RunWith(Arquillian.class)
public class DomainScopedTest extends Assert {

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class)
                .addPackage(ScopeContext.class.getPackage())
                .addPackage(DomainScopedExtension.class.getPackage())
                .addPackage(SimpleService.class.getPackage())
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/beans.xml"), "classes/META-INF/beans.xml")
                .addAsWebInfResource(new StringAsset(DomainScopedExtension.class.getName()),
                        "classes/META-INF/services/" + Extension.class.getName()
                );
    }

    @ArquillianResource
    private URL webappUrl;

    @Test
    @Ignore("Comment this out to run the test")
    public void test() throws Exception {
        assertDomain("http://orange/", 1);
        assertDomain("http://orange/", 2);
        assertDomain("http://red/", 1);
        assertDomain("http://red/", 2);
        assertDomain("http://red/", 3);
        assertDomain("http://orange/", 3);
        assertDomain("http://orange/", 4);
        assertDomain("http://red/", 4);
    }

    private void assertDomain(String orange, int i) throws URISyntaxException {
        final URI uri = setDomain(orange, webappUrl.toURI());
        final WebClient webClient = WebClient.create(uri);

        final String result = webClient.path("domain").get(String.class);
        assertEquals(orange + " domain, " + i + " invocations", result);
    }

    private static URI setDomain(String domain, URI uri) throws URISyntaxException {
        final URI target = URI.create(domain);
        return new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                target.getHost(),
                uri.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment());
    }
}
