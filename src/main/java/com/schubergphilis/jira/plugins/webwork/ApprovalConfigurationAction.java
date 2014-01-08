package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.schubergphilis.jira.plugins.components.ApprovalConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import webwork.action.ActionContext;

public class ApprovalConfigurationAction extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;

    ProjectManager projectManager;

    IssueTypeManager issueTypeManager;

    private String[] projectIds = new String[0];

    private String subTaskType;

    ApprovalConfiguration configuration;

    public ApprovalConfigurationAction(IssueTypeManager issueTypeManager, ProjectManager projectManager, ApprovalConfiguration config) {
        this.issueTypeManager = issueTypeManager;
        this.projectManager = projectManager;
        configuration = config;
    }

    @Override
    protected String doExecute() throws Exception {
        if (ActionContext.getRequest().getMethod().equals("POST")) {
            configuration.setProjectIds(Arrays.asList(projectIds));
            configuration.setSubtaskType(subTaskType);
        }
        return SUCCESS;
    }

    public List<Project> getAllProjects() {
        return projectManager.getProjectObjects();
    }

    public Collection<IssueType> getAllSubtaskTypes() {
        Collection<IssueType> answer = new ArrayList<IssueType>();
        for (IssueType issueType : issueTypeManager.getIssueTypes()) {
            if (issueType.isSubTask()) {
                answer.add(issueType);
            }
        }
        return answer;
    }

    public List<String> getConfiguredProjectIds() {
        return configuration.getEnabledProjectIds();
    }

    public String getConfiguredSubtaskType() {
        return configuration.getSubtaskType();
    }

    public void setProjectIds(String[] projectIds) {
        this.projectIds = projectIds;
    }

    public void setSubtaskType(String subTaskType) {
        this.subTaskType = subTaskType;
    }
}
