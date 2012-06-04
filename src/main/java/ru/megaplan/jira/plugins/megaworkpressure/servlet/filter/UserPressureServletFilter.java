package ru.megaplan.jira.plugins.megaworkpressure.servlet.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.permission.manager.ao.MegaPermissionGroupManager;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionGroupMock;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionMock;
//import ru.megaplan.jira.plugins.permission.manager.ao.MegaPermissionGroupManager;

//import ru.megaplan.jira.plugins.permission.manager.ao.bean.PermissionGroup;
//import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionGroupMock;
//import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionMock;

import javax.servlet.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/2/12
 */
public class UserPressureServletFilter implements Filter {

    private static final Logger log = Logger.getLogger(UserPressureServletFilter.class);
    private final Set<String> allowedLogins;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebResourceManager webResourceManager;
    private final MegaPermissionGroupManager megaPermissionGroupManager;
    private final UserProjectHistoryManager userProjectHistoryManager;

    private final String MAINJS_RESOURCEKEY = "ru.megaplan.jira.plugins.mega-work-pressure:megaplan-workflow-pressure-mainjs";
    private final String WORK_PRESSURE_GROUP = "ru.megaplan.jira.plugins.mega-work-pressure.ACCEPTED";

    public UserPressureServletFilter(JiraAuthenticationContext jiraAuthenticationContext,
                                     WebResourceManager webResourceManager,
                                     UserProjectHistoryManager userProjectHistoryManager,MegaPermissionGroupManager megaPermissionGroupManager) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.webResourceManager = webResourceManager;
        this.megaPermissionGroupManager = megaPermissionGroupManager;
        this.userProjectHistoryManager = userProjectHistoryManager;
        allowedLogins = new HashSet<String>();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
       IPermissionGroupMock pg = megaPermissionGroupManager.getPermissionGroup(WORK_PRESSURE_GROUP);
       checkNotNull(pg);
        for (IPermissionMock permissionMock : pg.getPermissions()) {
            if (permissionMock.getUserName() != null) {
                log.warn("adding name : " + permissionMock.getUserName());
                allowedLogins.add(permissionMock.getUserName());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            User u = jiraAuthenticationContext.getLoggedInUser();
            Project p = userProjectHistoryManager.getCurrentProject(Permissions.CREATE_ISSUE, u);
            if (p.getKey().equals("MP")) {
                log.warn("MP FOUND");
            }
            //if (u != null && allowedLogins.contains(u.getName())) {
                webResourceManager.requireResource("com.atlassian.auiplugin:ajs");
                webResourceManager.requireResource("jira.webresources:jira-global");
                webResourceManager.requireResource(MAINJS_RESOURCEKEY);
           // }
        } catch (Exception e) {
            log.error("Mega Pressure servlet filter ist kaputt");
            log.error(e);
        }

        filterChain.doFilter(request, response);
    }

    private String print(Enumeration<String> attributeNames) {
        StringBuilder sb = new StringBuilder();
        while (attributeNames.hasMoreElements()) {
            sb.append(" [ " + attributeNames.nextElement() + " ] ");
        }
        return sb.toString();
    }

    @Override
    public void destroy() {
    }
}
