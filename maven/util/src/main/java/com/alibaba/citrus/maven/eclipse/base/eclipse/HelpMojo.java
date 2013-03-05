/*
 * Copyright (c) 2002-2013 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.maven.eclipse.base.eclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Display help information on maven-eclipse-plugin.<br/> Call <pre>  mvn eclipse:help -Ddetail=true -Dgoal=&lt;goal-name&gt;</pre> to display parameter details.
 *
 * @author org.apache.maven.tools.plugin.generator.PluginHelpGenerator (version 2.4.3)
 * @version generated on Fri Jan 13 09:09:58 CST 2012
 * @goal help
 * @requiresProject false
 */
public class HelpMojo
        extends AbstractMojo {
    /**
     * If <code>true</code>, display all settable properties for each goal.
     *
     * @parameter expression="${detail}" default-value="false"
     */
    private boolean detail;

    /**
     * The name of the goal for which to show help. If unspecified, all goals will be displayed.
     *
     * @parameter expression="${goal}"
     */
    private java.lang.String goal;

    /**
     * The maximum length of a display line, should be positive.
     *
     * @parameter expression="${lineLength}" default-value="80"
     */
    private int lineLength;

    /**
     * The number of spaces per indentation level, should be positive.
     *
     * @parameter expression="${indentSize}" default-value="2"
     */
    private int indentSize;

    /** {@inheritDoc} */
    public void execute()
            throws MojoExecutionException {
        if (lineLength <= 0) {
            getLog().warn("The parameter 'lineLength' should be positive, using '80' as default.");
            lineLength = 80;
        }
        if (indentSize <= 0) {
            getLog().warn("The parameter 'indentSize' should be positive, using '2' as default.");
            indentSize = 2;
        }

        StringBuffer sb = new StringBuffer();

        append(sb, "org.apache.maven.plugins:maven-eclipse-plugin:2.9", 0);
        append(sb, "", 0);

        append(sb, "Maven Eclipse Plugin 2.9", 0);
        append(sb,
               "The Eclipse Plugin is used to generate Eclipse IDE files (.project, .classpath and the .settings folder) from a POM.",
               1);
        append(sb, "", 0);

        if (goal == null || goal.length() <= 0) {
            append(sb, "This plugin has 13 goals:", 0);
            append(sb, "", 0);
        }

        if (goal == null || goal.length() <= 0 || "add-maven-repo".equals(goal)) {
            append(sb, "eclipse:add-maven-repo", 0);
            append(sb, "Deprecated. Use configure-workspace goal instead.", 1);
            if (detail) {
                append(sb, "", 0);
                append(sb,
                       "Adds the classpath variable M2_REPO to Eclipse. DEPRECATED. Replaced by eclipse:configure-workspace.",
                       1);
            }
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "workspace", 2);
                append(sb, "Directory location of the Eclipse workspace.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "clean".equals(goal)) {
            append(sb, "eclipse:clean", 0);
            append(sb, "Deletes the .project, .classpath, .wtpmodules files and .settings folder used by Eclipse.", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb, "additional generic configuration files for eclipse", 3);
                append(sb, "", 0);

                append(sb, "basedir", 2);
                append(sb, "The root directory of the project", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "Packaging for the current project.", 3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "configure-workspace".equals(goal)) {
            append(sb, "eclipse:configure-workspace", 0);
            append(sb,
                   "Configures The following Eclipse Workspace features:\n-\tAdds the classpath variable MAVEN_REPO to Eclipse.\n-\tOptionally load Eclipse code style file via a URL.\n",
                   1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "workspace", 2);
                append(sb, "Directory location of the Eclipse workspace.", 3);
                append(sb, "", 0);

                append(sb, "workspaceActiveCodeStyleProfileName", 2);
                append(sb,
                       "Name of a profile in workspaceCodeStylesURL to activate. Default is the first profile name in the code style file in workspaceCodeStylesURL",
                       3);
                append(sb, "", 0);

                append(sb, "workspaceCodeStylesURL", 2);
                append(sb, "Point to a URL containing code styles content.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "eclipse".equals(goal)) {
            append(sb, "eclipse:eclipse", 0);
            append(sb,
                   "Generates the following eclipse configuration files:\n-\t.project and .classpath files\n-\t.setting/org.eclipse.jdt.core.prefs with project specific compiler settings\n-\tvarious configuration files for WTP (Web Tools Project), if the parameter wtpversion is set to a valid version (WTP configuration is not generated by default)\nIf this goal is run on a multiproject root, dependencies between modules will be configured as direct project dependencies in Eclipse (unless useProjectReferences is set to false).",
                   1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "addGroupIdToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the groupId of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "addVersionToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the version number of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalBuildcommands", 2);
                append(sb,
                       "List of eclipse build commands to be added to the default ones. Old style:\n<additionalBuildcommands>\n<buildcommand>org.springframework.ide.eclipse.core.springbuilder</buildcommand>\n</additionalBuildcommands>\nNew style:\n<additionalBuildcommands>\n<buildCommand>\n<name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n<triggers>auto,full,incremental,</triggers>\n<arguments>\n<LaunchConfigHandle>&lt;project&gt;./externalToolBuilders/MavenBuilder.launch</LaunchConfighandle>\n</arguments>\n</buildCommand>\n</additionalBuildcommands>\nNote the difference between buildcommand and buildCommand. You can mix and match old and new-style configuration entries.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb,
                       "Allow to configure additional generic configuration files for eclipse that will be written out to disk when running eclipse:eclipse. FOr each file you can specify the name and the text content.\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<content>\n<![CDATA[<fileset-config\u00a0file-format-version=\'1.2.0\'\u00a0simple-config=\'true\'>\n<fileset\u00a0name=\'all\'\u00a0enabled=\'true\'\u00a0check-config-name=\'acme\u00a0corporate\u00a0style\'\u00a0local=\'false\'>\n<file-match-pattern\u00a0match-pattern=\'.\'\u00a0include-pattern=\'true\'/>\n</fileset>\n<filter\u00a0name=\'NonSrcDirs\'\u00a0enabled=\'true\'/>\n</fileset-config>]]>\n</content>\n</file>\n</additionalConfig>\n</configuration>\n</plugin>\nInstead of the content you can also define (from version 2.5) an url to download the file :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<url>http://some.place.org/path/to/file</url>\n</file>\n</additionalConfig>\n</configuration>\nor a location :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<location>/checkstyle-config.xml</location>\n</file>\n</additionalConfig>\n</configuration>\n<dependencies>\n<!--\u00a0The\u00a0file\u00a0defined\u00a0in\u00a0the\u00a0location\u00a0is\u00a0stored\u00a0in\u00a0this\u00a0dependency\u00a0-->\n<dependency>\n<groupId>eclipsetest</groupId>\n<artifactId>checkstyle-config</artifactId>\n<version>1.0</version>\n</dependency>\n</dependencies>\n</plugin>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectFacets", 2);
                append(sb,
                       "List of eclipse project facets to be added to the default ones.\n<additionalProjectFacets>\n<jst.jsf>1.1<jst.jsf/>\n</additionalProjectFacets>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectnatures", 2);
                append(sb,
                       "List of eclipse project natures to be added to the default ones.\n<additionalProjectnatures>\n<projectnature>org.springframework.ide.eclipse.core.springnature</projectnature>\n</additionalProjectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "ajdtVersion (Default: none)", 2);
                append(sb,
                       "The version of AJDT for which configuration files will be generated. The default value is \'1.5\', supported versions are \'none\' (AJDT support disabled), \'1.4\', and \'1.5\'.",
                       3);
                append(sb, "", 0);

                append(sb, "buildOutputDirectory (Default: ${project.build.outputDirectory})", 2);
                append(sb, "The default output directory", 3);
                append(sb, "", 0);

                append(sb, "buildcommands", 2);
                append(sb,
                       "List of eclipse build commands. By default the org.eclipse.jdt.core.javabuilder builder plus the needed WTP builders are added. If you specify any configuration for this parameter, only those buildcommands specified will be used; the defaults won\'t be added. Use the additionalBuildCommands parameter for that. Configuration example: Old style:\n<buildcommands>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilder</buildcommand>\n<buildcommand>org.eclipse.jdt.core.javabuilder</buildcommand>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilderDependencyResolver</buildcommand>\n</buildcommands>\nFor new style, see additionalBuildCommands.",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainers", 2);
                append(sb,
                       "List of container classpath entries. By default the org.eclipse.jdt.launching.JRE_CONTAINER classpath container is added. Configuration example:\n<classpathContainers>\n<classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>\n<classpathContainer>org.eclipse.jst.server.core.container/org.eclipse.jst.server.tomcat.runtimeTarget/Apache\u00a0Tomcat\u00a0v5.5</classpathContainer>\n<classpathContainer>org.eclipse.jst.j2ee.internal.web.container/artifact</classpathContainer>\n</classpathContainers>\n",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainersLast (Default: false)", 2);
                append(sb,
                       "Put classpath container entries last in eclipse classpath configuration. Note that this behaviour, although useful in situations were you want to override resources found in classpath containers, will made JRE classes loaded after 3rd party jars, so enabling it is not suggested.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadJavadocs", 2);
                append(sb,
                       "Enables/disables the downloading of javadoc attachments. Defaults to false. When this flag is true remote repositories are checked for javadocs: in order to avoid repeated check for unavailable javadoc archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadSources", 2);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. When this flag is true remote repositories are checked for sources: in order to avoid repeated check for unavailable source archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseDownloadSources", 2);
                append(sb, "Deprecated. use downloadSources", 3);
                append(sb, "", 0);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. DEPRECATED - use downloadSources",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseProjectDir", 2);
                append(sb, "Eclipse workspace directory.", 3);
                append(sb, "", 0);

                append(sb, "excludes", 2);
                append(sb,
                       "List of artifacts, represented as groupId:artifactId, to exclude from the eclipse classpath, being provided by some eclipse classPathContainer.",
                       3);
                append(sb, "", 0);

                append(sb, "forceRecheck", 2);
                append(sb,
                       "Enables/disables the rechecking of the remote repository for downloading source/javadoc attachments. Defaults to false. When this flag is true and the source or javadoc attachment has a status cache to indicate that it is not available, then the remote repository will be rechecked for a source or javadoc attachment and the status cache updated to reflect the new state.",
                       3);
                append(sb, "", 0);

                append(sb, "jeeversion", 2);
                append(sb,
                       "The plugin is often capable in predicting the required jee version based on the dependencies of the project. By setting this parameter to one of the jeeversion options the version will be locked. \njeeversion\nEJB version\nServlet version\nJSP version\n6.0\n3.1\n3.0\n2.2\n5.0\n3.0\n2.5\n2.1\n1.4\n2.1\n2.4\n2.0\n1.3\n2.0\n2.3\n1.2\n1.2\n1.1\n2.2\n1.1",
                       3);
                append(sb, "", 0);

                append(sb, "limitProjectReferencesToWorkspace (Default: false)", 2);
                append(sb,
                       "Limit the use of project references to the current workspace. No project references will be created to projects in the reactor when they are not available in the workspace.",
                       3);
                append(sb, "", 0);

                append(sb, "linkedResources", 2);
                append(sb,
                       "A list of links to local files in the system. A configuration like this one in the pom :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<linkedResources>\n<linkedResource>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</linkedResource>\n</linkedResources>\n</configuration>\n</plugin>\nwill produce in the .project :\n<linkedResources>\n<link>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</link>\n</linkedResources>\n",
                       3);
                append(sb, "", 0);

                append(sb, "manifest (Default: ${basedir}/META-INF/MANIFEST.MF)", 2);
                append(sb, "The relative path of the manifest file", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "The project packaging.", 3);
                append(sb, "", 0);

                append(sb, "pde (Default: false)", 2);
                append(sb,
                       "Is it an PDE project? If yes, the plugin adds the necessary natures and build commands to the .project file. Additionally it copies all libraries to a project local directory and references them instead of referencing the files in the local Maven repository. It also ensured that the \'Bundle-Classpath\' in META-INF/MANIFEST.MF is synchronized.",
                       3);
                append(sb, "", 0);

                append(sb, "projectNameTemplate", 2);
                append(sb,
                       "Allows configuring the name of the eclipse projects. This property if set wins over addVersionToProjectName and addGroupIdToProjectName You can use [groupId], [artifactId] and [version] variables. eg. [groupId].[artifactId]-[version]",
                       3);
                append(sb, "", 0);

                append(sb, "projectnatures", 2);
                append(sb,
                       "List of eclipse project natures. By default the org.eclipse.jdt.core.javanature nature plus the needed WTP natures are added. Natures added using this property replace the default list.\n<projectnatures>\n<projectnature>org.eclipse.jdt.core.javanature</projectnature>\n<projectnature>org.eclipse.wst.common.modulecore.ModuleCoreNature</projectnature>\n</projectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);

                append(sb, "sourceExcludes", 2);
                append(sb,
                       "List of exclusions to add to the source directories on the classpath. Adds excluding=\'\' to the classpathentry of the eclipse .classpath file. [MECLIPSE-104]",
                       3);
                append(sb, "", 0);

                append(sb, "sourceIncludes", 2);
                append(sb,
                       "List of inclusions to add to the source directories on the classpath. Adds including=\'\' to the classpathentry of the eclipse .classpath file.\nJava projects will always include \'**/*.java\'\n\nAjdt projects will always include \'**/*.aj\'\n\n[MECLIPSE-104]\n",
                       3);
                append(sb, "", 0);

                append(sb, "testSourcesLast (Default: false)", 2);
                append(sb,
                       "Whether to place test resources after main resources. Note that the default behavior of Maven version 2.0.8 or later is to have test dirs before main dirs in classpath so this is discouraged if you need to reproduce the maven behavior during tests. The default behavior is also changed in eclipse plugin version 2.6 in order to better match the maven one. Switching to \'test source last\' can anyway be useful if you need to run your application in eclipse, since there is no concept in eclipse of \'phases\' with different set of source dirs and dependencies like we have in maven.",
                       3);
                append(sb, "", 0);

                append(sb, "useProjectReferences (Default: true)", 2);
                append(sb,
                       "When set to false, the plugin will not create sub-projects and instead reference those sub-projects using the installed package in the local repository",
                       3);
                append(sb, "", 0);

                append(sb, "workspace", 2);
                append(sb,
                       "This eclipse workspace is read and all artifacts detected there will be connected as eclipse projects and will not be linked to the jars in the local repository. Requirement is that it was created with the similar wtp settings as the reactor projects, but the project name template my differ. The pom\'s in the workspace projects may not contain variables in the artefactId, groupId and version tags. If workspace is not defined, then an attempt to locate it by checking up the directory hierarchy will be made.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpContextName", 2);
                append(sb,
                       "JEE context name of the WTP module. ( ex. WEB context name ). You can use \'ROOT\' if you want to map the webapp to the root context.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpapplicationxml (Default: false)", 2);
                append(sb, "Must the application files be written for ear projects in a separate directory.", 3);
                append(sb, "", 0);

                append(sb, "wtpdefaultserver", 2);
                append(sb, "What WTP defined server to use for deployment informations.", 3);
                append(sb, "", 0);

                append(sb, "wtpmanifest (Default: false)", 2);
                append(sb,
                       "Must the manifest files be written for java projects so that that the jee classpath for wtp is correct.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpversion (Default: none)", 2);
                append(sb,
                       "The version of WTP for which configuration files will be generated. The default value is \'none\' (don\'t generate WTP configuration), supported versions are \'R7\', \'1.0\', \'1.5\' and \'2.0\'",
                       3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "help".equals(goal)) {
            append(sb, "eclipse:help", 0);
            append(sb,
                   "Display help information on maven-eclipse-plugin.\nCall\n\u00a0\u00a0mvn\u00a0eclipse:help\u00a0-Ddetail=true\u00a0-Dgoal=<goal-name>\nto display parameter details.",
                   1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "detail (Default: false)", 2);
                append(sb, "If true, display all settable properties for each goal.", 3);
                append(sb, "", 0);

                append(sb, "goal", 2);
                append(sb,
                       "The name of the goal for which to show help. If unspecified, all goals will be displayed.",
                       3);
                append(sb, "", 0);

                append(sb, "lineLength (Default: 80)", 2);
                append(sb, "The maximum length of a display line, should be positive.", 3);
                append(sb, "", 0);

                append(sb, "indentSize (Default: 2)", 2);
                append(sb, "The number of spaces per indentation level, should be positive.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "install-plugins".equals(goal)) {
            append(sb, "eclipse:install-plugins", 0);
            append(sb, "Install plugins resolved from the Maven repository system into an Eclipse instance.", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "eclipseDir", 2);
                append(sb, "This is the installed base directory of the Eclipse instance you want to modify.", 3);
                append(sb, "", 0);

                append(sb, "overwrite (Default: false)", 2);
                append(sb,
                       "Determines whether this mojo leaves existing installed plugins as-is, or overwrites them.",
                       3);
                append(sb, "", 0);

                append(sb, "pluginDependencyTypes (Default: jar)", 2);
                append(sb,
                       "Comma-delimited list of dependency <type/> values which will be installed in the eclipse instance\'s plugins directory.",
                       3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "make-artifacts".equals(goal)) {
            append(sb, "eclipse:make-artifacts", 0);
            append(sb, "Deprecated. use EclipseToMavenMojo for the latest naming conventions", 1);
            if (detail) {
                append(sb, "", 0);
                append(sb,
                       "Add eclipse artifacts from an eclipse installation to the local repo. This mojo automatically analize the eclipse directory, copy plugins jars to the local maven repo, and generates appropriate poms. Use eclipse:to-maven for the latest naming conventions in place, groupId. artifactId.",
                       1);
            }
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "deployTo", 2);
                append(sb,
                       "Specifies a remote repository to which generated artifacts should be deployed to. If this property is specified, artifacts are also deployed to the remote repo. The format for this parameter is id::layout::url",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseDir", 2);
                append(sb,
                       "Eclipse installation dir. If not set, a value for this parameter will be asked on the command line.",
                       3);
                append(sb, "", 0);

                append(sb, "forcedQualifier", 2);
                append(sb,
                       "Default token to use as a qualifier. Tipically qualifiers for plugins in the same eclipse build are different. This parameter can be used to \'align\' qualifiers so that all the plugins coming from the same eclipse build can be easily identified. For example, setting this to \'M3\' will force the pluging versions to be \'*.*.*.M3\'",
                       3);
                append(sb, "", 0);

                append(sb, "resolveVersionRanges (Default: false)", 2);
                append(sb,
                       "Resolve version ranges in generated pom dependencies to versions of the other plugins being converted",
                       3);
                append(sb, "", 0);

                append(sb, "stripQualifier (Default: true)", 2);
                append(sb,
                       "Strip qualifier (fourth token) from the plugin version. Qualifiers are for eclipse plugin the equivalent of timestamped snapshot versions for Maven, but the date is maintained also for released version (e.g. a jar for the release 3.2 can be named org.eclipse.core.filesystem_1.0.0.v20060603.jar. It\'s usually handy to not to include this qualifier when generating maven artifacts for major releases, while it\'s needed when working with eclipse integration/nightly builds.",
                       3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "myeclipse".equals(goal)) {
            append(sb, "eclipse:myeclipse", 0);
            append(sb, "Generates MyEclipse configuration files", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "addGroupIdToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the groupId of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "addVersionToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the version number of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalBuildcommands", 2);
                append(sb,
                       "List of eclipse build commands to be added to the default ones. Old style:\n<additionalBuildcommands>\n<buildcommand>org.springframework.ide.eclipse.core.springbuilder</buildcommand>\n</additionalBuildcommands>\nNew style:\n<additionalBuildcommands>\n<buildCommand>\n<name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n<triggers>auto,full,incremental,</triggers>\n<arguments>\n<LaunchConfigHandle>&lt;project&gt;./externalToolBuilders/MavenBuilder.launch</LaunchConfighandle>\n</arguments>\n</buildCommand>\n</additionalBuildcommands>\nNote the difference between buildcommand and buildCommand. You can mix and match old and new-style configuration entries.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb,
                       "Allow to configure additional generic configuration files for eclipse that will be written out to disk when running eclipse:eclipse. FOr each file you can specify the name and the text content.\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<content>\n<![CDATA[<fileset-config\u00a0file-format-version=\'1.2.0\'\u00a0simple-config=\'true\'>\n<fileset\u00a0name=\'all\'\u00a0enabled=\'true\'\u00a0check-config-name=\'acme\u00a0corporate\u00a0style\'\u00a0local=\'false\'>\n<file-match-pattern\u00a0match-pattern=\'.\'\u00a0include-pattern=\'true\'/>\n</fileset>\n<filter\u00a0name=\'NonSrcDirs\'\u00a0enabled=\'true\'/>\n</fileset-config>]]>\n</content>\n</file>\n</additionalConfig>\n</configuration>\n</plugin>\nInstead of the content you can also define (from version 2.5) an url to download the file :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<url>http://some.place.org/path/to/file</url>\n</file>\n</additionalConfig>\n</configuration>\nor a location :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<location>/checkstyle-config.xml</location>\n</file>\n</additionalConfig>\n</configuration>\n<dependencies>\n<!--\u00a0The\u00a0file\u00a0defined\u00a0in\u00a0the\u00a0location\u00a0is\u00a0stored\u00a0in\u00a0this\u00a0dependency\u00a0-->\n<dependency>\n<groupId>eclipsetest</groupId>\n<artifactId>checkstyle-config</artifactId>\n<version>1.0</version>\n</dependency>\n</dependencies>\n</plugin>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectFacets", 2);
                append(sb,
                       "List of eclipse project facets to be added to the default ones.\n<additionalProjectFacets>\n<jst.jsf>1.1<jst.jsf/>\n</additionalProjectFacets>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectnatures", 2);
                append(sb,
                       "List of eclipse project natures to be added to the default ones.\n<additionalProjectnatures>\n<projectnature>org.springframework.ide.eclipse.core.springnature</projectnature>\n</additionalProjectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "ajdtVersion (Default: none)", 2);
                append(sb,
                       "The version of AJDT for which configuration files will be generated. The default value is \'1.5\', supported versions are \'none\' (AJDT support disabled), \'1.4\', and \'1.5\'.",
                       3);
                append(sb, "", 0);

                append(sb, "buildOutputDirectory (Default: ${project.build.outputDirectory})", 2);
                append(sb, "The default output directory", 3);
                append(sb, "", 0);

                append(sb, "buildcommands", 2);
                append(sb,
                       "List of eclipse build commands. By default the org.eclipse.jdt.core.javabuilder builder plus the needed WTP builders are added. If you specify any configuration for this parameter, only those buildcommands specified will be used; the defaults won\'t be added. Use the additionalBuildCommands parameter for that. Configuration example: Old style:\n<buildcommands>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilder</buildcommand>\n<buildcommand>org.eclipse.jdt.core.javabuilder</buildcommand>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilderDependencyResolver</buildcommand>\n</buildcommands>\nFor new style, see additionalBuildCommands.",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainers", 2);
                append(sb,
                       "List of container classpath entries. By default the org.eclipse.jdt.launching.JRE_CONTAINER classpath container is added. Configuration example:\n<classpathContainers>\n<classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>\n<classpathContainer>org.eclipse.jst.server.core.container/org.eclipse.jst.server.tomcat.runtimeTarget/Apache\u00a0Tomcat\u00a0v5.5</classpathContainer>\n<classpathContainer>org.eclipse.jst.j2ee.internal.web.container/artifact</classpathContainer>\n</classpathContainers>\n",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainersLast (Default: false)", 2);
                append(sb,
                       "Put classpath container entries last in eclipse classpath configuration. Note that this behaviour, although useful in situations were you want to override resources found in classpath containers, will made JRE classes loaded after 3rd party jars, so enabling it is not suggested.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadJavadocs", 2);
                append(sb,
                       "Enables/disables the downloading of javadoc attachments. Defaults to false. When this flag is true remote repositories are checked for javadocs: in order to avoid repeated check for unavailable javadoc archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadSources", 2);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. When this flag is true remote repositories are checked for sources: in order to avoid repeated check for unavailable source archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseDownloadSources", 2);
                append(sb, "Deprecated. use downloadSources", 3);
                append(sb, "", 0);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. DEPRECATED - use downloadSources",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseProjectDir", 2);
                append(sb, "Eclipse workspace directory.", 3);
                append(sb, "", 0);

                append(sb, "excludes", 2);
                append(sb,
                       "List of artifacts, represented as groupId:artifactId, to exclude from the eclipse classpath, being provided by some eclipse classPathContainer.",
                       3);
                append(sb, "", 0);

                append(sb, "forceRecheck", 2);
                append(sb,
                       "Enables/disables the rechecking of the remote repository for downloading source/javadoc attachments. Defaults to false. When this flag is true and the source or javadoc attachment has a status cache to indicate that it is not available, then the remote repository will be rechecked for a source or javadoc attachment and the status cache updated to reflect the new state.",
                       3);
                append(sb, "", 0);

                append(sb, "hibernate", 2);
                append(sb,
                       "Hibernate configuration placeholder\n\n<hibernate>\n<config-file>src/main/resources/applicationContext-persistence.xml</config-file>\n<session-factory-id>mySessionFactory</session-factory-id>\n</hibernate>\n",
                       3);
                append(sb, "", 0);

                append(sb, "jeeversion", 2);
                append(sb,
                       "The plugin is often capable in predicting the required jee version based on the dependencies of the project. By setting this parameter to one of the jeeversion options the version will be locked. \njeeversion\nEJB version\nServlet version\nJSP version\n6.0\n3.1\n3.0\n2.2\n5.0\n3.0\n2.5\n2.1\n1.4\n2.1\n2.4\n2.0\n1.3\n2.0\n2.3\n1.2\n1.2\n1.1\n2.2\n1.1",
                       3);
                append(sb, "", 0);

                append(sb, "limitProjectReferencesToWorkspace (Default: false)", 2);
                append(sb,
                       "Limit the use of project references to the current workspace. No project references will be created to projects in the reactor when they are not available in the workspace.",
                       3);
                append(sb, "", 0);

                append(sb, "linkedResources", 2);
                append(sb,
                       "A list of links to local files in the system. A configuration like this one in the pom :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<linkedResources>\n<linkedResource>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</linkedResource>\n</linkedResources>\n</configuration>\n</plugin>\nwill produce in the .project :\n<linkedResources>\n<link>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</link>\n</linkedResources>\n",
                       3);
                append(sb, "", 0);

                append(sb, "manifest (Default: ${basedir}/META-INF/MANIFEST.MF)", 2);
                append(sb, "The relative path of the manifest file", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "The project packaging.", 3);
                append(sb, "", 0);

                append(sb, "pde (Default: false)", 2);
                append(sb,
                       "Is it an PDE project? If yes, the plugin adds the necessary natures and build commands to the .project file. Additionally it copies all libraries to a project local directory and references them instead of referencing the files in the local Maven repository. It also ensured that the \'Bundle-Classpath\' in META-INF/MANIFEST.MF is synchronized.",
                       3);
                append(sb, "", 0);

                append(sb, "projectNameTemplate", 2);
                append(sb,
                       "Allows configuring the name of the eclipse projects. This property if set wins over addVersionToProjectName and addGroupIdToProjectName You can use [groupId], [artifactId] and [version] variables. eg. [groupId].[artifactId]-[version]",
                       3);
                append(sb, "", 0);

                append(sb, "projectnatures", 2);
                append(sb,
                       "List of eclipse project natures. By default the org.eclipse.jdt.core.javanature nature plus the needed WTP natures are added. Natures added using this property replace the default list.\n<projectnatures>\n<projectnature>org.eclipse.jdt.core.javanature</projectnature>\n<projectnature>org.eclipse.wst.common.modulecore.ModuleCoreNature</projectnature>\n</projectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);

                append(sb, "sourceExcludes", 2);
                append(sb,
                       "List of exclusions to add to the source directories on the classpath. Adds excluding=\'\' to the classpathentry of the eclipse .classpath file. [MECLIPSE-104]",
                       3);
                append(sb, "", 0);

                append(sb, "sourceIncludes", 2);
                append(sb,
                       "List of inclusions to add to the source directories on the classpath. Adds including=\'\' to the classpathentry of the eclipse .classpath file.\nJava projects will always include \'**/*.java\'\n\nAjdt projects will always include \'**/*.aj\'\n\n[MECLIPSE-104]\n",
                       3);
                append(sb, "", 0);

                append(sb, "spring", 2);
                append(sb,
                       "Spring configuration placeholder\n\n<spring>\n<version>1.0/2.0</version>\n<file-pattern>applicationContext-*.xml</file-pattern>\n<basedir>src/main/resources</basedir>\n</spring>\n",
                       3);
                append(sb, "", 0);

                append(sb, "struts", 2);
                append(sb,
                       "Allow declaration of struts properties for MyEclipse\n\n<struts>\n<version>1.2.9</version>\n<servlet-name>action</servlet-name>\n<pattern>*.do</pattern>\n<base-package>1.2.9</base-package>\n</struts>\n",
                       3);
                append(sb, "", 0);

                append(sb, "testSourcesLast (Default: false)", 2);
                append(sb,
                       "Whether to place test resources after main resources. Note that the default behavior of Maven version 2.0.8 or later is to have test dirs before main dirs in classpath so this is discouraged if you need to reproduce the maven behavior during tests. The default behavior is also changed in eclipse plugin version 2.6 in order to better match the maven one. Switching to \'test source last\' can anyway be useful if you need to run your application in eclipse, since there is no concept in eclipse of \'phases\' with different set of source dirs and dependencies like we have in maven.",
                       3);
                append(sb, "", 0);

                append(sb, "useProjectReferences (Default: true)", 2);
                append(sb,
                       "When set to false, the plugin will not create sub-projects and instead reference those sub-projects using the installed package in the local repository",
                       3);
                append(sb, "", 0);

                append(sb, "workspace", 2);
                append(sb,
                       "This eclipse workspace is read and all artifacts detected there will be connected as eclipse projects and will not be linked to the jars in the local repository. Requirement is that it was created with the similar wtp settings as the reactor projects, but the project name template my differ. The pom\'s in the workspace projects may not contain variables in the artefactId, groupId and version tags. If workspace is not defined, then an attempt to locate it by checking up the directory hierarchy will be made.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpContextName", 2);
                append(sb,
                       "JEE context name of the WTP module. ( ex. WEB context name ). You can use \'ROOT\' if you want to map the webapp to the root context.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpapplicationxml (Default: false)", 2);
                append(sb, "Must the application files be written for ear projects in a separate directory.", 3);
                append(sb, "", 0);

                append(sb, "wtpdefaultserver", 2);
                append(sb, "What WTP defined server to use for deployment informations.", 3);
                append(sb, "", 0);

                append(sb, "wtpmanifest (Default: false)", 2);
                append(sb,
                       "Must the manifest files be written for java projects so that that the jee classpath for wtp is correct.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpversion (Default: none)", 2);
                append(sb,
                       "The version of WTP for which configuration files will be generated. The default value is \'none\' (don\'t generate WTP configuration), supported versions are \'R7\', \'1.0\', \'1.5\' and \'2.0\'",
                       3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "myeclipse-clean".equals(goal)) {
            append(sb, "eclipse:myeclipse-clean", 0);
            append(sb, "Deletes configuration files used by MyEclipse", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb, "additional generic configuration files for eclipse", 3);
                append(sb, "", 0);

                append(sb, "basedir", 2);
                append(sb, "The root directory of the project", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "Packaging for the current project.", 3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "rad".equals(goal)) {
            append(sb, "eclipse:rad", 0);
            append(sb, "Generates the rad-6 configuration files.", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "addGroupIdToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the groupId of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "addVersionToProjectName (Default: false)", 2);
                append(sb,
                       "If set to true, the version number of the artifact is appended to the name of the generated Eclipse project. See projectNameTemplate for other options.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalBuildcommands", 2);
                append(sb,
                       "List of eclipse build commands to be added to the default ones. Old style:\n<additionalBuildcommands>\n<buildcommand>org.springframework.ide.eclipse.core.springbuilder</buildcommand>\n</additionalBuildcommands>\nNew style:\n<additionalBuildcommands>\n<buildCommand>\n<name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n<triggers>auto,full,incremental,</triggers>\n<arguments>\n<LaunchConfigHandle>&lt;project&gt;./externalToolBuilders/MavenBuilder.launch</LaunchConfighandle>\n</arguments>\n</buildCommand>\n</additionalBuildcommands>\nNote the difference between buildcommand and buildCommand. You can mix and match old and new-style configuration entries.",
                       3);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb,
                       "Allow to configure additional generic configuration files for eclipse that will be written out to disk when running eclipse:eclipse. FOr each file you can specify the name and the text content.\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<content>\n<![CDATA[<fileset-config\u00a0file-format-version=\'1.2.0\'\u00a0simple-config=\'true\'>\n<fileset\u00a0name=\'all\'\u00a0enabled=\'true\'\u00a0check-config-name=\'acme\u00a0corporate\u00a0style\'\u00a0local=\'false\'>\n<file-match-pattern\u00a0match-pattern=\'.\'\u00a0include-pattern=\'true\'/>\n</fileset>\n<filter\u00a0name=\'NonSrcDirs\'\u00a0enabled=\'true\'/>\n</fileset-config>]]>\n</content>\n</file>\n</additionalConfig>\n</configuration>\n</plugin>\nInstead of the content you can also define (from version 2.5) an url to download the file :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<url>http://some.place.org/path/to/file</url>\n</file>\n</additionalConfig>\n</configuration>\nor a location :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<additionalConfig>\n<file>\n<name>.checkstyle</name>\n<location>/checkstyle-config.xml</location>\n</file>\n</additionalConfig>\n</configuration>\n<dependencies>\n<!--\u00a0The\u00a0file\u00a0defined\u00a0in\u00a0the\u00a0location\u00a0is\u00a0stored\u00a0in\u00a0this\u00a0dependency\u00a0-->\n<dependency>\n<groupId>eclipsetest</groupId>\n<artifactId>checkstyle-config</artifactId>\n<version>1.0</version>\n</dependency>\n</dependencies>\n</plugin>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectFacets", 2);
                append(sb,
                       "List of eclipse project facets to be added to the default ones.\n<additionalProjectFacets>\n<jst.jsf>1.1<jst.jsf/>\n</additionalProjectFacets>\n",
                       3);
                append(sb, "", 0);

                append(sb, "additionalProjectnatures", 2);
                append(sb,
                       "List of eclipse project natures to be added to the default ones.\n<additionalProjectnatures>\n<projectnature>org.springframework.ide.eclipse.core.springnature</projectnature>\n</additionalProjectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "ajdtVersion (Default: none)", 2);
                append(sb,
                       "The version of AJDT for which configuration files will be generated. The default value is \'1.5\', supported versions are \'none\' (AJDT support disabled), \'1.4\', and \'1.5\'.",
                       3);
                append(sb, "", 0);

                append(sb, "buildOutputDirectory (Default: ${project.build.outputDirectory})", 2);
                append(sb, "The default output directory", 3);
                append(sb, "", 0);

                append(sb, "buildcommands", 2);
                append(sb,
                       "List of eclipse build commands. By default the org.eclipse.jdt.core.javabuilder builder plus the needed WTP builders are added. If you specify any configuration for this parameter, only those buildcommands specified will be used; the defaults won\'t be added. Use the additionalBuildCommands parameter for that. Configuration example: Old style:\n<buildcommands>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilder</buildcommand>\n<buildcommand>org.eclipse.jdt.core.javabuilder</buildcommand>\n<buildcommand>org.eclipse.wst.common.modulecore.ComponentStructuralBuilderDependencyResolver</buildcommand>\n</buildcommands>\nFor new style, see additionalBuildCommands.",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainers", 2);
                append(sb,
                       "List of container classpath entries. By default the org.eclipse.jdt.launching.JRE_CONTAINER classpath container is added. Configuration example:\n<classpathContainers>\n<classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER</classpathContainer>\n<classpathContainer>org.eclipse.jst.server.core.container/org.eclipse.jst.server.tomcat.runtimeTarget/Apache\u00a0Tomcat\u00a0v5.5</classpathContainer>\n<classpathContainer>org.eclipse.jst.j2ee.internal.web.container/artifact</classpathContainer>\n</classpathContainers>\n",
                       3);
                append(sb, "", 0);

                append(sb, "classpathContainersLast (Default: false)", 2);
                append(sb,
                       "Put classpath container entries last in eclipse classpath configuration. Note that this behaviour, although useful in situations were you want to override resources found in classpath containers, will made JRE classes loaded after 3rd party jars, so enabling it is not suggested.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadJavadocs", 2);
                append(sb,
                       "Enables/disables the downloading of javadoc attachments. Defaults to false. When this flag is true remote repositories are checked for javadocs: in order to avoid repeated check for unavailable javadoc archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "downloadSources", 2);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. When this flag is true remote repositories are checked for sources: in order to avoid repeated check for unavailable source archives, a status cache is mantained. With versions 2.6+ of the plugin to reset this cache run mvn eclipse:remove-cache, or use the forceRecheck option with versions. With older versions delete the file mvn-eclipse-cache.properties in the target directory.",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseDownloadSources", 2);
                append(sb, "Deprecated. use downloadSources", 3);
                append(sb, "", 0);
                append(sb,
                       "Enables/disables the downloading of source attachments. Defaults to false. DEPRECATED - use downloadSources",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseProjectDir", 2);
                append(sb, "Eclipse workspace directory.", 3);
                append(sb, "", 0);

                append(sb, "excludes", 2);
                append(sb,
                       "List of artifacts, represented as groupId:artifactId, to exclude from the eclipse classpath, being provided by some eclipse classPathContainer.",
                       3);
                append(sb, "", 0);

                append(sb, "forceRecheck", 2);
                append(sb,
                       "Enables/disables the rechecking of the remote repository for downloading source/javadoc attachments. Defaults to false. When this flag is true and the source or javadoc attachment has a status cache to indicate that it is not available, then the remote repository will be rechecked for a source or javadoc attachment and the status cache updated to reflect the new state.",
                       3);
                append(sb, "", 0);

                append(sb, "generatedResourceDirName (Default: target/generated-resources/rad6)", 2);
                append(sb,
                       "Use this to specify a different generated resources folder than target/generated-resources/rad6. Set to \'none\' to skip this folder generation.",
                       3);
                append(sb, "", 0);

                append(sb, "jeeversion", 2);
                append(sb,
                       "The plugin is often capable in predicting the required jee version based on the dependencies of the project. By setting this parameter to one of the jeeversion options the version will be locked. \njeeversion\nEJB version\nServlet version\nJSP version\n6.0\n3.1\n3.0\n2.2\n5.0\n3.0\n2.5\n2.1\n1.4\n2.1\n2.4\n2.0\n1.3\n2.0\n2.3\n1.2\n1.2\n1.1\n2.2\n1.1",
                       3);
                append(sb, "", 0);

                append(sb, "limitProjectReferencesToWorkspace (Default: false)", 2);
                append(sb,
                       "Limit the use of project references to the current workspace. No project references will be created to projects in the reactor when they are not available in the workspace.",
                       3);
                append(sb, "", 0);

                append(sb, "linkedResources", 2);
                append(sb,
                       "A list of links to local files in the system. A configuration like this one in the pom :\n<plugin>\n<groupId>org.apache.maven.plugins</groupId>\n<artifactId>maven-eclipse-plugin</artifactId>\n<configuration>\n<linkedResources>\n<linkedResource>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</linkedResource>\n</linkedResources>\n</configuration>\n</plugin>\nwill produce in the .project :\n<linkedResources>\n<link>\n<name>src/test/resources/oracle-ds.xml</name>\n<type>1</type>\n<location>C://jboss/server/default/deploy/oracle-ds.xml</location>\n</link>\n</linkedResources>\n",
                       3);
                append(sb, "", 0);

                append(sb, "manifest (Default: ${basedir}/META-INF/MANIFEST.MF)", 2);
                append(sb, "The relative path of the manifest file", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "The project packaging.", 3);
                append(sb, "", 0);

                append(sb, "pde (Default: false)", 2);
                append(sb,
                       "Is it an PDE project? If yes, the plugin adds the necessary natures and build commands to the .project file. Additionally it copies all libraries to a project local directory and references them instead of referencing the files in the local Maven repository. It also ensured that the \'Bundle-Classpath\' in META-INF/MANIFEST.MF is synchronized.",
                       3);
                append(sb, "", 0);

                append(sb, "projectNameTemplate", 2);
                append(sb,
                       "Allows configuring the name of the eclipse projects. This property if set wins over addVersionToProjectName and addGroupIdToProjectName You can use [groupId], [artifactId] and [version] variables. eg. [groupId].[artifactId]-[version]",
                       3);
                append(sb, "", 0);

                append(sb, "projectnatures", 2);
                append(sb,
                       "List of eclipse project natures. By default the org.eclipse.jdt.core.javanature nature plus the needed WTP natures are added. Natures added using this property replace the default list.\n<projectnatures>\n<projectnature>org.eclipse.jdt.core.javanature</projectnature>\n<projectnature>org.eclipse.wst.common.modulecore.ModuleCoreNature</projectnature>\n</projectnatures>\n",
                       3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);

                append(sb, "sourceExcludes", 2);
                append(sb,
                       "List of exclusions to add to the source directories on the classpath. Adds excluding=\'\' to the classpathentry of the eclipse .classpath file. [MECLIPSE-104]",
                       3);
                append(sb, "", 0);

                append(sb, "sourceIncludes", 2);
                append(sb,
                       "List of inclusions to add to the source directories on the classpath. Adds including=\'\' to the classpathentry of the eclipse .classpath file.\nJava projects will always include \'**/*.java\'\n\nAjdt projects will always include \'**/*.aj\'\n\n[MECLIPSE-104]\n",
                       3);
                append(sb, "", 0);

                append(sb, "testSourcesLast (Default: false)", 2);
                append(sb,
                       "Whether to place test resources after main resources. Note that the default behavior of Maven version 2.0.8 or later is to have test dirs before main dirs in classpath so this is discouraged if you need to reproduce the maven behavior during tests. The default behavior is also changed in eclipse plugin version 2.6 in order to better match the maven one. Switching to \'test source last\' can anyway be useful if you need to run your application in eclipse, since there is no concept in eclipse of \'phases\' with different set of source dirs and dependencies like we have in maven.",
                       3);
                append(sb, "", 0);

                append(sb, "useProjectReferences (Default: true)", 2);
                append(sb,
                       "When set to false, the plugin will not create sub-projects and instead reference those sub-projects using the installed package in the local repository",
                       3);
                append(sb, "", 0);

                append(sb, "warContextRoot", 2);
                append(sb,
                       "The context root of the webapplication. This parameter is only used when the current project is a war project, else it will be ignored.",
                       3);
                append(sb, "", 0);

                append(sb, "workspace", 2);
                append(sb,
                       "This eclipse workspace is read and all artifacts detected there will be connected as eclipse projects and will not be linked to the jars in the local repository. Requirement is that it was created with the similar wtp settings as the reactor projects, but the project name template my differ. The pom\'s in the workspace projects may not contain variables in the artefactId, groupId and version tags. If workspace is not defined, then an attempt to locate it by checking up the directory hierarchy will be made.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpContextName", 2);
                append(sb,
                       "JEE context name of the WTP module. ( ex. WEB context name ). You can use \'ROOT\' if you want to map the webapp to the root context.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpapplicationxml (Default: false)", 2);
                append(sb, "Must the application files be written for ear projects in a separate directory.", 3);
                append(sb, "", 0);

                append(sb, "wtpdefaultserver", 2);
                append(sb, "What WTP defined server to use for deployment informations.", 3);
                append(sb, "", 0);

                append(sb, "wtpmanifest (Default: false)", 2);
                append(sb,
                       "Must the manifest files be written for java projects so that that the jee classpath for wtp is correct.",
                       3);
                append(sb, "", 0);

                append(sb, "wtpversion (Default: none)", 2);
                append(sb,
                       "The version of WTP for which configuration files will be generated. The default value is \'none\' (don\'t generate WTP configuration), supported versions are \'R7\', \'1.0\', \'1.5\' and \'2.0\'",
                       3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "rad-clean".equals(goal)) {
            append(sb, "eclipse:rad-clean", 0);
            append(sb, "Deletes the config files used by Rad-6. the files .j2ee and the file .websettings", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "additionalConfig", 2);
                append(sb, "additional generic configuration files for eclipse", 3);
                append(sb, "", 0);

                append(sb, "basedir", 2);
                append(sb, "The root directory of the project", 3);
                append(sb, "", 0);

                append(sb, "packaging", 2);
                append(sb, "Packaging for the current project.", 3);
                append(sb, "", 0);

                append(sb, "skip (Default: false)", 2);
                append(sb, "Skip the operation when true.", 3);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "remove-cache".equals(goal)) {
            append(sb, "eclipse:remove-cache", 0);
            append(sb, "Removes the not-available marker files from the repository.", 1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);
            }
        }

        if (goal == null || goal.length() <= 0 || "to-maven".equals(goal)) {
            append(sb, "eclipse:to-maven", 0);
            append(sb,
                   "Add eclipse artifacts from an eclipse installation to the local repo. This mojo automatically analize the eclipse directory, copy plugins jars to the local maven repo, and generates appropriate poms. This is the official central repository builder for Eclipse plugins, so it has the necessary default values. For customized repositories see MakeArtifactsMojo Typical usage: mvn eclipse:to-maven -DdeployTo=maven.org::default::scpexe://repo1.maven.org/home/maven/repository-staging/to-ibiblio/eclipse-staging -DeclipseDir=.",
                   1);
            append(sb, "", 0);
            if (detail) {
                append(sb, "Available parameters:", 1);
                append(sb, "", 0);

                append(sb, "deployTo", 2);
                append(sb,
                       "Specifies a remote repository to which generated artifacts should be deployed to. If this property is specified, artifacts are also deployed to the remote repo. The format for this parameter is id::layout::url",
                       3);
                append(sb, "", 0);

                append(sb, "eclipseDir", 2);
                append(sb,
                       "Eclipse installation dir. If not set, a value for this parameter will be asked on the command line.",
                       3);
                append(sb, "", 0);

                append(sb, "stripQualifier (Default: false)", 2);
                append(sb,
                       "Strip qualifier (fourth token) from the plugin version. Qualifiers are for eclipse plugin the equivalent of timestamped snapshot versions for Maven, but the date is maintained also for released version (e.g. a jar for the release 3.2 can be named org.eclipse.core.filesystem_1.0.0.v20060603.jar. It\'s usually handy to not to include this qualifier when generating maven artifacts for major releases, while it\'s needed when working with eclipse integration/nightly builds.",
                       3);
                append(sb, "", 0);
            }
        }

        if (getLog().isInfoEnabled()) {
            getLog().info(sb.toString());
        }
    }

    /**
     * <p>Repeat a String <code>n</code> times to form a new string.</p>
     *
     * @param str    String to repeat
     * @param repeat number of times to repeat str
     * @return String with repeated String
     * @throws NegativeArraySizeException if <code>repeat < 0</code>
     * @throws NullPointerException       if str is <code>null</code>
     */
    private static String repeat(String str, int repeat) {
        StringBuffer buffer = new StringBuffer(repeat * str.length());

        for (int i = 0; i < repeat; i++) {
            buffer.append(str);
        }

        return buffer.toString();
    }

    /**
     * Append a description to the buffer by respecting the indentSize and lineLength parameters.
     * <b>Note</b>: The last character is always a new line.
     *
     * @param sb          The buffer to append the description, not <code>null</code>.
     * @param description The description, not <code>null</code>.
     * @param indent      The base indentation level of each line, must not be negative.
     */
    private void append(StringBuffer sb, String description, int indent) {
        for (Iterator it = toLines(description, indent, indentSize, lineLength).iterator(); it.hasNext(); ) {
            sb.append(it.next().toString()).append('\n');
        }
    }

    /**
     * Splits the specified text into lines of convenient display length.
     *
     * @param text       The text to split into lines, must not be <code>null</code>.
     * @param indent     The base indentation level of each line, must not be negative.
     * @param indentSize The size of each indentation, must not be negative.
     * @param lineLength The length of the line, must not be negative.
     * @return The sequence of display lines, never <code>null</code>.
     * @throws NegativeArraySizeException if <code>indent < 0</code>
     */
    private static List toLines(String text, int indent, int indentSize, int lineLength) {
        List lines = new ArrayList();

        String ind = repeat("\t", indent);
        String[] plainLines = text.split("(\r\n)|(\r)|(\n)");
        for (int i = 0; i < plainLines.length; i++) {
            toLines(lines, ind + plainLines[i], indentSize, lineLength);
        }

        return lines;
    }

    /**
     * Adds the specified line to the output sequence, performing line wrapping if necessary.
     *
     * @param lines      The sequence of display lines, must not be <code>null</code>.
     * @param line       The line to add, must not be <code>null</code>.
     * @param indentSize The size of each indentation, must not be negative.
     * @param lineLength The length of the line, must not be negative.
     */
    private static void toLines(List lines, String line, int indentSize, int lineLength) {
        int lineIndent = getIndentLevel(line);
        StringBuffer buf = new StringBuffer(256);
        String[] tokens = line.split(" +");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (i > 0) {
                if (buf.length() + token.length() >= lineLength) {
                    lines.add(buf.toString());
                    buf.setLength(0);
                    buf.append(repeat(" ", lineIndent * indentSize));
                } else {
                    buf.append(' ');
                }
            }
            for (int j = 0; j < token.length(); j++) {
                char c = token.charAt(j);
                if (c == '\t') {
                    buf.append(repeat(" ", indentSize - buf.length() % indentSize));
                } else if (c == '\u00A0') {
                    buf.append(' ');
                } else {
                    buf.append(c);
                }
            }
        }
        lines.add(buf.toString());
    }

    /**
     * Gets the indentation level of the specified line.
     *
     * @param line The line whose indentation level should be retrieved, must not be <code>null</code>.
     * @return The indentation level of the line.
     */
    private static int getIndentLevel(String line) {
        int level = 0;
        for (int i = 0; i < line.length() && line.charAt(i) == '\t'; i++) {
            level++;
        }
        for (int i = level + 1; i <= level + 4 && i < line.length(); i++) {
            if (line.charAt(i) == '\t') {
                level++;
                break;
            }
        }
        return level;
    }
}
