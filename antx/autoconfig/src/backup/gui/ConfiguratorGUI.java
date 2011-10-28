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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Shell;

import com.alibaba.antx.config.ConfigRuntimeImpl;
import com.alibaba.antx.config.gui.action.ExitAction;
import com.alibaba.antx.config.gui.action.SettingsAction;
import com.alibaba.antx.config.gui.resource.Resources;
import com.alibaba.antx.util.PatternSet;

/**
 * 代表GUI运行时数据。
 * 
 * @author Michael Zhou
 */
public class ConfiguratorGUI {
    private final MainWindow        mainWindow;
    private final ConfigRuntimeImpl runtime;
    private final SettingsAction        openAction;
    private final ExitAction        exitAction;
    private List                    entries;

    public ConfiguratorGUI(MainWindow mainWindow, ConfigRuntimeImpl runtime) {
        this.mainWindow = mainWindow;
        this.runtime = runtime;
        this.openAction = new SettingsAction(this);
        this.exitAction = new ExitAction(this);
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public Shell getShell() {
        return mainWindow.getShell();
    }

    protected void configureMenuManager(MenuManager rootMenu) {
        MenuManager fileMenu = new MenuManager(Resources.getText("menu.file"));

        rootMenu.add(fileMenu);

        fileMenu.add(openAction);
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);
    }

    protected void configureToolBarManager(ToolBarManager toolBarManager) {
        toolBarManager.add(openAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(exitAction);
    }

    public List getOpenFiles() {
        return Arrays.asList(runtime.getDestFiles());
    }

    public void setOpenFiles(Collection files) {
        runtime.setDestFiles((File[]) files.toArray(new File[files.size()]));
    }

    public PatternSet getDescriptorPatterns() {
        return runtime.getDescriptorPatterns();
    }

    public PatternSet getPackagePatterns() {
        return runtime.getPackagePatterns();
    }

    public void setDescriptorPatterns(PatternSet descriptorPatterns) {
        runtime.setDescriptorPatterns(descriptorPatterns.getIncludes(), descriptorPatterns.getExcludes());
    }

    public void setPackagePatterns(PatternSet packagePatterns) {
        runtime.setPackagePatterns(packagePatterns.getIncludes(), packagePatterns.getExcludes());
    }

    public List getEntries() {
        return entries;
    }

    public void scan() {
        this.entries = runtime.scan(true);
    }

    public String toString() {
        return "ConfiguratorGUI" + getOpenFiles().toString();
    }
}
