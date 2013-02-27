package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.BasicConstant.*;
import static com.alibaba.citrus.util.StringUtil.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;

public class SpringExtPluginUtil {
    public static IProject getProjectFromInput(IEditorInput input) {
        return getProjectFromInput(input, true);
    }

    public static IProject getProjectFromInput(IEditorInput input, boolean required) {
        IProject project = null;

        if (input instanceof IFileEditorInput) {
            project = ((IFileEditorInput) input).getFile().getProject();
        } else {
            project = (IProject) input.getAdapter(IProject.class);
        }

        assertTrue(project != null || !required, "Could not get project from editor input: %s", input);

        return project;
    }

    /**
     * 找到target file所对应的source file。
     */
    public static IFile findSourceFile(IFile targetFile) {
        IJavaProject javaProject = getJavaProject(targetFile.getProject(), false);

        if (javaProject != null) {
            // 确保targetFile在output location
            IPath targetPath = targetFile.getFullPath();
            IPath outputLocation = null;

            try {
                outputLocation = javaProject.getOutputLocation();
            } catch (JavaModelException ignored) {
            }

            if (outputLocation != null) {
                // 取得相对路径
                IPath path = targetPath.makeRelativeTo(outputLocation);

                // 从每个source location中查找文件。
                IPackageFragmentRoot[] roots = null;

                try {
                    roots = javaProject.getPackageFragmentRoots();
                } catch (JavaModelException ignored) {
                }

                if (roots != null) {
                    for (IPackageFragmentRoot root : roots) {
                        if (root.getResource() instanceof IFolder) {
                            IFolder folder = (IFolder) root.getResource();
                            IFile sourceFile = folder.getFile(path);

                            if (sourceFile != null && sourceFile.exists()) {
                                return sourceFile;
                            }
                        }
                    }
                }
            }
        }

        return targetFile;
    }

    public static IJavaProject getJavaProject(IProject project, boolean create) {
        IJavaProject javaProject = null;

        if (project != null) {
            try {
                if (project.hasNature(JavaCore.NATURE_ID)) {
                    javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
                }

                if (javaProject == null && create) {
                    javaProject = JavaCore.create(project);
                }
            } catch (CoreException ignored) {
            }
        }

        return javaProject;
    }

    public static void logAndDisplay(IStatus status) {
        logAndDisplay(Display.getDefault().getActiveShell(), status);
    }

    public static void logAndDisplay(Shell shell, IStatus status) {
        logAndDisplay(shell, "Error", status);
    }

    public static void logAndDisplay(Shell shell, String title, IStatus status) {
        SpringExtPlugin.getDefault().getLog().log(status);

        if (status.getSeverity() == IStatus.INFO) {
            MessageDialog.openInformation(shell, title, status.getMessage());
        } else {
            MessageDialog.openError(shell, title, status.getMessage());
        }
    }

    public static URL getSourceURL(Object object) {
        assertTrue(object instanceof SourceInfo<?>, "not a source info");
        return getSourceURL((SourceInfo<?>) object);
    }

    public static URL getSourceURL(SourceInfo<?> sourceInfo) {
        Resource resource = (Resource) sourceInfo.getSource();
        URL url = null;

        try {
            url = resource.getURL();
        } catch (IOException ignored) {
        }

        return url;
    }

    public static String getLastSegment(String path) {
        if (path == null) {
            return null;
        }

        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String decode(String in) {
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

    public static String encode(String in) {
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
}
