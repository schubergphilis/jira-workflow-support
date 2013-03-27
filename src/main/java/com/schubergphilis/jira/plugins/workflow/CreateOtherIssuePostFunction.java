package com.schubergphilis.jira.plugins.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class CreateOtherIssuePostFunction extends AbstractJiraFunctionProvider {

    private static final Logger log = LoggerFactory.getLogger(CreateOtherIssuePostFunction.class);

    public static final String FIELD_NAME_PROJECTS_FIELD_ID = "projectsFieldId";
    public static final String FIELD_NAME_LOG_MESSAGE = "logMessage";
    public static final String FIELD_NAME_ISSUE_TYPE_ID = "issueTypeId";
    public static final String FIELD_NAME_LINK_TYPE_ID = "linkTypeId";
    public static final String FIELD_NAME_STATUS_ID = "statusId";

    private ProjectManager projectManager;
    private CustomFieldManager customFieldManager;

    CreateOtherIssuePostFunction(ProjectManager projectManager, CustomFieldManager customFieldManager) {
        this.projectManager = projectManager;
        this.customFieldManager = customFieldManager;
    }

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);


        Long projectsFieldId = 0L;
        try {
            projectsFieldId = Long.parseLong((String) args.get(FIELD_NAME_PROJECTS_FIELD_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String issueTypeId = (String) args.get(FIELD_NAME_ISSUE_TYPE_ID);
        String statusId = (String) args.get(FIELD_NAME_STATUS_ID);
        Long linkTypeId = Long.parseLong((String) args.get(FIELD_NAME_LINK_TYPE_ID));
        String logMessage = (String) args.get(FIELD_NAME_LOG_MESSAGE);

        Collection<Project> projects = getProjects(issue, projectsFieldId);
        issue.getProjectObject().getId();

        for (Project project : projects) {
            Issue newIssue = createIssue(project.getId(), issue, issueTypeId, statusId);
            if (logMessage != null) {
                addCommentToIssue(issue, logMessage + newIssue.getKey());
            }
            linkIssues(issue, newIssue, linkTypeId);
        }

    }

    private Collection<Project> getProjects(MutableIssue issue, Long projectsFieldId) {
        if (projectsFieldId < 1) {
            return getAllProjects();
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

    private Collection<Project> getAllProjects() {
        return projectManager.getProjectObjects();
    }

    private void linkIssues(MutableIssue oldIssue, Issue newIssue, Long linkTypeId) {
        log.debug("trying to create link from " + oldIssue.getId() + " to " + newIssue.getId());
        long sequence = 0L;
        try {
            ComponentAccessor.getIssueLinkManager().createIssueLink(oldIssue.getId(), newIssue.getId(), linkTypeId, sequence, getRemoteUser());
        } catch (CreateException e) {
            log.error("cannot create linkfrom " + oldIssue.getId() + " to " + newIssue.getId(), e);
        }
    }

    private void addCommentToIssue(Issue newIssue, String comment) {
        ComponentAccessor.getCommentManager().create(newIssue, getRemoteUser().getName(), comment, false);
    }

    private IssueInputParameters provideInput(Long projectId, Issue originatingIssue, String issuetypeId, String statusId) {
        IssueInputParameters answer = getIssueInputParameters()
                .setProjectId(projectId)
                .setIssueTypeId(issuetypeId)
                .setSummary(originatingIssue.getSummary())
                .setReporterId(getRemoteUser().getName())
                .setAssigneeId(originatingIssue.getAssigneeId())
                .setDescription(originatingIssue.getDescription())
                .setStatusId(statusId)
                .setPriorityId(originatingIssue.getPriorityObject().getId());
        return answer;
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

    private Issue createIssue(Long projectId, Issue originatingIssue, String issueTypeId, String statusId) {
        CreateValidationResult result = getIssueService().validateCreate(getRemoteUser(), provideInput(projectId, originatingIssue, issueTypeId, statusId));
        if (!result.isValid()) {
            log.error("cannot create issue");

            for (Entry<String, String> e : result.getErrorCollection().getErrors().entrySet()) {
                log.error(e.getKey() + " " + e.getValue());
            }

            log.error("" + result.getErrorCollection().getErrors().entrySet().iterator().next().getKey());
            throw new IllegalStateException("Unable to create a new linked issue while closing this one");

        }
        IssueResult answer = getIssueService().create(getRemoteUser(), result);
        if (!answer.isValid()) {
            log.error("cannot create issue, although I checked before");
        }

        return answer.getIssue();
    }


}
