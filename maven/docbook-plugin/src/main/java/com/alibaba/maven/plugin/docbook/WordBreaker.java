package com.alibaba.maven.plugin.docbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.Locale;

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
    private final File dir;
    private final File srcfile;
    private final File destfile;

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

                int j;
                for (int i = 0; (j = breaker.next()) >= 0; i = j) {
                    buf.append(content.substring(i, j));

                    if (j < content.length()) {
                        buf.append('\u200B');
                    }
                }

                return buf.toString();
            }
        });
    }

    private abstract class Filter extends DefaultHandler {
        private OutputStream out = new FileOutputStream(destfile);
        private TransformerHandler handler;

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
            handler.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
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
    }
}
