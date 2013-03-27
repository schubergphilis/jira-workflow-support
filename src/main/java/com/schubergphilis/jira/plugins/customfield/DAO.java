package com.schubergphilis.jira.plugins.customfield;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import org.apache.log4j.Logger;

import java.util.HashMap;

public class DAO {

    public static final Logger log = Logger.getLogger(DAO.class);

    private static PropertySet getPS(FieldConfig fieldConfig) {
        PropertySet ofbizPs = null;
        HashMap<String, Object> ofbizArgs = new HashMap<String, Object>();
        ofbizArgs.put("delegator.name", "default");
        ofbizArgs.put("entityName", "com.schubergphilis.jira.plugins.workflow-support.projects-field");
        ofbizArgs.put("entityId", new Long(fieldConfig.getId()));
        ofbizPs = PropertySetManager.getInstance("ofbiz", ofbizArgs);
        return ofbizPs;
    }

    private static String getEntityName(FieldConfig fieldConfig) {
        Long context = fieldConfig.getId();
        String psEntityName = fieldConfig.getCustomField().getId() + "_" + context + "_config";
        return psEntityName;
    }

    private static Double retrieveStoredValue(FieldConfig fieldConfig) {
        String entityName = getEntityName(fieldConfig);
        return getPS(fieldConfig).getDouble(entityName);
    }

    private static void updateStoredValue(FieldConfig fieldConfig, Double value) {
        String entityName = getEntityName(fieldConfig);
        getPS(fieldConfig).setDouble(entityName, value);
    }

    /**
     * @return the current Locale
     */
    public static Long getProjectCategory(FieldConfig fieldConfig) {
        Double projectCategoryDouble = retrieveStoredValue(fieldConfig);
        log.debug("Current stored: " + projectCategoryDouble);

        return projectCategoryDouble.longValue();
    }
    
    public static void setProjectCategory(FieldConfig fieldConfig, Long projectCategoryId) {
        updateStoredValue(fieldConfig, projectCategoryId.doubleValue());
    }

}
