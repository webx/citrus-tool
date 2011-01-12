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
package com.alibaba.antx.expand.cli;

import java.text.MessageFormat;

import com.alibaba.antx.expand.ExpanderException;
import com.alibaba.antx.expand.ExpanderRuntime;
import com.alibaba.antx.expand.ExpanderRuntimeImpl;
import com.alibaba.antx.util.Profiler;
import com.alibaba.antx.util.cli.CommandLine;
import com.alibaba.citrus.logconfig.LogConfigurator;

/**
 * Antxexpand的命令行主程序。
 * 
 * @author Michael Zhou
 */
public class Main {
    private static CommandLine cli;
    private static ExpanderRuntime runtime;

    public static void main(String[] args) {
        // 起始时间
        Profiler.start("Starting antxexpand");

        // 初始化日志
        initLogging(false, null);

        // 执行
        int returnCode = 0;

        try {
            returnCode = run(args);
        } catch (CLIException e) {
            System.err.println(e.getMessage());
            System.exit(returnCode);
        } finally {
            Profiler.release();
        }

        // 结束，显示总时间
        runtime.info("");
        runtime.info(getDuration(
                "总耗费时间：{0,choice,0#|.1#{0,number,integer}分}{1,choice,0#|.1#{1,number,integer}秒}{2,number,integer}毫秒",
                Profiler.getEntry().getDuration()));
        runtime.info("");

        // 返回值
        System.exit(returnCode);
    }

    private static String getDuration(String message, long duration) {
        long ms = duration % 1000;
        long secs = (duration / 1000) % 60;
        long min = duration / 1000 / 60;

        return MessageFormat.format(message, new Object[] { new Long(min), new Long(secs), new Long(ms) });
    }

    private static void initLogging(boolean verbose, String charset) {
        LogConfigurator.getConfigurator().configureDefault(verbose, charset);
    }

    private static int run(String[] args) {
        CLIManager manager = new CLIManager();

        if (args.length == 0) {
            args = new String[] { "-h" };
        }

        cli = manager.parse(args);

        String charset = cli.getOptionValue(CLIManager.OPT_CHARSET);
        ExpanderRuntimeImpl runtimeImpl = new ExpanderRuntimeImpl(System.in, System.out, System.err, charset);

        runtime = runtimeImpl;

        // 显示帮助
        if (cli.hasOption(CLIManager.OPT_HELP)) {
            manager.help(runtimeImpl.getOut());
            return 0;
        }

        if (cli.hasOption(CLIManager.OPT_VERBOSE)) {
            runtimeImpl.setVerbose();
            initLogging(true, charset);
        } else {
            initLogging(false, charset);
        }

        if (cli.hasOption(CLIManager.OPT_EXPAND_WAR)) {
            runtimeImpl.getExpander().setExpandWar(getBooleanValue(CLIManager.OPT_EXPAND_WAR));
        }

        if (cli.hasOption(CLIManager.OPT_EXPAND_RAR)) {
            runtimeImpl.getExpander().setExpandRar(getBooleanValue(CLIManager.OPT_EXPAND_RAR));
        }

        if (cli.hasOption(CLIManager.OPT_EXPAND_EJB_JAR)) {
            runtimeImpl.getExpander().setExpandEjbjar(getBooleanValue(CLIManager.OPT_EXPAND_EJB_JAR));
        }

        if (cli.hasOption(CLIManager.OPT_OVERWRITE)) {
            runtimeImpl.getExpander().setOverwrite(getBooleanValue(CLIManager.OPT_OVERWRITE));
        }

        if (cli.hasOption(CLIManager.OPT_KEEP_REDUNDANT_FILES)) {
            runtimeImpl.getExpander().setKeepRedundantFiles(getBooleanValue(CLIManager.OPT_KEEP_REDUNDANT_FILES));
        }

        args = cli.getArgs();

        if (args.length >= 1) {
            runtimeImpl.getExpander().setSrcfile(args[0]);
        }

        if (args.length >= 2) {
            runtimeImpl.getExpander().setDestdir(args[1]);
        }

        // 运行antxexpand
        try {
            runtimeImpl.start();
        } catch (Exception e) {
            runtimeImpl.error(e);
        }

        return 0;
    }

    private static boolean getBooleanValue(String key) {
        String value = cli.getOptionValue(key);

        if (value == null) {
            value = "yes";
        }

        if (value.equalsIgnoreCase("yes")) {
            return true;
        } else if (value.equalsIgnoreCase("no")) {
            return false;
        } else {
            throw new ExpanderException("invalid value of -" + key + ": " + value + ", should be yes or no");
        }
    }
}
