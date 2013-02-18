package com.alibaba.ide.plugin.eclipse.springext.editor.component.cp;

import org.eclipse.ui.forms.editor.FormPage;

public class ConfigurationPointPage extends FormPage {
    public final static String PAGE_ID = ConfigurationPointPage.class.getName();

    public ConfigurationPointPage(ConfigurationPointEditor editor) {
        super(editor, PAGE_ID, "Configuration Point");
    }
}
