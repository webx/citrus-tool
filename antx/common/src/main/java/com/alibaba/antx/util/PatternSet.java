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
package com.alibaba.antx.util;

import com.alibaba.toolkit.util.collection.ArrayHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 代表includes、excludes的pattern集合。
 *
 * @author Michael Zhou
 */
public class PatternSet {
    private String[] includes;
    private String[] excludes;

    public PatternSet() {
        this(new String[0], new String[0]);
    }

    public PatternSet(String includes) {
        this(StringUtil.split(includes), new String[0]);
    }

    public PatternSet(String includes, String excludes) {
        this(StringUtil.split(includes), StringUtil.split(excludes));
    }

    public PatternSet(String[] includes, String[] excludes) {
        this.includes = normalizePatterns(includes);
        this.excludes = normalizePatterns(excludes);
    }

    public PatternSet(PatternSet patterns, PatternSet defaultPatterns) {
        if ((patterns == null) || patterns.isEmpty()) {
            patterns = defaultPatterns;

            if (patterns == null) {
                patterns = new PatternSet();
            }
        }

        this.includes = patterns.includes;
        this.excludes = patterns.excludes;
    }

    /**
     * 将所有pattern规格化成：无/前缀/后缀，以/分隔。
     */
    private static String[] normalizePatterns(String[] patterns) {
        if (patterns == null) {
            return new String[0];
        }

        List patternList = new ArrayList(patterns.length);

        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];

            if (pattern == null) {
                continue;
            } else {
                pattern = pattern.trim().replace('\\', '/');
            }

            int startIndex = 0;
            int endIndex   = pattern.length();

            while ((startIndex < pattern.length()) && (pattern.charAt(startIndex) == '/')) {
                startIndex++;
            }

            while ((endIndex > 0) && (pattern.charAt(endIndex - 1) == '/')) {
                endIndex--;
            }

            if ((startIndex > 0) || (endIndex < pattern.length())) {
                pattern = pattern.substring(startIndex, endIndex);
            }

            if (pattern.length() > 0) {
                patternList.add(pattern);
            }
        }

        return (String[]) patternList.toArray(new String[patternList.size()]);
    }

    /**
     * 排除指定的文件。
     */
    public PatternSet addExcludes(String[] addedExcludes) {
        Set excludeSet = new ArrayHashSet();

        for (int i = 0; i < excludes.length; i++) {
            excludeSet.add(excludes[i]);
        }

        for (int i = 0; i < addedExcludes.length; i++) {
            excludeSet.add(addedExcludes[i]);
        }

        excludes = (String[]) excludeSet.toArray(new String[excludeSet.size()]);

        return this;
    }

    /**
     * 排除默认的文件。
     */
    public PatternSet addDefaultExcludes() {
        return addExcludes(FileUtil.DEFAULT_EXCLUDES);
    }

    /**
     * 取得include patterns
     */
    public String[] getIncludes() {
        return includes;
    }

    /**
     * 取得exclude patterns
     */
    public String[] getExcludes() {
        return excludes;
    }

    /**
     * 是否为空。
     */
    public boolean isEmpty() {
        return (includes.length == 0) && (excludes.length == 0);
    }

    /**
     * 是否包含所有。
     */
    public boolean isIncludeAll() {
        if (isExcludeAll()) {
            return false;
        }

        return (includes.length == 1) && "**".equals(includes[0]) && (excludes.length == 0);
    }

    /**
     * 是否排除所有。
     */
    public boolean isExcludeAll() {
        return (excludes.length == 1) && "**".equals(excludes[0]);
    }

    /**
     * 转换成字符串形式。
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // includes
        buffer.append("includes[");

        for (int i = 0; i < includes.length; i++) {
            String include = includes[i];

            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append(include);
        }

        buffer.append("]");

        // excludes
        buffer.append(", excludes[");

        for (int i = 0; i < excludes.length; i++) {
            String exclude = excludes[i];

            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append(exclude);
        }

        buffer.append("]");

        return buffer.toString();
    }
}
