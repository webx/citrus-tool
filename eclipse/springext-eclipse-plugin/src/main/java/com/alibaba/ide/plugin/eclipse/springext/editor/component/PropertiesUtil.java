package com.alibaba.ide.plugin.eclipse.springext.editor.component;

import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;

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

    private static String decode(String in) {
        in = trimToNull(in);

        if (in == null) {
            return null;
        }

        StringBuilder out = new StringBuilder();

        for (int offset = 0; offset < in.length();) {
            char ch = in.charAt(offset++);

            if (ch == '\\') {
                ch = in.charAt(offset++);

                if (ch == 'u') {
                    int value = 0;

                    for (int i = 0; i < 4; i++) {
                        ch = in.charAt(offset++);

                        switch (ch) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + ch - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + ch - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + ch - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }

                    out.append((char) value);
                } else {
                    if (ch == 't') {
                        ch = '\t';
                    } else if (ch == 'r') {
                        ch = '\r';
                    } else if (ch == 'n') {
                        ch = '\n';
                    } else if (ch == 'f') {
                        ch = '\f';
                    }

                    out.append(ch);
                }
            } else {
                out.append(ch);
            }
        }

        return out.toString();
    }

    private static String encode(String in) {
        in = trimToNull(in);

        if (in == null) {
            return EMPTY_STRING;
        }

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {
            char ch = in.charAt(i);

            if (ch > 61 && ch < 127) {
                if (ch == '\\') {
                    out.append("\\\\");
                } else {
                    out.append(ch);
                }

                continue;
            }

            switch (ch) {
                case '\t':
                    out.append("\\t");
                    break;

                case '\n':
                    out.append("\\n");
                    break;

                case '\r':
                    out.append("\\r");
                    break;

                case '\f':
                    out.append("\\f");
                    break;

                case ':':
                case '#':
                case '!':
                    out.append('\\').append(ch);
                    break;

                default:
                    if (ch < 0x0020 || ch > 0x007e) {
                        out.append("\\u");
                        out.append(toHex(ch >> 12 & 0xF));
                        out.append(toHex(ch >> 8 & 0xF));
                        out.append(toHex(ch >> 4 & 0xF));
                        out.append(toHex(ch & 0xF));
                    } else {
                        out.append(ch);
                    }
            }
        }

        return out.toString();
    }

    private static char toHex(int nibble) {
        return hexDigit[nibble & 0xF];
    }

    private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F' };

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
