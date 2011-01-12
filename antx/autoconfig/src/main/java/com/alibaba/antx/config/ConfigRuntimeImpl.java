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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.entry.ConfigEntry;
import com.alibaba.antx.config.entry.ConfigEntryFactory;
import com.alibaba.antx.config.entry.ConfigEntryFactoryImpl;
import com.alibaba.antx.config.props.PropertiesResource;
import com.alibaba.antx.config.props.PropertiesSet;
import com.alibaba.antx.config.wizard.text.ConfigWizardLoader;
import com.alibaba.antx.util.CharsetUtil;
import com.alibaba.antx.util.PatternSet;
import com.alibaba.antx.util.StringUtil;

public class ConfigRuntimeImpl implements ConfigRuntime {
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter err;
    private String charset;
    private String mode;
    private String interactiveMode;
    private String type;
    private PatternSet descriptorPatterns;
    private PatternSet packagePatterns;
    private String[] dests;
    private String[] outputs;
    private File[] destFiles;
    private File[] outputFiles;
    private PropertiesSet props;
    private boolean verbose;
    private File tempdir;
    private ConfigEntryFactory configEntryFactory = new ConfigEntryFactoryImpl(this);

    public ConfigRuntimeImpl() {
        this(System.in, System.out, System.err, null);
    }

