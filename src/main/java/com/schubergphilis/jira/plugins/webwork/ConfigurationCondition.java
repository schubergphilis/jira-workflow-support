package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import java.util.Map;


public class ConfigurationCondition implements Condition {
    PluginSettingsFactory factory;
    
    public ConfigurationCondition(PluginSettingsFactory pFactory) {
        factory = pFactory;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        return pluginCorrectlyConfigured(factory.createSettingsForKey(ApprovalConfigurationAction.PLUGIN_KEY));
    }

    public static boolean pluginCorrectlyConfigured(PluginSettings pSettings) {
        Object configObject = pSettings.get(ApprovalConfigurationAction.KEY_APPROVAL_SUBTASK_TYPE);
        if (configObject != null && configObject instanceof String) {
            String s = (String) configObject;
            if (!s.isEmpty()) {
                // TODO check if this thing really exists
                return true;
            }
        }
        return false;
    }
    
}
