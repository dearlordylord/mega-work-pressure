package ru.megaplan.jira.plugins.megaworkpressure.resource.util;

import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRange;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 30.07.12
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class TimeUtils {

    private final static Logger log = Logger.getLogger(TimeUtils.class);

    // result should be same size as estimates argument
    // begin or end dates in any time can't lie in non-working interval
    public static List<DatePair> getStartEndDates(List<DateRange> nonWorkingIntervalsArg, List<Long> estimates) {
        ArrayList<DateRange> nonWorkingIntervals = new ArrayList<DateRange>(); //copy arg cause we modify this list here
        for (DateRange dr : nonWorkingIntervalsArg) {
            nonWorkingIntervals.add(new DateRange(new Date(dr.getStartDate().getTime()), new Date(dr.getEndDate().getTime())));
        }
        List<DatePair> result = new ArrayList<DatePair>();
        Date beginDate = new Date();
        Date endDate = new Date(beginDate.getTime());
        DateRange nextInterval = nonWorkingIntervals.remove(0);
        for (Long estimate : estimates) {
            beginDate.setTime(endDate.getTime());
            Date remainingTime = new Date(estimate);
            addPadding(endDate, remainingTime, nonWorkingIntervals, nextInterval);
            result.add(new DatePair(new Date(beginDate.getTime()),new Date(endDate.getTime())));
        }
        stateTrue("estimates is equals result", estimates.size() == result.size());
        return result;
    }

    private static void addPadding(Date endDate, Date remainingTime, ArrayList<DateRange> nonWorkingIntervals, DateRange nextInterval) {
        if (remainingTime.getTime() <= 0) {
        } else if (nonWorkingIntervals.isEmpty()) {
            log.error("nonWorkingIntervals is empty now!");
        } else if(endDate.getTime() >= nextInterval.getStartDate().getTime() &&
                endDate.getTime() < nextInterval.getEndDate().getTime()) {
            endDate.setTime(nextInterval.getEndDate().getTime());
            DateRange interval = nonWorkingIntervals.remove(0);
            nextInterval.setStartDate(interval.getStartDate());
            nextInterval.setEndDate(interval.getEndDate());
            addPadding(endDate, remainingTime, nonWorkingIntervals, nextInterval);
        } else if (endDate.getTime()+remainingTime.getTime() < nextInterval.getStartDate().getTime()) {
            endDate.setTime(endDate.getTime()+remainingTime.getTime());
            remainingTime.setTime(0);
        } else if (endDate.getTime()+remainingTime.getTime() >= nextInterval.getStartDate().getTime()) {

            long chopTime = nextInterval.getStartDate().getTime() - endDate.getTime();
            if (chopTime > 0) {
                remainingTime.setTime(remainingTime.getTime()-chopTime);
                endDate.setTime(nextInterval.getEndDate().getTime());
            } else {
                log.error("CHOP TIME LESSER THAN 0 : " + chopTime);
            }
            DateRange interval = nonWorkingIntervals.remove(0);
            nextInterval.setStartDate(interval.getStartDate());
            nextInterval.setEndDate(interval.getEndDate());
            addPadding(endDate, remainingTime, nonWorkingIntervals, nextInterval);
        }
    }

    public static class DatePair {
        private Date d1;
        private Date d2;

        public DatePair(Date d1, Date d2) {
            this.d1 = d1;
            this.d2 = d2;
        }

        public Date getD1() {
            return d1;
        }

        public Date getD2() {
            return d2;
        }

        @Override
        public String toString() {
            return "DatePair{" +
                    "d1=" + d1 +
                    ", d2=" + d2 +
                    '}';
        }
    }

}
