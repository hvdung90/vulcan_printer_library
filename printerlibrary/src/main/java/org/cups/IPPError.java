package org.cups;


public class IPPError {
    private int error_number;
    private String error_string = ippErrorString(this.error_number);

    public IPPError(int p_error) {
        this.error_number = p_error;
    }

    private String ippErrorString(int error) {
        String[] status_oks = {"successful-ok", "successful-ok-ignored-or-substituted-attributes", "successful-ok-conflicting-attributes", "successful-ok-ignored-subscriptions", "successful-ok-ignored-notifications", "successful-ok-too-many-events", "successful-ok-but-cancel-subscription"};
        String[] status_400s = {"client-error-bad-request", "client-error-forbidden", "client-error-not-authenticated", "client-error-not-authorized", "client-error-not-possible", "client-error-timeout", "client-error-not-found", "client-error-gone", "client-error-request-entity-too-large", "client-error-request-value-too-long", "client-error-document-format-not-supported", "client-error-attributes-or-values-not-supported", "client-error-uri-scheme-not-supported", "client-error-charset-not-supported", "client-error-conflicting-attributes", "client-error-compression-not-supported", "client-error-compression-error", "client-error-document-format-error", "client-error-document-access-error", "client-error-attributes-not-settable", "client-error-ignored-all-subscriptions", "client-error-too-many-subscriptions", "client-error-ignored-all-notifications", "client-error-print-support-file-not-found"};
        String[] status_500s = {"server-error-internal-error", "server-error-operation-not-supported", "server-error-service-unavailable", "server-error-version-not-supported", "server-error-device-error", "server-error-temporary-error", "server-error-not-accepting-jobs", "server-error-busy", "server-error-job-canceled", "server-error-multiple-document-jobs-not-supported", "server-error-printer-is-deactivated"};
        if (error >= 0 && error <= 6) {
            return status_oks[error];
        }
        if (error == 768) {
            return "redirection-other-site";
        }
        if (error >= 1024 && error <= 1047) {
            return status_400s[error - 1024];
        }
        if (error < 1280 || error > 1290) {
            return "" + error;
        }
        return status_500s[error - 1280];
    }
}
