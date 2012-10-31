package ru.megaplan.jira.plugins.megaworkpressure.resource;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.PriorityManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.api.messages.TextMessage;
import com.atlassian.jira.rest.api.util.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ofbiz.core.entity.GenericValue;
import ru.megaplan.jira.plugins.logicus.MegaBusinessLogicManager;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.ErrorCollection;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.JqlUtil;
import ru.megaplan.jira.plugins.megaworkpressure.resource.util.TimeUtils;
import ru.megaplan.jira.plugins.megaworkpressure.service.OppressionService;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRange;
import ru.megaplan.jira.plugins.megaworkpressure.util.DateRanger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.*;

import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 30.07.12
 * Time: 13:00
 * To change this template use File | Settings | File Templates.
 */
@Path("/oppression")
public class OppressionResource {

    private final static Logger log = Logger.getLogger(OppressionResource.class);

    private static final String ASSIGNEES = "assignees";
    private static final String STATUSES = "statuses";
    private static final String CANDIDATE_ISSUE = "candidateIssue";

    private static final String CANDIDATE_ISSUE_POSITION = "candidateIssuePosition";
    private static final String CANDIDATE_ISSUE_TIME = "candidateIssueTime";
    private static final String CANDIDATE_ISSUE_SUMMARY = "candidateIssueSummary";
    private static final String CANDIDATE_ISSUE_PROJECT = "candidateIssueProject";
    private static final String CANDIDATE_ISSUE_TYPE = "candidateIssueType";
    private static final String CANDIDATE_ISSUE_KEY = "candidateIssueKey";
    private static final String CANDIDATE_ISSUE_ID = "candidateIssueId";

    private final DateRanger dateRanger;
    private final JqlUtil jqlUtil;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchService searchService;
    private final UserManager userManager;
    private final StatusManager statusManager;
    private final MegaBusinessLogicManager megaBusinessLogicManager;

    private final JiraBaseUrls jiraBaseUrls;
    private final CustomFieldManager customFieldManager;
    private final CustomFieldValuePersister customFieldValuePersister;
    private final JqlQueryParser jqlQueryParser;
    private final IssueManager issueManager;
    private final IssueService issueService;
    private final IssueTypeManager issueTypeManager;
    private final PriorityManager priorityManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final OppressionService oppressionService;

    public OppressionResource(DateRanger dateRanger, JiraAuthenticationContext jiraAuthenticationContext, SearchService searchService, UserManager userManager, StatusManager statusManager, MegaBusinessLogicManager megaBusinessLogicManager, JiraBaseUrls jiraBaseUrls, CustomFieldManager customFieldManager, CustomFieldValuePersister customFieldValuePersister, JqlQueryParser jqlQueryParser, IssueManager issueManager, IssueService issueService, IssueTypeManager issueTypeManager, PriorityManager priorityManager, OppressionService oppressionService) {
        this.dateRanger = dateRanger;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchService = searchService;
        this.userManager = userManager;
        this.statusManager = statusManager;
        this.megaBusinessLogicManager = megaBusinessLogicManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.customFieldManager = customFieldManager;
        this.customFieldValuePersister = customFieldValuePersister;
        this.jqlQueryParser = jqlQueryParser;
        this.issueManager = issueManager;
        this.issueService = issueService;
        this.issueTypeManager = issueTypeManager;
        this.priorityManager = priorityManager;
        this.oppressionService = oppressionService;
        this.jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
        jqlUtil = new JqlUtil(searchService, jqlQueryParser, megaBusinessLogicManager);
    }


