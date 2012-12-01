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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.ConfigResource;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.plugins.PluginCreateRule;
import org.apache.commons.digester.plugins.PluginDeclarationRule;
import org.apache.commons.digester.plugins.PluginRules;

/**
 * 装入一个config descriptor的工具类。
 *
 * @author Michael Zhou
 */
public class ConfigDescriptorLoader {
    /**
     * 从指定输入流装入配置文件。
     *
     * @param url  配置文件的URL
     * @param name descriptor的名字（路径）
     * @return config descriptor
     */
    public synchronized ConfigDescriptor load(ConfigResource descriptorResource, InputStream istream) {
        Digester digester = getDigester();

        digester.push(new ConfigDescriptor(descriptorResource));

        try {
            return (ConfigDescriptor) digester.parse(istream);
        } catch (Exception e) {
            throw new ConfigException("Failed to load config descriptor: " + descriptorResource.getURL(), e);
        }
    }

    /** 取得validator的列表。 */
    public synchronized Map loadValidatorClasses() {
        Digester digester = loadValidatorPlugins();

        return (Map) digester.pop();
    }

    /** 创建读取descriptor的digester。 */
    protected Digester getDigester() {
        Digester digester = loadValidatorPlugins();

        // config
        digester.addSetProperties("config");

        // config/group
        digester.addObjectCreate("config/group", ConfigGroup.class);
        digester.addSetProperties("config/group");
        digester.addSetNext("config/group", "addGroup");

        // config/group/property
        digester.addObjectCreate("config/group/property", ConfigProperty.class);
        digester.addSetProperties("config/group/property");
        digester.addCallMethod("config/group/property", "afterPropertiesSet");
        digester.addSetNext("config/group/property", "addProperty");

        // config/group/property/validator
        PluginCreateRule pcr = new PluginCreateRule(ConfigValidator.class);

        pcr.setPluginIdAttribute(null, "name");

        digester.addRule("config/group/property/validator", pcr);
        digester.addSetNext("config/group/property/validator", "addValidator");

        // config/script/generate
        digester.addObjectCreate("config/script/generate", ConfigGenerate.class);
        digester.addSetProperties("config/script/generate");
        digester.addSetNext("config/script/generate", "addGenerate");

        digester.clear();

        return digester;
    }

    /** 读取validators.xml中的validator定义。 */
    private Digester loadValidatorPlugins() {
        Digester digester = new Digester();

        digester.setRules(new PluginRules());

        digester.addObjectCreate("config-property-validators", HashMap.class);

        digester.addCallMethod("config-property-validators/validator", "put", 2);
        digester.addCallParam("config-property-validators/validator", 0, "id");
        digester.addCallParam("config-property-validators/validator", 1, "class");

        digester.addRule("config-property-validators/validator", new PluginDeclarationRule());

        InputStream istream = getClass().getResourceAsStream("validators.xml");

        try {
            digester.push(digester.parse(istream));
        } catch (Exception e) {
            throw new ConfigException("Failed to load validators", e);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }
        }

        digester.getRules().clear();

        return digester;
    }
}
