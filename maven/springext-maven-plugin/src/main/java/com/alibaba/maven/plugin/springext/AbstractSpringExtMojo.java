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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * @aggregator
 * @requiresDependencyResolution test
 */
public abstract class AbstractSpringExtMojo extends AbstractMojo {
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private Object projects;

    /**
     * If true, the &lt;testOutputDirectory&gt; and the dependencies of
     * &lt;scope&gt;test&lt;scope&gt; will be put first on the runtime
     * classpath.
     *
     * @parameter expression="${noTestClasspath}" default-value="false"
     */
    private boolean noTestClasspath;

    protected List<File> createClassPath() {
        return new DependencyLister().getDependencyFiles();
    }

    protected final MavenProject getCurrentProject() {
        return project;
    }

    protected final MavenProject[] getProjects() {
        if (projects instanceof MavenProject[]) {
            return (MavenProject[]) projects; // maven 3
        } else if (projects instanceof Collection<?>) {
            Collection<MavenProject> mavenProjects = (Collection<MavenProject>) projects; // maven 2
            return mavenProjects.toArray(new MavenProject[mavenProjects.size()]);
        } else {
            return new MavenProject[0];
        }
    }

    private class DependencyLister {
        private final Set<String>    dependencyFileNames = new HashSet<String>();
        private final List<File>     dependencyFiles     = new ArrayList<File>();
        private final String         currentDir          = new File("").getAbsolutePath() + File.separator;
        private final List<String[]> displayClasspath    = new ArrayList<String[]>();
        private       int            width1              = -1;
        private       int            width2              = -1;
        private       int            width3              = -1;

        private List<File> getDependencyFiles() {
            getLog().info("Setting up classpath ..."
                          + (noTestClasspath ? ""
                                             : "\n  (includes test files, " +
                                               "use \"-DnoTestClasspath\" to get rid of it)\n"));

            for (MavenProject project : getProjects()) {
                String classesDirectory = project.getBuild().getOutputDirectory();
                String testClassesDirectory = project.getBuild().getTestOutputDirectory();

                if (!noTestClasspath && testClassesDirectory != null) {
                    addDependency(new File(testClassesDirectory), project, Artifact.SCOPE_TEST);
                }

                if (classesDirectory != null) {
                    addDependency(new File(classesDirectory), project, Artifact.SCOPE_COMPILE);
                }
            }

            displayClasspath.add(null);

            for (MavenProject project : getProjects()) {
                for (Iterator i = project.getArtifacts().iterator(); i.hasNext(); ) {
                    Artifact artifact = (Artifact) i.next();

                    if (artifact != null && "jar".equals(artifact.getType())) {
                        String scope = artifact.getScope();

                        if (!artifact.isOptional() && !Artifact.SCOPE_PROVIDED.equals(scope) && (!noTestClasspath || !Artifact.SCOPE_TEST.equals(scope))) {
                            addDependency(artifact.getFile(), project, scope);
                        }
                    }
                }
            }

            width1 += 2;
            width2 += 2;
            width3 += 2;

            String format = "%-" + width1 + "s %-" + width2 + "s %-" + width3 + "s";
            String sepLine = repeat('-', width1) + " " + repeat('-', width2) + " " + repeat('-', width3);

            getLog().info(sepLine);
            getLog().info(String.format(format, "Artifact", "Project", "Scope"));
            getLog().info(sepLine);

            for (String[] item : displayClasspath) {
                if (item == null) {
                    getLog().info(sepLine);
                } else {
                    getLog().info(String.format(format, item));
                }
            }

            getLog().info(sepLine);

            return dependencyFiles;
        }

        private String repeat(char c, int count) {
            StringBuilder buf = new StringBuilder(count);

            for (int i = 0; i < count; i++) {
                buf.append(c);
            }

            return buf.toString();
        }

        private void addDependency(File file, MavenProject project, String scope) {
            // file可能为null, unresolved
            if (file != null) {
                String fileName = file.getAbsolutePath();

                if (!dependencyFileNames.contains(fileName)) {
                    dependencyFiles.add(file);
                    dependencyFileNames.add(fileName);

                    String displayName = fileName;

                    if (displayName.startsWith(currentDir)) {
                        displayName = "." + File.separator + displayName.substring(currentDir.length());
                    } else {
                        displayName = file.getName();
                    }

                    displayClasspath.add(new String[] { displayName, project.getArtifactId(), scope });

                    if (displayName.length() > width1) {
                        width1 = displayName.length();
                    }

                    if (project.getArtifactId().length() > width2) {
                        width2 = project.getArtifactId().length();
                    }

                    if (scope.length() > width3) {
                        width3 = scope.length();
                    }
                }
            }
        }
    }
}
