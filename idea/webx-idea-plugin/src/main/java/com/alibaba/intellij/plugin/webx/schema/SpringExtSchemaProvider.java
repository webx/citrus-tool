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

package com.alibaba.intellij.plugin.webx.schema;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.intellij.plugin.webx.util.SpringExtPluginUtil.*;
import static java.util.Collections.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;
import com.alibaba.citrus.springext.support.SchemaSet;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringExtSchemaProvider extends XmlSchemaProvider {
    private final Logger                          log                = Logger.getInstance(getClass());
    private final Key<CachedValue<SchemaSetInfo>> CACHED_SCHEMAS_KEY = Key.create("Cached SpringExt Schemas");
    private final Key<Module>                     MODULE_KEY         = Key.create("Containing module");

    @Override
    public boolean isAvailable(@NotNull XmlFile file) {
        return isSpringConfigurationFile(file) || isXsdFile(file);
    }

    @Override
    public XmlFile getSchema(@NotNull @NonNls String url, @Nullable Module module, @NotNull PsiFile baseFile) {
        // URL may be an empty namespace
        if (isBlank(url)) {
            return null;
        }

        module = findModule(module, baseFile);

        if (module == null) {
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Loading %s within %s in module %s%n", url, baseFile.getName(), module.getName()));
        }

        SchemaSetInfo schemasInfo = getSchemas(module);
        Schema schema = null;

        // Case 1: url represents a namespace url
        Set<Schema> namespaceSchemas = schemasInfo.schemas.getNamespaceMappings().get(url);

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
        }

        // Case 2: url represents a schema location
        if (schema == null) {
            schema = schemasInfo.schemas.findSchema(url);
        }

        XmlFile xmlFile = schema == null ? null
                                         : getOrCreateSchemaFile(schema, module, schemasInfo);

        if (xmlFile != null && log.isDebugEnabled()) {
            log.debug(String.format("  - returns %s (%x)%n", xmlFile, xmlFile.hashCode()));
        }

        return xmlFile;
    }

    @NotNull
    private XmlFile getOrCreateSchemaFile(@NotNull Schema schema, @NotNull Module module, @NotNull SchemaSetInfo schemaSetInfo) {
        XmlFile xmlFile = schemaSetInfo.files.get(schema.getName());

        if (xmlFile == null) {
            xmlFile = (XmlFile) PsiFileFactory.getInstance(module.getProject())
                                              .createFileFromText(schema.getName(), StdLanguages.XML, schema.getText());

            // 将module对象和内存中的XmlFile绑定，否则当系统试图读取这个file所include/import的另一个file时，会找不到module。
            xmlFile.putUserData(MODULE_KEY, module);

            schemaSetInfo.files.put(schema.getName(), xmlFile);
        }

        return xmlFile;
    }

    @NotNull
    @Override
    public Set<String> getAvailableNamespaces(@NotNull XmlFile file, @Nullable String tagName) {
        Module module = findModule(null, file);

        if (module == null) {
            return emptySet();
        }

        if (tagName == null) {
            SchemaSetInfo schemasInfo = getSchemas(module);
            return schemasInfo.schemas.getNamespaceMappings().keySet();
        }

        return emptySet();
    }

    @Nullable
    @Override
    public String getDefaultPrefix(@NotNull @NonNls String namespace, @NotNull XmlFile context) {
        Module module = findModule(null, context);

        if (module == null) {
            return null;
        }

        SchemaSetInfo schemasInfo = getSchemas(module);
        Set<Schema> namespaceSchemas = schemasInfo.schemas.getNamespaceMappings().get(namespace);
        Schema schema;

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
            return schema.getNamespacePrefix();
        }

        return null;
    }

    @Nullable
    @Override
    public Set<String> getLocations(@NotNull @NonNls String namespace, @NotNull XmlFile context) {
        Module module = findModule(null, context);

        if (module == null) {
            return null;
        }

        SchemaSetInfo schemasInfo = getSchemas(module);
        Set<Schema> namespaceSchemas = schemasInfo.schemas.getNamespaceMappings().get(namespace);
        Schema schema;

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
            return createHashSet("http://localhost:8080/schema/" + schema.getName());
        }

        return null;
    }

    @Nullable
    private Module findModule(@Nullable Module module, @NotNull PsiFile psiFile) {
        if (module != null) {
            return module;
        }

        // 对于从内存创建的file，无法知道它属于哪个module。
        // 但我们在创建它的时候，就已经把module保存在user data holder中了。
        module = psiFile.getUserData(MODULE_KEY);

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

    @NotNull
    private SchemaSetInfo getSchemas(final Module module) {
        CachedValuesManager manager = CachedValuesManager.getManager(module.getProject());

        // Schemas是module作用域的。
        return manager.getCachedValue(module, CACHED_SCHEMAS_KEY, new CachedValueProvider<SchemaSetInfo>() {
            public Result<SchemaSetInfo> compute() {
                if (log.isDebugEnabled()) {
                    log.debug("Recompute schemas for module " + module.getName());
                }

                return computeSchemas(module);
            }
        }, false);
    }

    @NotNull
    private Result<SchemaSetInfo> computeSchemas(@NotNull final Module module) {
        Project project = module.getProject();
        List<Object> dependencies = createLinkedList();

        dependencies.add(ProjectRootManager.getInstance(project));

        SchemaSet schemas = new SpringExtSchemaSet(new IntellijResourceResolver(module, dependencies));

        schemas.transformAll(getAddPrefixTransformer(schemas, "http://localhost:8080/schema/"));

        return new Result<SchemaSetInfo>(new SchemaSetInfo(schemas), dependencies.toArray());
    }

    private static class SchemaSetInfo {
        private final SchemaSet schemas;
        private final ConcurrentHashMap<String, XmlFile> files = createConcurrentHashMap();

        private SchemaSetInfo(@NotNull SchemaSet schemas) {
            this.schemas = schemas;
        }
    }
}
