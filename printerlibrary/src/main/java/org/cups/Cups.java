package org.cups;

import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.codec.TIFFConstants;
import com.itextpdf.text.xml.xmp.XmpWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import net.jsecurity.printbot.model.GUIConstants;

public class Cups {
    static final int FILEREQ_STATE_CREATE_HTTP = 0;
    static final int FILEREQ_STATE_DONE = 7;
    static final int FILEREQ_STATE_FINISH_IPP_ATTRS = 4;
    static final int FILEREQ_STATE_READ_RESPONSE = 6;
    static final int FILEREQ_STATE_WRITE_FILE_DATA = 5;
    static final int FILEREQ_STATE_WRITE_HTTP_HEADER = 1;
    static final int FILEREQ_STATE_WRITE_IPP_ATTRS = 3;
    static final int FILEREQ_STATE_WRITE_IPP_HEADER = 2;
    static final int REQ_STATE_CREATE_HTTP = 0;
    static final int REQ_STATE_DONE = 6;
    static final int REQ_STATE_FINISH_IPP_ATTRS = 4;
    static final int REQ_STATE_READ_RESPONSE = 5;
    static final int REQ_STATE_WRITE_HTTP_HEADER = 1;
    static final int REQ_STATE_WRITE_IPP_ATTRS = 3;
    static final int REQ_STATE_WRITE_IPP_HEADER = 2;
    static final String[] filereq_state_names = {"Create HTTP", "Write Http Header", "Write IPP Header", "Write IPP Attrs", "Finish IPP Attrs", "Write File Data", "Read Response", "Done"};
    static final String[] req_state_names = {"Create HTTP", "Write Http Header", "Write IPP Header", "Write IPP Attrs", "Finish IPP Attrs", "Read Response", "Done"};
    String address;
    String dest;
    boolean encrypt;
    String error_text;
    IPPHttp http;
    String instance;
    IPP ipp;
    int last_error;
    String passwd;
    String path;
    int port;
    String protocol;
    String site;
    String user;

    public Cups() {
        this.http = null;
        this.ipp = null;
        this.protocol = GUIConstants.PROTOCOL_IPP;
        this.address = "localhost";
        this.port = 631;
        this.path = "/";
        this.site = "http://localhost:631/";
        this.dest = PdfObject.NOTHING;
        this.instance = PdfObject.NOTHING;
        this.user = PdfObject.NOTHING;
        this.passwd = PdfObject.NOTHING;
        this.encrypt = false;
    }

    public Cups(URL p_url) {
        this.http = null;
        this.ipp = null;
        this.protocol = String.valueOf(p_url.getProtocol()) + "://";
        this.address = p_url.getHost();
        this.port = p_url.getPort();
        this.path = p_url.getPath();
        this.site = String.valueOf(this.protocol) + this.address;
        if (this.port > 0) {
            this.site = String.valueOf(this.site) + ":" + this.port;
        }
        if (this.path.length() > 0) {
            this.site = String.valueOf(this.site) + this.path;
        }
        this.dest = PdfObject.NOTHING;
        this.instance = PdfObject.NOTHING;
        this.user = PdfObject.NOTHING;
        this.passwd = PdfObject.NOTHING;
        this.encrypt = false;
    }

    public void setProtocol(String p_protocol) {
        this.protocol = p_protocol;
        this.site = String.valueOf(this.protocol) + "://" + this.address + ":" + this.port + this.path;
    }

    public void setServer(String p_server) {
        this.address = p_server;
        this.site = String.valueOf(this.protocol) + "://" + this.address + ":" + this.port + this.path;
    }

    public void setPort(int p_port) {
        this.port = p_port;
        this.site = String.valueOf(this.protocol) + "://" + this.address + ":" + this.port + this.path;
    }

    public void setUser(String p_user) {
        this.user = p_user;
    }

    public void setPasswd(String p_passwd) {
        this.passwd = p_passwd;
    }

    public void setDest(String p_dest) {
        this.dest = p_dest;
    }

    public void setInstance(String p_instance) {
        this.instance = p_instance;
    }

    public void setEncrypt(boolean p_encrypt) {
        this.encrypt = p_encrypt;
    }

    public boolean getEncrypt() {
        return this.encrypt;
    }

    public void setPath(String p_path) {
        this.path = p_path;
        this.site = this.protocol + "://" + this.address + ":" + this.port + this.path;
    }

