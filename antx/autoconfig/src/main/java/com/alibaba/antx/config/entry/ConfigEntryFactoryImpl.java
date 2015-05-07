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

package com.alibaba.antx.config.entry;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.ConfigResource;
import com.alibaba.antx.config.ConfigSettings;
import com.alibaba.antx.util.NumberUtil;
import com.alibaba.antx.util.PatternSet;
import com.alibaba.antx.util.StringUtil;
import com.alibaba.toolkit.util.regex.PathNameCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

public class ConfigEntryFactoryImpl implements ConfigEntryFactory {
    private ConfigSettings settings;

    public ConfigEntryFactoryImpl(ConfigSettings settings) {
        this.settings = settings;
    }

    private final static java.util.regex.Pattern extPattern = java.util.regex.Pattern.compile("\\.(\\w+)$");

    public ConfigEntry create(ConfigResource resource, File outputFile, String type) {
        File file = resource.getFile();
        String name = resource.getName();

        if (type != null) {
            type = type.trim().toLowerCase();
        } else {
            Matcher m = extPattern.matcher(name);

            if (m.find()) {
                type = m.group(1).toLowerCase();
            }
        }

        if (StringUtil.isBlank(type)) {
            type = null;
        }

        if (name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }

        if (file != null && !file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        if ("war".equals(type) || file != null && file.isDirectory() && new File(file, "WEB-INF").isDirectory()) {
            return createWarEntry(resource, outputFile);
        }

        if ("jar".equals(type) || "ear".equals(type) || "rar".equals(type)) {
            return createGenericJarEntry(resource, outputFile);
        }

        if (file != null && file.isDirectory()) {
            return createGenericDirectoryEntry(resource, outputFile);
        }

        return createGenericJarEntry(resource, outputFile);
    }

    /**
     * 设置公共context。
     *
     * @param context context
     */
    protected void populateCommonContext(Map context) {
        context.put("stringUtil", new StringUtil());
        context.put("numberUtil", new NumberUtil());
    }

    /**
     * 设置基于war的context，根据descriptor name取得component name。
     *
     * @param context context
     */
    protected void populateWarContext(Map context, String name) {
        Pattern componentNamePattern;
        PatternMatcher matcher = new Perl5Matcher();
        String componentName = "";

        try {
            componentNamePattern = new PathNameCompiler().compile("META-INF/**/autoconf/auto-config.xml");
        } catch (MalformedPatternException e) {
            throw new ConfigException(e);
        }

        if (matcher.matches(name.replace('\\', '/'), componentNamePattern)) {
            componentName = matcher.getMatch().group(1);
        }

        context.put("component", componentName);
    }

    /** 创建基于war的entry。 */
    private ConfigEntry createWarEntry(ConfigResource resource, File outputFile) {
        File file = resource.getFile();
        ConfigEntry entry;

        if (file != null && file.isDirectory()) {
            entry = new DirectoryConfigEntry(resource, outputFile, settings) {
                @Override
                protected void populateDescriptorContext(Map context, String name) {
                    populateCommonContext(context);
                    populateWarContext(context, name);
                }
            };
        } else {
            entry = new ZipConfigEntry(resource, outputFile, settings) {
                @Override
                protected void populateDescriptorContext(Map context, String name) {
                    populateCommonContext(context);
                    populateWarContext(context, name);
                }
            };
        }

        entry.setDescriptorPatterns(new PatternSet(settings.getDescriptorPatterns(), new PatternSet("META-INF/**/auto-config.xml")).addDefaultExcludes());

        entry.setPackagePatterns(new PatternSet(settings.getPackagePatterns(), new PatternSet("WEB-INF/lib/*.jar")).addDefaultExcludes());

        return entry;
    }

    /** 创建基于普通jar的entry。 */
    private ConfigEntry createGenericJarEntry(ConfigResource resource, File outputFile) {
        File file = resource.getFile();
        ConfigEntry entry;

        if (file != null && file.isDirectory()) {
            entry = new DirectoryConfigEntry(resource, outputFile, settings) {
                @Override
                protected void populateDescriptorContext(Map context, String name) {
                    populateCommonContext(context);
                }
            };
        } else {
            entry = new ZipConfigEntry(resource, outputFile, settings) {
                @Override
                protected void populateDescriptorContext(Map context, String name) {
                    populateCommonContext(context);
                }
            };
        }

        entry.setDescriptorPatterns(new PatternSet(settings.getDescriptorPatterns(), new PatternSet("META-INF/**/auto-config.xml")).addDefaultExcludes());

        entry.setPackagePatterns(new PatternSet(settings.getPackagePatterns(), new PatternSet("**/*.jar, **/*.war, **/*.rar, **/*.ear")).addDefaultExcludes());

        return entry;
    }

    /** 创建基于普通目录的entry。 */
    private ConfigEntry createGenericDirectoryEntry(ConfigResource resource, File outputFile) {
        ConfigEntry entry = new DirectoryConfigEntry(resource, outputFile, settings) {
            @Override
            protected void populateDescriptorContext(Map context, String name) {
                populateCommonContext(context);
            }
        };

        entry.setDescriptorPatterns(new PatternSet(settings.getDescriptorPatterns(), new PatternSet(
                "conf/**/auto-config.xml, META-INF/**/auto-config.xml")).addDefaultExcludes());

        // 如果是对目录操作，且未指定package patterns，则默认不搜索目录下的packages文件
        entry.setPackagePatterns(new PatternSet(settings.getPackagePatterns(), new PatternSet(null, "**"))
                                         .addDefaultExcludes());

        return entry;
    }
}