    public ConfigRuntimeImpl(InputStream inputStream, OutputStream outStream, OutputStream errStream, String charset) {
        this.interactiveMode = ConfigConstant.INTERACTIVE_AUTO;

        boolean charsetSpecified = !StringUtil.isEmpty(charset);
        this.charset = charsetSpecified ? charset : CharsetUtil.detectedSystemCharset();

        try {
            in = new BufferedReader(new InputStreamReader(inputStream, this.charset));
            out = new PrintWriter(new OutputStreamWriter(outStream, this.charset), true);
            err = new PrintWriter(new OutputStreamWriter(errStream, this.charset), true);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigException(e); // 不应发生
        }

        if (!charsetSpecified) {
            out.println("Detected system charset encoding: " + this.charset);
            out.println("If your can't read the following text, specify correct one like this: ");
            out.println("  autoconfig -c mycharset");
            out.println();
        }
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public PrintWriter getErr() {
        return err;
    }

    public String getCharset() {
        return charset;
    }

    public PatternSet getDescriptorPatterns() {
        return descriptorPatterns;
    }

    public PatternSet getPackagePatterns() {
        return packagePatterns;
    }

    public String getInteractiveMode() {
        return interactiveMode;
    }

    public String getMode() {
        return mode;
    }

    public File[] getDestFiles() {
        return destFiles;
    }

    public File[] getOutputFiles() {
        return outputFiles;
    }

    public PropertiesSet getPropertiesSet() {
        if (props == null) {
            props = new PropertiesSet(getIn(), getOut());
        }

        return props;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ConfigEntryFactory getConfigEntryFactory() {
        return configEntryFactory;
    }

    public void setDescriptorPatterns(String includes, String excludes) {
        this.descriptorPatterns = new PatternSet(includes, excludes);
    }

    public void setDescriptorPatterns(String[] includes, String[] excludes) {
        this.descriptorPatterns = new PatternSet(includes, excludes);
    }

    public void setPackagePatterns(String includes, String excludes) {
        this.packagePatterns = new PatternSet(includes, excludes);
    }

    public void setPackagePatterns(String[] includes, String[] excludes) {
        this.packagePatterns = new PatternSet(includes, excludes);
    }

    public void setInteractiveMode(String mode) {
        if (ConfigConstant.INTERACTIVE_AUTO.equals(mode) || ConfigConstant.INTERACTIVE_ON.equals(mode)
                || ConfigConstant.INTERACTIVE_OFF.equals(mode)) {
            this.interactiveMode = mode;
        }
    }

    public void setGuiMode() {
        mode = ConfigConstant.MODE_GUI;
    }

    public void setTextMode() {
        mode = ConfigConstant.MODE_TEXT;
    }

    public void setDests(String[] dests) {
        this.dests = dests;
    }

    public void setDestFiles(File[] destFiles) {
        this.destFiles = (File[]) destFiles.clone();
    }

    public void setOutputs(String[] outputs) {
        this.outputs = outputs;
    }

    public void setOutputFiles(File[] outputFiles) {
        this.outputFiles = (File[]) outputFiles.clone();
    }

    public void setUserPropertiesFile(String userPropertiesFile, String charset) {
        PropertiesSet props = getPropertiesSet();

        props.setUserPropertiesFile(userPropertiesFile);
        props.getUserPropertiesFile().setCharset(charset);
    }

    public void setSharedPropertiesFiles(String[] sharedPropertiesFiles, String name, String charset) {
        getPropertiesSet().setSharedPropertiesFiles(sharedPropertiesFiles);
        getPropertiesSet().setSharedPropertiesFilesName(name);

        PropertiesResource[] resources = getPropertiesSet().getSharedPropertiesFiles();

        for (int i = 0; i < resources.length; i++) {
            resources[i].setCharset(charset);
        }

        if (!StringUtil.isEmpty(name) || (sharedPropertiesFiles != null && sharedPropertiesFiles.length > 0)) {
            this.interactiveMode = ConfigConstant.INTERACTIVE_ON;
        }
    }

    public void setVerbose() {
        this.verbose = true;
    }

    private void init() {
        // tempdir
        if (tempdir == null) {
            tempdir = new File("");
        }

        tempdir = tempdir.getAbsoluteFile();

        // dests
        if (dests != null && dests.length > 0) {
            destFiles = new File[dests.length];

            for (int i = 0; i < dests.length; i++) {
                destFiles[i] = new File(dests[i]).getAbsoluteFile();
            }
        } else {
            destFiles = new File[0];
        }

        // outputs
        if (outputs != null && outputs.length > 0) {
            outputFiles = new File[outputs.length];

            for (int i = 0; i < outputs.length; i++) {
                if (outputs[i] != null) {
                    outputFiles[i] = new File(outputs[i]).getAbsoluteFile();
                }
            }
        } else {
            outputFiles = new File[destFiles.length];
        }

        if (outputFiles.length != destFiles.length) {
            throw new IllegalArgumentException("Mismatched output files and dest files");
        }

        // user properties file
        getPropertiesSet().init();

        info("User-defined properties: " + getPropertiesSet().getUserPropertiesFile().getURI() + "\n");
    }

    public void debug(String message) {
        if (verbose) {
            getOut().println(message);
        }
    }

    public void info(String message) {
        getOut().println(message);
    }

    public void warn(String message) {
        getOut().println(message);
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(Throwable cause) {
        error(null, cause);
    }

    public void error(String message, Throwable cause) {
        if (StringUtil.isBlank(message) && (cause != null)) {
            message = "ERROR: " + cause.getMessage();
        }

        getErr().println(message);

        if (verbose) {
            cause.printStackTrace(getErr());
            getErr().println();
        }
    }

    public boolean start() {
        return start(null);
    }

    public boolean start(ConfigDescriptor inlineDescriptor) {
        init();

        if (inlineDescriptor == null && ConfigConstant.MODE_GUI.equals(mode)) {
            // MainWindow.run(this);
            throw new UnsupportedOperationException("GUI mode currently unsupported");
        } else if (inlineDescriptor == null) {
            // 扫描所有文件或目录，取得要配置的entries
            List entries = scan(false);

            if (entries.isEmpty() && !ConfigConstant.INTERACTIVE_ON.equals(interactiveMode)) {
                info("Nothing to configure");
                return true;
            }

            ConfigWizardLoader wizard = new ConfigWizardLoader(this, entries);

            // 交互式编辑props文件
            wizard.loadAndStart();

            // 生成配置文件
            boolean allSuccess = true;

            for (Iterator i = entries.iterator(); i.hasNext();) {
                ConfigEntry entry = (ConfigEntry) i.next();

                allSuccess &= entry.generate();
            }

            return allSuccess;
        } else {
            ConfigWizardLoader wizard = new ConfigWizardLoader(this, inlineDescriptor);

            // 交互式编辑props文件
            wizard.loadAndStart();

            return true;
        }
    }

    public List scan(boolean includeEmptyEntries) {
        List entries = new ArrayList(destFiles.length);

        for (int i = 0; i < destFiles.length; i++) {
            File destFile = destFiles[i];
            File outputFile = outputFiles[i];

            ConfigEntry entry = getConfigEntryFactory().create(new ConfigResource(destFile), outputFile, type);

            entry.scan();

            if (includeEmptyEntries || !entry.isEmpty()) {
                entries.add(entry);
            }
        }

        return entries;
    }
}
