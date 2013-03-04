package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

class RemoveNamespaceVisitor extends AbstractAddRemoveNamespaceVisitor {
    private final String[] namespacesToUpdate;

    public RemoveNamespaceVisitor(SchemaResourceSet schemas, String... namespacesToUpdate) {
        super(schemas);
        this.namespacesToUpdate = namespacesToUpdate;
    }

    @Override
    protected void updateNamespaces() {
        if (namespacesToUpdate == null) {
            return;
        }

        for (String namespaceToUpdate : namespacesToUpdate) {
            String nsToRemove = namespaceToUpdate;

            // 避免删除正在使用的namespace
            if (!namespacesInUse.contains(nsToRemove)) {
                defs.remove(nsToRemove);
                getSchemaLocations().remove(nsToRemove);
            }
        }
    }
}
