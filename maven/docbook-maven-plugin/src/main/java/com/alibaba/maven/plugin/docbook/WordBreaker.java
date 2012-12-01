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

package com.alibaba.maven.plugin.docbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 解析XML，在words中间插入隐藏的空白（<code>U+200B</code>）。
 *
 * @author Michael Zhou
 */
public class WordBreaker {
    private final Locale locale;
    private final File   dir;
    private final File   srcfile;
    private final File   destfile;

    public WordBreaker(File srcfile, Locale locale) {
        this.locale = locale;
        this.srcfile = srcfile;
        this.dir = srcfile.getParentFile();
        this.destfile = new File(dir, srcfile.getName() + ".tmp.xml");
    }

    public void filter() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);
        spf.setValidating(false);

        SAXParser saxParser = spf.newSAXParser();

        saxParser.parse(srcfile, new Filter() {
            @Override
            protected String filter(String content) {
                BreakIterator breaker = BreakIterator.getWordInstance(locale);

                breaker.setText(content);

                StringBuilder buf = new StringBuilder(content.length() * 2);
                boolean preserveSpaces = "preserve".equalsIgnoreCase(getAttribute("", "white-space-treatment"));

                int j;
                for (int i = 0; (j = breaker.next()) >= 0; i = j) {
                    buf.append(content.substring(i, j));

                    // 如果preserveSpaces时，不插入空白，fop处理不好。
                    if (!preserveSpaces && j > i) {
                        int c1 = content.charAt(j - 1);
                        int type1 = Character.getType(c1);

                        // 在左括弧的后面，不要插入空白。
                        if (type1 == Character.START_PUNCTUATION) {
                            continue;
                        }

                        if (j < content.length()) {
                            int c2 = content.charAt(j);
                            int type2 = Character.getType(c2);

                            // 如果被空白包围，则不插入空白。
                            if (!Character.isWhitespace(c1) && Character.isWhitespace(c2)) {
                                continue;
                            }

                            // 在标点符号、右括弧的前面，不要插入空白。
                            if (type2 == Character.OTHER_PUNCTUATION || type2 == Character.END_PUNCTUATION) {
                                continue;
                            }

                            // 如果前后两字符属于不同的unicode block，则插入空白。
                            if (Character.UnicodeBlock.of(c1) != Character.UnicodeBlock.of(c2)) {
                                buf.append('\u200B');
                                continue;
                            }
                        }

                        // 在标点符号的后面，插入空白。
                        if (Character.OTHER_PUNCTUATION == Character.getType(c1) && c1 != '/' && c1 != '|') {
                            buf.append('\u200B');
                        }
                    }
                }

                return buf.toString();
            }
        });
    }

    private abstract class Filter extends DefaultHandler {
        private OutputStream out = new FileOutputStream(destfile);
        private TransformerHandler handler;
        private AttributesStack attributesStack = new AttributesStack();

        public Filter() throws Exception {
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();

            handler = tf.newTransformerHandler();
            Transformer serializer = handler.getTransformer();

            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "no");

            OutputStream out = new FileOutputStream(destfile);
            StreamResult streamResult = new StreamResult(out);

            handler.setResult(streamResult);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String content = new String(ch, start, length);
            String filtered = filter(content);

            if (content == filtered || filtered == null) {
                handler.characters(ch, start, length);
            } else {
                handler.characters(filtered.toCharArray(), 0, filtered.length());
            }
        }

        protected abstract String filter(String content);

        protected final String getAttribute(String uri, String localName) {
            return attributesStack.getAttributeValue(uri, localName);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            handler.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();

            try {
                out.close();
            } catch (IOException e) {
            }

            destfile.renameTo(srcfile);
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            handler.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            handler.endPrefixMapping(prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            attributesStack.pushAttributes(atts);
            handler.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            attributesStack.popAttributes();
            handler.endElement(uri, localName, qName);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            handler.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            handler.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            handler.skippedEntity(name);
        }

        private class AttributesStack {
            private final LinkedList<Map<String, String>> stack = new LinkedList<Map<String, String>>();

            public void pushAttributes(Attributes attrs) {
                Map<String, String> keyValues = new HashMap<String, String>();

                for (int i = 0; i < attrs.getLength(); i++) {
                    keyValues.put(getQname(attrs.getURI(i), attrs.getLocalName(i)), attrs.getValue(i));
                }

                stack.addFirst(keyValues);
            }

            private String getQname(String uri, String localName) {
                String qname;

                if (uri == null || uri.length() == 0) {
                    qname = localName;
                } else {
                    qname = uri + "#" + localName;
                }
                return qname;
            }

            public void popAttributes() {
                stack.removeFirst();
            }

            public String getAttributeValue(String uri, String localName) {
                String qname = getQname(uri, localName);

                for (Map<String, String> attrs : stack) {
                    if (attrs.containsKey(qname)) {
                        return attrs.get(qname);
                    }
                }

                return null;
            }
        }
    }
}
