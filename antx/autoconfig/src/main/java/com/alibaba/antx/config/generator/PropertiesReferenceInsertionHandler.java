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

package com.alibaba.antx.config.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.ContextAware;

import com.alibaba.antx.config.ConfigConstant;
import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.descriptor.ConfigGroup;
import com.alibaba.antx.config.descriptor.ConfigProperty;
import com.alibaba.antx.util.StringUtil;

public class PropertiesReferenceInsertionHandler implements ReferenceInsertionEventHandler, ContextAware {
    private static final Pattern referencePattern = Pattern
            .compile("\\s*\\$\\s*\\!?\\s*(\\{\\s*(.*?)\\s*\\}|(.*?))\\s*");

    private Context context;
    private Map<String, Boolean> definedProperties;
    private Map<String, String> props;

    public PropertiesReferenceInsertionHandler(ConfigDescriptor configDescriptor, Map props) {
        this.props = props;
        this.definedProperties = new HashMap<String, Boolean>();

        for (ConfigGroup group : configDescriptor.getGroups()) {
            for (ConfigProperty prop : group.getProperties()) {
                definedProperties.put(prop.getName(), prop.isRequired());
                definedProperties.put(StringUtil.getValidIdentifier(prop.getName()), prop.isRequired());
            }
        }
    }

    public Object referenceInsert(String reference, Object value) {
        // 假如可从context中取得的值，直接返回之。
        if (value != null) {
            return value;
        }

        String normalizedRef = normalizeReference(reference);

        // 从props中取值，也就是从antx.properties中取值。
        value = PropertiesLoader.evaluate(normalizedRef, props);

        // 假如${placeholder被定义，必定是合法值（因为已经验证过了）。
        // 对于null值返回空白。
        if (value == null) {
            value = "";
        }

        // 检查placeholder是否在auto-config.xml中定义。
        // 有一种情况：auto-config.xml中未定义，但antx.properties中有值，仍然会返回该值，但报警告。
        @SuppressWarnings("unchecked")
        Set<String> unknownRefs = (Set<String>) context.get(ConfigConstant.UNKNWON_REFS_KEY);

        // 假如${placeholder}未定义
        if (!definedProperties.containsKey(normalizedRef)) {
            if (unknownRefs != null) {
                unknownRefs.add(normalizedRef);
            }
        }

        return value;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private static String normalizeReference(String reference) {
        if (reference == null) {
            return "";
        }

        Matcher matcher = referencePattern.matcher(reference);

        if (matcher.matches()) {
            String form1 = matcher.group(2);
            String form2 = matcher.group(3);

            if (form1 == null) {
                return form2;
            } else {
                return form1;
            }
        } else {
            return reference;
        }
    }
}
