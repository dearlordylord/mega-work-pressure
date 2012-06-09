package ru.megaplan.jira.plugins.megaworkpressure.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchProviderFactory;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.atlassian.velocity.VelocityManager;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 5/31/12
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserPressureAction extends JiraWebActionSupport {



    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final SearchService searchService;
    private final ProjectManager projectManager;
    private final SearchProviderFactory searchProviderFactory;
    private final IssueFactory issueFactory;
    private final VelocityManager velocityManager;
    private final SearchProvider searchProvider;
    private final PriorityManager priorityManager;

    Issue[] issues;
    Priority priority;
    Priority[] priorities;

    public Priority[] getPriorities() {
        return priorities;
    }

    public UserPressureAction(UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext,
    PermissionManager permissionManager, UserProjectHistoryManager userProjectHistoryManager, SearchService searchService,
    ProjectManager projectManager, SearchProviderFactory searchProviderFactory, IssueFactory issueFactory,
    VelocityManager velocityManager, SearchProvider searchProvider, PriorityManager priorityManager
    ) {
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.userProjectHistoryManager = userProjectHistoryManager;
        this.searchService = searchService;
        this.projectManager = projectManager;
        this.searchProviderFactory = searchProviderFactory;
        this.issueFactory = issueFactory;
        this.velocityManager = velocityManager;
        this.searchProvider = searchProvider;
        this.priorityManager = priorityManager;
    }

    @Override
    public String doExecute() {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        User u = userManager.getUser(request.getParameter("user"));
        Project p = projectManager.getProjectObjByKey(request.getParameter("project"));
        Priority priority = priorityManager.getPriority(request.getParameter("priority"));
        if (u == null || p == null || priority == null) return ERROR;
        this.priority = priority;
        //String jqlQuery = "project="+p.getKey()+" and assignee="+u.getName();
        JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
        Query q = jqlQueryBuilder.where().project(p.getId()).and().assigneeUser(u.getName()).endWhere().orderBy().priority(SortOrder.DESC).buildQuery();
        SearchResults results = null;
        try {
            results = searchService.search(loggedInUser, q, PagerFilter.getUnlimitedFilter());
        } catch (SearchException e) {
            e.printStackTrace();
            return ERROR;
        }
        List<Issue> lissues = results.getIssues();
        issues = lissues.toArray(new Issue[lissues.size()]);
        List<Priority> priorities = priorityManager.getPriorities();
        this.priorities = priorities.toArray(new Priority[priorities.size()]);
//        SearchService.ParseResult parseResult = searchService.
//        parseQuery(loggedInUser, jqlQuery);
//        if (parseResult.isValid()) {
//            Query query = parseResult.getQuery();
//            try {
//                SearchResults results = searchService.search(loggedInUser, query,
//                    PagerFilter.getUnlimitedFilter());
//                List<Issue> lissues = results.getIssues();
//                issues = lissues.toArray(new Issue[lissues.size()]);
//            } catch (SearchException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//
//        }

        return SUCCESS;
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

//    public String getBaseUrl() {
//        return baseUrl;
//    }

}
