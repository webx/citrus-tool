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

package com.alibaba.antx.config.resource.util;

import java.io.PrintWriter;

import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;
import org.w3c.tidy.Tidy;

import com.alibaba.antx.config.resource.Resource;
import com.alibaba.antx.util.ByteArrayOutputStream;

public abstract class TextBasedPageParser implements IndexPageParser {
    private String overridingCharset;

    public TextBasedPageParser() {
    }

    public TextBasedPageParser(String overridingCharset) {
        setOverridingCharset(overridingCharset);
    }

    public void setOverridingCharset(String charset) {
        this.overridingCharset = charset;
    }

    /**
     * 取得xml文档。
     */
    protected Document getXmlDocument(Resource resource) {
        String contentType = resource.getContentType();

        if (contentType != null && contentType.startsWith("text/xml")) {
            try {
                return new SAXReader().read(resource.getInputStream());
            } catch (Exception e) {
            }
        }

        return null;
    }

    /**
     * 取得html文档。
     */
    protected Document getHtmlDocument(Resource resource) {
        String contentType = resource.getContentType();

        if (contentType != null && contentType.startsWith("text/html")) {
            try {
                Tidy tidy = new Tidy();
                tidy.setQuiet(true);
                tidy.setXmlOut(true);
                tidy.setErrout(new PrintWriter(new ByteArrayOutputStream()));

                org.w3c.dom.Document dom = tidy.parseDOM(resource.getInputStream(), null);

                return new DOMReader().read(dom);
            } catch (Exception e) {
            }
        }

        return null;
    }

    protected String getCharset(Resource resource) {
        if (overridingCharset != null) {
            return overridingCharset;
        } else if (resource.getCharset() != null) {
            return resource.getCharset();
        } else {
            return "ISO-8859-1";
        }
    }

    /**
     * 根据名字取得item。
     */
    protected Item getItem(String name) {
        if (name == null) {
            return null;
        }

        boolean directory = name.endsWith("/");

        if (directory) {
            name = name.substring(0, name.length() - 1);
        }

        if (name.length() == 0 || name.indexOf("/") >= 0 || name.startsWith("?") || name.equals(".")
                || name.equals("..")) {
            return null;
        }

        return new Item(name, directory);
    }
}
