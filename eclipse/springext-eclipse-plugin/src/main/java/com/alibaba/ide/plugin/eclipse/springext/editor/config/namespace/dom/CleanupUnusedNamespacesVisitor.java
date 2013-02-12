package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

class CleanupUnusedNamespacesVisitor extends AbstractAddRemoveNamespaceVisitor {
    public CleanupUnusedNamespacesVisitor(SchemaResourceSet schemas) {
        super(schemas);
    }

    @Override
    protected void updateNamespaces() {
        for (String ns : defs.getNamespaces()) {
            // 避免删除正在使用的namespace
            if (!namespacesInUse.contains(ns)) {
                defs.remove(ns);
                getSchemaLocations().remove(ns);
            }
        }
    }
}
