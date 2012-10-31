package ru.megaplan.jira.plugins.megaworkpressure.resource;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.api.util.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.DelimeterInserter;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.lang.StringUtils;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.ErrorCollection;
import webwork.util.TextUtil;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parent Resource for all pickers in the gadget plugin.
 *
 * @since v4.0
 */
@Path ("pickers")
@Produces ({ MediaType.APPLICATION_JSON })
public class Pickers
{
    //JRA-19918 searches for 4.0 don't work well
    private static final String DELIMS = "-_/\\,+=&^%$#*@!~`'\":;<> ";

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestManager searchRequestManager;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final IssueManager issueManager;

    public Pickers(PermissionManager permissionManager, JiraAuthenticationContext authenticationContext, SearchRequestManager searchRequestManager, UserManager userManager, GroupManager groupManager, IssueManager issueManager)
    {
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.searchRequestManager = searchRequestManager;
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.issueManager = issueManager;
    }

    @Path ("user")
    @GET
    public Response searchForUsers(@QueryParam ("query") @DefaultValue ("") String query,
                                   @QueryParam ("selectedIssueKey") @DefaultValue ("") String selectedIssueKey)
    {
        return Response.ok(getUsers(query, selectedIssueKey)).build();
    }

    public UserPickerWrapper getUsers(String query, String selectedIssueKey)
    {
        final Collection<User> users = new ArrayList<User>();
        for (String username : groupManager.getUserNamesInGroup("jira-users")) {
            users.add(userManager.getUser(username));
        }
        Issue selectedIssue = issueManager.getIssueObject(selectedIssueKey);
        final User initiator = authenticationContext.getLoggedInUser();
        final UserPickerWrapper result = new UserPickerWrapper();
        for (User user : users) {
            if (!userMatches(user, query)) continue;
            if (!permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, selectedIssue, initiator)) continue;
            if (selectedIssue.getAssignee() != null && selectedIssue.getAssignee().equals(user)) continue;
            result.addUser(new UserPickerBean(String.valueOf(user.getName()), formatUser(user, query), user.getDisplayName(), user.getName()));
        }
        return result;
    }






    private boolean userMatches(User user, String query)
    {
        query = query.toLowerCase().trim();
        final String userName = user.getDisplayName().toLowerCase();
        final String userLogin = user.getName().toLowerCase();

        if (userName.startsWith(query) || userLogin.startsWith(query))
        {
            return true;
        }

        final StringTokenizer tokenizer = new StringTokenizer(user.getDisplayName().toLowerCase(), DELIMS);
        while (tokenizer.hasMoreElements())
        {
            final String namePart = tokenizer.nextToken();
            if (namePart.startsWith(query))
            {
                return true;
            }
        }
        return false;
    }



    private String formatUser(User user, String query)
    {
        final String userName = formatField(user.getDisplayName(), query);
        final String userLogin = formatField(user.getName(), query);

        final StringBuffer sb = new StringBuffer();
        sb.append(userName);
        sb.append("&nbsp;(");
        sb.append(userLogin);
        sb.append(")");
        return sb.toString();
    }



    private String formatField(String field, String query)
    {

        final DelimeterInserter delimeterInserter = new DelimeterInserter("<strong>", "</strong>");
        delimeterInserter.setConsideredWhitespace(DELIMS);

        final StringTokenizer tokenizer = new StringTokenizer(query, DELIMS);
        final List<String> terms = new ArrayList<String>();

        while (tokenizer.hasMoreElements())
        {
            final String projPart = tokenizer.nextToken();
            if (StringUtils.isNotBlank(projPart))
            {
                terms.add(projPart);
            }
        }
        return delimeterInserter.insert(TextUtil.escapeHTML(field), terms.toArray(new String[terms.size()]));
    }

    ///CLOVER:OFF


    @XmlRootElement
    public static class UserPickerWrapper
    {
        @XmlElement
        private List<UserPickerBean> users;

        private UserPickerWrapper()
        {
        }

        private UserPickerWrapper(List<UserPickerBean> users)
        {
            this.users = users;
        }

        public void addUser(UserPickerBean user)
        {
            if (users == null)
            {
                users = new ArrayList<UserPickerBean>();
            }
            users.add(user);
        }
    }

    @XmlRootElement
    public static class UserPickerBean
    {
        @XmlElement
        private String html;
        @XmlElement
        private String name;
        @XmlElement
        private String key;
        @XmlElement
        private String id;

        private UserPickerBean()
        {
        }

        private UserPickerBean(String id, String html, String name, String key)
        {
            this.html = html;
            this.name = name;
            this.key = key;
            this.id = id;
        }
    }


    ///CLOVER:ON
}