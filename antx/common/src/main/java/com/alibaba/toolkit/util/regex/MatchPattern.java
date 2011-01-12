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
 *
 */
package com.alibaba.toolkit.util.regex;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;

/**
 * 表示一个用于匹配的pattern.
 *
 * @version $Id: MatchPattern.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 * @author Michael Zhou
 */
public class MatchPattern {
    private Pattern pattern;

    /**
     * 创建新的pattern.
     */
    public MatchPattern() {
    }

    /**
     * 创建新的pattern.
     *
     * @param pattern 用于匹配正则表达式的pattern
     */
    public MatchPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * 创建新的pattern.
     *
     * @param compiler pattern编译器
     * @param pattern  用于匹配正则表达式的pattern
     *
     * @throws MalformedPatternException 如果pattern不合法
     */
    public MatchPattern(PatternCompiler compiler, String pattern)
            throws MalformedPatternException {
        this.pattern = compiler.compile(pattern);
    }

    /**
     * 创建新的pattern.
     *
     * @param compiler pattern编译器
     * @param pattern  用于匹配正则表达式的pattern
     * @param options  编译器选项
     *
     * @throws MalformedPatternException 如果pattern不合法
     */
    public MatchPattern(PatternCompiler compiler, String pattern, int options)
            throws MalformedPatternException {
        this.pattern = compiler.compile(pattern, options);
    }

    /**
     * 设置pattern.
     *
     * @param pattern 用于匹配正则表达式的pattern
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * 取得用于匹配正则表达式的pattern.
     *
     * @return 匹配正则表达式的pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * 匹配pattern, 如果成功, 则返回<code>true</code>.  调用者可以通过<code>context.getMatchItem()</code>来取得匹配项.
     *
     * @param context  匹配上下文
     *
     * @return 如果匹配成功, 则返回<code>true</code>
     */
    public boolean matches(MatchContext context) {
        PatternMatcher matcher = context.getMatcher();

        if (matcher.contains(context.getInputReset(), pattern)) {
            MatchItem item = context.createMatchItem(this, matcher.getMatch());

            context.setLastMatchItem(item);
            return true;
        }

        return false;
    }
}
