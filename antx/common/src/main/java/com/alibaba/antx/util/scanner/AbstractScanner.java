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

/**
 * 所有scanner的基类。
 *
 * @author Michael Zhou
 */
public abstract class AbstractScanner implements Scanner {
    private ScannerHandler handler;
    private String         path;

/**
     * 创建一个scanner。
     *
     * @param 回调函数
     */
    public AbstractScanner(ScannerHandler handler) {
        this.handler = handler;
    }

    /**
     * 取得scanner handler。
     *
     * @return scanner handler
     */
    public ScannerHandler getScannerHandler() {
        return handler;
    }

    /**
     * 取得当前正在扫描的文件路径。
     *
     * @return 文件路径
     */
    public String getPath() {
        return (path == null) ? ""
                              : path;
    }

    /**
     * 设置当前正在扫描的文件路径。
     *
     * @param path 文件路径
     *
     * @return 原路径
     */
    protected String setPath(String path) {
        String old = getPath();

        if (path != null) {
            path = path.replace('\\', '/');
        }

        this.path = path;

        return old;
    }

    /**
     * 转换成字符串。
     *
     * @return 字符串表示
     */
    public String toString() {
        return "Scanner[URL=" + getBaseURL() + "]";
    }
}
