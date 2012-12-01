/*
 * Copyright 2010 Alibaba Group Holding Limited.
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

package com.alibaba.maven.plugin.springext;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

/**
 * Start jetty server and run SchemaExporterServlet.
 *
 * @author Michael Zhou
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="test-compile"
 * @description Runs SchemaExporterServlet directly from a maven project
 */
public class SchemaExporterRunnerMojo extends AbstractSchemaExporterMojo {
    /**
     * The port for connector. Defaults to 8080.
     *
     * @parameter expression="${port}" default-value="8080"
     * @required
     */
    private int port;

    /**
     * The context path for the webapp. Defaults to "/schema".
     *
     * @parameter expression="${contextPath}" default-value="/"
     * @required
     */
    private String contextPath;

    /**
     * The root directory to use for the webapp. Defaults to target/jetty/work.
     *
     * @parameter expression="${project.build.directory}/jetty/work"
     * @required
     */
    private File webappDirectory;

    /**
     * The temporary directory to use for the webapp. Defaults to
     * target/jetty/temp.
     *
     * @parameter expression="${project.build.directory}/jetty/temp"
     * @required
     */
    private File tempDirectory;

    /**
     * The location of the web.xml. If not set then it is assumed it is
     * META-INF/web-springext-helper.xml
     *
     * @parameter default-value="META-INF/web-springext-helper.xml"
     * @required
     */
    private String webXml;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Configuring Jetty for project: " + project.getName());

            Server server = createServer();
            Connector connector = createConnector();

            getLog().debug("Setting Connector: " + connector.getClass().getName() + " on port " + connector.getPort());

            server.setConnectors(new Connector[] { connector });
            server.setHandler(createHandler());

            server.start();

            getLog().info("Started Jetty Server");

            server.join();
        } catch (Exception e) {
            throw new MojoExecutionException("Failure: " + e.getClass().getName() + " " + e.getMessage(), e);
        } finally {
            getLog().info("Jetty server exiting.");
        }
    }

    private Server createServer() {
        Server server = new Server();

        server.setStopAtShutdown(true);

        return server;
    }

    private Connector createConnector() {
        SelectChannelConnector connector = new SelectChannelConnector();

        connector.setPort(port);
        connector.setMaxIdleTime(30000);

        return connector;
    }

    private Handler createHandler() throws Exception {
        HandlerCollection handlers = new HandlerCollection();

        // handler1
        ContextHandlerCollection contexts = new ContextHandlerCollection();

        contexts.addHandler(createWebAppContext());

        // handler2
        DefaultHandler defaultHandler = new DefaultHandler();

        // handler3
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        NCSARequestLog requestLog = new NCSARequestLog();

        requestLog.setLogServer(true);
        requestLog.setLogLocale(Locale.ENGLISH);
        requestLogHandler.setRequestLog(requestLog);

        handlers.setHandlers(new Handler[] { contexts, defaultHandler, requestLogHandler });

        return handlers;
    }

    private WebAppContext createWebAppContext() throws Exception {
        WebAppContext context = new WebAppContext();

        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        tempDirectory.mkdirs();
        webappDirectory.mkdirs();

        if (!tempDirectory.isDirectory()) {
            throw new IllegalArgumentException("missing temporary directory: " + tempDirectory.getCanonicalPath());
        }

        if (!webappDirectory.isDirectory()) {
            throw new IllegalArgumentException("missing webapp directory: " + webappDirectory.getCanonicalPath());
        }

        context.setContextPath(contextPath);
        context.setTempDirectory(tempDirectory);
        context.setWar(webappDirectory.getCanonicalPath());
        context.setDescriptor(webXml);
        context.setConfigurations(createConfigurations());

        getLog().info("Context path     = " + context.getContextPath());
        getLog().info("Webapp directory = " + context.getWar());
        getLog().info("Temp directory   = " + context.getTempDirectory());
        getLog().info("Web defaults     = " + context.getDefaultsDescriptor());
        getLog().info("Web descriptor   = " + context.getDescriptor());

        return context;
    }

    private Configuration[] createConfigurations() throws Exception {
        return new Configuration[] { new MavenConfiguration() };
    }

    private final class MavenConfiguration extends WebXmlConfiguration {
        private static final long serialVersionUID = -6818576369445258461L;

        public MavenConfiguration() throws ClassNotFoundException {
            super();
        }

        @Override
        public void preConfigure(WebAppContext context) throws Exception {
            configureClassLoader(context);
            super.preConfigure(context);
        }

        private void configureClassLoader(WebAppContext context) throws Exception {
            List classPathFiles = createClassPath();

            if (classPathFiles != null) {
                WebAppClassLoader cl = (WebAppClassLoader) context.getClassLoader();

                for (Iterator i = classPathFiles.iterator(); i.hasNext(); ) {
                    cl.addClassPath(((File) i.next()).getCanonicalPath());
                }
            }

            // knock out environmental maven and plexus classes from webAppContext
            String[] existingServerClasses = context.getServerClasses();
            String[] newServerClasses = new String[2 + (existingServerClasses == null ? 0
                                                                                      : existingServerClasses.length)];

            newServerClasses[0] = "-org.apache.maven.";
            newServerClasses[1] = "-org.codehaus.plexus.";

            System.arraycopy(existingServerClasses, 0, newServerClasses, 2, existingServerClasses.length);

            context.setServerClasses(newServerClasses);
        }

        @Override
        protected Resource findWebXml(WebAppContext context) throws IOException, MalformedURLException {
            Resource result = super.findWebXml(context);

            if (result == null) {
                String descriptor = context.getDescriptor();
                ClassLoader cl = context.getClassLoader();

                URL url = cl.getResource(descriptor);

                if (url != null) {
                    result = Resource.newResource(url);
                }
            }

            if (result == null) {
                throw new IOException("Could not find SchemaExporter resources.\n" + "Please make sure this project ("
                                      + project.getId() + ") depends on \"citrus-common-springext\"");
            }

            return result;
        }
    }
}
