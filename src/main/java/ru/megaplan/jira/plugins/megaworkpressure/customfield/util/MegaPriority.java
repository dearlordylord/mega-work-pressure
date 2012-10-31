package ru.megaplan.jira.plugins.megaworkpressure.customfield.util;

import com.atlassian.crowd.embedded.api.User;
import ru.megaplan.jira.plugins.megaworkpressure.customfield.OppressionPriorityCFType;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.08.12
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public class MegaPriority implements Serializable {
    private String userName;
    private Integer priority;

    public MegaPriority() {

    }

    public MegaPriority(String userName, Integer priority) {
        this.userName = userName;
        this.priority = priority;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "[" + userName + " : " + priority + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MegaPriority that = (MegaPriority) o;

        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        return result;
    }
}
