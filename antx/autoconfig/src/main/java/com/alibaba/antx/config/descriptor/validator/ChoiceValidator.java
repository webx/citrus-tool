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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChoiceValidator extends ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(ChoiceValidator.class);
    private String[] choices;

    @Override
    public Logger getLogger() {
        return log;
    }

    public String[] getAllChoices() {
        return choices;
    }

    public void setChoice(String choice) {
        choices = StringUtil.split(choice);
    }

    @Override
    public boolean validate(String value) {
        String[] choices = getAllChoices();

        if (choices == null || choices.length == 0) {
            throw new ConfigValidatorException("You must define an attribute called 'choice' for choice validator");
        }

        if (value == null) {
            return true;
        }

        value = value.trim();

        if (StringUtil.isEmpty(value)) {
            return true;
        }

        if (getLogger().isDebugEnabled()) {
            StringBuffer buffer = new StringBuffer();

            buffer.append("Validating value with choice[");

            for (int i = 0; i < choices.length; i++) {
                if (i > 0) {
                    buffer.append(", ");
                }

                buffer.append(choices[i]);
            }

            buffer.append("]: ").append(value);

            getLogger().debug(buffer.toString());
        }

        for (String choice : choices) {
            if (value.equals(choice)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected String getDefaultMessage() {
        StringBuffer buffer = new StringBuffer("您必须在下列值中选择：");
        String[] choices = getAllChoices();

        for (int i = 0; i < choices.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }

            buffer.append(choices[i]);
        }

        return buffer.toString();
    }
}
