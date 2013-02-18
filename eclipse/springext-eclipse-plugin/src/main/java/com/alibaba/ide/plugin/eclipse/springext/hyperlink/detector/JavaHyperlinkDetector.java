package com.alibaba.ide.plugin.eclipse.springext.hyperlink.detector;

import static com.alibaba.ide.plugin.eclipse.springext.SpringExtPluginUtil.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class JavaHyperlinkDetector extends AbstractContextualHyperlinkDetector {
    private final static Pattern classPattern = Pattern
            .compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*");

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IDocument document = textViewer.getDocument();

        if (document == null) {
            return null;
        }

        int offset = region.getOffset();
        IRegion lineInfo;
        String line;

        try {
            lineInfo = document.getLineInformationOfOffset(offset);
            line = document.get(lineInfo.getOffset(), lineInfo.getLength());
        } catch (BadLocationException e) {
            return null;
        }

        int offsetInLine = offset - lineInfo.getOffset();

        Matcher matcher = classPattern.matcher(line);
        String className = null;

        while (matcher.find()) {
            if (matcher.start() <= offsetInLine && offsetInLine < matcher.end()) {
                className = matcher.group();
                return createJavaHyperlinks(document, className, new Region(lineInfo.getOffset() + matcher.start(),
                        className.length()));
            }
        }

        return null;
    }

    private IHyperlink[] createJavaHyperlinks(IDocument document, String className, Region region) {
        IProject project = getFromContext(IProject.class);

        if (project != null) {
            IJavaProject javaProject = getJavaProject(project, true);

            if (javaProject != null && javaProject.exists()) {
                try {
                    IJavaElement element = javaProject.findType(className);

                    if (element != null && element.exists()) {
                        return new IHyperlink[] { new JavaElementHyperlink(region, element) };
                    }
                } catch (JavaModelException ignore) {
                }
            }
        }

        return null;
    }

    private static class JavaElementHyperlink implements IHyperlink {
        private final IJavaElement element;
        private final IRegion region;

        private JavaElementHyperlink(IRegion region, IJavaElement element) {
            this.region = region;
            this.element = element;
        }

        public IRegion getHyperlinkRegion() {
            return region;
        }

        public String getHyperlinkText() {
            return String.format("Open '%s'",
                    JavaElementLabels.getElementLabel(element, JavaElementLabels.ALL_POST_QUALIFIED));
        }

        public String getTypeLabel() {
            return null;
        }

        public void open() {
            try {
                JavaUI.openInEditor(element);
            } catch (Exception ignored) {
            }
        }
    }
}
