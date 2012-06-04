(function() {
    var fieldGroupDiv = jQuery('<div/>', {
        id : 'pressure-field-group'
    }).addClass('field-group');
    var actionRef;
    var assigneeFieldSelector = '#assignee-field';
    var assigneeField = jQuery(assigneeFieldSelector);
    var pressureLink = jQuery('<a/>',{
           href : actionRef,
           text : 'Задачи на этом пользователе',
           id : 'pressure-link'
       }).addClass('trigger-dialog');

    var pressureHtml = fieldGroupDiv.append(pressureLink);

    jQuery(document).delegate(assigneeFieldSelector,"click", function() {
        new JIRA.FormDialog({
            trigger: pressureLink,
            id: pressureLink.id + "-dialog",
            ajaxOptions: {
                url: pressureLink.href,
                data: {
                    decorator: "dialog",
                    inline: "true",
                }
            }
        });
        var ascentorDiv = jQuery(assigneeFieldSelector).closest('div.field-group');
        pressureHtml.insertAfter(ascentorDiv);
    });
    jQuery(document).delegate("#assignee-suggestions a","click", function(){
        var name = jQuery(assigneeFieldSelector).val();
        log.console(name);
        var assignee = jQuery('#assignee option[data-field-text="'+name+'"]').val();
        if (!actionRef) actionRef = AJS.params.baseURL + "/secure/UserPressureAction.jspa";
        if (assignee === "-1") {
           pressureHtml.hide();
        } else {
           pressureHtml.show();
           var fullLink = actionRef + '?user=' + assignee;
           console.log(fullLink);
           pressureLink.attr('href', fullLink);
        }

    });

})();
