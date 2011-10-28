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

package com.alibaba.antx.util.scanner;

import com.alibaba.antx.util.ZipUtil;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Zip文件扫描器。
 *
 * @author Michael Zhou
 */
public class ZipScanner extends AbstractScanner {
    private URL            zipURL;
    private ZipInputStream zis;
    private ZipEntry       zipEntry;

/**
     * 创建一个zip文件扫描器。
     *
     * @param zipfile zip文件
     * @param handler 回调函数
     */
    public ZipScanner(File zipfile, ScannerHandler handler) {
        super(handler);

        if (!zipfile.exists() || !zipfile.isFile() || !zipfile.canRead()) {
            throw new IllegalArgumentException(zipfile + " is not a readable file");
        }

        try {
            this.zipURL = zipfile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(zipfile + " is not a readable file");
        }
    }

/**
     * 创建一个zip文件扫描器。
     *
     * @param zipURL zip文件的URL
     * @param handler 回调函数
     */
    public ZipScanner(URL zipURL, ScannerHandler handler) {
        super(handler);

        this.zipURL = zipURL;
    }

    /**
     * 取得扫描的zip文件的URL。
     *
     * @return 正在扫描的zip文件的URL
     */
    public URL getBaseURL() {
        return zipURL;
    }

    /**
     * 取得当前正在扫描的文件的URL。
     *
     * @return URL
     */
    public URL getURL() {
        try {
            return ZipUtil.getJarURL(getBaseURL(), getPath());
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
        return new FilterInputStream(zis) {
                public void close() throws IOException {
                    // 避免关闭
                }
            };
    }

    /**
     * 设置zip文件的输入流。
     *
     * @param istream zip文件的输入流
     */
    public void setInputStream(InputStream istream) {
        if (istream != null) {
            this.zis = new ZipInputStream(istream);
        }
    }

    /**
     * 取得当前正在处理的zip entry。
     *
     * @return zip entry
     */
    public ZipEntry getZipEntry() {
        return zipEntry;
    }

    /**
     * 执行扫描。
     */
    public void scan() {
        getScannerHandler().setScanner(this);
        getScannerHandler().startScanning();

        boolean needClose = false;

        if (zis == null) {
            try {
                zis       = new ZipInputStream(getBaseURL().openStream());
                needClose = true;
            } catch (IOException e) {
                throw new ScannerException(e);
            }
        }

        try {
            doScan();
        } finally {
            if (needClose) {
                try {
                    zis.close();
                } catch (IOException e) {
                }
            }
        }

        getScannerHandler().endScanning();
    }

    /**
     * 执行扫描。
     */
    protected void doScan() {
        try {
            while ((zipEntry = zis.getNextEntry()) != null) {
                String savedPath = setPath(zipEntry.getName());

                if (zipEntry.isDirectory()) {
                    getScannerHandler().directory();
                } else {
                    getScannerHandler().file();
                }

                setPath(savedPath);
            }
        } catch (IOException e) {
            System.err.println("WARING! Failed to get ZipEntry: " + zipURL.toExternalForm());
            e.printStackTrace();
        }
    }
}
