package ru.megaplan.jira.plugins.megaworkpressure.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 07.09.12
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
public interface OppressionService {
    public final static String MEGAPRIORITYSORTINGNAME = "Mega Priority Sorting";
    List<Issue> updatePriority(String issue, String assignee, Integer priority, @Nullable Set<String> statuses, User initiator);
    Set<String> getStatusesIn(User user);
    Set<String> getStatusesNot(Set<String> statusesIn);
}
