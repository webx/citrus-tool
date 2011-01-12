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
package com.alibaba.antx.util.configuration;

import com.alibaba.toolkit.util.exception.ChainedException;

/**
 * Thrown when a <code>Configurable</code> component cannot be configured properly, or if a value
 * cannot be retrieved properly.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 */
public class ConfigurationException extends ChainedException {
    private static final long serialVersionUID = 3257281452726235443L;

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Construct a new <code>ConfigurationException</code> instance.
     *
     * @param message The detail message for this exception.
     */
    public ConfigurationException(final String message) {
        this(message, null);
    }

    /**
     * Construct a new <code>ConfigurationException</code> instance.
     *
     * @param message The detail message for this exception.
     * @param throwable the root cause of the exception
     */
    public ConfigurationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
