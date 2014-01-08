package com.schubergphilis.jira.plugins.customfield;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import org.apache.log4j.Logger;

import java.util.HashMap;

public class DAO {

    private static PropertySet getPS(FieldConfig fieldConfig) {
        PropertySet ofbizPs;
        HashMap<String, Object> ofbizArgs = new HashMap<String, Object>();
        ofbizArgs.put("delegator.name", "default");
        ofbizArgs.put("entityName", "com.schubergphilis.jira.plugins.workflow-support.projects-field");
        ofbizArgs.put("entityId", new Long(fieldConfig.getId()));
        ofbizPs = PropertySetManager.getInstance("ofbiz", ofbizArgs);
        return ofbizPs;
    }

    private static String getEntityName(FieldConfig fieldConfig) {
        Long context = fieldConfig.getId();
        return fieldConfig.getCustomField().getId() + "_" + context + "_config";
    }

    private static Double retrieveStoredValue(FieldConfig fieldConfig) {
        String entityName = getEntityName(fieldConfig);
        return getPS(fieldConfig).getDouble(entityName);
    }

    private static void updateStoredValue(FieldConfig fieldConfig, Double value) {
        String entityName = getEntityName(fieldConfig);
        getPS(fieldConfig).setDouble(entityName, value);
    }

    public static Long getProjectCategory(FieldConfig fieldConfig) {
        Double projectCategoryDouble = retrieveStoredValue(fieldConfig);
        return projectCategoryDouble.longValue();
    }

    public static void setProjectCategory(FieldConfig fieldConfig, Long projectCategoryId) {
        updateStoredValue(fieldConfig, projectCategoryId.doubleValue());
    }
}
