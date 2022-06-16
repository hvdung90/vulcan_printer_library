package org.cups;

import com.itextpdf.text.pdf.PdfObject;

public class CupsJob {
    public String document_format = PdfObject.NOTHING;
    public String job_hold_until = PdfObject.NOTHING;
    public int job_id = -1;
    public int job_k_octets = 0;
    public int job_media_sheets_completed = 0;
    public String job_more_info = PdfObject.NOTHING;
    public String job_name = PdfObject.NOTHING;
    public String job_originating_host_name = PdfObject.NOTHING;
    public String job_originating_user_name = PdfObject.NOTHING;
    public long job_printer_up_time = 0;
    public String job_printer_uri = PdfObject.NOTHING;
    public int job_priority = -1;
    public String job_sheets = PdfObject.NOTHING;
    public int job_state = 0;
    public String job_state_reasons = PdfObject.NOTHING;
    public String job_uri = PdfObject.NOTHING;
    public long time_at_completed = 0;
    public long time_at_creation = 0;
    public long time_at_processing = 0;

    public void updateAttribute(IPPAttribute a) {
        if (a.values.size() >= 1) {
            IPPValue val = (IPPValue) a.values.get(0);
            if (a.name.compareTo("job-more-info") == 0) {
                this.job_more_info = val.text;
            } else if (a.name.compareTo("job-uri") == 0) {
                this.job_uri = val.text;
            } else if (a.name.compareTo("job-printer-up-time") == 0) {
                this.job_printer_up_time = (long) val.integer_value;
            } else if (a.name.compareTo("job-originating-user-name") == 0) {
                this.job_originating_user_name = val.text;
            } else if (a.name.compareTo("document-format") == 0) {
                this.document_format = val.text;
            } else if (a.name.compareTo("job-priority") == 0) {
                this.job_priority = val.integer_value;
            } else if (a.name.compareTo("job-originating-host-name") == 0) {
                this.job_originating_host_name = val.text;
            } else if (a.name.compareTo("job-id") == 0) {
                this.job_id = val.integer_value;
            } else if (a.name.compareTo("job-state") == 0) {
                this.job_state = val.integer_value;
            } else if (a.name.compareTo("job-media-sheets-completed") == 0) {
                this.job_media_sheets_completed = val.integer_value;
            } else if (a.name.compareTo("job-printer-uri") == 0) {
                this.job_printer_uri = val.text;
            } else if (a.name.compareTo("job-name") == 0) {
                this.job_name = val.text;
            } else if (a.name.compareTo("job-k-octets") == 0) {
                this.job_k_octets = val.integer_value;
            } else if (a.name.compareTo("time-at-creation") == 0) {
                this.time_at_creation = (long) val.integer_value;
            } else if (a.name.compareTo("time-at-processing") == 0) {
                this.time_at_processing = (long) val.integer_value;
            } else if (a.name.compareTo("time-at-completed") == 0) {
                this.time_at_completed = (long) val.integer_value;
            } else if (a.name.compareTo("job-hold-until") == 0) {
                this.job_hold_until = val.text;
            } else if (a.name.compareTo("job-sheets") == 0) {
                this.job_sheets = val.text;
            } else if (a.name.compareTo("job-state-reasons") == 0) {
                this.job_state_reasons = val.text;
            }
        }
    }

    public String jobStatusText() {
        switch (this.job_state) {
            case 3:
                return "Pending";
            case 4:
                return "Held";
            case 5:
                return "Processing";
            case 6:
                return "Stopped";
            case 7:
                return "Cancelled";
            case 8:
                return "Aborted";
            case 9:
                return "Completed";
            default:
                return "Unknown";
        }
    }

    public int jobStatus() {
        return this.job_state;
    }
}
