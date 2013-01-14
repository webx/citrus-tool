package com.alibaba.eclipse.plugin.webx.extension.resolver;

import static com.alibaba.eclipse.plugin.webx.extension.resolver.SpringExtURLUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.resources.IProject;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.Schema;

public class SpringExtURLStreamHandler extends AbstractURLStreamHandlerService {
    private static final Logger log = LoggerFactory.getLogger(SpringExtURLStreamHandler.class);

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return new URLConnection(url) {
            @Override
            public void connect() throws IOException {
            }

            @Override
            public InputStream getInputStream() throws IOException {
                IProject project = getProjectFromURL(url);

                if (project != null) {
                    SpringExtSchemaResourceSet schemas = SpringExtSchemaResourceSet.getInstance(project);

                    if (schemas != null) {
                        String schemaName = getSchemaNameFromURL(url);
                        Schema schema = schemas.getNamedMappings().get(schemaName);

                        if (schema != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Loading schema: {}", url);
                            }

                            return schema.getInputStream();
                        }
                    }
                }

                throw new IOException("Unable to open stream for URL: " + url);
            }
        };
    }
}
