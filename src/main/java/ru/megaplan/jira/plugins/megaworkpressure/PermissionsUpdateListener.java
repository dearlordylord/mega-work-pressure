package ru.megaplan.jira.plugins.megaworkpressure;

import com.atlassian.plugin.event.PluginEventListener;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.megaworkpressure.servlet.filter.UserPressureServletFilter;
import ru.megaplan.jira.plugins.permission.manager.event.ConfigurationUpdatedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 6/17/12
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionsUpdateListener {

    private static final Logger log = Logger.getLogger(PermissionsUpdateListener.class);

    private final UserPressureServletFilter userPressureServletFilter;

    public PermissionsUpdateListener(UserPressureServletFilter userPressureServletFilter) {
        this.userPressureServletFilter = checkNotNull(userPressureServletFilter);
    }


    @PluginEventListener
    public void onPluginRefreshedEvent(ConfigurationUpdatedEvent event) {
        if (event == null) return;
        if (userPressureServletFilter.WORK_PRESSURE_GROUP.equals(event.getPermissionGroup())) {
            userPressureServletFilter.initConfig();
        }
    }
}
