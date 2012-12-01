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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.antx.config.ConfigResource;
import com.alibaba.antx.config.ConfigSettings;
import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.generator.ConfigGenerator;
import com.alibaba.antx.util.PatternSet;
import com.alibaba.antx.util.SelectorUtil;
import com.alibaba.antx.util.scanner.DefaultScannerHandler;

/**
 * 代表一个可配置项的信息。
 *
 * @author Michael Zhou
 */
public abstract class ConfigEntry {
    private       ConfigSettings  settings;
    private       ConfigResource  resource;
    private       File            outputFile;
    private       PatternSet      descriptorPatterns;
    private       PatternSet      packagePatterns;
    protected     ConfigEntry[]   subEntries;
    private final ConfigGenerator generator;

    /**
     * 创建一个结点。
     *
     * @param resource 指定结点的资源
     * @param settings antxconfig的设置
     */
    public ConfigEntry(ConfigResource resource, File outputFile, ConfigSettings settings) {
        this.resource = resource;
        this.outputFile = outputFile;
        this.settings = settings;
        this.generator = new ConfigGenerator(settings);
    }

    /**
     * 取得结点的名称。
     *
     * @return 结点的名称
     */
    public String getName() {
        return getConfigEntryResource().getName();
    }

    /** 不包含任何descriptor和sub entries的空结点。 */
    public boolean isEmpty() {
        return getSubEntries().length == 0 && getGenerator().getConfigDescriptors().length == 0;
    }

    /**
     * 取得资源。
     *
     * @return 资源
     */
    public ConfigResource getConfigEntryResource() {
        return resource;
    }

    /** 取得输出文件或目录。 */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * 取得config设置。
     *
     * @return config设置
     */
    public ConfigSettings getConfigSettings() {
        return settings;
    }

    /**
     * 取得config descriptor的patterns，如果未设置，则使用默认值。
     *
     * @return config descriptor的pattern
     */
    public PatternSet getDescriptorPatterns() {
        return descriptorPatterns;
    }

    /**
     * 设置config descriptor的pattern，如果未设置，则使用默认值。
     *
     * @param descriptorsPatterns config descriptor的pattern
     */
    public void setDescriptorPatterns(PatternSet descriptorPatterns) {
        this.descriptorPatterns = descriptorPatterns;
    }

    /**
     * 取得用于匹配当前结点下的所有子结点的pattern。
     *
     * @return pattern
     */
    public PatternSet getPackagePatterns() {
        return packagePatterns;
    }

    /**
     * 设置用于匹配当前结点下的所有子结点的pattern。
     *
     * @param packagePatterns pattern
     */
    public void setPackagePatterns(PatternSet packagePatterns) {
        this.packagePatterns = packagePatterns;
    }

    /**
     * 取得当前config结点对应的generator。
     *
     * @return generator对象
     */
    public ConfigGenerator getGenerator() {
        return generator;
    }

    /**
     * 取得当前config结点下的所有子结点。
     *
     * @return 子结点数组，如果不存在，则返回空数组
     */
    public ConfigEntry[] getSubEntries() {
        return subEntries;
    }

    /** 扫描结点。 */
    public void scan() {
        scan(null);
    }

    /** 扫描结点。 */
    protected abstract void scan(InputStream istream);

    /** 装配descriptor的context，用来生成文件。 */
    protected void populateDescriptorContext(Map context, String string) {
    }

    /** 生成配置文件。 */
    public boolean generate() {
        if (getOutputFile() != null) {
            settings.getOut().printf("Output file: %s%n%n", getOutputFile().getAbsolutePath());
        }

        return generate(null, null);
    }

    /** 生成配置文件。 */
    protected abstract boolean generate(InputStream istream, OutputStream ostream);

    /** 扫描处理器。 */
    public class Handler extends DefaultScannerHandler {
        private List subEntries = new ArrayList();

        public ConfigEntry[] getSubEntries() {
            return (ConfigEntry[]) subEntries.toArray(new ConfigEntry[subEntries.size()]);
        }

