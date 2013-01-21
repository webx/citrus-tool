/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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

package com.alibaba.ide.plugin.idea.springext.extension;

import com.alibaba.citrus.springext.ContributionType;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

/**
 * 监视以下文件的创建。如果发现了，则更新cache。
 * <ul>
 * <li>spring.schemas</li>
 * <li>spring.configuration-points</li>
 * <li>*.bean-definition-parsers</li>
 * <li>*.bean-definition-decorators</li>
 * <li>*.bean-definition-decorators-for-attribute</li>
 * <li>*.xsd</li>
 * </ul>
 *
 * @author Michael Zhou
 */
public class SpringExtFileMonitor implements ProjectComponent, ModificationTracker {
    private final Project project;
    private       long    count;

    public static SpringExtFileMonitor getInstance(@NotNull Project project) {
        return project.getComponent(SpringExtFileMonitor.class);
    }

    public SpringExtFileMonitor(@NotNull Project project) {
        this.project = project;
    }

    public long getModificationCount() {
        return count;
    }

    public void projectOpened() {
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
            public void fileCreated(VirtualFileEvent event) {
                VirtualFile virtualFile = event.getFile();
                String fileName = virtualFile.getName();

                if ("spring.schemas".equals(fileName)
                    || "spring.configuration-points".equals(fileName)
                    || fileName.endsWith(ContributionType.BEAN_DEFINITION_PARSER.getContributionsLocationSuffix())
                    || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR.getContributionsLocationSuffix())
                    || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE.getContributionsLocationSuffix())
                    || fileName.toLowerCase().endsWith(".xsd")) {
                    count++;
                }
            }
        }, project);
    }

    public void projectClosed() {
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return getClass().getName();
    }
}
