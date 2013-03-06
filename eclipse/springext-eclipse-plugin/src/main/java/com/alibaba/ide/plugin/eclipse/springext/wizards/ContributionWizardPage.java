/*
 * Copyright 2010 Alibaba Group Holding Limited.
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
 *
 */
package com.alibaba.ide.plugin.eclipse.springext.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;

import com.alibaba.citrus.springext.ConfigurationPoint;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtConfigUtil;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;
import static com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant.LINE_BR;

/**
 * 新建Springext Contribution向导页
 * 
 * @author zhiqing.ht
 * @author xuanxiao
 * @author qianchao
 * @version 1.0.0, 2010-07-10
 */
@SuppressWarnings("restriction")
public class ContributionWizardPage extends WizardPage {
	private IStructuredSelection	selection	        = null;

	private Text	             configurationPointText	= null;

	private Button	             selectCpButton	        = null;

	private Text	             contributionNameText	= null;

	private Text	             beanParserClassText	= null;

	private Button	             beanParserButton	    = null;

	protected ContributionWizardPage(String pageName, String title, ImageDescriptor titleImage, IStructuredSelection selection) {
		super(pageName, title, titleImage);
		this.selection = selection;
		setDescription(Messages.ContributionWizardPage_new);
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout(2, false));
		setControl(comp);
		Label label = new Label(comp, SWT.NULL);
		label.setText(Messages.ContributionWizardPage_chooseCP);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		configurationPointText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		configurationPointText.setEnabled(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		configurationPointText.setLayoutData(data);
		selectCpButton = new Button(comp, SWT.NULL);
		selectCpButton.setText(Messages.ContributionWizardPage_browser);

		label = new Label(comp, SWT.NULL);
		label.setText(Messages.ContributionWizardPage_exampleCN);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		contributionNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		contributionNameText.setLayoutData(data);
		label = new Label(comp, SWT.NULL);
		label.setText(Messages.ContributionWizardPage_bdpClass);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		beanParserClassText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		beanParserClassText.setLayoutData(data);
		beanParserButton = new Button(comp, SWT.NULL);
		beanParserButton.setText(Messages.ContributionWizardPage_browser);
		initListeners();

	}

	private void initListeners() {
		selectCpButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				ConfigurationPoint[] points = SpringExtConfigUtil.getAllConfigurationPoints(SpringExtPluginUtil.getSelectProject(selection));
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new ConfigurationPointLabelProvider());
				dialog.setTitle(Messages.ContributionWizardPage_chooseCP); //$NON-NLS-1$
				dialog.setMultipleSelection(false);
				dialog.setMessage(Messages.ContributionWizardPage_chooseCP); //$NON-NLS-1$
				dialog.setElements(points);
				if (dialog.open() == Window.OK) {
					Object[] objs = dialog.getResult();
					if (objs != null && objs.length > 0) {
						Object obj = objs[0];
						ConfigurationPoint point = (ConfigurationPoint) obj;
						configurationPointText.setData(point);
						configurationPointText.setText(point.getName());
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		beanParserButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				IProject p = SpringExtPluginUtil.getSelectProject(selection);
				if (p == null) {
					return;
				}
				IJavaElement[] elements = new IJavaElement[] { JavaCore.create(p) };
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);

				FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(), false, getWizard().getContainer(),
				        scope, IJavaSearchConstants.CLASS);
				dialog.setTitle(Messages.ContributionWizardPage_chooseBDP);
				dialog.setMessage(Messages.ContributionWizardPage_chooseBDPDetail);
				if (Window.OK == dialog.open()) {
					Object obj = dialog.getFirstResult();
					IType type = (IType) obj;
					beanParserClassText.setText(type.getFullyQualifiedName());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

	}

