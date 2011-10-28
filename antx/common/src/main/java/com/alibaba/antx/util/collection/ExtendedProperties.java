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

package com.alibaba.antx.util.collection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import com.alibaba.antx.util.StringUtil;
import com.alibaba.antx.util.i18n.LocaleInfo;

/**
 * 扩展<code>Properties</code>类, 支持从<code>Reader</code>中读取unicode字符。
 * 
 * @author Michael Zhou
 */
public class ExtendedProperties extends Properties {
    private static final long serialVersionUID = 3258126960071555380L;
    private static final String KEY_VALUE_SEPARATORS = "= \t\r\n\f";
    private static final String STRICT_KEY_VALUE_SEPARATORS = "=";
    private static final String WHITE_SPACE_CHARS = " \t\r\n\f";

    /**
     * 从指定的properties文件中，以默认的编码字符集读取属性和值。
     * 
     * @param resource properties文件
     * @throws IOException 读文件失败或文件格式错误
     */
    public synchronized void load(URL resource) throws IOException {
        load(resource, null);
    }

    /**
     * 从指定的properties文件中，以指定的编码字符集读取属性和值。
     * 
     * @param resource properties文件
     * @param charset 编码字符集
     * @throws IOException 读文件失败或文件格式错误
     */
    public synchronized void load(URL resource, String charset) throws IOException {
        charset = getCharset(charset);

        InputStream istream = null;

        try {
            istream = resource.openStream();

            if (!(istream instanceof BufferedInputStream)) {
                istream = new BufferedInputStream(istream, 8192);
            }

            load(new InputStreamReader(istream, charset), resource.toExternalForm());
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
     * 从指定的输入流中，以默认的编码字符集读取属性和值。
     * 
     * @param istream 输入字符流
     * @throws IOException 读文件失败或文件格式错误
     */
    @Override
    public void load(InputStream istream) throws IOException {
        load(istream, null, null);
    }

    /**
     * 从指定的输入流中，以指定的编码字符集读取属性和值。
     * 
     * @param istream 输入字符流
     * @throws IOException 读文件失败或文件格式错误
     */
    public synchronized void load(InputStream istream, String charset, String url) throws IOException {
        if (charset == null) {
            charset = getCharset(null);
        }

        if (!(istream instanceof BufferedInputStream)) {
            istream = new BufferedInputStream(istream, 8192);
        }

        load(new InputStreamReader(istream, charset), url);
    }

    private String getCharset(String charset) {
        if (charset == null) {
            charset = LocaleInfo.getDefault().getCharset();
        }

        return charset;
    }

    /**
     * 从指定的输入流中，以默认的编码字符集读取属性和值。
     * 
     * @param reader 输入字符流
     * @throws IOException 读文件失败或文件格式错误
     */
    private synchronized void load(Reader reader, String url) throws IOException {
        BufferedReader in = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
        int lineNumber = 0;

        while (true) {
            // 取得下一行
            String line = in.readLine();

            lineNumber++;

            if (line == null) {
                return;
            }

            // 去掉行首尾的空白
            line = line.trim();

            if (line.length() > 0) {
                // 如果该行以“\”结尾，则看作是上一行的继续
                char firstChar = line.charAt(0);

                if (firstChar != '#' && firstChar != '!') {
                    while (isContinueLine(line)) {
                        String nextLine = in.readLine();

                        if (nextLine == null) {
                            nextLine = "";
                        }

                        String loppedLine = line.substring(0, line.length() - 1);

                        // 去掉新行上的空格
                        int startIndex = 0;

                        for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
                            if (WHITE_SPACE_CHARS.indexOf(nextLine.charAt(startIndex)) == -1) {
                                break;
                            }
                        }

                        nextLine = nextLine.substring(startIndex, nextLine.length());
                        line = new String(loppedLine + nextLine);
                    }

                    // 找到key的开始处
                    int len = line.length();
                    int keyStart;

                    for (keyStart = 0; keyStart < len; keyStart++) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(keyStart)) == -1) {
                            break;
                        }
                    }

                    // 忽略空行
                    if (keyStart == len) {
                        continue;
                    }

                    // 查找key和value的分界符
                    int separatorIndex;

                    for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);

                        if (currentChar == '\\') {
                            separatorIndex++;
                        } else if (KEY_VALUE_SEPARATORS.indexOf(currentChar) != -1) {
                            break;
                        }
                    }

                    // 跳过key后面的空白（如果有的话）
                    int valueIndex;

                    for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }
                    }

                    // 跳过一个非空白的key-value分界符
                    if (valueIndex < len) {
                        if (STRICT_KEY_VALUE_SEPARATORS.indexOf(line.charAt(valueIndex)) != -1) {
                            valueIndex++;
                        }
                    }

                    // 跳过分界符后面的空白
                    while (valueIndex < len) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
                            break;
                        }

                        valueIndex++;
                    }

                    String key = line.substring(keyStart, separatorIndex);
                    String value = separatorIndex < len ? line.substring(valueIndex, len) : "";

                    // 转换key和value
                    key = loadConvert(key, url, lineNumber);
                    value = loadConvert(value, url, lineNumber);

                    put(key, value);
                }
            }
        }
    }

    /**
     * 判断该行是否和下一行是连续的行。
     * 
     * @param line 指定行
     * @return 如果是和下一行相连的，则返回<code>true</code>
     */
    private boolean isContinueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;

        while (index >= 0 && line.charAt(index--) == '\\') {
            slashCount++;
        }

        return slashCount % 2 == 1;
    }

    /**
     * 将&#92;uxxxx转换成unicode字符，将特殊符号转换成其原来的格式。
     * 
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    private String loadConvert(String str, String url, int lineNumber) {
        char ch;
        int len = str.length();
        StringBuffer buffer = new StringBuffer(len);

        if (StringUtil.isEmpty(url)) {
            url = "<unknown source>";
        }

        for (int x = 0; x < len;) {
            ch = str.charAt(x++);

            if (ch == '\\') {
                ch = str.charAt(x++);

                if (ch == 'u') {
                    // Read the xxxx
                    int value = 0;

                    for (int i = 0; i < 4; i++) {
                        ch = str.charAt(x++);

                        switch (ch) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + ch - '0';
                                break;

                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + ch - 'a';
                                break;

                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + ch - 'A';
                                break;

                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding at " + url + ", line "
                                        + lineNumber);
                        }
                    }

                    buffer.append((char) value);
                } else {
                    if (ch == '\\') {
                        ch = '\\';
                    } else if (ch == 't') {
                        ch = '\t';
                    } else if (ch == 'r') {
                        ch = '\r';
                    } else if (ch == 'n') {
                        ch = '\n';
                    } else if (ch == 'f') {
                        ch = '\f';
                    } else {
                        throw new IllegalArgumentException("Invalid \\" + ch + " at " + url + ", line " + lineNumber);
                    }

                    buffer.append(ch);
                }
            } else {
                buffer.append(ch);
            }
        }

        return buffer.toString();
    }
}
