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

package com.alibaba.antx.util.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A ConfigurationSerializer serializes configurations via SAX2 compliant
 * parser.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultConfigurationSerializer {
    private SAXTransformerFactory m_tfactory;
    private Properties m_format = new Properties();

    /**
     * Sets the Serializer's use of indentation. This will cause linefeeds to be
     * added after each element, but it does not add any indentation via spaces.
     *
     * @param indent a <code>boolean</code> value
     */
    public void setIndent(boolean indent) {
        if (indent) {
            m_format.put(OutputKeys.INDENT, "yes");
        } else {
            m_format.put(OutputKeys.INDENT, "no");
        }
    }

    /**
     * Create a ContentHandler for an OutputStream
     *
     * @param result the result
     * @return contenthandler that goes to specified OutputStream
     */
    protected ContentHandler createContentHandler(final Result result) {
        try {
            TransformerHandler handler = getTransformerFactory().newTransformerHandler();

            m_format.put(OutputKeys.METHOD, "xml");
            handler.setResult(result);
            handler.getTransformer().setOutputProperties(m_format);

            return handler;
        } catch (final Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Get the SAXTransformerFactory so we can get a serializer without being
     * tied to one vendor.
     *
     * @return a <code>SAXTransformerFactory</code> value
     */
    protected SAXTransformerFactory getTransformerFactory() {
        if (m_tfactory == null) {
            m_tfactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        }

        return m_tfactory;
    }

    /**
     * Serialize the configuration to a ContentHandler
     *
     * @param handler a <code>ContentHandler</code> to serialize to
     * @param source  a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void serialize(final ContentHandler handler, final Configuration source) throws SAXException,
                                                                                           ConfigurationException {
        handler.startDocument();
        serializeElement(handler, new NamespaceSupport(), source);
        handler.endDocument();
    }

    /**
     * Serialize each Configuration element. This method is called recursively.
     *
     * @param handler          a <code>ContentHandler</code> to use
     * @param namespaceSupport a <code>NamespaceSupport</code> to use
     * @param element          a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    protected void serializeElement(final ContentHandler handler, final NamespaceSupport namespaceSupport,
                                    final Configuration element) throws SAXException, ConfigurationException {
        namespaceSupport.pushContext();

        AttributesImpl attr = new AttributesImpl();
        String[] attrNames = element.getAttributeNames();

        if (null != attrNames) {
            for (String attrName : attrNames) {
                attr.addAttribute("", // namespace URI
                                  attrName, // local name
                                  attrName, // qName
                                  "CDATA", // type
                                  element.getAttribute(attrName, "") // value
                );
            }
        }

        final String nsURI = element.getNamespace();
        String nsPrefix = "";

        if (element instanceof AbstractConfiguration) {
            nsPrefix = ((AbstractConfiguration) element).getPrefix();
        }

        // nsPrefix is guaranteed to be non-null at this point.
        boolean nsWasDeclared = false;

        final String existingURI = namespaceSupport.getURI(nsPrefix);

        // ie, there is no existing URI declared for this prefix or we're
        // remapping the prefix to a different URI
        if (existingURI == null || !existingURI.equals(nsURI)) {
            nsWasDeclared = true;

            if (nsPrefix.equals("") && nsURI.equals("")) {
                // implicit mapping; don't need to declare
            } else if (nsPrefix.equals("")) {
                // (re)declare the default namespace
                attr.addAttribute("", "xmlns", "xmlns", "CDATA", nsURI);
            } else {
                // (re)declare a mapping from nsPrefix to nsURI
                attr.addAttribute("", "xmlns:" + nsPrefix, "xmlns:" + nsPrefix, "CDATA", nsURI);
            }

            handler.startPrefixMapping(nsPrefix, nsURI);
            namespaceSupport.declarePrefix(nsPrefix, nsURI);
        }

        String localName = element.getName();
        String qName = element.getName();

        if (nsPrefix == null || nsPrefix.length() == 0) {
            qName = localName;
        } else {
            qName = nsPrefix + ":" + localName;
        }

        handler.startElement(nsURI, localName, qName, attr);

        String value = element.getValue(null);

        if (null == value) {
            Configuration[] children = element.getChildren();

            for (Configuration element2 : children) {
                serializeElement(handler, namespaceSupport, element2);
            }
        } else {
            handler.characters(value.toCharArray(), 0, value.length());
        }

        handler.endElement(nsURI, localName, qName);

        if (nsWasDeclared) {
            handler.endPrefixMapping(nsPrefix);
        }

        namespaceSupport.popContext();
    }

    /**
     * Serialize the configuration object to a file using a filename.
     *
     * @param filename a <code>String</code> value
     * @param source   a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws IOException            if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void serializeToFile(final String filename, final Configuration source) throws SAXException, IOException,
                                                                                          ConfigurationException {
        serializeToFile(new File(filename), source);
    }

    /**
     * Serialize the configuration object to a file using a File object.
     *
     * @param file   a <code>File</code> value
     * @param source a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws IOException            if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void serializeToFile(final File file, final Configuration source) throws SAXException, IOException,
                                                                                    ConfigurationException {
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            serialize(outputStream, source);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Serialize the configuration object to an output stream.
     *
     * @param outputStream an <code>OutputStream</code> value
     * @param source       a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws IOException            if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void serialize(final OutputStream outputStream, final Configuration source) throws SAXException,
                                                                                              IOException,
                                                                                              ConfigurationException {
        serialize(createContentHandler(new StreamResult(outputStream)), source);
    }

    /**
     * Serialize the configuration object to an output stream derived from an
     * URI. The URI must be resolveable by the <code>java.net.URL</code> object.
     *
     * @param uri    a <code>String</code> value
     * @param source a <code>Configuration</code> value
     * @throws SAXException           if an error occurs
     * @throws IOException            if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public void serialize(final String uri, final Configuration source) throws SAXException, IOException,
                                                                               ConfigurationException {
        OutputStream outputStream = null;

        try {
            outputStream = new URL(uri).openConnection().getOutputStream();
            serialize(outputStream, source);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Serialize the configuration object to a string
     *
     * @param source a <code>Configuration</code> value
     * @return configuration serialized as a string.
     * @throws SAXException           if an error occurs
     * @throws ConfigurationException if an error occurs
     */
    public String serialize(final Configuration source) throws SAXException, ConfigurationException {
        final StringWriter writer = new StringWriter();

        serialize(createContentHandler(new StreamResult(writer)), source);

        return writer.toString();
    }
}
