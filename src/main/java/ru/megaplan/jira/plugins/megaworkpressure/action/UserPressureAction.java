package ru.megaplan.jira.plugins.megaworkpressure.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.util.collect.Function;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.logicus.MegaBusinessLogicManager;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.JqlUtil;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRange;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRanger;
import ru.megaplan.jira.plugins.megaworkpressure.util.MegaFormatter;

import java.text.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 5/31/12
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserPressureAction extends JiraWebActionSupport {

    private static final Logger log = Logger.getLogger(UserPressureAction.class);

    private final String WORK_PRESSURE_PROJECTS = "ru.megaplan.jira.plugins.mega-work-pressure.PROJECTS";

    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchService searchService;
    private final ProjectManager projectManager;
    private final PriorityManager priorityManager;
    private final ApplicationProperties applicationProperties;
    private final DateRanger dateRanger;
    private final MegaFormatter megaFormatter;
    private final JqlUtil jqlUtil;


    Issue[] issues;
    Priority priority;
    Priority[] priorities;
    DateRange[] nonWorkingDates;
    String dateFormat;
    int hoursPerDay;
    int daysPerWeek;


    public UserPressureAction(UserManager userManager,
                              JiraAuthenticationContext jiraAuthenticationContext,
                              SearchService searchService,
                              ProjectManager projectManager,
                              PriorityManager priorityManager,
                              ApplicationProperties applicationProperties,
                              DateRanger dateRanger,
                              MegaFormatter megaFormatter,
                              MegaBusinessLogicManager megaBusinessLogicManager,
                              JqlQueryParser jqlQueryParser
    ) {
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchService = searchService;
        this.projectManager = projectManager;
        this.priorityManager = priorityManager;
        this.applicationProperties = applicationProperties;
        this.dateRanger = dateRanger;
        this.megaFormatter = megaFormatter;
        this.isClosed = megaBusinessLogicManager.getIssueIsClosed();
        hoursPerDay = Integer.parseInt(applicationProperties.getDefaultBackedString("jira.timetracking.hours.per.day"));
        daysPerWeek = Integer.parseInt(applicationProperties.getDefaultBackedString("jira.timetracking.days.per.week"));
        jqlUtil = new JqlUtil(searchService, jqlQueryParser, megaBusinessLogicManager);
    }


    @Override
    public String doExecute() {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        User u = userManager.getUser(request.getParameter("user"));
        Project project = projectManager.getProjectObjByKey(request.getParameter("project"));
        String stringPriority = request.getParameter("priority");
        Priority priority = null;
        if (stringPriority == null) {
            priority = priorityManager.getDefaultPriority();
        } else {
            priority = priorityManager.getPriority(request.getParameter("priority"));
        }
        if (u == null || project == null || priority == null) return ERROR;
        this.priority = priority;

        List<Issue> lissues = null;
        try {
            lissues = jqlUtil.getAssignedIssues(jiraAuthenticationContext.getLoggedInUser(), u); //results.getIssues();
        } catch (SearchException e) {
            e.printStackTrace();
            return ERROR;
        }
        sanitizeClosedIssues(lissues);
        issues = lissues.toArray(new Issue[lissues.size()]);
        List<Priority> priorities = priorityManager.getPriorities();
        this.priorities = priorities.toArray(new Priority[priorities.size()]);

        String dateFormat = (String) applicationProperties.asMap().get(APKeys.JIRA_DATE_PICKER_JAVASCRIPT_FORMAT);
        if (dateFormat != null) {
            this.dateFormat = dateFormat;
        }


        List<DateRange> nwd = dateRanger.generateNonWorkingDates(lissues);
        nonWorkingDates = nwd.toArray(new DateRange[nwd.size()]);
        DateFormat df = new SimpleDateFormat();
        for (DateRange range : nonWorkingDates) {
            log.debug(df.format(range.startDate) + " -------- " + df.format(range.endDate));
        }


        return SUCCESS;
    }




    private void sanitizeClosedIssues(List<Issue> lissues) {
        Iterator<Issue> it = lissues.iterator();
        while(it.hasNext()) {
            Issue issue = it.next();
            if (isClosed(issue)) {
                it.remove();
            }

        }
    }

    private final Function<Issue, Boolean> isClosed;

    private Boolean isClosed(Issue issue) {
        return isClosed.get(issue);
    }

    public Issue[] getIssues() {
        return issues;
    }

    public void setIssues(Issue[] issues) {
        this.issues = issues;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Priority[] getPriorities() {
        return priorities;
    }

    public DateRange[] getNonWorkingDates() {
        return nonWorkingDates;
    }

    public MegaFormatter getMegaFormatter() {
        return megaFormatter;
    }

    public int getDaysPerWeek() {
        return daysPerWeek;
    }

    public int getHoursPerDay() {
        return hoursPerDay;
    }
}