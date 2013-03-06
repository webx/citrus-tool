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

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * 新建Springext Configuration Point向导
 * 
 * @author zhiqing.ht
 * @author xuanxiao
 * @author qianchao
 * @version 1.0.0, 2010-07-10
 */
@SuppressWarnings("restriction")
public class ConfigurationPointWizard extends Wizard implements INewWizard {

	private ConfigurationPointWizardpage	page	= null;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.ConfigurationPointWizard_new);
		Object obj = selection.getFirstElement();
		page = new ConfigurationPointWizardpage(Messages.ConfigurationPointWizard_configuration, Messages.ConfigurationPointWizard_new,
		        JavaPluginImages.DESC_WIZBAN_REFACTOR_TYPE, selection);
		if (obj == null) {
			page.setErrorMessage(Messages.ConfigurationPointWizard_noProject);
			page.setPageComplete(false);
		}
	}

	@Override
	public void addPages() {
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		if (page.validate()) {
			page.createConfigurationPoint();
			return true;
		}
		return false;
	}

}
