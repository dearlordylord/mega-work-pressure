(function() {
    var pressureDialogData = {
        decorator: "dialog",
        inline: "true",
        dataType: "html"
    }
    var lastWorklogData = {
        decorator: "dialog",
        inline: "true",
        dataType: "html",
        weekends: true,
        showUsers: true,
        reportKey: 'jira-timesheet-plugin:report'
    }
    var reportRef;
    var fieldGroupDiv = jQuery('<div/>', {
        id : 'pressure-field-group'
    }).addClass('field-group');
    var actionRef;
    var findUsername = /\(([^()]*)\)$/;
    var pressureLink = jQuery('<a/>',{
           href : '#',
           text : 'Задачи на этом пользователе',
           id : 'pressure-link',
       }).addClass('trigger-dialog');

    var pressureHtml = fieldGroupDiv.append(pressureLink);
    var dialog;
    var projectKey;
    var priority;
    var selectedUser;
    var selectedUserName;
    var selectedProject;

    jQuery(document).delegate('#assignee-field',"click", init);

    function init() {
        if (!dialog) {
           dialog = new AJS.Dialog({
                width:1280,
                height:800,
                id:"work-pressure-dialog",
                closeOnOutsideClick: true
            });

            dialog.addPanel("В будущем", "<p>Загрузка... Или нет.</p>", "panel-body-pressure");
            dialog.addPanel("В прошлом", "<p>Загрузка... Или нет.</p>", "panel-body-worklogs");
            dialog.addButton("Done", function (dialog) {
                dialog.hide();
            });
            actionRef = AJS.params.baseURL + "/secure/UserPressureAction.jspa";
            reportRef = AJS.params.baseURL + "/secure/ConfigureReport.jspa";

            jQuery(document).delegate('#pressure-link','click', function(){

                dialog.addHeader("Трудолюбие : " + selectedUserName);
                pressureDialogData.user = selectedUser;
                pressureDialogData.priority = priority;
                lastWorklogData.targetUser = selectedUser;
                pressureDialogData.project = selectedProject;
                var dateStart = new Date();
                dateStart.setDate(dateStart.getDate() - 7);
                var dateEnd = new Date();


                AJS.$.ajax({
                    url: actionRef,
                    type:'post',
                    data:pressureDialogData,
                    async:false,
                    success:function(data){
                        dialog.getPanel(0).html(data);
                        //it necessary to be here, workpressure.date.format injecting statically from workpressure action
                        lastWorklogData.startDate = dateStart.print(AJS.params['workpressure.date.format']);
                        lastWorklogData.endDate = dateEnd.print(AJS.params['workpressure.date.format']);
                        var originalEstimateParentText = jQuery('#create-issue-dialog #timetracking_originalestimate');
                        if (!originalEstimateParentText[0]) originalEstimateParentText = jQuery('#timetracking_originalestimate');
                        originalEstimateParentText = originalEstimateParentText.val();
                        if (originalEstimateParentText && originalEstimateParentText.length > 0) {
                            jQuery('#dummy-estimate').val(parseFloat(originalEstimateParentText.replace(',','.')));
                            var granularitySelect = jQuery('#dummy-time-granularity');
                            var lastSymbol = originalEstimateParentText[originalEstimateParentText.length-1];
                            switch (lastSymbol) {
                                case 'h':
                                    console.log('h');
                                    granularitySelect.val('h');
                                    break;
                                case 'm':
                                    granularitySelect.val('m');
                                    break;
                                case 'd':
                                    granularitySelect.val('d');
                                    break;
                            }
                        }
                        var summary = jQuery('#create-issue-dialog #summary');
                        if (!summary[0]) summary = jQuery('#summary');
                        summary = summary.val();
                        if (summary && summary.length > 0) {
                            jQuery('#dummy-summary').text(summary);
                        }

                        jQuery('#dummy-estimate').keypress(function( b ){
                             var C = /[0-9\x25\x27\x24\x23]/;
                             var a = b.which;
                             console.warn(a);
                             var c = String.fromCharCode(a);
                             return !!(a==0||a==8||a==9||a==13||a==44||a==46||c.match(C));
                         });


                        initPressureBeginEnd();
                    }
                });
                AJS.$.ajax({
                    url: reportRef,
                    type:'post',
                    data:lastWorklogData,
                    async:true,
                    success:function(data){
                        dialog.getPanel(1).html(data);
                        jQuery('.panel-body-worklogs .content-body').css('border-color','white');
                        jQuery('.panel-body-worklogs .content-body .aui-message').hide();
                    }
                });
                dialog.gotoPanel(0);
                jQuery('#work-pressure-dialog .dialog-page-menu').css('width','10%');
                dialog.show();
            });
        }
        projectKey = jQuery('#assignee-container fieldset.hidden.parameters input').attr('value') || jQuery('#issue-create-project-name').text();
        var enabledProjects = AJS.params['workpressure.enabled.projects'];
        if (!enabledProjects || !enabledProjects[projectKey]) return;
        var assigneeField = jQuery('#create-issue-dialog').find('#assignee-field'); //dynamic
        if (!assigneeField[0]) assigneeField = jQuery('#assignee-field');
        var ascentorDiv = assigneeField.closest('div.field-group');
        pressureHtml.insertAfter(ascentorDiv);

    }
    jQuery(document).delegate("#assignee-field","click", function(e){
        init();
        var pr = jQuery('#pressure-link');
        pr.hover(initHover);
        function initHover() {
            var pr = jQuery('#create-issue-dialog #pressure-link');
            if(!pr[0]) pr = jQuery('#pressure-link');
            var option = jQuery('#create-issue-dialog #assignee-single-select li.active a');
            if(!option[0]) option = jQuery('#assignee-single-select li.active a');
            var name = findUsername.exec(option.attr('title'));

            var assigneeInBox = jQuery('#assignee-group-suggested option[data-field-text="'+jQuery('#assignee-field').val()+'"]').attr('value');
            if (assigneeInBox === '-1') name = null;

            if (!name && assigneeInBox === '-1') {
                pressureHtml.hide();
                return;
            } else if (!name && assigneeInBox !== '-1') {
                selectedUser = assigneeInBox;

                selectedUserName = jQuery('#assignee-field').val();
                pressureDialogData.user = jQuery('#assignee-field').val();
            } else {
                selectedUser = name[1];
                selectedUserName = jQuery('#create-issue-dialog #assignee-field');
                if (!selectedUserName[0]) selectedUserName = jQuery('#assignee-field');
                selectedUserName = selectedUserName.val();
                pressureDialogData.user = name[1]; //or selectedUserName dunno
            }

            priority = jQuery('select#priority').val();
            selectedProject = projectKey;

            //pr.attr('href', actionRef + name);
        }
       pressureHtml.show();
       //pressureLink.attr('href',actionRef);
    });
    jQuery(document).delegate('#assign-to-me-trigger','click', function() {
        if (!dialog) init();
        var pr = jQuery('#pressure-link');
        pr.unbind('mouseenter mouseleave');
        var name = jQuery('#assignee-field').val();
        if (!name) {
            pressureHtml.hide();
            return;
        }
        priority = jQuery('select#priority').val();
        selectedUser = name;
        selectedUserName = name;
        selectedProject = projectKey;
        pressureHtml.show();
    });

    jQuery(document).delegate('#priority-change-pipka', 'change', function(pr) {
        var newPriority = jQuery(pr.target[pr.target.selectedIndex]).val();
        var element = jQuery('.dummyissue');
        var nextElement = jQuery(element.next());
        var previousElement = jQuery(element.prev());
        var nextPriority = getPriority(nextElement);
        var previousPriority = getPriority(previousElement);
        if (nextElement && nextPriority <= newPriority) {
            while(nextPriority <= newPriority) {
                var next = jQuery(nextElement.next());
                nextPriority = getPriority(next);
                if (!isNaN(nextPriority) && nextPriority <= newPriority) nextElement = next;
                else break;
            }
            element.insertAfter(nextElement);
        } else if (previousElement && previousPriority > newPriority) {
            var after = true;
            while(previousPriority > newPriority) {
               var previous = jQuery(jQuery(previousElement).prev());
               previousPriority = getPriority(previous);
               if (!isNaN(previousPriority)) previousElement = previous;
               else {
                   after = false;
                   break;
               }
           }
           if (after) element.insertAfter(previousElement);
           else element.insertBefore(previousElement);
        }

        var panel = jQuery('.panel-body-pressure');
        panel.scrollTo(element);

        jQuery('select#priority').val(newPriority);

        function getPriority(element) {
            return parseInt(element.attr('class'));
        }

        initPressureBeginEnd();


    });

    jQuery(document).delegate('#dummy-estimate', 'change', passEstimateToParent);
    jQuery(document).delegate('#dummy-time-granularity', 'change', passEstimateToParent);

    function passEstimateToParent() {
        var estimate = jQuery('#dummy-estimate');
        var estimateParent = jQuery('#create-issue-dialog #timetracking_originalestimate');
        if (!estimateParent[0]) estimateParent = jQuery('#timetracking_originalestimate') ;
        if (estimate.val() && estimate.val().length > 0) estimate = parseFloat(estimate.val().replace(',','.'));
        if (!estimate || isNaN(estimate)) {
            estimateParent.val(''); return;
        }
        var granularity = jQuery('#dummy-time-granularity').val();
        estimateParent.val(estimate + granularity);
        initPressureBeginEnd();
    }

    jQuery(document).delegate('#dummy-time-granularity', 'change', function() {
        jQuery('#dummy-estimate').change();
    });


    function copyArray(arr) {
        var result = [];
        for (var i = 0; i < arr.length; ++i) {
            result[i] = arr[i];
        }
        return result;
    }

    function initPressureBeginEnd() {
        var cText = {
            months: ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"],
        };
        var nonWorkingDates = copyArray(AJS.params['workpressure.non.working.intervals']);
        var hoursPerDay = AJS.params['workpressure.hours.per.day'];
        var daysPerWeek = AJS.params['workpressure.days.per.week']
        var beginDate = new Date();
        var nextInterval = nonWorkingDates.shift();
        sanitizeNonWorkingTail(beginDate);
        if (inInterval(beginDate, nextInterval)) {
            beginDate.setTime(nextInterval.end.getTime());
            nextInterval = nonWorkingDates.shift();
        }
        var endDate = new Date(beginDate.getTime());
        function inInterval(date, interval) {
            return (date.getTime() >= interval.begin.getTime() && date.getTime() < interval.end.getTime());
        }
        jQuery('#work-pressure-issuetable tbody tr').each(function(n,e){

            beginDate = new Date(endDate.getTime());

            var element = jQuery(e);
            var currentEstimate = getEstimate(element);
            if (!currentEstimate) {
                currentEstimate = 0;
                if (!isDummy(element)) element.css("background-color","pink");
                //return;
            }
            var granularity = element.find('#dummy-time-granularity').val();
            if (!granularity) granularity = 'h';
            addToDate(endDate, currentEstimate, granularity);
            var remainingTime = endDate.getTime() - beginDate.getTime();
            endDate = new Date(beginDate.getTime()); //we use endDate first for remaining time calculation
            if (!nextInterval) console.error("non-working intervals is end");
            if (nonWorkingDates.length != 0) addPadding(endDate);
            //element.find('.pressure-begin').text(toMonthAndDay(beginDate) + beginDate.toString());
            element.find('.pressure-begin').text(toMonthAndDay(beginDate));
            //element.find('.pressure-end').text(toMonthAndDay(endDate) + endDate.toString());
            element.find('.pressure-end').text(toMonthAndDay(endDate));

            function addPadding(endDate) {

                if (remainingTime <= 0) {
                    return;
                } else if (!nextInterval) {
                    console.warn("nextInterval isn't exist");
                    return;
                } else if(endDate.getTime() >= nextInterval.begin.getTime() &&
                endDate.getTime() < nextInterval.end.getTime()) {
                    endDate.setTime(nextInterval.end.getTime());
                    nextInterval = nonWorkingDates.shift();
                    return addPadding(endDate);
                } else if (endDate.getTime()+remainingTime < nextInterval.begin.getTime()) {
                    console.log("endDate not in interval : " + nextInterval.begin + " - " + nextInterval.end);
                    endDate.setTime(endDate.getTime()+remainingTime);
                    remainingTime = 0;
                    return;

                } else if (endDate.getTime()+remainingTime >= nextInterval.begin.getTime()) {

                    var chopTime = nextInterval.begin.getTime() - endDate.getTime();
                    if (chopTime > 0) {
                        remainingTime -= chopTime;
                        endDate.setTime(nextInterval.end.getTime());
                    } else {
                        console.error("CHOP TIME SMALLER THAN 0 : " + chopTime);
                    }
                    nextInterval = nonWorkingDates.shift();
                    return addPadding(endDate);
                }

            }

            function toMonthAndDay(date) {
                var month = cText.months[date.getUTCMonth()];
                var day = date.getUTCDate();
                return day + " " + month;
            }
            function addToDate(date, num, granularity) {
                var minutes = 0;
                switch (granularity) {
                    case 'd':
                        minutes = num*hoursPerDay*60;
                        break;
                    case 'h':
                        minutes = num*60;
                        break;
                    case 'm':
                        minutes = num;
                        break;
                    default:
                        console.error('what format? : ' + granularity);
                        minutes = num*60;
                }
                date.setMinutes(date.getMinutes()+minutes);
            }
            function isDummy(element) {
                return (element.find('#dummy-estimate').length !== 0);
            }
        });
        function sanitizeNonWorkingTail(endDate) {
            while (nextInterval && endDate.getTime() >= nextInterval.begin.getTime() &&
            endDate.getTime() >= nextInterval.end.getTime()) {
                nextInterval = nonWorkingDates.shift();
            }
        }
        function chop(beginDate, endDate, nonWorkingDates) {
            var pair = nonWorkingDates[0];
            var chop =  pair.start.getTime() - beginDate.getTime();
            var nonWorkingChop = pair.end.getTime() - pair.start.getTime();
            return chop;
        }

        function getFirstNumberPart(value) {
            return parseInt(value.toString().split('.')[0]);
        }
        function getSecondNumberPart(value) {
            var minutesPartStr = value.toString().replace(',','.').split('.')[1];
            if (!minutesPartStr || minutesPartStr.length == 0) return 0;
            while (minutesPartStr.substr(0,1) == '0' && minutesPartStr.length>1)
            { minutesPartStr = minutesPartStr.substr(1,9999); }
            minutesPart = parseInt(0+','+minutesPartStr);
            return 60*minutesPart;
        }
        function getEstimate(element) {
            var estimateCell = element.find('td.pressure-estimate');
            var result;
            var dummyString = estimateCell.find('#dummy-estimate');
            if (dummyString) {
                if (dummyString.val() && dummyString.val().length > 0) {
                    result = parseFloat(dummyString.val().replace(',','.'));
                    return result;
                }
            }
            if (estimateCell.text() && estimateCell.text().length > 0) result = parseFloat(estimateCell.text().replace(',','.'));
            return result;
        }
    }   //if (e.target.value && e.target.value.length > 0) estimate = parseFloat(e.target.value.replace(',','.'));


    ;(function( $ ){

    	var $scrollTo = $.scrollTo = function( target, duration, settings ){
    		$(window).scrollTo( target, duration, settings );
    	};

    	$scrollTo.defaults = {
    		axis:'xy',
    		duration: parseFloat($.fn.jquery) >= 1.3 ? 0 : 1,
    		limit:true
    	};

    	// Returns the element that needs to be animated to scroll the window.
    	// Kept for backwards compatibility (specially for localScroll & serialScroll)
    	$scrollTo.window = function( scope ){
    		return $(window)._scrollable();
    	};

    	// Hack, hack, hack :)
    	// Returns the real elements to scroll (supports window/iframes, documents and regular nodes)
    	$.fn._scrollable = function(){
    		return this.map(function(){
    			var elem = this,
    				isWin = !elem.nodeName || $.inArray( elem.nodeName.toLowerCase(), ['iframe','#document','html','body'] ) != -1;

    				if( !isWin )
    					return elem;

    			var doc = (elem.contentWindow || elem).document || elem.ownerDocument || elem;

    			return $.browser.safari || doc.compatMode == 'BackCompat' ?
    				doc.body :
    				doc.documentElement;
    		});
    	};

    	$.fn.scrollTo = function( target, duration, settings ){
    		if( typeof duration == 'object' ){
    			settings = duration;
    			duration = 0;
    		}
    		if( typeof settings == 'function' )
    			settings = { onAfter:settings };

    		if( target == 'max' )
    			target = 9e9;

    		settings = $.extend( {}, $scrollTo.defaults, settings );
    		// Speed is still recognized for backwards compatibility
    		duration = duration || settings.duration;
    		// Make sure the settings are given right
    		settings.queue = settings.queue && settings.axis.length > 1;

    		if( settings.queue )
    			// Let's keep the overall duration
    			duration /= 2;
    		settings.offset = both( settings.offset );
    		settings.over = both( settings.over );

    		return this._scrollable().each(function(){
    			var elem = this,
    				$elem = $(elem),
    				targ = target, toff, attr = {},
    				win = $elem.is('html,body');

    			switch( typeof targ ){
    				// A number will pass the regex
    				case 'number':
    				case 'string':
    					if( /^([+-]=)?\d+(\.\d+)?(px|%)?$/.test(targ) ){
    						targ = both( targ );
    						// We are done
    						break;
    					}
    					// Relative selector, no break!
    					targ = $(targ,this);
    				case 'object':
    					// DOMElement / jQuery
    					if( targ.is || targ.style )
    						// Get the real position of the target
    						toff = (targ = $(targ)).offset();
    			}
    			$.each( settings.axis.split(''), function( i, axis ){
    				var Pos	= axis == 'x' ? 'Left' : 'Top',
    					pos = Pos.toLowerCase(),
    					key = 'scroll' + Pos,
    					old = elem[key],
    					max = $scrollTo.max(elem, axis);

    				if( toff ){// jQuery / DOMElement
    					attr[key] = toff[pos] + ( win ? 0 : old - $elem.offset()[pos] );

    					// If it's a dom element, reduce the margin
    					if( settings.margin ){
    						attr[key] -= parseInt(targ.css('margin'+Pos)) || 0;
    						attr[key] -= parseInt(targ.css('border'+Pos+'Width')) || 0;
    					}

    					attr[key] += settings.offset[pos] || 0;

    					if( settings.over[pos] )
    						// Scroll to a fraction of its width/height
    						attr[key] += targ[axis=='x'?'width':'height']() * settings.over[pos];
    				}else{
    					var val = targ[pos];
    					// Handle percentage values
    					attr[key] = val.slice && val.slice(-1) == '%' ?
    						parseFloat(val) / 100 * max
    						: val;
    				}

    				// Number or 'number'
    				if( settings.limit && /^\d+$/.test(attr[key]) )
    					// Check the limits
    					attr[key] = attr[key] <= 0 ? 0 : Math.min( attr[key], max );

    				// Queueing axes
    				if( !i && settings.queue ){
    					// Don't waste time animating, if there's no need.
    					if( old != attr[key] )
    						// Intermediate animation
    						animate( settings.onAfterFirst );
    					// Don't animate this axis again in the next iteration.
    					delete attr[key];
    				}
    			});

    			animate( settings.onAfter );

    			function animate( callback ){
    				$elem.animate( attr, duration, settings.easing, callback && function(){
    					callback.call(this, target, settings);
    				});
    			};

    		}).end();
    	};

    	// Max scrolling position, works on quirks mode
    	// It only fails (not too badly) on IE, quirks mode.
    	$scrollTo.max = function( elem, axis ){
    		var Dim = axis == 'x' ? 'Width' : 'Height',
    			scroll = 'scroll'+Dim;

    		if( !$(elem).is('html,body') )
    			return elem[scroll] - $(elem)[Dim.toLowerCase()]();

    		var size = 'client' + Dim,
    			html = elem.ownerDocument.documentElement,
    			body = elem.ownerDocument.body;

    		return Math.max( html[scroll], body[scroll] )
    			 - Math.min( html[size]  , body[size]   );
    	};

    	function both( val ){
    		return typeof val == 'object' ? val : { top:val, left:val };
    	};

    })( jQuery );


})();


