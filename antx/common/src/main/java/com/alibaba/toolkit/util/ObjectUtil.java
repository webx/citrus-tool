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

package com.alibaba.toolkit.util;

/**
 * 和一般对象有关的小工具.
 *
 * @author Michael Zhou
 * @version $Id: ObjectUtil.java,v 1.1 2003/07/03 07:26:15 baobao Exp $
 */
public class ObjectUtil {
    /**
     * 比较两个对象是否相等.
     *
     * @param o1 对象1
     * @param o2 对象2
     * @return 如果相等, 则返回<code>true</code>
     */
    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /**
     * 取得对象的hash值, 如果对象为<code>null</code>, 则返回<code>0</code>
     *
     * @param o 对象
     * @return hash值
     */
    public static int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }
}
