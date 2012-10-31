package ru.megaplan.jira.plugins.megaworkpressure.resource;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.NotNull;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import ru.megaplan.jira.plugins.megaworkpressure.service.OppressionService;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

import static ru.megaplan.jira.plugins.megaworkpressure.resource.OppressionResource.noCache;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.08.12
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
@Path("/megaPriority")
public class MegaPriorityResource {

    private final static Logger log = Logger.getLogger(MegaPriorityResource.class);

    private final CustomFieldManager customFieldManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueManager issueManager;
    private final IssueService issueService;

    private final static String ISSUEKEY = "issueKey";
    private final static String MEGAPRIORITY = "megaPriority";

    public final static String MEGAPRIORITYCFNAME = "Mega Priority Linked";


    MegaPriorityResource(CustomFieldManager customFieldManager, JiraAuthenticationContext jiraAuthenticationContext, IssueManager issueManager, IssueService issueService) {

        this.customFieldManager = customFieldManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.issueManager = issueManager;
        this.issueService = issueService;
    }

    @POST
    @Path ("/set")
    @Produces({MediaType.APPLICATION_JSON})
    public Response set(PriorityUpdateBean updateBean) {
        String issueKey = updateBean.issueKey();
        Integer megaPriority = updateBean.megaPriority();
        Collection<Error> errors = new ArrayList<Error>();
        User u = jiraAuthenticationContext.getLoggedInUser();
        //CustomField megaPriorityCf = customFieldManager.getCustomFieldObjectByName(MEGAPRIORITYCFNAME);
        //if (megaPriorityCf == null) errors.add(new Error("can't find custom field with name : " + MEGAPRIORITYCFNAME));
        CustomField megaPrioritySortingCf = customFieldManager.getCustomFieldObjectByName(OppressionService.MEGAPRIORITYSORTINGNAME);
        if (megaPrioritySortingCf == null) errors.add(new Error("can't find custom field with name : " + OppressionService.MEGAPRIORITYSORTINGNAME));
        MutableIssue issue = issueManager.getIssueObject(issueKey);
        if (issue == null) errors.add(new Error("can't find issue with key : " + issueKey));
        if (megaPriority == null) errors.add(new Error("can't find priority in request"));
        if (!errors.isEmpty()) {
            log.warn("eggogs:" + errors);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errors)
                    .cacheControl(OppressionResource.noCache())
                    .build();
        }
        return null;
    }


    @XmlRootElement(name = "error")
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Error {

        @XmlAttribute
        private String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }

    @XmlRootElement(name = "response")
    @XmlAccessorType(XmlAccessType.FIELD)
    public class Ok {
        @XmlAttribute
        private Number newValue;
        public Ok(Number newValue) {
            this.newValue = newValue;
        }

        public Number getNewValue() {
            return newValue;
        }
    }

    @JsonIgnoreProperties()
    public static class PriorityUpdateBean {
        @JsonProperty
        private String issueKey;
        @JsonProperty
        private Integer megaPriority;

        public String issueKey() {
            return issueKey;
        }

        public Integer megaPriority() {
            return megaPriority;
        }
    }

    //Mega Priority



}
