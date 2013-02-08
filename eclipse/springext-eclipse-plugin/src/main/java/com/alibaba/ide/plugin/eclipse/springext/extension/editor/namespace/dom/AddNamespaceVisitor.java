package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace.dom;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

class AddNamespaceVisitor extends AbstractAddRemoveNamespaceVisitor {
    private final String[] namespacesToUpdate;

    public AddNamespaceVisitor(SchemaResourceSet schemas, String... namespacesToUpdate) {
        super(schemas);
        this.namespacesToUpdate = namespacesToUpdate;
    }

    @Override
    protected void updateNamespaces() {
        if (namespacesToUpdate == null) {
            return;
        }

        for (String namespaceToUpdate : namespacesToUpdate) {
            Schema schema = getSchema(namespaceToUpdate); // 注：schema可能为null，比如：spring/c
            String locationPrefix = guessLocationPrefix(getSchemaLocations(), schemas);

            if (defs.find(namespaceToUpdate).isEmpty()) {
                String nsPrefixBase = SchemaUtil.getNamespacePrefix(
                        schema == null ? null : schema.getPreferredNsPrefix(), namespaceToUpdate);

                String nsPrefix = nsPrefixBase;

                // 避免prefix重复。
                for (int i = 1; existingPrefixes.contains(nsPrefix); i++) {
                    nsPrefix = nsPrefixBase + i;
                }

                defs.add(new NamespaceDefinition(namespaceToUpdate, nsPrefix, getSchemaLocations()));
            }

            if (schema != null) {
                getSchemaLocations().put(namespaceToUpdate, locationPrefix + schema.getName());
            }
        }
    }

    protected Schema getSchema(String namespace) {
        return schemas.findSchemaByUrl(namespace);
    }
}
