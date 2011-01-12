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
package com.alibaba.antx.util;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class PasswordField {
    public String getPassword(PrintWriter out, String oneLinePrompt) throws IOException {
        return getPassword(out, oneLinePrompt, null);
    }

    public String getPassword(PrintWriter out, String oneLinePrompt, String message) throws IOException {
        if (message == null) {
            message = "";
        }

        String pwd = java6_console_password(out, oneLinePrompt);

        if (pwd != null) {
            return pwd;
        }

        pwd = swing_password(out, oneLinePrompt, message);

        if (pwd != null) {
            return pwd;
        }

        return ugly_password(out, oneLinePrompt);
    }

    private String swing_password(PrintWriter out, String oneLinePrompt, String message) {
        String password = null;

        try {
            JPasswordField passwordField = new JPasswordField(20);
            String[] lines = StringUtil.split(message + "\n" + oneLinePrompt, "\r\n");
            Object[] messages = new Object[lines.length + 1];

            for (int i = 0; i < lines.length; i++) {
                messages[i] = new JLabel(lines[i]);
            }

            messages[lines.length] = passwordField;

            final JOptionPane pane = new JOptionPane(messages, JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);

            final JDialog dialog = new JDialog((Frame) null, "Password required", true);

            dialog.setContentPane(pane);
            dialog.pack();
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            pane.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();

                    if (dialog.isVisible() && (e.getSource() == pane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                }
            });

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();

            int x = (screenSize.width - dialog.getWidth()) / 2;
            int y = (screenSize.height - dialog.getHeight()) / 2;

            dialog.setLocation(x, y);
            dialog.setVisible(true);

            int value = ((Integer) pane.getValue()).intValue();

            if (value == JOptionPane.OK_OPTION) {
                password = new String(passwordField.getPassword());
            } else {
                password = "";
            }
        } catch (Exception e) {
            return null;
        } catch (Error e) {
            return null;
        }

        return password;
    }

    private String java6_console_password(PrintWriter out, String oneLinePrompt) {
        Method consoleMethod = null;
        Method readPasswordMethod = null;

        try {
            consoleMethod = System.class.getMethod("console", new Class[0]);
            readPasswordMethod = consoleMethod.getReturnType().getMethod("readPassword", new Class[0]);
        } catch (NoSuchMethodException e) {
            System.err.println();
            System.err.println("------------------------------------------------------------------------");
            System.err.println(" 缺少密码工具：推荐使用Java6或更新版本，它提供了在控制台输入密码的工具。");
            System.err.println("                       ^^^^^^^^^^^^^^^                                  ");
            System.err.println(" 但现在，我们只能使用一个不完美的方法来代替。");
            System.err.println("------------------------------------------------------------------------");
            System.err.println();
            System.err.flush();
        }

        char[] password;

        if (consoleMethod == null || readPasswordMethod == null) {
            return null;
        }

        try {
            Object console = consoleMethod.invoke(null, new Object[0]);

            if (console == null) {
                return null;
            }

            out.print(oneLinePrompt);
            out.flush();

            password = (char[]) readPasswordMethod.invoke(console, new Object[0]);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return password == null ? "" : new String(password);
    }

    private String ugly_password(PrintWriter out, String oneLinePrompt) throws IOException {
        StringBuffer password = new StringBuffer();
        MaskingThread maskingthread = new MaskingThread(out, oneLinePrompt);
        Thread thread = new Thread(maskingthread);

        thread.start();

        while (true) {
            char c = (char) System.in.read();

            maskingthread.stopMasking();

            if (c == '\r') {
                c = (char) System.in.read();

                if (c == '\n') {
                    break;
                } else {
                    continue;
                }
            } else if (c == '\n') {
                break;
            } else {
                password.append(c);
            }
        }

        return password.toString();
    }

    class MaskingThread extends Thread {
        private boolean           stop = false;
        private final PrintWriter out;
        private final String      oneLinePrompt;

        public MaskingThread(PrintWriter out, String oneLinePrompt) {
            super("password-masking");
            this.out = out;
            this.oneLinePrompt = oneLinePrompt;
        }

        public void run() {
            while (!stop) {
                try {
                    // attempt masking at this rate
                    sleep(1);
                } catch (InterruptedException iex) {
                }

                if (!stop) {
                    out.print("\r" + oneLinePrompt + " \r" + oneLinePrompt);
                }

                out.flush();
            }
        }

        public void stopMasking() {
            this.stop = true;
        }
    }

    public static void main(String[] args) throws IOException {
        PasswordField pf = new PasswordField();
        String pass = pf.getPassword(new PrintWriter(System.out, true), "Enter password: ");

        System.out.println("password is " + pass);
    }
}
