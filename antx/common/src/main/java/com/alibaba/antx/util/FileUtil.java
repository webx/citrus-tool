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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.alibaba.antx.util.scanner.DefaultScannerHandler;
import com.alibaba.antx.util.scanner.DirectoryScanner;
import com.alibaba.antx.util.scanner.Scanner;
import com.alibaba.antx.util.scanner.ScannerException;

/**
 * 和操作文件有关的工具类。
 *
 * @author Michael Zhou
 */
public class FileUtil {
    /** 系统属性：用户home目录 */
    public static final String SYS_PROP_USER_HOME = "user.home";

    /** 系统属性：用户当前目录 */
    public static final String SYS_PROP_USER_CURRENT_DIR = "user.dir";

    /** 默认排除的文件。 */
    public static final String[] DEFAULT_EXCLUDES = {
            // Miscellaneous typical temporary files
            "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

            // CVS
            "**/CVS", "**/CVS/**", "**/.cvsignore",

            // SCCS
            "**/SCCS", "**/SCCS/**",

            // Visual SourceSafe
            "**/vssver.scc",

            // Subversion
            "**/.svn", "**/.svn/**",

            // Mac
            "**/.DS_Store" };

    /**
     * 取得用户home目录。
     *
     * @return 用户home目录
     */
    public static File getUserHome() {
        return new File(System.getProperty(SYS_PROP_USER_HOME));
    }

    /**
     * 取得用户当前目录。
     *
     * @return 用户当前目录
     */
    public static File getUserCurrentDir() {
        return new File(System.getProperty(SYS_PROP_USER_CURRENT_DIR));
    }

    /**
     * 从指定路径创建<code>File</code>。如果<code>path</code>为相对路径，则相对于指定
     * <code>basedir</code>。
     *
     * @param basedir 相对路径的根目录
     * @param path    绝对路径或相对路径
     * @return 文件对象
     */
    public static File getFile(String basedir, String path) {
        return getFile(new File(basedir), path);
    }

    /**
     * 从指定路径创建<code>File</code>。如果<code>path</code>为相对路径，则相对于指定
     * <code>basedir</code>。
     *
     * @param basedir 相对路径的根目录
     * @param path    绝对路径或相对路径
     * @return 文件对象
     */
    public static File getFile(File basedir, String path) {
        File file = new File(path);

        if (file.isAbsolute()) {
            return file;
        }

        return new File(basedir, path);
    }

    /**
     * 从指定路径开始查找文件，一直找到根目录为止。
     *
     * @param dir      从这个目录开始找
     * @param filename 要查找的文件名
     * @return 找到的文件，如果未找到，则返回<code>null</code>
     */
    public static File find(String filename) {
        return find(getUserCurrentDir(), filename);
    }

    /**
     * 从指定路径开始查找文件，一直找到根目录为止。
     *
     * @param dir      从这个目录开始找
     * @param filename 要查找的文件名
     * @return 找到的文件，如果未找到，则返回<code>null</code>
     */
    public static File find(File dir, String filename) {
        if (dir == null) {
            return null;
        }

        File file = new File(dir, filename);

        return file.exists() ? file : find(dir.getParentFile(), filename);
    }

