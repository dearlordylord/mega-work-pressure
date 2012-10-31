package ru.megaplan.jira.plugins.megaworkpressure.util.impl;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.I18nHelper;
import ru.megaplan.jira.plugins.megaworkpressure.util.MegaFormatter;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/16/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class MegaFormatterImpl implements MegaFormatter {

    private long secondsPerDay;
    private long secondsPerWeek;

    private NumberFormat decimalFormat;

    private MegaFormatterImpl(ApplicationProperties applicationProperties) {
        secondsPerDay = new Float(Float.valueOf(applicationProperties.getDefaultBackedString("jira.timetracking.hours.per.day")) * 3600).longValue();
        secondsPerWeek = new Float(Float.valueOf(applicationProperties.getDefaultBackedString("jira.timetracking.days.per.week")) * secondsPerDay).longValue();
        decimalFormat = NumberFormat.getInstance();
    }


    public String getPrettyDuration(long value) {
        return DateUtils.getDurationStringSeconds(value, secondsPerDay, secondsPerWeek);
    }


    @Override
    public String getPrettyHours(long value) {
        return getHours(value) + "h";
    }

    public String getHours(long value)
    {
        return decimalFormat.format(((float)value) / 60 / 60);
    }




}
