package org.cups;

public class IPPStatus {
    int status;
    String status_text;

    public IPPStatus(int p_status) {
        this.status = p_status;
        switch (this.status) {
            case 0:
                this.status_text = "OK";
                return;
            case 1:
                this.status_text = "OK, substituted";
                return;
            case 2:
                this.status_text = "OK, conflict";
                return;
            case 3:
                this.status_text = "OK, ignored subscriptions";
                return;
            case 4:
                this.status_text = "OK, ignored notifications";
                return;
            case 5:
                this.status_text = "OK, too many events";
                return;
            case 6:
                this.status_text = "OK, but cancel subscription";
                return;
            case IPPDefs.REDIRECTION_OTHER_SITE:
                this.status_text = "Redirected to other site";
                return;
            case 1024:
                this.status_text = "Bad request";
                return;
            case IPPDefs.FORBIDDEN:
                this.status_text = "Forbidden";
                return;
            case IPPDefs.NOT_AUTHENTICATED:
                this.status_text = "Not authenticated";
                return;
            case IPPDefs.NOT_AUTHORIZED:
                this.status_text = "Not authorized";
                return;
            case IPPDefs.NOT_POSSIBLE:
                this.status_text = "Not possible";
                return;
            case IPPDefs.TIMEOUT:
                this.status_text = "Timeout";
                return;
            case IPPDefs.NOT_FOUND:
                this.status_text = "Not found";
                return;
            case IPPDefs.GONE:
                this.status_text = "Gone";
                return;
            case IPPDefs.REQUEST_ENTITY:
                this.status_text = "Request entity";
                return;
            case IPPDefs.REQUEST_VALUE:
                this.status_text = "Request value";
                return;
            case IPPDefs.DOCUMENT_FORMAT:
                this.status_text = "Document format";
                return;
            case IPPDefs.ATTRIBUTES:
                this.status_text = "Attributes";
                return;
            case IPPDefs.URI_SCHEME:
                this.status_text = "URI scheme";
                return;
            case IPPDefs.CHARSET:
                this.status_text = "Charset";
                return;
            case IPPDefs.CONFLICT:
                this.status_text = "Conflict";
                return;
            case IPPDefs.COMPRESSION_NOT_SUPPORTED:
                this.status_text = "Compression not supported";
                return;
            case 1040:
                this.status_text = "Compression error";
                return;
            case IPPDefs.DOCUMENT_FORMAT_ERROR:
                this.status_text = "Document format error";
                return;
            case 1042:
                this.status_text = "Document access error";
                return;
            case IPPDefs.ATTRIBUTES_NOT_SETTABLE:
                this.status_text = "Attributes not settable";
                return;
            case IPPDefs.IGNORED_ALL_SUBSCRIPTIONS:
                this.status_text = "Ignored all subscriptions";
                return;
            case 1045:
                this.status_text = "Too many subscriptions";
                return;
            case 1046:
                this.status_text = "Ingored all notifications";
                return;
            case IPPDefs.PRINT_SUPPORT_FILE_NOT_FOUND:
                this.status_text = "Support file not found";
                return;
            case IPPDefs.INTERNAL_ERROR:
                this.status_text = "Internal error";
                return;
            case IPPDefs.OPERATION_NOT_SUPPORTED:
                this.status_text = "Operation not supported";
                return;
            case IPPDefs.SERVICE_UNAVAILABLE:
                this.status_text = "Service unavailable";
                return;
            case IPPDefs.VERSION_NOT_SUPPORTED:
                this.status_text = "Version not supported";
                return;
            case IPPDefs.DEVICE_ERROR:
                this.status_text = "Device error";
                return;
            case IPPDefs.TEMPORARY_ERROR:
                this.status_text = "Temporary error";
                return;
            case IPPDefs.NOT_ACCEPTING:
                this.status_text = "Not accepting";
                return;
            case IPPDefs.PRINTER_BUSY:
                this.status_text = "Printer busy";
                return;
            case IPPDefs.ERROR_JOB_CANCELLED:
                this.status_text = "Error, job cancelled";
                return;
            case IPPDefs.MULTIPLE_JOBS_NOT_SUPPORTED:
                this.status_text = "Multiple jobs not supported";
                return;
            case IPPDefs.PRINTER_IS_DEACTIVATED:
                this.status_text = "Printer is de-activated";
                return;
            default:
                this.status_text = "Unknown error";
                return;
        }
    }
}
