package ru.megaplan.jira.plugins.megaworkpressure.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 5/31/12
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserPressureAction extends JiraWebActionSupport {

    Issue[] issues;

    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final SearchService searchService;

    public UserPressureAction(UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext,
    PermissionManager permissionManager, UserProjectHistoryManager userProjectHistoryManager, SearchService searchService) {
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.searchService = searchService;
    }

    @Override
    public String doExecute() {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        User u = userManager.getUser(request.getParameter("user"));
        Project currentProject = userProjectHistoryManager.getCurrentProject(Permissions.CREATE_ISSUE, loggedInUser);
        String jqlQuery = "project="+currentProject.getKey()+" and assignee="+u.getName();
        SearchService.ParseResult parseResult = searchService.
        parseQuery(loggedInUser, jqlQuery);
        if (parseResult.isValid()) {
            Query query = parseResult.getQuery();
            try {
                SearchResults results = searchService.search(loggedInUser, query,
                    PagerFilter.getUnlimitedFilter());
                List<Issue> lissues = results.getIssues();
                issues = lissues.toArray(new Issue[lissues.size()]);
            } catch (SearchException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        return SUCCESS;
    }

    public Issue[] getIssues() {
        return issues;
    }

    public void setIssues(Issue[] issues) {
        this.issues = issues;
    }
}
