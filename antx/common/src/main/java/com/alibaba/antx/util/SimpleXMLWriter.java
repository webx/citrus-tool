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

package com.alibaba.antx.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * 代表一个简单的XML writer，不支持名字空间。
 * 
 * @author Michael Zhou
 */
public class SimpleXMLWriter extends XMLWriter {
    /**
     * 创建一个XML writer。
     * 
     * @param file XML文件
     * @throws IOException 文件打开失败
     */
    public SimpleXMLWriter(File file) throws IOException {
        super(new FileOutputStream(file), OutputFormat.createPrettyPrint());
    }

    /**
     * 开始一个XML element。
     * 
     * @param elementName element名
     * @throws SAXException SAX异常
     */
    public void startElement(String elementName) throws SAXException {
        startElement("", "", elementName, new AttributesImpl());
    }

    /**
     * 开始一个XML element。
     * 
     * @param elementName element名
     * @param attrName 属性名
     * @param attrValue 属性值
     * @throws SAXException SAX异常
     */
    public void startElement(String elementName, String attrName, String attrValue) throws SAXException {
        if (attrValue == null) {
            attrValue = "";
        }

        AttributesImpl attrs = new AttributesImpl();

        attrs.addAttribute("", "", attrName, "CDATA", attrValue);

        startElement("", "", elementName, attrs);
    }

    /**
     * 开始一个XML element。
     * 
     * @param elementName element名
     * @param attrName1 属性名
     * @param attrValue1 属性值
     * @param attrName2 属性名
     * @param attrValue2 属性值
     * @throws SAXException SAX异常
     */
    public void startElement(String elementName, String attrName1, String attrValue1, String attrName2,
                             String attrValue2) throws SAXException {
        if (attrValue1 == null) {
            attrValue1 = "";
        }

        if (attrValue2 == null) {
            attrValue2 = "";
        }

        AttributesImpl attrs = new AttributesImpl();

        attrs.addAttribute("", "", attrName1, "CDATA", attrValue1);
        attrs.addAttribute("", "", attrName2, "CDATA", attrValue2);

        startElement("", "", elementName, attrs);
    }

    /**
     * 创建一个XML element。
     * 
     * @param elementName element名
     * @param bodyText element值
     * @throws SAXException SAX异常
     */
    public void processElement(String elementName, String bodyText) throws SAXException {
        if (StringUtil.isEmpty(bodyText)) {
            return;
        }

        startElement("", "", elementName, new AttributesImpl());
        characters(bodyText.toCharArray(), 0, bodyText.length());
        endElement("", "", elementName);
    }

    /**
     * 结束一个XML element。
     * 
     * @param elementName element名
     * @throws SAXException SAX异常
     */
    public void endElement(String elementName) throws SAXException {
        endElement("", "", elementName);
    }
}
