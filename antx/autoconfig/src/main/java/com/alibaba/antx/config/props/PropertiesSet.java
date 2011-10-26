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

package com.alibaba.antx.config.props;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.antx.config.ConfigException;
import com.alibaba.antx.config.generator.PropertiesLoader;
import com.alibaba.antx.config.generator.expr.Expression;
import com.alibaba.antx.config.resource.ResourceManager;
import com.alibaba.antx.config.resource.ResourceURI;
import com.alibaba.antx.util.StringUtil;

/**
 * 代表一组props文件的组合。
 * 
 * @author Michael Zhou
 */
public class PropertiesSet {
    private static final Logger log = LoggerFactory.getLogger(PropertiesSet.class);
    private final ResourceManager manager;
    private final SystemProperties systemProps;
    private boolean inited;
    private PropertiesResource[] sharedPropertiesFiles;
    private PropertiesFile[] sharedPropertiesFilesExpanded;
    private PropertiesFile userPropertiesFile;
    private Map namedPropertiesFiles; // Map: name => List of shared properties file names
    private String sharedName;
    private Map mergedProps;
    private Set mergedKeys;

    public PropertiesSet() {
        this(null, null);
    }

    public PropertiesSet(BufferedReader in, PrintWriter out) {
        this.manager = new ResourceManager();
        this.systemProps = new SystemProperties();

        manager.setIn(in);
        manager.setOut(out);
    }

    public SystemProperties getSystemProperties() {
        return systemProps;
    }

    public PropertiesFile getUserPropertiesFile() {
        return userPropertiesFile;
    }

    public void setUserPropertiesFile(String userPropertiesFile) {
        this.userPropertiesFile = new PropertiesFile(manager, userPropertiesFile);
    }

    public PropertiesResource[] getSharedPropertiesFiles() {
        return sharedPropertiesFiles;
    }

    public void setSharedPropertiesFiles(String[] sharedPropertiesFiles) {
        if (sharedPropertiesFiles == null) {
            this.sharedPropertiesFiles = new PropertiesFile[0];
            return;
        }

        PropertiesResource[] files = new PropertiesResource[sharedPropertiesFiles.length];

        for (int i = 0; i < sharedPropertiesFiles.length; i++) {
            String sharedPropertiesFile = sharedPropertiesFiles[i];

            // 试着组装file，如果失败，则试着URI
            URI uri = ResourceURI.guessURI(sharedPropertiesFile);

            if (new ResourceURI(uri).guessDirectory()) {
                files[i] = new PropertiesFileSet(manager, uri);
            } else {
                files[i] = new PropertiesFile(manager, uri);
            }
        }

        this.sharedPropertiesFiles = files;
    }

    public String getSharedPropertiesFilesName() {
        return sharedName;
    }

    public void setSharedPropertiesFilesName(String sharedName) {
        this.sharedName = StringUtil.isEmpty(sharedName) ? null : sharedName;
    }

    public Map getMergedProperties() {
        init();
        return mergedProps;
    }

    public Set getMergedKeys() {
        init();
        return mergedKeys;
    }

    public PropertiesFile[] getSharedPropertiesFilesExpanded() {
        return sharedPropertiesFilesExpanded;
    }

