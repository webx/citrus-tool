package com.alibaba.ide.plugin.eclipse.springext.hyperlink;

import static com.alibaba.citrus.generictype.TypeInfoUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

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

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;

public abstract class AbstractSpringExtHyperlink<C> implements IHyperlink {
    protected final IProject project;
    protected final Class<C> componentType;
    protected final C component;
    private final IRegion region;
    private final Schema componentSchema;

    public AbstractSpringExtHyperlink(IRegion region, IProject project, C component, Schema componentSchema) {
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

    /**
     * 返回link的名称，这个名称将作为editor的标题。
     */
    protected abstract String getName();

    /**
     * 返回link的详细描述，这个名称将被显示为link的文本，例如<code>Open xyz.xsd</code>，以及编辑器标题的弹出提示。
     */
    protected abstract String getDescription();

    /**
     * 返回要打开的、用来编辑此link所代表的组件的编辑器的ID。
     */
    protected abstract String getEditorId();

    /**
     * 一个component可能有多个版本的schema。如果指定了某个版本的schema，则打开之。否则，此方法会被调用，以获取默认的schema版本
     * ，并打开之。
     */
    protected abstract Schema getComponentDefaultSchema();

    /**
     * 此方法用来避免重复打开编辑器。
     */
    protected abstract boolean compareComponent(C thisComponent, C otherComponent);

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
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (obj != null && obj.getClass().equals(getClass())) {
                Object otherComponent = ((SpringExtComponentEditorInput) obj).getComponent();
                C component = getComponent();

                if (component != null && otherComponent != null
                        && otherComponent.getClass().equals(component.getClass())) {
                    return compareComponent(component, (C) otherComponent);
                }
            }

            return super.equals(obj);
        }

        private C getComponent() {
            return component;
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
                    return getComponentDefaultSchema();
                }
            }

            return super.getAdapter(adapter);
        }
    }
}
