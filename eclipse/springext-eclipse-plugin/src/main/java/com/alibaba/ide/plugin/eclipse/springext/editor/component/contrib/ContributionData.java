package com.alibaba.ide.plugin.eclipse.springext.editor.component.contrib;

import org.eclipse.ui.IEditorInput;

import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.AbstactSpringExtComponentData;

public class ContributionData extends AbstactSpringExtComponentData<Contribution> {
    private Contribution contrib;
    private Schema schema;

    public Contribution getContribution() {
        return contrib;
    }

    public Schema getSchema() {
        return schema;
    }

    @Override
    public void initWithEditorInput(IEditorInput input) {
        super.initWithEditorInput(input);

        contrib = (Contribution) input.getAdapter(Contribution.class);
        schema = (Schema) input.getAdapter(Schema.class);
    }
}