    public void init() {
        if (inited) {
            return;
        }

        inited = true;

        // user props
        if (userPropertiesFile == null) {
            // 查找antx.properties
            File defaultPropertiesFile;

            // 查找当前目录
            defaultPropertiesFile = new File("antx.properties").getAbsoluteFile();

            if (!defaultPropertiesFile.exists() || !defaultPropertiesFile.isFile()) {
                defaultPropertiesFile = new File(System.getProperty("user.home"), "antx.properties");
            }

            userPropertiesFile = new PropertiesFile(manager, defaultPropertiesFile.getAbsoluteFile());
        }

        userPropertiesFile.setAllowNonExistence(true);

        // 从system props和user props中取得：
        // antx.properties.name1.1
        // antx.properties.name1.2
        // antx.properties.name1.3
        // antx.properties.name1.4
        // antx.properties.name2.1
        // antx.properties.name2.2
        // antx.properties.name2.3
        // antx.properties.name2.4
        Map props = new HashMap();

        PropertiesLoader.mergeProperties(props, systemProps.getProperties());
        PropertiesLoader.mergeProperties(props, userPropertiesFile.getProperties());

        namedPropertiesFiles = getNamedSharedPropertiesFiles(props);

        // 取得antx.properties参数，如果没有，默认值为"default",
        if (sharedName == null) {
            if (sharedPropertiesFiles != null && sharedPropertiesFiles.length > 0) {
                sharedName = "default";
            } else {
                Object value = props.get("antx.properties");

                if (value instanceof Expression) {
                    value = ((Expression) value).evaluate(null);
                }

                sharedName = (String) value;

                if (StringUtil.isEmpty(sharedName)) {
                    sharedName = "default";
                }
            }
        }

        // 如果在命令行上指定了sharedPropertiesFiles，则更新namedSharedPropertiesFiles
        // 否则设置sharedPropertiesFiles
        if (sharedPropertiesFiles != null && sharedPropertiesFiles.length > 0) {
            List fileList = new ArrayList(sharedPropertiesFiles.length);

            for (int i = 0; i < sharedPropertiesFiles.length; i++) {
                PropertiesResource resource = sharedPropertiesFiles[i];

                fileList.add(resource.getURI().toString());
            }

            namedPropertiesFiles.put(sharedName, fileList);
        } else {
            List fileList = (List) namedPropertiesFiles.get(sharedName);
            String[] files = null;

            if (fileList != null) {
                files = (String[]) fileList.toArray(new String[fileList.size()]);
            }

            setSharedPropertiesFiles(files);
        }

        // 装载并合并所有shared properties
        loadUserProperties(false);
    }

    private final static Pattern ANTX_PROPERTIES_PATTERN;

