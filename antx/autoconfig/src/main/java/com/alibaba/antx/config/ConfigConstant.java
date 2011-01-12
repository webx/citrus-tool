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

import com.alibaba.antx.config.generator.VelocityTemplateEngine;

/**
 * Config的常量。
 * 
 * @author Michael Zhou
 */
public interface ConfigConstant {
    /** GUI交互模式。 */
    String MODE_GUI = "gui";

    /** 文本交互模式。 */
    String MODE_TEXT = "text";

    /** 交互模式：开 */
    String INTERACTIVE_ON = "on";

    /** 交互模式：关 */
    String INTERACTIVE_OFF = "off";

    /** 交互模式：自动 */
    String INTERACTIVE_AUTO = "auto";

    /** Velocity设置：缓冲池中创建的parser数. */
    int VELOCITY_NUMBER_OF_PARSERS = 1;

    /** Velocity设置：默认的macro文件, 从classpath中装入 */
    String VELOCITY_MACRO_FILE = VelocityTemplateEngine.class.getPackage().getName().replace('.', '/') + "/macro.vm";

    String UNKNWON_REFS_KEY = "_unknwonRefs";
}
