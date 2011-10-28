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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rules;

/**
 * 这是从commons-digester扩展的<code>Digester</code>, 增加了一些方法, 以便实现上下文相关的规则.
 * 
 * @version $Id: ContextSensitiveDigester.java,v 1.1 2003/07/03 07:26:16 baobao
 *          Exp $
 * @author Michael Zhou
 */
public class ContextSensitiveDigester extends Digester {
    /**
     * 覆盖父类的方法, 将参数<code>rules</code>包装成<code>ContextSensitiveRules</code>对象.
     * 
     * @param rules <code>Rules</code>对象
     */
    @Override
    public void setRules(Rules rules) {
        if (!(rules instanceof ContextSensitiveRules)) {
            rules = new ContextSensitiveRules(rules);
        }

        this.rules = rules;
        this.rules.setDigester(this);
    }

    /**
     * 覆盖父类方法, 确保取得的对象为<code>ContextSensitiveRules</code>对象.
     * 
     * @return <code>Rules</code>对象
     */
    @Override
    public Rules getRules() {
        if (rules == null) {
            setRules(new ContextSensitiveRules());
        }

        return rules;
    }

    /**
     * 创建<code>SetContextRule</code>, 使用指定attribute的值作为当前context的值.
     * 
     * @param pattern 当前的匹配
     * @param attributeName XML属性名
     */
    public void addSetContextRule(String pattern, String attributeName) {
        addRule(pattern, new SetContextRule(attributeName));
    }

    /**
     * 创建<code>SetContextRule</code>, 使用指定类作为取得context的工厂.
     * 
     * @param pattern 当前的匹配
     * @param contextFactoryClass 工厂类
     */
    public void addSetContextRule(String pattern, Class contextFactoryClass) {
        addRule(pattern, new SetContextRule(contextFactoryClass));
    }

    /**
     * 创建<code>SetContextRule</code>, 使用指定context工厂取得当前context的值.
     * 
     * @param pattern 当前的匹配
     * @param contextFactory 工厂对象
     */
    public void addSetContextRule(String pattern, ContextFactory contextFactory) {
        addRule(pattern, new SetContextRule(contextFactory));
    }

    /**
     * 创建<code>SetRuleSetRule</code>, 使用指定类作为取得<code>RuleSet</code>的工厂.
     * 
     * @param pattern 当前的匹配
     * @param ruleSetFactoryClass 工厂类
     */
    public void addSetRuleSetRule(String pattern, Class ruleSetFactoryClass) {
        addRule(pattern, new SetRuleSetRule(ruleSetFactoryClass));
    }

    /**
     * 创建<code>SetRuleSetRule</code>, 使用指定<code>RuleSet</code>工厂.
     * 
     * @param pattern 当前的匹配
     * @param ruleSetFactory 工厂对象
     */
    public void addSetRuleSetRule(String pattern, RuleSetFactory ruleSetFactory) {
        addRule(pattern, new SetRuleSetRule(ruleSetFactory));
    }
}
