package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

public class PropertiesUtil {
    public static <T extends PropertyModel> T getModel(Class<T> modelType, IDocument document, String searchKey) {
        return getModel(modelType, document, searchKey, false);
    }

    public static <T extends PropertyModel> T getModel(Class<T> modelType, IDocument document, String searchKey,
                                                       boolean matchPrefix) {
        try {
            if (document != null) {
                Constructor<T> constructor = modelType.getConstructor(String.class, String.class);

                for (int line = 0; line < document.getNumberOfLines(); line++) {
                    IRegion region = document.getLineInformation(line);

                    for (int i = 0; i < region.getLength(); i++) {
                        String c = document.get(region.getOffset() + i, 1);

                        if ("#".equals(c)) {
                            break; // skip comment line
                        }

                        if ("=".equals(c)) {
                            String key = decode(document.get(region.getOffset(), i));
                            boolean matched;

                            if (matchPrefix) {
                                matched = key.endsWith(searchKey);
                            } else {
                                matched = searchKey.equals(key);
                            }

                            if (matched) {
                                String rawValue = decode(document.get(region.getOffset() + i + 1, region.getLength()
                                        - i - 1));

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

    public static void updateDocument(IDocument document, String searchKey, String newKey, String rawValue) {
        try {
            if (document != null) {
                for (int line = 0; line < document.getNumberOfLines(); line++) {
                    IRegion region = document.getLineInformation(line);

                    for (int i = 0; i < region.getLength(); i++) {
                        String c = document.get(region.getOffset() + i, 1);

                        if ("#".equals(c)) {
                            break; // skip comment line
                        }

                        if ("=".equals(c)) {
                            String key = decode(document.get(region.getOffset(), i));

                            if (searchKey.equals(key)) {
                                document.replace(region.getOffset(), region.getLength(), encode(newKey) + " = "
                                        + encode(rawValue));

                                return;
                            }

                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not modify document", e);
        }
    }

    public static abstract class PropertyModel {
        protected final String key;
        protected final String value;
        protected final Map<String, String> params = createHashMap();

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