    /**
     * 取得正规的文件。
     *
     * @param filename 文件名
     * @return 正规的文件，如果文件名为空，则返回<code>null</code>
     */
    public static File getCanonicalFile(String filename) {
        if (StringUtil.isEmpty(filename)) {
            return null;
        }

        try {
            return new File(filename).getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 判断指定资源ID是否存在于jar文件或目录中。
     *
     * @param base       目录或jar文件
     * @param resourceId 资源ID
     * @return 如果存在，则返回<code>true</code>
     */
    public static boolean resourceAvailable(File base, String resourceId) {
        boolean available = false;

        if (base.exists()) {
            if (base.isDirectory()) {
                available = new File(base, resourceId).exists();
            } else {
                available = false;

                try {
                    String[] files = ZipUtil.getFileNamesInZipFile(base.toURI().toURL(), new String[] { resourceId },
                                                                   null);

                    available = files != null && files.length > 0;
                } catch (IOException e) {
                }
            }
        }

        return available;
    }

    /**
     * 扫描目录，取得符合要求的所有文件。
     *
     * @param dir      目录
     * @param includes 包含文件
     * @param excludes 不包含文件
     * @return 所有文件
     */
    public static File[] getFilesInDirectory(File dir, String[] includes, String[] excludes) {
        String[] filenames = getFileNamesInDirectory(dir, includes, excludes);
        File[] files = new File[filenames.length];

        for (int i = 0; i < filenames.length; i++) {
            files[i] = new File(dir, filenames[i]).getAbsoluteFile();
        }

        return files;
    }

    /**
     * 扫描目录，取得符合要求的所有文件。
     *
     * @param dir      目录
     * @param includes 包含文件
     * @param excludes 不包含文件
     * @return 所有文件
     */
    public static String[] getFileNamesInDirectory(File dir, String[] includes, String[] excludes) {
        final PatternSet patterns = new PatternSet(includes, excludes).addDefaultExcludes();
        final List files = new ArrayList();
        Scanner scanner = new DirectoryScanner(dir, new DefaultScannerHandler() {
            @Override
            public boolean followUp() {
                String name = getScanner().getPath();

                return SelectorUtil.matchPathPrefix(name, patterns.getIncludes(), patterns.getExcludes());
            }

            @Override
            public void file() throws ScannerException {
                String name = getScanner().getPath();

                if (SelectorUtil.matchPath(name, patterns.getIncludes(), patterns.getExcludes())) {
                    files.add(name);
                }
            }
        });

        scanner.scan();

        return (String[]) files.toArray(new String[files.size()]);
    }

    public static void writeFile(File file, String text) throws IOException {
        FileWriter out = null;

        try {
            out = new FileWriter(file);
            out.write(text);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static void deleteDirectory(String directory) throws IOException {
        deleteDirectory(new File(directory));
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            String message = "Directory " + directory + " unable to be deleted.";

            throw new IOException(message);
        } else {
            return;
        }
    }

    public static void cleanDirectory(String directory) throws IOException {
        cleanDirectory(new File(directory));
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";

            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";

            throw new IllegalArgumentException(message);
        }

        IOException exception = null;
        File[] files = directory.listFiles();

        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        } else {
            return;
        }
    }

    public static void forceDelete(String file) throws IOException {
        forceDelete(new File(file));
    }

    public static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else if (!file.delete()) {
            String message = "File " + file + " unable to be deleted.";

            throw new IOException(message);
        }
    }

    public static File normalizeFile(String path) {
        return normalizeFile(new File(path));
    }

    public static File normalizeFile(File file) {
        File basedir = file.getParentFile();
        String filename = file.getName();
        StringBuffer buffer = new StringBuffer(filename.length());

        char lastChar = '\0';

        for (int i = 0; i < filename.length(); i++) {
            char c = filename.charAt(i);

            if (c == '_' || c == '-' || c == '.' || c == ',') {
                if (lastChar != '\0') {
                    if (c != lastChar) {
                        buffer.append(c);
                    }

                    lastChar = c;
                }
            } else {
                buffer.append(c);
                lastChar = c;
            }
        }

        int index = buffer.length() - 1;

        for (; index > 0; index--) {
            if (buffer.charAt(index) == '.') {
                for (int i = index - 1; i >= 0; i--) {
                    char c = buffer.charAt(i);

                    if (c == '_' || c == '-' || c == '.' || c == ',') {
                        buffer.deleteCharAt(i);
                    } else {
                        break;
                    }
                }
            }
        }

        return new File(basedir, buffer.toString()).getAbsoluteFile();
    }

