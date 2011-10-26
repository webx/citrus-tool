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

package com.alibaba.antx.config.gui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import com.alibaba.antx.config.gui.ConfiguratorGUI;
import com.alibaba.antx.config.gui.dialog.SettingsDialog;
import com.alibaba.antx.config.gui.resource.Resources;

public class SettingsAction extends Action {
    private final ConfiguratorGUI gui;

    public SettingsAction(ConfiguratorGUI gui) {
        super(Resources.getText("menu.file.settings"), AS_PUSH_BUTTON);
        setToolTipText(Resources.getText("menu.file.settings.tip"));
        setImageDescriptor(Resources.getImageDescriptor("menu.file.settings.image"));

        this.gui = gui;
    }

    public void run() {
        SettingsDialog od = new SettingsDialog(gui);

        if (od.open() == Dialog.OK) {
            gui.setOpenFiles(od.getFiles());
            gui.setDescriptorPatterns(od.getDescriptorPatterns());
            gui.setPackagePatterns(od.getPackagePatterns());

            open();
        }
    }

    private void open() {

    }
}
