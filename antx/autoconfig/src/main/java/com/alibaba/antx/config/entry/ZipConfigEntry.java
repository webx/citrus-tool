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
package com.alibaba.antx.config.entry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.ConfigResource;
import com.alibaba.antx.config.ConfigSettings;
import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.descriptor.ConfigGenerate;
import com.alibaba.antx.config.generator.ConfigGeneratorCallback;
import com.alibaba.antx.util.scanner.ScannerException;
import com.alibaba.antx.util.scanner.ZipScanner;

/**
 * 代表一个Jar文件类型的配置项信息。
 * 
 * @author Michael Zhou
 */
public class ZipConfigEntry extends ConfigEntry {
    /**
     * 创建一个结点。
     * 
     * @param resource 指定结点的资源
     * @param settings antxconfig的设置
     */
    public ZipConfigEntry(ConfigResource resource, File outputFile, ConfigSettings settings) {
        super(resource, outputFile, settings);
    }

    /**
     * 扫描结点。
     * 
     * @param istream zip文件的输入流
     */
    protected void scan(InputStream istream) {
        Handler handler = new Handler();
        ZipScanner scanner = new ZipScanner(getConfigEntryResource().getURL(), handler);

        scanner.setInputStream(istream);

        try {
            scanner.scan();
        } catch (ScannerException e) {
            throw new ConfigException(e);
        }

        subEntries = handler.getSubEntries();

        getGenerator().init();
    }

    /**
     * 生成配置文件。
     */
    protected boolean generate(InputStream istream, OutputStream ostream) {
        boolean needCloseOutputStream = false;
        boolean needCloseInputStream = false;
        boolean success = false;
        File destfile = null;
        File outputFile = getOutputFile();

        getConfigSettings().debug("Processing files in " + getConfigEntryResource());

        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        Set dirs = new HashSet();

        boolean allSuccess = true;

        try {
            // 检查或打开ostream
            if (ostream == null) {
                if (outputFile == null) {
                    destfile = getConfigEntryResource().getFile();

                    if ((destfile == null) || !destfile.exists()) {
                        throw new ConfigException("Could not find " + getConfigEntryResource().getURL());
                    }

                    outputFile = new File(destfile.getParentFile(), destfile.getName() + ".tmp");
                }

                outputFile.getParentFile().mkdirs();

                ostream = new BufferedOutputStream(new FileOutputStream(outputFile), 8192);
                needCloseOutputStream = true;
            }

            // 检查或打开istream
            if (istream == null) {
                istream = getConfigEntryResource().getURL().openStream();

                if (!(istream instanceof BufferedInputStream)) {
                    istream = new BufferedInputStream(istream, 8192);
                }

                needCloseInputStream = true;
            }

            zis = new ZipInputStream(istream);
            zos = new ZipOutputStream(ostream);

            ZipEntry zipEntry;

            getGenerator().startSession(getConfigSettings().getPropertiesSet());

            while ((zipEntry = zis.getNextEntry()) != null) {
                allSuccess &= processZipEntry(zipEntry, zis, zos, dirs);
            }

            allSuccess &= getGenerator().getSession().generateLazyItems(new ZipCallback(zos, dirs));

            getGenerator().getSession().checkNonprocessedTemplates();
            getGenerator().getSession().generateLog(new ZipCallback(zos, dirs));

            success = true;
        } catch (IOException e) {
            throw new ConfigException(e);
        } finally {
            getGenerator().closeSession();

            // 结束zip文件，但不关闭流
            if (zos != null) {
                try {
                    zos.finish();
                } catch (IOException e) {
                }
            }

            // 仅当输入流是由当前entry亲自打开的，才关闭流
            if (needCloseInputStream && (istream != null)) {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }

            // 仅当输出流是由当前entry亲自打开的，才关闭流
            if (needCloseOutputStream && (ostream != null)) {
                try {
                    ostream.flush();
                    ostream.close();
                } catch (IOException e) {
                }

                // 如果成功，则将临时文件改成正式文件，否则删除临时文件
                if (success) {
                    // 仅当没有指定outputFile时才做下面的事 
                    if (getOutputFile() == null) {
                        // 在windows下，观察到rename失败，因为lock的原因。过一会重试。
                        int retryTimes = 10;
                        boolean succ = false;
                        String message = String.format("Moving file %s to %s failed.", outputFile.getName(),
                                destfile.getName());

                        for (int i = 0; i < retryTimes; i++) {
                            destfile.delete();
                            succ = outputFile.renameTo(destfile);

                            if (succ) {
                                break;
                            }

                            getConfigSettings().warn(
                                    String.format(message + "  Wait 0.5s and try again...%d of %d", i + 1, retryTimes));

                            try {
                                System.gc();
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                            }
                        }

                        if (!succ) {
                            throw new ConfigException(message);
                        }
                    }
                } else {
                    outputFile.delete();
                }
            }
        }

        return allSuccess;
    }

