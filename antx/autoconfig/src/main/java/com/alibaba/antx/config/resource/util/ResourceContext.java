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

package com.alibaba.antx.config.resource.util;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public final class ResourceContext {
    private static ThreadLocal contextHolder = new ThreadLocal();
    private URI                currentURI;
    private String             currentUsername;
    private Set                visitedURIs   = new HashSet();

    private ResourceContext() {
    }

    public static ResourceContext get() {
        ResourceContext context = (ResourceContext) contextHolder.get();

        if (context == null) {
            context = new ResourceContext();
            contextHolder.set(context);
        }

        return context;
    }

    public static void clear() {
        contextHolder.set(null);
    }

    public URI getCurrentURI() {
        return currentURI;
    }

    public void setCurrentURI(URI currentURI) {
        this.currentURI = currentURI;
        this.currentUsername = ResourceUtil.getUsername(currentURI);
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public Set getVisitedURIs() {
        return visitedURIs;
    }
}
