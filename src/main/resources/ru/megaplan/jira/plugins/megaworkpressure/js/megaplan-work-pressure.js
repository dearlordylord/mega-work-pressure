(function() {
    var fieldGroupDiv = jQuery('<div/>', {
        id : 'pressure-field-group'
    }).addClass('field-group');
    var actionRef = "UserPressureAction.jspa";
    var assigneeFieldSelector = '#assignee-field';
    var assigneeField = jQuery(assigneeFieldSelector);
    var pressureLink = jQuery('<a/>',{
           href : actionRef,
           text : 'Задачи на этом пользователе',
           id : 'pressure-link'
       }).addClass('trigger-dialog');

    var pressureHtml = fieldGroupDiv.append(pressureLink);

    jQuery(document).delegate(assigneeFieldSelector,"click", function(){
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
        pressureHtml.hide();
    });
    jQuery(document).delegate("#assignee-suggestions a","click", function(){
        var assignee = jQuery(assigneeFieldSelector).val();
        if (assignee === 'Automatic') {
           pressureHtml.hide();
        } else {
           pressureHtml.show();
           pressureLink.attr('href',actionRef + '?user=' + assignee);
        }

    });

})();
