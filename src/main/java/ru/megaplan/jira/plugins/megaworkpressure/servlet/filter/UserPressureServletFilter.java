package ru.megaplan.jira.plugins.megaworkpressure.servlet.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.log4j.Logger;
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
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class UserPressureServletFilter implements Filter {

    private static final Logger log = Logger.getLogger(UserPressureServletFilter.class);
    private final Set<String> allowedLogins;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebResourceManager webResourceManager;
    //private final MegaPermissionGroupManager megaPermissionGroupManager;

    private final String MAINJS_RESOURCEKEY = "ru.megaplan.jira.plugins.mega-work-pressure:megaplan-workflow-pressure-mainjs";
    private final String WORK_PRESSURE_GROUP = "ru.megaplan.jira.plugins.mega-work-pressure.ACCEPTED";

    public UserPressureServletFilter(JiraAuthenticationContext jiraAuthenticationContext,
                                     WebResourceManager webResourceManager /*,*MegaPermissionGroupManager megaPermissionGroupManager*/) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.webResourceManager = webResourceManager;
        /*this.megaPermissionGroupManager = megaPermissionGroupManager; */
        allowedLogins = new HashSet<String>();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
       // IPermissionGroupMock pg = megaPermissionGroupManager.getPermissionGroup(WORK_PRESSURE_GROUP);
       // checkNotNull(pg);
      //  for (IPermissionMock permissionMock : pg.getPermissions()) {
     //       if (permissionMock.getUserName() != null) {
       //         log.warn("adding name : " + permissionMock.getUserName());
      //          allowedLogins.add(permissionMock.getUserName());
     //       }
     //   }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        try {
            User u = jiraAuthenticationContext.getLoggedInUser();
            //if (u != null && allowedLogins.contains(u.getName())) {
                webResourceManager.requireResource(MAINJS_RESOURCEKEY);
                log.warn("lolsequence");
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
