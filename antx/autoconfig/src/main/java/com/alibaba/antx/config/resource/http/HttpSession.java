/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.antx.config.resource.http;

import java.net.URI;
import java.util.Set;

import com.alibaba.antx.config.resource.AuthenticationHandler.UsernamePassword;
import com.alibaba.antx.config.resource.Resource;
import com.alibaba.antx.config.resource.ResourceDriver;
import com.alibaba.antx.config.resource.ResourceURI;
import com.alibaba.antx.config.resource.Session;
import com.alibaba.antx.config.resource.util.ResourceContext;
import com.alibaba.antx.config.resource.util.ResourceKey;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;

public class HttpSession extends Session {
    private final HttpClient client;

    public HttpSession(ResourceDriver driver) {
        super(driver);

        client = new HttpClient();

        client.getParams().setAuthenticationPreemptive(true);
        client.getParams().setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {
            public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy)
                    throws CredentialsNotAvailableException {
                URI uri = ResourceContext.get().getCurrentURI();
                String username = ResourceContext.get().getCurrentUsername();
                Set visitedURIs = ResourceContext.get().getVisitedURIs();
                ResourceKey key = new ResourceKey(new ResourceURI(uri));
                String message;

                message = "\n";
                message += "Authentication required.\n";
                message += "realm: " + scheme.getRealm() + "\n";
                message += "  uri: " + uri + "\n";

                UsernamePassword up = getResourceManager().getAuthenticationHandler().authenticate(message, uri,
                                                                                                   username, visitedURIs.contains(key));

                visitedURIs.add(key);

                return new UsernamePasswordCredentials(up.getUsername(), up.getPassword());
            }
        });
    }

    public HttpClient getClient() {
        return client;
    }

    @Override
    public boolean acceptOption(String optionName) {
        if ("charset".equals(optionName)) {
            return true;
        }

        return false;
    }

    @Override
    public Resource getResource(final ResourceURI uri) {
        return new HttpResource(this, uri);
    }
}
