package com.alibaba.eclipse.plugin.webx.util;

import static com.alibaba.citrus.springext.support.SchemaUtil.getAddPrefixTransformer;
import static com.alibaba.citrus.util.Assert.assertNotNull;
import static com.alibaba.citrus.util.CollectionUtil.createConcurrentHashMap;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.impl.SpringExtSchemaSet;

public class SpringExtSchemaResourceSet extends SpringExtSchemaSet {
    private static final Logger log = LoggerFactory.getLogger(SpringExtSchemaResourceSet.class);
    private static final ConcurrentMap<IProject, Future<SpringExtSchemaResourceSet>> projectCache = createConcurrentHashMap();

    @Nullable
    public static SpringExtSchemaResourceSet getInstance(IProject project) {
        assertNotNull(project, "project");

        Future<SpringExtSchemaResourceSet> future = projectCache.get(project);

        if (future == null) {
            final IJavaProject javaProject = getJavaProject(project);

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

        // Case 1: url represents a namespace url
        Set<Schema> namespaceSchemas = getNamespaceMappings().get(url);

        if (namespaceSchemas != null && !namespaceSchemas.isEmpty()) {
            schema = namespaceSchemas.iterator().next();
        }

        // Case 2: url represents a schema location
        if (schema == null) {
            schema = findSchema(url); // by name
        }

        return schema;
    }

    public SpringExtSchemaResourceSet(ClassLoader classLoader) {
        super(classLoader);
    }

    @Nullable
    private static IJavaProject getJavaProject(IProject project) {
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                return JavaCore.create(project);
            }
        } catch (CoreException ignored) {
        }

        return null;
    }

    private static SpringExtSchemaResourceSet computeSchemas(IJavaProject javaProject) {
        SpringExtSchemaResourceSet schemas;
        ClassLoader cl = createClassLoader(javaProject);

        if (cl != null) {
            schemas = new SpringExtSchemaResourceSet(cl);
            schemas.transformAll(getAddPrefixTransformer(schemas, "http://localhost:8080/schema/"));
            return schemas;
        }

        return null;
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
}
