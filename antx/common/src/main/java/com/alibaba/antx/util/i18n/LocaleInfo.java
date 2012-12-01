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

package com.alibaba.antx.util.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.alibaba.antx.util.StringUtil;

/**
 * 代表一个locale信息的类。
 *
 * @author Michael Zhou
 */
public class LocaleInfo {
    private static final String DEFAULT_LOCALE = "china";
    private static final Map    LOCALE_MAP     = new HashMap();
    private static LocaleInfo defaultLocaleInfo;

    static {
        LOCALE_MAP.put("us", new LocaleInfo("US", Locale.US, "ISO-8859-1"));
        LOCALE_MAP.put("china", new LocaleInfo("China", Locale.CHINA, "GBK"));

        try {
            setDefault(DEFAULT_LOCALE);
        } catch (UnsupportedLocaleException e) {
            throw new InternalError(); // 不会发生！
        }
    }

    private String name;
    private Locale locale;
    private String charset;

    /**
     * 创建一个locale信息项。
     *
     * @param name    地区名
     * @param locale  区域
     * @param charset 字符集
     */
    public LocaleInfo(String name, Locale locale, String charset) {
        this.name = name;
        this.locale = locale;
        this.charset = charset;
    }

    /**
     * 取得字符集名称。
     *
     * @return 编码字符集名称
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 取得区域。
     *
     * @return 区域
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * 取得地区名。
     *
     * @return 地区名
     */
    public String getName() {
        return name;
    }

    /**
     * 取得指定名称的<code>ResourceBundle</code>。
     *
     * @param bundleName bundle名称
     * @return 指定名称的<code>ResourceBundle</code>。
     */
    public ResourceBundle getBundle(String bundleName) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, getLocale());

        if (!getLocale().toString().startsWith(bundle.getLocale().toString())) {
            bundle = ResourceBundle.getBundle(bundleName, new Locale("", "", ""));
        }

        return bundle;
    }

    /**
     * 设置默认的locale。
     *
     * @param name locale名。
     * @throws UnsupportedLocaleException 如果指定名称的locale不存在
     */
    public static void setDefault(String name) throws UnsupportedLocaleException {
        LocaleInfo locale = get(name);

        if (locale == null) {
            throw new UnsupportedLocaleException("Unsupported locale \"" + name + "\"");
        }

        defaultLocaleInfo = locale;
    }

    /**
     * 取得默认的locale。
     *
     * @return 默认的locale
     */
    public static LocaleInfo getDefault() {
        return defaultLocaleInfo;
    }

    /**
     * 取得指定名称的locale。
     *
     * @param name locale名称
     * @return 指定名称的locale，如果不存在，则返回<code>null</code>。
     */
    public static LocaleInfo get(String name) {
        if (name == null) {
            return null;
        }

        LocaleInfo localeInfo = (LocaleInfo) LOCALE_MAP.get(name.toLowerCase());

        if (localeInfo == null) {
            int index = name.indexOf(":");
            String localeName = null;
            String charsetName = null;

            if (index >= 0) {
                localeName = name.substring(0, index).trim();
                charsetName = name.substring(index + 1).trim();
            } else {
                localeName = name.trim();
            }

            Locale locale = null;

            if (localeName != null) {
                String[] localeParts = StringUtil.split(localeName, "_");

                if (localeParts.length == 1) {
                    locale = new Locale(localeParts[0]);
                }

                if (localeParts.length == 2) {
                    locale = new Locale(localeParts[0], localeParts[1]);
                }

                if (localeParts.length == 3) {
                    locale = new Locale(localeParts[0], localeParts[1], localeParts[2]);
                }

                if (locale == null) {
                    return null;
                }
            }

            localeInfo = new LocaleInfo(name, locale, charsetName);
        }

        return localeInfo;
    }
}
