package com.alibaba.eclipse.plugin.webx.extension.schema;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.eclipse.plugin.webx.SpringExtEclipsePlugin.*;
import static com.alibaba.eclipse.plugin.webx.util.PluginUtil.*;
import static org.eclipse.jdt.core.IJavaElementDelta.*;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.internet.cache.internal.Cache;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;
import com.alibaba.citrus.springext.support.ClasspathResourceResolver;

@SuppressWarnings("restriction")
public class SpringExtSchemaResourceSet extends SpringExtSchemaSet {
    private static final Logger log = LoggerFactory.getLogger(SpringExtSchemaResourceSet.class);
    private static final ConcurrentMap<IProject, Future<SpringExtSchemaResourceSet>> projectCache = createConcurrentHashMap();
    private final IProject project;

    @Nullable
    public static SpringExtSchemaResourceSet getInstance(IDocument document) {
        IProject project = getProjectFromDocument(document);

        if (project != null) {
            return getInstance(project);
        } else {
            return null;
        }
    }

    @Nullable
    public static SpringExtSchemaResourceSet getInstance(IProject project) {
        assertNotNull(project, "project");

        Future<SpringExtSchemaResourceSet> future = projectCache.get(project);

        if (future == null) {
            final IJavaProject javaProject = getJavaProject(project, true);

            if (javaProject == null) {
                return null;
            }

            FutureTask<SpringExtSchemaResourceSet> futureTask = new FutureTask<SpringExtSchemaResourceSet>(
                    new Callable<SpringExtSchemaResourceSet>() {
                        public SpringExtSchemaResourceSet call() throws Exception {
                            if (log.isDebugEnabled()) {
                                log.debug("Recompute schemas for project {}", javaProject.getProject().getName());
                            }

                            return computeSchemas(javaProject);
                        }
                    });

            future = projectCache.putIfAbsent(project, futureTask);

            if (future == null) {
                future = futureTask;
                futureTask.run();
            }
        }

        SpringExtSchemaResourceSet schemas = null;

        try {
            schemas = future.get();
        } catch (Exception ignored) {
        }

        if (schemas == null) {
            projectCache.remove(project);
        }

        return schemas;
    }

    @Nullable
    public Schema findSchemaByUrl(String url) {
        Schema schema = null;

        if (url != null) {
            // Case 1: url represents a namespace url
            Set<Schema> namespaceSchemas = getNamespaceMappings().get(url);

            if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
                schema = namespaceSchemas.iterator().next();
            }

            // Case 2: url represents a schema location
            if (schema == null) {
                schema = findSchema(url); // by name
            }
        }

        return schema;
    }

    public SpringExtSchemaResourceSet(ClassLoader classLoader, IProject project) {
        // 传递ResourceResolver而不是直接传ClassLoader，目的是避免创建类实例。
        super(new ClasspathResourceResolver(classLoader));
        this.project = project;
    }

    private static SpringExtSchemaResourceSet computeSchemas(IJavaProject javaProject) {
        SpringExtSchemaResourceSet schemas;
        ClassLoader cl = createClassLoader(javaProject);

        if (cl != null) {
            schemas = new SpringExtSchemaResourceSet(cl, javaProject.getProject());
            schemas.transformAll(getAddPrefixTransformer(schemas, URL_PREFIX));
            return schemas;
        }

        return null;
    }

    public IProject getProject() {
        return project;
    }

    @Nullable
    private static ClassLoader createClassLoader(IJavaProject javaProject) {
        URL[] urls;

        try {
            String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
            urls = new URL[classpath.length];

            for (int i = 0; i < classpath.length; i++) {
                urls[i] = new File(classpath[i]).toURI().toURL();
            }
        } catch (Exception e) {
            return null;
        }

        return new URLClassLoader(urls);
    }

    public static void registerChangedListener() {
        JavaCore.addElementChangedListener(new ClasspathChangeListener(), ElementChangedEvent.POST_CHANGE);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(),
                IResourceChangeEvent.POST_CHANGE);
    }

    private static boolean hasBits(int value, int mask) {
        return (value & mask) != 0;
    }

    private static void clearCache() {
        Cache.getInstance().clear();
    }

    /**
     * 观测classpath的改变。
     */
    private static class ClasspathChangeListener implements IElementChangedListener {
        private final static int CLASSPATH_CHANGED_MASK = F_RESOLVED_CLASSPATH_CHANGED | F_CLASSPATH_CHANGED | F_CLOSED
                | F_OPENED;

        public void elementChanged(ElementChangedEvent event) {
            visitDelta(event.getDelta());
        }

        private void visitDelta(IJavaElementDelta delta) {
            boolean changed = false;

            if (delta != null) {
                if (hasBits(delta.getFlags(), CLASSPATH_CHANGED_MASK)) {
                    IJavaElement element = delta.getElement();

                    // 直接的classpath改变
                    if (element instanceof IJavaProject) {
                        IProject project = ((IJavaProject) element).getProject();
                        projectCache.remove(project);
                        changed = true;
                    }

                    // 间接的classpath改变
                    for (Iterator<IProject> i = projectCache.keySet().iterator(); i.hasNext();) {
                        IProject project = i.next();
                        IJavaProject javaProject = getJavaProject(project, false);

                        if (javaProject != null) {
                            if (javaProject.isOnClasspath(element)) {
                                i.remove();
                                changed = true;
                            }
                        }
                    }
                }

                for (IJavaElementDelta child : delta.getAffectedChildren()) {
                    visitDelta(child);
                }
            }

            if (changed) {
                clearCache();
            }
        }
    }

    /**
     * 监视以下文件的创建。如果发现了，则更新cache。
     * <ul>
     * <li>spring.schemas</li>
     * <li>spring.configuration-points</li>
     * <li>*.bean-definition-parsers</li>
     * <li>*.bean-definition-decorators</li>
     * <li>*.bean-definition-decorators-for-attribute</li>
     * <li>*.xsd</li>
     * </ul>
     */
    private static class ResourceChangeListener implements IResourceChangeListener {
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();

            if (delta != null) {
                try {
                    delta.accept(new IResourceDeltaVisitor() {
                        public boolean visit(IResourceDelta delta) throws CoreException {
                            IResource resource = delta.getResource();

                            if (resource instanceof IFile) {
                                fileChanged((IFile) resource);
                            }

                            return true;
                        }
                    });
                } catch (CoreException ignored) {
                }
            }
        }

        private void fileChanged(IFile file) {
            String fileName = trimToNull(file.getName());

            if (fileName != null) {
                if ("spring.schemas".equals(fileName)
                        || "spring.configuration-points".equals(fileName)
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_PARSER.getContributionsLocationSuffix())
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR
                                .getContributionsLocationSuffix())
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE
                                .getContributionsLocationSuffix()) || fileName.toLowerCase().endsWith(".xsd")) {
                    projectChanged(file.getProject());
                }
            }
        }

        private void projectChanged(IProject project) {
            // 直接的classpath改变
            projectCache.remove(project);

            // 间接的classpath改变
            IJavaProject javaProject = getJavaProject(project, false);

            if (javaProject != null) {
                for (Iterator<IProject> i = projectCache.keySet().iterator(); i.hasNext();) {
                    IProject cachedProject = i.next();
                    IJavaProject cachedJavaProject = getJavaProject(cachedProject, false);

                    if (cachedJavaProject != null) {
                        if (cachedJavaProject.isOnClasspath(javaProject)) {
                            i.remove();
                        }
                    }
                }
            }

            clearCache();
        }
    }
}
