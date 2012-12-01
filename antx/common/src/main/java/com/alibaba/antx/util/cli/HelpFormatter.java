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

package com.alibaba.antx.util.cli;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A formatter of help messages for the current command line options
 *
 * @author Slawek Zachcial
 * @author John Keyes (john at integralsource.com)
 */
public class HelpFormatter {
    // --------------------------------------------------------------- Constants
    public static final int    DEFAULT_WIDTH           = 74;
    public static final int    DEFAULT_LEFT_PAD        = 1;
    public static final int    DEFAULT_DESC_PAD        = 3;
    public static final String DEFAULT_SYNTAX_PREFIX   = "usage: ";
    public static final String DEFAULT_OPT_PREFIX      = "-";
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";
    public static final String DEFAULT_ARG_NAME        = "arg";

    // ------------------------------------------------------------------ Static
    // -------------------------------------------------------------- Attributes
    public int    defaultWidth;
    public int    defaultLeftPad;
    public int    defaultDescPad;
    public String defaultSyntaxPrefix;
    public String defaultNewLine;
    public String defaultOptPrefix;
    public String defaultLongOptPrefix;
    public String defaultArgName;

    // ------------------------------------------------------------ Constructors
    public HelpFormatter() {
        defaultWidth = DEFAULT_WIDTH;
        defaultLeftPad = DEFAULT_LEFT_PAD;
        defaultDescPad = DEFAULT_DESC_PAD;
        defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;
        defaultNewLine = System.getProperty("line.separator");
        defaultOptPrefix = DEFAULT_OPT_PREFIX;
        defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;
        defaultArgName = DEFAULT_ARG_NAME;
    }

