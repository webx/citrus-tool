package com.alibaba.eclipse.plugin.webx;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.alibaba.citrus.logconfig.LogConfigurator;
import com.alibaba.eclipse.plugin.webx.extension.schema.SpringExtSchemaResourceSet;

public class SpringExtEclipsePlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "webx-eclipse-plugin"; //$NON-NLS-1$
    public static final String URL_PROTOCOL = "springext";
    private static SpringExtEclipsePlugin plugin;

    public SpringExtEclipsePlugin() {
    }

    @SuppressWarnings("restriction")
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // init logger
        LogConfigurator.getConfigurator().configureDefault();

        // activate URL handler
        Platform.getBundle(org.eclipse.ecf.internal.filetransfer.Activator.PLUGIN_ID);

        // register listener
        SpringExtSchemaResourceSet.registerChangedListener();
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static SpringExtEclipsePlugin getDefault() {
        return plugin;
    }

    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
}