    @GET
    @Path ("/generate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOppression(@Context HttpServletRequest request,
                                  @QueryParam(ASSIGNEES) String assigneesString,
                                  @QueryParam(STATUSES) String statusesInString,
                                  @QueryParam(CANDIDATE_ISSUE) final String candidateIssue,
                                  @QueryParam(CANDIDATE_ISSUE_POSITION) Integer candidateIssuePosition,
                                  @QueryParam(CANDIDATE_ISSUE_TIME) String candidateIssueTimeString,
                                  @QueryParam(CANDIDATE_ISSUE_SUMMARY) String candidateIssueSummary,
                                  @QueryParam(CANDIDATE_ISSUE_PROJECT) String candidateIssueProject,
                                  @QueryParam(CANDIDATE_ISSUE_TYPE) String candidateIssueType,
                                  @QueryParam(CANDIDATE_ISSUE_KEY) String candidateIssueKey, // fake key
                                  @QueryParam(CANDIDATE_ISSUE_ID) long candidateIssueId
                                  ) throws SearchException {
        MutableIssue candidate=  null;
        CustomField sortCustomField = customFieldManager.getCustomFieldObjectByName(OppressionService.MEGAPRIORITYSORTINGNAME);
        if (sortCustomField==null) {
            String error = "Sorting custom field with name : " + OppressionService.MEGAPRIORITYSORTINGNAME + " is not found";
            log.error(error);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
        if (!StringUtils.isEmpty(candidateIssue)) candidate = issueManager.getIssueObject(candidateIssue);
        if (candidate != null) {
            if (candidateIssuePosition == null) {
                if ((assigneesString == null || assigneesString.isEmpty() || !isAssigneeChanged(candidate, assigneesString))) {
                    Object priorityObjectValue = candidate.getCustomFieldValue(sortCustomField);
                    if (priorityObjectValue != null) {
                        candidateIssuePosition = ((Double)priorityObjectValue).intValue();
                    }
                }
            }
            if (StringUtils.isEmpty(assigneesString)) {
                User assignee = candidate.getAssignee();
                if (assignee != null) assigneesString = assignee.getName();
            }
        }
        if (candidateIssuePosition == null) {
            candidateIssuePosition = 1;
        }
        Collection<ValidationError> assigneeErrors = new ArrayList<ValidationError>();
        Set<String> assignees = validateAssignees(assigneesString, assigneeErrors);
        Set<String> statuses = getStatusesFromString(statusesInString);
        Set<String> statusesNot = oppressionService.getStatusesNot(statuses);
        List<OppressionRow> oppressionRows = new ArrayList<OppressionRow>();



        for (String assignee : assignees) {
            User user = userManager.getUser(assignee);
            Set<String> userStatuses = statuses;
            Set<String> userStatusesNot = statusesNot;
            if (userStatuses == null || userStatuses.isEmpty()) {
                userStatuses = oppressionService.getStatusesIn(user);
                userStatusesNot = oppressionService.getStatusesNot(userStatuses);
            }
            List<Issue> issues = jqlUtil.getAssignedIssues(jiraAuthenticationContext.getLoggedInUser(), user, statuses, statusesNot, sortCustomField);
            int candidateIssueIndex = candidateIssuePosition-1;
            boolean isCandidate = false;
            if (candidateIssueIndex < 0 || candidateIssueIndex > issues.size()) candidateIssueIndex = 0;
            if (!StringUtils.isEmpty(candidateIssue) || candidateIssueId != 0) {
                if (candidateIssueId != 0) {
                    candidate = issueManager.getIssueObject(candidateIssueId);
                } else {
                    candidate = issueManager.getIssueObject(candidateIssue);
                }
                if (candidate != null) {
                    issues.remove(candidate); // only if contains
                    long duration = safeParseDuration(candidateIssueTimeString);
                    candidate.setEstimate(duration);
                    if (!StringUtils.isEmpty(candidateIssueSummary)) candidate.setSummary(candidateIssueSummary);
                    issues.add(candidateIssueIndex, candidate);
                    isCandidate = true;
                }
            } else if (!StringUtils.isEmpty(candidateIssueSummary)) {
                FakeIssue fakeIssue = new FakeIssue(statusManager, priorityManager);
                long duration = safeParseDuration(candidateIssueTimeString);
                fakeIssue.setRemainingEstimate(duration);
                fakeIssue.setSummary(candidateIssueSummary);
                fakeIssue.setIssueKey(candidateIssueKey);
                IssueType issueType = null;
                if (candidateIssueType != null ) issueType = issueTypeManager.getIssueType(candidateIssueType);
                if (issueType != null) {
                    fakeIssue.setIssueType(issueType);
                } else {
                    fakeIssue.setIssueType(issueTypeManager.getIssueTypes().iterator().next());
                }
                issues.add(candidateIssueIndex, fakeIssue);
                isCandidate = true;
            }
            List<DateRange> nonWorking = dateRanger.generateNonWorkingDates(issues);
            List<TimeUtils.DatePair> intervals = TimeUtils.getStartEndDates(nonWorking, getEstimates(issues));
            OppressionRow rw = createOppressionRow(user, issues, intervals, userStatuses, userStatusesNot);
            if (isCandidate) rw.getNow().getIssues().get(candidateIssueIndex).setCandidate(true);
            oppressionRows.add(rw);

        }

        return Response.ok(oppressionRows).build();
    }

    private boolean isAssigneeChanged(MutableIssue candidate, String assigneesString) {
        if (candidate.getAssignee() == null && (assigneesString != null && !assigneesString.isEmpty())) return true;
        if (candidate.getAssignee() == null && (assigneesString == null || assigneesString.isEmpty())) return false;
        return (!candidate.getAssignee().getName().equals(assigneesString));
    }

    private long safeParseDuration(String candidateIssueTimeString) {
        long duration;
        try {
            duration = jiraDurationUtils.parseDuration(candidateIssueTimeString, jiraAuthenticationContext.getLocale());
        } catch (InvalidDurationException e) {
            duration = 0;
        }
        return duration;
    }

    @POST
    @Path("/megaPriority")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateMegaPriorityAndReturnSortedTable(@Context HttpServletRequest request, MegaPriorityUpdateBean megaPriorityUpdateBean) throws SearchException {
        Issue issue = issueManager.getIssueObject(megaPriorityUpdateBean.issue());
        User newAssignee = userManager.getUser(megaPriorityUpdateBean.newAssignee());
        User oldAssignee = issue.getAssignee();
        boolean assigneeChanged = false;
        if (newAssignee != null) {
            if (!newAssignee.equals(oldAssignee)) {
                assigneeChanged = true;
            }
        }
        User assignee;
        if (assigneeChanged) assignee = newAssignee;
        else assignee = oldAssignee;
        Set<String> statuses = getStatusesFromString(megaPriorityUpdateBean.statuses());
        List<Issue> issues = oppressionService.updatePriority(megaPriorityUpdateBean.issue(), assignee.getName(), megaPriorityUpdateBean.megaPriority(), statuses, jiraAuthenticationContext.getLoggedInUser());
        if (statuses == null || statuses.isEmpty()) statuses = oppressionService.getStatusesIn(assignee);
        Set<String> statusesNot = oppressionService.getStatusesNot(statuses);

        //OppressionRow oppressionBefore = createOppressionRow(assignee, assignee.getName(), megaPriorityUpdateBean.statuses());

       // String[] values = MegaPriorityResource.getNewPriorityCfValues(issue, megaPriorityCf, );
//        IssueInputParameters issueInputParameters =
//                MegaPriorityResource.getNewIssueInputParameters(megaPriorityCf,
//                        megaPrioritySortingCf,
//                        String.valueOf(megaPriorityUpdateBean.megaPriority()),
//                        values);
//        IssueService.UpdateValidationResult updateValidationResult = issueService.validateUpdate(initiator, issue.getId(), issueInputParameters);
//        if (updateValidationResult.getErrorCollection().hasAnyErrors()) {
//            log.warn("eggogs:" + updateValidationResult.getErrorCollection().getErrorMessages());
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(updateValidationResult.getErrorCollection().getErrors())
//                    .cacheControl(OppressionResource.noCache()).build();
//        }
//        IssueService.IssueResult updateResult = issueService.update(initiator, updateValidationResult);
        List<DateRange> nonWorking = dateRanger.generateNonWorkingDates(issues);
        List<TimeUtils.DatePair> intervals = TimeUtils.getStartEndDates(nonWorking, getEstimates(issues));
        OppressionRow rw = createOppressionRow(assignee, issues, intervals, statuses, statusesNot);
        List<OppressionRow> oppressionRows = new ArrayList<OppressionRow>();
        oppressionRows.add(rw);
        CustomField megaPrioritySortingCf = customFieldManager.getCustomFieldObject(OppressionService.MEGAPRIORITYSORTINGNAME);
        if (oldAssignee != null) {
            List<Issue> oldAssigneeIssues = jqlUtil.getAssignedIssues(jiraAuthenticationContext.getLoggedInUser(), oldAssignee, statuses, statusesNot, megaPrioritySortingCf);
            List<TimeUtils.DatePair> oldAssigneeIntervals = TimeUtils.getStartEndDates(nonWorking, getEstimates(oldAssigneeIssues));
            OppressionRow oldAssigneeOppressionRow = createOppressionRow(oldAssignee, oldAssigneeIssues, oldAssigneeIntervals, statuses, statusesNot);
            oppressionRows.add(oldAssigneeOppressionRow);
        }
        return Response.ok(oppressionRows).build();
    }








    private boolean _equalsTo(Issue previous, String previous1) {
        if (previous == null && previous1 == null) return true;
        if (previous == null || previous1 == null) return false;
        return previous.getKey().equals(previous1);
    }

    private Issue getPrevious(int i, LinkedList<Issue> forInsert) {
        if (i > -1) {
            return forInsert.get(i);
        }
        return null;
    }

    public static <T> Collection<T> _getShallowCopy(Collection<T>... lists) {
        Collection<T> result = new ArrayList<T>();
        for (Collection<T> list : lists) {
            result.addAll(list);
        }
        return result;
    }





    private List<Long> getEstimates(List<Issue> issues) {
        List<Long> estimates = new ArrayList<Long>();
        for (Issue i : issues) {
            estimates.add(i.getEstimate()!=null?i.getEstimate()*1000:i.getOriginalEstimate()!=null?i.getOriginalEstimate()*1000:0L);
        }
        stateTrue("issues number equals estimates number", estimates.size()==issues.size());
        return estimates;
    }

    private OppressionRow createOppressionRow(User user, List<Issue> issues, List<TimeUtils.DatePair> intervals, Collection<String> statusesIn, Collection<String> statusesNotIn) {
        OppressionRow result = new OppressionRow();
        NowCell nowCell = new NowCell();
        PlanCell planCell = new PlanCell();
        List<OppressionXMLIssue> nowIssues11 = getXMLIssueList(user, issues, intervals, statusesIn, statusesNotIn);
        //List<OppressionXMLIssue> planIssues11 = getXMLIssueList(user, issues, intervals, statusesNotIn);
        nowCell.setIssues(nowIssues11);
        //planCell.setIssues(planIssues11);
        result.setUsername(user.getDisplayName());
        result.setUserLogin(user.getName());
        result.setNow(nowCell);
        return result;
    }

    private List<OppressionXMLIssue> getXMLIssueList(User user, List<Issue> issues, List<TimeUtils.DatePair> intervals, Collection<String> statusesIn, Collection<String> statusesNotIn) {
        List<OppressionXMLIssue> result = new ArrayList<OppressionXMLIssue>();
        for (int i = 0; i < issues.size(); i++) {
            Issue issue = issues.get(i);

            OppressionXMLIssue xmlIssue = new OppressionXMLIssue();
            if (statusesNotIn.contains(issue.getStatusObject().getId())) xmlIssue.setPlan(true);
            xmlIssue.start = intervals.get(i).getD1();
            xmlIssue.end = intervals.get(i).getD2();
            Priority issuePriority = issue.getPriorityObject();
            if (issuePriority != null) {
                xmlIssue.issuePrioritySequence = issuePriority.getSequence();
                xmlIssue.issuePriority = issuePriority.getIconUrlHtml();
            }
            IssueType issueType = issue.getIssueTypeObject();
            xmlIssue.iconHtml = issueType.getIconUrlHtml();
            xmlIssue.typeName = issueType.getName();
            Long issueEstimate = issue.getEstimate();
            xmlIssue.estimate = issueEstimate!=null?issueEstimate:issue.getOriginalEstimate()!=null?issue.getOriginalEstimate():0L;
            xmlIssue.summary = issue.getSummary();
            xmlIssue.key = issue.getKey();
            CustomField megaPriority = customFieldManager.getCustomFieldObjectByName(OppressionService.MEGAPRIORITYSORTINGNAME);
            if (megaPriority != null) {
                Double cfValue = (Double) megaPriority.getValue(issue);
                xmlIssue.megaPriority = (cfValue==null?null:cfValue.intValue());
            } else {
                log.error("custom field : " + OppressionService.MEGAPRIORITYSORTINGNAME + " not found");
            }
            result.add(xmlIssue);
        }
        return result;
    }

    @GET
    @Path ("/validate")
    public Response validate(@QueryParam("ololo") @DefaultValue("30") final String daysBefore) {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        return createValidationResponse(errors);
    }

    protected Response createErrorResponse(final Collection<ValidationError> errors)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        for (ValidationError error : errors) {
            errorCollection.addError(error.getField(), error.getError());
        }
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        return Response.status(400).entity(ErrorCollection.Builder.newBuilder(errors).build()).cacheControl(cacheControl).build();  //entity(ErrorCollection.Builder.newBuilder(errors).build()).cacheControl()
    }