    static {
        try {
            ANTX_PROPERTIES_PATTERN = new Perl5Compiler().compile("antx\\.properties\\.(\\w+)(\\.(\\d+))?",
                    Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedPatternException e) {
            throw new ConfigException(e);
        }
    }

    /**
     * 判断property name是否覆盖shared properties中的值。
     */
    public boolean isShared(String name) {
        for (int i = sharedPropertiesFilesExpanded.length - 1; i >= 0; i--) {
            PropertiesFile sharedFile = sharedPropertiesFilesExpanded[i];

            if (sharedFile.getProperties().containsKey(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 将mergedProperties中的值，除去在sharedProperperties中和systemProperties中的值。
     */
    public Map getModifiedProperties() {
        Map modifiedProperties = new HashMap();

        for (Iterator i = getMergedKeys().iterator(); i.hasNext();) {
            String key = (String) i.next();
            String value = toString(getMergedProperties().get(key));
            PatternMatcher matcher = new Perl5Matcher();

            if (value == null) {
                continue;
            }

            // 跳过antx.properties.*，因为有特殊意义
            if (matcher.matches(key, ANTX_PROPERTIES_PATTERN) || "antx.properties".equals(key)) {
                continue;
            }

            String defaultValue = null;

            // 在shared properties中找key
            PropertiesFile[] files = getSharedPropertiesFilesExpanded();

            for (int j = files.length - 1; j >= 0; j--) {
                if (files[j].getProperties().containsKey(key)) {
                    defaultValue = toString(files[j].getProperties().get(key));
                    break;
                }
            }

            // 在system properties中找key
            if (defaultValue == null && getSystemProperties().getProperties().containsKey(key)) {
                defaultValue = toString(getSystemProperties().getProperties().get(key));
            }

            // 如果没找到，或者值不同，则加入modified properties
            if (defaultValue == null || !defaultValue.equals(value)) {
                modifiedProperties.put(key, value);
            }
        }

        // 插入antx.properties.*
        for (Iterator i = namedPropertiesFiles.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            List fileList = (List) entry.getValue();
            int index = 1;

            for (Iterator j = fileList.iterator(); j.hasNext(); index++) {
                String file = (String) j.next();
                String key = "antx.properties." + name;

                if (index > 1 || j.hasNext()) {
                    key += "." + index;
                }

                modifiedProperties.put(key, file);
            }
        }

        if (!"default".equals(sharedName)) {
            modifiedProperties.put("antx.properties", sharedName);
        }

        return modifiedProperties;
    }

    private String toString(Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }

        if (value instanceof Expression) {
            return ((Expression) value).getExpressionText();
        }

        return String.valueOf(value);
    }

    public void reloadUserProperties() {
        loadUserProperties(true);
    }

    private void loadUserProperties(boolean reload) {
        mergedProps = new HashMap();
        mergedKeys = new TreeSet();

        // system properties
        mergedKeys.addAll(getSystemProperties().getKeys());
        PropertiesLoader.mergeProperties(mergedProps, getSystemProperties().getProperties());

        // shared properties
        List expandedFiles = new LinkedList();

        for (int i = 0; i < getSharedPropertiesFiles().length; i++) {
            loadResource(getSharedPropertiesFiles()[i], mergedProps, mergedKeys, expandedFiles);
        }

        sharedPropertiesFilesExpanded = (PropertiesFile[]) expandedFiles.toArray(new PropertiesFile[expandedFiles
                .size()]);

        // user properties
        if (reload) {
            getUserPropertiesFile().reload();
        }

        mergedKeys.addAll(getUserPropertiesFile().getKeys());
        PropertiesLoader.mergeProperties(mergedProps, getUserPropertiesFile().getProperties());

        checkOverlap(reload);
    }

    /**
     * 检查shared properties中的被覆盖的值。
     */
    private void checkOverlap(boolean reload) {
        for (Iterator i = getMergedKeys().iterator(); i.hasNext();) {
            String key = (String) i.next();
            PatternMatcher matcher = new Perl5Matcher();

            // 跳过antx.properties.*，因为有特殊意义
            if (matcher.matches(key, ANTX_PROPERTIES_PATTERN) || "antx.properties".equals(key)) {
                continue;
            }

            // 在shared properties中找key
            PropertiesFile[] files = getSharedPropertiesFilesExpanded();
            List definedInFiles = new ArrayList(files.length + 1);

            if (userPropertiesFile.getProperties().containsKey(key)) {
                definedInFiles.add(userPropertiesFile);
            }

            for (int j = files.length - 1; j >= 0; j--) {
                if (files[j].getProperties().containsKey(key)) {
                    definedInFiles.add(files[j]);
                }
            }

            // 判断重复
            if (definedInFiles.size() > 1) {
                StringBuffer message = new StringBuffer();
                boolean overlapByUserProperties = definedInFiles.get(0) == userPropertiesFile;

                if (!reload || overlapByUserProperties) {
                    message.append("覆盖警告： ");

                    if (overlapByUserProperties) {
                        message.append("用户properties文件中的值“").append(key).append("”覆盖了").append("共享properties文件中的值：\n");
                    } else {
                        message.append("“").append(key).append("”出现在").append(definedInFiles.size()).append(
                                "个共享properties文件中（最终值将以第一个为准）：\n");
                    }

                    for (Iterator k = definedInFiles.iterator(); k.hasNext();) {
                        PropertiesFile f = (PropertiesFile) k.next();

                        message.append("  - ").append(f.getURI());
                        message.append(", value = ").append(toString(f.getProperties().get(key))).append("\n");
                    }

                    log.warn(message.toString());
                }
            }
        }
    }

    // Map: name => List of shared properties file names
    private Map getNamedSharedPropertiesFiles(Map props) {
        Map names = new TreeMap();

        for (Iterator i = props.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            PatternMatcher matcher = new Perl5Matcher();

            if (matcher.matches(key, ANTX_PROPERTIES_PATTERN)) {
                MatchResult result = matcher.getMatch();
                String name = result.group(1);
                int index;

                try {
                    index = Integer.parseInt(result.group(3));
                } catch (NumberFormatException e) {
                    index = 0;
                }

                if (!names.containsKey(name)) {
                    names.put(name, new TreeMap());
                }

                Object value = props.get(key);

                if (value instanceof Expression) {
                    value = ((Expression) value).evaluate(null);
                }

                ((Map) names.get(name)).put(new Integer(index), value);
            }
        }

        for (Iterator i = names.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            List files = new ArrayList(((Map) entry.getValue()).values());

            entry.setValue(files);
        }

        return names;
    }

    private void loadResource(PropertiesResource resource, Map mergedProperties, Set mergedKeys, List expandedFiles) {
        if (resource instanceof PropertiesFile) {
            PropertiesFile file = (PropertiesFile) resource;

            expandedFiles.add(file);
            mergedKeys.addAll(file.getKeys());

            PropertiesLoader.mergeProperties(mergedProperties, file.getProperties());
        } else if (resource instanceof PropertiesFileSet) {
            PropertiesFileSet files = (PropertiesFileSet) resource;

            for (Iterator i = files.getPropertiesFiles().iterator(); i.hasNext();) {
                PropertiesResource pr = (PropertiesResource) i.next();

                loadResource(pr, mergedProperties, mergedKeys, expandedFiles);
            }
        } else {
            throw new IllegalArgumentException("unknown resource type: " + resource.getClass().getName());
        }
    }
}
