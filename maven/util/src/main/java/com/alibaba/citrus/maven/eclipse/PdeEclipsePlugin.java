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

package com.alibaba.citrus.maven.eclipse;

import java.io.File;
import java.io.IOException;

import com.alibaba.citrus.maven.eclipse.base.eclipse.EclipsePlugin;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.EclipseClasspathWriter;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.EclipseOSGiManifestWriter;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.EclipseProjectWriter;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.EclipseWriter;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.EclipseWriterConfig;
import com.alibaba.citrus.maven.eclipse.base.ide.IdeDependency;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * Modification on the original eclipse:eclipse plugin, to ignore osgi dependencies in pde project.
 *
 * @author Michael Zhou
 * @goal pde-eclipse
 */
public class PdeEclipsePlugin extends EclipsePlugin {
    /**
     * if set ignoreOsgiBundle=true in a PDE project, it will include all dependencies of project.
     * If not, the OSGI dependencies will be excluded.
     * <p/>
     *
     * @parameter expression="${eclipse.ignoreOsgiBundle}" default-value="false"
     */
    private boolean ignoreOsgiBundle;

    /**
     * if a libdir is specified, e.g. "lib", in a PDE project, it will copy dependencies into this folder
     * instead of creating resource links for them.
     * <p/>
     *
     * @parameter expression="${eclipse.libdir}"
     */
    private String libdir;

    @Override
    protected IdeDependency[] doDependencyResolution() throws MojoExecutionException {
        IdeDependency[] deps = super.doDependencyResolution();

        if (ignoreOsgiBundle) {
            for (int i = 0; i < deps.length; i++) {
                IdeDependency dep = deps[i];

                if (dep.isOsgiBundle()) {
                    deps[i] = new IdeDependency(dep.getGroupId(),
                                                dep.getArtifactId(),
                                                dep.getVersion(),
                                                dep.getClassifier(),
                                                dep.isReferencedProject(),
                                                dep.isTestDependency(),
                                                dep.isSystemScoped(),
                                                dep.isProvided(),
                                                dep.isAddedToClasspath(),
                                                dep.getFile(),
                                                dep.getType(),
                                                false, // force to be false
                                                null,
                                                -1,
                                                dep.getEclipseProjectName());
                }
            }
        }

        if (isPdeProject() && libdir != null) {
            for (int j = 0; j < deps.length; j++) {
                IdeDependency dep = deps[j];

                if (!dep.isProvided() && !dep.isReferencedProject() && !dep.isTestDependency() && !dep.isOsgiBundle() && dep.getFile() != null) {
                    File lib = new File(getProject().getBasedir(), libdir);
                    File srcfile = dep.getFile();

                    getLog().info("Copying " + srcfile.getName() + " to " + lib.getAbsolutePath());

                    try {
                        FileUtils.copyFileToDirectory(srcfile, lib);
                    } catch (IOException e) {
                        getLog().error("Failed to copy " + srcfile.getName() + " to " + lib.getAbsolutePath(), e);
                    }
                }
            }
        }

        return deps;
    }

    @Override
    protected EclipseWriter getEclipseClasspathWriter(EclipseWriterConfig config) {
        return new EclipseClasspathWriter() {
            @Override
            protected String getDependencyPathForPde(String name) {
                return addLibdir(name);
            }
        }.init(getLog(), config);
    }

    @Override
    protected EclipseWriter getEclipseProjectWriter(EclipseWriterConfig config) {
        return new EclipseProjectWriter() {
            @Override
            protected void writeResourceLinksForPdeProject(XMLWriter writer, IdeDependency[] dependencies)
                    throws MojoExecutionException {
                if (libdir == null) {
                    super.writeResourceLinksForPdeProject(writer,
                                                          dependencies);
                }
            }
        }.init(getLog(), config);
    }

    @Override
    @Deprecated
    protected EclipseWriter getEclipseOSGiManifestWriter(EclipseWriterConfig config) {
        return new EclipseOSGiManifestWriter() {
            @Override
            protected String getDependencyPathForPde(String name) {
                return addLibdir(name);
            }
        }.init(getLog(), config);
    }

    private String addLibdir(String name) {
        if (libdir == null) {
            return name;
        } else {
            return (libdir + "/" + name).replace("^/+", "").replace("/+", "/");
        }
    }
}
