

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
	/* projects field */
	var 	projectsField = new AJS.MultiSelect({
		element: AJS.$('#projects-field'),
		itemAttrDisplayed: "title",
	});
	
	new AJS.SingleSelect({
	       element: AJS.$('#projectCategoryId'),
	       itemAttrDisplayed: "title",
	});
	
	AJS.$('#projects-field-select-all').click(function(e) {
		var unselectedProjects = projectsField.model.getUnSelectedDescriptors();
		for (i=0; i<unselectedProjects.length; i++) {
			projectsField.addItem(unselectedProjects[i]);
		}
	});
	
	/* configuration action */
	new AJS.SingleSelect({
	       element: AJS.$('#approvalSubtaskType'),
	       itemAttrDisplayed: "title",
	});
	
	new AJS.MultiSelect({
		element: AJS.$('#approvalProjectIds'),
		itemAttrDisplayed: "title",
	});

});


AJS.$(function () {
    JIRA.Dialogs.approvalIssue = new JIRA.FormDialog({
        id: "schedule-dialog",
        trigger: "a.approval",
        handleRedirect: true,
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
        onDialogFinished: function() {
        	JIRA.Messages.showSuccessMsg("wooooot");
        },
        issueMsg : 'thanks_issue_updated'
    });
});


