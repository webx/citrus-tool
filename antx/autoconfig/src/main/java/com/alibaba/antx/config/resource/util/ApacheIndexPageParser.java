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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import com.alibaba.antx.config.resource.Resource;

public class ApacheIndexPageParser extends TextBasedPageParser {
    public ApacheIndexPageParser() {
        super();
    }

    public ApacheIndexPageParser(String overridingCharset) {
        super(overridingCharset);
    }

    public List parse(Resource resource) {
        Document doc = getHtmlDocument(resource);

        if (doc != null) {
            Node title = doc.selectSingleNode("//head/title");

            if (title != null && title.getText() != null && title.getText().startsWith("Index of")) {
                List nodes = doc
                        .selectNodes("//pre/img[starts-with(@alt,'[') and ends-with(@alt,']')]/following::a/@href");
                List items = new ArrayList(nodes.size());

                for (Iterator i = nodes.iterator(); i.hasNext();) {
                    Node node = (Node) i.next();
                    String name = node.getText();

                    try {
                        name = URLDecoder.decode(name, getCharset(resource));
                    } catch (UnsupportedEncodingException e) {
                    }

                    Item item = getItem(name);

                    if (item != null) {
                        items.add(item);
                    }
                }

                return items;
            }
        }

        return null;
    }
}
