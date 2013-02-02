package com.alibaba.ide.plugin.eclipse.springext;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

@SuppressWarnings("restriction")
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
        Platform.getBundle(org.eclipse.ecf.internal.filetransfer.Activator.PLUGIN_ID);

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
        registerImage(registry, "horizontal", "horizontal.gif");
        registerImage(registry, "vertical", "vertical.gif");
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
