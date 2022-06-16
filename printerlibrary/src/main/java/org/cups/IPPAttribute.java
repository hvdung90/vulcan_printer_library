package org.cups;

import com.itextpdf.text.pdf.BidiOrder;
import com.itextpdf.text.pdf.codec.TIFFConstants;

import java.util.ArrayList;
import java.util.List;

public class IPPAttribute {
    int group_tag;
    String name;
    int value_tag;
    List values = new ArrayList();

    public IPPAttribute(int p_group_tag, int p_value_tag, String p_name) {
        this.group_tag = p_group_tag;
        this.value_tag = p_value_tag;
        this.name = p_name;
    }

    public boolean addBoolean(boolean p_bool) {
        this.values.add(new IPPValue(p_bool));
        return true;
    }

    public boolean addBooleans(boolean[] p_bools) {
        if (p_bools.length < 1) {
            return false;
        }
        for (boolean z : p_bools) {
            this.values.add(new IPPValue(z));
        }
        return true;
    }

    public boolean addEnum(int p_int) {
        this.values.add(new IPPValue(p_int, true));
        return true;
    }

    public boolean addInteger(int p_int) {
        this.values.add(new IPPValue(p_int));
        return true;
    }

    public boolean addIntegers(int[] p_ints) {
        if (p_ints.length < 1) {
            return false;
        }
        for (int i : p_ints) {
            this.values.add(new IPPValue(i));
        }
        return true;
    }

    public boolean addString(String p_charset, String p_text) {
        String l_value;
        String final_value;
        if (this.value_tag == 72 && p_text == "C") {
            l_value = "en";
        } else {
            l_value = p_text;
        }
        if (this.value_tag == 72 || this.value_tag == 71) {
            StringBuffer temp = new StringBuffer(l_value.length());
            for (int i = 0; i < l_value.length(); i++) {
                char c = l_value.charAt(i);
                if (c == '_') {
                    c = '-';
                } else if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                temp.append(c);
            }
            final_value = temp.toString();
        } else {
            final_value = l_value;
        }
        this.values.add(new IPPValue(p_charset, final_value));
        return true;
    }

    public boolean addStrings(String p_charset, String[] p_texts) {
        if (p_texts.length < 1) {
            return false;
        }
        for (String str : p_texts) {
            addString(p_charset, str);
        }
        return true;
    }

    public boolean addDate(char[] p_date) {
        this.values.add(new IPPValue(p_date));
        return true;
    }

    public boolean addRange(int p_lower, int p_upper) {
        this.values.add(new IPPValue(p_lower, p_upper));
        return true;
    }

    public boolean addRanges(int[] p_lower, int[] p_upper) {
        if (p_lower.length != p_upper.length) {
            return false;
        }
        for (int i = 0; i < p_lower.length; i++) {
            addRange(p_lower[i], p_upper[i]);
        }
        return true;
    }

    public boolean addResolution(byte p_units, int p_xres, int p_yres) {
        this.values.add(new IPPValue(p_units, p_xres, p_yres));
        return true;
    }

    public boolean addResolutions(byte p_units, int[] p_xres, int[] p_yres) {
        if (p_xres.length != p_yres.length) {
            return false;
        }
        for (int i = 0; i < p_xres.length; i++) {
            addResolution(p_units, p_xres[i], p_yres[i]);
        }
        return true;
    }

    public boolean addSeparator() {
        this.value_tag = 0;
        this.group_tag = 0;
        return true;
    }

