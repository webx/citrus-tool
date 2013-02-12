package com.alibaba.ide.plugin.eclipse.springext.editor.config.namespace;

import static com.alibaba.citrus.util.BasicConstant.*;

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.NamespaceItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;
import com.alibaba.ide.plugin.eclipse.springext.editor.config.SpringExtConfig;

@SuppressWarnings("restriction")
public class NamespacesTreeProvider extends LabelProvider implements ITreePathLabelProvider, ITreeContentProvider,
        ICheckStateProvider {
    private final SpringExtConfig config;

    public NamespacesTreeProvider(SpringExtConfig config) {
        this.config = config;
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IDOMDocument && config.getSchemas() != null) {
            if (config.isListNamespacesAsTree()) {
                return config.getSchemas().getIndependentItems();
            } else {
                return config.getSchemas().getAllItems();
            }
        } else {
            return EMPTY_OBJECT_ARRAY;
        }
    }

    public Object[] getChildren(Object element) {
        if (config.isListNamespacesAsTree()) {
            return ((TreeItem) element).getChildren();
        } else {
            return EMPTY_OBJECT_ARRAY;
        }
    }

    public boolean hasChildren(Object element) {
        if (config.isListNamespacesAsTree()) {
            return ((TreeItem) element).hasChildren();
        } else {
            return false;
        }
    }

    public Object getParent(Object element) {
        if (config.isListNamespacesAsTree() && config.getSchemas() != null) {
            for (TreeItem item : config.getSchemas().getIndependentItems()) {
                if (item == element) {
                    return null;
                }

                TreeItem found = find(item, null, element);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private TreeItem find(TreeItem item, TreeItem parent, Object element) {
        if (item == element) {
            return parent;
        }

        for (TreeItem child : item.getChildren()) {
            TreeItem found = find(child, item, element);

            if (found != null) {
                return found;
            }
        }

        return null;
    }

    @Override
    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void updateLabel(ViewerLabel label, TreePath elementPath) {
        TreeItem item = getTreeItem(elementPath);

        label.setText(item.toString());

        if (item instanceof ContributionItem) {
            label.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("plug"));
        } else if (item instanceof ConfigurationPointItem) {
            label.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("socket"));
        } else if (item instanceof SpringPluggableItem) {
            if (((SpringPluggableItem) item).getSchemas().isEmpty()) {
                label.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("spring-stroke"));
            } else {
                label.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("spring"));
            }
        }
    }

    public boolean isChecked(Object element) {
        boolean checked = false;

        if (element instanceof NamespaceItem) {
            checked = isChecked((NamespaceItem) element);
        } else if (element instanceof ContributionItem) {
            for (TreeItem item : ((ContributionItem) element).getChildren()) {
                checked |= isChecked((NamespaceItem) item);
            }
        }

        return checked;
    }

    private boolean isChecked(NamespaceItem item) {
        return !config.getNamespaceDefinitions().find(item.getNamespace()).isEmpty();
    }

    public boolean isGrayed(Object element) {
        boolean greyed = false;

        if (element instanceof ContributionItem) {
            for (TreeItem item : ((ContributionItem) element).getChildren()) {
                if (!isChecked((NamespaceItem) item)) {
                    greyed = true;
                }
            }
        }

        return greyed;
    }

    private TreeItem getTreeItem(TreePath parentPath) {
        return (TreeItem) parentPath.getLastSegment();
    }
}
