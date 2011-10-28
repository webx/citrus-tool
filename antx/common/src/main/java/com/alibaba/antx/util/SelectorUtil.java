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

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * <p>
 * This is a utility class used by selectors and DirectoryScanner. The
 * functionality more properly belongs just to selectors, but unfortunately
 * DirectoryScanner exposed these as protected methods. Thus we have to support
 * any subclasses of DirectoryScanner that may access these methods.
 * </p>
 * <p>
 * This is a Singleton.
 * </p>
 * 
 * @since 1.5
 */
public final class SelectorUtil {
    private static final String FILE_SEP = "/";
    private static final char FILE_SEP_CHAR = '/';

    /**
     * Tests whether or not a given path matches the start of a given pattern up
     * to the first "".
     * <p>
     * This is not a general purpose test and should only be used if you can
     * live with false positives. For example, <code>pattern=\a</code> and
     * <code>str=b</code> will yield <code>true</code>.
     * </p>
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @return whether or not a given path matches the start of a given pattern
     *         up to the first "".
     */
    public static boolean matchPatternStart(String pattern, String str) {
        return matchPatternStart(pattern, str, true);
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up
     * to the first "".
     * <p>
     * This is not a general purpose test and should only be used if you can
     * live with false positives. For example, <code>pattern=\a</code> and
     * <code>str=b</code> will yield <code>true</code>.
     * </p>
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case
     *            sensitively.
     * @return whether or not a given path matches the start of a given pattern
     *         up to the first "".
     */
    public static boolean matchPatternStart(String pattern, String str, boolean isCaseSensitive) {
        // When str starts with a FILE_SEP, pattern has to start with a
        // FILE_SEP.
        // When pattern starts with a FILE_SEP, str has to start with a
        // FILE_SEP.
        if (str.startsWith(FILE_SEP) != pattern.startsWith(FILE_SEP)) {
            return false;
        }

        String[] patDirs = tokenizePathAsArray(pattern);
        String[] strDirs = tokenizePathAsArray(str);

        int patIdxStart = 0;
        int patIdxEnd = patDirs.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxStart];

            if (patDir.equals("**")) {
                break;
            }

            if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
                return false;
            }

