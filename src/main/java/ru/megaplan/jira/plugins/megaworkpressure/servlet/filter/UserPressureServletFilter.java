package ru.megaplan.jira.plugins.megaworkpressure.servlet.filter;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import ru.megaplan.jira.plugins.megaworkpressure.PermissionsUpdateListener;
import ru.megaplan.jira.plugins.permission.manager.ao.MegaPermissionGroupManager;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionGroupMock;
import ru.megaplan.jira.plugins.permission.manager.ao.bean.mock.IPermissionMock;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/2/12
 */
public class UserPressureServletFilter implements Filter, InitializingBean, DisposableBean {

    private static final Logger log = Logger.getLogger(UserPressureServletFilter.class);
    private final Set<String> allowedLogins = new HashSet<String>();
    private final Set<String> allowedProjects = new HashSet<String>();
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebResourceManager webResourceManager;
    private final MegaPermissionGroupManager megaPermissionGroupManager;
    private final PluginEventManager pluginEventManager;

    private PermissionsUpdateListener permissionsUpdateListener;

    private final static String MAINJS_RESOURCEKEY = "ru.megaplan.jira.plugins.mega-work-pressure:megaplan-work-pressure-mainjs";
    private final static String WORK_PRESSURE_CSS = "ru.megaplan.jira.plugins.mega-work-pressure:megaplan-work-pressure-css";
    public final static String WORK_PRESSURE_GROUP = "ru.megaplan.jira.plugins.mega-work-pressure.ACCEPTED";

    private final static String XREQHEADER = "X-Requested-With";
    private final static String XMLHTTPREQUEST = "XMLHttpRequest";
    private final static String QUICKACTIONNAME = "/QuickCreateIssue!default.jspa";
    private final static String QUICKEDITACTION = "/QuickEditIssue!default.jspa";
    private final static String SLOWACTIONNAME = "/CreateIssue.jspa";
    private final static String SLOWEDITACTIONNAME = "/EditIssue!default.jspa";
    private final static String SLOWEDITDETAILS = "/EditIssue.jspa";
    private final static String DETAILSACTIONNAME = "/CreateIssueDetails.jspa";

