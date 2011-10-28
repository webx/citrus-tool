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

package com.agilejava.docbkx.maven;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;

import com.alibaba.maven.plugin.docbook.WordBreaker;

public abstract class AbstractCitrusPdfMojo extends AbstractPdfMojo {
    /**
     * 指定一个locale，例如：“zh_CN”，用来对fo文件强制分词。这样做是为了解决目前fop不支持
     * <code>character-by-character</code>分词方法，只支持<code>word-by-word</code>
     * 的问题。对于中文fo文件，如果不设置该参数（<code>forceBreakingWords == zh_CN</code>
     * ），那么可能出现英文字母错误使用中文字体的问题。
     * 
     * @parameter
     */
    private String forceBreakingWords;

    /**
     * The fonts that should be taken into account. (Without this parameter, the
     * PDF document will only be able to reference the default fonts.)
     * 
     * @parameter
     */
    private Font[] fonts;

    @Override
    public void postProcessResult(File result) throws MojoExecutionException {
        // 将fonts复制到父类中 -- 因为maven无法注入另一个包中的父类参数。
        if (fonts != null) {
            setField(AbstractPdfMojo.class, "fonts", fonts);
        }

        // 对fop文件进行分词
        forceBreakingWords = trimToNull(forceBreakingWords);

        if (forceBreakingWords != null) {
            Locale locale = parseLocale(forceBreakingWords);
            WordBreaker wb = new WordBreaker(result, locale);

            try {
                wb.filter();
            } catch (Exception e) {
                throw new MojoExecutionException("Failed to breaking words", e);
            }
        }

        super.postProcessResult(result);
    }

    private static String trimToNull(String str) {
        if (str == null) {
            return null;
        }

        String result = str.trim();

        if (result == null || result.length() == 0) {
            return null;
        }

        return result;
    }

    private static Locale parseLocale(String localeString) {
        localeString = trimToNull(localeString);

        if (localeString == null) {
            return null;
        }

        String language = "";
        String country = "";
        String variant = "";

        // language
        int start = 0;
        int index = localeString.indexOf("_");

        if (index >= 0) {
            language = localeString.substring(start, index).trim();

            // country
            start = index + 1;
            index = localeString.indexOf("_", start);

            if (index >= 0) {
                country = localeString.substring(start, index).trim();

                // variant
                variant = localeString.substring(index + 1).trim();
            } else {
                country = localeString.substring(start).trim();
            }
        } else {
            language = localeString.substring(start).trim();
        }

        return new Locale(language, country, variant);
    }

    private void setField(Class<?> targetType, String fieldName, Object value) throws MojoExecutionException {
        try {
            Field field = targetType.getDeclaredField(fieldName);

            if (field != null) {
                field.setAccessible(true);
                field.set(this, value);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
