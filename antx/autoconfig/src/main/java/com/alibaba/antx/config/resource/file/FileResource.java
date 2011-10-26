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

package com.alibaba.antx.config.resource.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.resource.Resource;
import com.alibaba.antx.config.resource.ResourceNotFoundException;
import com.alibaba.antx.config.resource.ResourceURI;
import com.alibaba.antx.config.resource.Session;
import com.alibaba.antx.util.StreamUtil;

public class FileResource extends Resource {
    private File file;

    public FileResource(Session session, ResourceURI uri) {
        super(session, uri);
        this.file = uri.getFile();
    }

    public Resource getRelatedResource(String suburi) {
        return new FileResource(getSession(), getURI().getSubURI(suburi));
    }

    public byte[] getContent() {
        try {
            return StreamUtil.readBytes(getInputStream(), true).toByteArray();
        } catch (IOException e) {
            throw new ConfigException(e);
        }
    }

    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public List list() {
        if (!isDirectory()) {
            return null;
        }

        File[] subfiles = file.listFiles();
        List files = new ArrayList(subfiles.length);

        for (int i = 0; i < subfiles.length; i++) {
            File subfile = subfiles[i];
            FileResource resource = new FileResource(getSession(), new ResourceURI(subfile.toURI(), getSession()));

            files.add(resource);
        }

        Collections.sort(files);

        return files;
    }
}
