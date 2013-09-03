package com.schubergphilis.jira.plugins.components;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.schubergphilis.jira.plugins.webwork.PluginConfigurationInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class ApprovalConfigurationImpl implements ApprovalConfiguration {

    public static final String PLUGIN_KEY = "com.schubergphilis.jira.plugins.workflow-support";

    public static final String KEY_APPROVAL_SUBTASK_TYPE = "approval.subtask.type";
    public static final String KEY_PROJECT_IDS = "approval.project.ids";

    private PluginSettings pluginSettings;

    private IssueTypeManager issueTypeManager;

    public ApprovalConfigurationImpl(PluginSettingsFactory pluginSettingsFactory, IssueTypeManager issueTypeManager) {
        pluginSettings = pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY);
        this.issueTypeManager = issueTypeManager;
    }

    @Override
    public boolean isCorrectlyConfigured() {
        Object configObject = pluginSettings.get(KEY_APPROVAL_SUBTASK_TYPE);
        if (configObject != null && configObject instanceof String) {
            String s = (String) configObject;
            if (!s.isEmpty() && issueTypeManager.getIssueType(s) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSubtaskType() {
        if (!isCorrectlyConfigured()) {
            throw new PluginConfigurationInvalidException();
        }
        return (String) pluginSettings.get(KEY_APPROVAL_SUBTASK_TYPE);
    }

    /**
     * This could/should have return List<Long>. But it's currently only used as strings, so we don't bother.
     */
    @Override
    public List<String> getEnabledProjectIds() {
        String configEntry = (String) pluginSettings.get(KEY_PROJECT_IDS);
        return Arrays.asList(StringUtils.splitCommaSeparatedString(configEntry));
    }

    @Override
    public void setProjectIds(List<String> asList) {
        pluginSettings.put(ApprovalConfigurationImpl.KEY_PROJECT_IDS, StringUtils.createCommaSeperatedString(asList));
    }

    @Override
    public void setSubtaskType(String subTaskType) {
        pluginSettings.put(ApprovalConfigurationImpl.KEY_APPROVAL_SUBTASK_TYPE, subTaskType);
    }

    @Override
    public boolean isEnabledForProject(Project project) {
        // Note: we check for a string
        return getEnabledProjectIds().contains("" + project.getId());
    }

}
