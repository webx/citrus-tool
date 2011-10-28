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

package com.alibaba.antx.config.wizard.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.alibaba.antx.config.descriptor.ConfigDescriptor;
import com.alibaba.antx.config.descriptor.ConfigGroup;
import com.alibaba.antx.config.descriptor.ConfigProperty;
import com.alibaba.antx.config.descriptor.ConfigValidator;
import com.alibaba.antx.config.generator.expr.CompositeExpression;
import com.alibaba.antx.config.generator.expr.Expression;
import com.alibaba.antx.config.generator.expr.ExpressionContext;
import com.alibaba.antx.config.props.PropertiesSet;
import com.alibaba.antx.util.ObjectUtil;
import com.alibaba.antx.util.StringUtil;
import com.alibaba.antx.util.i18n.LocaleInfo;

/**
 * 基于文本的交互地配置属性文件的工具类。
 * 
 * @author Michael Zhou
 */
public class ConfigWizard {
    private static final int PREVIOUS = -1;
    private static final int NEXT = -2;
    private static final int QUIT = -3;
    private static final int MAX_ALIGN = 40;

    // wizard参数
    private ConfigGroup[] groups;
    private PropertiesSet propSet;
    private String confirmMessage;
    private BufferedReader in;
    private PrintWriter out;
    private PrintWriter fileWriter;

    // Wizard状态变量
    private int step;
    private ConfigGroup group;
    private ConfigProperty[] props;
    private ConfigProperty validatorProperty;
    private String validatorMessage;
    private int validatorIndex;

    /**
     * 创建一个wizard。
     * 
     * @param descriptors 所有描述文件
     * @param props 属性文件
     */
    public ConfigWizard(ConfigDescriptor[] descriptors, PropertiesSet propSet, String charset) {
        this.propSet = propSet;

        // 初始化输入输出流
        try {
            in = new BufferedReader(new InputStreamReader(System.in, charset));
            out = new PrintWriter(new OutputStreamWriter(System.out, charset), true);
        } catch (UnsupportedEncodingException e) {
            throw new ConfigWizardException(e);
        }

        // 取得descriptors中的所有groups
        List groups = new ArrayList();

        for (int i = 0; i < descriptors.length; i++) {
            ConfigGroup[] descriptorGroups = descriptors[i].getGroups();

            for (int j = 0; j < descriptorGroups.length; j++) {
                groups.add(descriptorGroups[j]);
            }
        }

        this.groups = (ConfigGroup[]) groups.toArray(new ConfigGroup[groups.size()]);

        // 设置初始step
        setStep(0);
    }

    /**
     * 设置确认信息。
     * 
     * @param message 确认信息
     */
    public void setConfirmMessage(String confirmMessage) {
        this.confirmMessage = confirmMessage;
    }

