package org.cups;

import java.util.GregorianCalendar;

/* access modifiers changed from: package-private */
public class IPPCalendar extends GregorianCalendar {
    IPPCalendar() {
    }

    public long getTimeInMillis() {
        return super.getTimeInMillis();
    }

    public int getUnixTime() {
        return (int) (getTimeInMillis() / 1000);
    }
}
