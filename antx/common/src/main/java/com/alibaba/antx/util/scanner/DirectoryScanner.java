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

package com.alibaba.antx.util.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.antx.util.FileUtil;

/**
 * 文件扫描器。
 *
 * @author Michael Zhou
 */
public class DirectoryScanner extends AbstractScanner {
    private File basedir;
    private URL  baseURL;
    private boolean followSymlinks = true;

    /**
     * 创建一个文件目录扫描器。
     *
     * @param basedir 文件目录
     * @param handler 回调函数
     */
    public DirectoryScanner(File basedir, ScannerHandler handler) {
        super(handler);

        if (!basedir.exists() || !basedir.isDirectory()) {
            throw new IllegalArgumentException(basedir + " is not a directory");
        }

        try {
            this.baseURL = basedir.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(basedir + " is not a directory");
        }

        this.basedir = basedir;
    }

    /**
     * 取得扫描的根目录。
     *
     * @return 正在扫描的根目录
     */
    public File getBasedir() {
        return basedir;
    }

    /**
     * 取得扫描的根目录的URL。
     *
     * @return 正在扫描的根目录的URL
     */
    public URL getBaseURL() {
        return baseURL;
    }

    /**
     * 取得当前正在扫描的文件的URL。
     *
     * @return URL
     */
    public URL getURL() {
        try {
            return new File(getBasedir(), getPath()).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ScannerException(e);
        }
    }

    /**
     * 取得当前正在扫描的文件的输入流。
     *
     * @return 输入流
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(new File(getBasedir(), getPath()));
        } catch (FileNotFoundException e) {
            throw new ScannerException(e);
        }
    }

    /**
     * 是否扫描符号链接。
     *
     * @return 如果是，则返回<code>true</code>
     */
    public boolean isFollowSymlinks() {
        return followSymlinks;
    }

    /**
     * 设置是否扫描符号链接。
     *
     * @param followSymlinks 是否扫描符号链接
     */
    public void setFollowSymlinks(boolean followSymlinks) {
        this.followSymlinks = followSymlinks;
    }

    /** 执行扫描。 */
    public void scan() {
        Set processed = new HashSet();

        getScannerHandler().setScanner(this);

        getScannerHandler().startScanning();

        scandir(getBasedir(), processed);

        getScannerHandler().endScanning();
    }

    /**
     * 扫描指定目录。
     *
     * @param dir       被扫描的目录
     * @param processed 已被扫描的绝对路径，用来防止因为符合链接错误导致的重复扫描
     */
    protected void scandir(File dir, Set processed) {
        // 防止符号链接无限循环
        try {
            String canonicalPath = dir.getCanonicalPath();

            if (processed.contains(canonicalPath)) {
                return;
            }

            processed.add(canonicalPath);
        } catch (IOException e) {
            throw new ScannerException(e);
        }

        // 列出当前目录下的所有文件
        String[] files = dir.list();

        if (files == null) {
            throw new ScannerException("IO error scanning directory " + dir.getAbsolutePath());
        }

        // 排除符号链接（如果需要的话）
        if (!followSymlinks) {
            List noLinks = new ArrayList(files.length);

            for (int i = 0; i < files.length; i++) {
                try {
                    if (!FileUtil.isSymbolicLink(dir, files[i])) {
                        noLinks.add(files[i]);
                    }
                } catch (IOException e) {
                    System.err.println("IOException caught while checking for links, couldn't get cannonical path!");
                    noLinks.add(files[i]);
                }
            }

            files = (String[]) noLinks.toArray(new String[noLinks.size()]);
        }

        // 递归扫描文件和目录
        for (String file2 : files) {
            String name = getPath() + file2;
            File file = new File(dir, file2);
            String savedPath;

            if (file.isDirectory()) {
                savedPath = setPath(name + '/');

                getScannerHandler().directory();

                if (getScannerHandler().followUp()) {
                    scandir(file, processed);
                }
            } else {
                savedPath = setPath(name);

                getScannerHandler().file();
            }

            setPath(savedPath);
        }
    }
}