    /*
     * ==========================================================================
     * ==
     */
    /* 常量和singleton。 */
    /*
     * ==========================================================================
     * ==
     */
    private static final char   COLON_CHAR     = ':';
    private static final String UNC_PREFIX     = "//";
    private static final String SLASH          = "/";
    private static final String BACKSLASH      = "\\";
    private static final char   SLASH_CHAR     = '/';
    private static final char   BACKSLASH_CHAR = '\\';

    /** 当前目录记号："." */
    public static final String CURRENT_DIR = ".";

    /** 上级目录记号：".." */
    public static final String UP_LEVEL_DIR = "..";

    /*
     * ==========================================================================
     * ==
     */
    /* 规格化路径。 */
    /*                                                                              */
    /* 去除'.'和'..'，支持windows路径和UNC路径。 */
    /*
     * ==========================================================================
     * ==
     */

    /**
     * 规格化路径。
     * <p>
     * 该方法忽略操作系统的类型，并是返回以“<code>/</code>”开始的绝对路径。转换规则如下：
     * <ol>
     * <li>路径为<code>null</code>，则返回<code>null</code>。</li>
     * <li>将所有backslash("\\")转化成slash("/")。</li>
     * <li>去除重复的"/"或"\\"。</li>
     * <li>去除"."，如果发现".."，则向上朔一级目录。</li>
     * <li>空路径返回"/"。</li>
     * <li>保留路径末尾的"/"（如果有的话）。</li>
     * <li>对于绝对路径，如果".."上朔的路径超过了根目录，则看作非法路径，返回<code>null</code>。</li>
     * </ol>
     * </p>
     *
     * @param path 要规格化的路径
     * @return 规格化后的路径，如果路径非法，则返回<code>null</code>
     */
    public static String normalizeAbsolutePath(String path) {
        String normalizedPath = normalizePath(path, false);

        if (normalizedPath != null && !normalizedPath.startsWith(SLASH)) {
            if (normalizedPath.equals(CURRENT_DIR) || normalizedPath.equals(CURRENT_DIR + SLASH_CHAR)) {
                normalizedPath = SLASH;
            } else if (normalizedPath.startsWith(UP_LEVEL_DIR)) {
                normalizedPath = null;
            } else {
                normalizedPath = SLASH_CHAR + normalizedPath;
            }
        }

        return normalizedPath;
    }

    /**
     * 规格化路径。
     * <p>
     * 该方法自动判别操作系统的类型。转换规则如下：
     * <ol>
     * <li>路径为<code>null</code>，则返回<code>null</code>。</li>
     * <li>将所有backslash("\\")转化成slash("/")。</li>
     * <li>去除重复的"/"或"\\"。</li>
     * <li>去除"."，如果发现".."，则向上朔一级目录。</li>
     * <li>空绝对路径返回"/"，空相对路径返回"./"。</li>
     * <li>保留路径末尾的"/"（如果有的话）。</li>
     * <li>对于绝对路径，如果".."上朔的路径超过了根目录，则看作非法路径，返回<code>null</code>。</li>
     * <li>对于Windows系统，有些路径有特殊的前缀，如驱动器名"c:"和UNC名"//hostname"，对于这些路径，保留其前缀，
     * 并对其后的路径部分适用上述所有规则。</li>
     * <li>Windows驱动器名被转换成大写，如"c:"转换成"C:"。</li>
     * </ol>
     * </p>
     *
     * @param path 要规格化的路径
     * @return 规格化后的路径，如果路径非法，则返回<code>null</code>
     */
    public static String normalizePath(String path) {
        return normalizePath(path, isWindows());
    }

