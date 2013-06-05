package com.schubergphilis.jira.plugins.webwork;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ComplementApprovalAction extends JiraWebActionSupport {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ComplementApprovalAction.class);
    private IssueService issueService;
    private WebResourceManager webResourceManager;
    private JiraAuthenticationContext authenticationContext;
    FieldScreenManager fieldScreenManager;
    private Long subTaskId;
    FieldLayoutManager fieldLayoutManager;
    
    String editHtml = "";

    public ComplementApprovalAction(IssueService issueService, JiraAuthenticationContext authenticationContext, WebResourceManager webResourceManager, FieldLayoutManager fieldLayoutManager, FieldScreenManager fieldScreenManager) {
        this.issueService = issueService;
        this.authenticationContext = authenticationContext;
        this.webResourceManager = webResourceManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldScreenManager = fieldScreenManager;
    }

    @Override
    public String doExecute() throws Exception {
        includeResources();
        Issue issue = issueService.getIssue(getLoggedInUser(), subTaskId).getIssue();
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
        FieldScreen fieldScreen = fieldScreenManager.getFieldScreen(10000L);
         FieldScreenTab tab = fieldScreen.getTab(0);
        for (FieldScreenLayoutItem fieldScreenLayoutItem : tab.getFieldScreenLayoutItems()) {
            String fieldId = fieldScreenLayoutItem.getFieldId();
            FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(fieldId);
            HashMap hm = new HashMap();
            
            hm.put("description", "wooooot");
IssueInputParameters i = issueService.newIssueInputParameters();
i.setDescription("woooooot");
i.setSummary("summmmmmmaaaary");

OperationContext context = new OperationContextImpl(IssueOperations.EDIT_ISSUE_OPERATION, i.getActionParameters());
            editHtml += "<div class='field-group'>" +  fieldScreenLayoutItem.getEditHtml(fieldLayoutItem, context, this, issue) + "</div>";
        }
        
        return "input";
    }

    private void includeResources() {
        webResourceManager.requireResource("jira.webresources:jira-fields");
//        webResourceManager.requireResource("com.schubergphilis.jira.plugins.workflow-support:create-approval-subtask");
    }
    
    public String getEditHtml() {
     return "<h2>awesome</h2>" + editHtml;
    }
    
    public void setId(Long subTaskId) {
        this.subTaskId = subTaskId;
    }

}
