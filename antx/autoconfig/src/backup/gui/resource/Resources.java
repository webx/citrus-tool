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

package com.alibaba.antx.config.gui.resource;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;

public class Resources {
    private static ResourceBundle bundle = ResourceBundle.getBundle(Resources.class.getName());

    public static String getText(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String getText(String key, Object[] args) {
        try {
            return MessageFormat.format(getText(key), args);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static ImageDescriptor getImageDescriptor(String key) {
        while (!key.startsWith("icon.") && !key.startsWith("image.")) {
            key = getText(key);
        }

        return ImageDescriptor.createFromFile(Resources.class, getText(key));
    }
}
