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

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternMatcherInput;

/**
 * 代表一个匹配结果, 通过这个结果, 可以做进一步的操作, 如替换, 取得匹配字符串等.
 *
 * @version $Id: MatchItem.java,v 1.1 2003/07/03 07:26:34 baobao Exp $
 * @author Michael Zhou
 */
public class MatchItem implements MatchResult {
    public static final int SUBSTITUTION_ONLY           = 0;
    public static final int SUBSTITUTION_WITH_PREMATCH  = 1;
    public static final int SUBSTITUTION_WITH_POSTMATCH = 2;
    private MatchContext    context;
    private MatchPattern    pattern;
    private MatchResult     result;

    /**
     * 创建一个匹配结果.
     *
     * @param context 产生这个匹配结果的context
     * @param pattern 产生这个匹配结果的pattern
     */
    public MatchItem(MatchContext context, MatchPattern pattern) {
        this.context = context;
        this.pattern = pattern;
    }

    /**
     * 创建一个匹配结果.
     *
     * @param context      产生这个匹配结果的context
     * @param pattern      产生这个匹配结果的pattern
     * @param result       正则表达式的匹配结果
     */
    public MatchItem(MatchContext context, MatchPattern pattern, MatchResult result) {
        this(context, pattern);
        this.result = result;
    }

    /**
     * 取得产生这个匹配结果的context.
     *
     * @return 产生这个匹配结果的context
     */
    public MatchContext getMatchContext() {
        return this.context;
    }

    /**
     * 取得产生这个匹配结果的pattern.
     *
     * @return 产生这个匹配结果的pattern
     */
    public MatchPattern getMatchPattern() {
        return this.pattern;
    }

    /**
     * 取得完整的输入值字符串.
     *
     * @return 完整的输入值字符串
     */
    public String getInput() {
        return (String) this.context.getInput().getInput();
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得匹配长度.
     *
     * @return 匹配的长度
     */
    public int length() {
        return (result == null) ? 0
                                : result.length();
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得group的总数.
     *
     * @return group的总数, 包括group0, 也就是整个匹配
     */
    public int groups() {
        return (result == null) ? 0
                                : result.groups();
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得指定group的子串.
     *
     * @param group  group号, 0代表整个匹配
     *
     * @return 指定group的子串
     */
    public String group(int group) {
        return (result == null) ? null
                                : result.group(group);
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得指定group相对于整个匹配的位移量.
     *
     * @param group group号, 0代表整个匹配
     *
     * @return 指定group相对于整个匹配的位移量, 注意如果被匹配的字符串长度为0, 且位于字符串的末尾, 则位移量等于字符串的长度.
     */
    public int begin(int group) {
        return (result == null) ? (-1)
                                : result.begin(group);
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得指定group末尾相对于整个匹配的位移量.
     *
     * @param group group号, 0代表整个匹配
     *
     * @return 指定group末尾相对于整个匹配的位移量, 如果指定group不存在或未匹配, 则返回-1, 被匹配的字符串长度为0, 则返回起始位移量
     */
    public int end(int group) {
        return (result == null) ? (-1)
                                : result.end(group);
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得指定group相对于整个字符串的位移量.
     *
     * @param group group号, 0代表整个匹配
     *
     * @return 指定group相对于整个字符串的位移量, 如果指定group不存在或未匹配, 则返回-1
     */
    public int beginOffset(int group) {
        return (result == null) ? (-1)
                                : result.beginOffset(group);
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得指定group末尾相对于整个字符串的位移量.
     *
     * @param group group号, 0代表整个匹配
     *
     * @return 指定group末尾相对于整个字符串的位移量, 如果指定group不存在或未匹配, 则返回-1, 被匹配的字符串长度为0, 则返回起始位移量
     */
    public int endOffset(int group) {
        return (result == null) ? (-1)
                                : result.endOffset(group);
    }

    /**
     * 实现<code>MatchResult</code>接口, 取得整个匹配的字符串, 相当于<code>group(0)</code>.
     *
     * @return 整个匹配的字符串
     */
    public String toString() {
        return (result == null) ? ""
                                : result.toString();
    }

    /**
     * 将匹配字符串前的子串, 加到指定<code>StringBuffer</code>中.
     *
     * @param buffer  要添加的<code>StringBuffer</code>
     */
    public void appendPreMatch(StringBuffer buffer) {
        PatternMatcherInput input       = context.getInput();
        char[]              inputBuffer = input.getBuffer();
        int                 beginOffset = input.getBeginOffset();

        buffer.append(inputBuffer, beginOffset, beginOffset(0) - beginOffset);
    }

    /**
     * 将匹配字符串后的子串, 加到指定<code>StringBuffer</code>中.
     *
     * @param buffer  要添加的<code>StringBuffer</code>
     */
    public void appendPostMatch(StringBuffer buffer) {
        PatternMatcherInput input       = context.getInput();
        char[]              inputBuffer = input.getBuffer();
        int                 beginOffset = endOffset(0);

        buffer.append(inputBuffer, beginOffset, input.length() - beginOffset);
    }

    /**
     * 将匹配字符串, 加到指定<code>StringBuffer</code>中.
     *
     * @param buffer  要添加的<code>StringBuffer</code>
     */
    public void appendMatch(StringBuffer buffer) {
        PatternMatcherInput input       = context.getInput();
        char[]              inputBuffer = input.getBuffer();
        int                 beginOffset = beginOffset(0);

        buffer.append(inputBuffer, beginOffset, endOffset(0));
    }

    /**
     * 将替换字符串加入到指定<code>StringBuffer</code>中.
     *
     * @param buffer        要添加的<code>StringBuffer</code>
     * @param substitution  替换表达式
     */
    public void appendSubstitution(StringBuffer buffer, String substitution) {
        context.getSubstitution(substitution)
               .appendSubstitution(buffer, this, 1, context.getInput(), context.getMatcher(),
                                   pattern.getPattern());
    }

    /**
     * 替换匹配的字符串.
     *
     * @param substitution 替换字符串
     *
     * @return 被替换的字符串
     */
    public String substitute(String substitution) {
        return substitute(substitution, SUBSTITUTION_ONLY);
    }

    /**
     * 替换匹配的字符串.
     *
     * @param substitution 替换字符串
     * @param options      替换选项, 可以为<code>SUBSTITUTION_ONLY</code>,
     *        <code>SUBSTITUTION_WITH_PREMATCH</code>或<code>SUBSTITUTION_WITH_POSTMATCH</code>或它们的组合
     *
     * @return 被替换的字符串
     */
    public String substitute(String substitution, int options) {
        StringBuffer buffer = new StringBuffer();

        if ((options & SUBSTITUTION_WITH_PREMATCH) != 0) {
            appendPreMatch(buffer);
        }

        appendSubstitution(buffer, substitution);

        if ((options & SUBSTITUTION_WITH_POSTMATCH) != 0) {
            appendPostMatch(buffer);
        }

        return buffer.toString();
    }
}
