package com.alibaba.ide.plugin.eclipse.springext.util.dom;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

public class RemoveUnusedNamespacesVisitor extends AbstractAddRemoveNamespaceVisitor {
    public RemoveUnusedNamespacesVisitor(SchemaResourceSet schemas) {
        super(schemas);
    }

    @Override
    protected void updateNamespaces() {
        for (String nsToRemove : defs.getNamespaces()) {
            // 避免删除正在使用的namespace
            if (!namespacesInUse.contains(nsToRemove)) {
                defs.remove(nsToRemove);
                getSchemaLocations().remove(nsToRemove);
            }
        }
    }
}
