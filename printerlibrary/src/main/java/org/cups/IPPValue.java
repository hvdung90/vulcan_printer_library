package org.cups;

import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class IPPValue {
    boolean boolean_value;
    String charset;
    char[] data;
    char[] date_value;
    int integer_value;
    int length;
    int lower;
    String text;
    byte units;
    long unix_time;
    int upper;
    int value_type = 33;
    int xres;
    int yres;

    public IPPValue(byte p_byte) {
        this.integer_value = p_byte;
    }

    public IPPValue(short p_short) {
        this.integer_value = p_short;
    }

    public IPPValue(int p_int) {
        this.integer_value = p_int;
    }

    public IPPValue(int p_int, boolean anything) {
        this.integer_value = p_int;
    }

    public IPPValue(boolean p_boolean) {
        this.boolean_value = p_boolean;
    }

    public IPPValue(char[] p_date) {
        this.date_value = p_date;
        this.unix_time = IPPDateToTime();
    }

    public IPPValue(String p_charset, String p_text) {
        this.charset = p_charset;
        this.text = p_text;
    }

    public IPPValue(int p_lower, int p_upper) {
        if (p_lower < p_upper) {
            this.lower = p_lower;
            this.upper = p_upper;
            return;
        }
        this.lower = p_upper;
        this.upper = p_lower;
    }

    public IPPValue(byte p_units, int p_xres, int p_yres) {
        this.units = p_units;
        this.xres = p_xres;
        this.yres = p_yres;
    }

    public IPPValue(int p_length, char[] p_data) {
        this.length = p_length;
        this.data = p_data;
    }

    public int getInteger() {
        return this.integer_value;
    }

    public boolean isTrue() {
        return this.boolean_value;
    }

    public char[] getDate() {
        return this.date_value;
    }

    public long IPPDateToTime() {
        int raw_offset = ((this.date_value[9] * 3600) + (this.date_value[10] * '<')) * 1000;
        if (this.date_value[8] == '-') {
            raw_offset = 0 - raw_offset;
        }
        TimeZone tz = new SimpleTimeZone(raw_offset, "GMT");
        IPPCalendar cl = new IPPCalendar();
        int year = (this.date_value[0] << '\b') | (this.date_value[1] - 1900);
        int month = this.date_value[2] - 1;
        char c = this.date_value[3];
        char c2 = this.date_value[4];
        char c3 = this.date_value[5];
        char c4 = this.date_value[6];
        cl.setTimeZone(tz);
        cl.set(year, month, c, c2, c3, c4);
        return cl.getTimeInMillis() / 1000;
    }
}
