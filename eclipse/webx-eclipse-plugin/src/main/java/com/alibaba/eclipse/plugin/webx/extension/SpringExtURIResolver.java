package com.alibaba.eclipse.plugin.webx.extension;

import static com.alibaba.eclipse.plugin.webx.util.SpringExtPluginUtil.getProjectFromURL;
import static com.alibaba.eclipse.plugin.webx.util.SpringExtPluginUtil.toSpringextURL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.eclipse.plugin.webx.util.SpringExtSchemaResourceSet;

@SuppressWarnings("restriction")
public class SpringExtURIResolver implements URIResolverExtension {
    public String resolve(IFile file, String baseLocation, String publicId, String systemId) {
        IProject project = null;

        if (file != null) {
            project = file.getProject();
        }

        if (project == null) {
            project = getProjectFromURL(baseLocation);
        }

        if (project != null) {
            SpringExtSchemaResourceSet schemas = SpringExtSchemaResourceSet.getInstance(project);

            if (schemas != null) {
                Schema schema = schemas.findSchemaByUrl(systemId == null ? publicId : systemId);

                if (schema != null) {
                    return toSpringextURL(project, schema);
                }
            }
        }

        return null;
    };
}
