package com.alibaba.eclipse.plugin.webx.extension.hyperlink;

import static com.alibaba.eclipse.plugin.webx.extension.hyperlink.HyperlinkUtil.*;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.ContributionType;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.eclipse.plugin.webx.extension.schema.SpringExtSchemaResourceSet;

/**
 * 在XML中，如果某个tag或attribute的名称、namespace代表某个configuration
 * point，contribution，或spring pluggable schema，则打开它们。
 * 
 * @author Michael Zhou
 */
public class XmlElementHyperlinkDetector extends AbstractXMLHyperlinkDetector {
    @Override
    protected IHyperlink[] visitTagPrefix(IDocument document, IRegion region, String namespaceURI) {
        Schema schema = findSchema(document, namespaceURI);

        if (schema != null) {
            return createHyperlinks(region, schema);
        }

        return null;
    }

    @Override
    protected IHyperlink[] visitTagName(IDocument document, IRegion region, String namespaceURI) {
        return visitTagOrAttribute(document, region, namespaceURI, true);
    }

    private IHyperlink[] visitTagOrAttribute(IDocument document, IRegion region, String namespaceURI, boolean tag) {
        Schema schema = findSchema(document, namespaceURI);
        ConfigurationPoint cp = getConfigurationPoint(schema);
        String name = null;

        try {
            name = document.get(region.getOffset(), region.getLength());
        } catch (BadLocationException ignore) {
        }

        if (cp != null && name != null) {
            Contribution contrib = null;

            if (tag) {
                contrib = cp.getContribution(name, ContributionType.BEAN_DEFINITION_PARSER);

                if (contrib == null) {
                    contrib = cp.getContribution(name, ContributionType.BEAN_DEFINITION_DECORATOR);
                }
            } else {
                contrib = cp.getContribution(name, ContributionType.BEAN_DEFINITION_DECORATOR_FOR_ATTRIBUTE);
            }

            if (contrib != null) {
                Schema contribSchema = contrib.getSchemas().getVersionedSchema(schema.getVersion());

                if (contribSchema == null) {
                    contribSchema = contrib.getSchemas().getMainSchema();
                }

                return createHyperlinks(region, contribSchema);
            }
        }

        return null;
    }

    @Override
    protected IHyperlink[] visitAttrPrefix(IDocument document, IRegion region, String namespaceURI) {
        return visitTagPrefix(document, region, namespaceURI);
    }

    @Override
    protected IHyperlink[] visitAttrName(IDocument document, IRegion region, String namespaceURI) {
        return visitTagOrAttribute(document, region, namespaceURI, false);
    }

    private Schema findSchema(IDocument document, String namespaceURI) {
        SpringExtSchemaResourceSet schemas = SpringExtSchemaResourceSet.getInstance(document);

        if (schemas != null) {
            return schemas.findSchemaByUrl(namespaceURI);
        }

        return null;
    }
}
