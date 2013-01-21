/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

class IdeaLoggerFactory implements ILoggerFactory {
    public Logger getLogger(String name) {
        String category = isClassName(name) ? "#" + name : name;
        return new IdeaLoggerAdapter(com.intellij.openapi.diagnostic.Logger.getInstance(category), name);
    }

    private boolean isClassName(String name) {
        int index = name.lastIndexOf(".");

        if (index > 0 && index + 1 < name.length()) {
            return Character.isUpperCase(name.charAt(index + 1));
        } else {
            return false;
        }
    }
}
