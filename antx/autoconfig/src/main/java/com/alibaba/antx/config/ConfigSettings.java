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
package com.alibaba.antx.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;

import com.alibaba.antx.config.entry.ConfigEntryFactory;
import com.alibaba.antx.config.props.PropertiesSet;
import com.alibaba.antx.util.PatternSet;

public interface ConfigSettings extends ConfigLogger {
    BufferedReader getIn();

    PrintWriter getOut();

    PrintWriter getErr();

    String getCharset();

    PatternSet getDescriptorPatterns();

    PatternSet getPackagePatterns();

    String getInteractiveMode();

    String getMode();

    File[] getDestFiles();

    File[] getOutputFiles();

    PropertiesSet getPropertiesSet();

    boolean isVerbose();

    ConfigEntryFactory getConfigEntryFactory();

    String getType();
}
