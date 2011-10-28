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

package com.alibaba.toolkit.util.configuration.digester;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

/**
 * 在被匹配元素开始时, 设置上下文, 在元素结束时, 清除最近的上下文.
 * 
 * @version $Id: SetContextRule.java,v 1.2 2003/08/07 08:08:59 zyh Exp $
 * @author Michael Zhou
 */
public class SetContextRule extends Rule {
    protected String attributeName;
    protected Class contextFactoryClass;
    protected ContextFactory contextFactory;

    /**
     * 使用指定attribute的值作为当前context的值.
     * 
     * @param attributeName XML属性名
     */
    public SetContextRule(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * 使用指定类作为取得context的工厂.
     * 
     * @param contextFactoryClass 工厂类
     */
    public SetContextRule(Class contextFactoryClass) {
        this.contextFactoryClass = contextFactoryClass;
    }

    /**
     * 使用指定context工厂取得当前context的值.
     * 
     * @param contextFactory 工厂对象
     */
    public SetContextRule(ContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    /**
     * 开始处理, 压入新的context.
     * 
     * @param attributes XML属性值
     * @throws Exception 如果失败
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        String context = null;

        if (attributeName != null) {
            context = attributes.getValue(attributeName);
        }

        if (context == null && getFactory() != null) {
            context = getFactory().getContext(attributes);
        }

        if (context != null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[SetContextRule]{" + digester.getMatch() + "} New " + context);
            }

            ContextSensitiveRules rules = (ContextSensitiveRules) digester.getRules();

            rules.pushContext(context);
        }
    }

    /**
     * 结束处理, 弹出最近的context
     * 
     * @throws Exception 如果失败
     */
    @Override
    public void end(String namespace, String name) throws Exception {
        ContextSensitiveRules rules = (ContextSensitiveRules) digester.getRules();
        String context = rules.popContext();

        if (context != null) {
            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[SetContextRule]{" + digester.getMatch() + "} Pop " + context);
            }
        }
    }

    /**
     * 取得rule的字符串表示.
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("SetContextRule[");

        if (attributeName != null) {
            buffer.append("attributeName=").append(attributeName);
        } else if (contextFactoryClass != null) {
            buffer.append("contextFactory=").append(contextFactoryClass);
        } else if (contextFactory != null) {
            buffer.append("contextFactory=").append(contextFactory);
        }

        buffer.append("]");
        return buffer.toString();
    }

    /**
     * 取得工厂.
     * 
     * @return 取得当前context的工厂
     * @throws Exception 如果失败
     */
    protected ContextFactory getFactory() throws Exception {
        if (contextFactory == null && contextFactoryClass != null) {
            contextFactory = (ContextFactory) contextFactoryClass.newInstance();
        }

        contextFactory.setDigester(digester);

        return contextFactory;
    }
}
