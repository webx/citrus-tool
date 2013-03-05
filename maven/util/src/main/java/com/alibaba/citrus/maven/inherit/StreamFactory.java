/*
 * Copyright 2007 Stuart McCulloch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.maven.inherit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;

/** Various utility methods for getting the right kind of XML encoding stream */
final class StreamFactory {
    /** Hide constructor for utility class */
    private StreamFactory() {
        /*
         * nothing to do
         */
    }

    /** use internal class so we can recover from linkage errors when using maven 2.0.5 */
    private static final class XmlStreamFactory {
        /** Hide constructor for utility class */
        private XmlStreamFactory() {
            /*
             * nothing to do
             */
        }

        /**
         * @param xmlFile XML file to be read
         * @return reader with correct XML encoding
         * @throws IOException
         */
        static Reader newXmlReader(File xmlFile)
                throws IOException {
            return ReaderFactory.newXmlReader(xmlFile);
        }

        /**
         * @param xmlFile XML file to be written
         * @return writer with correct XML encoding
         * @throws IOException
         */
        static Writer newXmlWriter(File xmlFile)
                throws IOException {
            return WriterFactory.newXmlWriter(xmlFile);
        }
    }

    /**
     * @param xmlFile XML file to be read
     * @return reader with correct XML encoding
     * @throws IOException
     */
    public static Reader newXmlReader(File xmlFile)
            throws IOException {
        try {
            return XmlStreamFactory.newXmlReader(xmlFile);
        } catch (NoClassDefFoundError e) {
            return new FileReader(xmlFile);
        }
    }

    /**
     * @param xmlFile XML file to be written
     * @return writer with correct XML encoding
     * @throws IOException
     */
    public static Writer newXmlWriter(File xmlFile)
            throws IOException {
        try {
            return XmlStreamFactory.newXmlWriter(xmlFile);
        } catch (NoClassDefFoundError e) {
            return new FileWriter(xmlFile);
        }
    }

    /**
     * @param xmlFile XML file
     * @return current XML encoding if the file exists, otherwise the current platform encoding
     */
    public static String getXmlEncoding(File xmlFile) {
        Reader reader = null;
        try {
            reader = newXmlReader(xmlFile);
            return ((XmlStreamReader) reader).getEncoding();
        } catch (IOException e) {
            return System.getProperty("file.encoding");
        } catch (NoClassDefFoundError e) {
            return System.getProperty("file.encoding");
        } finally {
            IOUtil.close(reader);
        }
    }
}
