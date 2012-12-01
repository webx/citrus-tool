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

package com.alibaba.antx.config.descriptor.validator;

import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.config.descriptor.ConfigValidatorException;
import com.alibaba.antx.util.StringUtil;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexpValidator extends ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(RegexpValidator.class);
    private String regexp;
    private String mode;

    @Override
    public Logger getLogger() {
        return log;
    }

    protected String getRegexp() {
        return regexp;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    protected String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public boolean validate(String value) {
        if (StringUtil.isEmpty(getRegexp())) {
            throw new ConfigValidatorException("You must define an attribute called 'regexp' for regexp validator");
        }

        if (StringUtil.isEmpty(getMode())) {
            setMode("contain");
        }

        if (value == null) {
            return true;
        }

        value = value.trim();

        if (StringUtil.isEmpty(value)) {
            return true;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Validating value with regexp[" + getRegexp() + "]: " + value);
        }

        Pattern pattern = null;

        try {
            pattern = new Perl5Compiler().compile(getRegexp(), Perl5Compiler.READ_ONLY_MASK
                                                               | Perl5Compiler.SINGLELINE_MASK);
        } catch (MalformedPatternException e) {
            throw new ConfigValidatorException(e);
        }

        if (!getMode().endsWith("contain") && !getMode().endsWith("exact") && !getMode().endsWith("prefix")) {
            throw new ConfigValidatorException("Invalid regexp mode: " + getMode()
                                               + ", should be contain, exact or prefix.");
        }

        // 匹配
        PatternMatcher matcher = new Perl5Matcher();
        boolean not = getMode().startsWith("!");

        if (getMode().endsWith("contain")) {
            boolean match = matcher.contains(value, pattern);

            return not ? !match : match;
        } else if (getMode().endsWith("exact")) {
            boolean match = matcher.matches(value, pattern);

            return not ? !match : match;
        } else if (getMode().endsWith("prefix")) {
            boolean match = matcher.matchesPrefix(value, pattern);

            return not ? !match : match;
        }

        return false;
    }

    @Override
    protected String getDefaultMessage() {
        return "参数值必须符合正则表达式：" + getRegexp();
    }
}
