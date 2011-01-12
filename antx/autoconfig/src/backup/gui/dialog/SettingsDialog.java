/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.antx.config.gui.dialog;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.alibaba.antx.config.gui.ConfiguratorGUI;
import com.alibaba.antx.config.gui.resource.Resources;
import com.alibaba.antx.config.gui.util.ConfiguratorConstant;
import com.alibaba.antx.util.PatternSet;
import com.alibaba.antx.util.StringUtil;

public class SettingsDialog extends Dialog {
    private TableViewer   filesViewer;
    private final Set     files;
    private PatternSet    descriptorPatterns;
    private PatternsGroup descriptorPatternsControl;
    private PatternSet    packagePatterns;
    private PatternsGroup packagePatternsControl;
    private Text          commandLine;

    public SettingsDialog(ConfiguratorGUI gui) {
        super(gui.getShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.files = new LinkedHashSet(gui.getOpenFiles());
        this.descriptorPatterns = gui.getDescriptorPatterns();
        this.packagePatterns = gui.getPackagePatterns();
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Resources.getText("menu.file.settings.tip"));
    }

    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        ((GridLayout) composite.getLayout()).numColumns = 2;

        GridData data;
        TabFolder tabs = new TabFolder(composite, SWT.NONE);

        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        tabs.setLayoutData(data);

        Label commandLineLabel = new Label(composite, SWT.NONE);
        commandLineLabel.setText(Resources.getText("dialog.settings.commandLine"));
        data = new GridData();
        data.verticalAlignment = SWT.TOP;
        commandLineLabel.setLayoutData(data);

        commandLine = new Text(composite, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        data = new GridData(GridData.FILL_BOTH);
        data.heightHint = new GC(commandLine).textExtent("A").y * 3;
        data.widthHint = 400;
        commandLine.setLayoutData(data);

        // Files
        TabItem filesTab = new TabItem(tabs, SWT.NONE);
        Composite filesComposite = new Composite(tabs, SWT.NONE);

        filesTab.setText(Resources.getText("dialog.settings.tab1"));
        filesTab.setControl(filesComposite);
        createFilesGroup(filesComposite);

        // Patterns
        TabItem patternsTab = new TabItem(tabs, SWT.NONE);
        Composite patternsComposite = new Composite(tabs, SWT.NONE);

        patternsTab.setText(Resources.getText("dialog.settings.tab2"));
        patternsTab.setControl(patternsComposite);
        createPatternsGroup(patternsComposite);

        updateCommandLine();

        return parent;
    }

    private void createFilesGroup(Composite composite) {
        composite.setLayout(new FormLayout());

        // 文件列表
        filesViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

        filesViewer.setLabelProvider(new ITableLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex) {
                if (((File) element).isDirectory()) {
                    return Resources.getImageDescriptor("icon.folder").createImage();
                } else {
                    return Resources.getImageDescriptor("icon.jar").createImage();
                }
            }

            public String getColumnText(Object element, int columnIndex) {
                return ((File) element).getAbsolutePath();
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        });

        filesViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object inputElement) {
                return ((Collection) inputElement).toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        filesViewer.setComparator(new ViewerComparator());
        filesViewer.setInput(files);

        // 添加、删除按钮
        Button addFolder = new Button(composite, SWT.NULL);
        Button addFile = new Button(composite, SWT.NULL);
        Button remove = new Button(composite, SWT.NULL);

        addFolder.setText(Resources.getText("dialog.settings.addFolder"));
        addFolder.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                addFolder();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        addFile.setText(Resources.getText("dialog.settings.addFile"));
        addFile.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                addFile();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        remove.setText(Resources.getText("dialog.settings.remove"));
        remove.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                remove();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        // 设置layout
        FormData data;

        data = new FormData(400, 300);
        data.top = new FormAttachment(0, 20);
        data.left = new FormAttachment(0, 20);
        data.right = new FormAttachment(80, 0);
        data.bottom = new FormAttachment(100, -20);
        filesViewer.getControl().setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(0, 20);
        data.left = new FormAttachment(filesViewer.getControl(), 20);
        data.right = new FormAttachment(100, -20);
        addFolder.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(addFolder, 5);
        data.left = new FormAttachment(filesViewer.getControl(), 20);
        data.right = new FormAttachment(100, -20);
        addFile.setLayoutData(data);

        data = new FormData();
        data.top = new FormAttachment(addFile, 20);
        data.left = new FormAttachment(filesViewer.getControl(), 20);
        data.right = new FormAttachment(100, -20);
        remove.setLayoutData(data);
    }

    private void createPatternsGroup(Composite composite) {
        composite.setLayout(new GridLayout());

        // descriptor/include/exclude patterns
        descriptorPatternsControl = new PatternsGroup(composite, Resources.getText("dialog.settings.patterns.descriptors"),
                Resources.getText("dialog.settings.patterns.descriptors.message"), descriptorPatterns);
        packagePatternsControl = new PatternsGroup(composite, Resources.getText("dialog.settings.patterns.packages"),
                Resources.getText("dialog.settings.patterns.packages.message"), packagePatterns);
    }

    public Set getFiles() {
        return files;
    }

    public PatternSet getDescriptorPatterns() {
        return descriptorPatterns;
    }

    public PatternSet getPackagePatterns() {
        return packagePatterns;
    }

    protected void okPressed() {
        descriptorPatterns = descriptorPatternsControl.getPatterns();
        packagePatterns = packagePatternsControl.getPatterns();

        super.okPressed();
    }

    private void addFolder() {
        DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);

        dialog.setText(Resources.getText("dialog.settings.addFolder"));
        dialog.setMessage(Resources.getText("dialog.settings.addFolder.message"));
        dialog.setFilterPath(getCurrentDir().getAbsolutePath());

        String folder = dialog.open();

        if (folder != null) {
            File folderFile = new File(folder);
            files.add(new File(folder));
            filesViewer.refresh();
            filesViewer.setSelection(new StructuredSelection(folderFile));
            updateCommandLine();
        }
    }

    private void addFile() {
        FileDialog dialog = new FileDialog(getShell(), SWT.NONE);

        dialog.setText(Resources.getText("dialog.settings.addFile"));
        dialog.setFilterPath(getCurrentDir().getAbsolutePath());
        dialog.setFilterExtensions(ConfiguratorConstant.PACKAGE_FILE_EXTS);

        String file = dialog.open();

        if (file != null) {
            File f = new File(file);
            files.add(f);
            filesViewer.refresh();
            filesViewer.setSelection(new StructuredSelection(f));
            updateCommandLine();
        }
    }

    private void remove() {
        StructuredSelection selection = (StructuredSelection) filesViewer.getSelection();

        if (!selection.isEmpty()) {
            File nextFile = removeAndGetNext(files, (File) selection.getFirstElement());

            filesViewer.refresh();
            updateCommandLine();

            if (nextFile != null) {
                filesViewer.setSelection(new StructuredSelection(nextFile));
            }
        }
    }

    private File getCurrentDir() {
        StructuredSelection selection = (StructuredSelection) filesViewer.getSelection();
        File dir = null;

        if (!selection.isEmpty()) {
            File file = (File) selection.getFirstElement();
            File parent = file.getParentFile();

            if (parent != null) {
                dir = parent.getAbsoluteFile(); // 当前选中文件的父目录
            } else if (file.isDirectory()) {
                dir = file.getAbsoluteFile(); // 当前选中的目录
            }
        }

        if (dir == null) {
            dir = new File("").getAbsoluteFile(); // 当前目录
        }

        return dir;
    }

    private File removeAndGetNext(Collection files, File fileToRemove) {
        File nextFile = null;

        for (Iterator i = files.iterator(); i.hasNext();) {
            File file = (File) i.next();

            if (file.equals(fileToRemove)) {
                i.remove();

                if (i.hasNext()) {
                    nextFile = (File) i.next();
                }

                break;
            }
        }

        if (nextFile == null && !files.isEmpty()) {
            nextFile = (File) files.iterator().next();
        }

        return nextFile;
    }

    private void updateCommandLine() {
        StringBuffer buf = new StringBuffer();

        buf.append("antxconfig ");

        // descriptor patterns
        PatternSet descriptorPatterns = descriptorPatternsControl.getPatterns();

        buf.append(patternsToString("-d ", descriptorPatterns.getIncludes(), " "));
        buf.append(patternsToString("-D ", descriptorPatterns.getExcludes(), " "));

        // package patterns
        PatternSet packagePatterns = packagePatternsControl.getPatterns();

        buf.append(patternsToString("-p ", packagePatterns.getIncludes(), " "));
        buf.append(patternsToString("-P ", packagePatterns.getExcludes(), " "));

        // files
        for (Iterator i = getFiles().iterator(); i.hasNext();) {
            File file = (File) i.next();

            buf.append(file.getAbsolutePath()).append(" ");
        }

        String cmd = buf.toString();

        commandLine.setText(cmd);
    }

    private String patternsToString(String prefix, String[] patterns, String suffix) {
        if (patterns != null && patterns.length > 0) {
            return prefix + StringUtil.join(patterns, ",") + suffix;
        }

        return "";
    }

    /**
     * 代表一对include/exclude patterns。
     * 
     * @author Michael Zhou
     */
    private class PatternsGroup {
        private final Group composite;
        private final Text  includeInput;
        private final Text  excludeInput;

        public PatternsGroup(Composite parent, String text, String message, PatternSet patterns) {
            // Group
            composite = new Group(parent, SWT.NONE);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            composite.setText(text);

            // Layout
            GridLayout layout = new GridLayout(4, false);

            layout.marginWidth = 20;
            layout.marginHeight = 20;
            layout.horizontalSpacing = 10;
            layout.verticalSpacing = 10;

            composite.setLayout(layout);

            // Include
            Label includeLabel = new Label(composite, SWT.NONE);
            includeLabel.setText(Resources.getText("dialog.settings.patterns.include"));
            includeLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            includeInput = new Text(composite, SWT.BORDER | SWT.MULTI);
            includeInput.setLayoutData(new GridData(GridData.FILL_BOTH));

            if (patterns != null) {
                includeInput.setText(StringUtil.join(patterns.getIncludes(), "\n"));
            }

            includeInput.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateCommandLine();
                }
            });

            // Exclude
            Label excludeLabel = new Label(composite, SWT.NONE);
            excludeLabel.setText(Resources.getText("dialog.settings.patterns.exclude"));
            excludeLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
            excludeInput = new Text(composite, SWT.BORDER | SWT.MULTI);
            excludeInput.setLayoutData(new GridData(GridData.FILL_BOTH));

            if (patterns != null) {
                excludeInput.setText(StringUtil.join(patterns.getExcludes(), "\n"));
            }

            excludeInput.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    updateCommandLine();
                }
            });

            // Message
            Text messagelLabel = new Text(composite, SWT.READ_ONLY);
            GridData data = new GridData();

            data.horizontalSpan = 4;
            messagelLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE));
            messagelLabel.setText(message);
            messagelLabel.setLayoutData(data);
        }

        public PatternSet getPatterns() {
            String[] includes = StringUtil.split(includeInput.getText(), "\r\n, ");
            String[] excludes = StringUtil.split(excludeInput.getText(), "\r\n, ");

            return new PatternSet(includes, excludes);
        }
    }
}
