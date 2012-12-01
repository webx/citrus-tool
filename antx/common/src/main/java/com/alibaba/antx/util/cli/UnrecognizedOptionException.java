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

public class UnrecognizedOptionException extends ParseException {
    private static final long serialVersionUID = 3545793295331636280L;

    /**
     * <p>
     * Construct a new <code>UnrecognizedArgumentException</code> with the
     * specified detail message.
     * </p>
     *
     * @param message the detail message
     */
    public UnrecognizedOptionException(String message) {
        super(message);
    }
}
