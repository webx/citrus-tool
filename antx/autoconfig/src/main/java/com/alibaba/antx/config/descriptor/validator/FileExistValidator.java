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
package com.alibaba.antx.config.descriptor.validator;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.util.StringUtil;

public class FileExistValidator extends ConfigValidator {
    private static final Logger log = LoggerFactory.getLogger(FileExistValidator.class);
    private String filename;
    private File file;

    @Override
    public Logger getLogger() {
        return log;
    }

    public void setFile(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean validate(String value) {
        if (value == null) {
            return true;
        }

        value = value.trim();

        if (StringUtil.isEmpty(value)) {
            return true;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Validating file or directory: " + value);
        }

        if (filename == null) {
            file = new File(value);
        } else {
            file = new File(value, filename);
        }

        return file.isAbsolute() && file.exists();
    }

    @Override
    protected String getDefaultMessage() {
        return "文件或目录不存在，或不是绝对路径：" + file;
    }
}
