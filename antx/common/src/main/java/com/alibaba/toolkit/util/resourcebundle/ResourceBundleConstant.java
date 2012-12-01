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

/**
 * 定义<code>ResourceBundle</code>相关的常量和错误信息.
 *
 * @author Michael Zhou
 * @version $Id: ResourceBundleConstant.java,v 1.1 2003/07/03 07:26:35 baobao
 *          Exp $
 */
public interface ResourceBundleConstant {
    // ResourceBundle类的常量.
    int   INITIAL_CACHE_SIZE   = 25;
    float CACHE_LOAD_FACTOR    = 1.0f;
    int   MAX_BUNDLES_SEARCHED = 3;

    // ResourceBundle类的错误信息.
    String RB_BASE_NAME_IS_NULL                 = "The basename of the resource bundle should not be null";
    String RB_MISSING_RESOURCE_BUNDLE           = "Could not find bundle for base name \"{0}\", locale \"{1}\"";
    String RB_FAILED_LOADING_RESOURCE_BUNDLE    = "Failed to load bundle for base name \"{0}\", locale \"{1}\"";
    String RB_RESOURCE_NOT_FOUND                = "Could not find resource for bundle \"{0}\", key \"{1}\"";
    String RB_CLONE_NOT_SUPPORTED               = "Clone is not supported by class \"{0}\"";
    String RB_BASE_NAME_LONGER_THAN_BUNDLE_NAME = "The basename \"{0}\" is longer than the bundle name \"{1}\"";
    String RB_FAILED_OPENING_STREAM             = "Could not open stream for resource \"{0}\"";

    // XMLResourceBundle类的resource类型
    String RB_RESOURCE_TYPE_MESSAGE = "message";
    String RB_RESOURCE_TYPE_MAP     = "map";
    String RB_RESOURCE_TYPE_LIST    = "list";

    // XMLResourceBundle的文件名后缀
    String RB_RESOURCE_EXT_XML = ".xml";

    // XMLResourceBundle类的错误信息
    String RB_FAILED_READING_XML_DOCUMENT = "Failed to read XML document \"{0}\"";
    String RB_DUPLICATED_RESOURCE_KEY     = "Duplicated resource key \"{0}\"";
    String RB_ENUM_CLASS_NOT_FOUND        = "Specified Enum class \"{0}\" not found in context class loader \"{1}\"";
    String RB_ENUM_ID_NOT_FOUND           = "Invalid Enum ID \"{0}\" for Enum class \"{1}\"";
    String RB_DUPLICATED_MAP_RESOURCE_KEY = "Duplicated mapped resource key \"{0}\" for resource \"{1}\"";

    // XMLResourceBundle的XPATH常量.
    String XPATH_GROUPS                = "/resource-bundle/group";
    String XPATH_UNGROUPED_RESOURCES   = "/resource-bundle/message | /resource-bundle/map | /resource-bundle/list";
    String XPATH_RESOURCES             = "message | map | list";
    String XPATH_GROUP_ENUM            = "string(@enum)";
    String XPATH_RESOURCE_ID           = "string(@id)";
    String XPATH_RESOURCE_MESSAGE_DATA = "normalize-space(data)";
}
