package ru.megaplan.jira.plugins.megaworkpressure.listener;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.plugin.util.collect.Function;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import ru.megaplan.jira.plugins.history.search.HistorySearchManager;
import ru.megaplan.jira.plugins.logicus.MegaBusinessLogicManager;
import ru.megaplan.jira.plugins.megaworkpressure.resource.MegaPriorityResource;
import ru.megaplan.jira.plugins.megaworkpressure.resource.OppressionResource;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.JqlUtil;
import ru.megaplan.jira.plugins.megaworkpressure.service.OppressionService;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 22.08.12
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public class OppressionPriorityChangeListener implements InitializingBean, DisposableBean {

    private final static Logger log = Logger.getLogger(OppressionPriorityChangeListener.class);

    private final EventPublisher eventPublisher;
    private final CustomFieldManager customFieldManager;
    private CustomField priorityCustomField;
    private final JqlUtil jqlUtil;
    private final StatusManager statusManager;
    private final HistorySearchManager historySearchManager;
    private final OppressionService oppressionService;

    private final Function<HistorySearchManager.ChangeLogRequest, String> changeLogSearchFunction;

    OppressionPriorityChangeListener(EventPublisher eventPublisher, CustomFieldManager customFieldManager, SearchService searchService, JqlQueryParser jqlQueryParser, MegaBusinessLogicManager megaBusinessLogicManager, StatusManager statusManager, HistorySearchManager historySearchManager, OppressionService oppressionService) throws Exception {
        this.eventPublisher = eventPublisher;
        this.customFieldManager = customFieldManager;
        this.statusManager = statusManager;
        this.historySearchManager = historySearchManager;
        this.oppressionService = oppressionService;
        priorityCustomField = getSortingCustomField();
        jqlUtil = new JqlUtil(searchService, jqlQueryParser, megaBusinessLogicManager);
        changeLogSearchFunction = historySearchManager.getFindInChangeLogFunction();
        if (changeLogSearchFunction == null) {
            String error = "can't find changelogsearch function in historysearchmanager wtf";
            log.error(error);
            throw new Exception(error);
        }
    }

    private CustomField getSortingCustomField() {
        return customFieldManager.getCustomFieldObjectByName(OppressionService.MEGAPRIORITYSORTINGNAME);
    }

    @EventListener
    public void issueEvent(IssueEvent issueEvent) {
        if (priorityCustomField == null) priorityCustomField = getSortingCustomField();
        if (priorityCustomField == null) return;
        Issue issue = issueEvent.getIssue();
        User initiator = issueEvent.getUser();
        //if priority not changed stringNewPriority is null
        if (issueEvent.getEventTypeId().equals(EventType.ISSUE_CREATED_ID)) {
            Object priorityObject = issue.getCustomFieldValue(priorityCustomField);
            if (priorityObject == null) return;
            Double priority = (Double) priorityObject;
            User assignee = issue.getAssignee();
            updatePriorities(initiator, assignee, issue, priority);
        } else {
            String stringNewPriority = getChangedField(issueEvent.getChangeLog(), priorityCustomField.getName(), false);
            String stringNewAssignee = getChangedField(issueEvent.getChangeLog(), "Assignee", false);
            if (stringNewAssignee != null || stringNewPriority != null) {
                Double priority = (Double) issue.getCustomFieldValue(priorityCustomField);
                if (priority == null) return;
                User assignee = issue.getAssignee();
                if (assignee == null) return;
                updatePriorities(initiator, assignee, issue, priority);
            }

        }
    }

    private String getChangedField(GenericValue changeLog, String name, boolean isOld) {
        return changeLogSearchFunction.get(new HistorySearchManager.ChangeLogRequest(changeLog, name, isOld));
    }

    private void updatePriorities(User initiator, User assignee, Issue issue, Double priority) {
        oppressionService.updatePriority(issue.getKey(), assignee.getName(), priority.intValue(), null, initiator);
    }

    private Set<String> getWorkStatuses(User assignee) {
        Set<String> result = new HashSet<String>();

        return result;
    }






    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }





}
