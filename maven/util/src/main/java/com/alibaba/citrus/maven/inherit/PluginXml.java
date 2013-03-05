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
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

/** Plugin metadata, usually stored in META-INF/maven/plugin.xml */
public class PluginXml {
    private static final String EXECUTE_GOAL  = "executeGoal";
    private static final String EXECUTE_PHASE = "executePhase";

    private final File    m_file;
    private       Xpp3Dom m_xml;

    /**
     * Parses a file presumed to contain plugin metadata
     *
     * @param file file containing plugin metadata
     * @throws XmlPullParserException
     * @throws IOException
     */
    public PluginXml(File file)
            throws XmlPullParserException, IOException {
        m_file = file;

        XmlPullParser parser = new MXParser();
        Reader reader = StreamFactory.newXmlReader(m_file);
        parser.setInput(reader);

        m_xml = Xpp3DomBuilder.build(parser, false);

        IOUtil.close(reader);
    }

    /** {@inheritDoc} */
    public String toString() {
        return m_xml.getChild("artifactId").getValue();
    }

    /**
     * Gets all mojo descriptors
     *
     * @return an array of mojo descriptors
     */
    public Xpp3Dom[] getMojos() {
        return m_xml.getChild("mojos").getChildren("mojo");
    }

    /**
     * Finds a mojo descriptor with a named goal
     *
     * @param goal mojo goal
     * @return first matching mojo descriptor, null if not found
     */
    public Xpp3Dom findMojo(String goal) {
        Xpp3Dom[] mojos = getMojos();
        for (int i = 0; i < mojos.length; i++) {
            if (goal.equals(mojos[i].getChild("goal").getValue())) {
                return mojos[i];
            }
        }

        return null;
    }

    /**
     * Merges a mojo descriptor with its super-mojo (inherit from it)
     * <p/>
     * The local mojo is considered the dominant one - if any parameters, configuration or requirements match then the
     * local version is kept. Any parameters, configuration or requirements not in the local mojo are appended to its
     * descriptor
     *
     * @param mojo      local mojo inheriting from superMojo
     * @param superMojo mojo being extended
     */
    public static void mergeMojo(Xpp3Dom mojo, Xpp3Dom superMojo) {
        // clone superMojo to get an editable copy
        Xpp3Dom tempMojo = new Xpp3Dom(superMojo);

        // duplicates are removed from the temp copy of the superMojo
        removeDuplicates(mojo, tempMojo, "parameters", "name/", true);
        removeDuplicates(mojo, tempMojo, "configuration", null, false);
        removeDuplicates(mojo, tempMojo, "requirements", "field-name/", true);

        removeDuplicateExecuteTags(mojo, tempMojo);

        // now we can safely append these sections
        setAppendMode(mojo.getChild("parameters"));
        setAppendMode(mojo.getChild("configuration"));
        setAppendMode(mojo.getChild("requirements"));

        Xpp3Dom.mergeXpp3Dom(mojo, tempMojo);
    }

    /**
     * Removes any common elements from a given list in the temporary mojo
     * <p/>
     * Elements are matched by comparing their ids, which are located using a simple XML path notation. If the path ends
     * in '/' then the content of the id element is used, otherwise the name of the id element itself is used
     *
     * @param mojo     dominant mojo
     * @param tempMojo temporary mojo
     * @param name     name of the element list to check
     * @param path     simple XML path with the location of the id element
     * @param verbose  print warnings about any matching elements
     */
    private static void removeDuplicates(Xpp3Dom mojo, Xpp3Dom tempMojo, String name, String path, boolean verbose) {
        Xpp3Dom list = mojo.getChild(name);
        Xpp3Dom tempList = tempMojo.getChild(name);

        if (null == tempList || null == list) {
            return;
        }

        for (int s = 0; s < tempList.getChildCount(); s++) {
            Xpp3Dom idElement = getIdElement(tempList.getChild(s), path);
            if (hasMatchingElement(list, path, verbose, idElement)) {
                // decrement index to avoid skipping entries, as list shrinks by one
                tempList.removeChild(s--);
            }
        }
    }