    public int sizeInBytes(int last_group) {
        int bytes = 0;
        if (last_group != this.group_tag) {
            bytes = 0 + 1;
        }
        int bytes2 = bytes + 1 + 2 + this.name.length();
        for (int i = 0; i < this.values.size(); i++) {
            IPPValue val = (IPPValue) this.values.get(i);
            if (i > 0) {
                bytes2 += 3;
            }
            switch (this.value_tag) {
                case 33:
                case 35:
                    bytes2 = bytes2 + 2 + 4;
                    break;
                case 34:
                    bytes2 = bytes2 + 2 + 1;
                    break;
                case 36:
                case IPPDefs.HOLD_NEW_JOBS /*{ENCODED_INT: 37}*/:
                case 38:
                case 39:
                case 40:
                case IPPDefs.RESTART_PRINTER /*{ENCODED_INT: 41}*/:
                case 42:
                case 43:
                case IPPDefs.REPROCESS_JOB /*{ENCODED_INT: 44}*/:
                case IPPDefs.CANCEL_CURRENT_JOB /*{ENCODED_INT: 45}*/:
                case 46:
                case 47:
                case 52:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 67:
                default:
                    bytes2 += 2;
                    if (val.data != null) {
                        bytes2 += val.data.length;
                        break;
                    } else {
                        break;
                    }
                case 48:
                case IPPDefs.TAG_TEXT /*{ENCODED_INT: 65}*/:
                case IPPDefs.TAG_NAME /*{ENCODED_INT: 66}*/:
                case IPPDefs.TAG_KEYWORD /*{ENCODED_INT: 68}*/:
                case IPPDefs.TAG_URI /*{ENCODED_INT: 69}*/:
                case IPPDefs.TAG_URISCHEME /*{ENCODED_INT: 70}*/:
                case IPPDefs.TAG_CHARSET /*{ENCODED_INT: 71}*/:
                case IPPDefs.TAG_LANGUAGE /*{ENCODED_INT: 72}*/:
                case IPPDefs.TAG_MIMETYPE /*{ENCODED_INT: 73}*/:
                    bytes2 = bytes2 + 2 + val.text.length();
                    break;
                case 49:
                    bytes2 = bytes2 + 2 + 11;
                    break;
                case 50:
                    bytes2 = bytes2 + 2 + 9;
                    break;
                case 51:
                    bytes2 = bytes2 + 2 + 8;
                    break;
                case 53:
                case IPPDefs.TAG_NAMELANG /*{ENCODED_INT: 54}*/:
                    bytes2 = bytes2 + 6 + val.charset.length() + val.text.length();
                    break;
            }
        }
        return bytes2;
    }

