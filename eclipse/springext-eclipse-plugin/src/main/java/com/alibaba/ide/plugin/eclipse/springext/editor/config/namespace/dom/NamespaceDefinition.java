package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import static com.alibaba.citrus.util.StringUtil.*;

import java.util.Map;

public class NamespaceDefinition {
    private final String namespace;
    private final String prefix;
    private final String location;

    public NamespaceDefinition(String namespace, String prefix, Map<String, String> schemaLocations) {
        this.namespace = trimToNull(namespace);
        this.prefix = trimToNull(prefix);
        this.location = schemaLocations.get(this.namespace);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "xmlns" + (prefix == null ? "" : ":" + prefix) + "=\"" + namespace + "\"";
    }
}
