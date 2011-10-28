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

package com.alibaba.antx.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberUtil {
    /**
     * Convert a String to a Float
     * 
     * @param val a String to convert
     * @return converted Float
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Float createFloat(String val) {
        return Float.valueOf(val);
    }

    /**
     * Convert a String to a Double
     * 
     * @param val a String to convert
     * @return converted Double
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Double createDouble(String val) {
        return Double.valueOf(val);
    }

    /**
     * Convert a String to a Integer, handling hex and octal notations.
     * 
     * @param val a String to convert
     * @return converted Integer
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Integer createInteger(String val) {
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
        return Integer.decode(val);
    }

    /**
     * Convert a String to a Long
     * 
     * @param val a String to convert
     * @return converted Long
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Long createLong(String val) {
        return Long.valueOf(val);
    }

    /**
     * Convert a String to a BigInteger
     * 
     * @param val a String to convert
     * @return converted BigInteger
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigInteger createBigInteger(String val) {
        BigInteger bi = new BigInteger(val);

        return bi;
    }

    /**
     * Convert a String to a BigDecimal
     * 
     * @param val a String to convert
     * @return converted BigDecimal
     * @throws NumberFormatException if the value cannot be converted
     */
    public static BigDecimal createBigDecimal(String val) {
        BigDecimal bd = new BigDecimal(val);

        return bd;
    }

    /**
     * <p>
     * Turns a string value into a java.lang.Number. First, the value is
     * examined for a type qualifier on the end (
     * <code>'f','F','d','D','l','L'</code>). If it is found, it starts trying
     * to create succissively larger types from the type specified until one is
     * found that can hold the value.
     * </p>
     * <p>
     * If a type specifier is not found, it will check for a decimal point and
     * then try successively larger types from Integer to BigInteger and from
     * Float to BigDecimal.
     * </p>
     * <p>
     * If the string starts with "0x" or "-0x", it will be interpreted as a
     * hexadecimal integer. Values with leading 0's will not be interpreted as
     * octal.
     * </p>
     * 
     * @param val String containing a number
     * @return Number created from the string
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Number createNumber(String val) throws NumberFormatException {
        if (val == null) {
            return null;
        }

        if (val.length() == 0) {
            throw new NumberFormatException("\"\" is not a valid number.");
        }

        if (val.startsWith("--")) {
            // this is protection for poorness in java.lang.BigDecimal.
            // it accepts this as a legal value, but it does not appear 
            // to be in specification of class. OS X Java parses it to 
            // a wrong value.
            return null;
        }

        if (val.startsWith("0x") || val.startsWith("-0x")) {
            return createInteger(val);
        }

        char lastChar = val.charAt(val.length() - 1);
        String mant;
        String dec;
        String exp;
        int decPos = val.indexOf('.');
        int expPos = val.indexOf('e') + val.indexOf('E') + 1;

        if (decPos > -1) {
            if (expPos > -1) {
                if (expPos < decPos) {
                    throw new NumberFormatException(val + " is not a valid number.");
                }

                dec = val.substring(decPos + 1, expPos);
            } else {
                dec = val.substring(decPos + 1);
            }

            mant = val.substring(0, decPos);
        } else {
            if (expPos > -1) {
                mant = val.substring(0, expPos);
            } else {
                mant = val;
            }

            dec = null;
        }

        if (!Character.isDigit(lastChar)) {
            if (expPos > -1 && expPos < val.length() - 1) {
                exp = val.substring(expPos + 1, val.length() - 1);
            } else {
                exp = null;
            }

            //Requesting a specific type..
            String numeric = val.substring(0, val.length() - 1);
            boolean allZeros = isAllZeros(mant) && isAllZeros(exp);

            switch (lastChar) {
                case 'l':
                case 'L':

                    if (dec == null && exp == null && isDigits(numeric.substring(1))
                            && (numeric.charAt(0) == '-' || Character.isDigit(numeric.charAt(0)))) {
                        try {
                            return createLong(numeric);
                        } catch (NumberFormatException nfe) {
                            //Too big for a long
                        }

                        return createBigInteger(numeric);
                    }

                    throw new NumberFormatException(val + " is not a valid number.");

                case 'f':
                case 'F':

                    try {
                        Float f = createFloat(numeric);

                        if (!(f.isInfinite() || f.floatValue() == 0.0F && !allZeros)) {
                            //If it's too big for a float or the float value = 0 and the string
                            //has non-zeros in it, then float doens't have the presision we want
                            return f;
                        }
                    } catch (NumberFormatException nfe) {
                    }

                    //Fall through
                case 'd':
                case 'D':

                    try {
                        Double d = createDouble(numeric);

                        if (!(d.isInfinite() || d.floatValue() == 0.0D && !allZeros)) {
                            return d;
                        }
                    } catch (NumberFormatException nfe) {
                    }

                    try {
                        return createBigDecimal(numeric);
                    } catch (NumberFormatException e) {
                    }

                    //Fall through
                default:
                    throw new NumberFormatException(val + " is not a valid number.");
            }
        } else {
            //User doesn't have a preference on the return type, so let's start
            //small and go from there...
            if (expPos > -1 && expPos < val.length() - 1) {
                exp = val.substring(expPos + 1, val.length());
            } else {
                exp = null;
            }

            if (dec == null && exp == null) {
                //Must be an int,long,bigint
                try {
                    return createInteger(val);
                } catch (NumberFormatException nfe) {
                }

                try {
                    return createLong(val);
                } catch (NumberFormatException nfe) {
                }

                return createBigInteger(val);
            } else {
                //Must be a float,double,BigDec
                boolean allZeros = isAllZeros(mant) && isAllZeros(exp);

                try {
                    Float f = createFloat(val);

                    if (!(f.isInfinite() || f.floatValue() == 0.0F && !allZeros)) {
                        return f;
                    }
                } catch (NumberFormatException nfe) {
                }

                try {
                    Double d = createDouble(val);

                    if (!(d.isInfinite() || d.doubleValue() == 0.0D && !allZeros)) {
                        return d;
                    }
                } catch (NumberFormatException nfe) {
                }

                return createBigDecimal(val);
            }
        }
    }

    /**
     * Checks whether the String contains only digit characters. Null and blank
     * string will return false.
     * 
     * @param str the string to check
     * @return boolean contains only unicode numeric
     */
    public static boolean isDigits(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Utility method for createNumber. Returns true if s is null
     * 
     * @param s the String to check
     * @return if it is all zeros or null
     */
    private static boolean isAllZeros(String s) {
        if (s == null) {
            return true;
        }

        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) != '0') {
                return false;
            }
        }

        return s.length() > 0;
    }
}
