console.log("working");

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
	/* projects field */
	new AJS.MultiSelect({
		element: AJS.$('#projects-field'),
		itemAttrDisplayed: "title",
	});
	
console.log();
	new AJS.SingleSelect({
	       element: AJS.$('#projectCategoryId'),
	       itemAttrDisplayed: "title",
	});

});
