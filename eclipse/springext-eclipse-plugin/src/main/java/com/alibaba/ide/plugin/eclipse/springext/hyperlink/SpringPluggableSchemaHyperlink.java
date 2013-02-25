package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.editor.component.spring.SpringPluggableSchemaEditor;

public class SpringPluggableSchemaHyperlink extends AbstractSpringExtHyperlink<SpringPluggableSchemaSourceInfo> {
    public SpringPluggableSchemaHyperlink(IRegion region, IProject project,
                                          SpringPluggableSchemaSourceInfo springPluggableSchema) {
        super(region, project, springPluggableSchema, (Schema) springPluggableSchema);
    }

    public SpringPluggableSchemaHyperlink(IProject project, SpringPluggableSchemaSourceInfo springPluggableSchema) {
        super(null, project, springPluggableSchema, (Schema) springPluggableSchema);
    }

    @Override
    protected String getDescription() {
        return getTargetNamespace();
    }

    @Override
    public String getName() {
        return getLastSegment(getTargetNamespace());
    }

    private String getTargetNamespace() {
        return ((Schema) component).getTargetNamespace();
    }

    @Override
    protected String getEditorId() {
        return SpringPluggableSchemaEditor.EDITOR_ID;
    }

    @Override
    protected Schema getComponentDefaultSchema() {
        return null;
    }

    @Override
    protected boolean compareComponent(SpringPluggableSchemaSourceInfo thisComponent,
                                       SpringPluggableSchemaSourceInfo otherComponent) {
        URL u1 = getSourceURL(thisComponent);
        URL u2 = getSourceURL(otherComponent);

        return u1.equals(u2);
    }
}
