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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.descriptor.ConfigGenerate;
import com.alibaba.antx.util.StreamUtil;

/**
 * 在目录中生成文件的callback。
 * 
 * @author Michael Zhou
 */
public class DirectoryCallback implements ConfigGeneratorCallback {
    private final ConfigGenerator generator;
    private File destfileBase;
    private InputStream istream;
    private OutputStream ostream;

    public DirectoryCallback(ConfigGenerator generator) {
        this.generator = generator;
    }

    public DirectoryCallback(ConfigGenerator generator, File destfileBase) {
        this.generator = generator;
        this.destfileBase = destfileBase;
    }

    public String nextEntry(String template, ConfigGenerate generate) {
        ConfigDescriptor descriptor = generate.getConfigDescriptor();
        String base = generate.getTemplateBase();
        template = generate.getTemplate();
        String dest = generate.getDestfile();

        File templateBase = descriptor.getBaseFile();
        File destfileBase = this.destfileBase;

        if (destfileBase == null) {
            destfileBase = templateBase;
        }

        File destFile = new File(destfileBase, dest);

        File templateFile = new File(templateBase, base + template);
        File templateFileInPlace = new File(templateBase, template);

        if (!templateFile.exists()) {
            if (!templateFileInPlace.exists()) {
                throw new ConfigException("Could not find template file: " + templateFileInPlace.getAbsolutePath()
                        + " for descriptor: " + descriptor.getURL());
            }

            if (templateFileInPlace.getAbsolutePath().equals(destFile.getAbsolutePath())) {
                templateFile.getParentFile().mkdirs();

                try {
                    StreamUtil.io(new FileInputStream(templateFileInPlace), new FileOutputStream(templateFile), true,
                            true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                templateFile = templateFileInPlace;
            }
        } else {
            template = base + template;
        }

        // 创建destFile的父目录
        File destBase = destFile.getParentFile();

        destBase.mkdirs();

        if (!destBase.isDirectory()) {
            throw new ConfigException("Could not create directory: " + destBase.getAbsolutePath());
        }

        try {
            istream = new BufferedInputStream(new FileInputStream(templateFile), 8192);
            ostream = new BufferedOutputStream(new FileOutputStream(destFile), 8192);
        } catch (FileNotFoundException e) {
            throw new ConfigException(e);
        }

        generator.getSession().setInputStream(istream);
        generator.getSession().setOutputStream(ostream);

        return template;
    }

    public void nextEntry(ConfigDescriptor descriptor, InputStream is, String dest) {
    }

    public void logEntry(ConfigDescriptor descriptor, String logfileName) {
        File templateBase = descriptor.getBaseFile();
        File destfileBase = this.destfileBase;

        if (destfileBase == null) {
            destfileBase = templateBase;
        }

        File logfile = new File(destfileBase, logfileName);

        // 创建logfile的父目录
        File logbase = logfile.getParentFile();

        logbase.mkdirs();

        if (!logbase.isDirectory()) {
            throw new ConfigException("Could not create directory: " + logbase.getAbsolutePath());
        }

        try {
            ostream = new BufferedOutputStream(new FileOutputStream(logfile), 8192);
        } catch (FileNotFoundException e) {
            throw new ConfigException(e);
        }

        generator.getSession().setOutputStream(ostream);
    }

    public void closeEntry() {
        if (istream != null) {
            try {
                istream.close();
            } catch (IOException e) {
            }

            istream = null;
        }

        if (ostream != null) {
            try {
                ostream.close();
            } catch (IOException e) {
            }

            ostream = null;
        }
    }
}
