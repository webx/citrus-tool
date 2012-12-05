/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.intellij.plugin.webx.util;

import com.alibaba.intellij.plugin.webx.model.spring.Beans;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

public class SpringExtPluginUtil {
    public static boolean isSpringConfigurationFile(@NotNull XmlFile file) {
        return DomManager.getDomManager(file.getProject()).getFileElement(file, Beans.class) != null;
    }

    public static boolean isXsdFile(@NotNull XmlFile file) {
        VirtualFile virtualFile = file.getVirtualFile();

        if (virtualFile == null) {
            return false;
        }

        String extension = virtualFile.getExtension();

        return extension != null && extension.equals("xsd");
    }
}
