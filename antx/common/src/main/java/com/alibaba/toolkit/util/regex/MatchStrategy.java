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

package com.alibaba.toolkit.util.regex;

/**
 * 匹配策略.
 * 
 * @version $Id: MatchStrategy.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 * @author Michael Zhou
 */
public interface MatchStrategy {
    /** 最佳匹配策略, 总是试图匹配最长的一项. 如果有多项具有相同的匹配长度, 则返回第一个匹配项. */
    MatchStrategy BEST_MATCH_STRATEGY = new BestMatchStrategy();

    /**
     * 试图匹配指定的输入值, 如果成功, 则返回<code>true</code>. 调用者可以通过
     * <code>context.getMatchItem()</code>来取得匹配项.
     * 
     * @param context 匹配上下文
     * @return 如果匹配成功, 则返回<code>true</code>
     */
    boolean matches(MatchContext context);
}
