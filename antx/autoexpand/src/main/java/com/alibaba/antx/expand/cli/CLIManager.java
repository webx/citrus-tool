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

package com.alibaba.antx.expand.cli;

import java.io.PrintWriter;

import com.alibaba.antx.util.cli.CommandLine;
import com.alibaba.antx.util.cli.HelpFormatter;
import com.alibaba.antx.util.cli.OptionBuilder;
import com.alibaba.antx.util.cli.Options;
import com.alibaba.antx.util.cli.ParseException;
import com.alibaba.antx.util.cli.PosixParser;

/**
 * Antxexpand命令行解析器。
 *
 * @author Michael Zhou
 */
public class CLIManager {
    public static final String OPT_HELP                 = "h";
    public static final String OPT_VERBOSE              = "v";
    public static final String OPT_CHARSET              = "c";
    public static final String OPT_EXPAND_WAR           = "w";
    public static final String OPT_EXPAND_RAR           = "r";
    public static final String OPT_EXPAND_EJB_JAR       = "e";
    public static final String OPT_OVERWRITE            = "o";
    public static final String OPT_KEEP_REDUNDANT_FILES = "k";
    private Options options;

    public CLIManager() {
        OptionBuilder builder = new OptionBuilder();

        options = new Options();

        options.addOption(builder.withLongOpt("help").withDescription("显示帮助信息").create(OPT_HELP));

        options.addOption(builder.withLongOpt("verbose").withDescription("显示更多信息").create(OPT_VERBOSE));

        options.addOption(builder.withLongOpt("charset").hasArg().withDescription("输入/输出编码字符集").create(OPT_CHARSET));

        options.addOption(builder.withLongOpt("expand-war").hasOptionalArg().withDescription("是否展开war（yes|no），默认为yes")
                                 .create(OPT_EXPAND_WAR));

        options.addOption(builder.withLongOpt("expand-rar").hasOptionalArg().withDescription("是否展开rar（yes|no），默认为yes")
                                 .create(OPT_EXPAND_RAR));

        options.addOption(builder.withLongOpt("expand-ejb-jar").hasOptionalArg()
                                 .withDescription("是否展开ejb-jar（yes|no），默认为no").create(OPT_EXPAND_EJB_JAR));

        options.addOption(builder.withLongOpt("overwrite").hasOptionalArg()
                                 .withDescription("如果目标目录中的文件比zip文件中的项要新，是否覆盖之，默认为no").create(OPT_OVERWRITE));

        options.addOption(builder.withLongOpt("keep-redundant-files").hasOptionalArg()
                                 .withDescription("如果目标目录中有多余的文件，是否保持而不删除，默认为no").create(OPT_KEEP_REDUNDANT_FILES));
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

        formatter.printHelp(out, HelpFormatter.DEFAULT_WIDTH, "antxexpand [可选参数] 文件名 [目标目录]\n", "可选参数：", options,
                            HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "\n");
    }
}
