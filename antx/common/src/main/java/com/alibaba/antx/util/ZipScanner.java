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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipScanner {
    /** The base directory to be scanned. */
    protected URL zipURL;

    /** The patterns for the files to be included. */
    protected String[] includes;

    /** The patterns for the files to be excluded. */
    protected String[] excludes;

    /** The files which matched at least one include and no excludes and were selected. */
    protected List filesIncluded;

    /** The files which did not match any includes or selectors. */
    protected List filesNotIncluded;

    /** The files which matched at least one include and at least one exclude. */
    protected List filesExcluded;

    /** The directories which matched at least one include and no excludes and were selected. */
    protected List dirsIncluded;

    /** The directories which were found and did not match any includes. */
    protected List dirsNotIncluded;

    /** The directories which matched at least one include and at least one exclude. */
    protected List dirsExcluded;

    /** Whether or not the file system should be treated as a case sensitive one. */
    protected boolean isCaseSensitive = true;

    /** Whether or not everything tested so far has been included. */
    protected boolean everythingIncluded = true;

/**
     * Sole constructor.
     */
    public ZipScanner() {
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the
     * first "".<p>This is not a general purpose test and should only be used if you can
     * live with false positives. For example, <code>pattern=\a</code> and <code>str=b</code> will
     * yield <code>true</code>.</p>
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     *
     * @return whether or not a given path matches the start of a given pattern up to the first "".
     */
    protected static boolean matchPatternStart(String pattern, String str) {
        return SelectorUtil.matchPatternStart(pattern, str);
    }

    /**
     * Tests whether or not a given path matches the start of a given pattern up to the
     * first "".<p>This is not a general purpose test and should only be used if you can
     * live with false positives. For example, <code>pattern=\a</code> and <code>str=b</code> will
     * yield <code>true</code>.</p>
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     *
     * @return whether or not a given path matches the start of a given pattern up to the first "".
     */
    protected static boolean matchPatternStart(String pattern, String str, boolean isCaseSensitive) {
        return SelectorUtil.matchPatternStart(pattern, str, isCaseSensitive);
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     *
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code>
     *         otherwise.
     */
    protected static boolean matchPath(String pattern, String str) {
        return SelectorUtil.matchPath(pattern, str);
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The path to match, as a String. Must not be <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     *
     * @return <code>true</code> if the pattern matches against the string, or <code>false</code>
     *         otherwise.
     */
    protected static boolean matchPath(String pattern, String str, boolean isCaseSensitive) {
        return SelectorUtil.matchPath(pattern, str, isCaseSensitive);
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two
     * special characters:<br>
     * '' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not be
     *        <code>null</code>.
     *
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code>
     *         otherwise.
     */
    public static boolean match(String pattern, String str) {
        return SelectorUtil.match(pattern, str);
    }

    /**
     * Tests whether or not a string matches against a pattern. The pattern may contain two
     * special characters:<br>
     * '' means zero or more characters<br>
     * '?' means one and only one character
     *
     * @param pattern The pattern to match against. Must not be <code>null</code>.
     * @param str The string which must be matched against the pattern. Must not be
     *        <code>null</code>.
     * @param isCaseSensitive Whether or not matching should be performed case sensitively.
     *
     * @return <code>true</code> if the string matches against the pattern, or <code>false</code>
     *         otherwise.
     */
    protected static boolean match(String pattern, String str, boolean isCaseSensitive) {
        return SelectorUtil.match(pattern, str, isCaseSensitive);
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned
     * recursively. All '/' and '\' characters are replaced by <code>File.separatorChar</code>, so
     * the separator used need not match <code>File.separatorChar</code>.
     *
     * @param basedir The base directory to scan. Must not be <code>null</code>.
     */
    public void setZipURL(String zipURL) throws MalformedURLException {
        setSrc(new URL(zipURL));
    }

    /**
     * Sets the base directory to be scanned. This is the directory which is scanned
     * recursively.
     *
     * @param basedir The base directory for scanning. Should not be <code>null</code>.
     */
    public void setSrc(URL zipURL) {
        this.zipURL = zipURL;
    }

    /**
     * Returns the base directory to be scanned. This is the directory which is scanned
     * recursively.
     *
     * @return the base directory to be scanned
     */
    public URL getZipURL() {
        return zipURL;
    }

    /**
     * Sets whether or not the file system should be regarded as case sensitive.
     *
     * @param isCaseSensitive whether or not the file system should be regarded as a case sensitive
     *        one
     */
    public void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * Sets the list of include patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match
     * <code>File.separatorChar</code>.<p>When a pattern ends with a '/' or '\', "" is
     * appended.</p>
     *
     * @param includes A list of include patterns. May be <code>null</code>, indicating that all
     *        files should be included. If a non-<code>null</code> list is given, all elements
     *        must be non-<code>null</code>.
     */
    public void setIncludes(String[] includes) {
        if (includes == null) {
            this.includes = null;
        } else {
            this.includes = new String[includes.length];

            for (int i = 0; i < includes.length; i++) {
                String pattern;

                pattern = includes[i].replace('\\', '/');

                if (pattern.endsWith(File.separator)) {
                    pattern += "**";
                }

                this.includes[i] = pattern;
            }
        }
    }

    /**
     * Sets the list of exclude patterns to use. All '/' and '\' characters are replaced by
     * <code>File.separatorChar</code>, so the separator used need not match
     * <code>File.separatorChar</code>.<p>When a pattern ends with a '/' or '\', "" is
     * appended.</p>
     *
     * @param excludes A list of exclude patterns. May be <code>null</code>, indicating that no
     *        files should be excluded. If a non-<code>null</code> list is given, all elements
     *        must be non-<code>null</code>.
     */
    public void setExcludes(String[] excludes) {
        if (excludes == null) {
            this.excludes = null;
        } else {
            this.excludes = new String[excludes.length];

            for (int i = 0; i < excludes.length; i++) {
                String pattern;

                pattern = excludes[i].replace('\\', '/');

                if (pattern.endsWith(File.separator)) {
                    pattern += "**";
                }

                this.excludes[i] = pattern;
            }
        }
    }

    /**
     * Returns whether or not the scanner has included all the files or directories it has
     * come across so far.
     *
     * @return <code>true</code> if all files and directories which have been found so far have
     *         been included.
     */
    public boolean isEverythingIncluded() {
        return everythingIncluded;
    }

    /**
     * Scans the base directory for files which match at least one include pattern and
     * don't match any exclude patterns. If there are selectors then the files must pass muster
     * there, as well.
     *
     * @exception IllegalStateException if the base directory was set incorrectly (i.e. if it is
     *            <code>null</code>, doesn't exist, or isn't a directory).
     */
    public void scan() throws IllegalStateException, IOException {
        if (zipURL == null) {
            throw new IllegalStateException("No zipURL set");
        }

        if (includes == null) {
            // No includes supplied, so set it to 'matches all'
            includes    = new String[1];
            includes[0] = "**";
        }

        if (excludes == null) {
            excludes = new String[0];
        }

        filesIncluded    = new ArrayList();
        filesNotIncluded = new ArrayList();
        filesExcluded    = new ArrayList();
        dirsIncluded     = new ArrayList();
        dirsNotIncluded  = new ArrayList();
        dirsExcluded     = new ArrayList();

        if (isIncluded("")) {
            if (!isExcluded("")) {
                dirsIncluded.add("");
            } else {
                dirsExcluded.add("");
            }
        } else {
            dirsNotIncluded.add("");
        }

        scanZipFile(zipURL);
    }

    /**
     * Scans the given directory for files and directories. Found files and directories are
     * placed in their respective collections, based on the matching of includes, excludes, and
     * the selectors.  When a directory is found, it is scanned recursively.
     *
     * @param dir The directory to scan. Must not be <code>null</code>.
     * @param vpath The path relative to the base directory (needed to prevent problems with an
     *        absolute path when using dir). Must not be <code>null</code>.
     * @param fast Whether or not this call is part of a fast scan.
     *
     * @see #filesIncluded
     * @see #filesNotIncluded
     * @see #filesExcluded
     * @see #dirsIncluded
     * @see #dirsNotIncluded
     * @see #dirsExcluded
     * @see #slowScan
     */
    protected void scanZipFile(URL zipURL) throws IOException {
        InputStream    istream   = null;
        ZipInputStream zipStream = null;

        try {
            istream = zipURL.openStream();

            if (!(istream instanceof BufferedInputStream)) {
                istream = new BufferedInputStream(istream, 8192);
            }

            zipStream = new ZipInputStream(istream);

            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');

                if (name.endsWith(File.separator)) {
                    name = name.substring(0, name.length() - 1);
                }

                if (entry.isDirectory()) {
                    if (isIncluded(name)) {
                        if (!isExcluded(name)) {
                            dirsIncluded.add(name);
                        } else {
                            everythingIncluded = false;
                            dirsExcluded.add(name);
                        }
                    } else {
                        everythingIncluded = false;
                        dirsNotIncluded.add(name);
                    }
                } else {
                    if (isIncluded(name)) {
                        if (!isExcluded(name)) {
                            filesIncluded.add(name);
                        } else {
                            everythingIncluded = false;
                            filesExcluded.add(name);
                        }
                    } else {
                        everythingIncluded = false;
                        filesNotIncluded.add(name);
                    }
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }

            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Tests whether or not a name matches against at least one include pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     *
     * @return <code>true</code> when the name matches against at least one include pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean isIncluded(String name) {
        for (int i = 0; i < includes.length; i++) {
            if (matchPath(includes[i], name, isCaseSensitive)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tests whether or not a name matches against at least one exclude pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     *
     * @return <code>true</code> when the name matches against at least one exclude pattern, or
     *         <code>false</code> otherwise.
     */
    protected boolean isExcluded(String name) {
        for (int i = 0; i < excludes.length; i++) {
            if (matchPath(excludes[i], name, isCaseSensitive)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the names of the files which matched at least one of the include patterns
     * and none of the exclude patterns. The names are relative to the base directory.
     *
     * @return the names of the files which matched at least one of the include patterns and none
     *         of the exclude patterns.
     */
    public String[] getIncludedFiles() {
        return (String[]) filesIncluded.toArray(new String[filesIncluded.size()]);
    }

    /**
     * Returns the names of the files which matched none of the include patterns. The names
     * are relative to the base directory. This involves performing a slow scan if one has not
     * already been completed.
     *
     * @return the names of the files which matched none of the include patterns.
     *
     * @see #slowScan
     */
    public String[] getNotIncludedFiles() {
        return (String[]) filesNotIncluded.toArray(new String[filesNotIncluded.size()]);
    }

    /**
     * Returns the names of the files which matched at least one of the include patterns
     * and at least one of the exclude patterns. The names are relative to the base directory.
     * This involves performing a slow scan if one has not already been completed.
     *
     * @return the names of the files which matched at least one of the include patterns and at at
     *         least one of the exclude patterns.
     *
     * @see #slowScan
     */
    public String[] getExcludedFiles() {
        return (String[]) filesExcluded.toArray(new String[filesExcluded.size()]);
    }

    /**
     * Returns the names of the directories which matched at least one of the include
     * patterns and none of the exclude patterns. The names are relative to the base directory.
     *
     * @return the names of the directories which matched at least one of the include patterns and
     *         none of the exclude patterns.
     */
    public String[] getIncludedDirectories() {
        return (String[]) dirsIncluded.toArray(new String[dirsIncluded.size()]);
    }

    /**
     * Returns the names of the directories which matched none of the include patterns. The
     * names are relative to the base directory. This involves performing a slow scan if one has
     * not already been completed.
     *
     * @return the names of the directories which matched none of the include patterns.
     *
     * @see #slowScan
     */
    public String[] getNotIncludedDirectories() {
        return (String[]) dirsNotIncluded.toArray(new String[dirsNotIncluded.size()]);
    }

    /**
     * Returns the names of the directories which matched at least one of the include
     * patterns and at least one of the exclude patterns. The names are relative to the base
     * directory. This involves performing a slow scan if one has not already been completed.
     *
     * @return the names of the directories which matched at least one of the include patterns and
     *         at least one of the exclude patterns.
     *
     * @see #slowScan
     */
    public String[] getExcludedDirectories() {
        return (String[]) dirsExcluded.toArray(new String[dirsExcluded.size()]);
    }

    /**
     * Adds default exclusions to the current exclusions set.
     */
    public void addDefaultExcludes() {
        int      excludesLength = (excludes == null) ? 0
                                                     : excludes.length;
        String[] newExcludes;

        newExcludes             = new String[excludesLength + FileUtil.DEFAULT_EXCLUDES.length];

        if (excludesLength > 0) {
            System.arraycopy(excludes, 0, newExcludes, 0, excludesLength);
        }

        for (int i = 0; i < FileUtil.DEFAULT_EXCLUDES.length; i++) {
            newExcludes[i + excludesLength] = FileUtil.DEFAULT_EXCLUDES[i].replace('\\', '/');
        }

        excludes = newExcludes;
    }
}
