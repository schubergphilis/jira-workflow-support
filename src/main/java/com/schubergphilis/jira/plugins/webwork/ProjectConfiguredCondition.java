package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.schubergphilis.jira.plugins.components.ApprovalConfiguration;

import java.util.Map;

public class ProjectConfiguredCondition implements Condition {

    ApprovalConfiguration configuration;

    public ProjectConfiguredCondition(ApprovalConfiguration config) {
        configuration = config;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return configuration.isCorrectlyConfigured() && isEnabledForCurrentProject(context);
    }

    private boolean isEnabledForCurrentProject(Map<String, Object> context) {
        return configuration.isEnabledForProject((Project) context.get("project"));
    }
}
