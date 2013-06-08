package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.schubergphilis.jira.plugins.components.ApprovalConfiguration;
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

    private ApprovalConfiguration configuration;

    public CreateApprovalAction(IssueService issueService, SubTaskManager subtaskManager, ApprovalConfiguration config) {
        this.issueService = issueService;
        this.subTaskManager = subtaskManager;
        this.configuration = config;
    }

    /**
     * Get the parent issue by id that was passed to us
     */
    private Issue getParentIssue() {
        IssueResult parentIssueResult = issueService.getIssue(getLoggedInUser(), parentIssueId);
        Issue parentIssue = parentIssueResult.getIssue();
        return parentIssue;
    }

    @Override
    public String doExecute() throws Exception {
        log.debug("request to create a subtask for " + parentIssueId);
        Issue subTask = createSubTask();
        return returnMsgToUser("/browse/" + subTask.getKey(), "created approval subtask: " + subTask.getKey(), MessageType.SUCCESS , true, null);
    }

    private Issue createSubTask() {
        Issue parentIssue = getParentIssue();
        IssueInputParameters issueInput = provideInput(getParentIssue(), getConfiguredSubtaskType());
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

        // Apparently we are responsible for creating the link here. Although I have seen this somewhere in the taskManager.
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
        answer.setSummary("Approval: " + parentIssue.getSummary());
        answer.setDescription(parentIssue.getDescription());

        return answer;
    }


    public void setParentIssueId(Long parentIssueId) {
        this.parentIssueId = parentIssueId;
    }

    String getConfiguredSubtaskType() {
        String subtaskType = (String) configuration.getSubtaskType();
        return subtaskType;
    }

}