    private boolean processZipEntry(ZipEntry zipEntry, ZipInputStream zis, ZipOutputStream zos, Set dirs)
            throws IOException {
        String name = zipEntry.getName();
        ConfigEntry subEntry = getSubEntry(name);

        if (subEntry != null) {
            // 这是一个嵌套的jar entry
            ZipEntry zipEntryToWrite = new ZipEntry(zipEntry.getName());

            zos.putNextEntry(zipEntryToWrite);

            return subEntry.generate(zis, zos);
        } else if (getGenerator().isTemplateFile(name)) {
            // 先把流倒到内存里，因为要用多次
            byte[] bytes = streamToBytes(zis);

            if (getGenerator().isDestFile(name)) {
                // 对于目标文件，假设为WEB-INF/web.xml，先保存其内容。
                // 在最后，检查如果没有META-INF/autoconf/WEB-INF/web.xml存在，则将其视为模板并生成之。
                getGenerator().getSession().addLazyGenerateItem(name, bytes);
                return true;
            } else {
                // 假设当前文件为META-INF/autoconf/**，那么复制并生成目标文件。
                copyFile(zipEntry, new ByteArrayInputStream(bytes), zos);
                return getGenerator().getSession().generate(name, new ZipCallback(bytes, zos, dirs));
            }
        } else if (getGenerator().isDestFile(name)) {
            // 这个文件将被模板生成的文件覆盖，故忽略之
        } else if (getGenerator().isDescriptorLogFile(name)) {
            // 这个文件将被descriptor日志文件覆盖，故忽略之
        } else if (zipEntry.isDirectory()) {
            // 这是一个的目录，在不重复创建目录的前提下，复制即可
            mkdirs(zipEntry.getName(), zos, dirs);
        } else {
            // 这是一个普通文件，复制即可
            copyFile(zipEntry, zis, zos);
        }

        return true;
    }

    private byte[] streamToBytes(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        io(zis, baos);
        return baos.toByteArray();
    }

    private void mkdirs(String dir, ZipOutputStream zos, Set dirs) throws IOException {
        dir = dir.replace('\\', '/');

        while (dir.startsWith("/")) {
            dir = dir.substring(1);
        }

        if (!dir.endsWith("/")) {
            dir += "/";
        }

        for (int index = dir.indexOf("/"); index > 0; index = dir.indexOf("/", index + 1)) {
            String subDir = dir.substring(0, index + 1);

            if (!dirs.contains(subDir)) {
                zos.putNextEntry(new ZipEntry(subDir));
                dirs.add(subDir);
            }
        }
    }

    private void copyFile(ZipEntry zipEntry, InputStream istream, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(zipEntry));
        io(istream, zos);
    }

    private void io(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int amount;

        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    private ConfigEntry getSubEntry(String name) {
        ConfigEntry[] entries = getSubEntries();

        for (int i = 0; i < entries.length; i++) {
            ConfigEntry subEntry = entries[i];

            if (subEntry.getName().equals(name.replace('\\', '/'))) {
                return subEntry;
            }
        }

        return null;
    }

    /**
     * 转换成字符串。
     * 
     * @return 字符串表示
     */
    public String toString() {
        return "ZipConfigEntry[" + getConfigEntryResource() + "]";
    }

    /**
     * 用来生成目标文件的callback。
     */
    private final class ZipCallback implements ConfigGeneratorCallback {
        private final byte[] bytes;
        private final ZipOutputStream zos;
        private final Set dirs;

        private ZipCallback(ZipOutputStream zos, Set dirs) {
            this(null, zos, dirs);
        }

        private ZipCallback(byte[] bytes, ZipOutputStream zos, Set dirs) {
            this.bytes = bytes;
            this.zos = zos;
            this.dirs = dirs;
        }

        public String nextEntry(String template, ConfigGenerate generate) {
            nextEntry(generate.getConfigDescriptor(), new ByteArrayInputStream(bytes), generate.getDestfile());
            return template;
        }

        public void nextEntry(ConfigDescriptor descriptor, InputStream is, String dest) {
            try {
                makeParentDirs(dest);
                zos.putNextEntry(new ZipEntry(dest));
            } catch (IOException e) {
                throw new ConfigException(e);
            }

            getGenerator().getSession().setInputStream(is);
            getGenerator().getSession().setOutputStream(zos);
        }

        public void logEntry(ConfigDescriptor descriptor, String logfileName) {
            try {
                makeParentDirs(logfileName);
                zos.putNextEntry(new ZipEntry(logfileName));
            } catch (IOException e) {
                throw new ConfigException(e);
            }

            getGenerator().getSession().setOutputStream(zos);
        }

        public void closeEntry() {
            // 不需要关闭流，因为是zip stream。
        }

        private void makeParentDirs(String name) throws IOException {
            int index = name.lastIndexOf("/");

            if (index > 0) {
                mkdirs(name.substring(0, index + 1), zos, dirs);
            }
        }
    }
}
