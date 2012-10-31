package ru.megaplan.jira.plugins.megaworkpressure.resource;

import com.atlassian.jira.config.StatusManager;
import com.google.common.collect.Lists;
import org.springframework.core.io.AbstractResource;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/status")
public class StatusResource {

    private final StatusManager statusManager;


    public StatusResource(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    @GET
    @Path ("/all")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllStatuses(@Context HttpServletRequest request) {
        List<Status> responseList = new ArrayList<Status>();
        Collections.sort(responseList);
        for (com.atlassian.jira.issue.status.Status status : statusManager.getStatuses()) {
            responseList.add(new Status(status.getId(), status.getName()));
        }
        return Response.ok(responseList).cacheControl(OppressionResource.noCache()).build();


    }

    @XmlRootElement(name = "status")
    @XmlAccessorType(XmlAccessType.FIELD)
    static public class Status implements Comparable<Status>{
        @XmlAttribute
        private String value;
        @XmlAttribute
        private String label;

        public Status(String key, String name) {
            this.value = key;
            this.label = name;
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
        public int compareTo(Status status) {
            return label.compareTo(status.label);
        }
    }




}