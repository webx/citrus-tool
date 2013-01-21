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

package com.alibaba.ide.plugin.intellij.springext.util;

import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.ide.plugin.intellij.springext.SpringExtConstant;
import com.alibaba.ide.plugin.intellij.springext.util.SpringExtSchemaXmlFileSet.VirtualFileResource;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringExtPluginUtil {
    public static boolean isSpringConfigurationFile(@NotNull XmlFile file) {
        return SpringExtConstant.BEANS_NAMESPACE_URI.equals(file.getRootTag().getNamespace()) && "beans".equals(file.getRootTag().getLocalName());
    }

    public static boolean isXsdFile(@NotNull XmlFile file) {
        VirtualFile virtualFile = file.getVirtualFile();

        if (virtualFile == null) {
            return false;
        }

        String extension = virtualFile.getExtension();

        return extension != null && extension.equals("xsd");
    }

    @Nullable
    public static Module findModule(@Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }

        return findModule(null, psiElement.getContainingFile());
    }

    @Nullable
    public static Module findModule(@Nullable PsiFile psiFile) {
        return findModule(null, psiFile);
    }

    @Nullable
    public static Module findModule(@Nullable Module module, @Nullable PsiFile psiFile) {
        if (module != null) {
            return module;
        }

        if (psiFile == null) {
            return null;
        }

        module = SpringExtSchemaXmlFileSet.getContainingModule(psiFile);

        if (module != null) {
            return module;
        }

        // 从文件或父文件中查找
        module = ModuleUtil.findModuleForPsiElement(psiFile);

        if (module != null) {
            return module;
        }

        PsiDirectory directory = psiFile.getParent();

        if (directory != null) {
            module = ModuleUtil.findModuleForPsiElement(directory);
        }

        return module;
    }

    public static boolean isCalledBy(@NotNull Class<?> clazz, @Nullable String methodName) {
        String className = clazz.getName();

        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (className.equals(e.getClassName()) && (methodName == null || methodName.equals(e.getMethodName()))) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public static PsiFile getSourceFile(@NotNull Schema schema, @NotNull Module module) {
        if (schema instanceof SourceInfo<?>) {
            return getSourceFile((SourceInfo<?>) schema, module);
        }

        return null;
    }

    @Nullable
    public static PsiFile getSourceFile(@NotNull SourceInfo<?> sourceInfo, @NotNull Module module) {
        Resource resource = sourceInfo.getSource();
        VirtualFile virtualFile = null;
        PsiFile psiFile = null;

        if (resource instanceof VirtualFileResource) {
            virtualFile = ((VirtualFileResource) resource).getVirtualFile();
        }

        if (virtualFile != null) {
            psiFile = PsiManager.getInstance(module.getProject()).findFile(virtualFile);
        }

        return psiFile;
    }
}
