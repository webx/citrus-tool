package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Set;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.util.ObjectUtil;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

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

    public NamespaceDefinition findNamespaceByPrefix(String prefix) {
        prefix = trimToNull(prefix);

        for (Map<String, NamespaceDefinition> prefixes : namespaces.values()) {
            for (NamespaceDefinition ns : prefixes.values()) {
                if (ObjectUtil.isEquals(prefix, ns.getPrefix())) {
                    return ns;
                }
            }
        }

        return null;
    }

    public String getLocation(String namespace) {
        Map<String, NamespaceDefinition> nds = find(namespace);

        if (!nds.isEmpty()) {
            return nds.values().iterator().next().getLocation();
        }

        return null;
    }

    public String getPrefix(String namespace, SchemaResourceSet schemas) {
        Map<String, NamespaceDefinition> prefixes = find(namespace);

        if (prefixes.isEmpty()) {
            Schema schema = schemas.findSchemaByUrl(namespace);

            if (schema != null) {
                return schema.getNamespacePrefix();
            } else {
                return getLastSegment(namespace);
            }
        } else {
            return prefixes.keySet().iterator().next();
        }
    }

    /**
     * 根据schema location，取得指定版本的schema。
     */
    public Schema getSchemaOfLocation(String namespace, SchemaResourceSet schemas) {
        Set<Schema> set = schemas.getNamespaceMappings().get(namespace);
        String location = getLocation(namespace);

        if (set != null && !set.isEmpty()) {
            if (location != null) {
                for (Schema schema : set) {
                    if (location.endsWith(schema.getName())) {
                        return schema;
                    }
                }
            }

            // 取得第一个schema作为默认结果。
            return set.iterator().next();
        }

        return null;
    }

    public String[] getNamespaces() {
        return namespaces.keySet().toArray(new String[namespaces.size()]);
    }
}
