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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.ide.IdeDependency;

/**
 * Modification on the original eclipse:eclipse plugin, to ignore osgi dependencies in pde project.
 *
 * @author Michael Zhou
 * @extendsPlugin maven-eclipse-plugin
 * @extendsGoal eclipse
 * @goal eclipse
 */
public class EclipsePlugin extends org.apache.maven.plugin.eclipse.EclipsePlugin {
    /**
     * if set ignoreOsgiBundle=true in PDE project, it will include all dependencies of project.
     * If not, the OSGI dependencies will be excluded.
     *
     * @parameter expression="${eclipse.ignoreOsgiBundle}" default-value="false"
     */
    private boolean ignoreOsgiBundle;

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

        return deps;
    }
}
