package com.alibaba.ide.plugin.eclipse.springext.extension.editor.namespace;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.LinkedList;

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
import com.alibaba.ide.plugin.eclipse.springext.extension.editor.SpringExtConfig;
import com.alibaba.ide.plugin.eclipse.springext.util.DomDocumentUtil;

@SuppressWarnings("restriction")
public class NamespacesProvider extends LabelProvider implements ITreePathLabelProvider, ITreeContentProvider,
        ICheckStateProvider {
    private final SpringExtConfig config;

    public NamespacesProvider(SpringExtConfig config) {
        this.config = config;
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IDOMDocument && config.getSchemas() != null) {
            return config.getSchemas().getIndependentItems();
        }

        return new Object[0];
    }

    public Object[] getChildren(Object element) {
        return ((TreeItem) element).getChildren();
    }

    public boolean hasChildren(Object element) {
        return ((TreeItem) element).hasChildren();
    }

    public Object getParent(Object element) {
        TreePath[] paths = getParents(element);

        if (paths.length > 0) {
            return paths[0].getLastSegment();
        } else {
            return null;
        }
    }

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
            label.setImage(SpringExtPlugin.getDefault().getImageRegistry().get("spring"));
        }
    }

    public boolean isChecked(Object element) {
        if (element instanceof NamespaceItem) {
            return isChecked((NamespaceItem) element);
        } else if (element instanceof ContributionItem) {
            boolean checked = false;

            for (TreeItem item : ((ContributionItem) element).getChildren()) {
                checked |= isChecked((NamespaceItem) item);
            }

            return checked;
        }

        return false;
    }

    private boolean isChecked(NamespaceItem item) {
        return DomDocumentUtil.getNamespace(config.getDomDocument(), item.getNamespace()) != null;
    }

    public boolean isGrayed(Object element) {
        return element instanceof ContributionItem;
    }

    private TreeItem getTreeItem(TreePath parentPath) {
        return (TreeItem) parentPath.getLastSegment();
    }

    private TreePath[] getParents(Object element) {
        LinkedList<TreeItem> path = createLinkedList();
        LinkedList<TreePath> allPaths = createLinkedList();

        if (config.getSchemas() != null) {
            for (TreeItem item : config.getSchemas().getIndependentItems()) {
                visit(item, path, element, allPaths);
            }
        }

        return allPaths.toArray(new TreePath[allPaths.size()]);
    }

    private void visit(TreeItem item, LinkedList<TreeItem> path, Object element, LinkedList<TreePath> allPaths) {
        if (item == element) {
            allPaths.add(new TreePath(path.toArray()));
        }

        try {
            path.addLast(item);

            for (TreeItem child : item.getChildren()) {
                visit(child, path, element, allPaths);
            }
        } finally {
            path.removeLast();
        }
    }
}
