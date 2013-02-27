package com.alibaba.ide.plugin.eclipse.springext.editor;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.IPropertiesFilePartitions;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileSourceViewerConfiguration;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 扩展原properties编辑器，使之可以取得一个<code>IAdaptable</code>上下文对象。
 * 
 * @author Michael Zhou
 */
@SuppressWarnings("restriction")
public class ContextualPropertiesFileEditor extends PropertiesFileEditor {
    private final IAdaptable context;

    public ContextualPropertiesFileEditor(IAdaptable context) {
        this.context = context;
    }

    public IAdaptable getContext() {
        return context;
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setSourceViewerConfiguration(new PropertiesFileSourceViewerConfigurationSpringExt(this));
    }

    @Override
    protected final void setSourceViewerConfiguration(SourceViewerConfiguration config) {
        if (config instanceof PropertiesFileSourceViewerConfigurationSpringExt) {
            ((PropertiesFileSourceViewerConfigurationSpringExt) config).setContext(getContext());
        }

        super.setSourceViewerConfiguration(config);
    }

    /**
     * 修改eclipse原来的properties编辑器的配置。
     */
    public static class PropertiesFileSourceViewerConfigurationSpringExt extends
            PropertiesFileSourceViewerConfiguration {
        private IAdaptable context;

        public PropertiesFileSourceViewerConfigurationSpringExt(ITextEditor editor) {
            super(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), JavaPlugin.getDefault()
                    .getCombinedPreferenceStore(), editor, IPropertiesFilePartitions.PROPERTIES_FILE_PARTITIONING);
        }

        public IAdaptable getContext() {
            return context;
        }

        public void setContext(IAdaptable context) {
            this.context = context;
        }

        @Override
        public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
            return super.getHyperlinkDetectors(sourceViewer);
        }

        /**
         * <ul>
         * <li>将default text editor对应的detectors排除，因为SpringExt中的URL不需要由默认的URL
         * hyperlink来解析。</li>
         * <li>将<code>IAdaptable</code>对象（例如：<code>SpringExtConfigData</code>
         * ）对象作为context传递给hyperlink。</li>
         * </ul>
         */
        @Override
        protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
            Map<String, IAdaptable> targets = createHashMap();

            for (String contentType : SUPPORTED_CONTENT_TYPES) {
                targets.put(contentType, getContext());
            }

            return targets;
        }
    }
}