    public boolean doRequest(String from) throws IOException {
        return doRequest();
    }

    public boolean doRequest() throws IOException {
        int state = 0;
        int errors = 0;
        while (true) {
            switch (state) {
                case 0:
                    String url_str = this.site + this.dest;
                    try {
                        if (this.user.length() <= 0 || this.passwd.length() <= 0) {
                            this.http = new IPPHttp(url_str);
                        } else {
                            this.http = new IPPHttp(url_str, PdfObject.NOTHING, this.user, this.passwd);
                        }
                        state++;
                    } catch (IOException e) {
                        throw e;
                    }
                    break;
                case 1:
                    switch (this.http.writeHeader(this.http.path, this.ipp.sizeInBytes())) {
                        case -1:
                        case IPPHttp.HTTP_BAD_REQUEST /*{ENCODED_INT: 400}*/:
                        case IPPHttp.HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                        case IPPHttp.HTTP_PAYMENT_REQUIRED /*{ENCODED_INT: 402}*/:
                        case IPPHttp.HTTP_FORBIDDEN /*{ENCODED_INT: 403}*/:
                        case IPPHttp.HTTP_NOT_FOUND /*{ENCODED_INT: 404}*/:
                        case IPPHttp.HTTP_METHOD_NOT_ALLOWED /*{ENCODED_INT: 405}*/:
                        case IPPHttp.HTTP_UPGRADE_REQUIRED /*{ENCODED_INT: 426}*/:
                            errors++;
                            if (errors >= 5) {
                                return false;
                            }
                            if (!this.http.reConnect()) {
                                System.out.println("Could not reConnect(0)!");
                                return false;
                            }
                            continue;
                        default:
                            state++;
                            continue;
                    }
                case 2:
                    this.http.write(new byte[]{1, 1, (byte) ((this.ipp.request.operation_id & 65280) >> 8), (byte) (this.ipp.request.operation_id & 255), (byte) ((this.ipp.request.request_id & -16777216) >> 24), (byte) ((this.ipp.request.request_id & 16711680) >> 16), (byte) ((this.ipp.request.request_id & 65280) >> 8), (byte) (this.ipp.request.request_id & TIFFConstants.TIFFTAG_OSUBFILETYPE)});
                    if (this.http.checkForResponse() >= 400) {
                        errors++;
                        if (errors >= 5) {
                            return false;
                        }
                        if (this.http.reConnect()) {
                            state = 1;
                        } else {
                            System.out.println("Could not reConnect(1)\n");
                            return false;
                        }
                    } else {
                        state++;
                    }
                case 3:
                    int last_group = -1;
                    boolean auth_error = false;
                    for (int i = 0; i < this.ipp.attrs.size() && !auth_error; i++) {
                        IPPAttribute attr = (IPPAttribute) this.ipp.attrs.get(i);
                        byte[] bytes = attr.getBytes(attr.sizeInBytes(last_group), last_group);
                        last_group = attr.group_tag;
                        this.http.write(bytes);
                        if (this.http.checkForResponse() >= 400) {
                            errors++;
                            if (errors >= 5) {
                                return false;
                            }
                            if (!this.http.reConnect()) {
                                System.out.println("Could not reConnect(2)");
                                return false;
                            }
                            state = 1;
                            auth_error = true;
                        }
                    }
                    if (!auth_error) {
                        state++;
                    }
                    break;
                case 4:
                    this.http.write(new byte[]{3});
                    if (this.http.checkForResponse() >= 400) {
                        errors++;
                        if (errors >= 5) {
                            return false;
                        }
                        if (!this.http.reConnect()) {
                            System.out.println("Could not reConnect(3)");
                            return false;
                        }
                        state = 1;
                    } else {
                        state++;
                    }
                case 5:
                    int read_length = this.http.read_header();
                    switch (this.http.status) {
                        case 200:
                            break;
                        case IPPHttp.HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                            this.http.reConnect();
                            state = 1;
                            errors = 0;
                            break;
                        default:
                            errors++;
                            if (errors >= 5) {
                                return false;
                            }
                            if (this.http.reConnect()) {
                                state = 1;
                                break;
                            } else {
                                System.out.println("Could not reConnect(4)");
                                return false;
                            }
                    }
                    if (read_length > 0 && state == 5) {
                        this.http.read_buffer = this.http.read(read_length);
                        this.ipp = this.http.processResponse();
                        state++;
                    }
                case 6:
                    this.http.conn.close();
                    this.http = null;
                    return true;
            }
        }
    }

