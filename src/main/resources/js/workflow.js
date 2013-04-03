
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

});

