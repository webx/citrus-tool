/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Tool to convert a SpringExt configuration file into new unqualified-style for Webx 3.2.x.
 *
 * @author Michael Zhou
 * @goal convert
 * @description Migrate Webx configuration files to new unqualified style.
 */
public class ConfigurationConvertMojo extends AbstractSpringExtMojo {
    /** @parameter expression="${includes}" */
    private String includes;

    /** @parameter expression="${excludes}" */
    private String excludes;

    /** @parameter expression="${forceConvert}" */
    private boolean forceConvert;

    /** @parameter expression="${noBackup}" */
    private boolean noBackup;

    public String[] getIncludes() {
        return getStringList(includes, "**/*.xml");
    }

    public String[] getExcludes() {
        String basedir = getCurrentProject().getBasedir().getAbsolutePath();
        String target = getCurrentProject().getBuild().getOutputDirectory();
        String pattern = null;

        if (target.startsWith(basedir)) {
            pattern = "**/" + target.substring(basedir.length()).replace('\\', '/').replaceAll("^/+", "").replaceAll("/.*$", "") + "/**";
        }

        return getStringList(excludes, pattern);
    }

    private String[] getStringList(String value, String defaultValue) {
        List<String> list = new ArrayList<String>();

        if (value == null) {
            value = defaultValue;
        }

        if (value != null) {
            for (String s : value.split(",| ")) {
                list.add(s);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        ClassLoader cl = createClassLoader();
        Class c;

        try {
            c = cl.loadClass("com.alibaba.citrus.springext.util.ConvertToUnqualifiedStyle");
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Webx 3.2.x is needed to support unqualified-style configurations");
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            // set new class loader as a context class loader
            Thread.currentThread().setContextClassLoader(cl);

            Object o = c.getConstructor(File[].class, boolean.class, boolean.class).newInstance(getSources(), forceConvert, !noBackup);
            c.getMethod("convert").invoke(o);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to do conversion", e);
        } finally {
            // restore original class loader
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private File[] getSources() {
        DirectoryScanner scanner = new DirectoryScanner();
        File basedir = new File("").getAbsoluteFile();
        String[] includes = getIncludes();
        String[] excludes = getExcludes();

        scanner.setBasedir(basedir);
        scanner.setIncludes(includes);
        scanner.setExcludes(excludes);

        getLog().info("Looking for files in \n" +
                      "  basedir  =" + scanner.getBasedir().getAbsolutePath() + "\n"
                      + "  includes =" + Arrays.toString(includes) + "\n"
                      + "  excludes =" + Arrays.toString(excludes));

        scanner.scan();

        String[] sources = scanner.getIncludedFiles();

        if (sources == null) {
            return new File[0];
        }

        File[] files = new File[sources.length];

        for (int i = 0; i < files.length; i++) {
            files[i] = new File(sources[i]);
        }

        getLog().info("Found " + files.length + " files\n");

        return files;
    }

    private ClassLoader createClassLoader() {
        List<File> classPathFiles = createClassPath();
        List<URL> urls = new LinkedList<URL>();

        for (File file : classPathFiles) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException ignored) {
            }
        }

        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }
}
