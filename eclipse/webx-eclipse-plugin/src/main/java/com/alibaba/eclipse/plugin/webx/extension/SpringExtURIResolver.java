package com.alibaba.eclipse.plugin.webx.extension;

import static com.alibaba.eclipse.plugin.webx.util.SpringExtPluginUtil.*;
import static org.eclipse.jdt.core.IJavaElementDelta.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.eclipse.plugin.webx.util.SpringExtSchemaResourceSet;

@SuppressWarnings("restriction")
public class SpringExtURIResolver implements URIResolverExtension, IElementChangedListener {
    public SpringExtURIResolver() {
        JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
    }

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
    }

    public void elementChanged(ElementChangedEvent event) {
        int flagMasks = F_RESOLVED_CLASSPATH_CHANGED | F_CLASSPATH_CHANGED | F_CLOSED | F_OPENED | REMOVED | ADDED;

        for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
            if ((delta.getFlags() & flagMasks) != 0) {
                SpringExtSchemaResourceSet.resetForChangedElement(delta.getElement());
            }
        }
    }
}
