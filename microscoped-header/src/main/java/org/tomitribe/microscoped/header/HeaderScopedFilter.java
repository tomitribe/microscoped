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
package org.tomitribe.microscoped.header;

import org.tomitribe.microscoped.core.ScopeContext;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class HeaderScopedFilter implements javax.servlet.Filter {

    @Inject
    private BeanManager beanManager;

    @Inject
    private HeaderScopedConfig config;

    @Inject
    private Header header;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        final String value = getKey((HttpServletRequest) servletRequest);

        final ScopeContext<String> context = (ScopeContext<String>) beanManager.getContext(HeaderScoped.class);

        // set the name and value in the thread for others to see
        header.setName(config.getHeaderName());
        header.setValue(value);

        // There won't be a "previous", but doesn't hurt anything to be complete
        final String previous = context.enter(value);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            context.exit(previous);
        }
    }

    private String getKey(final HttpServletRequest request) {
        final String header = request.getHeader(config.getHeaderName());

        if (header != null) {
            return header;
        } else {
            return config.getDefaultValue();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
