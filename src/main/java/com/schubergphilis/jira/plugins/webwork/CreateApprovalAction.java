package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.schubergphilis.jira.plugins.workflow.IssueCreationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

public class CreateApprovalAction extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CreateApprovalAction.class);

    private Long parentIssueId;

    IssueService issueService;

    SubTaskManager subTaskManager;

    PluginSettings pluginSettings;

    public CreateApprovalAction(IssueService issueService, SubTaskManager subtaskManager, PluginSettingsFactory pluginSettingsFactory) {
        this.issueService = issueService;
        this.subTaskManager = subtaskManager;
        pluginSettings = pluginSettingsFactory.createSettingsForKey(ApprovalConfigurationAction.PLUGIN_KEY);
    }

    @Override
    public String doExecute() throws Exception {
        log.debug("request to create a subtask for " + parentIssueId);
        Issue subTask = createSubTask();
        return getRedirect("/secure/ComplementApprovalAction.jspa?id=" + subTask.getId() + addDialogParametersWhenInline());
    }

    private Issue createSubTask() {
        IssueResult parentIssueResult = issueService.getIssue(getLoggedInUser(), parentIssueId);
        Issue parentIssue = parentIssueResult.getIssue();
        IssueInputParameters issueInput = provideInput(parentIssue, getConfiguredSubtaskType());
        CreateValidationResult result = issueService.validateSubTaskCreate(getLoggedInUser(), parentIssue.getId(), issueInput);
        if (!result.isValid()) {
            String firstError = null;
            for (Entry<String, String> e : result.getErrorCollection().getErrors().entrySet()) {
                log.error(e.getKey() + " " + e.getValue());
                if (firstError == null) {
                    firstError = e.getKey() + ": " + e.getValue();
                }
            }

            throw new IllegalStateException("Unable to create a new subtask issue: " + firstError);
        }
        IssueResult answer = issueService.create(getLoggedInUser(), result);

        try {
            subTaskManager.createSubTaskIssueLink(parentIssue, answer.getIssue(), getLoggedInUser());
        } catch (CreateException ce) {
            throw new IssueCreationException();
        }

        if (!answer.isValid()) {
            log.error("cannot create issue, although I checked before");
        }

        return answer.getIssue();
    }

    private IssueInputParameters provideInput(Issue parentIssue, String issuetypeId) {
        IssueInputParameters answer = issueService.newIssueInputParameters()
                .setProjectId(parentIssue.getProjectObject().getId())
                .setIssueTypeId(issuetypeId)
                .setReporterId(getLoggedInUser().getName());

        // copy some basic fields
        answer.setSummary(parentIssue.getSummary());
        answer.setDescription(parentIssue.getDescription());

        return answer;
    }


    private String addDialogParametersWhenInline() {
        if (isInlineDialogMode()) {
            return "&decorator=dialog&inline=true";
        } else {
            return "";
        }
    }

    public void setParentIssueId(Long parentIssueId) {
        this.parentIssueId = parentIssueId;
    }

    String getConfiguredSubtaskType() {
        if (pluginCorrectlyConfigured()) {
            String subtaskType = (String) pluginSettings.get(ApprovalConfigurationAction.KEY_APPROVAL_SUBTASK_TYPE);
            return subtaskType;
        } else {
            throw new PluginNotConfiguredException();
        }
    }

    private boolean pluginCorrectlyConfigured() {
        return ConfigurationCondition.pluginCorrectlyConfigured(pluginSettings);
    }

}
