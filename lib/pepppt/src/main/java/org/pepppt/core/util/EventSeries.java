package org.pepppt.core.util;

import java.util.ArrayList;

/**
 * This class count the number of occurrences in a specified time frame.
 */
public class EventSeries {

    private ArrayList<Long> list;
    final private Object lock = new Object();

    public EventSeries() {
        list = new ArrayList<>();
    }
    public void push(long timestamp) {
        synchronized (lock) {
            list.add(timestamp);
        }
    }

    public int removeOlderThen(long olderthen) {
        synchronized (lock) {
            ArrayList<Long> newlist = new ArrayList<>();
            for (long l : list) {
                if (l >= olderthen)
                    newlist.add(l);
            }
            list = newlist;
            return list.size();
        }
    }
}
