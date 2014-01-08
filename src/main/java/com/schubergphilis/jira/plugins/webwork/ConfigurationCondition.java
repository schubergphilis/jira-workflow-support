package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.schubergphilis.jira.plugins.components.ApprovalConfiguration;

import java.util.Map;

public class ConfigurationCondition implements Condition {

    ApprovalConfiguration configuration;

    public ConfigurationCondition(ApprovalConfiguration config) {
        configuration = config;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {

    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return configuration.isCorrectlyConfigured();
    }
}
