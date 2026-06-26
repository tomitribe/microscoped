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
package org.tomitribe.microscoped.domain;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import org.tomitribe.microscoped.core.ScopeContext;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = "/*")
public class DomainScopedFilter implements jakarta.servlet.Filter {

    @Inject
    private BeanManager beanManager;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        final String domain = servletRequest.getServerName();

        final ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(DomainScoped.class);

        getContextualReference(Domain.class, beanManager).setName(domain);

        // There won't be a "previous" domain, but doesn't hurt anything to be complete
        final String previous = context.enter(domain);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            context.exit(previous);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    public static <T> T getContextualReference(Class<T> type, final BeanManager beanManager, Annotation... qualifiers) {

        final Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);

        if (beans == null || beans.isEmpty()) {
            throw new IllegalStateException("Could not find beans for Type=" + type
                    + " and qualifiers:" + Arrays.toString(qualifiers));
        }

        final Bean<?> bean = beanManager.resolve(beans);

        final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        return (T) beanManager.getReference(bean, type, creationalContext);
    }
}
