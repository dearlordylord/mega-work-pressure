package ru.megaplan.jira.plugins.megaworkpressure.resource;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 10.07.12
 * Time: 19:08
 * To change this template use File | Settings | File Templates.
 */
@Path("/assignee")
public class AssigneeResource  {

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final static String JIRAUSERS = "jira-users";

    public AssigneeResource(UserManager userManager, GroupManager groupManager) {
        this.userManager = userManager;
        this.groupManager = groupManager;
    }

    @GET
    @Path ("/allActive")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllStatuses(@Context HttpServletRequest request) {
        Collection<User> users = groupManager.getUsersInGroup(JIRAUSERS);
        List<Assignee> result = new ArrayList<Assignee>();
        for (User user : users) {
            if (user.isActive()) {
                StringBuilder stringBuilder = new StringBuilder(user.getDisplayName());
                stringBuilder.append(' ').append('[').append(user.getName()).append(']');
                result.add(new Assignee(user.getName(),stringBuilder.toString()));
            }
        }
        Collections.sort(result);
        return Response.ok(result).cacheControl(OppressionResource.noCache()).build();
    }

    @XmlRootElement(name = "assignee")
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Assignee implements Comparable<Assignee> {
        @XmlAttribute
        private String value;
        @XmlAttribute
        private String label;

        public Assignee(String login, String fullname) {
            this.value = login;
            this.label = fullname;
        }

        public String getKey() {
            return value;
        }

        public void setKey(String key) {
            this.value = key;
        }

        public String getName() {
            return label;
        }

        public void setName(String name) {
            this.label = name;
        }

        @Override
        public int compareTo(Assignee assignee) {
            return label.compareTo(assignee.label);
        }
    }

}
