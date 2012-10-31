package ru.megaplan.jira.plugins.megaworkpressure.customfield.util;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.08.12
 * Time: 18:54
 * To change this template use File | Settings | File Templates.
 */
public class MegaPriorityComparator implements Comparator<MegaPriority> {
    @Override
    public int compare(MegaPriority megaPriority1, MegaPriority megaPriority2) {
        return megaPriority1.getPriority().compareTo(megaPriority2.getPriority());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MegaPriorityComparator) return true;
        return false;
    }
}
