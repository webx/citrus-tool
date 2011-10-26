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

import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Formatter;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.antx.config.ConfigConstant;
import com.alibaba.antx.config.ConfigException;

/**
 * Velocity引擎。
 * 
 * @author Michael Zhou
 */
public class VelocityTemplateEngine {
    private static Logger log = LoggerFactory.getLogger(VelocityTemplateEngine.class);
    private static VelocityTemplateEngine instance;
    private VelocityEngine engine = new VelocityEngine();

    public static VelocityTemplateEngine getInstance() {
        if (instance == null) {
            instance = new VelocityTemplateEngine();
        }

        return instance;
    }

    /**
     * 初始化velocity.
     */
    public VelocityTemplateEngine() {
        // parser的数量.
        engine.setProperty(RuntimeConstants.PARSER_POOL_SIZE, new Integer(ConfigConstant.VELOCITY_NUMBER_OF_PARSERS));

        // 设置log系统.
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new LogSystem());

        // 允许递归
        engine.setProperty("velocimacro.context.localscope", "true");

        // 设置resource loader系统.
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        // 设置velocimacro.
        engine.setProperty(RuntimeConstants.VM_LIBRARY, ConfigConstant.VELOCITY_MACRO_FILE);

        // 执行初始化.
        try {
            engine.init();
        } catch (Exception e) {
            throw new ConfigException(e);
        }
    }

    /**
     * 渲染模板.
     * 
     * @param context 上下文信息
     * @param reader 模板源
     * @param writer 输出流
     * @param url
     * @return 被渲染后的字符数组
     * @throws Exception 渲染出错
     */
    public boolean render(Context context, Reader reader, Writer writer, String templateName, String configName,
                          URL baseURL) throws Exception {
        Set<String> unknwonRefs = new TreeSet<String>();
        context.put(ConfigConstant.UNKNWON_REFS_KEY, unknwonRefs);

        try {
            engine.evaluate(context, writer, templateName, reader);
        } finally {
            context.remove(ConfigConstant.UNKNWON_REFS_KEY);
        }

        if (!unknwonRefs.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            Formatter fmt = new Formatter(buf);

            fmt.format("Undefined placeholders found in template:%n");
            fmt.format("- Template:   %s%n", templateName);
            fmt.format("- Descriptor: %s%n", configName);
            fmt.format("- Base URL:   %s%n", baseURL.toExternalForm());
            fmt.format("---------------------------------------------------------------%n");

            for (String ref : unknwonRefs) {
                fmt.format("-> %s%n", ref);
            }

            fmt.format("---------------------------------------------------------------%n");

            log.error(fmt.toString());

            return false;
        }

        return true;
    }

    /**
     * Velocity Logger
     */
    private class LogSystem implements LogChute {
        public void init(RuntimeServices runtimeServices) {
        }

        public boolean isLevelEnabled(int level) {
            switch (level) {
                case ERROR_ID:
                    return log.isErrorEnabled();

                case WARN_ID:
                    return log.isWarnEnabled();

                case INFO_ID:
                    return log.isInfoEnabled();

                case DEBUG_ID:
                    return log.isDebugEnabled();

                case TRACE_ID:
                    return log.isTraceEnabled();

                default:
                    return false;
            }
        }

        public void log(int level, String message) {
            log(level, message, null);
        }

        public void log(int level, String message, Throwable t) {
            message = processMessage(message);

            switch (level) {
                case ERROR_ID:
                    log.error(message);
                    break;

                case WARN_ID:
                    log.warn(message);
                    break;

                case INFO_ID:
                    log.info(message);
                    break;

                case DEBUG_ID:
                    log.debug(message);
                    break;

                case TRACE_ID:
                    log.trace(message);
                    break;

                default:
            }
        }

        /**
         * 除去message中的exception前缀，使之更美观。
         */
        private String processMessage(String message) {
            if (message != null) {
                message = message.replaceFirst("^[\\w\\.\\$]+Exception: ", "");
            }

            return message;
        }
    }
}
