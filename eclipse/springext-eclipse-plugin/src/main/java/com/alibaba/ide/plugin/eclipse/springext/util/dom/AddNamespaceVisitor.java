package com.alibaba.ide.plugin.eclipse.springext.util.dom;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SchemaUtil;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;
import com.alibaba.ide.plugin.eclipse.springext.util.dom.DomDocumentUtil.NamespaceDefinition;

public class AddNamespaceVisitor extends AbstractAddRemoveNamespaceVisitor {
    private final String namespaceToUpdate;

    public AddNamespaceVisitor(SchemaResourceSet schemas, String namespaceToUpdate) {
        super(schemas);
        this.namespaceToUpdate = namespaceToUpdate;
    }

    @Override
    protected void updateNamespaces() {
        Schema schema = schemas.findSchemaByUrl(namespaceToUpdate); // 注：schema可能为null，比如：spring/c
        String locationPrefix = getLocationPrefix();

        if (defs.find(namespaceToUpdate).isEmpty()) {
            String nsPrefixBase = SchemaUtil.getNamespacePrefix(schema == null ? null : schema.getPreferredNsPrefix(),
                    namespaceToUpdate);

            String nsPrefix = nsPrefixBase;

            // 避免prefix重复。
            for (int i = 1; existingPrefixes.contains(nsPrefix); i++) {
                nsPrefix = nsPrefixBase + i;
            }

            defs.add(new NamespaceDefinition(namespaceToUpdate, nsPrefix, getSchemaLocations()));

            if (schema != null) {
                getSchemaLocations().put(namespaceToUpdate, locationPrefix + schema.getName());
            }
        }
    }
}
