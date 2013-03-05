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

package com.alibaba.citrus.maven.eclipse.base.eclipse;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;

/** @requiresProject true */
public abstract class AbstractWorkspaceMojo
        extends AbstractMojo {

    /**
     * Directory location of the <code>Eclipse</code> workspace.
     *
     * @parameter expression="${eclipse.workspace}"
     * @required
     */
    private String workspace;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
}