    /**
     * Searches an element list for any elements with the same id
     * <p/>
     * Elements are matched by comparing their ids, which are located using a simple XML path notation. If the path is
     * not null and ends in / then the content of the id element is used, otherwise the name of the id element is used
     *
     * @param list         element list
     * @param path         simple XML path with the location of the id element
     * @param verbose      print warnings about any matching elements
     * @param theIdElement id element to compare against
     * @return true if element list contains a matching element
     */
    private static boolean hasMatchingElement(Xpp3Dom list, String path, boolean verbose, Xpp3Dom theIdElement) {
        for (int n = 0; n < list.getChildCount(); n++) {
            Xpp3Dom idElement = getIdElement(list.getChild(n), path);

            String lhs;
            String rhs;

            if (null != path && path.endsWith("/")) {
                lhs = theIdElement.getValue();
                rhs = idElement.getValue();
            } else {
                lhs = theIdElement.getName();
                rhs = idElement.getName();
            }

            if (lhs.equals(rhs)) {
                if (verbose) {
                    System.out.println("[WARN] overriding field " + lhs);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Finds the element that represents the id, according to an XML path
     *
     * @param element start element
     * @param path    simple XML path with the location of the id element
     * @return id element
     */
    private static Xpp3Dom getIdElement(Xpp3Dom element, String path) {
        Xpp3Dom idElement = element;

        if (null != path) {
            String[] idSegments = path.split("/");
            for (int i = 0; i < idSegments.length; i++) {
                idElement = element.getChild(idSegments[i]);
            }
        }

        return idElement;
    }

    /**
     * Special check for duplicate execute tags, as there are two discrete types (goal vs phase)
     *
     * @param mojo     dominant mojo
     * @param tempMojo temporary mojo
     */
    private static void removeDuplicateExecuteTags(Xpp3Dom mojo, Xpp3Dom tempMojo) {
        Xpp3Dom executeNode = mojo.getChild(EXECUTE_PHASE);
        if (null == executeNode) {
            executeNode = mojo.getChild(EXECUTE_GOAL);
        }

        if (executeNode != null) {
            removeChild(tempMojo, tempMojo.getChild(EXECUTE_PHASE));
            removeChild(tempMojo, tempMojo.getChild(EXECUTE_GOAL));

            if ("none".equals(executeNode.getValue())) {
                // allow removal of execute tags
                removeChild(mojo, executeNode);
            }
        }
    }

    /**
     * Remove the child from the XML fragment
     *
     * @param xml   xml fragment
     * @param child child node
     * @return true if node was removed, otherwise false
     */
    private static boolean removeChild(Xpp3Dom xml, Xpp3Dom child) {
        for (int i = xml.getChildCount() - 1; i >= 0; i--) {
            // reference equality test is ok
            if (xml.getChild(i) == child) {
                xml.removeChild(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Requests that all child elements are appended when the metadata is merged
     *
     * @param element an element in the plugin metadata
     */
    private static void setAppendMode(Xpp3Dom element) {
        if (null != element) {
            element.setAttribute(Xpp3Dom.CHILDREN_COMBINATION_MODE_ATTRIBUTE, Xpp3Dom.CHILDREN_COMBINATION_APPEND);
        }
    }

    /**
     * Writes the plugin metadata back to the file where it was loaded from
     *
     * @throws IOException
     */
    public void write()
            throws IOException {
        String encoding = StreamFactory.getXmlEncoding(m_file);
        Writer writer = StreamFactory.newXmlWriter(m_file);

        XmlSerializer serializer = new PluginSerializer();

        serializer.setOutput(writer);
        serializer.startDocument(encoding, null);
        m_xml.writeToSerializer(null, serializer);
        serializer.endDocument();

        IOUtil.close(writer);
    }
}
