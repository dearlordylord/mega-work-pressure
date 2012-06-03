
var contextPath = "http://127.0.0.1:2990/jira"; // Adding context path as it is required for actions dropdowns 
gadgets.window.setTitle("__MSG_gadget.assignedtome.title__");
AJS.Gadget({
    baseUrl: "http://127.0.0.1:2990/jira",
    useOauth: "/rest/gadget/1.0/currentUser",
    config: {
        descriptor: function (args) {
            var gadget = this;
            var columnType = function (name) {
                    var options = [{
                        label: gadget.getMsg("gadget.common.default.columns"),
                        value: "--Default--"
                    }];
                    options = options.concat(args.columnChoices.availableColumns);
                    var colList = " ";
                    AJS.$.each(args.columnChoices.defaultColumns, function () {
                        if (colList !== " ") {
                            colList += ", ";
                        }
                        colList += gadgets.util.escapeString(this.label); // first time no comma 
                    });
                    var desc = gadget.getMsg("gadget.common.columns.to.display.description") + colList;
                    return {
                        userpref: name,
                        label: gadget.getMsg("gadget.common.columns.to.display.label"),
                        description: desc,
                        type: "multiselect",
                        selected: gadget.getPref(name),
                        options: options,
                        value: gadget.getPrefArray("columnNames")
                    };
                };
            return {
                action: "/rest/gadget/1.0/issueTable/jql/validate",
                theme: function () {
                    if (gadgets.window.getViewportDimensions().width < 450) {
                        return "gdt top-label";
                    } else {
                        return "gdt";
                    }
                }(),
                fields: [AJS.gadget.fields.numberToShow(gadget, "num"), columnType("columnNames"), AJS.gadget.fields.nowConfigured()]
            };
        },
        args: [{
            key: "columnChoices",
            ajaxOptions: "/rest/gadget/1.0/availableColumns"
        }]
    },
    view: {
        onResizeAdjustHeight: true,
        enableReload: true,
        template: function (args) {
            var gadget = this;
            args.linkHtml = "<a href=\"http://127.0.0.1:2990/jira/secure/IssueNavigator.jspa?reset=true&jqlQuery=assignee+%3D+currentUser%28%29+AND+resolution+%3D+unresolved+ORDER+BY+priority+DESC%2C+created+ASC\" target=\"_parent\" title=\"__MSG_gadget.assignedtome.title__\">";
            args.linkEndHtml = "</a>";
            args.issueTable.empty = args.issueTable.displayed === 0;
            args.issueTable.hasPaging = args.issueTable.displayed === args.issueTable.total;
            gadget.getView().attr("id", "assigned-to-me-content").html(JIRA.Templates.Gadgets.assignedToMe(args));
            var $resultCountLink = gadget.getView().find(".results-count-link");
            $resultCountLink.replaceWith(AJS.$(args.linkHtml + $resultCountLink.html() + args.linkEndHtml));
            AJS.$("th.sortable").each(function () {
                this.onclick = null;
            }).click(function () {
                gadget.savePref("sortColumn", AJS.$(this).attr("rel"));
                gadget.sortBy = AJS.$(this).attr("rel");
                gadget.showView(true);
            });
            AJS.$(".pagination a").click(function (event) {
                event.preventDefault();
                gadget.startIndex = AJS.$(this).attr("rel");
                gadget.showView(true);
            });
            if (gadget.isLocal()) {
                if (typeof contextPath === "undefined") {
                    contextPath = "http://127.0.0.1:2990/jira";
                }
                AJS.Dropdown.create({
                    trigger: ".issue-actions-trigger",
                    autoScroll: false,
                    ajaxOptions: {
                        dataType: "json",
                        cache: false,
                        formatSuccess: JIRA.FRAGMENTS.issueActionsFragment
                    }
                });
            } // Apply hover class to issuetable 
            jQuery("#issuetable tr").hover(function () {
                jQuery(this).addClass("hover");
            }, function () {
                if (!AJS.dropDown.current) {
                    jQuery(this).removeClass("hover");
                }
            });
        },
        args: [{
            key: "issueTable",
            ajaxOptions: function () {
                var gadget = this;
                var columnNames = gadget.getPrefArray("columnNames");
                var hasDefault = false;
                var indexOf = -1;
                for (var i = 0; i < columnNames.length; i++) {
                    if (columnNames[i] === "--Default--") {
                        hasDefault = true;
                        indexOf = i;
                        break;
                    }
                }
                if (hasDefault) {
                    columnNames.splice(indexOf, 1);
                }
                if (!this.sortBy) {
                    this.sortBy = gadget.getPref("sortColumn") || null;
                }
                return {
                    url: "/rest/gadget/1.0/issueTable/jql",
                    data: {
                        jql: "assignee = currentUser() AND resolution = unresolved ORDER BY priority DESC, created ASC",
                        num: this.getPref("num"),
                        addDefault: hasDefault,
                        columnNames: columnNames,
                        enableSorting: true,
                        sortBy: function () {
                            if (gadget.sortBy && gadget.sortBy !== "") {
                                return gadget.sortBy;
                            } else {
                                return null;
                            }
                        }(),
                        paging: true,
                        startIndex: function () {
                            if (gadget.startIndex && gadget.startIndex !== "") {
                                return gadget.startIndex;
                            } else {
                                return "0";
                            }
                        }(),
                        showActions: gadget.isLocal()
                    }
                };
            }
        }]
    }
}); 