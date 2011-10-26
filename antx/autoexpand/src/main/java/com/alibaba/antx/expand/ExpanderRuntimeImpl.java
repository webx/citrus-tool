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

package com.alibaba.antx.expand;

import com.alibaba.antx.util.CharsetUtil;
import com.alibaba.antx.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class ExpanderRuntimeImpl implements ExpanderRuntime {
    private final BufferedReader in;
    private final PrintWriter out;
    private final PrintWriter err;
    private final String charset;
    private boolean verbose;
    private final Expander expander;

    public ExpanderRuntimeImpl() {
        this(System.in, System.out, System.err, null);
    }

    public ExpanderRuntimeImpl(InputStream inputStream, OutputStream outStream, OutputStream errStream, String charset) {
        boolean charsetSpecified = !StringUtil.isEmpty(charset);
        this.charset = charsetSpecified ? charset : CharsetUtil.detectedSystemCharset();

        try {
            in = new BufferedReader(new InputStreamReader(inputStream, this.charset));
            out = new PrintWriter(new OutputStreamWriter(outStream, this.charset), true);
            err = new PrintWriter(new OutputStreamWriter(errStream, this.charset), true);
        } catch (UnsupportedEncodingException e) {
            throw new ExpanderException(e); // 不应发生
        }

        if (!charsetSpecified) {
            out.println("Detected system charset encoding: " + this.charset);
            out.println("If your can't read the following text, specify correct one like this: ");
            out.println("  autoexpand -c mycharset");
            out.println();
        }

        this.expander = new Expander(this);
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

    public boolean isVerbose() {
        return verbose;
    }

    public Expander getExpander() {
        return expander;
    }

    public void setVerbose() {
        this.verbose = true;
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

    public void start() {
        expander.expand();
    }
}
