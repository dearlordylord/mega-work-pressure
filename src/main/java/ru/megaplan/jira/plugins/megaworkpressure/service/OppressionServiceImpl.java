package ru.megaplan.jira.plugins.megaworkpressure.service;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.NotNull;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.logicus.MegaBusinessLogicManager;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.JqlUtil;
import ru.megaplan.jira.plugins.permission.manager.ao.MegaPermissionGroupManager;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionMock;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 07.09.12
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public class OppressionServiceImpl implements OppressionService {

    private final static Logger log = Logger.getLogger(OppressionServiceImpl.class);

    private final IssueManager issueManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;
    private final IssueService issueService;
    private final StatusManager statusManager;
    private final CustomFieldManager customFieldManager;
    private final JqlUtil jqlUtil;
    private final SearchService searchService;
    private final MegaBusinessLogicManager megaBusinessLogicManager;
    private final JqlQueryParser jqlQueryParser;
    private final MegaPermissionGroupManager megaPermissionGroupManager;
    private final GroupManager groupManager;

    private final static String DEVELOPERS_KEY = "ru.megaplan.jira.plugins.oppression.DEVELOPERS";
    private final static String TESTERS_KEY = "ru.megaplan.jira.plugins.oppression.TESTERS";

    private final Collection<IPermissionMock> developersPermissions;
    private final Collection<IPermissionMock> testersPermissions;

    private final List<String> statusesForDevelopers;
    private final List<String> statusesForTesters;




    public OppressionServiceImpl(IssueManager issueManager, JiraAuthenticationContext jiraAuthenticationContext, UserManager userManager, IssueService issueService, StatusManager statusManager, CustomFieldManager customFieldManager, SearchService searchService, MegaBusinessLogicManager megaBusinessLogicManager, JqlQueryParser jqlQueryParser, MegaPermissionGroupManager megaPermissionGroupManager, GroupManager groupManager) {
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
        this.issueService = issueService;
        this.statusManager = statusManager;
        this.customFieldManager = customFieldManager;
        this.searchService = searchService;
        this.megaBusinessLogicManager = megaBusinessLogicManager;
        this.jqlQueryParser = jqlQueryParser;
        this.megaPermissionGroupManager = megaPermissionGroupManager;
        this.groupManager = groupManager;
        jqlUtil = new JqlUtil(this.searchService, this.jqlQueryParser, this.megaBusinessLogicManager);
        developersPermissions = megaPermissionGroupManager.getPermissionGroup(DEVELOPERS_KEY).getPermissions();
        testersPermissions = megaPermissionGroupManager.getPermissionGroup(TESTERS_KEY).getPermissions();
        statusesForDevelopers = Arrays.asList(new String[] {"3"/*В работе*/ });
        statusesForTesters = Arrays.asList(new String[] {"10010"/*Тестирование*/});
    }


    @Override
    public List<Issue> updatePriority(String key, String assigneeName, Integer priority, Set<String> statuses, User initiator) {
        Issue issue = issueManager.getIssueObject(key);
        User assignee = issue.getAssignee();
        if (initiator == null) initiator = jiraAuthenticationContext.getLoggedInUser();
        User newAssignee = userManager.getUser(assigneeName);
        if (newAssignee != null) {
            IssueService.IssueResult issueResult = assign(issue, newAssignee, initiator);
            if (issueResult.getErrorCollection().hasAnyErrors()) {
                log.error(issueResult.getErrorCollection().getErrorMessages().toString() + issueResult.getErrorCollection().getErrors());
                //add some here
            }
            assignee = newAssignee;
        }
        if (statuses == null || statuses.isEmpty()) {
            statuses = getStatusesIn(assignee);
        }
        Set<String> statusesNot = getStatusesNot(statuses);
        CustomField megaPrioritySortingCf = customFieldManager.getCustomFieldObjectByName(OppressionService.MEGAPRIORITYSORTINGNAME);
        if (megaPrioritySortingCf == null) {
            String error = "can't get customfield with name : " + OppressionService.MEGAPRIORITYSORTINGNAME;
            log.error(error);
        }
        List<Issue> issues = null;
        try {
            issues = jqlUtil.getAssignedIssues(initiator, assignee, statuses, statusesNot, megaPrioritySortingCf);
        } catch (SearchException e) {
            log.error("search error", e);
        }
        insertIntoSuccession(issues, issue, priority, megaPrioritySortingCf);
        return issues;
    }

    //inserts and change
    private static void insertIntoSuccession(List<Issue> forInsert, Issue issue, int megaPriority, CustomField megaPrioritySortingCf) {
        int index =  forInsert.indexOf(issue);
        if (index == -1) {
            forInsert.add(issue);
            index = forInsert.size()-1;
        }
        if (index == megaPriority - 1) return;
        if (megaPriority > forInsert.size() || megaPriority == 0) megaPriority = forInsert.size();
        if (index < megaPriority) {
            Collections.rotate(forInsert.subList(index, megaPriority), -1);
        } else {
            Collections.rotate(forInsert.subList(megaPriority-1, index+1),1);
        }

        for (int i = 0; i < megaPriority; ++i) {
            megaPrioritySortingCf.updateValue(null, forInsert.get(i), new ModifiedValue((double) i, (double) (i + 1)), new DefaultIssueChangeHolder());
        }
        indexIssues(forInsert);
    }

    private static void indexIssues(@NotNull Collection<Issue> issues) {
        boolean oldValue = ImportUtils.isIndexIssues();
        ImportUtils.setIndexIssues(true);
        IssueIndexManager issueIndexManager = ComponentAccessor.getComponentOfType(IssueIndexManager.class);
        try {
            issueIndexManager.reIndexIssueObjects(issues);
        } catch (IndexException e) {
            log.error("Unable to index issues: " + _printIssues(issues), e);
        }
        ImportUtils.setIndexIssues(oldValue);
    }

    private static String _printIssues(Collection<Issue> forInsert) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Issue i : forInsert) {
            sb.append(i.getKey()+ " : " + i.getSummary() + "\n");
        }
        return sb.toString();
    }

    @Override
    public Set<String> getStatusesIn(User user) {
        Set<String> result = new HashSet<String>();
        Collection<Group> userGroups = groupManager.getGroupsForUser(user);
        for (IPermissionMock permissionMock : developersPermissions) {
            if (hasPermission(user, permissionMock)) {
                result.addAll(statusesForDevelopers);
                break;
            }
        }
        for (IPermissionMock permissionMock : testersPermissions) {
            if (hasPermission(user, permissionMock)) {
                result.addAll(statusesForTesters);
                break;
            }
        }
        log.warn("statuses for user : " + user.getName() + " : " + result);
        return result;
    }

    private boolean hasPermission(User user, IPermissionMock permissionMock) {
        boolean result = false;
        if (permissionMock.getGroupName() != null) {
            if (!groupManager.isUserInGroup(user.getName(), permissionMock.getGroupName())) return false;
            else result = true;
        }
        return result;
    }

    @Override
    public Set<String> getStatusesNot(Set<String> statusesIn) {
        Set<String> statusesNot = new HashSet<String>();
        for (Status s : statusManager.getStatuses()) {
            if (!statusesIn.contains(s.getId()))
                statusesNot.add(s.getId());
        }
        return statusesNot;
    }
    private IssueService.IssueResult assign(Issue issue, User newAssignee, User initiator) {
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
        issueInputParameters.setAssigneeId(newAssignee.getName());
        issueInputParameters.setSkipScreenCheck(true);
        IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(initiator, issue.getId(), issueInputParameters);
        return issueService.update(initiator, updateValidationResult);
    }


}
