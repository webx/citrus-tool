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

package com.alibaba.maven.plugin.springext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

public abstract class AbstractSchemaExporterMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${executedProject}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * If true, the &lt;testOutputDirectory&gt; and the dependencies of
     * &lt;scope&gt;test&lt;scope&gt; will be put first on the runtime
     * classpath.
     *
     * @parameter expression="${useTestClasspath}" default-value="false"
     */
    private boolean useTestClasspath;

    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * The directory containing generated test classes.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    private File testClassesDirectory;

    protected List createClassPath() {
        getLog().info("Setting up classpath ...");

        List classPathFiles = new ArrayList();

        if (useTestClasspath && testClassesDirectory != null) {
            classPathFiles.add(testClassesDirectory);
        }

        if (classesDirectory != null) {
            classPathFiles.add(classesDirectory);
        }

        classPathFiles.addAll(getDependencyFiles());

        for (Iterator i = classPathFiles.iterator(); i.hasNext(); ) {
            getLog().info("  added " + ((File) i.next()).getName());
        }

        return classPathFiles;
    }

    private List getDependencyFiles() {
        List dependencyFiles = new ArrayList();

        for (Iterator i = project.getArtifacts().iterator(); i.hasNext(); ) {
            Artifact artifact = (Artifact) i.next();

            if ("jar".equals(artifact.getType())) {
                String scope = artifact.getScope();

                if (!Artifact.SCOPE_PROVIDED.equals(scope) && (useTestClasspath || !Artifact.SCOPE_TEST.equals(scope))) {
                    dependencyFiles.add(artifact.getFile());
                }
            }
        }

        return dependencyFiles;
    }
}
