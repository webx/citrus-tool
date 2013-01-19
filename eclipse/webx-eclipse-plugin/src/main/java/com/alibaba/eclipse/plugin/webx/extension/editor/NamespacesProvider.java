package com.alibaba.eclipse.plugin.webx.extension.editor;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.LinkedList;

import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;

import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ConfigurationPointItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.ContributionItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.SpringPluggableItem;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet.TreeItem;
import com.alibaba.eclipse.plugin.webx.SpringExtEclipsePlugin;
import com.alibaba.eclipse.plugin.webx.extension.resolver.SpringExtSchemaResourceSet;

@SuppressWarnings("restriction")
public class NamespacesProvider extends LabelProvider implements ITreePathLabelProvider, ITreePathContentProvider {
    private SpringExtSchemaResourceSet schemas;

    public NamespacesProvider(SpringExtSchemaResourceSet schemas) {
        this.schemas = schemas;
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof IDOMDocument) {
            return schemas.getIndependentItems();
        }

        return new Object[0];
    }

    public boolean hasChildren(TreePath path) {
        return getTreeItem(path).hasChildren();
    }

    public Object[] getChildren(TreePath parentPath) {
        return getTreeItem(parentPath).getChildren();
    }

    public TreePath[] getParents(Object element) {
        LinkedList<TreeItem> path = createLinkedList();
        LinkedList<TreePath> allPaths = createLinkedList();

        for (TreeItem item : schemas.getIndependentItems()) {
            visit(item, path, element, allPaths);
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

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void updateLabel(ViewerLabel label, TreePath elementPath) {
        TreeItem item = getTreeItem(elementPath);

        label.setText(item.toString());

        if (item instanceof ContributionItem) {
            label.setImage(SpringExtEclipsePlugin.getDefault().getImageRegistry().get("plug"));
        } else if (item instanceof ConfigurationPointItem) {
            label.setImage(SpringExtEclipsePlugin.getDefault().getImageRegistry().get("socket"));
        } else if (item instanceof SpringPluggableItem) {
            label.setImage(SpringExtEclipsePlugin.getDefault().getImageRegistry().get("spring"));
        }
    }

    private TreeItem getTreeItem(TreePath parentPath) {
        return (TreeItem) parentPath.getLastSegment();
    }
}
