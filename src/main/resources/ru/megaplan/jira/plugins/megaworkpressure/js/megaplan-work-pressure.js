(function() {
    var fieldGroupDiv = jQuery('<div/>',{
            class : 'field-group'
        });
    var actionRef = "http://www.google.com";
    var pressureLink = jQuery('<a/>',{
           href : actionRef,
           text : "lolgoogle"
       });
    var pressureHtml = fieldGroupDiv.append(pressureLink);

    jQuery(document).delegate("#assignee-field","click", function(){
        var ascentorDiv = jQuery('#assignee-field').closest('div.field-group') ;
        pressureHtml.insertAfter(ascentorDiv);
    });
    jQuery(document).delegate("#assignee-suggestions a","click", function(){
        console.log('log');
        jQuery(pressureLink).attr('href','GOGAL');
    });

})();
