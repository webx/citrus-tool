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
import org.apache.commons.digester.RuleSet;
import org.xml.sax.Attributes;

/**
 * 设置上下文相关的<code>RuleSet</code>的规则.
 *
 * @author Michael Zhou
 * @version $Id: SetRuleSetRule.java,v 1.2 2003/08/07 08:08:59 zyh Exp $
 */
public class SetRuleSetRule extends Rule {
    private Class          ruleSetFactoryClass;
    private RuleSetFactory ruleSetFactory;

    /**
     * 使用指定类作为取得<code>RuleSet</code>的工厂.
     *
     * @param ruleSetFactoryClass 工厂类
     */
    public SetRuleSetRule(Class ruleSetFactoryClass) {
        this.ruleSetFactoryClass = ruleSetFactoryClass;
    }

    /**
     * 使用指定<code>RuleSet</code>工厂.
     *
     * @param ruleSetFactory 工厂对象
     */
    public SetRuleSetRule(RuleSetFactory ruleSetFactory) {
        this.ruleSetFactory = ruleSetFactory;
    }

    /**
     * 开始处理, 创建上下文相关的<code>RuleSet</code>.
     *
     * @param attributes XML属性值
     * @throws Exception 如果失败
     */
    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        ContextSensitiveRules rules = (ContextSensitiveRules) digester.getRules();
        String context = rules.getContext();

        if (!rules.isInitialized(context)) {
            rules.setInitializing(context);

            RuleSet ruleSet = getFactory().getRuleSet(attributes);

            digester.addRuleSet(ruleSet);
            rules.setInitialized(context);

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug("[SetRuleSetRule]{" + digester.getMatch() + "} New " + ruleSet);
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
        StringBuffer buffer = new StringBuffer("SetRuleSetRule[");

        if (ruleSetFactoryClass != null) {
            buffer.append("ruleSetFactory=").append(ruleSetFactoryClass);
        } else if (ruleSetFactory != null) {
            buffer.append("ruleSetFactory=").append(ruleSetFactory);
        }

        buffer.append("]");
        return buffer.toString();
    }

    /**
     * 取得工厂.
     *
     * @return 取得<code>RuleSet</code>的工厂
     * @throws Exception 如果失败
     */
    protected RuleSetFactory getFactory() throws Exception {
        if (ruleSetFactory == null && ruleSetFactoryClass != null) {
            ruleSetFactory = (RuleSetFactory) ruleSetFactoryClass.newInstance();
        }

        ruleSetFactory.setDigester(digester);

        return ruleSetFactory;
    }
}
