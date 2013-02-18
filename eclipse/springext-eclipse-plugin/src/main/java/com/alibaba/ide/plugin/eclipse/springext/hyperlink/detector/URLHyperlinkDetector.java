package com.alibaba.ide.plugin.eclipse.springext.hyperlink.detector;

import static com.alibaba.ide.plugin.eclipse.springext.hyperlink.detector.HyperlinkDetectorUtil.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.alibaba.citrus.springext.Schema;
import com.alibaba.ide.plugin.eclipse.springext.schema.SchemaResourceSet;

/**
 * URL hyperlink detector.
 * <p/>
 * Copied from {@link org.eclipse.jface.text.hyperlink.URLHyperlinkDetector}
 * 
 * @author Michael Zhou
 */
public class URLHyperlinkDetector extends AbstractContextualHyperlinkDetector {
    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
        if (region == null || textViewer == null) {
            return null;
        }

        IDocument document = textViewer.getDocument();

        if (document == null) {
            return null;
        }

        int offset = region.getOffset();
        String urlString = null;
        IRegion lineInfo;
        String line;

        try {
            lineInfo = document.getLineInformationOfOffset(offset);
            line = document.get(lineInfo.getOffset(), lineInfo.getLength());
        } catch (BadLocationException ex) {
            return null;
        }

        int offsetInLine = offset - lineInfo.getOffset();

        char quote = 0;
        int urlOffsetInLine = 0;
        int urlLength = 0;

        int urlSeparatorOffset = line.indexOf("://"); //$NON-NLS-1$

        while (urlSeparatorOffset >= 0) {
            // URL protocol (left to "://")
            urlOffsetInLine = urlSeparatorOffset;
            char ch;

            do {
                urlOffsetInLine--;
                ch = ' ';

                if (urlOffsetInLine > -1) {
                    ch = line.charAt(urlOffsetInLine);
                }

                if (ch == '"' || ch == '\'') {
                    quote = ch;
                }
            } while (Character.isUnicodeIdentifierStart(ch));

            urlOffsetInLine++;

            // Right to "://"
            StringTokenizer tokenizer = new StringTokenizer(line.substring(urlSeparatorOffset + 3),
                    " \t\n\r\f<>\"\';=", false); //$NON-NLS-1$

            if (!tokenizer.hasMoreTokens()) {
                return null;
            }

            urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffsetInLine;

            if (offsetInLine >= urlOffsetInLine && offsetInLine <= urlOffsetInLine + urlLength) {
                break;
            }

            urlSeparatorOffset = line.indexOf("://", urlSeparatorOffset + 1); //$NON-NLS-1$
        }

        if (urlSeparatorOffset < 0) {
            return null;
        }

        if (quote != 0) {
            int endOffset = -1;
            int nextQuote = line.indexOf(quote, urlOffsetInLine);
            int nextWhitespace = line.indexOf(' ', urlOffsetInLine);

            if (nextQuote != -1 && nextWhitespace != -1) {
                endOffset = Math.min(nextQuote, nextWhitespace);
            } else if (nextQuote != -1) {
                endOffset = nextQuote;
            } else if (nextWhitespace != -1) {
                endOffset = nextWhitespace;
            }

            if (endOffset != -1) {
                urlLength = endOffset - urlOffsetInLine;
            }
        }

        // Set and validate URL string
        try {
            urlString = line.substring(urlOffsetInLine, urlOffsetInLine + urlLength);
            new URL(urlString);
        } catch (MalformedURLException ex) {
            urlString = null;
            return null;
        }

        IRegion urlRegion = new Region(lineInfo.getOffset() + urlOffsetInLine, urlLength);
        SchemaResourceSet schemas = getContext(SchemaResourceSet.class);

        if (schemas != null) {
            Schema schema = schemas.findSchemaByUrl(urlString);

            if (schema != null) {
                return createHyperlinks(urlRegion, schema, schemas.getProject());
            }
        }

        return null;
    }
}
