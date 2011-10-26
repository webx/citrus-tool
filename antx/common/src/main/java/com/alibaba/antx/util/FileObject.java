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

package com.alibaba.antx.util;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 代表一个<code>File</code>的辅助类, 方便取得绝对和相对路径.
 *
 * @author Michael Zhou
 *
 */
public class FileObject {
    private static final String  CURRENT_DIR    = ".";
    private static final char    COLON_CHAR     = ':';
    private static final String  UNC_PREFIX     = "\\\\";
    private static final String  FILE_SEP       = File.separator;
    private static final String  SLASH          = "/";
    private static final String  BACKSLASH      = "\\";
    private static final char    SLASH_CHAR     = '/';
    private static final char    BACKSLASH_CHAR = '\\';
    private static final String  UP_LEVEL_DIR   = ".." + SLASH;
    private static final boolean IS_WINDOWS     = System.getProperty("os.name").toLowerCase()
                                                        .indexOf("windows") >= 0;
    private String               abspath;
    private String               relpath;

    /**
     * 创建一个空的file object。
     */
    public FileObject() {
    }

    /**
     * 创建一个<code>FileObject</code>.
     *
     * @param file 文件
     */
    public FileObject(File file) {
        this((file == null) ? null
                            : file.getAbsolutePath());
    }

    /**
     * 创建一个<code>FileObject</code>.
     *
     * @param path 文件名
     */
    public FileObject(String path) {
        setPath(path);
    }

    /**
     * 创建一个<code>FileObject</code>.
     *
     * @param abspath 绝对路径
     * @param relpath 相对路径
     */
    private FileObject(String abspath, String relpath) {
        this(abspath);
        this.relpath = normalizePath(relpath);
    }

    /**
     * 设置path。
     *
     * @param path path
     */
    public void setPath(String path) {
        path = normalizePath(path);

        boolean endsWithSlash = endsWithSlash(path);

        try {
            abspath = new File(path).getCanonicalPath();
        } catch (IOException e) {
            abspath = new File(path).getAbsolutePath();
        }

        if (endsWithSlash && !endsWithSlash(abspath)) {
            abspath += FILE_SEP;
        }
    }

    /**
     * 取得绝对路径.
     *
     * @return 当前<code>FileObject</code>的绝对路径
     */
    public String getAbsolutePath() {
        return toString(false, SLASH);
    }

    /**
     * 取得绝对路径.
     *
     * @param sep 分隔符
     *
     * @return 当前<code>FileObject</code>的绝对路径
     */
    public String getAbsolutePath(String sep) {
        return toString(false, sep);
    }

    /**
     * 取得相对路径.
     *
     * @return 当前<code>FileObject</code>的相对路径
     */
    public String getRelativePath() {
        return toString(true, SLASH);
    }

    /**
     * 取得相对路径.
     *
     * @param sep 分隔符
     *
     * @return 当前<code>FileObject</code>的相对路径
     */
    public String getRelativePath(String sep) {
        return toString(true, sep);
    }

    /**
     * 取得<code>File</code>对象.
     *
     * @return <code>File</code>对象
     */
    public File getFile() {
        return new File(abspath);
    }

    /**
     * 取得相对于当前<code>FileObject</code>的路径.
     *
     * @param basedir 根目录
     * @param path 文件
     *
     * @return 相对于当前<code>FileObject</code>的路径
     */
    public FileObject newFileObject(FileObject basedir, String path) {
        return newFileObject(basedir.newFileObject(path).getFile());
    }

    /**
     * 取得相对于当前<code>FileObject</code>的路径.
     *
     * @param file 文件
     *
     * @return 相对于当前<code>FileObject</code>的路径
     */
    public FileObject newFileObject(File file) {
        return newFileObject((file == null) ? null
                                            : file.getAbsolutePath());
    }

    /**
     * 取得相对于当前<code>FileObject</code>的路径.
     *
     * @param path 路径
     *
     * @return 相对于当前<code>FileObject</code>的路径
     */
    public FileObject newFileObject(String path) {
        path = normalizePath(path);

        boolean endsWithSlash = endsWithSlash(path);
        File    pathFile = new File(path);

        if (!pathFile.isAbsolute()) {
            pathFile = new File(abspath, path);
        }

        try {
            path = pathFile.getCanonicalPath();
        } catch (IOException e) {
            path = pathFile.getAbsolutePath();
        }

        if (endsWithSlash && !endsWithSlash(path)) {
            path += FILE_SEP;
        }

        String thisPrefix = getSystemDependentPrefix(abspath);
        String prefix = getSystemDependentPrefix(path);

        if (!prefix.equals(thisPrefix)) {
            return new FileObject(path); // 如果不能转成相对路径, 则返回绝对路径
        }

        String[]     thisParts = getPathParts(abspath, thisPrefix, isFile(abspath));
        String[]     parts = getPathParts(path, prefix, false);

        StringBuffer buffer = new StringBuffer();
        int          i      = 0;

        if (IS_WINDOWS) {
            while ((i < thisParts.length) && (i < parts.length)
                        && thisParts[i].equalsIgnoreCase(parts[i])) {
                i++;
            }
        } else {
            while ((i < thisParts.length) && (i < parts.length) && thisParts[i].equals(parts[i])) {
                i++;
            }
        }

        if ((i < thisParts.length) && (i < parts.length)) {
            for (int j = i; j < thisParts.length; j++) {
                buffer.append(UP_LEVEL_DIR);
            }
        }

        for (; i < parts.length; i++) {
            buffer.append(parts[i]);

            if (i < (parts.length - 1)) {
                buffer.append(SLASH_CHAR);
            }
        }

        String relpath = buffer.toString();

        if (endsWithSlash && !endsWithSlash(relpath)) {
            relpath += SLASH;
        }

        return new FileObject(path, relpath);
    }

