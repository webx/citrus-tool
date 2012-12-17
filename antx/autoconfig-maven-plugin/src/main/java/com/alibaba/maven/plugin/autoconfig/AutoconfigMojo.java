/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
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

package com.alibaba.maven.plugin.autoconfig;

import java.io.File;

import com.alibaba.antx.config.ConfigRuntimeImpl;
import com.alibaba.antx.expand.ExpanderRuntimeImpl;
import com.alibaba.antx.util.CharsetUtil;
import com.alibaba.citrus.logconfig.LogConfigurator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Maven plugin to invoke antx-autoconfig.
 *
 * @author Michael Zhou
 * @goal autoconfig
 * @phase package
 */
public class AutoconfigMojo extends AbstractMojo {
    /**
     * Package file or exploded dir to config.
     *
     * @parameter expression="${project.artifact.file}"
     * @required
     */
    private File dest;

    /**
     * exploding package to directory.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    private File explodedDirectory;

    /**
     * Whether or not to exploding package into directory.
     *
     * @parameter expression="${autoconfig.exploding}" default-value="false"
     */
    private boolean exploding;

    /**
     * Charset encoding of console.
     *
     * @parameter expression="${autoconfig.charset}"
     */
    private String charset;

    /**
     * Strict mode.
     *
     * @parameter expression="${autoconfig.strict}" default-value="true"
     */
    private boolean strict;

    /**
     * Interactive mode switch.
     *
     * @parameter expression="${autoconfig.interactive}"
     */
    private Boolean interactive;

    /**
     * Skipping autoconfig.
     *
     * @parameter expression="${autoconfig.skip}"
     */
    private boolean skip;

    /**
     * Package type: war, jar, ear, etc.
     *
     * @parameter expression="${autoconfig.type}"
     */
    private String type;

    /**
     * User properties file.
     *
     * @parameter expression="${autoconfig.userProperties}"
     */
    private File userProperties;

    /**
     * Includes/excludes descriptor files, e.g. conf/auto-config.xml.
     * Wildcards such as "*", "**", "?" are allowed.
     *
     * @parameter
     */
    private Patterns descriptors;

    /**
     * Includes/excludes package files, e.g. target/*.war.
     * Wildcards such as "*", "**", "?" are allowed.
     *
     * @parameter
     */
    private Patterns packages;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }

        String interactiveMode;

        if (interactive == null) {
            interactiveMode = "auto";
        } else if (interactive) {
            interactiveMode = "on";
        } else {
            interactiveMode = "off";
        }

        if (dest.exists()) {
            if (charset == null) {
                charset = CharsetUtil.detectedSystemCharset();
            }

            getLog().info("-------------------------------------------------");
            getLog().info("Detected system charset encoding: " + charset);
            getLog().info("If your can't read the following text, specify correct one like this: ");
            getLog().info("");
            getLog().info("  mvn -Dautoconfig.charset=yourcharset");
            getLog().info("");

            LogConfigurator.getConfigurator().configureDefault(false, charset);

            ConfigRuntimeImpl runtimeImpl = new ConfigRuntimeImpl(System.in, System.out, System.err, charset);

            runtimeImpl.setInteractiveMode(interactiveMode);
            runtimeImpl.setDests(new String[] { dest.getAbsolutePath() });
            runtimeImpl.setType(type);

            if (descriptors != null) {
                runtimeImpl.setDescriptorPatterns(descriptors.getIncludes(), descriptors.getExcludes());
            }

            if (packages != null) {
                runtimeImpl.setPackagePatterns(packages.getIncludes(), packages.getExcludes());
            }

            if (userProperties != null) {
                runtimeImpl.setUserPropertiesFile(userProperties.getAbsolutePath(), null);
            }

            getLog().info(
                    "Configuring " + dest.getAbsolutePath() + ", interactiveMode=" + interactiveMode + ", strict="
                    + strict);
            getLog().info("-------------------------------------------------");

            try {
                if (!runtimeImpl.start() && strict) {
                    throw new RuntimeException("undefined placeholders");
                }
            } catch (Exception e) {
                runtimeImpl.error(e);
                throw new MojoExecutionException("Autoconfig failed", e);
            }

            if (exploding && explodedDirectory != null) {
                unpack(dest, explodedDirectory);
            }
        } else {
            getLog().error("Dest directory or file for autoconfig does not exist: " + dest.getAbsolutePath());
        }
    }

    public void unpack(File srcfile, File destdir) throws MojoExecutionException {
        ExpanderRuntimeImpl expander = new ExpanderRuntimeImpl(System.in, System.out, System.err, charset);

        expander.getExpander().setSrcfile(srcfile.getAbsolutePath());
        expander.getExpander().setDestdir(destdir.getAbsolutePath());

        expander.start();
    }

    public static class Patterns {
        private String[] includes;
        private String[] excludes;

        public String[] getIncludes() {
            return includes;
        }

        public void setIncludes(String[] includes) {
            this.includes = includes;
        }

        public String[] getExcludes() {
            return excludes;
        }

        public void setExcludes(String[] excludes) {
            this.excludes = excludes;
        }
    }
}
