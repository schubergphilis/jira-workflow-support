package com.schubergphilis.jira.plugins.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CreateOtherIssuePostFunction extends AbstractJiraFunctionProvider {

    private static final Logger log = LoggerFactory.getLogger(CreateOtherIssuePostFunction.class);

    public static final String FIELD_NAME_PROJECTS_FIELD_ID = "projectsFieldId";
    public static final String FIELD_NAME_LOG_MESSAGE = "logMessage";
    public static final String FIELD_NAME_ISSUE_TYPE_ID = "issueTypeId";
    public static final String FIELD_NAME_LINK_TYPE_ID = "linkTypeId";
    public static final String FIELD_NAME_STATUS_ID = "statusId";
    public static final String FIELD_NAME_COPY_CUSTOM_FIELD_VALUES = "copyCustomFieldValues";
    public static final String FIELD_NAME_COPY_ASSIGNEE = "copyAssignee";

    private ProjectManager projectManager;
    private CustomFieldManager customFieldManager;
    private IssueManager issueManager;
    private IssueFactory issueFactory;

    public CreateOtherIssuePostFunction(ProjectManager projectManager, CustomFieldManager customFieldManager, IssueManager issueManager, IssueFactory issueFactory) {
        this.projectManager = projectManager;
        this.customFieldManager = customFieldManager;
        this.issueManager = issueManager;
        this.issueFactory = issueFactory;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);


        Long projectsFieldId = 0L;
        try {
            projectsFieldId = Long.parseLong((String) args.get(FIELD_NAME_PROJECTS_FIELD_ID));
        } catch (Exception e) {
            log.error("Expected a long, but could not parse it", e);
        }
        String issueTypeId = (String) args.get(FIELD_NAME_ISSUE_TYPE_ID);
        String statusId = (String) args.get(FIELD_NAME_STATUS_ID);
        Long linkTypeId = Long.parseLong((String) args.get(FIELD_NAME_LINK_TYPE_ID));
        String logMessage = (String) args.get(FIELD_NAME_LOG_MESSAGE);
        Boolean copyAssignee = Boolean.parseBoolean((String) args.get(FIELD_NAME_COPY_ASSIGNEE));
        Boolean copyCustomFieldValues = Boolean.parseBoolean((String) args.get(FIELD_NAME_COPY_CUSTOM_FIELD_VALUES));

        Collection<Project> projects = getProjects(issue, projectsFieldId);
        issue.getProjectObject().getId();

        Collection<Issue> newIssues = new ArrayList<Issue>();

        for (Project project : projects) {
            try {
                Issue newIssue = createIssue2(project.getId(), issue, issueTypeId, statusId, copyAssignee);
                if (copyCustomFieldValues) {
                    copyCustomFields(issue, newIssue);
                }
                newIssues.add(newIssue);
                linkIssues(issue, newIssue, linkTypeId);
            } catch (Exception e) {
                String message = "(!) cannot create an issue in project " + project.getName() + ". See log for more details.";
                addCommentToIssue(issue, message);
                log.error(message, e);
            }
        }

        if (logMessage != null && !newIssues.isEmpty()) {
            String logMessageIssuePart = "";
            for (Issue nextIssue : newIssues) {
                logMessageIssuePart += " " + nextIssue.getKey();
            }
            addCommentToIssue(issue, logMessage + logMessageIssuePart);
        }

    }

    private Collection<Project> getProjects(MutableIssue issue, Long projectsFieldId) {
        if (projectsFieldId < 1) {
            return Collections.singletonList(issue.getProjectObject());
        } else {
            return getProjectsFromField(issue, projectsFieldId);
        }
    }

    private Collection<Project> getProjectsFromField(MutableIssue issue, Long projectsFieldId) {
        ArrayList<Project> answer = new ArrayList<Project>();

        CustomField customField = customFieldManager.getCustomFieldObject(projectsFieldId);
        ArrayList<Long> projectIds = (ArrayList<Long>) issue.getCustomFieldValue(customField);

        for (Long projectId : projectIds) {
            answer.add(projectManager.getProjectObj(projectId));
        }

        return answer;
    }

    private void linkIssues(MutableIssue oldIssue, Issue newIssue, Long linkTypeId) {
        log.debug("trying to create link from " + oldIssue.getId() + " to " + newIssue.getId());
        long sequence = 0L;
        try {
            ComponentAccessor.getIssueLinkManager().createIssueLink(oldIssue.getId(), newIssue.getId(), linkTypeId, sequence, getRemoteUser());
        } catch (CreateException e) {
            log.error("cannot create link from " + oldIssue.getId() + " to " + newIssue.getId(), e);
        }
    }

    private void addCommentToIssue(Issue newIssue, String comment) {
        ComponentAccessor.getCommentManager().create(newIssue, getRemoteUser().getName(), comment, false);
    }

    private IssueInputParameters provideInput(Long projectId, Issue originatingIssue, String issuetypeId, String statusId, Boolean copyAssignee) {
        IssueInputParameters answer = getIssueInputParameters()
                .setProjectId(projectId)
                .setIssueTypeId(issuetypeId)
                .setReporterId(getRemoteUser().getName())
                .setStatusId(statusId);
        copyDefaultFields(originatingIssue, answer, copyAssignee);
        return answer;
    }

    private void copyDefaultFields(Issue originatingIssue, IssueInputParameters newIssue, Boolean copyAssignee) {
        newIssue
                .setSummary(originatingIssue.getSummary())
                .setDescription(originatingIssue.getDescription())
                .setPriorityId(originatingIssue.getPriorityObject().getId());
        if (copyAssignee) {
            newIssue.setAssigneeId(originatingIssue.getAssigneeId());
        }
    }

    private void copyCustomFields(Issue originatingIssue, Issue newIssue) {
        List<CustomField> customFields = customFieldManager.getCustomFieldObjects(originatingIssue);
        for (CustomField customField : customFields) {
            Object value = customField.getValue(originatingIssue);
            customField.updateValue(null, newIssue, new ModifiedValue(null, value), new DefaultIssueChangeHolder());
        }
    }

    private IssueService getIssueService() {
        return ComponentAccessor.getIssueService();
    }

    private IssueInputParameters getIssueInputParameters() {
        return getIssueService().newIssueInputParameters();
    }

    private User getRemoteUser() {
        User user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
        return user;
    }

    private Issue createIssue2(Long projectId, Issue originatingIssue, String issueTypeId, String statusId, Boolean copyAssignee) throws CreateException {
        MutableIssue newIssue = issueFactory.getIssue();
        newIssue.setProjectId(projectId);
        newIssue.setIssueTypeId(issueTypeId);
        newIssue.setStatusId(statusId);
        newIssue.setReporterId(getRemoteUser().getName());

        if (copyAssignee) {
            newIssue.setAssignee(originatingIssue.getAssignee());
        }

        // copy over some default fields
        newIssue.setSummary(originatingIssue.getSummary());
        newIssue.setDescription(originatingIssue.getDescription());
        newIssue.setPriorityId(originatingIssue.getPriorityObject().getId());

        Issue answer = null;
        try {
            answer = issueManager.createIssueObject(getRemoteUser(), newIssue);
        } catch (CreateException e) {
            Project project = projectManager.getProjectObj(projectId);
            throw new CreateException("Could not create a new issue in project " + project.getName(), e);
        }
        return answer;
    }

    private Issue createIssue(Long projectId, Issue originatingIssue, String issueTypeId, String statusId, Boolean copyAssignee) {
        CreateValidationResult result = getIssueService().validateCreate(getRemoteUser(), provideInput(projectId, originatingIssue, issueTypeId, statusId, copyAssignee));
        if (!result.isValid()) {
            String firstError = null;

            // try and find a decent explanation for the validation failure. It can come from a lot of places, we try two here:
            for (String message : result.getErrorCollection().getErrorMessages()) {
                if (firstError == null) {
                    firstError = message;
                }
            }

            for (ErrorCollection.Reason reason : result.getErrorCollection().getReasons()) {
                if (firstError == null) {
                    firstError = reason.toString();
                }
            }
            throw new IllegalStateException("Unable to create a new linked issue in project with projectId " + projectId + ". " + (firstError != null ? firstError : ""));
        }
        IssueResult answer = getIssueService().create(getRemoteUser(), result);
        if (!answer.isValid()) {
            log.error("cannot create issue, although I checked before");
        }

        return answer.getIssue();
    }


}
