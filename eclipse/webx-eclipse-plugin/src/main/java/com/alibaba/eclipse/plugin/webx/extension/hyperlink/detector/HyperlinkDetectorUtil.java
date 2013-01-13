package com.alibaba.eclipse.plugin.webx.extension.hyperlink.detector;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.SourceInfo;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.citrus.springext.support.ConfigurationPointSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSchemaSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSourceInfo;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.citrus.springext.support.SpringSchemasSourceInfo;
import com.alibaba.eclipse.plugin.webx.extension.hyperlink.SchemaHyperlink;
import com.alibaba.eclipse.plugin.webx.extension.hyperlink.URLHyperlink;

public class HyperlinkDetectorUtil {
    public static IHyperlink[] createHyperlinks(@NotNull IRegion region, @NotNull Schema schema,
                                                @NotNull IProject project) {
        // configuration point
        ConfigurationPoint cp = getConfigurationPoint(schema);

        if (cp != null) {
            return createConfigurationPointHyperlinks(region, schema, cp, project);
        }

        // contribution
        Contribution contrib = getContribution(schema);

        if (contrib != null) {
            return createContributionHyperlinks(region, schema, contrib, project);
        }

        // spring pluggable
        if (schema instanceof SpringPluggableSchemaSourceInfo) {
            return createSpringPluggableHyperlinks(region, schema, (SpringPluggableSchemaSourceInfo) schema, project);
        }

        // default
        return new IHyperlink[] { new SchemaHyperlink(region, schema, project) };
    }

    /**
     * 对于Configuration Point，打开如下链接：
     * <ul>
     * <li>打开生成的schema</li>
     * <li>打开定义文件</li>
     * </ul>
     */
    private static IHyperlink[] createConfigurationPointHyperlinks(IRegion region, final Schema schema,
                                                                   final ConfigurationPoint cp, IProject project) {
        IHyperlink link1 = new SchemaHyperlink(region, schema, project) {
            @Override
            public String getHyperlinkText() {
                return String.format("Open generated '%s' for Configuration Point '%s'", getSimpleName(schema),
                        cp.getNamespaceUri());
            }
        };

        ConfigurationPointSourceInfo definitionSource = ((ConfigurationPointSchemaSourceInfo) schema).getParent();
        URL definitionURL = getResourceURL(definitionSource);
        IHyperlink link2 = null;

        if (definitionURL != null) {
            link2 = new URLHyperlink(region, definitionURL, project) {
                @Override
                public String getHyperlinkText() {
                    return String.format("Open def-file '%s' for Configuration Point '%s'", getSimpleName(url),
                            cp.getNamespaceUri());
                }
            };
        }

        return new IHyperlink[] { link1, link2 };
    }

    /**
     * 对于Contribution，打开如下链接：
     * <ul>
     * <li>打开生成的schema</li>
     * <li>打开原始的schema</li>
     * <li>打开定义文件</li>
     * </ul>
     */
    private static IHyperlink[] createContributionHyperlinks(IRegion region, final Schema schema,
                                                             final Contribution contrib, IProject project) {
        IHyperlink link1 = new SchemaHyperlink(region, schema, project) {
            @Override
            public String getHyperlinkText() {
                return String.format("Open modified '%s' for Contribution '%s' - '%s'", getSimpleName(schema),
                        contrib.getName(), contrib.getConfigurationPoint().getNamespaceUri());
            }
        };

        URL originalSourceURL = getResourceURL((ContributionSchemaSourceInfo) schema);
        IHyperlink link2 = null;

        if (originalSourceURL != null) {
            link2 = new URLHyperlink(region, originalSourceURL, project) {
                @Override
                public String getHyperlinkText() {
                    return String.format("Open original '%s' for Contribution '%s' - '%s'", getSimpleName(url),
                            contrib.getName(), contrib.getConfigurationPoint().getNamespaceUri());
                }
            };
        }

        ContributionSourceInfo definitionSource = ((ContributionSchemaSourceInfo) schema).getParent();
        URL definitionURL = getResourceURL(definitionSource);
        IHyperlink link3 = null;

        if (definitionURL != null) {
            link3 = new URLHyperlink(region, definitionURL, project) {
                @Override
                public String getHyperlinkText() {
                    return String.format("Open def-file '%s' for Contribution '%s' - '%s'", getSimpleName(url),
                            contrib.getName(), contrib.getConfigurationPoint().getNamespaceUri());
                }
            };
        }

        return new IHyperlink[] { link1, link2, link3 };
    }

    /**
     * 对于Spring Pluggable，打开如下链接：
     * <ul>
     * <li>打开生成的schema</li>
     * <li>打开原始的schema</li>
     * <li>打开定义文件</li>
     * </ul>
     */
    private static IHyperlink[] createSpringPluggableHyperlinks(IRegion region, final Schema schema,
                                                                final SpringPluggableSchemaSourceInfo sourceInfo,
                                                                IProject project) {
        IHyperlink link1 = new SchemaHyperlink(region, schema, project) {
            @Override
            public String getHyperlinkText() {
                return String.format("Open modified '%s' - '%s'", getSimpleName(schema), schema.getTargetNamespace());
            }
        };

        URL originalSourceURL = getResourceURL(sourceInfo);
        IHyperlink link2 = null;

        if (originalSourceURL != null) {
            link2 = new URLHyperlink(region, originalSourceURL, project) {
                @Override
                public String getHyperlinkText() {
                    return String.format("Open original '%s' - '%s'", getSimpleName(url), schema.getTargetNamespace());
                }
            };
        }

        SpringSchemasSourceInfo definitionSource = sourceInfo.getParent();
        URL definitionURL = getResourceURL(definitionSource);
        IHyperlink link3 = null;

        if (definitionURL != null) {
            link3 = new URLHyperlink(region, definitionURL, project) {
                @Override
                public String getHyperlinkText() {
                    return String
                            .format("Open def-file '%s' for '%s'", getSimpleName(url), schema.getTargetNamespace());
                }
            };
        }

        return new IHyperlink[] { link1, link2, link3 };
    }

    public static ConfigurationPoint getConfigurationPoint(Schema schema) {
        if (schema instanceof ConfigurationPointSchemaSourceInfo) {
            ConfigurationPointSourceInfo cpsi = ((ConfigurationPointSchemaSourceInfo) schema).getParent();

            if (cpsi instanceof ConfigurationPoint) {
                return (ConfigurationPoint) cpsi;
            }
        }

        return null;
    }

    public static Contribution getContribution(Schema schema) {
        if (schema instanceof ContributionSchemaSourceInfo) {
            ContributionSourceInfo csi = ((ContributionSchemaSourceInfo) schema).getParent();

            if (csi instanceof Contribution) {
                return (Contribution) csi;
            }
        }

        return null;
    }

    private static URL getResourceURL(SourceInfo<?> sourceInfo) {
        if (sourceInfo != null && sourceInfo.getSource() instanceof Resource) {
            try {
                return ((Resource) sourceInfo.getSource()).getURL();
            } catch (IOException ignored) {
            }
        }

        return null;
    }

    private static String getSimpleName(Schema schema) {
        return new Path(schema.getName()).lastSegment();
    }

    private static String getSimpleName(URL url) {
        return new Path(url.toString()).lastSegment();
    }
}
