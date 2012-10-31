package ru.megaplan.jira.plugins.megaworkpressure.resource.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.util.collect.Function;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.logicus.MegaBusinessLogicManager;
import ru.megaplan.jira.plugins.megaworkpressure.resource.OppressionResource;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 30.07.12
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public class JqlUtil {

    private final static Logger log = Logger.getLogger(JqlUtil.class);

    private final SearchService searchService;
    private final JqlQueryParser jqlQueryParser;
    private final Function<Issue, Boolean> issueIsClosed;

    public JqlUtil(SearchService searchService, JqlQueryParser jqlQueryParser, MegaBusinessLogicManager megaBusinessLogicManager) {
        //To change body of created methods use File | Settings | File Templates.
        this.searchService = searchService;
        this.jqlQueryParser = jqlQueryParser;
        issueIsClosed = megaBusinessLogicManager.getIssueIsClosed();
    }

    private void filterClosedIssues(List<Issue> issuesNotIn) {
        for (Iterator<Issue> it = issuesNotIn.iterator(); it.hasNext();) {
            Issue i = it.next();
            if (issueIsClosed.get(i)) {
                it.remove();
            }
        }
    }

    public List<Issue> getAssignedIssues(User user, User initiator) throws SearchException {
        return getAssignedIssues(user, initiator, null, null, null);
    }


    public List<Issue> getAssignedIssues(User initiator, User user, Set<String> statusesList, Set<String> statusesNot, CustomField megaPrioritySortingCf) throws SearchException {
        JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().assigneeUser(user.getName());
        Collection<String> statusesAll = null;
        if (statusesList!=null && statusesNot!=null) statusesAll = OppressionResource._getShallowCopy(statusesList, statusesNot);
        if (statusesAll != null && !statusesAll.isEmpty()) {
            String[] statusesArray = statusesAll.toArray(new String[statusesAll.size()]);
            builder.and().status().in(statusesArray);
        }
        Query withoutOrder = null;
        if (megaPrioritySortingCf != null) {
            String qsring = searchService.getGeneratedJqlString(builder.endWhere().buildQuery()) + " ORDER BY " + '\"' + megaPrioritySortingCf.getName() + '\"';
            try {
                withoutOrder = jqlQueryParser.parseQuery(qsring);
            } catch (JqlParseException e) {
                log.warn("JqlParseException",e);
            }
        } else {
            withoutOrder = builder.endWhere().buildQuery();
        }
        JqlQueryBuilder builder2 = JqlQueryBuilder.newBuilder(withoutOrder);
        Query q = builder2.orderBy().priority(SortOrder.DESC).issueKey(SortOrder.DESC).buildQuery();
        SearchResults results = searchService.search(initiator, q, PagerFilter.getUnlimitedFilter());
        List<Issue> result = results.getIssues();
        filterClosedIssues(result);


        if (megaPrioritySortingCf != null) {
            List<Issue> goinEnd = new ArrayList<Issue>();
            Iterator<Issue> it = result.iterator();

            while (it.hasNext()) {
                Issue i = it.next();
                Object o = i.getCustomFieldValue(megaPrioritySortingCf);
                if (o == null) {
                    goinEnd.add(i);
                    it.remove();
                }
            }

            List<Issue> goinEndAsPlan = new ArrayList<Issue>();
            Iterator<Issue> itPlan = goinEnd.iterator();
            while (itPlan.hasNext()) {
                Issue i = itPlan.next();
                if (statusesNot.contains(i.getStatusObject().getId())) {
                    goinEndAsPlan.add(i);
                    itPlan.remove();
                }
            }

            StringBuilder sb = new StringBuilder();
            for (Issue i : goinEndAsPlan) {
                sb.append(i.getSummary()).append('\n');
            }
            goinEnd.addAll(goinEndAsPlan);
            result.addAll(goinEnd);
        }

        return result;
    }
}
