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

import com.alibaba.antx.config.ConfigResource;

import java.io.File;

import java.net.URL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 代表一个auto-config描述文件的内容。
 * 
 * @author Michael Zhou
 */
public class ConfigDescriptor {
    private ConfigResource resource;
    private String         description;
    private List           groups    = new LinkedList();
    private List           generates = new LinkedList();
    private Map            context   = new HashMap();

    public ConfigDescriptor(ConfigResource descriptorResource) {
        this.resource = descriptorResource;
    }

    public ConfigResource getConfigResource() {
        return resource;
    }

    public ConfigResource getConfigResourceBase() {
        return getConfigResource().getBase();
    }

    public File getFile() {
        return resource.getFile();
    }

    public File getBaseFile() {
        return getConfigResourceBase().getFile();
    }

    public URL getURL() {
        return resource.getURL();
    }

    public URL getBaseURL() {
        return getConfigResourceBase().getURL();
    }

    public String getName() {
        return resource.getName();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addGroup(ConfigGroup group) {
        group.setConfigDescriptor(this);
        groups.add(group);
    }

    public ConfigGroup[] getGroups() {
        return (ConfigGroup[]) groups.toArray(new ConfigGroup[groups.size()]);
    }

    public void setGroups(List list) {
        groups = list;
    }

    public void addGenerate(ConfigGenerate generate) {
        generate.setConfigDescriptor(this);
        generates.add(generate);
    }

    public void removeGenerate(ConfigGenerate generate) {
        generates.remove(generate);
    }

    public ConfigGenerate[] getGenerates() {
        return (ConfigGenerate[]) generates.toArray(new ConfigGenerate[generates.size()]);
    }

    public void setGenerates(List list) {
        generates = list;
    }

    public Map getContext() {
        return context;
    }

    public String toString() {
        return "Descriptor[" + ((getURL() == null) ? "" : getURL().toExternalForm()) + "]";
    }
}
