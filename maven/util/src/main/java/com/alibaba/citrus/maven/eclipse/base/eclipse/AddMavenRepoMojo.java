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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import com.alibaba.citrus.maven.eclipse.base.eclipse.writers.workspace.EclipseWorkspaceWriter;

/**
 * Adds the classpath variable M2_REPO to Eclipse. DEPRECATED. Replaced by eclipse:configure-workspace.
 * 
 * @goal add-maven-repo
 * @requiresProject false
 * @deprecated Use configure-workspace goal instead.
 */
public class AddMavenRepoMojo
    extends AbstractWorkspaceMojo
{
    public void execute()
        throws MojoExecutionException
    {
        WorkspaceConfiguration config = new WorkspaceConfiguration();
        config.setWorkspaceDirectory( new File( this.getWorkspace() ) );
        config.setLocalRepository( this.getLocalRepository() );

        new EclipseWorkspaceWriter().init( this.getLog(), config ).write();
    }

}
