package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;

@SuppressWarnings("restriction")
public class SpringExtPluginUtil {
    private static final String JAR_FILE_PROTOCOL = "jar:file:"; //$NON-NLS-1$

    /**
     * 取得指定document所在的project。
     * <p/>
     * 参考实现：
     * {@link org.eclipse.jst.jsp.ui.internal.hyperlink.XMLJavaHyperlinkDetector#createHyperlink(String, IRegion, IDocument)}
     */
    public static IProject getProjectFromDocument(IDocument document) {
        // try file buffers
        ITextFileBuffer textFileBuffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);

        if (textFileBuffer != null) {
            IPath basePath = textFileBuffer.getLocation();

            if (basePath != null && !basePath.isEmpty()) {
                IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(basePath.segment(0));

                if (basePath.segmentCount() > 1 && project.isAccessible()) {
                    return project;
                }
            }
        }

        // fallback to SSE-specific knowledge
        IStructuredModel model = null;

        try {
            model = StructuredModelManager.getModelManager().getExistingModelForRead(document);

            if (model != null) {
                String baseLocation = model.getBaseLocation();

                // URL fixup from the taglib index record
                if (baseLocation.startsWith("jar:/file:")) { //$NON-NLS-1$
                    baseLocation = StringUtils.replace(baseLocation, "jar:/", "jar:"); //$NON-NLS-1$ //$NON-NLS-2$
                }

                /*
                 * Handle opened TLD files from JARs on the Java Build Path by
                 * finding a package fragment root for the same .jar file and
                 * opening the class from there. Note that this might be from a
                 * different Java project's build path than the TLD.
                 */
                if (baseLocation.startsWith(JAR_FILE_PROTOCOL)
                        && baseLocation.indexOf('!') > JAR_FILE_PROTOCOL.length()) {
                    String baseFile = baseLocation.substring(JAR_FILE_PROTOCOL.length(), baseLocation.indexOf('!'));
                    IPath basePath = new Path(baseFile);
                    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

                    for (IProject project : projects) {
                        try {
                            if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
                                IJavaProject javaProject = JavaCore.create(project);

                                if (javaProject.exists()) {
                                    IPackageFragmentRoot root = javaProject.findPackageFragmentRoot(basePath);

                                    if (root != null) {
                                        return javaProject.getProject();
                                    }
                                }
                            }
                        } catch (CoreException ignored) {
                        }
                    }
                } else {
                    IPath basePath = new Path(baseLocation);

                    if (basePath.segmentCount() > 1) {
                        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(basePath.segment(0));

                        if (project != null && project.isAccessible()) {
                            return project;
                        }
                    }
                }
            }
        } finally {
            if (model != null) {
                model.releaseFromRead();
            }
        }

        // Try get project from editor input
        IEditorInput input = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                .getEditorInput();

        return getProjectFromInput(input);
    }

    public static IProject getProjectFromInput(IEditorInput input) {
        if (input instanceof IFileEditorInput) {
            return ((IFileEditorInput) input).getFile().getProject();
        }

        return (IProject) input.getAdapter(IProject.class);
    }

    /**
     * 找到target file所对应的source file。
     */
    public static IFile findSourceFile(@NotNull IFile targetFile) {
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

    @Nullable
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
}
