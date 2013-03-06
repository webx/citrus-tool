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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateFileOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.ide.plugin.eclipse.springext.SpringExtConstant;
import com.alibaba.ide.plugin.eclipse.springext.util.SpringExtPluginUtil;

/**
 * 新建Springext Configuration Point向导页
 * 
 * @author zhiqing.ht
 * @author xuanxiao
 * @author qianchao
 * @version 1.0.0, 2010-07-10
 */
public class ConfigurationPointWizardpage extends WizardPage {
	private static final Logger	 log	            = LoggerFactory.getLogger(ConfigurationPointWizardpage.class);
	private IStructuredSelection	selection	    = null;

	private Text	             cpNameText	        = null;

	private Text	             tNSNameText	    = null;

	private Text	             defaultElementText	= null;

	private Text	             nsPrefixText	    = null;

	private static final String	 DEFAULT_PREFIX	    = "http://www.alibaba.com/schema/";	                       //$NON-NLS-1$

	protected ConfigurationPointWizardpage(String pageName, String title, ImageDescriptor titleImage, IStructuredSelection selection) {
		super(pageName, title, titleImage);
		this.selection = selection;
		setDescription(Messages.ConfigurationPointWizardpage_new);
	}

	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout(1, false));
		setControl(comp);
		Label l = new Label(comp, SWT.NULL);
		l.setText(Messages.ConfigurationPointWizardpage_cname);
		cpNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		cpNameText.setLayoutData(data);
		l = new Label(comp, SWT.NULL);
		l.setText(Messages.ConfigurationPointWizardpage_targetNameSpace);
		tNSNameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		tNSNameText.setLayoutData(data);
		l = new Label(comp, SWT.NULL);
		l.setText(Messages.ConfigurationPointWizardpage_dElement);
		defaultElementText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		defaultElementText.setLayoutData(data);
		l = new Label(comp, SWT.NULL);
		l.setText(Messages.ConfigurationPointWizardpage_namespacePrefix);
		nsPrefixText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		nsPrefixText.setLayoutData(data);
		initListeners();
	}

	private void initListeners() {
		cpNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				tNSNameText.setText(DEFAULT_PREFIX + cpNameText.getText());
			}
		});
	}

	public boolean validate() {
		String cpName = cpNameText.getText();
		if (null == cpName || cpName.trim().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.ConfigurationPointWizardpage_cnNotNull);
			return false;
		} else {

			if (cpName.startsWith("/") || cpName.startsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
				setErrorMessage(Messages.ConfigurationPointWizardpage_invalid);
				return false;
			}
			if (cpName.indexOf("\\") >= 0) { //$NON-NLS-1$
				setErrorMessage(Messages.ConfigurationPointWizardpage_wrong1);
				return false;
			}
		}
		String tNSName = tNSNameText.getText();
		if (null == tNSName || tNSName.trim().equals("")) { //$NON-NLS-1$
			setErrorMessage(Messages.ConfigurationPointWizardpage_tnNotNull);
			return false;
		} else {
			if (!tNSName.trim().endsWith(cpName)) {
				setErrorMessage(Messages.ConfigurationPointWizardpage_tnMust + cpName + Messages.ConfigurationPointWizardpage_cpNameEnd);
				return false;
			}
		}

		return true;
	}

	public void createConfigurationPoint() {
		IProject project = SpringExtPluginUtil.getSelectProject(selection);
		SpringExtPluginUtil.checkSrcMetaExsit(project);
		final IFile file = getFileHandle("spring.configuration-points"); //$NON-NLS-1$
		String cpName = cpNameText.getText();
		String tNSName = tNSNameText.getText();
		String defaultElement = defaultElementText.getText();
		String nsPrefix = nsPrefixText.getText();
		String tempString = cpName + "=" + tNSName; //$NON-NLS-1$
		if (defaultElement != null && defaultElement.length() > 0) {
			tempString += "; defaultElement=" + defaultElement; //$NON-NLS-1$
		}
		if (nsPrefix != null && nsPrefix.length() > 0) {
			tempString += "; nsPrefix=" + nsPrefix; //$NON-NLS-1$
		}
		final String text = tempString;
		if (file.exists()) {
			FileOutputStream outStream = null;
			try {
				outStream = new FileOutputStream(file.getLocation().toString(), true);
				outStream.write((SpringExtConstant.LINE_BR + text).getBytes());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (outStream != null) {
					try {
						outStream.close();
					} catch (IOException e) {
					}
				}
			}
			try {
				SpringExtPluginUtil.getSelectProject(selection)
				        .getFolder("/src/main/resources/META-INF").refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor()); //$NON-NLS-1$
			} catch (CoreException e) {
				log.error(e.getMessage(), e);
			}

		} else {
			IRunnableWithProgress op = new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					CreateFileOperation op = new CreateFileOperation(file, null, new ByteArrayInputStream(text.getBytes()),
					        Messages.ConfigurationPointWizardpage_NewC);
					try {
						PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
						        .execute(op, monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
					} catch (ExecutionException e) {
						log.error(e.getMessage(), e);
					}
				}

			};
			try {
				getContainer().run(true, true, op);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
	}

	private IFile getFileHandle(String xsdName) {
		IProject project = SpringExtPluginUtil.getSelectProject(selection);
		IFile file = project.getFile("/src/main/resources/META-INF/" + xsdName); //$NON-NLS-1$
		return file;
	}

}
