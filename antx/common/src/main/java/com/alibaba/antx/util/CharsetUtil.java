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
 *
 */
package com.alibaba.antx.util;

import java.nio.charset.Charset;

import org.apache.commons.lang.SystemUtils;

public class CharsetUtil {
    public static String detectedSystemCharset() {
        String charset = Charset.defaultCharset().name();

        // 在unix环境中，根据LANG修正charset。
        if (SystemUtils.IS_OS_UNIX) {
            String lang = System.getenv("LANG");
            int index = -1;

            if (!StringUtil.isBlank(lang) && (index = lang.indexOf(".")) >= 0) {
                String langCharset = lang.substring(index + 1);

                if (Charset.isSupported(langCharset)) {
                    charset = langCharset;
                }
            }
        }

        return charset;
    }
}
