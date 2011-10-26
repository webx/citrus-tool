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

import org.xml.sax.Attributes;

/**
 * 上下文相关的规则的包装器.
 *
 * @version $Id: ContextSensitiveRule.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 * @author Michael Zhou
 */
public class ContextSensitiveRule extends Rule {
    protected Rule   rule;
    protected String context;

    /**
     * 包装指定的规则, 和指定的上下文对应.
     *
     * @param rule     规则
     * @param context  上下文字符串
     */
    public ContextSensitiveRule(Rule rule, String context) {
        this.rule    = rule;
        this.context = context;
    }

    /**
     * 判断当前上下文是否匹配.
     *
     * @param context  被匹配的上下文
     *
     * @return 如果匹配, 则返回<code>true</code>
     */
    public boolean isContextMatched(String context) {
        return this.context.equals(context);
    }

    /**
     * 取得digester.
     *
     * @return digester
     */
    public Digester getDigester() {
        return rule.getDigester();
    }

    /**
     * 设置digester.
     *
     * @param digester digester
     */
    public void setDigester(Digester digester) {
        rule.setDigester(digester);
    }

    /**
     * 取得名字空间.
     *
     * @return 名字空间
     */
    public String getNamespaceURI() {
        return rule.getNamespaceURI();
    }

    /**
     * 设置名字空间.
     *
     * @param namespaceURI 名字空间
     */
    public void setNamespaceURI(String namespaceURI) {
        rule.setNamespaceURI(namespaceURI);
    }

    /**
     * 匹配开始.
     *
     * @param namespace  名字空间
     * @param name       XML元素local名
     * @param attributes XML属性
     *
     * @throws Exception 如果失败
     */
    public void begin(String namespace, String name, Attributes attributes)
            throws Exception {
        rule.begin(namespace, name, attributes);
    }

    /**
     * 匹配主体部分.
     *
     * @param namespace  名字空间
     * @param name       XML元素local名
     * @param text       XML元素值
     *
     * @throws Exception 如果失败
     */
    public void body(String namespace, String name, String text)
            throws Exception {
        rule.body(namespace, name, text);
    }

    /**
     * 匹配结束.
     *
     * @param namespace  名字空间
     * @param name       XML元素local名
     *
     * @throws Exception 如果失败
     */
    public void end(String namespace, String name)
            throws Exception {
        rule.end(namespace, name);
    }

    /**
     * 清除环境.
     *
     * @throws Exception 如果失败
     */
    public void finish() throws Exception {
        rule.finish();
    }

    /**
     * 取得规则的字符串表示.
     *
     * @return 规则的字符串表示
     */
    public String toString() {
        return rule.toString();
    }
}
