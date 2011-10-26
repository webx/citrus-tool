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
 */

package com.alibaba.antx.config.gui;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.alibaba.antx.config.ConfigRuntimeImpl;
import com.alibaba.antx.config.entry.ConfigEntry;
import com.alibaba.antx.config.gui.resource.Resources;

public class MainWindow extends ApplicationWindow {
    private final ConfiguratorGUI gui;

    /**
     * Create the application window
     */
    public MainWindow(ConfigRuntimeImpl runtime) {
        super(null);

        this.gui = new ConfiguratorGUI(this, runtime);

        addMenuBar();
        addToolBar(SWT.FLAT | SWT.WRAP);
        addStatusLine();
    }

    /**
     * Create contents of the application window
     * 
     * @param parent
     */
    protected Control createContents(Composite parent) {
        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
        Composite leftPane = new Composite(sashForm, SWT.BORDER);
        Composite rightPane = new Composite(sashForm, SWT.BORDER);

        leftPane.setLayout(new GridLayout());
        rightPane.setLayout(new GridLayout());

        TreeViewer entriesViewer = new TreeViewer(leftPane, SWT.BORDER);

        entriesViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        entriesViewer.setContentProvider(new ITreeContentProvider() {
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof List) {
                    List entriesList = (List) parentElement;
                    return (ConfigEntry[]) entriesList.toArray(new ConfigEntry[entriesList.size()]);
                }

                return null;
            }

            public Object getParent(Object element) {
                return null;
            }

            public boolean hasChildren(Object element) {
                return false;
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    List entriesList = (List) inputElement;
                    return (ConfigEntry[]) entriesList.toArray(new ConfigEntry[entriesList.size()]);
                }

                return null;
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        gui.scan();
        entriesViewer.setInput(gui.getEntries());

        return sashForm;
    }

    /**
     * Create the menu manager
     * 
     * @return the menu manager
     */
    protected MenuManager createMenuManager() {
        MenuManager menuManager = new MenuManager(null);

        gui.configureMenuManager(menuManager);

        return menuManager;
    }

    /**
     * Create the toolbar manager
     * 
     * @return the toolbar manager
     */
    protected ToolBarManager createToolBarManager(int style) {
        ToolBarManager toolBarManager = new ToolBarManager(style);

        gui.configureToolBarManager(toolBarManager);

        return toolBarManager;
    }

    /**
     * Create the status line manager
     * 
     * @return the status line manager
     */
    protected StatusLineManager createStatusLineManager() {
        StatusLineManager statusLineManager = new StatusLineManager();

        return statusLineManager;
    }

    /**
     * Configure the shell
     * 
     * @param newShell
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Resources.getText("app.title"));
    }

    /**
     * Return the initial size of the window
     */
    protected Point getInitialSize() {
        return new Point(500, 375);
    }

    public void handleShellCloseEvent() {
        if (MessageDialog.openQuestion(getShell(), "退出提示", "你已经修改了资源/权限的映射规则，如果现在退出，所做的改动将不会保存，确定退出么？")) {
            super.handleShellCloseEvent();
        }
    }

    public static void run(ConfigRuntimeImpl runtime) {
        MainWindow window = new MainWindow(runtime);
        window.setBlockOnOpen(true);
        window.open();
        Display.getCurrent().dispose();
    }
}
