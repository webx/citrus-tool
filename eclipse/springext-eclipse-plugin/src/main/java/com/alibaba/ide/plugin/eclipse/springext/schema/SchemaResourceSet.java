package com.alibaba.ide.plugin.eclipse.springext.schema;

import static com.alibaba.citrus.springext.support.SchemaUtil.*;
import static com.alibaba.citrus.util.Assert.*;
import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.StringUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;
import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;
import static org.eclipse.jdt.core.IJavaElementDelta.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.internet.cache.internal.Cache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.ResourceResolver;
import com.alibaba.citrus.springext.ResourceResolver.Resource;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ClasspathResourceResolver;
import com.alibaba.citrus.springext.support.SpringExtSchemaSet;
import com.alibaba.ide.plugin.eclipse.springext.schema.ISchemaSetChangeListener.SchemaSetChangeEvent;

@SuppressWarnings("restriction")
public class SchemaResourceSet extends SpringExtSchemaSet {
    private static final Resource[] NO_RESOURCE = new Resource[0];
    private static final Logger log = LoggerFactory.getLogger(SchemaResourceSet.class);
    private static final ConcurrentMap<IProject, Future<SchemaResourceSet>> projectCache = createConcurrentHashMap();
    private static final Map<ISchemaSetChangeListener, ISchemaSetChangeListener> schemaSetChangeListeners = createConcurrentHashMap();
    private static final AtomicBoolean hasError = new AtomicBoolean(false);
    private final IProject project;
    private final Throwable error;
    private final boolean successful;

    private SchemaResourceSet(ClassLoader classLoader, IProject project) {
        // 传递ResourceResolver而不是直接传ClassLoader，目的是避免创建类实例。
        super(new ClasspathResourceResolver(classLoader));
        this.project = project;
        this.error = null;
        this.successful = true;
    }

    private SchemaResourceSet(IProject project, Throwable error) {
        super(new ResourceResolver() {
            @Override
            @Nullable
            public Resource getResource(@NotNull String location) {
                return null;
            }

            @Override
            @NotNull
            public Resource[] getResources(@NotNull String locationPattern) throws IOException {
                return NO_RESOURCE;
            }
        });

        this.project = project;
        this.error = error;
        this.successful = false;
    }

    public IProject getProject() {
        return project;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorMessage() {
        return error == null ? "" : error.getMessage();
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

    private static class CloneableConfigurationPointItem extends ConfigurationPointItem implements Cloneable {
        public CloneableConfigurationPointItem(@NotNull String namespace, @NotNull Set<Schema> schemas,
                                               @NotNull ConfigurationPoint configurationPoint) {
            super(namespace, schemas, configurationPoint);
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                unexpectedException(e);
                return null;
            }
        }
    }

    @Override
    protected ConfigurationPointItem createConfigurationPointItem(@NotNull String namespace,
                                                                  @NotNull Set<Schema> schemas,
                                                                  @NotNull ConfigurationPoint configurationPoint) {
        return new CloneableConfigurationPointItem(namespace, schemas, configurationPoint);
    }

    @Override
    protected <C extends TreeItem> void addChildItem(@NotNull ParentOf<C> parent, @NotNull String key,
                                                     @NotNull C childItem) {
        if (parent instanceof ContributionItem && childItem instanceof CloneableConfigurationPointItem) {
            // 由于eclipse tree viewer对于重复项的处理有问题，例如同一个configuration point item出现在多个contribution下，
            // 下面对于contribution下面的重复的configuration point item创建不同的对象。
            ContributionItem contributionItem = (ContributionItem) parent;

            // Shallow copy, all copies share the same children instances.
            ConfigurationPointItem configurationPointItem = (ConfigurationPointItem) ((CloneableConfigurationPointItem) childItem)
                    .clone();

            super.addChildItem(contributionItem, key, configurationPointItem);
        } else {
            super.addChildItem(parent, key, childItem);
        }
    }

    /**
     * 取得schema set，如果在生成schemas时出错，则返回一个空的schema set。
     * <p/>
     * 通过访问<code>isSuccessful()</code>可以知道schemas是否成功生成，通过访问
     * <code>getError()</code> 可以取得导致生成出错的异常信息。
     */
    @NotNull
    public static SchemaResourceSet getInstance(IProject project) {
        IJavaProject javaProject = getJavaProject(project, true);

        if (javaProject != null) {
            return getInstanceInternal(javaProject, null);
        } else {
            return getInstanceInternal(null, (project == null ? "null" : project.getName()) + " is not a Java project");
        }
    }

    private static SchemaResourceSet getInstanceInternal(final IJavaProject javaProject, String errorMessage) {
        SchemaResourceSet schemas = null;

        if (javaProject == null) {
            schemas = createEmptySet(null, errorMessage);
        } else {
            IProject project = javaProject.getProject();
            Future<SchemaResourceSet> future = projectCache.get(project);

            if (future == null) {
                FutureTask<SchemaResourceSet> futureTask = new FutureTask<SchemaResourceSet>(
                        new Callable<SchemaResourceSet>() {
                            public SchemaResourceSet call() throws Exception {
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

            Throwable error = null;

            try {
                schemas = future.get();
            } catch (Exception e) {
                error = e;
            }

            if (schemas == null) {
                schemas = createEmptySet(project, error);
            }
        }

        boolean hasErrorThisTime = !schemas.isSuccessful();
        boolean hadErrorLastTime = hasError.getAndSet(hasErrorThisTime);

        // 防止重复报错
        if (hasErrorThisTime && !hadErrorLastTime) {
            final String message = schemas.getErrorMessage();

            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "Could not compute SchemaSet",
                            "Could not compute SchemaSet.\n\n"
                                    + "XML validation will fail until this problem is solved.\n\n" + message);
                }
            });
        }

        return schemas;
    }

    private static SchemaResourceSet createEmptySet(IProject project, String message) {
        return createEmptySet(project, new RuntimeException(message));
    }

    private static SchemaResourceSet createEmptySet(IProject project, Throwable error) {
        if (error instanceof ExecutionException) {
            error = error.getCause();
        }

        return new SchemaResourceSet(project, error);
    }

    @NotNull
    private static SchemaResourceSet computeSchemas(IJavaProject javaProject) {
        SchemaResourceSet schemas;

        try {
            schemas = new SchemaResourceSet(createClassLoader(javaProject), javaProject.getProject());
            schemas.transformAll(getAddPrefixTransformer(schemas, URL_PREFIX));
        } catch (Exception e) {
            schemas = createEmptySet(javaProject.getProject(), e);
        }

        return schemas;
    }

    @NotNull
    private static ClassLoader createClassLoader(IJavaProject javaProject) throws Exception {
        String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(javaProject);
        URL[] urls = new URL[classpath.length];

        for (int i = 0; i < classpath.length; i++) {
            urls[i] = new File(classpath[i]).toURI().toURL();
        }

        return new URLClassLoader(urls);
    }

    private static void notifySchemaSetChangeListeners(IProject project) {
        for (ISchemaSetChangeListener listener : schemaSetChangeListeners.keySet()) {
            listener.onSchemaSetChanged(new SchemaSetChangeEvent(project));
        }
    }

    public static void addSchemaSetChangeListener(ISchemaSetChangeListener listener) {
        schemaSetChangeListeners.put(listener, listener);
    }

    public static void removeSchemaSetChangeListener(ISchemaSetChangeListener listener) {
        schemaSetChangeListeners.remove(listener);
    }

    public static void registerChangedListener() {
        JavaCore.addElementChangedListener(new ClasspathChangeListener(), ElementChangedEvent.POST_CHANGE);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(),
                IResourceChangeEvent.POST_CHANGE);
    }

