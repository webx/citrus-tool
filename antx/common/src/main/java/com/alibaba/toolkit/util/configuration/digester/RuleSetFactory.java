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
import org.apache.commons.digester.RuleSet;

import org.xml.sax.Attributes;

/**
 * 取得上下文相关的<code>RuleSet</code>的工厂.
 *
 * @version $Id: RuleSetFactory.java,v 1.1 2003/07/03 07:26:16 baobao Exp $
 * @author Michael Zhou
 */
public interface RuleSetFactory {
    /**
     * 取得<code>RuleSet</code>.
     *
     * @param attributes  XML属性
     *
     * @return <code>RuleSet</code>
     *
     * @throws Exception 如果失败
     */
    RuleSet getRuleSet(Attributes attributes) throws Exception;

    /**
     * 设置digester.
     *
     * @param digester 当前digester
     */
    void setDigester(Digester digester);

    /**
     * 取得当前digester.
     *
     * @return 当前digester
     */
    Digester getDigester();
}
