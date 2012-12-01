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

package com.alibaba.antx.config.descriptor.validator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostExistValidator extends ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(HostExistValidator.class);
    private String hostname;

    @Override
    public Logger getLogger() {
        return log;
    }

    @Override
    public boolean validate(String value) {
        if (value == null) {
            return true;
        }

        value = value.trim();

        if (StringUtil.isEmpty(value)) {
            return true;
        }

        getLogger().info("Validating host name or IP address: " + value);

        hostname = value;

        try {
            InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return false;
        }

        return true;
    }

    @Override
    protected String getDefaultMessage() {
        return "非法的域名或IP：" + hostname;
    }
}
