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

package com.alibaba.antx.util.cli;

import com.alibaba.toolkit.util.exception.ChainedException;

public class ParseException extends ChainedException {
    private static final long serialVersionUID = 3256725099842646072L;

    public ParseException() {
        super();
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>
     * Construct a new <code>ParseException</code>  with the specified detail message.
     * </p>
     *
     * @param message the detail message
     */
    public ParseException(String message) {
        super(message);
    }
}
