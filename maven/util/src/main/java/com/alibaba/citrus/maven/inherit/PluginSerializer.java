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

import java.io.IOException;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

/** Customized XML serializer for plugin metadata */
public class PluginSerializer extends MXSerializer {
    /** Configures basic formatting rules */
    public PluginSerializer() {
        setProperty(PROPERTY_SERIALIZER_INDENTATION, "  ");
        setProperty(PROPERTY_SERIALIZER_LINE_SEPARATOR, System.getProperty("line.separator"));
    }

    /**
     * Overrides method to hide internal attributes used in processing plugin metadata
     * <p/>
     * {@inheritDoc}
     */
    public XmlSerializer attribute(String namespace, String name, String value)
            throws IOException {
        if (!Xpp3Dom.CHILDREN_COMBINATION_MODE_ATTRIBUTE.equals(name)) {
            return super.attribute(namespace, name, value);
        }

        return this;
    }
}