    /**
     * 将指定的数组中的路径, 转换成相对于当前<code>FileObject</code>的树.
     *
     * @param basedir 根目录
     * @param paths 路径数组
     *
     * @return 树
     */
    public Map tree(FileObject basedir, String[] paths) {
        return tree(basedir, Arrays.asList(paths));
    }

    /**
     * 将指定的集合中的路径, 转换成相对于当前<code>FileObject</code>的树.
     *
     * @param basedir 根目录
     * @param paths 路径集合
     *
     * @return 树
     */
    public Map tree(FileObject basedir, Collection paths) {
        Map tree = new HashMap();

        for (Iterator i = paths.iterator(); i.hasNext();) {
            String          abspath       = i.next().toString();
            String          pathToBasedir = basedir.newFileObject(abspath).getRelativePath();
            String          path          = newFileObject(abspath).getRelativePath();
            StringTokenizer tokenizer     = new StringTokenizer(pathToBasedir, SLASH);
            Map             node          = tree;

            while (tokenizer.hasMoreTokens()) {
                String s = tokenizer.nextToken();

                if (tokenizer.hasMoreTokens()) {
                    Map tmp = (Map) node.get(s);

                    if (tmp == null) {
                        tmp = new HashMap();
                        node.put(s, tmp);
                    }

                    node = tmp;
                } else {
                    node.put(s, path);
                }
            }
        }

        return tree;
    }

    /**
     * 取得绝对路径的字符串.
     *
     * @return 绝对路径的字符串
     */
    public String toString() {
        return toString(false, SLASH);
    }

    /**
     * 取得相对或绝对路径的字符串.
     *
     * @param relative 是否为相对路径
     * @param sep 使用指定的分隔符(对UNC路径无效)
     *
     * @return 相对或绝对路径的字符串
     */
    private String toString(boolean relative, String sep) {
        String path;

        if (relative) {
            path = (relpath == null) ? abspath
                                     : relpath;
        } else {
            path = abspath;
        }

        if (isUncPath(path)) {
            return path;
        }

        if (BACKSLASH.equals(sep)) {
            return path.replace(SLASH_CHAR, BACKSLASH_CHAR);
        } else {
            return path.replace(BACKSLASH_CHAR, SLASH_CHAR);
        }
    }

    /**
     * 判断一个路径是否为文件.
     *
     * @param path 要检查的路径
     *
     * @return 如果是文件, 则返回<code>true</code>
     */
    private boolean isFile(String path) {
        if (path == null) {
            return false;
        }

        File file = new File(path);

        return file.isFile() && file.exists();
    }

    /**
     * 规格化路径, 确保路径非空.
     *
     * @param path 要规格化的路径
     *
     * @return 规格化的路径
     */
    private String normalizePath(String path) {
        if (path == null) {
            return CURRENT_DIR;
        } else {
            path = path.trim();

            if (path.length() == 0) {
                return CURRENT_DIR;
            }

            return path;
        }
    }

    /**
     * 检查指定路径是否为UNC路径.
     *
     * @param path 要检查的路径.
     *
     * @return 如果是UNC路径, 则返回<code>true</code>
     */
    private boolean isUncPath(String path) {
        return path.startsWith(UNC_PREFIX);
    }

    /**
     * 检查指定路径是否以"/"或"\\"结尾.
     *
     * @param path 要检查的路径.
     *
     * @return 如果以"/"或"\\"结尾, 则返回<code>true</code>
     */
    private boolean endsWithSlash(String path) {
        return path.endsWith(SLASH) || path.endsWith(BACKSLASH);
    }

    /**
     * 取得和系统相关的文件名前缀.  对于Windows系统, 可能是驱动器名或UNC路径前缀"\\". 如果不存在前缀, 则返回空字符串.
     *
     * @param path 绝对路径
     *
     * @return 和系统相关的文件名前缀
     */
    private String getSystemDependentPrefix(String path) {
        if (IS_WINDOWS) {
            if (isUncPath(path)) {
                int index = path.indexOf(FILE_SEP, UNC_PREFIX.length());

                if (index != -1) {
                    return path.substring(0, index);
                } else {
                    return path;
                }
            } else if ((path.length() > 1) && (path.charAt(1) == COLON_CHAR)) {
                return path.substring(0, 2).toLowerCase();
            }
        }

        return "";
    }

    /**
     * 将path拆成若干部分, 并放入数组中.
     *
     * @param path 绝对路径
     * @param prefix 路径前缀
     * @param treatAsFile 看作文件
     *
     * @return 指定绝对路径的片段数组
     */
    private String[] getPathParts(String path, String prefix, boolean treatAsFile) {
        StringTokenizer tokenizer = new StringTokenizer(path.substring(prefix.length()), FILE_SEP);
        List            parts = new ArrayList();

        while (tokenizer.hasMoreTokens()) {
            parts.add(tokenizer.nextToken());
        }

        if (treatAsFile) {
            parts.remove(parts.size() - 1);
        }

        return (String[]) parts.toArray(new String[parts.size()]);
    }
}