    /**
     * 验证属性文件是否满足所有descriptor的需要。
     * 
     * @return 如果满足要求，则返回true
     */
    public boolean validate() {
        for (int i = 0; i < groups.length; i++) {
            setStep(i);

            for (int j = 0; j < props.length; j++) {
                ConfigProperty prop = props[j];

                String value = evaluatePropertyValue(prop, false);

                for (Iterator k = prop.getValidators().iterator(); k.hasNext();) {
                    ConfigValidator validator = (ConfigValidator) k.next();

                    if (!validator.validate(value)) {
                        validatorIndex = j;
                        validatorProperty = prop;
                        validatorMessage = validator.getMessage();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private String getURI() {
        return propSet.getUserPropertiesFile().getURI().toString();
    }

    private Set getKeys() {
        return propSet.getMergedKeys();
    }

    private Map getValues() {
        return propSet.getMergedProperties();
    }

    /**
     * 填充默认值。
     */
    private void fillDefaultValues() {
        int savedStep = step;

        for (int i = 0; i < groups.length; i++) {
            setStep(i);

            for (int j = 0; j < props.length; j++) {
                ConfigProperty prop = props[j];

                // 除非key存在且value不为空，否则设置默认值
                if ((getValues().get(prop.getName()) == null) || !getKeys().contains(prop.getName())) {
                    String value = getPropertyValue(prop, true);

                    setProperty(prop.getName(), (value == null) ? "" : value);
                }
            }
        }

        setStep(savedStep);
    }

    /**
     * 执行wizard。
     */
    public void start() {
        boolean continueWizard = true;

        // 如果没有什么可做的，直接返回
        if (group == null) {
            confirmAndSave();
            return;
        }

        // 提示用户, 是否继续
        if (confirmMessage != null) {
            print(confirmMessage + " [Yes][No] ");

            try {
                String input = in.readLine();

                input = (input == null) ? "" : input.trim().toLowerCase();

                if (input.equals("n") || input.equals("no")) {
                    continueWizard = false;
                }
            } catch (IOException e) {
                throw new ConfigWizardException(e);
            }
        }

        println();

        // 装入默认值，以简化用户的操作
        if (continueWizard) {
            fillDefaultValues();
        }

        // 开始wizard
        while (continueWizard) {
            // 打印标题
            printTitle();

            // 打印group内容
            printGroup();

            // 提示输入
            int toStep = processMenu();

            switch (toStep) {
                case PREVIOUS:
                    setStep(step - 1);
                    break;

                case NEXT:
                    setStep(step + 1);
                    break;

                case QUIT:
                    continueWizard = confirmAndSave();
                    break;

                default:
                    processInput(toStep);
            }
        }
    }

    private boolean confirmAndSave() {
        boolean continueWizard = true;

        if (confirmSave()) {
            if (validateSave()) {
                save();
                continueWizard = false;
            }
        } else {
            // 用户选择了“退出不保存”，所以要恢复原始的数据
            propSet.reloadUserProperties();
            continueWizard = false;
        }

        return continueWizard;
    }

    private void print(Object message) {
        String messageString = (message == null) ? "" : message.toString();

        out.print(messageString);
        out.flush();

        if (fileWriter != null) {
            fileWriter.print(messageString);
            fileWriter.flush();
        }
    }

    private void println(Object message) {
        String messageString = (message == null) ? "" : message.toString();

        out.println(((fileWriter == null) ? "" : "│") + messageString);

        if (fileWriter != null) {
            fileWriter.println(messageString);
        }
    }

    private void println() {
        println(null);
    }

    private void printTitle() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("╭──────┬─ Step ").append(step + 1);
        buffer.append(" of ").append(groups.length).append(" ────────┈┈┈┈\n");

        buffer.append("│            │\n");

        if (group.getConfigDescriptor().getDescription() != null) {
            buffer.append(
                    formatLines(group.getConfigDescriptor().getDescription(), 60, LocaleInfo.getDefault().getLocale(),
                            "│Description │ ", "│            │   ")).append("\n");

            buffer.append("│┈┈┈┈┈┈│┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈\n");
        }

        buffer.append(
                formatLines(group.getConfigDescriptor().getURL().toString(), 60, LocaleInfo.getDefault().getLocale(),
                        "│Descriptor  │ ", "│            │   ")).append("\n");

        buffer.append("│┈┈┈┈┈┈│┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈\n");

        buffer.append(
                formatLines(getURI(), 60, LocaleInfo.getDefault().getLocale(), "│Properties  │ ", "│            │   "))
                .append("\n");

        buffer.append("│            │").append("\n");
        buffer.append("└──────┴┈┈┈┈┈┈┈┈┈┈┈").append("\n");

        println();
        println(buffer);
    }

    private void printGroup() {
        if (group.getDescription() != null) {
            println(" " + group.getDescription() + " (? - 该值在用户配置文件中不存在，* - 必填项，S - 覆盖共享默认值，s - 共享值)");
        } else {
            println(" (? - 该值在用户配置文件中不存在，* - 必填项，S - 覆盖共享默认值，s - 共享值)");
        }

        println();

        // 找出最长的名称和值
        int maxLength = -1;
        int maxLengthValue = -1;

        for (int i = 0; i < props.length; i++) {
            int length = props[i].getName().length();

            if ((length > maxLength) && (length < MAX_ALIGN)) {
                maxLength = length;
            }
        }

        for (int i = 0; i < props.length; i++) {
            String value = getPropertyValue(props[i], true);
            int length = Math.max(props[i].getName().length(), maxLength)
                    + ((value == null) ? 0 : ("  = ".length() + value.length()));

            if ((length > maxLengthValue) && (length < (MAX_ALIGN * 2))) {
                maxLengthValue = length;
            }
        }

        for (int i = 0; i < props.length; i++) {
            ConfigProperty prop = props[i];

            StringBuffer buffer = new StringBuffer();

            // 如果项目在配置文件中不存在，则显示?
            boolean absent = !getKeys().contains(prop.getName());

            // 如果是必填项, 则显示*号
            boolean required = prop.isRequired();

            // 如果项目定义在shared properties中，则显示S
            boolean shared = propSet.isShared(prop.getName());

            if (shared) {
                if (absent) {
                    buffer.append("s");
                } else {
                    buffer.append("S");
                }
            } else {
                if (absent) {
                    buffer.append("?");
                } else {
                    buffer.append(" ");
                }
            }

            if (required) {
                buffer.append("* ");
            } else {
                buffer.append("  ");
            }

            // 显示property序号
            buffer.append(i + 1).append(" - ");

            // 显示property名称
            buffer.append(prop.getName());

            // 显示property值
            String value = getPropertyValue(prop, true);

            if (value != null) {
                for (int j = 0; j < (maxLength - prop.getName().length()); j++) {
                    buffer.append(' ');
                }

                buffer.append("  = ").append(value);
            }

            // 显示property描述
            if (prop.getDescription() != null) {
                int length = (value == null) ? prop.getName().length() : (Math.max(prop.getName().length(), maxLength)
                        + "  = ".length() + value.length());

                for (int j = 0; j < (maxLengthValue - length); j++) {
                    buffer.append(' ');
                }

                buffer.append("   # ").append(prop.getDescription());
            }

            // 如果值是表达式，则同时显示表达式的计算值
            String evaluatedValue = evaluatePropertyValue(prop, true);

            if ((evaluatedValue != null) && !ObjectUtil.equals(value, evaluatedValue)) {
                buffer.append("\n");

                for (int j = 0; j < maxLength; j++) {
                    buffer.append(' ');
                }

                buffer.append("          (").append(evaluatedValue).append(")");

                if (i < (props.length - 1)) {
                    buffer.append("\n");
                }
            }

            println(buffer);
        }

        println();
    }

    private int processMenu() {
        while (true) {
            // 提示输入
            StringBuffer buffer = new StringBuffer(" 请选择");

            if (props.length > 0) {
                buffer.append("[1-").append(props.length).append("]");
            }

            buffer.append("[Quit]");

            if (step > 0) {
                buffer.append("[Previous]");
            }

            if (step < (groups.length - 1)) {
                buffer.append("[Next]");
            }

            buffer.append(" ");

            print(buffer);

            // 等待输入
            String input = null;

            try {
                input = in.readLine();
            } catch (IOException e) {
                throw new ConfigWizardException(e);
            }

            input = (input == null) ? "" : input.trim().toLowerCase();

            if ((input.equals("n") || input.equals("next")) && (step < (groups.length - 1))) {
                return NEXT;
            }

            if ((input.equals("p") || input.equals("previous")) && (step > 0)) {
                return PREVIOUS;
            }

            if (input.equals("q") || input.equals("quit")) {
                return QUIT;
            }

            try {
                int inputValue = Integer.parseInt(input) - 1;

                if ((inputValue >= 0) && (inputValue < props.length)) {
                    return inputValue;
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    private void processInput(int index) {
        ConfigProperty prop = props[index];
        StringBuffer buffer = new StringBuffer(" 请输入");

        // 显示property描述
        if (prop.getDescription() != null) {
            buffer.append(prop.getDescription()).append(" ");
        }

        // 显示property名称
        buffer.append(prop.getName()).append(" = ");

        // 显示property值
        String value = getPropertyValue(prop, true);

        if (value != null) {
            buffer.append("[").append(value).append("] ");
        }

        print(buffer);

        // 等待输入
        String input = null;

        try {
            input = in.readLine();
        } catch (IOException e) {
            throw new ConfigWizardException(e);
        }

        input = (input == null) ? "" : input.trim();

        if ((input == null) || (input.length() == 0)) {
            input = value;
        }

        setProperty(prop.getName(), input);
    }

    private boolean confirmSave() {
        println();
        print(" 即将保存到文件\"" + getURI() + "\"中, 确定? [Yes][No] ");

        String input = null;

        try {
            input = in.readLine();
        } catch (IOException e) {
            throw new ConfigWizardException(e);
        }

        input = (input == null) ? "" : input.trim().toLowerCase();

        if (input.equals("n") || input.equals("no")) {
            return false;
        }

        return true;
    }

    private boolean validateSave() {
        if (!validate()) {
            println();
            println(" 字段" + validatorProperty.getName() + "不合法: " + validatorMessage);
            println();
            print(" 您仍然要保存吗? [Yes=强制保存/No=继续编辑] ");

            String input = null;

            try {
                input = in.readLine();
            } catch (IOException e) {
                throw new ConfigWizardException(e);
            }

            input = (input == null) ? "" : input.trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            }

            printTitle();
            printGroup();
            processInput(validatorIndex);

            return false;
        }

        return true;
    }

    private void save() {
        Map modifiedProperties = propSet.getModifiedProperties();

        println();
        println("╭───────────────────────┈┈┈┈");
        println("│ 保存文件 " + getURI() + "...");
        println("│┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈┈");

        try {
            OutputStream os = propSet.getUserPropertiesFile().getResource().getOutputStream();
            String charset = propSet.getUserPropertiesFile().getCharset();

            fileWriter = new PrintWriter(new OutputStreamWriter(os, charset), true);
        } catch (IOException e) {
            throw new ConfigWizardException(e);
        }

        try {
            List[] keyGroups = getSortedKeys(modifiedProperties, 2);

            for (int i = 0; i < keyGroups.length; i++) {
                List keys = keyGroups[i];

                // 找出最长的名称
                int maxLength = -1;

                for (Iterator j = keys.iterator(); j.hasNext();) {
                    String key = (String) j.next();
                    int length = key.length();

                    if ((length > maxLength) && (length < MAX_ALIGN)) {
                        maxLength = length;
                    }
                }

                // 输出property项
                for (Iterator j = keys.iterator(); j.hasNext();) {
                    String key = (String) j.next();
                    String value = (String) modifiedProperties.get(key);

                    if (value == null) {
                        value = "";
                    }

                    value = ((String) value).replaceAll("\\\\", "\\\\\\\\");

                    StringBuffer buffer = new StringBuffer();

                    buffer.append(key);

                    for (int k = 0; k < (maxLength - key.length()); k++) {
                        buffer.append(' ');
                    }

                    buffer.append("  = ").append(value);

                    println(buffer);
                }

                if (i < (keyGroups.length - 1)) {
                    println();
                }
            }
        } finally {
            fileWriter.close();
            fileWriter = null;
        }

        println("└───────┈┈┈┈┈┈┈┈┈┈┈");
        println(" 已保存至文件: " + getURI());

        propSet.reloadUserProperties();
    }

    /**
     * 对所有properties的key排序并分组。
     * 
     * @param level 分组的级别
     * @return 分组列表
     */
    private List[] getSortedKeys(Map props, int level) {
        List keys = new ArrayList(props.keySet());

        Collections.sort(keys);

        List groups = new ArrayList();
        List group = null;
        String prefix = null;

        for (Iterator i = keys.iterator(); i.hasNext();) {
            String key = (String) i.next();
            String[] parts = StringUtil.split(key, ".");
            StringBuffer buffer = new StringBuffer();

            for (int j = 0; (j < (parts.length - 1)) && (j < level); j++) {
                if (buffer.length() > 0) {
                    buffer.append('.');
                }

                buffer.append(parts[j]);
            }

            String keyPrefix = buffer.toString();

            if (!keyPrefix.equals(prefix)) {
                if (group != null) {
                    groups.add(group);
                }

                prefix = keyPrefix;
                group = new ArrayList();
            }

            group.add(key);
        }

        if ((group != null) && (group.size() > 0)) {
            groups.add(group);
        }

        return (List[]) groups.toArray(new List[groups.size()]);
    }

    private void setProperty(String name, String value) {
        Object expr = value;

        if (value != null) {
            expr = CompositeExpression.parse(value);
        }

        if (expr == null) {
            getValues().remove(name);
            getKeys().remove(name);
        } else {
            getValues().put(name, expr);
            getValues().put(StringUtil.getValidIdentifier(name), expr);
            getKeys().add(name);
        }
    }

    /**
     * 取得property的值，不计算表达式。
     * 
     * @param prop 属性
     * @param defaultValue 是否使用默认值
     */
    private String getPropertyValue(ConfigProperty prop, boolean defaultValue) {
        Object value = getValues().get(prop.getName());

        if (defaultValue && (value == null)) {
            value = prop.getDefaultValue();
        }

        if (value instanceof Expression) {
            value = ((Expression) value).getExpressionText();
        }

        if (value instanceof String) {
            String stringValue = (String) value;

            if (stringValue != null) {
                stringValue = stringValue.trim();
            }

            if ((stringValue == null) || (stringValue.length() == 0)) {
                stringValue = null;
            }

            return stringValue;
        }

        return (value == null) ? null : value.toString();
    }

    /**
     * 计算property的值。
     * 
     * @param prop 属性
     * @param defaultValue 是否使用默认值
     */
    private String evaluatePropertyValue(ConfigProperty prop, boolean defaultValue) {
        final String ref = prop.getName();
        Object value = getValues().get(ref);

        if (defaultValue && (value == null)) {
            value = prop.getDefaultValue();

            if (value instanceof String) {
                Expression expr = CompositeExpression.parse((String) value);

                if (expr != null) {
                    value = expr;
                }
            }
        }

        if (value instanceof Expression) {
            value = ((Expression) value).evaluate(new ExpressionContext() {
                public Object get(String key) {
                    // 避免无限递归
                    if (ref.equals(key)
                            || StringUtil.getValidIdentifier(ref).equals(StringUtil.getValidIdentifier(key))) {
                        return null;
                    } else {
                        return getValues().get(key);
                    }
                }

                public void put(String key, Object value) {
                    getValues().put(key, value);
                }
            });
        }

        if (value instanceof String) {
            String stringValue = (String) value;

            if (stringValue != null) {
                stringValue = stringValue.trim();
            }

            if ((stringValue == null) || (stringValue.length() == 0)) {
                stringValue = null;
            }

            return stringValue;
        }

        return (value == null) ? null : value.toString();
    }

    /**
     * 设置当前步数。
     * 
     * @param step 当前步数
     */
    private void setStep(int step) {
        if (step >= groups.length) {
            step = groups.length - 1;
        }

        if (step < 0) {
            step = 0;
        }

        this.step = step;

        if (step < groups.length) {
            this.group = groups[step];
            this.props = group.getProperties();
        } else {
            this.group = null;
        }
    }

    /**
     * 格式化字符串，如果字符串超过指定长度，则自动折行。
     * 
     * @param text 要格式化的字符串
     * @param maxLength 行的长度
     * @param locale 国家地区
     * @param prefix1 首行前缀
     * @param prefix2 第二行及后面行的前缀
     * @return 格式化后的字符串
     */
    private String formatLines(String text, int maxLength, Locale locale, String prefix1, String prefix) {
        BreakIterator boundary = BreakIterator.getLineInstance(locale);
        StringBuffer result = new StringBuffer(prefix1);

        boundary.setText(text);

        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;

        while (end != BreakIterator.DONE) {
            String word = text.substring(start, end);

            lineLength = lineLength + word.length();

            if (lineLength >= maxLength) {
                result.append("\n").append(prefix);
                lineLength = word.length();
            }

            result.append(word);
            start = end;
            end = boundary.next();
        }

        return result.toString();
    }
}