    protected Response createIndexingUnavailableResponse(String message) {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        return Response.status(503).entity(ErrorCollection.Builder.newBuilder().addErrorMessage(message).build()).cacheControl(cacheControl).build();  // CacheControl(NO_CACHE)
    }

    protected Response createValidationResponse(Collection<ValidationError> errors)
    {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        if (errors.isEmpty())
        {
            return Response.ok(new TextMessage("No input validation errors found.")).cacheControl(cacheControl).build();
        }
        else
        {
            return createErrorResponse(errors);
        }
    }

    public static Set<String> validateAssignees(String assignees, Collection<ValidationError> errors) {
        Set<String> result = new HashSet<String>();
        if (assignees == null || assignees.isEmpty()) return  result;
        for (String assignee : assignees.split("\\|")) {
            if (assignee.equals("NULL")) return new HashSet<String>();
            User user = ComponentAccessor.getUserManager().getUser(assignee);
            if (user == null) {
                errors.add(new ValidationError(ASSIGNEES, "can't find user : " + assignee));
                continue;
            }
            result.add(assignee);
        }
        return result;
    }

    public static Set<String> getStatusesFromString(String statusesInString) {
        Set<String> result = new HashSet<String>();
        if (statusesInString == null || statusesInString.isEmpty()) {
            return result;
        }
        for (String s : statusesInString.split("\\|")) {
            if (s.equals("0")) return new HashSet<String>();
            result.add(String.valueOf(Long.parseLong(s)));
        }
        return result;
    }

