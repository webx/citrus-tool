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

package com.alibaba.ide.plugin.intellij.springext.extension;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.intellij.springext.util.SpringExtPluginUtil.*;

import java.util.List;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.ide.plugin.intellij.springext.util.SpringExtSchemaXmlFileSet;
import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nullable;

public class SpringExtGotoDeclarationHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        if (!isCalledBy(GotoDeclarationAction.class, "invoke")) {
            // 这是一个hack！此方法会被调用两次，一次用于highlight link，另一次用于打开link所指向的文件。
            // 只有在第二次才需要返回值，否则highlight schemaLocation时会不正确。
            return null;
        }

        PsiReference ref = TargetElementUtilBase.findReference(editor, offset);
        String url = ref == null ? null : trimToNull(ref.getCanonicalText());

        if (url == null) {
            return null;
        }

        Module module = findModule(sourceElement);

        if (module == null) {
            return null;
        }

        SpringExtSchemaXmlFileSet schemas = SpringExtSchemaXmlFileSet.getInstance(module);
        PsiFile baseFile = PsiDocumentManager.getInstance(module.getProject()).getPsiFile(editor.getDocument());
        Schema schema = schemas.findSchemaByUrl(url, baseFile);

        if (schema == null) {
            return null;
        }

        List<PsiElement> files = createLinkedList();

        files.add(schemas.getSchemaXmlFile(schema, module));

        if (schema instanceof SourceInfo<?>) {
            for (SourceInfo<?> sourceInfo = (SourceInfo<?>) schema; sourceInfo != null; sourceInfo = sourceInfo.getParent()) {
                PsiFile psiFile = getSourceFile(sourceInfo, module);

                if (psiFile != null) {
                    files.add(psiFile);
                }
            }
        }

        return files.toArray(new PsiElement[files.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext context) {
        return null;
    }
}
