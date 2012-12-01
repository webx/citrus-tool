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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Exports schema files to specified directory.
 *
 * @author Michael Zhou
 * @goal export
 * @requiresDependencyResolution runtime
 * @execute phase="test-compile"
 * @description Runs SchemaExporter to save schema files within current maven
 * project to specified directory.
 */
public class SchemaExporterExecMojo extends AbstractSchemaExporterMojo {
    private final static String mainClass = "com.alibaba.citrus.springext.export.SchemaExporterCLI";

    /**
     * The temporary directory to use for the webapp. Defaults to
     * target/schemas.
     *
     * @parameter expression="${destdir}"
     * default-value="${project.build.directory}/schemas"
     * @required
     */
    private File destdir;

    /**
     * The URI prefix used to replace the absolute URI imported or included in
     * schemas.
     *
     * @parameter expression="${uriPrefix}"
     */
    private String uriPrefix;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ClassLoader cl = createClassLoader();
            String[] args = createArgs();

            if (getLog().isDebugEnabled()) {
                StringBuffer msg = new StringBuffer();

                msg.append("Invoking : ").append(mainClass).append(".main(");

                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        msg.append(", ");
                    }

                    msg.append(args[i]);
                }

                msg.append(")");

                getLog().debug(msg.toString());
            }

            run(cl, mainClass, args);
        } catch (Exception e) {
            throw new MojoExecutionException("Failure: " + e.getClass().getName() + " " + e.getMessage(), e);
        }
    }

    private void run(ClassLoader cl, String mainClass, String[] args) throws Exception {
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(cl);

            Class clazz = cl.loadClass(mainClass);

            Method main = clazz.getMethod("main", new Class[] { String[].class });

            if (!main.isAccessible()) {
                main.setAccessible(true);
            }

            main.invoke(main, new Object[] { args });
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    private String[] createArgs() throws Exception {
        List args = new ArrayList(3);

        if (getLog().isDebugEnabled()) {
            args.add("-debug");
        }

        args.add(destdir.getCanonicalPath());

        if (uriPrefix != null) {
            args.add(uriPrefix);
        }

        return (String[]) args.toArray(new String[args.size()]);
    }

    private ClassLoader createClassLoader() throws Exception {
        List classPathFiles = createClassPath();
        URL[] urls = new URL[classPathFiles.size()];

        for (int i = 0; i < classPathFiles.size(); i++) {
            urls[i] = ((File) classPathFiles.get(i)).toURI().toURL();
        }

        return new URLClassLoader(urls);
    }
}