    public boolean doRequest(File file) throws IOException {
        int count;
        int state = 0;
        int errors = 0;
        FileInputStream fis = null;
        while (true) {
            switch (state) {
                case 0:
                    String url_str = String.valueOf(this.site) + this.dest;
                    try {
                        if (this.user.length() <= 0 || this.passwd.length() <= 0) {
                            this.http = new IPPHttp(url_str);
                        } else {
                            this.http = new IPPHttp(url_str, PdfObject.NOTHING, this.user, this.passwd);
                        }
                        state++;
                    } catch (IOException e) {
                        throw e;
                    }
                    break;
                case 1:
                    if (fis != null) {
                        fis.close();
                    }
                    try {
                        fis = new FileInputStream(file);
                        switch (this.http.writeHeader(this.http.path, this.ipp.sizeInBytes() + ((int) file.length()))) {
                            case -1:
                            case IPPHttp.HTTP_BAD_REQUEST /*{ENCODED_INT: 400}*/:
                            case IPPHttp.HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                            case IPPHttp.HTTP_PAYMENT_REQUIRED /*{ENCODED_INT: 402}*/:
                            case IPPHttp.HTTP_FORBIDDEN /*{ENCODED_INT: 403}*/:
                            case IPPHttp.HTTP_NOT_FOUND /*{ENCODED_INT: 404}*/:
                            case IPPHttp.HTTP_METHOD_NOT_ALLOWED /*{ENCODED_INT: 405}*/:
                            case IPPHttp.HTTP_UPGRADE_REQUIRED /*{ENCODED_INT: 426}*/:
                                errors++;
                                if (errors >= 5) {
                                    return false;
                                }
                                this.http.reConnect();
                                continue;
                            default:
                                state++;
                                continue;
                        }
                    } catch (IOException e2) {
                        this.last_error = -1;
                        this.error_text = "Error opening file input stream.";
                        throw e2;
                    }
                case 2:
                    this.http.write(new byte[]{1, 1, (byte) ((this.ipp.request.operation_id & 65280) >> 8), (byte) (this.ipp.request.operation_id & 255), (byte) ((this.ipp.request.request_id & -16777216) >> 24), (byte) ((this.ipp.request.request_id & 16711680) >> 16), (byte) ((this.ipp.request.request_id & 65280) >> 8), (byte) (this.ipp.request.request_id & TIFFConstants.TIFFTAG_OSUBFILETYPE)});
                    if (this.http.checkForResponse() >= 400) {
                        errors++;
                        if (errors >= 5) {
                            return false;
                        }
                        this.http.reConnect();
                        state = 1;
                    } else {
                        state++;
                    }
                case 3:
                    int last_group = -1;
                    boolean auth_error = false;
                    for (int i = 0; i < this.ipp.attrs.size() && !auth_error; i++) {
                        IPPAttribute attr = (IPPAttribute) this.ipp.attrs.get(i);
                        byte[] bytes = attr.getBytes(attr.sizeInBytes(last_group), last_group);
                        last_group = attr.group_tag;
                        this.http.write(bytes);
                        if (this.http.checkForResponse() >= 400) {
                            errors++;
                            if (errors >= 5) {
                                return false;
                            }
                            this.http.reConnect();
                            state = 1;
                            auth_error = true;
                        }
                    }
                    if (!auth_error) {
                        state++;
                    }
                    break;
                case 4:
                    this.http.write(new byte[]{3});
                    if (this.http.checkForResponse() >= 400) {
                        errors++;
                        if (errors >= 5) {
                            return false;
                        }
                        this.http.reConnect();
                        state = 1;
                    } else {
                        state++;
                    }
                case 5:
                    byte[] b = new byte[1024];
                    while (state == 5 && (count = fis.read(b)) != -1) {
                        if (this.http.checkForResponse() >= 400) {
                            errors++;
                            if (errors >= 5) {
                                return false;
                            }
                            this.http.reConnect();
                            state = 1;
                        } else if (count > 0) {
                            this.http.write(b, count);
                        }
                    }
                    if (state == 5) {
                        fis.close();
                        fis = null;
                        state++;
                    }
                    break;
                case 6:
                    int read_length = this.http.read_header();
                    switch (this.http.status) {
                        case 200:
                            break;
                        case IPPHttp.HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                            this.http.reConnect();
                            state = 1;
                            errors = 0;
                            break;
                        default:
                            errors++;
                            if (errors < 5) {
                                this.http.reConnect();
                                state = 1;
                                break;
                            } else {
                                return false;
                            }
                    }
                    if (read_length > 0 && state == 6) {
                        this.http.read_buffer = this.http.read(read_length);
                        this.ipp = this.http.processResponse();
                        state++;
                    }
                case 7:
                    this.http.conn.close();
                    this.http = null;
                    return true;
            }
        }
    }

