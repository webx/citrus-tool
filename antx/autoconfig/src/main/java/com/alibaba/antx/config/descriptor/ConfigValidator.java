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

package com.alibaba.antx.config.descriptor;

import com.alibaba.antx.util.StringUtil;
import org.slf4j.Logger;

/**
 * 属性验证器。
 *
 * @author Michael Zhou
 */
public abstract class ConfigValidator {
    private ConfigProperty property;
    private String         message;

    public ConfigDescriptor getConfigDescriptor() {
        return getConfigProperty().getConfigDescriptor();
    }

    public ConfigProperty getConfigProperty() {
        return property;
    }

    public void setConfigProperty(ConfigProperty property) {
        this.property = property;
    }

    public abstract Logger getLogger();

    public String getMessage() {
        return StringUtil.isEmpty(message) ? getDefaultMessage() : message;
    }

    public void setMessage(String string) {
        message = string;
    }

    protected String getDefaultMessage() {
        return getConfigProperty().getName() + ": " + this;
    }

    public abstract boolean validate(String value);

    @Override
    public String toString() {
        return StringUtil.getShortClassName(getClass());
    }
}
