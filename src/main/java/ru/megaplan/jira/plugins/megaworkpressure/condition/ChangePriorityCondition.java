package ru.megaplan.jira.plugins.megaworkpressure.condition;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import ru.megaplan.jira.plugins.permission.manager.ao.MegaPermissionGroupManager;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionMock;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 07.09.12
 * Time: 19:55
 * To change this template use File | Settings | File Templates.
 */
public class ChangePriorityCondition extends AbstractIssueCondition {

    private final static String PERMISSION_KEY = "ru.megaplan.jira.plugins.mega-work-pressure.ACCEPTED";

    private final MegaPermissionGroupManager megaPermissionGroupManager;

    private final Collection<IPermissionMock> permissions;

    ChangePriorityCondition(MegaPermissionGroupManager megaPermissionGroupManager) {
        this.megaPermissionGroupManager = megaPermissionGroupManager;
        permissions = megaPermissionGroupManager.getPermissionGroup(PERMISSION_KEY).getPermissions();
    }

    @Override
    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper) {
        boolean result = false;
        for (IPermissionMock permissionMock : permissions) {
            if (permissionMock.getUserName() != null) {
                if (!permissionMock.getUserName().equals(user.getName())) continue;
            }
            if (permissionMock.getProjectKey() != null) {
                if (!permissionMock.getProjectKey().equals(issue.getProjectObject().getKey())) continue;
            }
            result = true;
        }
        return result;
    }
}
