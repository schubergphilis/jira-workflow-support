package com.schubergphilis.jira.plugins.customfield;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Collection;

import webwork.action.ActionContext;

public class EditConfiguration extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;

    private FieldConfig fieldConfig;

    private Long fieldConfigId;

    private Long projectCategoryId;

    private FieldConfigManager fieldConfigManager;
    private ProjectManager projectManager;

    public EditConfiguration(FieldConfigManager fieldConfigManager, ProjectManager projectManager) {
        this.fieldConfigManager = fieldConfigManager;
        this.projectManager = projectManager;
    }

    @RequiresXsrfCheck
    protected void doValidation() {

    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception {
        if (ActionContext.getContext().getRequestImpl().getMethod().equals("GET")) {
            setProjectCategoryId(DAO.getProjectCategory(getFieldConfig()));
            return INPUT;
        } else {
            DAO.setProjectCategory(getFieldConfig(), getProjectCategoryId());
            setReturnUrl("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + getFieldConfig().getCustomField().getIdAsLong()
                    .toString());
            return getRedirect("not used");
        }
    }

    public Long getProjectCategoryId() {
        return projectCategoryId;
    }

    public void setProjectCategoryId(Long projectCategory) {
        this.projectCategoryId = projectCategory;
    }

    public Long getFieldConfigId() {
        return fieldConfigId;
    }

    public void setFieldConfigId(Long fieldConfigId) {
        this.fieldConfigId = fieldConfigId;
    }

    public FieldConfig getFieldConfig() {
        if (fieldConfig == null && fieldConfigId != null) {
            fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
        }

        return fieldConfig;
    }

    public Collection<ProjectCategory> getProjectCategories() {
        return projectManager.getAllProjectCategories();
    }
}