    /**
     * 规格化路径。规则如下：
     * <ol>
     * <li>路径为<code>null</code>，则返回<code>null</code>。</li>
     * <li>将所有backslash("\\")转化成slash("/")。</li>
     * <li>去除重复的"/"或"\\"。</li>
     * <li>去除"."，如果发现".."，则向上朔一级目录。</li>
     * <li>空绝对路径返回"/"，空相对路径返回"./"。</li>
     * <li>保留路径末尾的"/"（如果有的话）。</li>
     * <li>对于绝对路径，如果".."上朔的路径超过了根目录，则看作非法路径，返回<code>null</code>。</li>
     * <li>对于Windows系统，有些路径有特殊的前缀，如驱动器名"c:"和UNC名"//hostname"，对于这些路径，保留其前缀，
     * 并对其后的路径部分适用上述所有规则。</li>
     * <li>Windows驱动器名被转换成大写，如"c:"转换成"C:"。</li>
     * </ol>
     *
     * @param path 要规格化的路径
     * @return 规格化后的路径，如果路径非法，则返回<code>null</code>
     */
    public static String normalizeWindowsPath(String path) {
        return normalizePath(path, true);
    }

    /**
     * 规格化Unix风格的路径，不支持Windows驱动器名和UNC路径。
     * <p>
     * 转换规则如下：
     * <ol>
     * <li>路径为<code>null</code>，则返回<code>null</code>。</li>
     * <li>将所有backslash("\\")转化成slash("/")。</li>
     * <li>去除重复的"/"或"\\"。</li>
     * <li>去除"."，如果发现".."，则向上朔一级目录。</li>
     * <li>空绝对路径返回"/"，空相对路径返回"./"。</li>
     * <li>保留路径末尾的"/"（如果有的话）。</li>
     * <li>对于绝对路径，如果".."上朔的路径超过了根目录，则看作非法路径，返回<code>null</code>。</li>
     * </ol>
     * </p>
     *
     * @param path 要规格化的路径
     * @return 规格化后的路径，如果路径非法，则返回<code>null</code>
     */
    public static String normalizeUnixPath(String path) {
        return normalizePath(path, false);
    }

