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
package com.alibaba.antx.config.descriptor;

public class ConfigGenerate {
    private ConfigDescriptor descriptor;
    private String templateBase;
    private String template;
    private String destfile;
    private String charset;
    private String outputCharset;

    public ConfigDescriptor getConfigDescriptor() {
        return descriptor;
    }

    public void setConfigDescriptor(ConfigDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getDestfile() {
        return destfile;
    }

    public void setDestfile(String destfile) {
        this.destfile = destfile;
    }

    public String getOutputCharset() {
        return outputCharset;
    }

    public void setOutputCharset(String outputCharset) {
        this.outputCharset = outputCharset;
    }

    public String getTemplateBase() {
        return templateBase;
    }

    public void setTemplateBase(String templateBase) {
        this.templateBase = templateBase;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String toString() {
        return "Generate[" + getTemplate() + " => " + getDestfile() + "]";
    }
}