    private static boolean hasBits(int value, int mask) {
        return (value & mask) != 0;
    }

    private static void clearInternetCache() {
        Cache.getInstance().clear();
    }

    private static abstract class AbstractChangeListener {
        protected final void projectsChanged(Set<IProject> changedProjects) {
            if (!changedProjects.isEmpty()) {
                for (IProject project : changedProjects) {
                    projectCache.remove(project);
                    notifySchemaSetChangeListeners(project);
                }

                clearInternetCache();
            }
        }

        protected final void addChangedProject(IProject project, Set<IProject> changedProjects) {
            changedProjects.add(project);

            IJavaProject javaProject = getJavaProject(project, false);

            if (javaProject != null) {
                addIndirectProjects(javaProject, changedProjects);
            }
        }

        protected final void addChangedProject(IJavaElement element, Set<IProject> changedProjects) {
            // 直接的classpath改变
            if (element instanceof IJavaProject) {
                changedProjects.add(((IJavaProject) element).getProject());
            }

            // 间接的classpath改变
            addIndirectProjects(element, changedProjects);
        }

        private void addIndirectProjects(IJavaElement element, Set<IProject> changedProjects) {
            for (IProject cachedProject : projectCache.keySet()) {
                IJavaProject cachedJavaProject = getJavaProject(cachedProject, false);

                if (cachedJavaProject != null && cachedJavaProject.isOnClasspath(element)) {
                    changedProjects.add(cachedProject);
                }
            }
        }
    }

    /**
     * 观测classpath的改变。
     */
    private static class ClasspathChangeListener extends AbstractChangeListener implements IElementChangedListener {
        private final static int CLASSPATH_CHANGED_MASK = F_RESOLVED_CLASSPATH_CHANGED | F_CLASSPATH_CHANGED | F_CLOSED
                | F_OPENED;

        public void elementChanged(ElementChangedEvent event) {
            Set<IProject> changedProjects = createHashSet();

            visitDelta(event.getDelta(), changedProjects);

            projectsChanged(changedProjects);
        }

        private void visitDelta(IJavaElementDelta delta, Set<IProject> changedProjects) {
            if (delta != null) {
                if (hasBits(delta.getFlags(), CLASSPATH_CHANGED_MASK)) {
                    IJavaElement element = delta.getElement();

                    addChangedProject(element, changedProjects);
                }

                for (IJavaElementDelta child : delta.getAffectedChildren()) {
                    visitDelta(child, changedProjects);
                }
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
    private static class ResourceChangeListener extends AbstractChangeListener implements IResourceChangeListener {
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();
            final Set<IProject> changedProjects = createHashSet();

            if (delta != null) {
                try {
                    delta.accept(new IResourceDeltaVisitor() {
                        public boolean visit(IResourceDelta delta) throws CoreException {
                            IResource resource = delta.getResource();

                            if (resource instanceof IFile) {
                                fileChanged((IFile) resource, changedProjects);
                            }

                            return true;
                        }
                    });
                } catch (CoreException ignored) {
                }
            }

            projectsChanged(changedProjects);
        }

        private void fileChanged(IFile file, Set<IProject> changedProjects) {
            String fileName = trimToNull(file.getName());

            if (fileName != null) {
                if ("spring.schemas".equals(fileName)
                        || "spring.configuration-points".equals(fileName)
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_PARSER.getContributionsLocationSuffix())
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR
                                .getContributionsLocationSuffix())
                        || fileName.endsWith(ContributionType.BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE
                                .getContributionsLocationSuffix()) || fileName.toLowerCase().endsWith(".xsd")) {

                    addChangedProject(file.getProject(), changedProjects);
                }
            }
        }
    }
}