            patIdxStart++;
            strIdxStart++;
        }

        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            return true;
        } else if (patIdxStart > patIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else {
            // pattern now holds ** while string is not exhausted
            // this will generate false positives but we can live with that.
            return true;
        }
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @return <code>true</code> if the pattern matches against the string, or
     *         <code>false</code> otherwise.
     */
    public static boolean matchPath(String pattern, String str) {
        return matchPath(pattern, str, true);
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case
     *            sensitively.
     * @return <code>true</code> if the pattern matches against the string, or
     *         <code>false</code> otherwise.
     */
    public static boolean matchPath(String pattern, String str, boolean isCaseSensitive) {
        // When str starts with a FILE_SEP, pattern has to start with a
        // FILE_SEP.
        // When pattern starts with a FILE_SEP, str has to start with a
        // FILE_SEP.
        if (str.startsWith(FILE_SEP) != pattern.startsWith(FILE_SEP)) {
            return false;
        }

        String[] patDirs = tokenizePathAsArray(pattern);
        String[] strDirs = tokenizePathAsArray(str);

        int patIdxStart = 0;
        int patIdxEnd = patDirs.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strDirs.length - 1;

        // up to first '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxStart];

            if (patDir.equals("**")) {
                break;
            }

            if (!match(patDir, strDirs[strIdxStart], isCaseSensitive)) {
                patDirs = null;
                strDirs = null;
                return false;
            }

            patIdxStart++;
            strIdxStart++;
        }

        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }

            return true;
        } else {
            if (patIdxStart > patIdxEnd) {
                // String not exhausted, but pattern is. Failure.
                patDirs = null;
                strDirs = null;
                return false;
            }
        }

        // up to last '**'
        while (patIdxStart <= patIdxEnd && strIdxStart <= strIdxEnd) {
            String patDir = patDirs[patIdxEnd];

            if (patDir.equals("**")) {
                break;
            }

            if (!match(patDir, strDirs[strIdxEnd], isCaseSensitive)) {
                patDirs = null;
                strDirs = null;
                return false;
            }

            patIdxEnd--;
            strIdxEnd--;
        }

        if (strIdxStart > strIdxEnd) {
            // String is exhausted
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (!patDirs[i].equals("**")) {
                    patDirs = null;
                    strDirs = null;
                    return false;
                }
            }

            return true;
        }

        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;

            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }

            if (patIdxTmp == patIdxStart + 1) {
                // '**/**' situation, so skip one
                patIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = patIdxTmp - patIdxStart - 1;
            int strLength = strIdxEnd - strIdxStart + 1;
            int foundIdx = -1;

            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = patDirs[patIdxStart + j + 1];
                    String subStr = strDirs[strIdxStart + i + j];

                    if (!match(subPat, subStr, isCaseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                patDirs = null;
                strDirs = null;
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (!patDirs[i].equals("**")) {
                patDirs = null;
                strDirs = null;
                return false;
            }
        }

        return true;
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may
     * contain two special characters:<br>
     * '' means zero or more characters<br>
     * '?' means one and only one character
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not
     *            be <code>null</code>.
     * @return <code>true</code> if the string matches against the pattern, or
     *         <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str) {
        return match(pattern, str, true);
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may
     * contain two special characters:<br>
     * '' means zero or more characters<br>
     * '?' means one and only one character
     * 
     * @param pattern The pattern to match against. Must not be
     *            <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not
     *            be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case
     *            sensitively.
     * @return <code>true</code> if the string matches against the pattern, or
     *         <code>false</code> otherwise.
     */
    public static boolean match(String pattern, String str, boolean isCaseSensitive) {
        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;

        for (char element : patArr) {
            if (element == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }

            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];

                if (ch != '?') {
                    if (isCaseSensitive && ch != strArr[i]) {
                        return false; // Character mismatch
                    }

                    if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[i])) {
                        return false; // Character mismatch
                    }
                }
            }

            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (isCaseSensitive && ch != strArr[strIdxStart]) {
                    return false; // Character mismatch
                }

                if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart])) {
                    return false; // Character mismatch
                }
            }

            patIdxStart++;
            strIdxStart++;
        }

        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }

            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?') {
                if (isCaseSensitive && ch != strArr[strIdxEnd]) {
                    return false; // Character mismatch
                }

                if (!isCaseSensitive && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxEnd])) {
                    return false; // Character mismatch
                }
            }

            patIdxEnd--;
            strIdxEnd--;
        }

        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }

            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;

            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }

            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = patIdxTmp - patIdxStart - 1;
            int strLength = strIdxEnd - strIdxStart + 1;
            int foundIdx = -1;

            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];

                    if (ch != '?') {
                        if (isCaseSensitive && ch != strArr[strIdxStart + i + j]) {
                            continue strLoop;
                        }

                        if (!isCaseSensitive
                                && Character.toUpperCase(ch) != Character.toUpperCase(strArr[strIdxStart + i + j])) {
                            continue strLoop;
                        }
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }

        return true;
    }

    /**
     * Breaks a path up into a Vector of path elements, tokenizing on
     * <code>FILE_SEP</code>.
     * 
     * @param path Path to tokenize. Must not be <code>null</code>.
     * @return a Vector of path elements from the tokenized path
     */
    public static Vector tokenizePath(String path) {
        return tokenizePath(path, FILE_SEP);
    }

    /**
     * Breaks a path up into a Vector of path elements, tokenizing on
     * 
     * @param path Path to tokenize. Must not be <code>null</code>.
     * @param separator the separator against which to tokenize.
     * @return a Vector of path elements from the tokenized path
     * @since Ant 1.6
     */
    public static Vector tokenizePath(String path, String separator) {
        Vector ret = new Vector();
        StringTokenizer st = new StringTokenizer(path, separator);

        while (st.hasMoreTokens()) {
            ret.addElement(st.nextToken());
        }

        return ret;
    }

    /**
     * Same as {@link #tokenizePath tokenizePath} but hopefully faster.
     */
    private static String[] tokenizePathAsArray(String path) {
        char sep = FILE_SEP_CHAR;
        int start = 0;
        int len = path.length();
        int count = 0;

        for (int pos = 0; pos < len; pos++) {
            if (path.charAt(pos) == sep) {
                if (pos != start) {
                    count++;
                }

                start = pos + 1;
            }
        }

        if (len != start) {
            count++;
        }

        String[] l = new String[count];

        count = 0;
        start = 0;

        for (int pos = 0; pos < len; pos++) {
            if (path.charAt(pos) == sep) {
                if (pos != start) {
                    String tok = path.substring(start, pos);

                    l[count++] = tok;
                }

                start = pos + 1;
            }
        }

        if (len != start) {
            String tok = path.substring(start);

            l[count /* ++ */] = tok;
        }

        return l;
    }

    /**
     * Returns dependency information on these two files. If src has been
     * modified later than target, it returns true. If target doesn't exist, it
     * likewise returns true. Otherwise, target is newer than src and is not out
     * of date, thus the method returns false. It also returns false if the src
     * file doesn't even exist, since how could the target then be out of date.
     * 
     * @param src the original file
     * @param target the file being compared against
     * @param granularity the amount in seconds of slack we will give in
     *            determining out of dateness
     * @return whether the target is out of date
     */
    public static boolean isOutOfDate(File src, File target, int granularity) {
        if (!src.exists()) {
            return false;
        }

        if (!target.exists()) {
            return true;
        }

        if (src.lastModified() - granularity > target.lastModified()) {
            return true;
        }

        return false;
    }

    /**
     * Returns dependency information on these two resources. If src has been
     * modified later than target, it returns true. If target doesn't exist, it
     * likewise returns true. Otherwise, target is newer than src and is not out
     * of date, thus the method returns false. It also returns false if the src
     * file doesn't even exist, since how could the target then be out of date.
     * 
     * @param src the original resource
     * @param target the resource being compared against
     * @param granularity the amount in seconds of slack we will give in
     *            determining out of dateness
     * @return whether the target is out of date
     */
    public static boolean isOutOfDate(Resource src, Resource target, int granularity) {
        if (!src.isExists()) {
            return false;
        }

        if (!target.isExists()) {
            return true;
        }

        if (src.getLastModified() - granularity > target.getLastModified()) {
            return true;
        }

        return false;
    }

    /**
     * "Flattens" a string by removing all whitespace (space, tab, linefeed,
     * carriage return, and formfeed). This uses StringTokenizer and the default
     * set of tokens as documented in the single arguement constructor.
     * 
     * @param input a String to remove all whitespace.
     * @return a String that has had all whitespace removed.
     */
    public static String removeWhitespace(String input) {
        StringBuffer result = new StringBuffer();

        if (input != null) {
            StringTokenizer st = new StringTokenizer(input);

            while (st.hasMoreTokens()) {
                result.append(st.nextToken());
            }
        }

        return result.toString();
    }

    /**
     * Tests if a string contains stars or question marks
     * 
     * @param input a String which one wants to test for containing wildcard
     * @return true if the string contains at least a star or a question mark
     */
    public static boolean hasWildcards(String input) {
        return input.indexOf('*') != -1 || input.indexOf('?') != -1;
    }

    /**
     * removes from a pattern all tokens to the right containing wildcards
     * 
     * @param input the input string
     * @return the leftmost part of the pattern without wildcards
     */
    public static String rtrimWildcardTokens(String input) {
        Vector v = tokenizePath(input, FILE_SEP);
        StringBuffer sb = new StringBuffer();

        for (int counter = 0; counter < v.size(); counter++) {
            if (hasWildcards((String) v.elementAt(counter))) {
                break;
            }

            if (counter > 0) {
                sb.append(FILE_SEP);
            }

            sb.append((String) v.elementAt(counter));
        }

        return sb.toString();
    }

    /**
     * 查看指定名称是否符合patterns。
     * 
     * @param name 要匹配的名称
     * @param includes include patterns
     * @param excludes exclude patterns
     * @return 如果符合patterns，则返回<code>true</code>
     */
    public static boolean matchPath(String name, String[] includes, String[] excludes) {
        boolean match = includes.length == 0;

        for (String include : includes) {
            if (matchPath(include, name)) {
                match = true;
                break;
            }
        }

        if (match) {
            for (String exclude : excludes) {
                if (matchPath(exclude, name)) {
                    match = false;
                    break;
                }
            }
        }

        return match;
    }

    /**
     * 查看指定名称是否符合patterns的前缀。
     * 
     * @param name 要匹配的名称
     * @param includes include patterns
     * @param excludes exclude patterns
     * @return 如果符合patterns，则返回<code>true</code>
     */
    public static boolean matchPathPrefix(String name, String[] includes, String[] excludes) {
        boolean match = includes.length == 0;

        for (String include : includes) {
            if (matchPatternStart(include, name) && isMorePowerfulThanExcludes(name, excludes)
                    && isDeeper(include, name)) {
                match = true;
                break;
            }
        }

        if (match) {
            if (!name.endsWith(FILE_SEP)) {
                name = name + FILE_SEP;
            }

            for (String e : excludes) {
                if (e.equals("**") || e.endsWith("**") && matchPath(e.substring(0, e.length() - 2), name, true)) {
                    match = false;
                    break;
                }
            }
        }

        return match;
    }

    /**
     * Verify that a pattern specifies files deeper than the level of the
     * specified file.
     * 
     * @param pattern the pattern to check.
     * @param name the name to check.
     * @return whether the pattern is deeper than the name.
     * @since Ant 1.6.3
     */
    private static boolean isDeeper(String pattern, String name) {
        Vector p = tokenizePath(pattern);
        Vector n = tokenizePath(name);

        return p.contains("**") || p.size() > n.size();
    }

    /**
     * Find out whether one particular include pattern is more powerful than all
     * the excludes. Note: the power comparison is based on the length of the
     * include pattern and of the exclude patterns without the wildcards.
     * Ideally the comparison should be done based on the depth of the match;
     * that is to say how many file separators have been matched before the
     * first or the end of the pattern. IMPORTANT : this function should return
     * false "with care".
     * 
     * @param name the relative path to test.
     * @param includepattern one include pattern.
     * @return true if there is no exclude pattern more powerful than this
     *         include pattern.
     * @since Ant 1.6
     */
    private static boolean isMorePowerfulThanExcludes(String name, String[] excludes) {
        String soughtexclude = name + File.separator + "**";

        for (String exclude : excludes) {
            if (exclude.equals(soughtexclude)) {
                return false;
            }
        }

        return true;
    }
}
