package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import webwork.action.ActionContext;
import webwork.action.ActionSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class ApprovalConfigurationAction extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;

    public static final String PLUGIN_KEY = "com.schubergphilis.jira.plugins.workflow-support";

    public static final String KEY_APPROVAL_SUBTASK_TYPE = "approval.subtask.type";
    public static final String KEY_PROJECT_IDS = "approval.project.ids";

    PluginSettings pluginSettings;

    ProjectManager projectManager;

    IssueTypeManager issueTypeManager;

    private String[] projectIds = new String[0];

    private String subTaskType;

    public ApprovalConfigurationAction(IssueTypeManager issueTypeManager, ProjectManager projectManager, PluginSettingsFactory pFactory) {
        this.issueTypeManager = issueTypeManager;
        this.projectManager = projectManager;
        pluginSettings = pFactory.createSettingsForKey(PLUGIN_KEY);
    }

    @Override
    protected String doExecute() throws Exception {
        if (ActionContext.getRequest().getMethod().equals("POST")) {
            pluginSettings.put(KEY_PROJECT_IDS, StringUtils.createCommaSeperatedString(Arrays.asList(projectIds)));
            pluginSettings.put(KEY_APPROVAL_SUBTASK_TYPE, subTaskType);
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
        String configEntry = (String) pluginSettings.get(KEY_PROJECT_IDS);
        return Arrays.asList(StringUtils.splitCommaSeparatedString(configEntry));
    }

    public String getConfiguredSubtaskType() {
        return (String) pluginSettings.get(KEY_APPROVAL_SUBTASK_TYPE);
    }

    public void setProjectIds(String[] projectIds) {
        this.projectIds = projectIds;
    }

    public void setSubtaskType(String subTaskType) {
        this.subTaskType = subTaskType;
    }

}