    public UserPressureServletFilter(JiraAuthenticationContext jiraAuthenticationContext,
                                     WebResourceManager webResourceManager,
                                     MegaPermissionGroupManager megaPermissionGroupManager,
                                     PluginEventManager pluginEventManager
                                     ) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.webResourceManager = webResourceManager;
        this.megaPermissionGroupManager = megaPermissionGroupManager;
        this.pluginEventManager = pluginEventManager;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initConfig();
    }

    public void initConfig() {
        IPermissionGroupMock pg = megaPermissionGroupManager.getPermissionGroup(WORK_PRESSURE_GROUP);
        checkNotNull(pg);
        allowedLogins.clear();
        allowedProjects.clear();
        for (IPermissionMock permissionMock : pg.getPermissions()) {
            if (permissionMock.getUserName() != null) {
                allowedLogins.add(permissionMock.getUserName());
            }
            if (permissionMock.getProjectKey() != null) {
                allowedProjects.add(permissionMock.getProjectKey());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String dateFormat = null;
        boolean include = false;
        try {
            User u = jiraAuthenticationContext.getLoggedInUser();
            if (u != null && allowedLogins.contains(u.getName())) {
                webResourceManager.requireResource(MAINJS_RESOURCEKEY);
                //webResourceManager.requireResource(WORK_PRESSURE_CSS);
                include = true;
            }
        } catch (Exception e) {
            log.error("Mega Pressure servlet filter ist kaputt");
            log.error(e);
        }

        log.warn("preprocessing, include : " + include);

        if (include) {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest req = (HttpServletRequest) request;
                if (req.getHeader(XREQHEADER) != null && req.getHeader(XREQHEADER).equals(XMLHTTPREQUEST)) {
                    log.warn("it is quick action");
                    //ServletResponseWrapper wrapper = new InjectWrapper((HttpServletResponse)response,"\"editHtml\":\"",getInjectProjectPermissions(true));
                    filterChain.doFilter(request, response);
                    response.getWriter().write(getInjectProjectPermissions(false));
                    return;
                } else if (req.getRequestURI().contains(SLOWACTIONNAME) || req.getRequestURI().contains(DETAILSACTIONNAME) ||
                        req.getRequestURI().contains(SLOWEDITACTIONNAME) || req.getRequestURI().contains(SLOWEDITDETAILS)) {
                    filterChain.doFilter(request, response);
                    response.getWriter().write(getInjectProjectPermissions(false));
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        permissionsUpdateListener = new PermissionsUpdateListener(this);
        log.debug("initializing pressure filter with listener : " + permissionsUpdateListener);
        pluginEventManager.register(permissionsUpdateListener);
    }

    @Override
    public void destroy() {
        pluginEventManager.unregister(permissionsUpdateListener);
    }




    class InjectWrapper extends HttpServletResponseWrapper {

        private final Writer writer;
        private final StringWriter injectWriter;
        private final String injectAfter;
        private final String whatInject;

        public InjectWrapper(HttpServletResponse response, String injectAfter, String whatInject) throws IOException {
            super(response);
            writer = response.getWriter();
            injectWriter = new StringWriter();
            this.injectAfter = injectAfter;
            this.whatInject = whatInject;
        }

        @Override
        public PrintWriter getWriter() {
            return new MegaWriter(writer);
        }

        private class MegaWriter extends PrintWriter {

            public MegaWriter(Writer writer) {
                super(writer);
            }


            @Override
            public void write(char[] chars, int offset, int length) {
                webResourceManager.includeResources(injectWriter,UrlMode.AUTO);
                log.debug(whatInject);
                char[] newChars = addAfter(chars,injectAfter , whatInject);
                if (chars.length == newChars.length) log.error("string : " + whatInject + " was not injected but was supposed to be!");
                super.write(newChars, offset, length + whatInject.length());
            }

        }

        private char[] addAfter(char[] chars, String substring, String toAdd) {
            int endIndex = findArray(chars,substring.toCharArray()) + substring.length();
            if (endIndex == -1) {
                log.error("invalid injection");
                return chars;
            }
            char[] toAddArray = toAdd.toCharArray();
            char[] newArray = new char[chars.length + toAdd.length()];

            for (int i = 0; i < endIndex; ++i) {
                newArray[i] = chars[i];
            }
            for (int i = 0; i < toAdd.length(); ++i) {
                newArray[i+endIndex] = toAddArray[i];
            }
            for (int i = 0; i < chars.length - endIndex; ++i) {
                newArray[i+endIndex+toAdd.length()] = chars[i+endIndex];
            }
            return newArray;
        }

        public int findArray(char[] largeArray, char[] subArray) {

            /* If any of the arrays is empty then not found */
            if (largeArray.length == 0 || subArray.length == 0) {
                return -1;
            }

            /* If subarray is larger than large array then not found */
            if (subArray.length > largeArray.length) {
                return -1;
            }

            for (int i = 0; i < largeArray.length; i++) {
                /* Check if the next element of large array is the same as the first element of subarray */
                if (largeArray[i] == subArray[0]) {

                    boolean subArrayFound = true;
                    for (int j = 0; j < subArray.length; j++) {
                        /* If outside of large array or elements not equal then leave the loop */
                        if (largeArray.length <= i+j || subArray[j] != largeArray[i+j]) {
                            subArrayFound = false;
                            break;
                        }
                    }

                    /* Sub array found - return its index */
                    if (subArrayFound) {
                        return i;
                    }

                }
            }

            /* Return default value */
            return -1;
        }
    }

    private String getInjectProjectPermissions(boolean jsonEscape) {
        StringBuilder toInject = new StringBuilder("<script>(function(){AJS.params['workpressure.enabled.projects'] = {");
        for (String pkey : allowedProjects) {
            if (jsonEscape) toInject.append("\\\"");
            toInject.append(pkey);
            if (jsonEscape) toInject.append("\\\"");
            toInject.append(":true,");
        }
        toInject.append("};})();</script>");
        return toInject.toString();
    }





}
