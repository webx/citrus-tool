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
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.Rules;
import org.apache.commons.digester.RulesBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 上下文相关的<code>Rules</code>包装器.
 *
 * @author Michael Zhou
 * @version $Id: ContextSensitiveRules.java,v 1.2 2003/08/07 08:08:59 zyh Exp $
 */
public class ContextSensitiveRules implements Rules {
    private static final String CONTEXT_INITIALIZING = "INITIALIZING";
    private static final String CONTEXT_INITIALIZED = "INITIALIZED";
    protected Rules             rules;
    private String              context             = "";
    private StringBuffer        contextBuffer       = new StringBuffer();
    private Map                 contextStatus       = new HashMap();

    /**
     * 创建默认的<code>Rules</code>.
     */
    public ContextSensitiveRules() {
        this(new RulesBase());
    }

    /**
     * 创建指定<code>Rules</code>的包装.
     *
     * @param rules 被包装的<code>Rules</code>
     */
    public ContextSensitiveRules(Rules rules) {
        this.rules = rules;
    }

    /**
     * 压入指定的上下文.
     *
     * @param context 要压入的上下文字符串
     */
    public void pushContext(String context) {
        contextBuffer.append('/').append(context);
        this.context = contextBuffer.toString();
    }

    /**
     * 弹出最新的上下文.
     *
     * @return 最新的上下文
     */
    public String popContext() {
        int    index      = context.lastIndexOf("/");
        String topContext = null;

        if (index >= 0) {
            topContext = contextBuffer.substring(index + 1, contextBuffer.length());
            contextBuffer.setLength(index);
        }

        this.context = contextBuffer.toString();
        return topContext;
    }

    /**
     * 检查指定的context是否被初始化.
     *
     * @param context 要检查的context
     *
     * @return 如果被初始化, 则返回<code>true</code>
     */
    public boolean isInitialized(String context) {
        return CONTEXT_INITIALIZED.equals(contextStatus.get(context));
    }

    /**
     * 设置指定context为初始化中的状态.
     *
     * @param context 要设置的context
     */
    public void setInitializing(String context) {
        contextStatus.put(context, CONTEXT_INITIALIZING);
    }

    /**
     * 设置指定context为已初始化的状态.
     *
     * @param context 要设置的context
     */
    public void setInitialized(String context) {
        contextStatus.put(context, CONTEXT_INITIALIZED);
    }

    /**
     * 取得当前的上下文.
     *
     * @return 当前的上下文
     */
    public String getContext() {
        return context;
    }

    /**
     * 取得digester.
     *
     * @return digester
     */
    public Digester getDigester() {
        return rules.getDigester();
    }

    /**
     * 设置digester.
     *
     * @param digester digester
     */
    public void setDigester(Digester digester) {
        rules.setDigester(digester);
    }

    /**
     * 取得名字空间.
     *
     * @return 名字空间
     */
    public String getNamespaceURI() {
        return rules.getNamespaceURI();
    }

    /**
     * 设置名字空间.
     *
     * @param namespaceURI 名字空间
     */
    public void setNamespaceURI(String namespaceURI) {
        rules.setNamespaceURI(namespaceURI);
    }

    /**
     * 登记规则.
     *
     * @param pattern 匹配模板
     * @param rule 要登记的规则
     */
    public void add(String pattern, Rule rule) {
        if ((context.length() > 0) && !(rule instanceof SetContextRule)
                    && CONTEXT_INITIALIZING.equals(contextStatus.get(context))) {
            rule = new ContextSensitiveRule(rule, context);
        }

        rules.add(pattern, rule);
    }

    /**
     * 清除所有规则.
     */
    public void clear() {
        rules.clear();
    }

    /**
     * 匹配指定模板.
     *
     * @param pattern 匹配模板
     *
     * @return 匹配的规则
     *
     * @deprecated
     */
    public List match(String pattern) {
        return match(null, pattern);
    }

    /**
     * 匹配指定模板.
     *
     * @param namespaceURI 名字空间
     * @param pattern 匹配模板
     *
     * @return 匹配的规则
     */
    public List match(String namespaceURI, String pattern) {
        List list    = rules.match(namespaceURI, pattern);
        List sublist = new ArrayList(list.size());

        for (Iterator i = list.iterator(); i.hasNext();) {
            Rule rule = (Rule) i.next();

            if (!(rule instanceof ContextSensitiveRule)
                        || ((ContextSensitiveRule) rule).isContextMatched(context)) {
                sublist.add(rule);
            }
        }

        return sublist;
    }

    /**
     * 取得所有规则.
     *
     * @return 所有规则
     */
    public List rules() {
        return rules.rules();
    }

    /**
     * 取得<code>Rules</code>的字符串表示.
     *
     * @return <code>Rules</code>的字符串表示
     */
    public String toString() {
        return rules.toString();
    }
}
