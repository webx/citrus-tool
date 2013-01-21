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

package org.slf4j.impl;

import com.intellij.openapi.diagnostic.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

final class IdeaLoggerAdapter extends MarkerIgnoringBase implements LocationAwareLogger {
    private final transient Logger logger;

    IdeaLoggerAdapter(Logger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public boolean isTraceEnabled() {
        return logger.isDebugEnabled();
    }

    public void trace(String msg) {
        logger.debug(msg, null);
    }

    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String format, Object[] argArray) {
        if (isTraceEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void trace(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String msg) {
        logger.debug(msg, null);
    }

    public void debug(String format, Object arg) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String format, Object arg1, Object arg2) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String format, Object[] argArray) {
        if (logger.isDebugEnabled()) {
            FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
            logger.debug(ft.getMessage(), ft.getThrowable());
        }
    }

    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public void info(String msg) {
        logger.info(msg, null);
    }

    public void info(String format, Object arg) {
        FormattingTuple ft = MessageFormatter.format(format, arg);
        logger.info(ft.getMessage(), ft.getThrowable());
    }

    public void info(String format, Object arg1, Object arg2) {
        FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
        logger.info(ft.getMessage(), ft.getThrowable());
    }

    public void info(String format, Object[] argArray) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
        logger.info(ft.getMessage(), ft.getThrowable());
    }

    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(String msg) {
        logger.warn(msg, null);
    }

    public void warn(String format, Object arg) {
        FormattingTuple ft = MessageFormatter.format(format, arg);
        logger.warn(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String format, Object arg1, Object arg2) {
        FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
        logger.warn(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String format, Object[] argArray) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
        logger.warn(ft.getMessage(), ft.getThrowable());
    }

    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public void error(String msg) {
        logger.error(msg, (Throwable) null);
    }

    public void error(String format, Object arg) {
        FormattingTuple ft = MessageFormatter.format(format, arg);
        logger.error(ft.getMessage(), ft.getThrowable());
    }

    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple ft = MessageFormatter.format(format, arg1, arg2);
        logger.error(ft.getMessage(), ft.getThrowable());
    }

    public void error(String format, Object[] argArray) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, argArray);
        logger.error(ft.getMessage(), ft.getThrowable());
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public void log(Marker marker, String callerFQCN, int level, String msg, Object[] argArray, Throwable t) {
        FormattingTuple ft = MessageFormatter.arrayFormat(msg, argArray);

        switch (level) {
            case LocationAwareLogger.TRACE_INT:
                if (logger.isDebugEnabled()) {
                    logger.debug(ft.getMessage(), ft.getThrowable());
                }

                break;

            case LocationAwareLogger.DEBUG_INT:
                if (logger.isDebugEnabled()) {
                    logger.debug(ft.getMessage(), ft.getThrowable());
                }

                break;

            case LocationAwareLogger.INFO_INT:
                logger.info(ft.getMessage(), ft.getThrowable());

                break;

            case LocationAwareLogger.WARN_INT:
                logger.warn(ft.getMessage(), ft.getThrowable());

                break;

            case LocationAwareLogger.ERROR_INT:
                logger.error(ft.getMessage(), ft.getThrowable());

                break;

            default:
                throw new IllegalStateException("Level number " + level + " is not recognized.");
        }
    }
}

