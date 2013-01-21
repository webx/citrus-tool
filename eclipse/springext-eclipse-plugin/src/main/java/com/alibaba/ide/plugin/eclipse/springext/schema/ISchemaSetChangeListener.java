package com.alibaba.ide.plugin.eclipse.springext.schema;

import org.eclipse.core.resources.IProject;

/**
 * 当schema set被更新时，所有的listeners将得到通知。
 * 
 * @author Michael Zhou
 */
public interface ISchemaSetChangeListener {
    void onSchemaSetChanged(SchemaSetChangeEvent event);

    public class SchemaSetChangeEvent {
        private final IProject project;

        public SchemaSetChangeEvent(IProject project) {
            this.project = project;
        }

        public IProject getProject() {
            return project;
        }
    }
}
