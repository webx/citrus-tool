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

package com.alibaba.intellij.plugin.webx.xml.schema;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringExtSchemaXmlFileSet extends SpringExtSchemaSet {
    private final static Key<Module>                        MODULE_KEY     = Key.create("Containing module");
    private final        ConcurrentHashMap<String, XmlFile> nameToXmlFiles = createConcurrentHashMap();

    public SpringExtSchemaXmlFileSet(@NotNull ResourceResolver resourceResolver) {
        super(resourceResolver);
    }

    @NotNull
    public XmlFile getSchemaXmlFile(@NotNull Schema schema, @NotNull Module module) {
        XmlFile xmlFile = nameToXmlFiles.get(schema.getName());

        if (xmlFile == null) {
            xmlFile = (XmlFile) PsiFileFactory.getInstance(module.getProject())
                                              .createFileFromText(schema.getName(), StdLanguages.XML, schema.getText());

            // 将module对象和内存中的XmlFile绑定，否则当系统试图读取这个file所include/import的另一个file时，会找不到module。
            xmlFile.putUserData(MODULE_KEY, module);

            nameToXmlFiles.put(schema.getName(), xmlFile);
        }

        return xmlFile;
    }

    /**
     * 对于从内存创建的file，无法知道它属于哪个module。
     * 但我们在创建它的时候，就已经把module保存在user data holder中了。
     */
    @Nullable
    public static Module getContainingModule(@NotNull PsiFile psiFile) {
        return psiFile.getUserData(MODULE_KEY);
    }
}