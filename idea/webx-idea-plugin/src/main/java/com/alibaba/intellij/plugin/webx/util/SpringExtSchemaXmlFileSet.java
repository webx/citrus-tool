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

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.intellij.plugin.webx.util.SpringExtPluginUtil.getSourceFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;
import com.alibaba.intellij.plugin.webx.extension.SpringExtFileMonitor;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.WildcardFileNameMatcher;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpringExtSchemaXmlFileSet extends SpringExtSchemaSet {
    private final static Key<CachedValue<SpringExtSchemaXmlFileSet>> CACHED_SCHEMAS_KEY = Key.create("Cached SpringExt Schemas");
    private final static Key<Module>                                 MODULE_KEY         = Key.create("Containing module");
    private final static Logger                                      log                = Logger.getInstance(SpringExtSchemaXmlFileSet.class);
    private final        ConcurrentHashMap<String, XmlFile>          nameToXmlFiles     = createConcurrentHashMap();

    @NotNull
    public static SpringExtSchemaXmlFileSet getInstance(@NotNull final Module module) {
        CachedValuesManager manager = CachedValuesManager.getManager(module.getProject());

        // Schemas是module作用域的。
        return manager.getCachedValue(module, CACHED_SCHEMAS_KEY, new CachedValueProvider<SpringExtSchemaXmlFileSet>() {
            public Result<SpringExtSchemaXmlFileSet> compute() {
                if (log.isDebugEnabled()) {
                    log.debug("Recompute schemas for module " + module.getName());
                }

                Project project = module.getProject();
                List<Object> dependencies = createLinkedList();

                dependencies.add(ProjectRootManager.getInstance(project));
                dependencies.add(SpringExtFileMonitor.getInstance(project));

                SpringExtSchemaXmlFileSet schemas = new SpringExtSchemaXmlFileSet(new IntellijResourceResolver(module, dependencies));

                schemas.transformAll(getAddPrefixTransformer(schemas, "http://localhost:8080/schema/"));

                return new Result<SpringExtSchemaXmlFileSet>(schemas, dependencies.toArray());
            }
        }, false);
    }

    private SpringExtSchemaXmlFileSet(@NotNull ResourceResolver resourceResolver) {
        super(resourceResolver);
    }

    @Nullable
    public Schema findSchemaByUrl(String url, PsiFile baseFile) {
        Schema schema = null;

        // 试着读取schemaLocation
        if (baseFile instanceof XmlFile) {
            String schemaLocation = ((XmlFile) baseFile).getRootTag().getAttributeValue("schemaLocation", "http://www.w3.org/2001/XMLSchema-instance");
            Map<String, String> schemaLocationMap = parseSchemaLocation(schemaLocation);
            String location = schemaLocationMap.get(url);

            if (location != null) {
                url = location;
            }
        }

        // Case 1: url represents a namespace url
        Set<Schema> namespaceSchemas = getNamespaceMappings().get(url);

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
        }

        // Case 2: url represents a schema location
        if (schema == null) {
            schema = findSchema(url); // by name
        }

        return schema;
    }

    @NotNull
    public XmlFile getSchemaXmlFile(@NotNull Schema schema, @NotNull Module module) {
        XmlFile xmlFile = nameToXmlFiles.get(schema.getName());

        if (xmlFile == null) {
            xmlFile = (XmlFile) PsiFileFactory.getInstance(module.getProject())
                                              .createFileFromText(schema.getName(), StdLanguages.XML, schema.getText());

            VirtualFile vfile = xmlFile.getVirtualFile();

            if (vfile instanceof LightVirtualFile) {
                ((LightVirtualFile) vfile).setWritable(false); // set as read only file
            }

            PsiFile originalFile = getSourceFile(schema, module);

            if (originalFile != null) {
                ((PsiFileImpl) xmlFile).setOriginalFile(originalFile);
            }

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

    /** 这个类负责从Intellij IDEA项目的模块及其依赖中，装载SpringExt所需要的一切文件。 */
    static class IntellijResourceResolver extends ResourceResolver {
        private final Logger log = Logger.getInstance(getClass());
        private final Project      project;
        private final Module       module;
        private final List<Object> dependencies;

        public IntellijResourceResolver(@NotNull Module module, @NotNull List<Object> dependencies) {
            this.project = module.getProject();
            this.module = module;
            this.dependencies = dependencies;
        }

        @Override
        @Nullable
        public Resource getResource(@NotNull String location) {
            Resource[] resources = getResources(location, true);

            if (resources.length > 0) {
                return resources[0];
            } else {
                return null;
            }
        }

        /**
         * 装载符合pattern的所有文件。
         * 这个实现只支持文件名中包含通配符<code>*</code>，例如：<code>META-INF/services/xxx-*.xsd</code>。
         * 这对于SpringExt目前的实现足够了。
         */
        @NotNull
        @Override
        public Resource[] getResources(@NotNull String locationPattern) throws IOException {
            return getResources(locationPattern, false);
        }

        @NotNull
        private Resource[] getResources(@NotNull String locationPattern, boolean singleResult) {
            String packageName = getParentPackageName(locationPattern);
            String fileName = getFileName(locationPattern);
            WildcardFileNameMatcher fileNameMatcher = fileName.contains("*") ? new WildcardFileNameMatcher(fileName) : null;

            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);

            if (psiPackage != null) {
                PsiDirectory[] psiDirectories = psiPackage.getDirectories(GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, true));
                List<PsiFile> files = createLinkedList();

                LOOP:
                for (PsiDirectory psiDirectory : psiDirectories) {
                    if (fileNameMatcher == null) {
                        PsiFile psiFile = psiDirectory.findFile(fileName);

                        if (psiFile != null) {
                            files.add(psiFile);

                            if (singleResult) {
                                break;
                            }
                        }
                    } else {
                        for (PsiFile file : psiDirectory.getFiles()) {
                            if (fileNameMatcher.accept(file.getName())) {
                                files.add(file);

                                if (singleResult) {
                                    break LOOP;
                                }
                            }
                        }
                    }
                }

                if (!files.isEmpty()) {
                    List<Resource> resources = createLinkedList();

                    for (PsiFile psiFile : files) {
                        dependencies.add(psiFile);

                        final VirtualFile virtualFile = psiFile.getVirtualFile();

                        if (virtualFile != null) {
                            resources.add(new VirtualFileResource(virtualFile));
                        } else {
                            log.warn("PsiFile was ignored, because it only exists in memory: " + psiFile);
                        }
                    }

                    return resources.toArray(new Resource[resources.size()]);
                }
            }

            return new Resource[0];
        }

        /** 将location的parent路径转换成package名称。 */
        @NotNull
        private String getParentPackageName(@NotNull String location) {
            return location.substring(0, location.lastIndexOf("/") + 1).replaceAll("^/|/$", "").replace("/", ".");
        }

        /** 取得location的文件名称。 */
        @NotNull
        private String getFileName(@NotNull String location) {
            return location.substring(location.lastIndexOf("/") + 1);
        }
    }

    public static class VirtualFileResource extends Resource {
        private final VirtualFile virtualFile;

        public VirtualFileResource(@NotNull VirtualFile virtualFile) {
            this.virtualFile = virtualFile;
        }

        public VirtualFile getVirtualFile() {
            return virtualFile;
        }

        @Override
        public String getName() {
            return virtualFile.getPath();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return virtualFile.getInputStream();
        }
    }
}