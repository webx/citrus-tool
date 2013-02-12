package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace.dom;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

public class ChangeSchemaLocationVisitor extends AddNamespaceVisitor {
    private final Schema schema;

    public ChangeSchemaLocationVisitor(SchemaResourceSet schemas, Schema schema) {
        super(schemas, schema.getTargetNamespace());
        this.schema = schema;
    }

    @Override
    protected Schema getSchema(String namespace) {
        if (namespace.equals(schema.getTargetNamespace())) {
            return schema;
        }

        return null;
    }
}
