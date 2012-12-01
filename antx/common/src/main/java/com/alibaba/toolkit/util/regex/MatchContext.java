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

package com.alibaba.toolkit.util.regex;

import java.util.Collection;
import java.util.Collections;

import com.alibaba.toolkit.util.collection.Predicate;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;

/**
 * 代表一个匹配的context. 一个context中保存了一次或多次匹配过程中可重复使用的对象, 以及最近一次匹配的结果.
 *
 * @author Michael Zhou
 * @version $Id: MatchContext.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 */
public class MatchContext {
    private PatternMatcher      matcher;
    private PatternMatcherInput input;
    private Substitution        substitution;
    private MatchItem           lastMatchItem;

    /**
     * 创建一个新的context.
     *
     * @param input 要匹配的字符串
     */
    public MatchContext(String input) {
        init(input);
    }

    /**
     * 创建一个新的context.
     *
     * @param input 要匹配的字符串
     */
    public void init(String input) {
        if (this.matcher == null) {
            this.matcher = createPatternMatcher();
        }

        if (this.input == null) {
            this.input = new PatternMatcherInput(input);
        } else {
            this.input.setInput(input);
        }
    }

    /**
     * 取得用于匹配的patterns.
     *
     * @return 所有patterns
     */
    public Collection getPatterns() {
        return Collections.EMPTY_LIST;
    }

    /**
     * 取得用于过滤匹配项的条件, 默认返回<code>null</code>.
     *
     * @return 过滤匹配项的条件
     */
    public Predicate getPredicate() {
        return null;
    }

    /**
     * 取得匹配的输入值.
     *
     * @return 匹配的输入值
     */
    public PatternMatcherInput getInput() {
        return input;
    }

    /**
     * 取得并复位匹配的输入值. 每次匹配动作完成后, 必须调用此方法才可进行第二次匹配.
     *
     * @return input 匹配的输入值
     */
    public PatternMatcherInput getInputReset() {
        input.setCurrentOffset(input.getBeginOffset());
        return input;
    }

    /**
     * 取得pattern匹配器.
     *
     * @return pattern匹配器
     */
    public PatternMatcher getMatcher() {
        return matcher;
    }

    /**
     * 设置最近一次匹配的结果.
     *
     * @param item 最近一次匹配的结果
     */
    public void setLastMatchItem(MatchItem item) {
        this.lastMatchItem = item;
    }

    /**
     * 取得最近一次匹配的结果.
     *
     * @return 最近一次匹配的结果
     */
    public MatchItem getLastMatchItem() {
        return lastMatchItem;
    }

    /**
     * 取得替换对象.
     *
     * @param substitution 替换字符串
     * @return 替换对象
     */
    public Substitution getSubstitution(String substitution) {
        this.substitution = createSubstitution(this.substitution, substitution);

        return this.substitution;
    }

    /**
     * 创建正则表达式的匹配器, 默认为<code>Perl5Matcher</code>, 子类可以改变这个实现.
     *
     * @return 正则表达式的匹配器
     */
    protected PatternMatcher createPatternMatcher() {
        return new Perl5Matcher();
    }

    /**
     * 创建替换器, 默认为<code>Perl5Substitution</code>, 子类可以改变这个实现.
     *
     * @param reuse        可重用的替换器
     * @param substitution 替换字符串
     * @return 替换器
     */
    protected Substitution createSubstitution(Substitution reuse, String substitution) {
        if (reuse == null) {
            return new Perl5Substitution(substitution);
        }

        ((Perl5Substitution) reuse).setSubstitution(substitution);
        return reuse;
    }

    /**
     * 创建匹配结果, 默认为<code>MatchItem</code>, 子类可以改变这个实现.
     *
     * @param pattern 用于匹配的pattern
     * @param result  正则表达式的匹配结果
     * @return 匹配结果项
     */
    protected MatchItem createMatchItem(MatchPattern pattern, MatchResult result) {
        return new MatchItem(this, pattern, result);
    }
}
