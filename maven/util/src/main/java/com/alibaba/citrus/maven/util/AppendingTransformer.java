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

package com.alibaba.citrus.maven.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.SelectorUtils;

/**
 * 合并同名的资源，合并时以换行分隔。支持用通配符匹配资源。
 * 
 * <pre>
 * &lt;plugin&gt;
 *     &lt;artifactId&gt;maven-shade-plugin&lt;/artifactId&gt;
 *     &lt;configuration&gt;
 *         &lt;transformers&gt;
 *             &lt;transformer
 *                 implementation="com.alibaba.citrus.maven.util.AppendingTransformer"&gt;
 *                 &lt;patterns&gt;
 *                     &lt;pattern&gt;META-INF/*.bean-definition-parsers&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/*.bean-definition-decorators&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/*.bean-definition-decorators-for-attribute&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/spring.configuration-points&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/spring.handlers&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/spring.schemas&lt;/pattern&gt;
 *                     &lt;pattern&gt;META-INF/webx.internal-request-handlers&lt;/pattern&gt;
 *                 &lt;/patterns&gt;
 *             &lt;/transformer&gt;
 *         &lt;/transformers&gt;
 *     &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 * 
 * @author Michael Zhou
 */
public class AppendingTransformer implements ResourceTransformer {
    private final Map/* String, ByteArrayOutputStream */data = new HashMap();

    // injected
    String[] patterns;

    public boolean canTransformResource(String r) {
        if (patterns != null) {
            for (int i = 0; i < patterns.length; i++) {
                String pattern = patterns[i];

                if (SelectorUtils.matchPath(pattern, r)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void processResource(String resource, InputStream is, List relocators) throws IOException {
        ByteArrayOutputStream resourceData = (ByteArrayOutputStream) data.get(resource);

        if (resourceData == null) {
            resourceData = new ByteArrayOutputStream();
            data.put(resource, resourceData);
        }

        IOUtil.copy(is, resourceData);
        resourceData.write('\n');

        is.close();
    }

    public boolean hasTransformedResource() {
        return data.size() > 0;
    }

    public void modifyOutputStream(JarOutputStream jos) throws IOException {
        for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String resource = (String) entry.getKey();
            ByteArrayOutputStream resourceData = (ByteArrayOutputStream) entry.getValue();

            jos.putNextEntry(new JarEntry(resource));

            IOUtil.copy(new ByteArrayInputStream(resourceData.toByteArray()), jos);
            resourceData.reset();
        }
    }
}
