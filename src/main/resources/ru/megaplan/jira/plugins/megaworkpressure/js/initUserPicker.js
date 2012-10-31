(function($) {

    function createAssigneePicker(ctx) {
        $(".custom-assignee-picker", ctx).each(function () {
            var control = new JIRA.AssigneePicker({
                element: $(this),
            });

            control.options.ajaxOptions.url = AJS.params.baseURL + "/rest/api/2/user/search";
            $(document).trigger('ready.single-select.assignee', control);
        });
    }

    function selectCurrentUser(context){
        var loggedInUser = JIRA.Meta.getLoggedInUser();
        var assigneeSelect = $("#assignee", context);
        assigneeSelect.trigger('set-selection-value', loggedInUser.name);
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
        var oldContent = context.find("form#pressure-priority-form #assignee-field");
        if (oldContent.length > 0) return;
        createAssigneePicker(context);
    });

})(AJS.$);