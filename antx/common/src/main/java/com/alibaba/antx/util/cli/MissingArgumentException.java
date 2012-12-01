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

package com.alibaba.antx.util.cli;

/**
 * <p>
 * Thrown when an option requiring an argument is not provided with an argument.
 * </p>
 *
 * @author John Keyes (john at integralsource.com)
 * @see ParseException
 */
public class MissingArgumentException extends ParseException {
    private static final long serialVersionUID = 3546639927757648435L;

    /**
     * <p>
     * Construct a new <code>MissingArgumentException</code> with the specified
     * detail message.
     * </p>
     *
     * @param message the detail message
     */
    public MissingArgumentException(String message) {
        super(message);
    }
}
