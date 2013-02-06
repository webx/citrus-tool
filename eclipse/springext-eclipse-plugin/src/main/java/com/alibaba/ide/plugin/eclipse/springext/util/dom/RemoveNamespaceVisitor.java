package com.alibaba.ide.plugin.eclipse.springext.util.dom;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

class RemoveNamespaceVisitor extends AbstractAddRemoveNamespaceVisitor {
    private final String namespaceToUpdate;

    public RemoveNamespaceVisitor(SchemaResourceSet schemas, String namespaceToUpdate) {
        super(schemas);
        this.namespaceToUpdate = namespaceToUpdate;
    }

    @Override
    protected void updateNamespaces() {
        String nsToRemove = namespaceToUpdate;

        // 避免删除正在使用的namespace
        if (!namespacesInUse.contains(nsToRemove)) {
            defs.remove(nsToRemove);
            getSchemaLocations().remove(nsToRemove);
        }
    }
}
