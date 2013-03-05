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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.citrus.maven.eclipse.base.ide.IdeUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Removes the not-available marker files from the repository.
 *
 * @author <a href="mailto:baerrach@apache.org">Barrie Treloar</a>
 * @version $Id: RemoveCacheMojo.java 728546 2008-12-21 22:56:51Z bentmann $
 * @goal remove-cache
 */
public class RemoveCacheMojo
        extends AbstractMojo {
    /**
     * Local maven repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    public void execute()
            throws MojoExecutionException, MojoFailureException {
        getLog().info(Messages.getString("RemoveCacheMojo.checking"));
        List notAvailableMarkerFiles = getNotAvailableMarkerFiles();
        if (!notAvailableMarkerFiles.isEmpty()) {
            deleteMarkerFiles(notAvailableMarkerFiles);
        }
        getLog().info(Messages.getString("RemoveCacheMojo.complete"));
    }

    /**
     * Delete each file in the notAvailableMarkerFiles list.
     *
     * @param notAvailableMarkerFiles the list of marker files to delete.
     */
    private void deleteMarkerFiles(List/* <File> */notAvailableMarkerFiles) {
        for (Iterator iter = notAvailableMarkerFiles.iterator(); iter.hasNext(); ) {
            File markerFile = (File) iter.next();
            try {
                IdeUtils.delete(markerFile, getLog());
            } catch (MojoExecutionException e) {
                getLog().warn(e.getMessage(), e);
            }
        }
    }

    /**
     * A list of all the not available marker <code>File</code>s in the localRepository. If there are no marker files
     * then an empty list is returned.
     *
     * @return all the not available marker files in the localRepository or an empty list.
     */
    private List/* <File> */getNotAvailableMarkerFiles() {
        File localRepositoryBaseDirectory = new File(localRepository.getBasedir());
        List markerFiles = new ArrayList();

        Iterator iterator =
                FileUtils.iterateFiles(localRepositoryBaseDirectory,
                                       new SuffixFileFilter(IdeUtils.NOT_AVAILABLE_MARKER_FILE_SUFFIX),
                                       TrueFileFilter.INSTANCE);
        while (iterator.hasNext()) {
            File notAvailableMarkerFile = (File) iterator.next();
            markerFiles.add(notAvailableMarkerFile);
        }
        return markerFiles;
    }
}
