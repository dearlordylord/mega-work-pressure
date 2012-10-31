package ru.megaplan.jira.plugins.megaworkpressure.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/16/12
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DateRanger {
    List<DateRange> generateNonWorkingDates(List<Issue> issues);
}
