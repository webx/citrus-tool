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

package com.alibaba.antx.config;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * 代表一个ConfigEntry所代表的资源，可以是URL、File等。
 * 
 * @author Michael Zhou
 */
public final class ConfigResource {
    private final ConfigResource baseResource;
    private final File           file;
    private final URL            url;
    private final String         name;

    public ConfigResource(File file) {
        this(file, null, null, true);
    }

    public ConfigResource(File file, String name) {
        this(file, null, name, true);
    }

    public ConfigResource(URL url) {
        this(null, url, null, true);
    }

    public ConfigResource(URL url, String name) {
        this(null, url, name, true);
    }

    private ConfigResource(File file, URL url, String name, boolean setbase) {
        ConfigResource base = null;

        if ((url == null) && (file == null)) {
            throw new IllegalArgumentException("missing file or url");
        }

        // 处理name
        if (name == null) {
            if (file != null) {
                name = file.getName();

                if (setbase) {
                    base = new ConfigResource(file.getParentFile(), null, null, false);
                }
            } else if (url != null) {
                String[] pair = getURLName(url);

                name = pair[1];

                if (setbase) {
                    try {
                        base = new ConfigResource(null, new URL(pair[0]), null, false);
                    } catch (MalformedURLException e) {
                        IllegalArgumentException iae = new IllegalArgumentException("invalid URL object: " + pair[0]);

                        iae.initCause(e);

                        throw iae;
                    }
                }
            }
        }

        name = name.replace('\\', '/');

        if (name.startsWith("/")) {
            name = name.substring(1);
        }

        // 处理url
        if (file != null) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                IllegalArgumentException iae = new IllegalArgumentException("invalid file object");

                iae.initCause(e);
                throw iae;
            }
        } else if (url.getProtocol().equals("file")) {
            String path = url.getPath();

            if (path != null) {
                try {
                    file = new File(URLDecoder.decode(path, "utf8"));
                } catch (UnsupportedEncodingException e) {
                    IllegalArgumentException iae = new IllegalArgumentException("invalid URL object");

                    iae.initCause(e);
                    throw iae;
                }
            }
        }

        if ((base == null) && setbase) {
            String baseurl = url.toExternalForm();

            if (baseurl.endsWith(name) && (baseurl.length() > name.length())) {
                baseurl = baseurl.substring(0, baseurl.length() - name.length());
            }

            try {
                base = new ConfigResource(null, new URL(baseurl), null, false);
            } catch (MalformedURLException e) {
                IllegalArgumentException iae = new IllegalArgumentException("invalid URL object: " + baseurl);

                iae.initCause(e);
                throw iae;
            }
        }

        // 处理file
        if (file != null) {
            file = file.getAbsoluteFile();
        }

        this.name = name;
        this.file = file;
        this.url = url;
        this.baseResource = base;
    }

    /**
     * 取得URL的文件名。
     * 
     * @param url URL
     * @return URL所表示的的文件名
     */
    private static String[] getURLName(URL url) {
        String urlStr = url.toExternalForm();

        if (urlStr.endsWith("/")) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
        }

        int index = urlStr.lastIndexOf("/");

        return new String[] { urlStr.substring(0, index + 1), urlStr.substring(index + 1) };
    }

    public ConfigResource getBase() {
        return baseResource;
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getURL().toExternalForm();
    }
}
