package com.alibaba.ide.plugin.eclipse.springext;

import static com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil.*;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.ide.plugin.eclipse.springext.resolver.SpringExtURLStreamHandler;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

public class SpringExtPlugin extends AbstractUIPlugin implements SpringExtConstant {
    private static SpringExtPlugin plugin;

    public SpringExtPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // init logger
        LogConfigurator.getConfigurator().configureDefault();

        // activate URL handler
        registerURLStreamHandler(context, URL_PROTOCOL, new SpringExtURLStreamHandler());

        // register listener
        SchemaResourceSet.registerChangedListener();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static SpringExtPlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        registerImage(registry, "plug", "plug.png");
        registerImage(registry, "socket", "socket.png");
        registerImage(registry, "spring", "spring-lighter.png");
        registerImage(registry, "spring-stroke", "spring-stroke.png");
        registerImage(registry, "horizontal", "horizontal.gif");
        registerImage(registry, "vertical", "vertical.gif");
        registerImage(registry, "expand", "expand.png");
        registerImage(registry, "collapse", "collapse.png");
        registerImage(registry, "clear-ns", "clear-ns.png");
        registerImage(registry, "upgrade32", "upgrade32.png");
        registerImage(registry, "tree", "tree.png");
        registerImage(registry, "list", "list.png");
    }

    private void registerImage(ImageRegistry registry, String key, String fileName) {
        try {
            IPath path = new Path("icons/" + fileName);
            URL url = FileLocator.find(getBundle(), path, null);

            if (url != null) {
                ImageDescriptor desc = ImageDescriptor.createFromURL(url);
                registry.put(key, desc);
            }
        } catch (Exception ignored) {
        }
    }
}
