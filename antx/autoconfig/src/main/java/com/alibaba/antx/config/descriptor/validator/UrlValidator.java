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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlValidator extends ConfigValidator {
    private static final Logger  log            = LoggerFactory.getLogger(UrlValidator.class);
    private              boolean checkHostExist = true;
    private String[] protocols;
    private boolean endsWithSlash = true;
    private String message;

    @Override
    public Logger getLogger() {
        return log;
    }

    public void setCheckHostExist(boolean checkHostExist) {
        this.checkHostExist = checkHostExist;
    }

    public void setProtocols(String protocols) {
        this.protocols = StringUtil.split(protocols);
    }

    public void setEndsWithSlash(boolean endsWithSlash) {
        this.endsWithSlash = endsWithSlash;
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

        URL url;

        // 检查URL合法性
        getLogger().info("Checking URL validility: " + value);
        message = "非法的URL：" + value;

        try {
            url = new URL(value);

            if (StringUtil.isBlank(url.getHost())) {
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        // 检查协议
        if (!validateProtocols(url.getProtocol())) {
            message = getProtocolMessage();
            return false;
        }

        // 检查域名存在与否
        if (checkHostExist) {
            getLogger().info("Validating host name or IP address: " + url.getHost());

            try {
                InetAddress.getByName(url.getHost());
            } catch (UnknownHostException e) {
                message = "非法的域名或IP：" + url.getHost();
                return false;
            }
        }

        // 检查是否以/结尾
        if (endsWithSlash) {
            if (!value.endsWith("/")) {
                message = "URL请以/结尾";
                return false;
            }
        }

        return true;
    }

    private boolean validateProtocols(String protocol) {
        if (protocol == null) {
            return false;
        }

        protocol = protocol.trim();

        if (StringUtil.isEmpty(protocol)) {
            return false;
        }

        if (protocols == null || protocols.length == 0) {
            return true;
        }

        if (getLogger().isDebugEnabled()) {
            StringBuffer buffer = new StringBuffer();

            buffer.append("Validating protocol with choice[");

            for (int i = 0; i < protocols.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }

                buffer.append(protocols[i]);
            }

            buffer.append("]: ").append(protocol);

            getLogger().debug(buffer.toString());
        }

        for (String protocol2 : protocols) {
            if (protocol.equals(protocol2)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected String getDefaultMessage() {
        return message;
    }

    protected String getProtocolMessage() {
        StringBuffer buffer = new StringBuffer("您必须在下列值中选择协议：");

        for (int i = 0; i < protocols.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append(protocols[i]);
        }

        return buffer.toString();
    }
}
