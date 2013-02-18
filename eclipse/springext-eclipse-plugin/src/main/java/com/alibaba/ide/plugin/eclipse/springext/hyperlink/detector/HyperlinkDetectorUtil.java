package com.alibaba.ide.plugin.eclipse.springext.hyperlink.detector;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jetbrains.annotations.NotNull;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.citrus.springext.Contribution;
import com.alibaba.citrus.springext.Schema;
import com.alibaba.citrus.springext.support.ConfigurationPointSchemaSourceInfo;
import com.alibaba.citrus.springext.support.ConfigurationPointSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSchemaSourceInfo;
import com.alibaba.citrus.springext.support.ContributionSourceInfo;
import com.alibaba.citrus.springext.support.SpringPluggableSchemaSourceInfo;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.ConfigurationPointHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.ContributionHyperlink;
import com.alibaba.ide.plugin.eclipse.springext.hyperlink.SpringPluggableSchemaHyperlink;

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
        return new IHyperlink[0];
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
        return new IHyperlink[] { new ConfigurationPointHyperlink(region, project, cp) };
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
        return new IHyperlink[] { new ContributionHyperlink(region, project, contrib) };
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
        return new IHyperlink[] { new SpringPluggableSchemaHyperlink(region, project, sourceInfo) };
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
}
