AJS.$.namespace("AJS.Megaplan.Oppression");

var innerTrPrefix = 'innertr-';
var assigneeRowPrefix = 'assignee-row-';

var userSelectDialog;


AJS.Megaplan.Oppression.AssigneeTable = function(props) {
    if (!props) props = {};
    if (props) {
        this.props = props;
    }
    this.newAssigneeTable = function(issues, isDialogTable) {


        var innerTable = jQuery('<table/>');
        innerTable.addClass("aui"); innerTable.addClass("issuetable");

        var innerThead = jQuery('<thead/>');
        innerThead.append(jQuery('<th/>',{text: 'Пр'}))
        .append(jQuery('<th/>',{text: 'Тип'}))
        .append(jQuery('<th/>',{text: 'Сейчас'}))
        .append(jQuery('<th/>',{text: 'План'}))
        .append(jQuery('<th/>',{text: 'МгПр'}));

        var innerTbody = jQuery('<tbody/>');
        innerTable.append(innerThead);
        innerTable.append(innerTbody);
        var candidateIssueKey = props.candidateIssueKey;
        if (!props.candidateIssueKey && isDialogTable) candidateIssueKey = jQuery('#selectedInnerIssue').attr('value');
        for (var i in issues) {
            var issue = issues[i];
            innerTbody.append(getInnerTr(issue, parseInt(i)+1, candidateIssueKey, isDialogTable));
        }

        if (!isDialogTable) {

            innerThead.append(jQuery('<th/>',{text: 'Передать'}));
            innerTable.find('tr').each(function(n,e) {
                jQuery(e).append(getActionTd);
            });
        }

        addSortability(innerTbody, isDialogTable);

        addStylesTable(innerTable);


        return innerTable;

    }
    var newAssigneeTable = this.newAssigneeTable;

    var getInnerTr = function(issue, order, candidateIssueKey, isDialogTable) {
        var innerTr = jQuery('<tr/>',{id: innerTrPrefix+issue.key,'data-issuekey': issue.key});
        var isCandidateForInsert;
        if (issue.isCandidate) {
            isCandidateForInsert = true;
        }
        if (isDialogTable && !isCandidateForInsert) {
            innerTr.addClass('cancel-drugs')
        }
        if (isCandidateForInsert) {
            innerTr.css('background-color','yellow');
            innerTr.addClass('insert-candidate');
        }
        innerTr.addClass("issuerow");
        innerTr.append(getPriorityTd(issue, candidateIssueKey));
        innerTr.append(getTypeTd(issue));
        if (issue.isPlan) {
            innerTr.append(getTimeIssueInfoTd(issue));
            innerTr.append(getMainIssueInfoTd(issue));
        } else {
            innerTr.append(getMainIssueInfoTd(issue));
            innerTr.append(getTimeIssueInfoTd(issue));
        }
        innerTr.append(getMegaPriorityTd(issue, order, isCandidateForInsert, isDialogTable, candidateIssueKey));
        return innerTr;
    };

    var getPriorityTd = function(issue) {
        var td = getNavTd();
        if (issue.issuePriority) {
            var imgUrl;
            if (issue.issuePriority.substr(0,4)==="http") {
                imgUrl = issue.issuePriority;
            } else {
                imgUrl = getBaseUrl()+'/'+issue.issuePriority;
            }
            var img = jQuery('<img/>',{src: imgUrl});
            td.append(img);
        }
        var hiddenPrior = jQuery('<div/>',{text: issue.issuePrioritySequence});
        hiddenPrior.addClass("globalPrior");
        hiddenPrior.hide();


        td.append(hiddenPrior);
        return td;
    }

    var getTypeTd = function(issue) {
        var td = getNavTd();
        var imgUrl;
        if (issue.iconHtml.substr(0,4)==="http") {
            imgUrl = issue.iconHtml;
        } else {
            imgUrl = getBaseUrl()+issue.iconHtml;
        }
        var img = jQuery('<img/>',{src: imgUrl, title: issue.typeName});
        td.append(img);
        return td;
    }

    var getBaseUrl = function() {
        if (AJS.params.baseURL) return AJS.params.baseURL;
        else return AJS.gadget.getBaseUrl();
    }

    var getMainIssueInfoTd = function(issue) {
        var getPrettyTime = function(s1, s2) {
            var m1 = s1/60;
            var m2 = s2/60;
            if (((m1/60>=1||m1==0)&&(m2/60>=1||m2==0))&&!(m1==0&&m2==0)) { //we deal with hours
                return m1/60 + "h/" + m2/60 + "h";
            } else {
                return m1+"m/" + m2 +"m";
            }
        }
        var getEstimates = function(issue) {
            return "[ "+getPrettyTime(issue.timespent, issue.estimate)+" ]"
        }
        var crop = function(text) {
            return text.substr(0,50) + "...";
        }
        var getIssueLinkAndEstimates = function(issue) {
            var outerSpan = jQuery('<span/>');
            outerSpan.append(jQuery('<a/>',{href: getBaseUrl()+"/browse/"+issue.key, text: crop(issue.summary)}));
            outerSpan.append(getEstimates(issue));
            return outerSpan;
        }
        var td = getNavTd();
        td.append(getIssueLinkAndEstimates(issue));
        return td;

    }



    var getTimeIssueInfoTd = function(issue) {
        var td = getNavTd();
        td.text(AJS.Megaplan.Oppression.TimeFormatter.formatPlanTime(issue));
        if(issue.estimate == 0) td.css('color','red');
        return td;
    }

    var getMegaPriorityTd = function(issue, order, isCandidateForChange, isDialog, candidateIssueKey) {
        var td = getNavTd();
        var div = jQuery('<div/>');
        var attrs = {maxlength: 3, size: 3, value: order};
        var isDisabled;

        if (isDialog && !isCandidateForChange) {
            attrs['disabled'] = 'disabled';
            isDisabled = true;
        }

        if (isCandidateForChange) {
            attrs['name'] = 'priority';
        }


        var input = jQuery('<input/>', attrs);

        if (!isDisabled) input.change(updateMegaPriority(isDialog, candidateIssueKey));

        //if (!attrs['disabled'])   !!change here
        div.append(input);
        //div.append(a);
        td.append(div);
        return td;
    }

     var getActionTd = function() {
        var td = getNavTd();
        var a = jQuery('<a/>', {href: '#', target: '_self', text: 'Назначить на :'});
        td.append(a);
        if (!userSelectDialog) {
            if (!JIRA.Dialogs) {
                JIRA.Dialogs = {};
            }
            JIRA.Dialogs.ajsPressureAssign = new AJS.Dialog({
                width: window.frameElement?(window.frameElement.width - 20):800,
                height:400,
                id:"succession-dialog",
                closeOnOutsideClick: true
            });
            userSelectDialog = JIRA.Dialogs.ajsPressureAssign;
            userSelectDialog.addSubmit("Назначить", function (dialog) {
                var candidateTr = jQuery('#pressure-priority-table tr.insert-candidate');
                var candidateIssueKey = candidateTr.data('issuekey');
                var position = candidateTr.find('input').val();
                var newAssignee = jQuery('#filter_usors_id').val();
                AJS.$.ajax({
                    url: getBaseUrl() + "/rest/pressure/latest/oppression/megaPriority",
                    type: "POST",
                    data: JSON.stringify({
                        issue: candidateIssueKey,
                        megaPriority: parseInt(position),
                        statuses: props.statuses,
                        newAssignee: newAssignee
                    }),
                    success: function(msg) {
                        console.warn(msg);
                        var newInnerAssigneeTable = newAssigneeTable(msg[1].now.issues, false);
                        var innerAssigneeTable = jQuery('#'+jqSelector(assigneeRowPrefix+msg[1].userLogin)).find('table');
                        if (!(newInnerAssigneeTable.lenght==0)) {
                            innerAssigneeTable.replaceWith(newInnerAssigneeTable);
                        }
                        if (msg[1]) {
                            var newInnerOldAssigneeTable = newAssigneeTable(msg[0].now.issues, false);
                            var innerOldAssigneeTable = jQuery('#'+jqSelector(assigneeRowPrefix+msg[0].userLogin)).find('table');
                            if (innerOldAssigneeTable.length!=0) {
                                innerOldAssigneeTable.replaceWith(newInnerOldAssigneeTable);
                            }
                        }
                        if (props.gadget) props.gadget.resize();
                    },
                    contentType: 'application/json'
                });
                userSelectDialog.hide();
            }, 'assign-button');
            userSelectDialog.addCancel("Отменить", function (dialog) {
                userSelectDialog.hide();
            });
            userSelectDialog.addPanel("Выбор", "<div id='userselect-pressure'></div><div id='pressure-priority-table'></div><div id='selectedInnerIssue'></div>", 'main-succession-panel');
            if (props.gadget) AJS.gadget.fields.userPicker(props.gadget,'usors',"selectedInnerIssue").callback(jQuery('#userselect-pressure'));

            var assignButton = jQuery('#succession-dialog .assign-button');
            var filterUsors = jQuery('#filter_usors_id');
            var filterUsorsName = jQuery('#filter_usors_name');
            assignButton.attr('disabled','disabled');
            AJS.bind("show.dialog", function(e, data) {
                if (data.dialog.id !== 'succession-dialog') return;
                filterUsors.attr('value','');
                filterUsorsName.text('');
                filterUsors.trigger('contentchanged');
            });
            filterUsors.bind('contentchanged', function() {
                var val = this.value;
                if (!val||val.length==0) {
                    assignButton.attr('disabled','disabled');
                } else {
                    assignButton.removeAttr('disabled');
                }
                createDialogAssigneeTable(val, userSelectDialog);
            });

        }


        var createDialogAssigneeTable = function(assigneeName, dialog) {
            jQuery('#pressure-priority-table').empty();
            var issueKey = jQuery('#selectedInnerIssue').attr('value');
            if (!assigneeName||assigneeName.length == 0) return;
            var options = function () {
                return {
                    url: "/rest/pressure/1.0/oppression/generate",
                    data:  {
                        assignees : assigneeName,
                        statuses: gadgets.util.unescapeString(props.gadget.getPref("statuses")),
                        candidateIssue: issueKey,
                        candidateIssuePosition: 0
                    }
                };
            }();
            AJS.$.ajax(
             {
                url: AJS.gadget.getBaseUrl() + options.url,
                type: "GET",
                data: options.data,
                success: function(msg) {
                    var assigneeTable = newAssigneeTable(msg[0].now.issues, true);
                    var innerTable = jQuery('#pressure-priority-table');
                    innerTable.append(assigneeTable);
                },
                contentType: 'application/json'
             });

        }

        var openAssignDialog = function(event,s) {
            //update user select container because...
            var container = jQuery('#userselect-pressure #quickfind-container');
            container.remove(); // this shit caches request parameters and responces
            AJS.gadget.fields.userPicker(props.gadget,'usors',"selectedInnerIssue").callback(jQuery('#userselect-pressure'));

            updateDialogPosition(userSelectDialog);
            userSelectDialog.show();
            var issueKeyContainer = jQuery('#selectedInnerIssue');
            var issueKey = jQuery(event.target).closest('tr').data('issuekey');
            issueKeyContainer.attr('value',issueKey);
        };

        var updateDialogPosition = function(dialog) {
            var iframeId = window.frameElement.id;
            var iframePosition = window.top.jQuery('#'+iframeId).offset().top;
            var screenPosition = jQuery(window.parent).scrollTop();
            var delta = screenPosition-iframePosition;
            if (delta < 0) delta = 0;
            if (window.frameElement.height <= delta+dialog.height) return;
            var dialogDiv = jQuery('#succession-dialog');
            dialogDiv.css('top','0%');
            dialogDiv.css('margin-top', delta+100);
        };

        a.click(openAssignDialog);

        return td;
    }

    //--------------------- nav

    var getNavTd = function() {
        var td = jQuery('<td/>');
        td.addClass("nav");
        return td;
    }

    var getNavTable = function() {
        var table = jQuery('<table/>');
        innerTable.addClass("aui"); innerTable.addClass("issuetable");
        return table;
    }

    //--------------------- /nav

    //--------------------- styles

    var addStylesTable = function(innerTable) {
        innerTable.css('width','100%');
        var nowTds = innerTable.find('td:nth-child(3)');
        nowTds.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','right');
            el.css('width','30%');
        });
        var planTds = innerTable.find('td:nth-child(4)');
        planTds.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','left');
            el.css('width','30%');
        });
        var priorityThs = innerTable.find('th:nth-child(1)');
        priorityThs.each(function(i,e) {
            var el = jQuery(this);
            el.css('width','1%');
        });

        var types = innerTable.find('td:nth-child(2),th:nth-child(2)');
        types.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','center');
            el.css('width','1%');
        });

        var nowThs = innerTable.find('th:nth-child(3),th:nth-child(4)');
        nowThs.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','center');
            el.css('width','30%');
        });

        var priorityThs = innerTable.find('th:nth-child(5)');
        priorityThs.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','center');
            el.css('width','1%');
        });

        var priorityTds = innerTable.find('td:nth-child(6)');
        priorityTds.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','center');
            el.css('width','1%');
        });
        var nowThs = innerTable.find('th:nth-child(3),th:nth-child(4)');
        nowThs.each(function(i,e) {
            var el = jQuery(this);
            el.css('text-align','center');
            el.css('width','30%');
        });
    }

    //--------------------- /styles

    //--------------------- sortability

    var addSortability = function(tbody, isDialog) {
        var helper = function(e, tr) {
            var $originals = tr.children();
            var $helper = tr.clone();
            $helper.children().each(function(index)
            {
                // Set helper cell sizes to match the original sizes
                AJS.$(this).width($originals.eq(index).width());
            });
            return $helper;
        }
        var cancelSelector = ":input,button,a,.cancel-drugs";

        var updateSortable = function(event, ui) {
            var that = this;

            var high = AJS.$(ui.item).prev().data('issuekey') || '';
            var dragged = AJS.$(ui.item).data('issuekey');
            var low = AJS.$(ui.item).next().data('issuekey') || '';


            var updateTbody;
            if (props.candidateIssueKey) {
                updateTbody = updateMegaPriority(isDialog, props.candidateIssueKey);
            } else {
                updateTbody = updateMegaPriority(isDialog, dragged);
            }


            // get dragged destination row index
            var rowIndex = jQuery(ui.item).closest('table').find("tr.issuerow").index(AJS.$(ui.item));
            var newPriority = parseInt(rowIndex)+1;
            if (props.updateFunction) props.updateFunction({newPriority: newPriority}); // we use it twice for get rid of laggy table generation
            updateTbody(newPriority);
        };

        var sortableTable = AJS.$(tbody).sortable({
            axis:'y',
            cancel: cancelSelector,
            update: updateSortable,
            start: function(e, ui){
                ui.placeholder.height(ui.item.height());
            }
        });
    };

    //------------------------- /sortability

    var updateMegaPriority = function(isDialog, candidateIssueKey) {
        return function(eventOrPriority) {
            var selectedPriority = 0;
            if (jQuery.isNumeric(eventOrPriority)) {
                selectedPriority = parseInt(eventOrPriority);
            }
            else {
                selectedPriority = eventOrPriority.target.value;
                candidateIssueKey = jQuery(eventOrPriority.target).closest('tr').data('issuekey');
            }
            if (!isDialog) {
                var issueOuterTbody = jQuery('#'+innerTrPrefix+candidateIssueKey).closest('tbody');
                var issueOuterTable = issueOuterTbody.closest("table");
                issueOuterTbody.fadeTo(500,0.3);
                AJS.$.ajax({
                    url: getBaseUrl() + "/rest/pressure/latest/oppression/megaPriority",
                    type: "POST",
                    data: JSON.stringify({
                        issue: candidateIssueKey,
                        megaPriority: selectedPriority,
                        statuses: props.statuses
                    }),
                    success: function(msg) {
                        var login = jqSelector(msg[0].userLogin);
                        issueOuterTable.replaceWith(function(){
                            return newAssigneeTable(msg[0].now.issues, isDialog);
                        });
                        var innerTr = jQuery('#'+innerTrPrefix+candidateIssueKey);
                        issueOuterTbody.fadeTo(200,0, function(){
                            innerTr.hide(); innerTr.fadeIn('slow');
                        });


                    },
                    contentType: 'application/json'
                });
             } else {
                var assignees;
                if (props.assignees) {
                    assignees = props.assignees;
                } else {
                    assignees = jQuery('#filter_usors_id').val();
                }
                var data = {assignees: assignees,
                            statuses: props.statuses,
                            candidateIssuePosition: selectedPriority
                          }
                if (props.isCreate) {
                    for (var fakeParam in props.fakeIssueParams) {
                        data[fakeParam] = props.fakeIssueParams[fakeParam];
                    }
                } else {
                    data.candidateIssue = candidateIssueKey;
                }
                AJS.$.ajax({
                    url: getBaseUrl() + "/rest/pressure/latest/oppression/generate",
                    type: "GET",
                    data: data,
                    success: function(msg) {
                        var assigneeTable = newAssigneeTable(msg[0].now.issues, isDialog);
                        jQuery('#pressure-priority-table table').replaceWith(assigneeTable);
                        if (props.updateFunction) props.updateFunction({newPriority: selectedPriority});
                    },
                    contentType: 'application/json'
                });
             }

             return false;
        }
    }

    var jqSelector = function(str) {
        return str.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=>\|])/g, '\\$1');
    }

}