    public CupsJob[] cupsGetJobs(boolean showMyJobs, boolean showCompleted) throws IOException {
        String[] strArr = {"job-id", "job-priority", "job-k-octets", "job-state", "time-at-completed", "time-at-creation", "time-at-processing", "job-printer-uri", "document-format", "job-name", "job-originating-user-name"};
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 10);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        IPPAttribute a3 = new IPPAttribute(1, 69, "printer-uri");
        if (this.site != null) {
            a3.addString(PdfObject.NOTHING, this.site);
        } else {
            a3.addString(PdfObject.NOTHING, "ipp://localhost/jobs");
        }
        this.ipp.addAttribute(a3);
        IPPAttribute a4 = new IPPAttribute(1, 66, "requesting-user-name");
        a4.addString(PdfObject.NOTHING, "root");
        this.ipp.addAttribute(a4);
        if (showMyJobs) {
            IPPAttribute a5 = new IPPAttribute(1, 34, "my-jobs");
            a5.addBoolean(true);
            this.ipp.addAttribute(a5);
        }
        if (showCompleted) {
            IPPAttribute a6 = new IPPAttribute(1, 68, "which-jobs");
            a6.addString(PdfObject.NOTHING, "completed");
            this.ipp.addAttribute(a6);
        }
        if (!doRequest("cupsGetJobs")) {
            return null;
        }
        int i = 0;
        int group_tag = -1;
        while (i < this.ipp.attrs.size() && group_tag != 2) {
            group_tag = ((IPPAttribute) this.ipp.attrs.get(i)).group_tag;
            if (group_tag != 2) {
                i++;
            }
        }
        int num_jobs = 0;
        while (i < this.ipp.attrs.size() && 2 == 2) {
            int i2 = i + 1;
            IPPAttribute a7 = (IPPAttribute) this.ipp.attrs.get(i);
            if (a7 == null || a7.name.compareTo("job-id") != 0) {
                i = i2;
            } else {
                num_jobs++;
                i = i2;
            }
        }
        if (num_jobs < 1) {
            return null;
        }
        CupsJob[] jobs = new CupsJob[num_jobs];
        for (int n = 0; n < num_jobs; n++) {
            jobs[n] = new CupsJob();
        }
        int group_tag2 = -1;
        int i3 = 0;
        while (i3 < this.ipp.attrs.size() && group_tag2 != 2) {
            group_tag2 = ((IPPAttribute) this.ipp.attrs.get(i3)).group_tag;
            if (group_tag2 != 2) {
                i3++;
            }
        }
        int n2 = 0;
        while (i3 < this.ipp.attrs.size()) {
            IPPAttribute a8 = (IPPAttribute) this.ipp.attrs.get(i3);
            if (a8.group_tag == 0) {
                n2++;
            } else {
                try {
                    jobs[n2].updateAttribute(a8);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return jobs;
                }
            }
            i3++;
        }
        return jobs;
    }

    public String[] cupsGetPrinters() throws IOException {
        IPPValue val;
        String[] printers = null;
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 16386);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        if (doRequest("cupsGetPrinters")) {
            int num_printers = 0;
            for (int i = 0; i < this.ipp.attrs.size(); i++) {
                IPPAttribute a3 = (IPPAttribute) this.ipp.attrs.get(i);
                if (a3.name.compareTo("printer-name") == 0 && a3.value_tag == 66) {
                    num_printers++;
                }
            }
            if (num_printers >= 1) {
                printers = new String[num_printers];
                int n = 0;
                for (int i2 = 0; i2 < this.ipp.attrs.size(); i2++) {
                    IPPAttribute a4 = (IPPAttribute) this.ipp.attrs.get(i2);
                    if (a4.group_tag >= 2 && a4.name.compareTo("printer-name") == 0 && a4.value_tag == 66 && (val = (IPPValue) a4.values.get(0)) != null) {
                        printers[n] = val.text;
                        n++;
                    }
                }
            }
        }
        return printers;
    }

    public String cupsGetDefault() throws IOException {
        IPPValue val;
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 16385);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        if (!doRequest("cupsGetDefault") || this.ipp == null || this.ipp.attrs == null) {
            return null;
        }
        for (int i = 0; i < this.ipp.attrs.size(); i++) {
            IPPAttribute a3 = (IPPAttribute) this.ipp.attrs.get(i);
            if (a3.name.compareTo("printer-name") == 0 && a3.value_tag == 66 && (val = (IPPValue) a3.values.get(0)) != null) {
                return val.text;
            }
        }
        return null;
    }

    public List<IPPAttribute> cupsGetPrinterAttributes() throws IOException {
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 11);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        IPPAttribute a3 = new IPPAttribute(1, 69, "printer-uri");
        a3.addString(PdfObject.NOTHING, String.valueOf(this.site) + this.dest);
        this.ipp.addAttribute(a3);
        if (doRequest("cupsGetPrinterAttributes")) {
            return this.ipp.attrs;
        }
        return null;
    }

    public CupsJob cupsPrintFile(String p_filename, IPPAttribute[] p_attrs) throws IOException {
        CupsJob job = null;
        File file = new File(p_filename);
        if (!file.exists()) {
            this.last_error = -1;
            this.error_text = "File does not exist.";
        } else if (!file.canRead()) {
            this.last_error = -1;
            this.error_text = "File cannot be read.";
        } else {
            this.ipp = new IPP();
            this.ipp.request = new IPPRequest(1, (short) 2);
            IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
            a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
            this.ipp.addAttribute(a);
            IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
            a2.addString(PdfObject.NOTHING, "en");
            this.ipp.addAttribute(a2);
            IPPAttribute a3 = new IPPAttribute(1, 69, "printer-uri");
            a3.addString(PdfObject.NOTHING, String.valueOf(this.site) + this.dest);
            this.ipp.addAttribute(a3);
            IPPAttribute a4 = new IPPAttribute(1, 66, "job-name");
            a4.addString(PdfObject.NOTHING, file.getName());
            this.ipp.addAttribute(a4);
            if (p_attrs != null) {
                for (IPPAttribute a5 : p_attrs) {
                    this.ipp.addAttribute(a5);
                }
            }
            if (doRequest(file)) {
                job = new CupsJob();
                for (int i = 0; i < this.ipp.attrs.size(); i++) {
                    job.updateAttribute((IPPAttribute) this.ipp.attrs.get(i));
                }
            }
        }
        return job;
    }

    public int cupsCancelJob(String printer_name, int p_job_id, String p_user_name) throws IOException {
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 8);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        IPPAttribute a3 = new IPPAttribute(1, 69, "printer-uri");
        a3.addString(PdfObject.NOTHING, String.valueOf(this.site) + this.dest);
        this.ipp.addAttribute(a3);
        IPPAttribute a4 = new IPPAttribute(1, 33, "job-id");
        a4.addInteger(p_job_id);
        this.ipp.addAttribute(a4);
        IPPAttribute a5 = new IPPAttribute(1, 66, "requesting-user-name");
        a5.addString(PdfObject.NOTHING, p_user_name);
        this.ipp.addAttribute(a5);
        if (doRequest("cupsCancelJob")) {
            for (int i = 0; i < this.ipp.attrs.size(); i++) {
                ((IPPAttribute) this.ipp.attrs.get(i)).dump_values();
            }
        }
        return 0;
    }

    public List<IPPAttribute> cupsGetPrinterStatus() throws IOException {
        this.ipp = new IPP();
        this.ipp.request = new IPPRequest(1, (short) 11);
        IPPAttribute a = new IPPAttribute(1, 71, "attributes-charset");
        a.addString(PdfObject.NOTHING, XmpWriter.UTF8);
        this.ipp.addAttribute(a);
        IPPAttribute a2 = new IPPAttribute(1, 72, "attributes-natural-language");
        a2.addString(PdfObject.NOTHING, "en");
        this.ipp.addAttribute(a2);
        IPPAttribute a3 = new IPPAttribute(1, 69, "printer-uri");
        a3.addString(PdfObject.NOTHING, String.valueOf(this.site) + this.dest);
        this.ipp.addAttribute(a3);
        if (doRequest("cupsGetPrinterStatus")) {
            return this.ipp.attrs;
        }
        return null;
    }
}
