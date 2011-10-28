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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 和Zip文件相关的工具类。
 * 
 * @author Michael Zhou
 */
public class ZipUtil {
    private static final Log log = LogFactory.getLog(ZipUtil.class);

    /**
     * 取得jar URL。
     * 
     * @param jarfileURL 代表jar文件的URL
     * @param path 资源在jar文件中的路径
     * @return jar URL
     */
    public static URL getJarURL(URL jarfileURL, String path) throws MalformedURLException {
        StringBuffer url = new StringBuffer();

        url.append("jar:").append(jarfileURL.toExternalForm()).append("!");

        if (!path.startsWith("/")) {
            url.append("/");
        }

        url.append(path.replace('\\', '/'));

        return new URL(url.toString());
    }

    /**
     * 扫描zip文件，取得符合要求的所有文件。
     * 
     * @param zipfileURL zip文件的URL
     * @param includes 包含文件
     * @param excludes 不包含文件
     * @return 所有文件的URL
     */
    public static URL[] getFilesInZipFile(URL zipfileURL, String[] includes, String[] excludes) throws IOException {
        String[] filenames = getFileNamesInZipFile(zipfileURL, includes, excludes);
        URL[] urls = new URL[filenames.length];

        for (int i = 0; i < filenames.length; i++) {
            urls[i] = getJarURL(zipfileURL, filenames[i]);
        }

        return urls;
    }

    /**
     * 扫描zip文件，取得符合要求的所有文件。
     * 
     * @param zipfileURL zip文件的URL
     * @param includes 包含文件
     * @param excludes 不包含文件
     * @return 所有文件路径
     */
    public static String[] getFileNamesInZipFile(URL zipfileURL, String[] includes, String[] excludes)
            throws IOException {
        ZipScanner zipScanner = new ZipScanner();

        zipScanner.setSrc(zipfileURL);
        zipScanner.setIncludes(includes);
        zipScanner.setExcludes(excludes);
        zipScanner.addDefaultExcludes();
        zipScanner.scan();

        return zipScanner.getIncludedFiles();
    }

    /**
     * 展开zip文件到指定目录
     * 
     * @param zipfile Zip文件
     * @param todir 展开目录
     * @param overwrite 是否覆盖
     * @throws IOException 读写文件失败，或Zip格式错误
     */
    public static void expandFile(File zipfile, File todir, boolean overwrite) throws IOException {
        InputStream istream = null;

        try {
            istream = new FileInputStream(zipfile);
            expandFile(istream, todir, overwrite);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 展开zip文件到指定目录
     * 
     * @param istream 输入流
     * @param todir 展开目录
     * @param overwrite 是否覆盖
     * @throws IOException 读写文件失败，或Zip格式错误
     */
    public static void expandFile(InputStream istream, File todir, boolean overwrite) throws IOException {
        ZipInputStream zipStream = null;

        if (!(istream instanceof BufferedInputStream)) {
            istream = new BufferedInputStream(istream, 8192);
        }

        try {
            zipStream = new ZipInputStream(istream);

            ZipEntry zipEntry = null;

            while ((zipEntry = zipStream.getNextEntry()) != null) {
                extractFile(todir, zipStream, zipEntry, overwrite);
            }

            log.info("expand complete");
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 展开一个文件。
     * 
     * @param todir 展开到此目录
     * @param zipStream 压缩流
     * @param zipEntry zip结点
     * @param overwrite 如果文件或目录已存在，是否覆盖
     * @throws IOException 读写文件失败，或Zip格式错误
     */
    protected static void extractFile(File todir, InputStream zipStream, ZipEntry zipEntry, boolean overwrite)
            throws IOException {
        String entryName = zipEntry.getName();
        Date entryDate = new Date(zipEntry.getTime());
        boolean isDirectory = zipEntry.isDirectory();
        File targetFile = FileUtil.getFile(todir, entryName);

        if (!overwrite && targetFile.exists() && targetFile.lastModified() >= entryDate.getTime()) {
            log.debug("Skipping " + targetFile + " as it is up-to-date");
            return;
        }

        log.info("expanding " + entryName + " to " + targetFile);

        if (isDirectory) {
            targetFile.mkdirs();
        } else {
            File dir = targetFile.getParentFile();

            dir.mkdirs();

            byte[] buffer = new byte[8192];
            int length = 0;
            OutputStream ostream = null;

            try {
                ostream = new BufferedOutputStream(new FileOutputStream(targetFile), 8192);

                while ((length = zipStream.read(buffer)) >= 0) {
                    ostream.write(buffer, 0, length);
                }
            } finally {
                if (ostream != null) {
                    try {
                        ostream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        targetFile.setLastModified(entryDate.getTime());
    }
}
