package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class PropertiesUtil {
    public static <T extends PropertyModel> T getModel(Class<T> modelType, IDocument document, String searchKey) {
        try {
            if (document != null) {
                Constructor<T> constructor = modelType.getConstructor(String.class, String.class);

                for (int line = 0; line < document.getNumberOfLines(); line++) {
                    IRegion region = document.getLineInformation(line);

                    for (int i = 0; i < region.getLength(); i++) {
                        if ("=".equals(document.get(region.getOffset() + i, 1))) {
                            String key = trimToNull(document.get(region.getOffset(), i));

                            if (searchKey.equals(key)) {
                                String rawValue = document.get(region.getOffset() + i + 1, region.getLength() - i - 1);

                                return constructor.newInstance(key, rawValue);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get model", e);
        }

        return null;
    }

    public static abstract class PropertyModel {
        public final String key;
        public final String value;
        public final Map<String, String> params = createHashMap();

        public PropertyModel() {
            this(null, null);
        }

        public PropertyModel(String key, String rawValue) {
            this.key = key;
            this.value = parse(rawValue, params);
        }

        private static String parse(String rawValue, Map<String, String> params) {
            rawValue = trimToNull(rawValue);
            String value = null;

            if (rawValue != null) {
                String[] parts = rawValue.split(",|;");

                for (String part : parts) {
                    part = trimToNull(part);

                    if (part != null) {
                        int index = part.indexOf("=");

                        if (index >= 0) {
                            String paramKey = trimToNull(part.substring(0, index));
                            String paramValue = trimToNull(part.substring(index + 1));

                            if (paramKey == null) {
                                throw new IllegalArgumentException("Illegal namespace URI: " + rawValue);
                            }

                            params.put(paramKey, paramValue);
                        } else {
                            if (value == null) {
                                value = part;
                            }
                        }
                    }
                }
            }

            return value;
        }
    }
}
