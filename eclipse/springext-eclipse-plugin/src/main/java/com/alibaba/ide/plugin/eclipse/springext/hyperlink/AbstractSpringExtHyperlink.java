package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;

public abstract class AbstractSpringExtHyperlink<C> implements IHyperlink {
    protected final IProject project;
    protected final Class<C> componentType;
    protected final C component;
    private final IRegion region;
    private final Schema componentSchema;

    public AbstractSpringExtHyperlink(@Nullable IRegion region, @NotNull IProject project, @NotNull C component,
                                      @Nullable Schema componentSchema) {
        this.region = region;
        this.project = project;
        this.component = component;
        this.componentSchema = componentSchema;
        this.componentType = resolveComponentType();
    }

    @SuppressWarnings("unchecked")
    private Class<C> resolveComponentType() {
        return (Class<C>) resolveParameter(getClass(), AbstractSpringExtHyperlink.class, 0).getRawType();
    }

    public IRegion getHyperlinkRegion() {
        return region;
    }

    public String getTypeLabel() {
        return null;
    }

    public String getHyperlinkText() {
        return String.format("Open '%s'", getDescription());
    }

    public void open() {
        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IDE.openEditor(page, new SpringExtComponentEditorInput(), getEditorId(), true);
        } catch (PartInitException e) {
            logAndDisplay(new Status(IStatus.ERROR, SpringExtConstant.PLUGIN_ID, "Could not open editor for "
                    + getDescription(), e));
        }
    }

    protected abstract String getDescription();

    protected abstract String getName();

    protected abstract String getEditorId();

    protected abstract Schema getComponentSchema();

    private class SpringExtComponentEditorInput extends PlatformObject implements IEditorInput {
        public boolean exists() {
            return component != null;
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public String getToolTipText() {
            return getDescription();
        }

        public String getName() {
            return AbstractSpringExtHyperlink.this.getName();
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Object getAdapter(Class adapter) {
            if (adapter.isAssignableFrom(IProject.class)) {
                return project;
            }

            if (adapter.isAssignableFrom(componentType)) {
                return component;
            }

            if (adapter.isAssignableFrom(Schema.class)) {
                if (componentSchema != null) {
                    return componentSchema;
                } else {
                    return getComponentSchema();
                }
            }

            return super.getAdapter(adapter);
        }
    }
}
