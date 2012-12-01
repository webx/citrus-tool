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

import java.io.InputStream;
import java.net.URL;

/**
 * 扫描器。
 *
 * @author Michael Zhou
 */
public interface Scanner {
    /**
     * 取得当前扫描的base URL。
     *
     * @return 当前扫描的base URL
     */
    URL getBaseURL();

    /**
     * 取得当前正在扫描的文件路径。
     *
     * @return 文件路径
     */
    String getPath();

    /**
     * 取得当前正在扫描的文件的URL。
     *
     * @return URL
     */
    URL getURL();

    /**
     * 取得当前正在扫描的文件的输入流。
     *
     * @return 输入流
     */
    InputStream getInputStream();

    /** 执行扫描。 */
    void scan();
}