    @XmlRootElement(name = "oppression")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Oppression {

        @XmlAttribute
        private List<OppressionRow> oppressionRows;

        public List<OppressionRow> getOppressionRows() {
            return oppressionRows;
        }

        public void setOppressionRows(List<OppressionRow> oppressionRows) {
            this.oppressionRows = oppressionRows;
        }
    }

    @XmlRootElement(name = "oppressionRow")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class OppressionRow implements Comparable<OppressionRow>{

        @XmlAttribute
        private String username;
        @XmlAttribute
        private String userLogin;
        @XmlAttribute
        private NowCell now;
        @XmlAttribute
        private PlanCell plan;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUserLogin() {
            return userLogin;
        }

        public void setUserLogin(String userLogin) {
            this.userLogin = userLogin;
        }

        public NowCell getNow() {
            return now;
        }

        public void setNow(NowCell now) {
            this.now = now;
        }

        public PlanCell getPlan() {
            return plan;
        }

        public void setPlan(PlanCell plan) {
            this.plan = plan;
        }

        @Override
        public int compareTo(OppressionRow row) {
            return 0;
        }
    }

    @XmlRootElement(name = "now")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class NowCell {

        @XmlAttribute
        List<OppressionXMLIssue> issues;

        public List<OppressionXMLIssue> getIssues() {
            return issues;
        }

        public void setIssues(List<OppressionXMLIssue> issues) {
            this.issues = issues;
        }
    }