    public byte[] getBytes(int sz, int last_group) {
        int bi;
        int bi2 = 0;
        byte[] bytes = new byte[sz];
        if (this.group_tag != last_group) {
            bytes[0] = (byte) this.group_tag;
            int last_group2 = this.group_tag;
            bi2 = 0 + 1;
        }
        int bi3 = bi2 + 1;
        bytes[bi2] = (byte) this.value_tag;
        int bi4 = bi3 + 1;
        bytes[bi3] = (byte) ((this.name.length() & 65280) >> 8);
        int bi5 = bi4 + 1;
        bytes[bi4] = (byte) (this.name.length() & TIFFConstants.TIFFTAG_OSUBFILETYPE);
        int j = 0;
        while (true) {
            bi = bi5;
            if (j >= this.name.length()) {
                break;
            }
            bi5 = bi + 1;
            bytes[bi] = (byte) this.name.charAt(j);
            j++;
        }
        for (int i = 0; i < this.values.size(); i++) {
            if (i > 0) {
                int bi6 = bi + 1;
                bytes[bi] = (byte) this.value_tag;
                int bi7 = bi6 + 1;
                bytes[bi6] = 0;
                bytes[bi7] = 0;
                bi = bi7 + 1;
            }
            IPPValue val = (IPPValue) this.values.get(i);
            switch (this.value_tag) {
                case 33:
                case 35:
                    int bi8 = bi + 1;
                    bytes[bi] = 0;
                    int bi9 = bi8 + 1;
                    bytes[bi8] = 4;
                    int bi10 = bi9 + 1;
                    bytes[bi9] = (byte) ((val.integer_value & -16777216) >> 24);
                    int bi11 = bi10 + 1;
                    bytes[bi10] = (byte) ((val.integer_value & 16711680) >> 16);
                    int bi12 = bi11 + 1;
                    bytes[bi11] = (byte) ((val.integer_value & 65280) >> 8);
                    bi = bi12 + 1;
                    bytes[bi12] = (byte) (val.integer_value & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    break;
                case 34:
                    int bi13 = bi + 1;
                    bytes[bi] = 0;
                    int bi14 = bi13 + 1;
                    bytes[bi13] = 1;
                    if (val.boolean_value) {
                        bytes[bi14] = 1;
                        bi = bi14 + 1;
                        break;
                    } else {
                        bytes[bi14] = 0;
                        bi = bi14 + 1;
                        break;
                    }
                case 36:
                case IPPDefs.HOLD_NEW_JOBS /*{ENCODED_INT: 37}*/:
                case 38:
                case 39:
                case 40:
                case IPPDefs.RESTART_PRINTER /*{ENCODED_INT: 41}*/:
                case 42:
                case 43:
                case IPPDefs.REPROCESS_JOB /*{ENCODED_INT: 44}*/:
                case IPPDefs.CANCEL_CURRENT_JOB /*{ENCODED_INT: 45}*/:
                case 46:
                case 47:
                case 52:
                case 55:
                case 56:
                case 57:
                case 58:
                case 59:
                case 60:
                case 61:
                case 62:
                case 63:
                case 64:
                case 67:
                default:
                    if (val.data != null) {
                        int n = val.data.length;
                        int bi15 = bi + 1;
                        bytes[bi] = (byte) ((65280 & n) >> 8);
                        bi = bi15 + 1;
                        bytes[bi15] = (byte) (n & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                        int j2 = 0;
                        while (j2 < val.data.length) {
                            bytes[bi] = (byte) val.data[j2];
                            j2++;
                            bi++;
                        }
                        break;
                    } else {
                        break;
                    }
                case 48:
                case IPPDefs.TAG_TEXT /*{ENCODED_INT: 65}*/:
                case IPPDefs.TAG_NAME /*{ENCODED_INT: 66}*/:
                case IPPDefs.TAG_KEYWORD /*{ENCODED_INT: 68}*/:
                case IPPDefs.TAG_URI /*{ENCODED_INT: 69}*/:
                case IPPDefs.TAG_URISCHEME /*{ENCODED_INT: 70}*/:
                case IPPDefs.TAG_CHARSET /*{ENCODED_INT: 71}*/:
                case IPPDefs.TAG_LANGUAGE /*{ENCODED_INT: 72}*/:
                case IPPDefs.TAG_MIMETYPE /*{ENCODED_INT: 73}*/:
                    int bi16 = bi + 1;
                    bytes[bi] = (byte) ((val.text.length() & 65280) >> 8);
                    bi = bi16 + 1;
                    bytes[bi16] = (byte) (val.text.length() & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int j3 = 0;
                    while (j3 < val.text.length()) {
                        bytes[bi] = (byte) val.text.charAt(j3);
                        j3++;
                        bi++;
                    }
                    break;
                case 49:
                    int bi17 = bi + 1;
                    bytes[bi] = 0;
                    int bi18 = bi17 + 1;
                    bytes[bi17] = BidiOrder.AN;
                    for (int j4 = 0; j4 < 11; j4++) {
                        bi18++;
                        bytes[bi18] = (byte) val.date_value[j4];
                    }
                    bi = bi18;
                    break;
                case 50:
                    int bi19 = bi + 1;
                    bytes[bi] = 0;
                    int bi20 = bi19 + 1;
                    bytes[bi19] = 9;
                    int bi21 = bi20 + 1;
                    bytes[bi20] = (byte) ((val.xres & -16777216) >> 24);
                    int bi22 = bi21 + 1;
                    bytes[bi21] = (byte) ((val.xres & 16711680) >> 16);
                    int bi23 = bi22 + 1;
                    bytes[bi22] = (byte) ((val.xres & 65280) >> 8);
                    int bi24 = bi23 + 1;
                    bytes[bi23] = (byte) (val.xres & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int bi25 = bi24 + 1;
                    bytes[bi24] = (byte) ((val.yres & -16777216) >> 24);
                    int bi26 = bi25 + 1;
                    bytes[bi25] = (byte) ((val.yres & 16711680) >> 16);
                    int bi27 = bi26 + 1;
                    bytes[bi26] = (byte) ((val.yres & 65280) >> 8);
                    int bi28 = bi27 + 1;
                    bytes[bi27] = (byte) (val.yres & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    bytes[bi28] = val.units;
                    bi = bi28 + 1;
                    break;
                case 51:
                    int bi29 = bi + 1;
                    bytes[bi] = 0;
                    int bi30 = bi29 + 1;
                    bytes[bi29] = 8;
                    int bi31 = bi30 + 1;
                    bytes[bi30] = (byte) ((val.lower & -16777216) >> 24);
                    int bi32 = bi31 + 1;
                    bytes[bi31] = (byte) ((val.lower & 16711680) >> 16);
                    int bi33 = bi32 + 1;
                    bytes[bi32] = (byte) ((val.lower & 65280) >> 8);
                    int bi34 = bi33 + 1;
                    bytes[bi33] = (byte) (val.lower & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int bi35 = bi34 + 1;
                    bytes[bi34] = (byte) ((val.upper & -16777216) >> 24);
                    int bi36 = bi35 + 1;
                    bytes[bi35] = (byte) ((val.upper & 16711680) >> 16);
                    int bi37 = bi36 + 1;
                    bytes[bi36] = (byte) ((val.upper & 65280) >> 8);
                    bi = bi37 + 1;
                    bytes[bi37] = (byte) (val.upper & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    break;
                case 53:
                case IPPDefs.TAG_NAMELANG /*{ENCODED_INT: 54}*/:
                    int n2 = val.charset.length() + val.text.length() + 4;
                    int bi38 = bi + 1;
                    bytes[bi] = (byte) ((65280 & n2) >> 8);
                    int bi39 = bi38 + 1;
                    bytes[bi38] = (byte) (n2 & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int n3 = val.charset.length();
                    int bi40 = bi39 + 1;
                    bytes[bi39] = (byte) ((65280 & n3) >> 8);
                    int bi41 = bi40 + 1;
                    bytes[bi40] = (byte) (n3 & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int j5 = 0;
                    while (j5 < val.charset.length()) {
                        bytes[bi41] = (byte) val.charset.charAt(j5);
                        j5++;
                        bi41++;
                    }
                    int n4 = val.text.length();
                    int bi42 = bi41 + 1;
                    bytes[bi41] = (byte) ((65280 & n4) >> 8);
                    bi = bi42 + 1;
                    bytes[bi42] = (byte) (n4 & TIFFConstants.TIFFTAG_OSUBFILETYPE);
                    int j6 = 0;
                    while (j6 < ((byte) val.text.length())) {
                        bytes[bi] = (byte) val.text.charAt(j6);
                        j6++;
                        bi++;
                    }
                    break;
            }
        }
        return bytes;
    }

    public String toString() {
        if (this.values == null || this.values.size() < 1) {
            return " ---- Separator ---- \n";
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.values.size(); i++) {
            IPPValue val = (IPPValue) this.values.get(i);
            ret.append("ATTR GTAG: " + this.group_tag + "\n");
            ret.append("ATTR VTAG: " + this.value_tag + "\n");
            ret.append("ATTR NAME: " + this.name + "\n");
            switch (this.value_tag) {
                case 0:
                    ret.append(" ---- Separator ---- \n");
                    break;
                case 33:
                case 35:
                    ret.append(" INTEGER: " + val.integer_value + "\n");
                    break;
                case 34:
                    ret.append(" BOOLEAN: " + val.boolean_value + "\n");
                    break;
                case 48:
                case IPPDefs.TAG_TEXT /*{ENCODED_INT: 65}*/:
                case IPPDefs.TAG_NAME /*{ENCODED_INT: 66}*/:
                case IPPDefs.TAG_KEYWORD /*{ENCODED_INT: 68}*/:
                case IPPDefs.TAG_URI /*{ENCODED_INT: 69}*/:
                case IPPDefs.TAG_URISCHEME /*{ENCODED_INT: 70}*/:
                case IPPDefs.TAG_CHARSET /*{ENCODED_INT: 71}*/:
                case IPPDefs.TAG_LANGUAGE /*{ENCODED_INT: 72}*/:
                case IPPDefs.TAG_MIMETYPE /*{ENCODED_INT: 73}*/:
                    ret.append(" CHARSET: " + val.charset + " TEXT: " + val.text + "\n");
                    break;
                case 49:
                    ret.append(" DATE: " + val.unix_time);
                    break;
                case 50:
                    ret.append(" UNITS: " + ((int) val.units) + " XRES: " + val.xres + " YRES: " + val.yres + "\n");
                    break;
                case 51:
                    ret.append(" LOWER: " + val.lower + " UPPER: " + val.upper + "\n");
                    break;
                case 53:
                case IPPDefs.TAG_NAMELANG /*{ENCODED_INT: 54}*/:
                    ret.append(" CHARSET: " + val.charset + " TEXT: " + val.text + "\n");
                    break;
            }
        }
        return ret.toString();
    }

    public void dump_values() {
        System.out.println(toString());
    }

    public int getGroupTag() {
        return this.group_tag;
    }

    public int getValueTag() {
        return this.value_tag;
    }

    public String getName() {
        return this.name;
    }

    public List getValues() {
        return this.values;
    }
}
