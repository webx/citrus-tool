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
package com.alibaba.antx.util.cli;

/**
 * A class that implements the <code>CommandLineParser</code> interface  can parse a String array
 * according to the {@link Options} specified and return a {@link CommandLine}.
 *
 * @author John Keyes (john at integralsource.com)
 */
public interface CommandLineParser {
    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     *
     * @return the list of atomic option and value tokens
     *
     * @throws ParseException if there are any problems encountered while parsing the command line
     *         tokens.
     */
    public CommandLine parse(Options options, String[] arguments)
            throws ParseException;

    /**
     * Parse the arguments according to the specified options.
     *
     * @param options the specified Options
     * @param arguments the command line arguments
     * @param stopAtNonOption specifies whether to continue parsing the arguments if a non option
     *        is encountered.
     *
     * @return the list of atomic option and value tokens
     *
     * @throws ParseException if there are any problems encountered while parsing the command line
     *         tokens.
     */
    public CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption)
            throws ParseException;
}