    // ------------------------------------------------------------------ Public
    public void printHelp(String cmdLineSyntax, Options options) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, false);
    }

    public void printHelp(String cmdLineSyntax, Options options, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, autoUsage);
    }

    public void printHelp(String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, header, options, footer, autoUsage);
    }

    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer,
                          boolean autoUsage) {
        PrintWriter pw = new PrintWriter(System.out);

        printHelp(pw, width, cmdLineSyntax, header, options, defaultLeftPad, defaultDescPad, footer, autoUsage);
        pw.flush();
    }

    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad,
                          int descPad, String footer) throws IllegalArgumentException {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad,
                          int descPad, String footer, boolean autoUsage) throws IllegalArgumentException {
        if (cmdLineSyntax == null || cmdLineSyntax.length() == 0) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && header.trim().length() > 0) {
            printWrapped(pw, width, header);
        }

        printOptions(pw, width, options, leftPad, descPad);

        if (footer != null && footer.trim().length() > 0) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * <p>
     * Prints the usage statement for the specified application.
     * </p>
     *
     * @param pw      The PrintWriter to print the usage statement
     * @param width   ??
     * @param appName The application name
     * @param options The command line Options
     */
    public void printUsage(PrintWriter pw, int width, String app, Options options) {
        // initialise the string buffer
        StringBuffer buff = new StringBuffer(defaultSyntaxPrefix).append(app).append(" ");

        // create a list for processed option groups
        ArrayList list = new ArrayList();

        // temp variable
        Option option;

        // iterate over the options
        for (Iterator i = options.getOptions().iterator(); i.hasNext(); ) {
            // get the next Option
            option = (Option) i.next();

            // check if the option is part of an OptionGroup
            OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group and the group has not already
            // been processed
            if (group != null && !list.contains(group)) {
                // add the group to the processed list
                list.add(group);

                // get the names of the options from the OptionGroup
                Collection names = group.getNames();

                buff.append("[");

                // for each option in the OptionGroup
                for (Iterator iter = names.iterator(); iter.hasNext(); ) {
                    buff.append(iter.next());

                    if (iter.hasNext()) {
                        buff.append(" | ");
                    }
                }

                buff.append("]");
            }
            // if the Option is not part of an OptionGroup
            else {
                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("[");
                }

                if (!" ".equals(option.getOpt())) {
                    buff.append("-").append(option.getOpt());
                } else {
                    buff.append("--").append(option.getLongOpt());
                }

                if (option.hasArg()) {
                    buff.append(" ");
                }

                // if the Option has a value
                if (option.hasArg()) {
                    buff.append(option.getArgName());
                }

                // if the Option is not a required option
                if (!option.isRequired()) {
                    buff.append("]");
                }

                buff.append(" ");
            }
        }

        // call printWrapped
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1, buff.toString());
    }

    public void printUsage(PrintWriter pw, int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;

        printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos, defaultSyntaxPrefix + cmdLineSyntax);
    }

    public void printOptions(PrintWriter pw, int width, Options options, int leftPad, int descPad) {
        StringBuffer sb = new StringBuffer();

        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    public void printWrapped(PrintWriter pw, int width, String text) {
        printWrapped(pw, width, 0, text);
    }

    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
        StringBuffer sb = new StringBuffer(text.length());

        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    // --------------------------------------------------------------- Protected
    protected StringBuffer renderOptions(StringBuffer sb, int width, Options options, int leftPad, int descPad) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);

        //first create list containing only <lpad>-a,--aaa where -a is opt and --aaa is
        //long opt; in parallel look for the longest opt string
        //this list will be then used to sort options ascending
        int max = 0;
        StringBuffer optBuf;
        List prefixList = new ArrayList();
        Option option;
        List optList = options.helpOptions();

        Collections.sort(optList, new OptionComparator());

        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            option = (Option) i.next();
            optBuf = new StringBuffer(8);

            if (option.getOpt().equals(" ")) {
                optBuf.append(lpad).append("   " + defaultLongOptPrefix).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(defaultOptPrefix).append(option.getOpt());

                if (option.hasLongOpt()) {
                    optBuf.append(',').append(defaultLongOptPrefix).append(option.getLongOpt());
                }
            }

            if (option.hasArg()) {
                if (option.hasArgName()) {
                    optBuf.append(" <").append(option.getArgName()).append('>');
                } else {
                    optBuf.append(' ');
                }
            }

            prefixList.add(optBuf);
            max = optBuf.length() > max ? optBuf.length() : max;
        }

        int x = 0;

        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            option = (Option) i.next();
            optBuf = new StringBuffer(prefixList.get(x++).toString());

            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
            }

            optBuf.append(dpad);

            int nextLineTabStop = max + descPad;

            renderWrappedText(sb, width, nextLineTabStop, optBuf.append(option.getDescription()).toString());

            if (i.hasNext()) {
                sb.append(defaultNewLine);
            }
        }

        return sb;
    }

    protected StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
        int pos = findWrapPos(text, width, 0);

        if (pos == -1) {
            sb.append(rtrim(text));
            return sb;
        } else {
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }

        //all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStop);

        while (true) {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1) {
                sb.append(text);
                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

    /**
     * Finds the next text wrap position after <code>startPos</code> for the
     * text in <code>sb</code> with the column width <code>width</code>. The
     * wrap point is the last postion before startPos+width having a whitespace
     * character (space, \n, \r).
     *
     * @param sb       text to be analyzed
     * @param width    width of the wrapped text
     * @param startPos position from which to start the lookup whitespace
     *                 character
     * @return postion on which the text must be wrapped or -1 if the wrap
     *         position is at the end of the text
     */
    protected int findWrapPos(String text, int width, int startPos) {
        int pos = -1;

        // the line ends before the max wrap pos or a new line char found
        if ((pos = text.indexOf('\n', startPos)) != -1 && pos <= width || (pos = text.indexOf('\t', startPos)) != -1
                                                                          && pos <= width) {
            return pos;
        } else if (startPos + width >= text.length()) {
            return -1;
        }

        //look for the last whitespace character before startPos+width
        pos = startPos + width;

        char c;

        while (pos >= startPos && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
            --pos;
        }

        //if we found it - just return
        if (pos > startPos) {
            return pos;
        } else {
            //must look for the first whitespace chearacter after startPos + width
            pos = startPos + width;

            while (pos <= text.length() && (c = text.charAt(pos)) != ' ' && c != '\n' && c != '\r') {
                ++pos;
            }

            return pos == text.length() ? -1 : pos;
        }
    }

    protected String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);

        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }

        return sb.toString();
    }

    protected String rtrim(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }

        int pos = s.length();

        while (pos >= 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }

        return s.substring(0, pos);
    }

    // ------------------------------------------------------- Package protected
    // ----------------------------------------------------------------- Private
    // ----------------------------------------------------------- Inner classes
    private static class OptionComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Option opt1 = (Option) o1;
            Option opt2 = (Option) o2;

            return opt1.getOpt().toLowerCase().compareTo(opt2.getOpt().toLowerCase());
        }
    }
}
