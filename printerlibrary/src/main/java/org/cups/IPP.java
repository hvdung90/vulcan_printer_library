package org.cups;

import java.util.LinkedList;
import java.util.List;

public class IPP {
    public List attrs = new LinkedList();
    int current = -1;
    short current_tag = 0;
    int last = -1;
    public IPPRequest request;
    short state = 0;
    public IPPStatus status;

    public boolean addAttribute(IPPAttribute a) {
        this.attrs.add(a);
        return true;
    }

    public IPPAttribute getCurrentAttribute() {
        if (this.current >= 0) {
            return (IPPAttribute) this.attrs.get(this.current);
        }
        return null;
    }

    public IPPAttribute ippFindAttribute(String p_name, int p_type) {
        if (p_name.length() < 1) {
            return null;
        }
        this.current = -1;
        return ippFindNextAttribute(p_name, p_type);
    }

    public IPPAttribute ippFindNextAttribute(String p_name, int p_type) {
        if (p_name.length() < 1) {
            return null;
        }
        if (this.current < 0 || this.current >= this.attrs.size() - 1) {
            this.current = 0;
        } else {
            this.current++;
        }
        for (int i = this.current; i < this.attrs.size(); i++) {
            IPPAttribute tmp = (IPPAttribute) this.attrs.get(i);
            int value_tag = tmp.value_tag & IPPDefs.TAG_MASK;
            if (tmp.name.length() > 0 && tmp.name == p_name && (value_tag == p_type || p_type == 0 || ((value_tag == 53 && p_type == 65) || (value_tag == 54 && p_type == 66)))) {
                this.current = i;
                return tmp;
            }
        }
        return null;
    }

    public int sizeInBytes() {
        int bytes = 8;
        int last_group = 0;
        for (int i = 0; i < this.attrs.size(); i++) {
            IPPAttribute a = (IPPAttribute) this.attrs.get(i);
            bytes += a.sizeInBytes(last_group);
            last_group = a.group_tag;
        }
        return bytes + 1;
    }

    /* access modifiers changed from: package-private */
    public int ippBytes() {
        return 0;
    }

    public void setRequestID(short p_id) {
        this.request.request_id = p_id;
    }

    public void setRequestOperationID(short p_operation_id) {
        this.request.operation_id = p_operation_id;
    }

    public void dump_response() {
        for (int i = 0; i < this.attrs.size(); i++) {
            IPPAttribute a = (IPPAttribute) this.attrs.get(i);
            a.dump_values();
            int last_group = a.group_tag;
        }
    }
}
