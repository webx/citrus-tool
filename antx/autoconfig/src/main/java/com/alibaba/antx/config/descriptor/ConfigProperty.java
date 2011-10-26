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

import java.util.LinkedList;
import java.util.List;

import com.alibaba.antx.config.descriptor.validator.RequiredValidator;

public class ConfigProperty {
    private ConfigGroup group;
    private String name;
    private String defaultValue;
    private String description;
    private boolean required = true;
    private List<ConfigValidator> validators = new LinkedList<ConfigValidator>();

    public ConfigDescriptor getConfigDescriptor() {
        return getConfigGroup().getConfigDescriptor();
    }

    public ConfigGroup getConfigGroup() {
        return group;
    }

    public void setConfigGroup(ConfigGroup group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<ConfigValidator> getValidators() {
        return validators;
    }

    public void addValidator(ConfigValidator validator) {
        validator.setConfigProperty(this);
        validators.add(validator);

        if (validator instanceof RequiredValidator) {
            this.required = true;
        }
    }

    public void afterPropertiesSet() {
        // RequiredValidator是一个特殊的validator，默认情况下required=true
        if (required) {
            addValidator(new RequiredValidator());
        }
    }

    public boolean isRequired() {
        return required;
    }

    public String toString() {
        return "Property[name=" + getName() + "]";
    }
}
