(function() {
    var fieldGroupDiv = jQuery('<div/>', {
        id : 'pressure-field-group'
    }).addClass('field-group');
    var actionRef;
    var assigneeFieldSelector = '#assignee-field';
    var assigneeField = jQuery(assigneeFieldSelector);
    var findUsername = /\(([^()]*)\)$/;
    var pressureLink = jQuery('<a/>',{
           href : actionRef,
           text : 'Задачи на этом пользователе',
           id : 'pressure-link'
       }).addClass('trigger-dialog');

    var pressureHtml = fieldGroupDiv.append(pressureLink);
    var dialog;

    jQuery(document).delegate(assigneeFieldSelector,"click", init);
    function init() {
        actionRef = AJS.params.baseURL + "/secure/UserPressureAction.jspa";
        if (!dialog) dialog =
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
    }
    jQuery(document).delegate("#assignee-suggestions a","click", function(){
        var pr = jQuery('#pressure-link');
        pr.hover(function() {
            var option = jQuery('#assignee-single-select li.active a');
            var name = findUsername.exec(option.attr('title'));
            if (!name) {
                pressureHtml.hide();
                return;
            } else {
                name = '?user=' + name[1];
            }
            pr.attr('href', actionRef + name);
        });
       pressureHtml.show();
       pressureLink.attr('href',actionRef);
    });
    jQuery(document).delegate('#assign-to-me-trigger','click', function() {
        init();
        var pr = jQuery('#pressure-link');
        pr.unbind('mouseenter mouseleave');
        var name = jQuery('#assignee-field').val();
        if (!name) {
            pressureHtml.hide();
            return;
        }
        name = '?user=' + name;
        pr.attr('href', actionRef + name);
        pressureHtml.show();
    });


})();
