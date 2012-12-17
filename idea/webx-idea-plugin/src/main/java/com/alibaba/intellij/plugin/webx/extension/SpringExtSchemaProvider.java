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

package com.alibaba.intellij.plugin.webx.extension;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.intellij.plugin.webx.util.SpringExtPluginUtil.*;
import static java.util.Collections.*;

import java.util.Set;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.intellij.plugin.webx.util.SpringExtSchemaXmlFileSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringExtSchemaProvider extends XmlSchemaProvider {
    private final Logger log = Logger.getInstance(getClass());

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

        SpringExtSchemaXmlFileSet schemas = SpringExtSchemaXmlFileSet.getInstance(module);
        Schema schema = schemas.findSchemaByUrl(url);

        XmlFile xmlFile = schema == null ? null
                                         : schemas.getSchemaXmlFile(schema, module);

        if (xmlFile != null && log.isDebugEnabled()) {
            log.debug(String.format("  - returns %s (%x)%n", xmlFile, xmlFile.hashCode()));
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

        SpringExtSchemaXmlFileSet schemas = SpringExtSchemaXmlFileSet.getInstance(module);
        return DefaultXmlExtension.filterNamespaces(schemas.getNamespaceMappings().keySet(), tagName, file);
    }

    @Nullable
    @Override
    public String getDefaultPrefix(@NotNull @NonNls String namespace, @NotNull XmlFile context) {
        Module module = findModule(null, context);

        if (module == null) {
            return null;
        }

        SpringExtSchemaXmlFileSet schemas = SpringExtSchemaXmlFileSet.getInstance(module);
        Set<Schema> namespaceSchemas = schemas.getNamespaceMappings().get(namespace);
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

        SpringExtSchemaXmlFileSet schemas = SpringExtSchemaXmlFileSet.getInstance(module);
        Set<Schema> namespaceSchemas = schemas.getNamespaceMappings().get(namespace);
        Schema schema;

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
            return createHashSet("http://localhost:8080/schema/" + schema.getName());
        }

        return null;
    }
}
