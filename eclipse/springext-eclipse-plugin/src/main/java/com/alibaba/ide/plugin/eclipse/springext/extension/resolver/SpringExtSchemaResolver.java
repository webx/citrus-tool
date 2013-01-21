package com.alibaba.ide.plugin.eclipse.springext.extension.resolver;

import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.extension.resolver.SpringExtURLUtil.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

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

        if (file != null) {
            project = file.getProject();
        }

        if (project == null) {
            project = getProjectFromURL(baseLocation);
        }

        return project;
    }
}
