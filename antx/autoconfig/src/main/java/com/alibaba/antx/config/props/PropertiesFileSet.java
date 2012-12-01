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

package com.alibaba.antx.config.props;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.antx.config.resource.Resource;
import com.alibaba.antx.config.resource.ResourceManager;
import com.alibaba.antx.config.resource.ResourceURI;

/**
 * 代表一组props文件。
 *
 * @author Michael Zhou
 */
public class PropertiesFileSet extends PropertiesResource {
    private List files = new ArrayList();

    public PropertiesFileSet(ResourceManager manager, String file) {
        super(manager);
        setURI(ResourceURI.guessURI(file));
    }

    public PropertiesFileSet(ResourceManager manager, File file) {
        super(manager);
        setURI(file);
    }

    public PropertiesFileSet(ResourceManager manager, URI url) {
        super(manager);
        setURI(url);
    }

    public List getPropertiesFiles() {
        load();
        return files;
    }

    @Override
    protected void onLoad() {
        List subresources = getResource().list();

        for (Iterator i = subresources.iterator(); i.hasNext(); ) {
            Resource subres = (Resource) i.next();

            if (!subres.isDirectory()) {
                PropertiesFile file = new PropertiesFile(manager, subres);

                files.add(file);
            }
        }
    }
}
