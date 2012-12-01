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

package com.alibaba.antx.config.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.alibaba.antx.config.descriptor.ConfigGenerate;

public class LazyGenerateItem {
    private final String               templateName;
    private final List<ConfigGenerate> generates;
    private final byte[]               savedTemplateContent;

    public LazyGenerateItem(String templateName, List<ConfigGenerate> generates, byte[] savedTemplateContent) {
        this.templateName = templateName;
        this.generates = generates;
        this.savedTemplateContent = compress(savedTemplateContent);
    }

    private byte[] compress(byte[] bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);

        try {
            dos.write(bytes);
            dos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                dos.close();
            } catch (IOException e) {
            }
        }

        return baos.toByteArray();
    }

    public InputStream getTemplateContentStream() {
        return new InflaterInputStream(new ByteArrayInputStream(savedTemplateContent));
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<ConfigGenerate> getGenerates() {
        return generates;
    }
}
