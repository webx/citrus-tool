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

package com.alibaba.antx.config.generator;

import java.io.InputStream;

import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.descriptor.ConfigGenerate;

public interface ConfigGeneratorCallback {
    /** 切换到下一个目标文件，并设置相应的输入/输出流。 */
    String nextEntry(String template, ConfigGenerate generate);

    void nextEntry(ConfigDescriptor descriptor, InputStream is, String dest);

    /** 切换到日志文件，并设置相应的输入/输出流。 */
    void logEntry(ConfigDescriptor descriptor, String logfileName);

    /** 关闭一个目标或日志文件。 */
    void closeEntry();
}
