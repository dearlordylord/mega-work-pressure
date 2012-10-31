AJS.$(function () {

    JIRA.Dialogs.pressureAssign = new JIRA.FormDialog({
        id: "disposition-issue-dialog",
        trigger: "a.oppression-assign-issue-link",
        ajaxOptions: JIRA.Dialogs.getDefaultAjaxOptions,
        onSuccessfulSubmit : function() {
            console.warn('succesfulSubmit');
            JIRA.Dialogs.storeCurrentIssueIdOnSucessfulSubmit();
        },
        issueMsg : "Save",
        onContentRefresh: function () {
            jQuery(".overflow-ellipsis").textOverflow();
            var context = this.get$popupContent();
            console.warn(context);
        }
    });

    if (jQuery('#oppression-assign-issue-link').length > 0) {
        jQuery('#assign-issue').closest('.toolbar-item').hide();
    }

});