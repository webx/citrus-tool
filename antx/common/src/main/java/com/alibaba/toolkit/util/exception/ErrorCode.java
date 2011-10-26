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

package com.alibaba.toolkit.util.exception;

import com.alibaba.toolkit.util.enumeration.Enum;

/**
 * 标志指定的异常支持错误码.
 *
 * @version $Id: ErrorCode.java,v 1.1 2003/07/03 07:26:22 baobao Exp $
 * @author Michael Zhou
 */
public interface ErrorCode {
    /**
     * 取得错误码.
     *
     * @return 错误码
     */
    Enum getErrorCode();
}
