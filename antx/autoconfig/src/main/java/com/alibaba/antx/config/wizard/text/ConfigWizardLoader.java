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

package com.alibaba.antx.config.wizard.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.antx.config.ConfigConstant;
import com.alibaba.antx.config.ConfigSettings;
import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.entry.ConfigEntry;
import com.alibaba.antx.config.props.PropertiesSet;

public class ConfigWizardLoader {
    private final ConfigSettings settings;
    private final List configEntries;
    private final ConfigDescriptor inlineDescriptor;

    public ConfigWizardLoader(ConfigSettings settings, List configEntries) {
        this.settings = settings;
        this.configEntries = configEntries;
        this.inlineDescriptor = null;
    }

    public ConfigWizardLoader(ConfigSettings settings, ConfigDescriptor inlineDescription) {
        this.settings = settings;
        this.configEntries = null;
        this.inlineDescriptor = inlineDescription;
    }

    public void loadAndStart() {
        // 使用wizard验证并和用户交互
        ConfigDescriptor[] descriptors = getAllDescriptors();
        PropertiesSet props = settings.getPropertiesSet();
        ConfigWizard wizard = new ConfigWizard(descriptors, props, settings.getCharset());
        boolean valid = wizard.validate();
        String interactiveMode = settings.getInteractiveMode();
        boolean interactiveAuto = ConfigConstant.INTERACTIVE_AUTO.equals(interactiveMode);
        boolean interactiveOn = ConfigConstant.INTERACTIVE_ON.equals(interactiveMode);

        if (interactiveOn || (interactiveAuto && !valid)) {
            if (!valid) {
                StringBuffer confirm = new StringBuffer();

                confirm.append("╭───────────────────────┈┈┈┈\n");
                confirm.append("│\n");
                confirm.append("│ 您的配置文件需要被更新：\n");
                confirm.append("│\n");
                confirm.append("│ ").append(props.getUserPropertiesFile().getURI()).append("\n");
                confirm.append("│\n");
                confirm.append("│ 这个文件包括了您个人的特殊设置，\n");
                confirm.append("│ 包括服务器端口、您的邮件地址等内容。\n");
                confirm.append("│\n");
                confirm.append("└───────┈┈┈┈┈┈┈┈┈┈┈\n");
                confirm.append("\n").append(" 如果不更新此文件，可能会导致配置文件的内容不完整。\n");
                confirm.append(" 您需要现在更新此文件吗?");

                wizard.setConfirmMessage(confirm.toString());
            }

            wizard.start();

            valid = wizard.validate();
        }

        // 设置valid变量为true，或抛出异常
        if (!valid) {
            throw new ConfigWizardException("因为配置文件“" + props.getUserPropertiesFile().getURI() + "”未准备好，所以无法继续下去！");
        }
    }

    /**
     * 取得所有的descriptors。
     * 
     * @return 所有descriptors的数组
     */
    private ConfigDescriptor[] getAllDescriptors() {
        if (configEntries != null) {
            List descriptors = new ArrayList();

            for (Iterator i = configEntries.iterator(); i.hasNext();) {
                ConfigEntry entry = (ConfigEntry) i.next();

                addConfigEntryRecursive(entry, descriptors);
            }

            return (ConfigDescriptor[]) descriptors.toArray(new ConfigDescriptor[descriptors.size()]);
        } else if (inlineDescriptor != null) {
            return new ConfigDescriptor[] { inlineDescriptor };
        } else {
            return new ConfigDescriptor[0];
        }
    }

    /**
     * 将entry及所有子entry中的descriptors加入列表中。
     * 
     * @param entry config entry
     * @param descriptors descriptors列表
     */
    private void addConfigEntryRecursive(ConfigEntry entry, List descriptors) {
        if (entry == null) {
            return;
        }

        ConfigDescriptor[] entryDescriptors = entry.getGenerator().getConfigDescriptors();

        for (int i = 0; i < entryDescriptors.length; i++) {
            descriptors.add(entryDescriptors[i]);
        }

        ConfigEntry[] subEntries = entry.getSubEntries();

        for (int i = 0; i < subEntries.length; i++) {
            addConfigEntryRecursive(subEntries[i], descriptors);
        }
    }
}
