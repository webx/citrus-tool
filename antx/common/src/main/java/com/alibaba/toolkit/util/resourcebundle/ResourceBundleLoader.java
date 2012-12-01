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

package com.alibaba.toolkit.util.resourcebundle;

import java.io.InputStream;

/**
 * 装入resource bundle的数据.
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundleLoader.java,v 1.1 2003/07/03 07:26:35 baobao Exp
 *          $
 */
public interface ResourceBundleLoader {
    /**
     * 根据指定的bundle文件名称, 取得输入流.
     * <p>
     * 注意, 此处的参数为文件名, 区别于bundle的名称, <code>ResourceBundleFactory</code>必须在调用此方法前,
     * 将bundle的名称转换成文件名. 例如, bundle名为baseName_langauge_country,
     * 则文件名可能是baseName_language_country.xml.
     * </p>
     *
     * @param bundleFilename 要查找的bundle文件名
     * @return bundle的数据流, 如果指定bundle文件不存在, 则返回<code>null</code>
     * @throws ResourceBundleCreateException 如果文件存在, 但读取数据流失败
     */
    InputStream openStream(String bundleFilename) throws ResourceBundleCreateException;

    /**
     * 判断两个<code>ResourceBundleLoader</code>是否等效. 这将作为
     * <code>ResourceBundle</code>的cache的依据.
     *
     * @param obj 要比较的另一个对象
     * @return 如果等效, 则返回<code>true</code>
     */
    boolean equals(Object obj);

    /**
     * 取得hash值. 等效的<code>ResourceBundleLoader</code>应该具有相同的hash值.
     *
     * @return hash值
     */
    int hashCode();
}
