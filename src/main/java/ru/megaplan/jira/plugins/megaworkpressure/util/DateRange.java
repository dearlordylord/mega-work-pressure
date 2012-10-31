package ru.megaplan.jira.plugins.megaworkpressure.util;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/16/12
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateRange {
    public java.util.Date startDate;
    public java.util.Date endDate;

    public DateRange(java.util.Date startDate, java.util.Date endDate)
    {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "DateRange{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