    @XmlRootElement(name = "plan")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class PlanCell {

        @XmlAttribute
        List<OppressionXMLIssue> issues;

        public List<OppressionXMLIssue> getIssues() {
            return issues;
        }

        public void setIssues(List<OppressionXMLIssue> issues) {
            this.issues = issues;
        }
    }

    @XmlRootElement(name = "issue")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class OppressionXMLIssue {

        @XmlAttribute
        boolean isPlan;
        @XmlAttribute
        String summary;
        @XmlAttribute
        String key;
        @XmlAttribute
        String issuePriority;
        @XmlAttribute
        String typeName;
        @XmlAttribute
        String iconHtml;
        @XmlAttribute
        long timespent;
        @XmlAttribute
        long estimate;
        @XmlAttribute
        Date start;
        @XmlAttribute
        Date end;
        @XmlAttribute
        Integer megaPriority;
        @XmlAttribute
        List<String> actions;
        @XmlAttribute
        public Long issuePrioritySequence;
        @XmlAttribute
        public boolean isCandidate;


        public boolean isPlan() {
            return isPlan;
        }

        public void setPlan(boolean plan) {
            isPlan = plan;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getIssuePriority() {
            return issuePriority;
        }

        public void setIssuePriority(String issuePriority) {
            this.issuePriority = issuePriority;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getIconHtml() {
            return iconHtml;
        }

        public void setIconHtml(String iconHtml) {
            this.iconHtml = iconHtml;
        }

        public long getTimespent() {
            return timespent;
        }

        public void setTimespent(long timespent) {
            this.timespent = timespent;
        }

        public long getEstimate() {
            return estimate;
        }

        public void setEstimate(long estimate) {
            this.estimate = estimate;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public Integer getMegaPriority() {
            return megaPriority;
        }

        public void setMegaPriority(Integer megaPriority) {
            this.megaPriority = megaPriority;
        }

        public List<String> getActions() {
            return actions;
        }

        public void setActions(List<String> actions) {
            this.actions = actions;
        }

        public Long getIssuePrioritySequence() {
            return issuePrioritySequence;
        }

        public void setIssuePrioritySequence(Long issuePrioritySequence) {
            this.issuePrioritySequence = issuePrioritySequence;
        }

        public boolean isCandidate() {
            return isCandidate;
        }

        public void setCandidate(boolean candidate) {
            isCandidate = candidate;
        }

        @Override
        public String toString() {
            return "OppressionXMLIssue{" +
                    "summary='" + summary + '\'' +
                    ", key='" + key + '\'' +
                    ", issuePriority='" + issuePriority + '\'' +
                    ", timespent=" + timespent +
                    ", estimate=" + estimate +
                    ", start=" + start +
                    ", end=" + end +
                    ", megaPriority=" + megaPriority +
                    ", actions=" + actions +
                    ", issuePrioritySequence=" + issuePrioritySequence +
                    '}';
        }
    }

    @JsonIgnoreProperties()
    public static class MegaPriorityUpdateBean {
        @JsonProperty
        private String issue;
        @JsonProperty
        private int megaPriority;
        @JsonProperty
        private String statuses;
        @JsonProperty
        private String newAssignee;

        public String issue() {
            return issue;
        }

        public int megaPriority() {
            return megaPriority;
        }

        public String statuses() {
            return statuses;
        }

        public String newAssignee() {
            return newAssignee;
        }

    }

    public static CacheControl noCache() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
        return cacheControl;
    }

    private class FakeIssue implements Issue {

        private long remainingEstimate;
        private String summary;
        private Status statusObject;
        private String DEFAULT_OPEN_STATUS = "1";
        private Priority priorityObject;
        private IssueType issueType;
        private String issueKey;

        public FakeIssue(StatusManager statusManager, PriorityManager priorityManager) {
            statusObject = statusManager.getStatus(DEFAULT_OPEN_STATUS);
            priorityObject = priorityManager.getDefaultPriority();
        }

        public void setRemainingEstimate(long remainingEstimate) {
            this.remainingEstimate = remainingEstimate;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        @Override
        public Long getId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getProject() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Project getProjectObject() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getIssueType() {
            return issueType.getGenericValue();
        }

        @Override
        public IssueType getIssueTypeObject() {
            return issueType;
        }

        @Override
        public String getSummary() {
            return summary;
        }

        @Override
        public User getAssigneeUser() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public User getAssignee() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getAssigneeId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<GenericValue> getComponents() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<ProjectComponent> getComponentObjects() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public User getReporterUser() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public User getReporter() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getReporterId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getDescription() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getEnvironment() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<Version> getAffectedVersions() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<Version> getFixVersions() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Timestamp getDueDate() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getSecurityLevel() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getSecurityLevelId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getPriority() {
            return priorityObject.getGenericValue();
        }

        @Override
        public Priority getPriorityObject() {
            return priorityObject;
        }

        @Override
        public String getResolutionId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getResolution() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Resolution getResolutionObject() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getKey() {
            return issueKey;
        }

        @Override
        public Long getVotes() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getWatches() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Timestamp getCreated() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Timestamp getUpdated() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Timestamp getResolutionDate() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getWorkflowId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getCustomFieldValue(CustomField customField) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getStatus() {
            return statusObject.getGenericValue();
        }

        @Override
        public Status getStatusObject() {
            return statusObject;
        }

        @Override
        public Long getOriginalEstimate() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getEstimate() {
            return remainingEstimate;
        }

        @Override
        public Long getTimeSpent() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getExternalFieldValue(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isSubTask() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getParentId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isCreated() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Issue getParentObject() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getParent() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<GenericValue> getSubTasks() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<Issue> getSubTaskObjects() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isEditable() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public IssueRenderContext getIssueRenderContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<Attachment> getAttachments() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Set<Label> getLabels() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getString(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Timestamp getTimestamp(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Long getLong(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public GenericValue getGenericValue() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void store() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setIssueType(IssueType next) {
            this.issueType = next;
        }

        public void setIssueKey(String candidateIssueKey) {
            this.issueKey = candidateIssueKey;
        }
    }
}
