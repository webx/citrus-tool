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

package com.alibaba.antx.util.configuration;

import org.xml.sax.Locator;

/**
 * 配置项在配置文件中的位置.
 *
 * @author Michael Zhou
 */
public class Location implements Locator {
    private final String publicId;
    private final String systemId;
    private final int    lineNumber;
    private final int    columnNumber;

    /** 代表空的location. */
    public static final Location EMPTY_LOCATION = new Location(null, null, -1, -1);

    /**
     * 创建位置信息.
     *
     * @param locator XML文件中的位置信息
     */
    public Location(Locator locator) {
        this(locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
    }

    /**
     * 创建位置信息.
     *
     * @param publicId     public ID
     * @param systemId     system ID, 也就是文件路径或URL
     * @param lineNumber   行号
     * @param columnNumber 列号
     */
    public Location(String publicId, String systemId, int lineNumber, int columnNumber) {
        if (publicId != null) {
            publicId = publicId.trim();

            if (publicId != null && publicId.length() == 0) {
                publicId = null;
            }
        }

        if (systemId != null) {
            systemId = systemId.trim();

            if (systemId != null && systemId.length() == 0) {
                systemId = null;
            }
        }

        if (lineNumber <= 0) {
            lineNumber = -1;
        }

        if (columnNumber <= 0) {
            columnNumber = -1;
        }

        this.publicId = publicId;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    /**
     * 取得public ID.
     *
     * @return public ID, 如果不存在, 则返回<code>null</code>
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * 取得system ID.
     *
     * @return system ID, 如果不存在, 则返回<code>null</code>
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * 取得行号.
     *
     * @return 行号, 如果不存在, 则返回<code>-1</code>
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 取得列号.
     *
     * @return 列号, 如果不存在, 则返回<code>-1</code>
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * 转换成字符串表示.
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        if (systemId == null) {
            return "Unknown location";
        }

        StringBuffer buffer = new StringBuffer(systemId);

        if (lineNumber > 0) {
            buffer.append(':').append(lineNumber);

            if (columnNumber > 0) {
                buffer.append(':').append(columnNumber);
            }
        }

        return buffer.toString();
    }
}
