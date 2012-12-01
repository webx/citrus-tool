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

package com.alibaba.antx.config.descriptor.validator;

import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequiredValidator extends ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(RequiredValidator.class);

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean validate(String value) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Validating value: " + value);
        }

        if (value == null) {
            return false;
        }

        value = value.trim();

        return !StringUtil.isEmpty(value);
    }

    @Override
    protected String getDefaultMessage() {
        return "您还没有填写" + getConfigProperty().getName();
    }
}