    /**
     * 规格化路径。规则如下：
     * <ol>
     * <li>路径为<code>null</code>，则返回<code>null</code>。</li>
     * <li>将所有backslash("\\")转化成slash("/")。</li>
     * <li>去除重复的"/"或"\\"。</li>
     * <li>去除"."，如果发现".."，则向上朔一级目录。</li>
     * <li>空绝对路径返回"/"，空相对路径返回"./"。</li>
     * <li>保留路径末尾的"/"（如果有的话）。</li>
     * <li>对于绝对路径，如果".."上朔的路径超过了根目录，则看作非法路径，返回<code>null</code>。</li>
     * <li>对于Windows系统，有些路径有特殊的前缀，如驱动器名"c:"和UNC名"//hostname"，对于这些路径，保留其前缀，
     * 并对其后的路径部分适用上述所有规则。</li>
     * <li>Windows驱动器名被转换成大写，如"c:"转换成"C:"。</li>
     * </ol>
     *
     * @param path      要规格化的路径
     * @param isWindows 是否是windows路径，如果为<code>true</code>，则支持驱动器名和UNC路径
     * @return 规格化后的路径，如果路径非法，则返回<code>null</code>
     */
    private static String normalizePath(String path, boolean isWindows) {
        if (path == null) {
            return null;
        }

        path = path.trim();

        // 将"\\"转换成"/"，以便统一处理
        path = path.replace(BACKSLASH_CHAR, SLASH_CHAR);

        // 取得系统特定的路径前缀，对于windows系统，可能是："C:"或是"//hostname"
        String prefix = getSystemDependentPrefix(path, isWindows);

        if (prefix == null) {
            return null;
        }

        path = path.substring(prefix.length());

        // 对于绝对路径，prefix必须以"/"结尾，反之，绝对路径的prefix.length > 0
        if (prefix.length() > 0 || path.startsWith(SLASH)) {
            prefix += SLASH_CHAR;
        }

        // 保留path尾部的"/"
        boolean endsWithSlash = path.endsWith(SLASH);

        // 压缩路径中的"."和".."
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        StringBuffer buffer = new StringBuffer(prefix.length() + path.length());
        int level = 0;

        buffer.append(prefix);

        while (tokenizer.hasMoreTokens()) {
            String element = tokenizer.nextToken();

            // 忽略"."
            if (CURRENT_DIR.equals(element)) {
                continue;
            }

            // 回朔".."
            if (UP_LEVEL_DIR.equals(element)) {
                if (level == 0) {
                    // 如果prefix存在，并且试图越过最上层目录，这是不可能的，
                    // 返回null，表示路径非法。
                    if (prefix.length() > 0) {
                        return null;
                    }

                    buffer.append(UP_LEVEL_DIR).append(SLASH_CHAR);
                } else {
                    level--;

                    boolean found = false;

                    for (int i = buffer.length() - 2; i >= prefix.length(); i--) {
                        if (buffer.charAt(i) == SLASH_CHAR) {
                            buffer.setLength(i + 1);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        buffer.setLength(prefix.length());
                    }
                }

                continue;
            }

            // 添加到path
            buffer.append(element).append(SLASH_CHAR);
            level++;
        }

        // 如果是空的路径，则设置为"./"
        if (buffer.length() == 0) {
            buffer.append(CURRENT_DIR).append(SLASH_CHAR);
        }

        // 保留最后的"/"
        if (!endsWithSlash && buffer.length() > prefix.length() && buffer.charAt(buffer.length() - 1) == SLASH_CHAR) {
            buffer.setLength(buffer.length() - 1);
        }

        return buffer.toString();
    }

    /**
     * 取得和系统相关的文件名前缀。对于Windows系统，可能是驱动器名或UNC路径前缀"//hostname"。如果不存在前缀，则返回空字符串。
     *
     * @param path      绝对路径
     * @param isWindows 是否为windows系统
     * @return 和系统相关的文件名前缀，如果路径非法，例如："//"，则返回<code>null</code>
     */
    private static String getSystemDependentPrefix(String path, boolean isWindows) {
        if (isWindows) {
            // 判断UNC路径
            if (path.startsWith(UNC_PREFIX)) {
                // 非法UNC路径："//"
                if (path.length() == UNC_PREFIX.length()) {
                    return null;
                }

                // 假设路径为//hostname/subpath，返回//hostname
                int index = path.indexOf(SLASH, UNC_PREFIX.length());

                if (index != -1) {
                    return path.substring(0, index);
                } else {
                    return path;
                }
            }

            // 判断Windows绝对路径："c:/..."
            if (path.length() > 1 && path.charAt(1) == COLON_CHAR) {
                return path.substring(0, 2).toUpperCase();
            }
        }

        return "";
    }

    /*
     * ==========================================================================
     * ==
     */
    /* 取得基于指定basedir规格化路径。 */
    /*
     * ==========================================================================
     * ==
     */

    /**
     * 如果指定路径已经是绝对路径，则规格化后直接返回之，否则取得基于指定basedir的规格化路径。
     * <p>
     * 该方法自动判定操作系统的类型，如果是windows系统，则支持UNC路径和驱动器名。
     * </p>
     *
     * @param basedir 根目录，如果<code>path</code>为相对路径，表示基于此目录
     * @param path    要检查的路径
     * @return 规格化的路径，如果<code>path</code>非法，或<code>basedir</code>为
     *         <code>null</code>，则返回<code>null</code>
     */
    public static String getPathBasedOn(String basedir, String path) {
        return getPathBasedOn(basedir, path, isWindows());
    }

    /**
     * 如果指定路径已经是绝对路径，则规格化后直接返回之，否则取得基于指定basedir的规格化路径。
     *
     * @param basedir 根目录，如果<code>path</code>为相对路径，表示基于此目录
     * @param path    要检查的路径
     * @return 规格化的路径，如果<code>path</code>非法，或<code>basedir</code>为
     *         <code>null</code>，则返回<code>null</code>
     */
    public static String getWindowsPathBasedOn(String basedir, String path) {
        return getPathBasedOn(basedir, path, true);
    }

    /**
     * 如果指定路径已经是绝对路径，则规格化后直接返回之，否则取得基于指定basedir的规格化路径。
     *
     * @param basedir 根目录，如果<code>path</code>为相对路径，表示基于此目录
     * @param path    要检查的路径
     * @return 规格化的路径，如果<code>path</code>非法，或<code>basedir</code>为
     *         <code>null</code>，则返回<code>null</code>
     */
    public static String getUnixPathBasedOn(String basedir, String path) {
        return getPathBasedOn(basedir, path, false);
    }

    /**
     * 如果指定路径已经是绝对路径，则规格化后直接返回之，否则取得基于指定basedir的规格化路径。
     *
     * @param basedir   根目录，如果<code>path</code>为相对路径，表示基于此目录
     * @param path      要检查的路径
     * @param isWindows 是否是windows路径，如果为<code>true</code>，则支持驱动器名和UNC路径
     * @return 规格化的路径，如果<code>path</code>非法，或<code>basedir</code>为
     *         <code>null</code>，则返回<code>null</code>
     */
    private static String getPathBasedOn(String basedir, String path, boolean isWindows) {
        /*
         * ------------------------------------------- * 首先取得path的前缀，判断是否为绝对路径。
         * * 如果已经是绝对路径，则调用normalize后返回。 *
         * -------------------------------------------
         */
        if (path == null) {
            return null;
        }

        path = path.trim();

        // 将"\\"转换成"/"，以便统一处理
        path = path.replace(BACKSLASH_CHAR, SLASH_CHAR);

        // 取得系统特定的路径前缀，对于windows系统，可能是："C:"或是"//hostname"
        String prefix = getSystemDependentPrefix(path, isWindows);

        if (prefix == null) {
            return null;
        }

        // 如果是绝对路径，则直接返回
        if (prefix.length() > 0 || path.length() > prefix.length() && path.charAt(prefix.length()) == SLASH_CHAR) {
            return normalizePath(path, isWindows);
        }

        /*
         * ------------------------------------------- * 现在已经确定path是相对路径了，因此我们要
         * * 将它和basedir合并。 * -------------------------------------------
         */
        if (basedir == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();

        buffer.append(basedir.trim());

        // 防止重复的"/"，否则容易和UNC prefix混淆
        if (basedir.length() > 0 && path.length() > 0 && basedir.charAt(basedir.length() - 1) != SLASH_CHAR) {
            buffer.append(SLASH_CHAR);
        }

        buffer.append(path);

        return normalizePath(buffer.toString(), isWindows);
    }

    /*
     * ==========================================================================
     * ==
     */
    /* 取得相对于指定basedir相对路径。 */
    /*
     * ==========================================================================
     * ==
     */

    /**
     * 取得相对于指定根目录的相对路径。
     * <p>
     * 该方法自动判定操作系统的类型，如果是windows系统，则支持UNC路径和驱动器名。
     * </p>
     *
     * @param basedir 根目录
     * @param path    要计算的路径
     * @return 如果<code>path</code>和<code>basedir</code>是兼容的，则返回相对于
     *         <code>basedir</code>的相对路径，否则返回<code>path</code>本身。如果
     *         <code>basedir</code>不是绝对路径，或者路径非法，则返回<code>null</code>
     */
    public static String getRelativePath(String basedir, String path) {
        return getRelativePath(basedir, path, isWindows());
    }

    /**
     * 取得相对于指定根目录的相对路径。
     *
     * @param basedir 根目录
     * @param path    要计算的路径
     * @return 如果<code>path</code>和<code>basedir</code>是兼容的，则返回相对于
     *         <code>basedir</code>的相对路径，否则返回<code>path</code>本身。如果
     *         <code>basedir</code>不是绝对路径，或者路径非法，则返回<code>null</code>
     */
    public static String getWindowsRelativePath(String basedir, String path) {
        return getRelativePath(basedir, path, true);
    }

    /**
     * 取得相对于指定根目录的相对路径。
     *
     * @param basedir 根目录
     * @param path    要计算的路径
     * @return 如果<code>path</code>和<code>basedir</code>是兼容的，则返回相对于
     *         <code>basedir</code>的相对路径，否则返回<code>path</code>本身。如果
     *         <code>basedir</code>不是绝对路径，或者路径非法，则返回<code>null</code>
     */
    public static String getUnixRelativePath(String basedir, String path) {
        return getRelativePath(basedir, path, false);
    }

    /**
     * 取得相对于指定根目录的相对路径。
     *
     * @param basedir   根目录
     * @param path      要计算的路径
     * @param isWindows 是否是windows路径，如果为<code>true</code>，则支持驱动器名和UNC路径
     * @return 如果<code>path</code>和<code>basedir</code>是兼容的，则返回相对于
     *         <code>basedir</code>的相对路径，否则返回<code>path</code>本身。如果
     *         <code>basedir</code>不是绝对路径，或者路径非法，则返回<code>null</code>
     */
    private static String getRelativePath(String basedir, String path, boolean isWindows) {
        // 取得规格化的basedir，确保其为绝对路径
        basedir = normalizePath(basedir, isWindows);

        if (basedir == null) {
            return null;
        }

        String basePrefix = getSystemDependentPrefix(basedir, isWindows);

        if (basePrefix == null || basePrefix.length() == 0 && !basedir.startsWith(SLASH)) {
            return null; // basedir必须是绝对路径
        }

        // 取得规格化的path
        path = getPathBasedOn(basedir, path, isWindows);

        if (path == null) {
            return null;
        }

        String prefix = getSystemDependentPrefix(path, isWindows);

        // 如果path和basedir的前缀不同，则不能转换成相对于basedir的相对路径。
        // 直接返回规格化的path即可。
        if (!basePrefix.equals(prefix)) {
            return path;
        }

        // 保留path尾部的"/"
        boolean endsWithSlash = path.endsWith(SLASH);

        // 按"/"分隔basedir和path
        String[] baseParts = StringUtil.split(basedir.substring(basePrefix.length()), SLASH);
        String[] parts = StringUtil.split(path.substring(prefix.length()), SLASH);
        StringBuffer buffer = new StringBuffer();
        int i = 0;

        if (isWindows) {
            while (i < baseParts.length && i < parts.length && baseParts[i].equalsIgnoreCase(parts[i])) {
                i++;
            }
        } else {
            while (i < baseParts.length && i < parts.length && baseParts[i].equals(parts[i])) {
                i++;
            }
        }

        if (i < baseParts.length && i < parts.length) {
            for (int j = i; j < baseParts.length; j++) {
                buffer.append(UP_LEVEL_DIR).append(SLASH_CHAR);
            }
        }

        for (; i < parts.length; i++) {
            buffer.append(parts[i]);

            if (i < parts.length - 1) {
                buffer.append(SLASH_CHAR);
            }
        }

        if (buffer.length() == 0) {
            buffer.append(CURRENT_DIR);
        }

        String relpath = buffer.toString();

        if (endsWithSlash && !relpath.endsWith(SLASH)) {
            relpath += SLASH;
        }

        return relpath;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static boolean isSymbolicLink(File parent, String name) throws IOException {
        if (parent == null) {
            File f = new File(name);

            parent = f.getParentFile();
            name = f.getName();
        }

        File toTest = new File(parent.getCanonicalPath(), name);

        return !toTest.getAbsolutePath().equals(toTest.getCanonicalPath());
    }
}
