package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static java.util.Collections.*;

import java.util.Map;

public class NamespaceDefinitions {
    private final Map<String/* ns */, Map<String/* prefix */, NamespaceDefinition>> namespaces = createLinkedHashMap();

    public void add(NamespaceDefinition nd) {
        Map<String, NamespaceDefinition> prefixes = namespaces.get(nd.getNamespace());

        if (prefixes == null) {
            prefixes = createHashMap();
            namespaces.put(nd.getNamespace(), prefixes);
        }

        prefixes.put(nd.getPrefix(), nd);
    }

    public void remove(String namespace) {
        namespaces.remove(namespace);
    }

    public Map<String, NamespaceDefinition> find(String namespace) {
        Map<String, NamespaceDefinition> prefixes = namespaces.get(trimToNull(namespace));

        if (prefixes == null) {
            return emptyMap();
        } else {
            return prefixes;
        }
    }

    public String getLocation(String namespace) {
        Map<String, NamespaceDefinition> nds = find(namespace);

        if (!nds.isEmpty()) {
            return nds.values().iterator().next().getLocation();
        }

        return null;
    }

    public String[] getNamespaces() {
        return namespaces.keySet().toArray(new String[namespaces.size()]);
    }
}
