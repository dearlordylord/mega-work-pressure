package ru.megaplan.jira.plugins.megaworkpressure.util.impl;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRange;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRanger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/16/12
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class DateRangerImpl implements DateRanger {

    Logger log = Logger.getLogger(DateRangerImpl.class);

    private final ApplicationProperties applicationProperties;

    private int hoursPerDay;
    private int daysPerWeek;

    private static final int START_WORK_HOUR = 10;

    DateRangerImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        hoursPerDay = Integer.parseInt(applicationProperties.getDefaultBackedString("jira.timetracking.hours.per.day"));
        daysPerWeek = Integer.parseInt(applicationProperties.getDefaultBackedString("jira.timetracking.days.per.week"));

    }



    @Override
    public List<DateRange> generateNonWorkingDates(List<Issue> issues) {
        List<Integer> workdays = getWorkdays();
        List<DateRange> result = new ArrayList<DateRange>();
        DateRange minMaxIssue = getMinMaxIssue(issues);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(minMaxIssue.startDate);
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(minMaxIssue.endDate);
//        Set<Calendar> nonWorkingDays = null;
//        if (ghService != null) {
//            nonWorkingDays = new HashSet<Calendar>();
//            Set<NonWorkingDay> nwd = ghService.getConfiguration(p).getNonWorkingDays();
//            for (NonWorkingDay nd : nwd) {
//                Calendar c = Calendar.getInstance();
//                c.setTime(nd.getDate());
//                DateUtils.toStartOfPeriod(c, Calendar.HOUR_OF_DAY);
//                nonWorkingDays.add(c);
//            }
//        }

        while (calendar.before(endDate)) {
            log.debug(calendar.getTime() + " before " + endDate.getTime());
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            if (!workdays.contains(weekday)) {
                result.add(toDayRange(calendar));
//            } else if (nonWorkingDays!=null &&
//                    nonWorkingDays.contains(calendar)) {
//                result.add(DateUtils.toDateRange(calendar, Calendar.DAY_OF_WEEK));
            } else {
                Calendar night = Calendar.getInstance();
                night.setTime(calendar.getTime());
                toStartOfDay(night);
                night.add(Calendar.HOUR,-(24-START_WORK_HOUR-hoursPerDay));
                Date start = new Date(night.getTime().getTime());
                night.add(Calendar.HOUR, 24-hoursPerDay);
                Date end = new Date(night.getTime().getTime());
                result.add(new DateRange(start,end));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }




        return result;
    }
    private DateRange getMinMaxIssue(List<Issue> issues) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.setTime(start.getTime());
        for (Issue issue : issues) {
            if (issue.getEstimate() != null)    {
                long estimateMsPadded = 0;
                try {
                    estimateMsPadded = DateUtils.getDuration(issue.getEstimate()/60+"m",
                            hoursPerDay,
                            daysPerWeek,
                            DateUtils.Duration.SECOND)
                            *1000;
                } catch (InvalidDurationException e) {
                    e.printStackTrace();
                }
                Date t = end.getTime();
                t.setTime(t.getTime() + estimateMsPadded);
                end.setTime(t);
            }
        }
        //add some reserve
        end.add(Calendar.YEAR,1);
        return new DateRange(start.getTime(),end.getTime());
    }

    private List<Integer> getWorkdays() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 2, j = 0; j < daysPerWeek; ++j, ++i) {
            if (i == 8) i = 1;
            result.add(i);
        }
        return result;
    }

    private DateRange toDayRange(Calendar calendar) {
        Calendar other = (Calendar) calendar.clone();
        toStartOfDay(other);
        Date start = new Date(other.getTime().getTime());
        other.add(Calendar.HOUR_OF_DAY,24);
        Date end = other.getTime();
        return new DateRange(start,end);
    }

    private void toStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        log.debug("start'o'day : " + calendar.getTime());
    }

    private Set generateDefaultNonWorkingDays() {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

}
