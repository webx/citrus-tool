package com.alibaba.intellij.plugin.webx.xml.schema;

import static com.alibaba.citrus.util.CollectionUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.alibaba.citrus.springext.ResourceResolver;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.WildcardFileNameMatcher;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 这个类负责从Intellij IDEA项目的模块及其依赖中，装载SpringExt所需要的一切文件。
 *
 * @author Michael Zhou
 */
class IntellijResourceResolver extends ResourceResolver {
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
                        resources.add(new Resource() {
                            @Override
                            public String getName() {
                                return virtualFile.getPath();
                            }

                            @Override
                            public InputStream getInputStream() throws IOException {
                                return virtualFile.getInputStream();
                            }
                        });
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
