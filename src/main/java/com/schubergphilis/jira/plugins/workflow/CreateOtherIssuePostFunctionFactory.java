package com.schubergphilis.jira.plugins.workflow;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.project.ProjectManager;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.schubergphilis.jira.plugins.customfield.ProjectsField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This is the factory class responsible for dealing with the UI for the post-function.
 * This is typically where you put default values into the velocity context and where you store user input.
 */

public class CreateOtherIssuePostFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

    public static final String PARAM_LINK_TYPES = "linkTypes";

    private IssueTypeManager issueTypeManager;
    private IssueLinkTypeManager issueLinkTypeManager;
    private StatusManager statusManager;
    private ProjectManager projectManager;
    private CustomFieldManager customFieldManager;

    public CreateOtherIssuePostFunctionFactory(CustomFieldManager customFieldManager, ProjectManager projectManager, IssueTypeManager issueTypeManager, StatusManager statusManager, IssueLinkTypeManager issueLinkTypeManager) {
        this.customFieldManager = customFieldManager;
        this.issueTypeManager = issueTypeManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.statusManager = statusManager;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put("projectFields", getCustomFields());
        velocityParams.put("linkTypes", issueLinkTypeManager.getIssueLinkTypes());
        velocityParams.put("issueTypes", issueTypeManager.getIssueTypes());
        velocityParams.put("stati", statusManager.getStatuses());
        velocityParams.put("stati", statusManager.getStatuses());
    }

    private List<CustomField> getCustomFields() {
        List<CustomField> answer = new ArrayList<CustomField>();
        // enhance by filtering out the ProjectsFields
        List<CustomField> allCustomFields = customFieldManager.getCustomFieldObjects();
        for (CustomField customField : allCustomFields) {
            if (customField.getCustomFieldType() instanceof ProjectsField) {
                answer.add(customField);
            }
        }
        return answer;
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsForView(velocityParams, descriptor);
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        copyStringToVelocityTemplate(CreateOtherIssuePostFunction.FIELD_NAME_ISSUE_TYPE_ID, velocityParams, descriptor);
        copyStringToVelocityTemplate(CreateOtherIssuePostFunction.FIELD_NAME_STATUS_ID, velocityParams, descriptor);
        copyStringToVelocityTemplate(CreateOtherIssuePostFunction.FIELD_NAME_LINK_TYPE_ID, velocityParams, descriptor);
        copyStringToVelocityTemplate(CreateOtherIssuePostFunction.FIELD_NAME_LOG_MESSAGE, velocityParams, descriptor);
    }


    /**
     * Copy the value from storage to the velocity template
     */
    private void copyStringToVelocityTemplate(String key, Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        Object storedValue = functionDescriptor.getArgs().get(key);
        velocityParams.put(key, storedValue);
    }

    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map params = new HashMap();

        // Process The map
        extractSingleString(CreateOtherIssuePostFunction.FIELD_NAME_ISSUE_TYPE_ID, formParams, params);
        extractSingleString(CreateOtherIssuePostFunction.FIELD_NAME_STATUS_ID, formParams, params);
        extractSingleLong(CreateOtherIssuePostFunction.FIELD_NAME_LINK_TYPE_ID, formParams, params);
        extractSingleString(CreateOtherIssuePostFunction.FIELD_NAME_LOG_MESSAGE, formParams, params);

        return params;
    }

    private void extractSingleString(String key, Map<String, Object> formParams, Map params) {
        String value = extractSingleParam(formParams, key);
        params.put(key, value);
    }

    private void extractSingleLong(String key, Map<String, Object> formParams, Map params) {
        String value = extractSingleParam(formParams, key);
        params.put(key, Long.parseLong(value));
    }

}
