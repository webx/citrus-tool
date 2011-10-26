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

public class ResourceUtil {
    public static String getUsername(URI uri) {
        if (uri == null) {
            return null;
        }

        String user = uri.getUserInfo();
        int colon = user == null ? -1 : user.indexOf(":");

        return colon <= 0 ? user : user.substring(0, colon);
    }
}
