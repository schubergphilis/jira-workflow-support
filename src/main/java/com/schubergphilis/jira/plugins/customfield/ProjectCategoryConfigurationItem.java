package com.schubergphilis.jira.plugins.customfield;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;

import java.util.HashMap;
import java.util.Map;


public class ProjectCategoryConfigurationItem implements FieldConfigItemType {


    private ProjectManager projectManager;

    public ProjectCategoryConfigurationItem(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public String getDisplayName() {
        return "Project Category Configuration";
    }

    @Override
    public String getDisplayNameKey() {
        return "Project Category";
    }

    @Override
    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem) {
        Long projectCategoryId = DAO.getProjectCategory(fieldConfig);
        if (projectCategoryId > 0) {
            return getProjectCategory(projectCategoryId);
        } else {
            return "";
        }
    }

    private String getProjectCategory(Long projectCategoryId) {
        try {
            ProjectCategory projectCategory = projectManager.getProjectCategoryObject(projectCategoryId);
            return projectCategory.getName();
        } catch (DataAccessException dae) {
            return "" + projectCategoryId;
        }
    }

    @Override
    public String getObjectKey() {
        return "projectcategoryconfig";
    }

    @Override
    public Object getConfigurationObject(Issue issue, FieldConfig config) {
        Map result = new HashMap();
        result.put("projectCategoryId", DAO.getProjectCategory(config));
        return result;
    }

    @Override
    public String getBaseEditUrl() {
        return "editprojectcategory.jspa";
    }

}
