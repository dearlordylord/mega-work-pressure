package ru.megaplan.jira.plugins.megaworkpressure.action;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import com.atlassian.jira.web.action.issue.AssignIssue;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import ru.megaplan.jira.plugins.megaworkpressure.resource.OppressionResource;
import ru.megaplan.jira.plugins.megaworkpressure.service.OppressionService;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 23.08.12
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */
public class UserPressureAssignAction extends AssignIssue {

    private final WebResourceUrlProvider webResourceUrlProvider;

    private final static String INIT_PRESSURE_TABLE_MODULE = "ru.megaplan.jira.plugins.mega-work-pressure:init-pressure-priority-table";
    private final static String INIT_PRESSURE_TABLE_SCRIPT = "initPressurePriorityTableOnAssign.js";
    private final static String INIT_USERPICKER_MODULE = "ru.megaplan.jira.plugins.mega-work-pressure:init-user-picker";
    private final static String INIT_USERPICKER_SCRIPT = "initUserPicker.js";

    private static final String X_ATLASSIAN_DIALOG_CONTROL = "X-Atlassian-Dialog-Control";

    private String tableScriptUrl;
    private String userPickerUrl;
    private int priority;

    private final OppressionService oppressionService;
    private final IssueManager issueManager;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;

    public UserPressureAssignAction(SubTaskManager subTaskManager, FieldScreenRendererFactory fieldScreenRendererFactory, FieldLayoutManager fieldLayoutManager, FieldManager fieldManager, CommentService commentService, IssueManager issueManager, UserUtil userUtil, WebResourceUrlProvider webResourceUrlProvider, OppressionService oppressionService, IssueManager issueManager1, FieldManager fieldManager1, FieldLayoutManager fieldLayoutManager1) {
        super(subTaskManager, fieldScreenRendererFactory, fieldLayoutManager, fieldManager, commentService, issueManager, userUtil);
        this.fieldLayoutManager = fieldLayoutManager;
        this.fieldManager = fieldManager;
        this.issueManager = issueManager;
        this.webResourceUrlProvider = webResourceUrlProvider;
        this.oppressionService = oppressionService;
    }

    @Override
    public String doDefault() throws Exception {
        String initTableScriptUrl = webResourceUrlProvider.getStaticPluginResourceUrl(
                INIT_PRESSURE_TABLE_MODULE,
                INIT_PRESSURE_TABLE_SCRIPT,
                UrlMode.RELATIVE);
        tableScriptUrl = initTableScriptUrl;
        userPickerUrl = webResourceUrlProvider.getStaticPluginResourceUrl(INIT_USERPICKER_MODULE, INIT_USERPICKER_SCRIPT, UrlMode.RELATIVE);
        return super.doDefault();
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {

        Issue issue = getIssueObject();

        String initTableScriptUrl = webResourceUrlProvider.getStaticPluginResourceUrl(
                INIT_PRESSURE_TABLE_MODULE,
                INIT_PRESSURE_TABLE_SCRIPT,
                UrlMode.RELATIVE);
        tableScriptUrl = initTableScriptUrl;
        userPickerUrl = webResourceUrlProvider.getStaticPluginResourceUrl(INIT_USERPICKER_MODULE, INIT_USERPICKER_SCRIPT, UrlMode.RELATIVE);
        String result = super.doExecute();
        oppressionService.updatePriority(issue.getKey(), null, priority, null, getLoggedInUser());
        return result;
    }

    public String getTableScriptUrl() {
        return tableScriptUrl;
    }

    public void setTableScriptUrl(String tableScriptUrl) {
        this.tableScriptUrl = tableScriptUrl;
    }

    public String getUserPickerUrl() {
        return userPickerUrl;
    }

    public void setUserPickerUrl(String userPickerUrl) {
        this.userPickerUrl = userPickerUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
