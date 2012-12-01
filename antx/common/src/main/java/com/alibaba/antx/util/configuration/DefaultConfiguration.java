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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.alibaba.antx.util.StringUtil;

/**
 * This is the default <code>Configuration</code> implementation.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultConfiguration extends AbstractConfiguration implements Serializable {
    private static final   long            serialVersionUID = 3545521720271452213L;
    /** An empty (length zero) array of configuration objects. */
    protected static final Configuration[] EMPTY_ARRAY      = new Configuration[0];
    private final String    m_name;
    private final Location  m_location;
    private final String    m_namespace;
    private final String    m_prefix;
    private       HashMap   m_attributes;
    private       ArrayList m_children;
    private       String    m_value;
    private       boolean   m_readOnly;

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name a <code>String</code> value
     */
    public DefaultConfiguration(final String name) {
        this(name, null, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name     a <code>String</code> value
     * @param location a <code>String</code> value
     */
    public DefaultConfiguration(final String name, final Location location) {
        this(name, location, "", "");
    }

    /**
     * Create a new <code>DefaultConfiguration</code> instance.
     *
     * @param name     config node name
     * @param location Builder-specific locator string
     * @param ns       Namespace string (typically a URI). Should not be null; use ""
     *                 if no namespace.
     * @param prefix   A short string prefixed to element names, associating
     *                 elements with a longer namespace string. Should not be null;
     *                 use "" if no namespace.
     * @since 4.1
     */
    public DefaultConfiguration(final String name, final Location location, final String ns, final String prefix) {
        m_name = name;
        m_location = location;
        m_namespace = ns;
        m_prefix = prefix; // only used as a serialization hint. Cannot be null
    }

    /**
     * Returns the name of this configuration element.
     *
     * @return a <code>String</code> value
     */
    public String getName() {
        return m_name;
    }

    /**
     * Returns the namespace of this configuration element
     *
     * @return a <code>String</code> value
     * @throws ConfigurationException if an error occurs
     * @since 4.1
     */
    public String getNamespace() throws ConfigurationException {
        if (null != m_namespace) {
            return m_namespace;
        } else {
            throw new ConfigurationException("No namespace (not even default \"\") is associated with the "
                                             + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns the prefix of the namespace
     *
     * @return a <code>String</code> value
     * @throws ConfigurationException if prefix is not present (<code>null</code>
     *                                ).
     * @since 4.1
     */
    @Override
    protected String getPrefix() throws ConfigurationException {
        if (null != m_prefix) {
            return m_prefix;
        } else {
            throw new ConfigurationException("No prefix (not even default \"\") is associated with the "
                                             + "configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Returns a description of location of element.
     *
     * @return a <code>String</code> value
     */
    public Location getLocation() {
        return m_location;
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @param defaultValue the default value to return if value malformed or
     *                     empty
     * @return a <code>String</code> value
     */
    @Override
    public String getValue(final String defaultValue) {
        if (!StringUtil.isEmpty(m_value)) {
            return m_value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @return a <code>String</code> value
     * @throws ConfigurationException If the value is not present.
     */
    public String getValue() throws ConfigurationException {
        if (!StringUtil.isEmpty(m_value)) {
            return m_value;
        } else {
            throw new ConfigurationException("No value is associated with the " + "configuration element \""
                                             + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Return an array of all attribute names.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getAttributeNames() {
        if (null == m_attributes) {
            return new String[0];
        } else {
            return (String[]) m_attributes.keySet().toArray(new String[0]);
        }
    }

    /**
     * Return an array of <code>Configuration</code> elements containing all
     * node children.
     *
     * @return The child nodes with name
     */
    public Configuration[] getChildren() {
        if (null == m_children) {
            return new Configuration[0];
        } else {
            return (Configuration[]) m_children.toArray(new Configuration[0]);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     *
     * @param name a <code>String</code> value
     * @return a <code>String</code> value
     * @throws ConfigurationException If the attribute is not present.
     */
    public String getAttribute(final String name) throws ConfigurationException {
        final String value = null != m_attributes ? (String) m_attributes.get(name) : null;

        if (!StringUtil.isEmpty(value)) {
            return value;
        } else {
            throw new ConfigurationException("No attribute named \"" + name + "\" is "
                                             + "associated with the configuration element \"" + getName() + "\" at " + getLocation());
        }
    }

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name.
     *
     * @param name      a <code>String</code> value
     * @param createNew a <code>boolean</code> value
     * @return a <code>Configuration</code> value
     */
    @Override
    public Configuration getChild(final String name, final boolean createNew) {
        if (null != m_children) {
            final int size = m_children.size();

            for (int i = 0; i < size; i++) {
                final Configuration configuration = (Configuration) m_children.get(i);

                if (name.equals(configuration.getName())) {
                    return configuration;
                }
            }
        }

        if (createNew) {
            return new DefaultConfiguration(name, Location.EMPTY_LOCATION);
        } else {
            return null;
        }
    }

    /**
     * Return an array of <code>Configuration</code> objects children of this
     * associated with the given name. <br>
     * The returned array may be empty but is never <code>null</code>.
     *
     * @param name The name of the required children <code>Configuration</code>.
     * @return a <code>Configuration[]</code> value
     */
    public Configuration[] getChildren(final String name) {
        if (null == m_children) {
            return new Configuration[0];
        } else {
            final ArrayList children = new ArrayList();
            final int size = m_children.size();

            for (int i = 0; i < size; i++) {
                final Configuration configuration = (Configuration) m_children.get(i);

                if (name.equals(configuration.getName())) {
                    children.add(configuration);
                }
            }

            return (Configuration[]) children.toArray(new Configuration[0]);
        }
    }

    /**
     * Append data to the value of this configuration element.
     *
     * @param value a <code>String</code> value
     * @deprecated Use setValue() instead
     */
    @Deprecated
    public void appendValueData(final String value) {
        checkWriteable();

        if (null == m_value) {
            m_value = value;
        } else {
            m_value += value;
        }
    }

    /**
     * Set the value of this <code>Configuration</code> object to the specified
     * string.
     *
     * @param value a <code>String</code> value
     */
    public void setValue(final String value) {
        checkWriteable();

        m_value = value;
    }

    /**
     * Set the value of the specified attribute to the specified string.
     *
     * @param name  name of the attribute to set
     * @param value a <code>String</code> value
     */
    public void setAttribute(final String name, final String value) {
        checkWriteable();

        if (null == m_attributes) {
            m_attributes = new HashMap();
        }

        m_attributes.put(name, value);
    }

    /**
     * Add an attribute to this configuration element, returning its old value
     * or <b>null</b>.
     *
     * @param name  a <code>String</code> value
     * @param value a <code>String</code> value
     * @return a <code>String</code> value
     * @deprecated Use setAttribute() instead
     */
    @Deprecated
    public String addAttribute(final String name, String value) {
        checkWriteable();

        if (null == m_attributes) {
            m_attributes = new HashMap();
        }

        return (String) m_attributes.put(name, value);
    }

    /**
     * Add a child <code>Configuration</code> to this configuration element.
     *
     * @param configuration a <code>Configuration</code> value
     */
    public void addChild(final Configuration configuration) {
        checkWriteable();

        if (null == m_children) {
            m_children = new ArrayList();
        }

        m_children.add(configuration);
    }

    /**
     * Add all the attributes, children and value from specified configuration
     * element to current configuration element.
     *
     * @param other the {@link Configuration} element
     */
    public void addAll(final Configuration other) {
        checkWriteable();

        setValue(other.getValue(null));
        addAllAttributes(other);
        addAllChildren(other);
    }

    /**
     * Add all attributes from specified configuration element to current
     * configuration element.
     *
     * @param other the {@link Configuration} element
     */
    public void addAllAttributes(final Configuration other) {
        checkWriteable();

        final String[] attributes = other.getAttributeNames();

        for (final String name : attributes) {
            final String value = other.getAttribute(name, null);

            setAttribute(name, value);
        }
    }

    /**
     * Add all child <code>Configuration</code> objects from specified
     * configuration element to current configuration element.
     *
     * @param other the other {@link Configuration} value
     */
    public void addAllChildren(final Configuration other) {
        checkWriteable();

        final Configuration[] children = other.getChildren();

        for (Configuration element : children) {
            addChild(element);
        }
    }

    /**
     * Remove a child <code>Configuration</code> to this configuration element.
     *
     * @param configuration a <code>Configuration</code> value
     */
    public void removeChild(final Configuration configuration) {
        checkWriteable();

        if (null == m_children) {
            return;
        }

        m_children.remove(configuration);
    }

    /**
     * Return count of children.
     *
     * @return an <code>int</code> value
     */
    public int getChildCount() {
        if (null == m_children) {
            return 0;
        }

        return m_children.size();
    }

    /** Make this configuration read-only. */
    public void makeReadOnly() {
        m_readOnly = true;
    }

    /**
     * heck if this configuration is writeable.
     *
     * @throws IllegalStateException if this configuration s read-only
     */
    protected final void checkWriteable() throws IllegalStateException {
        if (m_readOnly) {
            throw new IllegalStateException("Configuration is read only and can not be modified");
        }
    }
}
