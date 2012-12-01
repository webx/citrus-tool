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

package com.alibaba.antx.config.cli;

import java.io.PrintWriter;

import com.alibaba.antx.util.cli.CommandLine;
import com.alibaba.antx.util.cli.HelpFormatter;
import com.alibaba.antx.util.cli.OptionBuilder;
import com.alibaba.antx.util.cli.Options;
import com.alibaba.antx.util.cli.ParseException;
import com.alibaba.antx.util.cli.PosixParser;

/**
 * Antxconfig命令行解析器。
 *
 * @author Michael Zhou
 */
public class CLIManager {
    public static final String OPT_HELP                   = "h";
    public static final String OPT_INCLUDE_PACKAGES       = "p";
    public static final String OPT_EXCLUDE_PACKAGES       = "P";
    public static final String OPT_INCLUDE_DESCRIPTORS    = "d";
    public static final String OPT_EXCLUDE_DESCRIPTORS    = "D";
    public static final String OPT_TEXT_MODE              = "t";
    public static final String OPT_GUI_MODE               = "g";
    public static final String OPT_INTERACTIVE_MODE       = "i";
    public static final String OPT_NON_INTERACTIVE_MODE   = "I";
    public static final String OPT_VERBOSE                = "v";
    public static final String OPT_CHARSET                = "c";
    public static final String OPT_USER_PROPERTIES        = "u";
    public static final String OPT_SHARED_PROPERTIES      = "s";
    public static final String OPT_SHARED_PROPERTIES_NAME = "n";
    public static final String OPT_OUTPUT_FILES           = "o";
    public static final String OPT_TYPE                   = "T";
    private Options options;

    public CLIManager() {
        OptionBuilder builder = new OptionBuilder();

        options = new Options();

        options.addOption(builder.withLongOpt("help").withDescription("显示帮助信息").create(OPT_HELP));

        options.addOption(builder.withLongOpt("include-descriptors").hasArg()
                                 .withDescription("包含哪些配置描述文件，例如：conf/auto-config.xml，可使用*、**、?通配符，如有多项，用逗号分隔")
                                 .create(OPT_INCLUDE_DESCRIPTORS));

        options.addOption(builder.withLongOpt("exclude-descriptors").hasArg()
                                 .withDescription("排除哪些配置描述文件，可使用*、**、?通配符，如有多项，用逗号分隔").create(OPT_EXCLUDE_DESCRIPTORS));

        options.addOption(builder.withLongOpt("include-packages").hasArg()
                                 .withDescription("包含哪些打包文件，例如：target/*.war，可使用*、**、?通配符，如有多项，用逗号分隔").create(OPT_INCLUDE_PACKAGES));

        options.addOption(builder.withLongOpt("exclude-packages").hasArg()
                                 .withDescription("排除哪些打包文件，可使用*、**、?通配符，如有多项，用逗号分隔").create(OPT_EXCLUDE_PACKAGES));

        options.addOption(builder.withLongOpt("interactive").hasOptionalArg()
                                 .withDescription("交互模式：auto|on|off，默认为auto，无参数表示on").create(OPT_INTERACTIVE_MODE));

        options.addOption(builder.withLongOpt("non-interactive").withDescription("非交互模式，相当于--interactive=off")
                                 .create(OPT_NON_INTERACTIVE_MODE));

        options.addOption(builder.withLongOpt("gui").withDescription("图形用户界面（交互模式）").create(OPT_GUI_MODE));

        options.addOption(builder.withLongOpt("text").withDescription("文本用户界面（交互模式）").create(OPT_TEXT_MODE));

        options.addOption(builder.withLongOpt("verbose").withDescription("显示更多信息").create(OPT_VERBOSE));

        options.addOption(builder.withLongOpt("charset").hasArg().withDescription("输入/输出编码字符集").create(OPT_CHARSET));

        options.addOption(builder.withLongOpt("userprop").hasArg().withDescription("用户属性文件")
                                 .create(OPT_USER_PROPERTIES));

        options.addOption(builder.withLongOpt("shared-props").hasArg().withDescription("共享的属性文件URL列表，以逗号分隔")
                                 .create(OPT_SHARED_PROPERTIES));

        options.addOption(builder.withLongOpt("shared-props-name").hasArg().withDescription("共享的属性文件的名称")
                                 .create(OPT_SHARED_PROPERTIES_NAME));

        options.addOption(builder.withLongOpt("output").hasArg().withDescription("输出文件名或目录名").create(OPT_OUTPUT_FILES));

        options.addOption(builder.withLongOpt("type").hasArg().withDescription("文件类型，例如：war, jar, ear等")
                                 .create(OPT_TYPE));
    }

    public CommandLine parse(String[] args) {
        CommandLine cli;

        try {
            cli = new PosixParser().parse(options, args);
        } catch (ParseException e) {
            throw new CLIException(e);
        }

        return cli;
    }

    public void help(PrintWriter out) {
        HelpFormatter formatter = new HelpFormatter();

        formatter.defaultSyntaxPrefix = "使用方法：";

        formatter.printHelp(out, HelpFormatter.DEFAULT_WIDTH, "antxconfig [可选参数] [目录名|包文件名]\n", "可选参数：", options,
                            HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "\n");
    }
}