	public boolean validate() {
		String cPoint = configurationPointText.getText();
		if (null == cPoint || cPoint.trim().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.ContributionWizardPage_chooseCP); //$NON-NLS-1$
			return false;
		}
		String cName = contributionNameText.getText();
		if (null == cName || cName.trim().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.ContributionWizardPage_cnNotNull);
			return false;
		} else {
			if (cName.indexOf("/") >= 0) { //$NON-NLS-1$
				setErrorMessage(Messages.ContributionWizardPage_invalidCN1);
				return false;
			}
		}
		String parserName = beanParserClassText.getText();
		if (null == parserName || parserName.trim().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.ContributionWizardPage_invalidCN2);
			return false;
		}
		return true;
	}

	public void createContribution() {
		IProject project = SpringExtPluginUtil.getSelectProject(selection);
		SpringExtPluginUtil.checkSrcMetaExsit(project);
		boolean sucess = createParserClz();
		if (sucess) {
			sucess = createBeanDefParserFile();
			if (sucess) {
				createXSDFile();
			}
		}
	}

	private void createXSDFile() {
		String parent = "/src/main/resources/META-INF/" + configurationPointText.getText(); //$NON-NLS-1$
		String contributionName = contributionNameText.getText();
		String fileName = contributionName + ".xsd"; //$NON-NLS-1$
		String pointName = configurationPointText.getText();
		String[] pointNames = pointName.split("/"); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		for (String point : pointNames) {
			char c = point.charAt(0);
			char cs = Character.toUpperCase(c);
			sb.append(cs + point.substring(1));
		}
		char c = Character.toUpperCase(contributionName.charAt(0));
		sb.append(c + contributionName.substring(1) + "Type"); //$NON-NLS-1$
		String content = SpringExtPluginUtil.formatXsd("webx-xsd-template.xml", new String[] { contributionName, sb.toString() }); //$NON-NLS-1$
		SpringExtPluginUtil.createFileIfNesscary(parent, fileName, content, selection, getShell(), Messages.ContributionWizardPage_newXsd,
		        getContainer());

	}

	private boolean createBeanDefParserFile() {
		try {
			String pointName = configurationPointText.getText();
			String fileName = pointName.replaceAll("/", "-") + ".bean-definition-parsers"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String contributionName = contributionNameText.getText();
			String parserName = beanParserClassText.getText();
			String content = contributionName + "=" + parserName; //$NON-NLS-1$
			SpringExtPluginUtil
			        .createFileIfNesscary(
			                "/src/main/resources/META-INF", fileName, content, selection, getShell(), Messages.ContributionWizardPage_newBPF, getContainer()); //$NON-NLS-1$

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean createParserClz() {
		String parserName = beanParserClassText.getText();
		IJavaProject project = SpringExtPluginUtil.getSelectJavaProject(selection);
		try {
			IType type = project.findType(parserName);
			if (type == null) {

				int index = parserName.lastIndexOf("."); //$NON-NLS-1$
				String packageName = ""; //$NON-NLS-1$
				String className = parserName;
				if (index > 0) {
					packageName = parserName.substring(0, index);
					className = parserName.substring(index + 1);
				}
				String filePath = packageName.replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				String content = null;
				if (packageName.length() == 0) {
					content = "import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser; " //$NON-NLS-1$
					        + LINE_BR + LINE_BR + "public class " + className + " extends AbstractSingleBeanDefinitionParser<Object> {" //$NON-NLS-1$ //$NON-NLS-2$
					        + LINE_BR + LINE_BR + "}"; //$NON-NLS-1$
				} else {
					content = "package " + packageName + "; " //$NON-NLS-1$ //$NON-NLS-2$
					        + LINE_BR + LINE_BR + "import com.alibaba.citrus.springext.support.parser.AbstractSingleBeanDefinitionParser; " //$NON-NLS-1$
					        + LINE_BR + LINE_BR + "public class " + className + " extends AbstractSingleBeanDefinitionParser<Object> {" //$NON-NLS-1$ //$NON-NLS-2$
					        + LINE_BR + LINE_BR + "}"; //$NON-NLS-1$
				}

				SpringExtPluginUtil
				        .createFileIfNesscary(
				                "/src/main/java/" + filePath, className + ".java", content, selection, getShell(), Messages.ContributionWizardPage_newBPC, getContainer()); //$NON-NLS-1$ //$NON-NLS-2$
				IFile file = SpringExtPluginUtil.getFileHandle("/src/main/java/" + filePath, className + ".java", selection); //$NON-NLS-1$ //$NON-NLS-2$
				if (file.exists()) {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
				}
			} else {
				return true;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	class ConfigurationPointLabelProvider extends LabelProvider {

		public String getText(Object element) {
			if (element instanceof ConfigurationPoint) {
				ConfigurationPoint point = (ConfigurationPoint) element;
				return point.getName();
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_QUICK_FIX);
		}
	}

}
