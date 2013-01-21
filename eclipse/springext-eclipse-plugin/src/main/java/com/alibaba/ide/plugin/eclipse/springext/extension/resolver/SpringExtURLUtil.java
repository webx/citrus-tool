package com.alibaba.ide.plugin.eclipse.springext.extension.resolver;

import static com.alibaba.citrus.util.StringEscapeUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPlugin.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alibaba.citrus.springext.Schema;

/**
 * 处理<code>springext://</code> URL协议。
 * 
 * @author Michael Zhou
 */
public class SpringExtURLUtil {
    public static boolean isSpringextURL(@NotNull URL url) {
        return URL_PROTOCOL.equals(url.getProtocol());
    }

    @NotNull
    public static String toSpringextURL(@NotNull IProject project, @NotNull Schema schema) {
        try {
            return URL_PROTOCOL + "://" + escapeURL(project.getName(), "UTF-8") + "/" + schema.getName();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("unbelievable", e);
        }
    }

    @Nullable
    public static IProject getProjectFromURL(@Nullable String url) {
        if (url != null) {
            try {
                return getProjectFromURL(new URL(url));
            } catch (MalformedURLException ignored) {
            }
        }

        return null;
    }

    @Nullable
    public static IProject getProjectFromURL(@NotNull URL url) {
        if (isSpringextURL(url)) {
            String projectName = null;

            try {
                projectName = unescapeURL(url.getHost(), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
            }

            if (projectName != null) {
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                return root.getProject(projectName);
            }
        }

        return null;
    }

    @Nullable
    public static String getSchemaNameFromURL(@NotNull URL url) {
        if (isSpringextURL(url)) {
            return url.getPath().replaceAll("^/+", "");
        } else {
            return null;
        }
    }
}
