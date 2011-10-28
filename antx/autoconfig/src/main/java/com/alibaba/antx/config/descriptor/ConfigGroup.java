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

package com.alibaba.antx.config.descriptor;

import java.util.ArrayList;
import java.util.List;

public class ConfigGroup {
    private ConfigDescriptor descriptor;
    private String name;
    private String description;
    private List properties = new ArrayList();

    public ConfigDescriptor getConfigDescriptor() {
        return descriptor;
    }

    public void setConfigDescriptor(ConfigDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addProperty(ConfigProperty configProperty) {
        configProperty.setConfigGroup(this);
        properties.add(configProperty);
    }

    public ConfigProperty[] getProperties() {
        return (ConfigProperty[]) properties.toArray(new ConfigProperty[properties.size()]);
    }

    @Override
    public String toString() {
        return "Group[name=" + getName() + "]";
    }
}
