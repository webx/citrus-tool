package com.alibaba.ide.plugin.eclipse.springext.resolver;

import static com.alibaba.citrus.util.StringUtil.isBlank;
import static com.alibaba.citrus.util.StringUtil.trimToNull;
import static com.alibaba.ide.plugin.eclipse.springext.resolver.SpringExtURLUtil.getProjectFromURL;
import static com.alibaba.ide.plugin.eclipse.springext.resolver.SpringExtURLUtil.toSpringextURL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

@SuppressWarnings("restriction")
public class SpringExtSchemaResolver implements URIResolverExtension {
    private static final Logger log = LoggerFactory.getLogger(SpringExtSchemaResolver.class);

    public String resolve(IFile file, String baseLocation, String publicId, String systemId) {
        String result = null;
        String urlToResolve = isBlank(systemId) ? trimToNull(publicId) : trimToNull(systemId);

        if (urlToResolve != null) {
            IProject project = getProject(file, baseLocation);

            if (project != null) {
                SchemaResourceSet schemas = SchemaResourceSet.getInstance(project);

                if (schemas != null) {
                    Schema schema = schemas.findSchemaByUrl(urlToResolve);

                    if (schema != null) {
                        result = toSpringextURL(project, schema);
                    }
                }
            }
        }

        if (result != null && log.isDebugEnabled()) {
            log.debug("Resolved schema: {}", result);
        }

        return result;
    }

    private IProject getProject(IFile file, String baseLocation) {
        IProject project = null;

        // 优先查找Java Project
        // 情况解释：Eclipse最近的更新导致，一个file可能会找到parent pom project，这时就无法生成classpath。
        // 这里优先找出file对应的java project，以确保schema set可读出。
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.getLocationURI());

        if (files != null) {
            for (IFile f : files) {
                if (SpringExtPluginUtil.getJavaProject(f.getProject(), false) != null) {
                    file = f;
                    break;
                }
            }
        }

        if (file != null) {
            project = file.getProject();

        }

        if (project == null) {
            project = getProjectFromURL(baseLocation);
        }

        return project;
    }
}
