package com.alibaba.ide.plugin.eclipse.springext.editor.config;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.*;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.wst.xml.ui.StructuredTextViewerConfigurationXML;

/**
 * 修改eclipse原来的XML编辑器的配置。
 * 
 * @author Michael Zhou
 */
public class StructuredTextViewerConfigurationSpringExtXML extends StructuredTextViewerConfigurationXML {
    private IAdaptable context;

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
     * <li>将<code>SpringExtConfig</code>对象作为context传递给hyperlink。</li>
     * </ul>
     */
    @Override
    protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
        Map<String, IAdaptable> targets = createHashMap();
        targets.put(SPRINGEXT_CONFIG_CONTENT_TYPE, getContext());
        return targets;
    }
}
