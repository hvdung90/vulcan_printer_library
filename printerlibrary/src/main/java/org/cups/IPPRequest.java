package org.cups;

public class IPPRequest {
    int op_status;
    short operation_id;
    int request_id;
    short status_code;
    char[] version;

    public IPPRequest() {
        this.version = new char[2];
    }

    public IPPRequest(int p_request_id, short p_operation_id) {
        this.version = new char[2];
        this.version[0] = 1;
        this.version[1] = 1;
        this.request_id = p_request_id;
        this.operation_id = p_operation_id;
    }

    public void setStatus(short p_status_code) {
        this.status_code = p_status_code;
    }

    public void setOpStatus(short p_status_code) {
        this.op_status = p_status_code;
    }
}