        @Override
        public void startScanning() {
            StringBuffer buffer = new StringBuffer();

            buffer.append("Scanning ").append(getScanner().getBaseURL()).append("\n");
            buffer.append("  descriptors: ").append(getDescriptorPatterns()).append("\n");
            buffer.append("     packages: ").append(getPackagePatterns()).append("\n");

            settings.debug(buffer.toString());
        }

        @Override
        public void file() {
            String name = getScanner().getPath();

            if (isDescriptorFile(name)) {
                settings.debug("Loading descriptor " + getScanner().getURL() + "\n");

                loadDescriptor();
            } else if (isPackageFile(name)) {
                ConfigResource resource = new ConfigResource(getScanner().getURL(), name);
                ConfigEntryFactory factory = getConfigSettings().getConfigEntryFactory();
                ConfigEntry subEntry = createSubEntry(name, resource, factory);

                InputStream istream = null;

                try {
                    istream = getScanner().getInputStream();
                    subEntry.scan(istream);
                } finally {
                    if (istream != null) {
                        try {
                            istream.close();
                        } catch (IOException e) {
                        }
                    }
                }

                if (!subEntry.isEmpty()) {
                    subEntries.add(subEntry);
                }
            }
        }

        @Override
        public void directory() {
            String name = getScanner().getPath();

            if (isPackageFile(name)) {
                ConfigResource resource = new ConfigResource(getScanner().getURL(), name);
                ConfigEntryFactory factory = getConfigSettings().getConfigEntryFactory();
                ConfigEntry subEntry = createSubEntry(name, resource, factory);

                subEntry.scan();

                if (!subEntry.isEmpty()) {
                    subEntries.add(subEntry);
                }
            }
        }

        private ConfigEntry createSubEntry(String name, ConfigResource resource, ConfigEntryFactory factory) {
            ConfigEntry subEntry;

            if (outputFile != null && (!outputFile.exists() || outputFile.isDirectory())) {
                subEntry = factory.create(resource, new File(outputFile, name), null);
            } else {
                subEntry = factory.create(resource, null, null);
            }

            return subEntry;
        }

        /**
         * 是否跟进指定目录或文件。该方法有助于提高扫描速度。
         *
         * @return 如果是，则返回<code>true</code>
         */
        @Override
        public boolean followUp() {
            String name = getScanner().getPath();
            boolean followUp = false;

            followUp |= SelectorUtil.matchPathPrefix(name, getDescriptorPatterns().getIncludes(),
                                                     getDescriptorPatterns().getExcludes());

            followUp |= SelectorUtil.matchPathPrefix(name, getPackagePatterns().getIncludes(), getPackagePatterns()
                    .getExcludes());

            if (isPackageFile(name)) {
                return false;
            }

            if (!followUp) {
                getConfigSettings().debug("Skipping directory " + name);
            }

            return followUp;
        }

        /** 装入descriptor。 */
        private void loadDescriptor() {
            URL descriptorURL = getScanner().getURL();
            ConfigResource descriptorResource = new ConfigResource(descriptorURL, getScanner().getPath());

            ConfigDescriptor descriptor;
            InputStream istream = null;

            try {
                istream = getScanner().getInputStream();
                descriptor = getGenerator().addConfigDescriptor(descriptorResource, istream);
            } finally {
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (IOException e) {
                    }
                }
            }

            populateDescriptorContext(descriptor.getContext(), descriptor.getName());
        }

        /**
         * 查看指定名称是否符合descriptor的patterns。
         *
         * @param name 要匹配的名称
         * @return 如果符合descriptor的patterns，则返回<code>true</code>
         */
        private boolean isDescriptorFile(String name) {
            return SelectorUtil.matchPath(name, getDescriptorPatterns().getIncludes(), getDescriptorPatterns()
                    .getExcludes());
        }

        /**
         * 查看指定名称是否符合jarfile的patterns。
         *
         * @param name 要匹配的名称
         * @return 如果符合jarfile的patterns，则返回<code>true</code>
         */
        private boolean isPackageFile(String name) {
            return SelectorUtil.matchPath(name, getPackagePatterns().getIncludes(), getPackagePatterns().getExcludes());
        }
    }
}
