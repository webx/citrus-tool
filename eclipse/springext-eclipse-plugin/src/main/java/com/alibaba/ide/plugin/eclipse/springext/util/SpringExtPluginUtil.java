package com.alibaba.ide.plugin.eclipse.springext.util;

import static com.alibaba.citrus.util.Assert.assertTrue;
import static com.alibaba.citrus.util.BasicConstant.EMPTY_STRING;
import static com.alibaba.citrus.util.StringUtil.trimToNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin;

public class SpringExtPluginUtil {
    public static void registerURLStreamHandler(BundleContext context, String protocol,
                                                AbstractURLStreamHandlerService service) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { protocol });
        context.registerService(URLStreamHandlerService.class.getName(), service, properties);
    }

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

    public static <T> T getFromContext(Object context, Class<T> type) {
        return getFromContext(context, type, true);
    }

    public static <T> T getFromContext(Object context, Class<T> type, boolean required) {
        IAdaptable adaptableContext = context instanceof IAdaptable ? (IAdaptable) context : null;
        T result = adaptableContext != null ? type.cast(adaptableContext.getAdapter(type)) : null;

        if (result == null && required) {
            throw new IllegalArgumentException("Could not get context object of type " + type.getName());
        }

        return result;
    }
    
    public static IProject getSelectProject(IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj != null && obj instanceof IResource) {
			IResource resource = (IResource) obj;
			return resource.getProject();
		}
		return null;
	}

	/**
	 * @param foldPath
	 * @param project
	 * 
	 *            刷新文件夹
	 */
	public static void refreshFolder(String foldPath, IProject project) {
		try {
			project.getFolder(foldPath).refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (Exception e) {
		}
	}

	/**
	 * @param foldPath
	 * @param project
	 * 
	 *            刷新文件夹
	 */
	public static void refreshFolder(IFolder folder) {
		try {
			folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (Exception e) {
		}
	}
	/**
	 * 检查"/src/main/resources/"是否存在，若不存在则创建 同时检查其是否在构建路径中，不在则加入到构建路径
	 * 
	 * @param project
	 */
	@SuppressWarnings("restriction")
	public static void checkSrcMetaExsit(IProject project) {
		if (null == project) {
			return;
		}
		// 如果文件夹不存在，则创建文件夹
		File projectFile = project.getLocation().toFile();
		String srcResourceFolderPath = "/src/main/resources/";
		String srcMetaFolderPath = "/src/main/resources/META-INF/";
		File fileSrcMeta = new File(projectFile, srcMetaFolderPath);
		if (!fileSrcMeta.exists()) {
			fileSrcMeta.mkdirs();
			refreshFolder(srcMetaFolderPath, project);
		}
		IFolder srcResourceFolder = project.getFolder(srcResourceFolderPath);
		IJavaProject javaProject = JavaCore.create(project);
		boolean srcMeTaClassPathExsit = false;
		IPackageFragmentRoot[] packageFragmentRoots = null;
		try {
			packageFragmentRoots = javaProject.getPackageFragmentRoots();
		} catch (JavaModelException e) {
		}
		if (null != packageFragmentRoots && packageFragmentRoots.length > 0) {
			for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
				IResource resource = packageFragmentRoot.getResource();
				if (null != resource && resource instanceof IFolder) {
					String resoucePath = resource.getProjectRelativePath().toString();
					if (resoucePath.toLowerCase().equals("src/main/resources")) {
						srcMeTaClassPathExsit = true;
						break;
					}
				}
			}
		}
		if (srcMeTaClassPathExsit) {
			return;
		}
		IPath path = srcResourceFolder.getFullPath();
		CPListElement cpListElement = new CPListElement(javaProject, IClasspathEntry.CPE_SOURCE, path,
		        srcResourceFolder);
		IClasspathEntry[] cpFormer = javaProject.readRawClasspath();
		IClasspathEntry[] cpAfter = null;
		int index = 0;
		if (cpFormer != null) {
			int cpSize = cpFormer.length + 1;
			cpAfter = new IClasspathEntry[cpSize];
			for (; index < cpFormer.length; index++) {
				cpAfter[index] = cpFormer[index];
			}
		} else {
			cpAfter = new IClasspathEntry[1];
		}
		IClasspathEntry ce = null;
		try {
			ce = cpListElement.getClasspathEntry();
		} catch (Exception e1) {
		}
		cpAfter[index] = ce;
		try {
			javaProject.setRawClasspath(cpAfter, javaProject.getOutputLocation(), new SubProgressMonitor(
			        new NullProgressMonitor(), 2));
		} catch (JavaModelException e2) {
		}

	}
	
	/**
	 * @param selection
	 * @return 通过selection获取到当前Javaproject
	 */
	public static IJavaProject getSelectJavaProject(IStructuredSelection selection){
		Object obj = selection.getFirstElement();
		if(obj != null && obj instanceof IResource){
			IResource resource = (IResource)obj;
			return JavaCore.create( resource.getProject());
		}
		return null;
	}
	
	public static IFile getFileHandle(String parent, String xsdName, IStructuredSelection selection) {
		IProject project = getSelectProject(selection);
		IFile file = project.getFile(parent + "/" + xsdName);
		return file;
	}
	
	/**
	 * 如果文件存在，则追加内容；否则，创建文件并写入内容
	 */
	public static void createFileIfNesscary(String parent, String fileName, final String content,
	        IStructuredSelection selection, final Shell shell, final String des, IWizardContainer container) {

		final IFile file = getFileHandle(parent, fileName, selection);
		if (file.exists()) {
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(file.getLocation().toString(), true);
				outStream.write(("\r\n" + content).getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (IOException e) {
					}
				}
			}
			try {
				getSelectProject(selection).getFolder(parent).refreshLocal(IResource.DEPTH_INFINITE,
				        new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CreateFileOperation op = new CreateFileOperation(file, null, new ByteArrayInputStream(
					        content.getBytes()), des);
					try {
						PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
						        .execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(shell));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			};
			try {
				container.run(true, true, op);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
	public static String formatXsd(String template,String... obj){
		String temp = getFileString(template);
		if(temp.length() > 0){
			MessageFormat format = new MessageFormat(temp);
			return format.format(obj);
		}
		return null;
	}

	/**
	 * @return 获取文件内容，返回String
	 */
	public static String getFileString(String template) {
		InputStream stream = SpringExtPluginUtil.class.getClassLoader().getResourceAsStream(template);
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(stream,"UTF-8"));
			String line = null;
			while((line = br.readLine()) != null){
				sb.append(line);
				sb.append(SpringExtConstant.LINE_BR);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return sb.toString();
	}
}
