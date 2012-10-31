AJS.$.namespace("AJS.Megaplan.Oppression.TimeFormatter");

AJS.Megaplan.Oppression.TimeFormatter.formatPlanTime = function(issue) {
    var d1 = new Date(issue.start);
    var d2 = new Date(issue.end);
    return toMonthAndDay(new Date(issue.start)) + " - " + toMonthAndDay(new Date(issue.end));
}

function toMonthAndDay(date) {
    var month = date.getMonth()+1;
    if (month < 10) month = '0'+month;
    var day = date.getDate();
    if (day < 10) day = '0'+day;
    return day + '/' + month;
}