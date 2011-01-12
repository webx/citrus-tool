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
 *
 */
package com.alibaba.antx.config.resource;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.oro.text.perl.Perl5Util;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.util.FileUtil;
import com.alibaba.antx.util.StringUtil;

public class ResourceURI {
    private final static Perl5Util util = new Perl5Util();
    private final URI              uri;
    private final Map              options;

    public static void main(String[] args) throws Exception {
        System.out.println(guessURI("file://c:/aaa/bbb"));
    }

    public static URI guessURI(String file) {
        URI uri = null;

        // 排除windows文件名，例如c:/test.txt
        if (!util.match("/^\\w:/", file)) {
            try {
                uri = new URI(file);

                if (!uri.isAbsolute()) {
                    uri = null;
                }
            } catch (URISyntaxException e) {
            }
        }

        if (uri == null) {
            uri = new File(FileUtil.getPathBasedOn(new File("").getAbsolutePath(), file)).toURI();
        }

        return fixFileURI(uri);
    }

    public ResourceURI(URI uri) {
        this(uri, (Session) null);
    }

    public ResourceURI(URI uri, Session session) {
        // 为file修正URI
        uri = fixFileURI(uri);

        String query = uri.getQuery();
        Map options = new HashMap();
        StringBuffer newQuery = new StringBuffer();

        if (!StringUtil.isEmpty(query)) {
            for (StringTokenizer tokenizer = new StringTokenizer(query, "&"); tokenizer.hasMoreElements();) {
                String token = StringUtil.trimWhitespace(tokenizer.nextToken());

                if (StringUtil.isEmpty(token)) {
                    continue;
                }

                int index = token.indexOf("=");
                String key;
                String value;

                if (index <= 0) {
                    key = token;
                    value = "true";
                } else {
                    key = token.substring(0, index);
                    value = token.substring(index + 1);
                }

                try {
                    key = StringUtil.trimWhitespace(URLDecoder.decode(key, "UTF-8"));
                    value = StringUtil.trimWhitespace(URLDecoder.decode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new ConfigException(e);
                }

                if (!StringUtil.isEmpty(key) && session != null && session.acceptOption(key)) {
                    options.put(key, value);
                } else {
                    if (newQuery.length() > 0) {
                        newQuery.append("&");
                    }

                    newQuery.append(token);
                }
            }
        }

        String newQueryStr = newQuery.length() > 0 ? newQuery.toString() : null;

        try {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                    newQueryStr, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new ConfigException(e);
        }

        this.options = options;
    }

    private static URI fixFileURI(URI uri) {
        if ("file".equals(uri.getScheme())) {
            try {
                uri = URI.create(util.substitute("s/file:\\/*/file:\\//", uri.toString()));
            } catch (Exception e) {
                throw new ConfigException(e);
            }
        }

        return uri;
    }

    public ResourceURI(URI uri, Map options) {
        this.uri = uri;
        this.options = options;
    }

    public final URI getURI() {
        return uri;
    }

    public final String getPath() {
        return uri.getPath();
    }

    public final File getFile() {
        return new File(uri);
    }

    public final String getName() {
        String path = uri.getPath();

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public ResourceURI getSubURI(String subname) {
        return getSubURI(subname, false);
    }

    public ResourceURI getSubURI(String subname, boolean directory) {
        String path = uri.getPath();

        if (!path.endsWith("/") && !StringUtil.isEmpty(subname)) {
            path += "/";
        }

        String subpath = path + subname;

        if (directory) {
            subpath += "/";
        }

        URI suburi;

        try {
            suburi = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), subpath, uri.getQuery(),
                    uri.getFragment()).normalize();
        } catch (URISyntaxException e) {
            throw new ConfigException(e);
        }

        return new ResourceURI(suburi, options);
    }

    public ResourceURI getSuperURI() {
        String path = uri.getPath();

        if (StringUtil.isEmpty(path) || path.equals("/")) {
            return null;
        }

        String superpath = path + "/../";
        URI superuri;

        try {
            superuri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), superpath, uri
                    .getQuery(), uri.getFragment()).normalize();
        } catch (URISyntaxException e) {
            throw new ConfigException(e);
        }

        return new ResourceURI(superuri, options);
    }

    public boolean guessDirectory() {
        return uri.getPath().endsWith("/");
    }

    public final String getOption(String name) {
        return (String) options.get(name);
    }

    public String toString() {
        return uri.toString();
    }
}
