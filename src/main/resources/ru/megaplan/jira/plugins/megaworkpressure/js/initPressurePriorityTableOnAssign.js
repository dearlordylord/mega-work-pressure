(function () {


    function init(e, context) {
        var issueKey = JIRA.Meta.getIssueKey();
        var pressurePriorityForm = jQuery('#pressure-priority-form');
        var pressurePriorityDiv = jQuery("#pressure-priority-table");

        var data = {
            candidateIssue: issueKey
        };
        var assignee;
        if (e && context && e.target.id == 'assignee') {
            assignee = context.properties.value;
            data['assignees'] = assignee;
        }
        var props = {candidateIssueKey:issueKey};
        if (assignee) props.assignees = assignee;
        var oppressionTable = new AJS.Megaplan.Oppression.AssigneeTable(props);


        AJS.$.ajax({
            url: AJS.params.baseURL+"/rest/pressure/1.0/oppression/generate",
            dataType: "json",
            type: "GET",
            data: data,
            success: function( msg ) {
                var table = oppressionTable.newAssigneeTable(msg[0].now.issues, true);
                pressurePriorityDiv.empty();
                pressurePriorityDiv.append(table);
                console.warn(table);
                JIRA.Dialogs.pressureAssign._positionInCenter(); //refresh height
            },
            contentType: 'application/json'
        });

    }

    init();

    JIRA.bind("selected", function (e, context) {
        if (e.target.form.id != "pressure-priority-form") return;
        init(e, context);
    });

})();