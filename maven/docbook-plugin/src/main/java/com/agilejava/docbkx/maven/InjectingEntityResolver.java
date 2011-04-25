package com.agilejava.docbkx.maven;

/*
 * Copyright 2006 Wilfred Springer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An <code>EntityResolver</code> that will inject a couple of additional
 * general internal entities into the DTD resolved by underlying resolver.
 * 
 * @author Wilfred Springer
 */
@SuppressWarnings("rawtypes")
public class InjectingEntityResolver implements EntityResolver {

    private final static String TYPE_ENTITY = "____type";

    /**
     * The <code>EntityResolver</code> wrapped by this implementation.
     */
    private EntityResolver resolver;

    /**
     * The entities to be injected. (Instances of {@link Entity}.)
     */
    private List entities;

    /**
     * A boolean to keep track of the fact if the entities already have been
     * injected.
     */
    private boolean injected;

    /**
     * The String representation of the type of conversion made. Used to
     * populate the docbkx.type entity.
     */
    private String type;

    /**
     * The Maven logger to be used for logging.
     */
    private Log log;

    public InjectingEntityResolver(List entities, EntityResolver resolver, String type, Log log) {
        this.resolver = resolver;
        this.entities = entities;
        this.injected = false;
        this.log = log;
        this.type = type;
    }

    /**
     * Returns the <code>InputSource</code> containing the external entity's
     * data. This implementation will normally return an
     * <code>InputSource</code> that contains all additional entities defined,
     * including a reference to the <code>InputSource</code> that was originally
     * requested. TODO Figure out if this works with all parsers. There might be
     * a couple of cases in which we would need to perform some additional work.
     * 
     * @param publicId The public identifier of the entity.
     * @param systemId The system identifier of the entity.
     * @return An <code>InputSource</code> producing the data required.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (systemId.endsWith("docbkx." + type + ".ent")) {
            log.debug("Handling type specific entities.");
            File file;
            try {
                file = new File(new URI(systemId));
                log.debug("Searching for " + file.getAbsolutePath());
                if (!file.exists()) {
                    log.debug("File does not exist, so returning empty file.");
                    return new InputSource(new StringReader(""));
                }
            } catch (URISyntaxException e) {
                // So let's treat this as an ordinary resource.
            }
        }

        if (systemId.endsWith("pom.ent")) {
            injected = true;
            ReplacementInputSource ris = new ReplacementInputSource(null);
            ris.createEntitiesFile(systemId);
            return ris;
        }

        InputSource source = resolver.resolveEntity(publicId, systemId);

        if (!injected) {
            injected = true;
            return new ReplacementInputSource(source == null ? null : source.getSystemId());
        } else {
            return source;
        }
    }

    /**
     * Forces the object to inject the entities.
     */
    public void forceInjection() {
        this.injected = false;
    }

    /**
     * The <code>InputSource</code> that will replace the first
     * <code>InputSource</code> retrieved, containig XML entity declarations for
     * all entities defined in the POM.
     */
    public class ReplacementInputSource extends InputSource {

        /**
         * The system identifier of the <code>InputSource</code> that should be
         * retrieved right after this one.
         */
        private String followUpSystemId;

        public ReplacementInputSource(String followUpSystemId) {
            this.followUpSystemId = followUpSystemId;
        }

        public void createEntitiesFile(String systemId) {
            try {
                File file = new File(new URI(systemId));
                Writer out = null;

                try {
                    File dir = file.getParentFile();

                    if ("target".equals(dir.getName())) {
                        dir.mkdirs();

                        out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), true);
                        out.write(createPlaceholderEntities());
                        log.info("Created " + file.getAbsolutePath());
                    }
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        @Override
        public InputStream getByteStream() {
            return null;
        }

        @Override
        public Reader getCharacterStream() {
            StringBuffer buffer = new StringBuffer();

            if (followUpSystemId != null) {
                buffer.append("<!ENTITY % ____next SYSTEM \"");
                buffer.append(followUpSystemId);
                buffer.append("\">\n");
                buffer.append("%____next;\n");
            }

            buffer.append(createPlaceholderEntities());

            buffer.append("<!ENTITY % ").append(TYPE_ENTITY).append(" SYSTEM \"docbkx.");
            buffer.append(type);
            buffer.append(".ent\">\n");
            buffer.append("%").append(TYPE_ENTITY).append(";\n");

            log.debug(buffer.toString());

            return new StringReader(buffer.toString());
        }

        private String createPlaceholderEntities() {
            StringBuffer buffer = new StringBuffer();

            Iterator iterator = entities.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();
                buffer.append("<!ENTITY ");
                buffer.append(entity.getName());
                buffer.append(" \"");
                buffer.append(entity.getValue());
                buffer.append("\">\n");
            }

            return buffer.toString();
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public void setByteStream(InputStream byteStream) {
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public void setPublicId(String publicId) {
        }

        @Override
        public void setSystemId(String systemId) {
        }

    }

}
