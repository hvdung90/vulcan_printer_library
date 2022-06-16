package org.cups;

import com.itextpdf.text.pdf.BidiOrder;
import com.itextpdf.text.pdf.PdfObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import org.apache.james.mime4j.field.FieldName;
import org.apache.james.mime4j.util.CharsetUtil;

public class IPPHttp {
    public static final int HTTP_0_9 = 9;
    public static final int HTTP_1_0 = 100;
    public static final int HTTP_1_1 = 101;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_AUTH_BASIC = 1;
    public static final int HTTP_AUTH_MD5 = 2;
    public static final int HTTP_AUTH_MD5_INT = 4;
    public static final int HTTP_AUTH_MD5_SESS = 3;
    public static final int HTTP_AUTH_MD5_SESS_INT = 5;
    public static final int HTTP_AUTH_NONE = 0;
    public static final int HTTP_BAD_GATEWAY = 502;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_CLOSE = 12;
    public static final int HTTP_CONFLICT = 409;
    public static final int HTTP_CONTINUE = 100;
    public static final int HTTP_CREATED = 201;
    public static final int HTTP_DELETE = 10;
    public static final int HTTP_ENCODE_CHUNKED = 1;
    public static final int HTTP_ENCODE_LENGTH = 0;
    public static final int HTTP_ENCRYPT_ALWAYS = 3;
    public static final int HTTP_ENCRYPT_IF_REQUESTED = 0;
    public static final int HTTP_ENCRYPT_NEVER = 1;
    public static final int HTTP_ENCRYPT_REQUIRED = 2;
    public static final int HTTP_ERROR = -1;
    public static final int HTTP_FIELD_ACCEPT_LANGUAGE = 0;
    public static final int HTTP_FIELD_ACCEPT_RANGES = 1;
    public static final int HTTP_FIELD_AUTHORIZATION = 2;
    public static final int HTTP_FIELD_CONNECTION = 3;
    public static final int HTTP_FIELD_CONTENT_ENCODING = 4;
    public static final int HTTP_FIELD_CONTENT_LANGUAGE = 5;
    public static final int HTTP_FIELD_CONTENT_LENGTH = 6;
    public static final int HTTP_FIELD_CONTENT_LOCATION = 7;
    public static final int HTTP_FIELD_CONTENT_MD5 = 8;
    public static final int HTTP_FIELD_CONTENT_RANGE = 9;
    public static final int HTTP_FIELD_CONTENT_TYPE = 10;
    public static final int HTTP_FIELD_CONTENT_VERSION = 11;
    public static final int HTTP_FIELD_DATE = 12;
    public static final int HTTP_FIELD_HOST = 13;
    public static final int HTTP_FIELD_IF_MODIFIED_SINCE = 14;
    public static final int HTTP_FIELD_IF_UNMODIFIED_SINCE = 15;
    public static final int HTTP_FIELD_KEEP_ALIVE = 16;
    public static final int HTTP_FIELD_LAST_MODIFIED = 17;
    public static final int HTTP_FIELD_LINK = 18;
    public static final int HTTP_FIELD_LOCATION = 19;
    public static final int HTTP_FIELD_MAX = 27;
    public static final int HTTP_FIELD_RANGE = 20;
    public static final int HTTP_FIELD_REFERER = 21;
    public static final int HTTP_FIELD_RETRY_AFTER = 22;
    public static final int HTTP_FIELD_TRANSFER_ENCODING = 23;
    public static final int HTTP_FIELD_UNKNOWN = -1;
    public static final int HTTP_FIELD_UPGRADE = 24;
    public static final int HTTP_FIELD_USER_AGENT = 25;
    public static final int HTTP_FIELD_WWW_AUTHENTICATE = 26;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_GATEWAY_TIMEOUT = 504;
    public static final int HTTP_GET = 2;
    public static final int HTTP_GET_SEND = 3;
    public static final int HTTP_GONE = 410;
    public static final int HTTP_HEAD = 4;
    public static final int HTTP_KEEPALIVE_OFF = 0;
    public static final int HTTP_KEEPALIVE_ON = 1;
    public static final int HTTP_LENGTH_REQUIRED = 411;
    public static final int HTTP_METHOD_NOT_ALLOWED = 405;
    public static final int HTTP_MOVED_PERMANENTLY = 301;
    public static final int HTTP_MOVED_TEMPORARILY = 302;
    public static final int HTTP_MULTIPLE_CHOICES = 300;
    public static final int HTTP_NOT_ACCEPTABLE = 406;
    public static final int HTTP_NOT_AUTHORITATIVE = 203;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_NOT_IMPLEMENTED = 501;
    public static final int HTTP_NOT_MODIFIED = 304;
    public static final int HTTP_NOT_SUPPORTED = 505;
    public static final int HTTP_NO_CONTENT = 204;
    public static final int HTTP_OK = 200;
    public static final int HTTP_OPTIONS = 1;
    public static final int HTTP_PARTIAL_CONTENT = 206;
    public static final int HTTP_PAYMENT_REQUIRED = 402;
    public static final int HTTP_POST = 5;
    public static final int HTTP_POST_RECV = 6;
    public static final int HTTP_POST_SEND = 7;
    public static final int HTTP_PRECONDITION = 412;
    public static final int HTTP_PROXY_AUTHENTICATION = 407;
    public static final int HTTP_PUT = 8;
    public static final int HTTP_PUT_RECV = 9;
    public static final int HTTP_REQUEST_TIMEOUT = 408;
    public static final int HTTP_REQUEST_TOO_LARGE = 413;
    public static final int HTTP_RESET_CONTENT = 205;
    public static final int HTTP_SEE_OTHER = 303;
    public static final int HTTP_SERVER_ERROR = 500;
    public static final int HTTP_SERVICE_UNAVAILABLE = 503;
    public static final int HTTP_STATUS = 13;
    public static final int HTTP_SWITCHING_PROTOCOLS = 101;
    public static final int HTTP_TRACE = 11;
    public static final int HTTP_UNAUTHORIZED = 401;
    public static final int HTTP_UNSUPPORTED_MEDIATYPE = 415;
    public static final int HTTP_UPGRADE_REQUIRED = 426;
    public static final int HTTP_URI_TOO_LONG = 414;
    public static final int HTTP_USE_PROXY = 305;
    public static final int HTTP_WAITING = 0;
    public static final int SOCKET_TIMEOUT = 1500;
    public static final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    public static final String[] http_fields = {"Accept-Language", "Accept-Ranges", "Authorization", "Connection", "Content-Encoding", "Content-Language", "Content-Length", "Content-Location", "Content-MD5", "Content-Range", "Content-Type", "Content-Version", FieldName.DATE, "Host", "If-Modified-Since", "If-Unmodified-since", "Keep-Alive", "Last-Modified", "Link", "Location", "Range", "Referer", "Retry-After", "Transfer-Encoding", "Upgrade", "User-Agent", "WWW-Authenticate"};
    public static final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public int activity;
    public String auth_type;
    public BufferedReader br;
    public Socket conn;
    public boolean connected;
    private boolean encrypted;
    public int error;
    public String hostname;
    public int http_content_length;
    public String http_request;
    public BufferedInputStream is;
    public String method;
    public String nonce;
    public String opaque;
    public BufferedOutputStream os;
    public String passwd;
    public String path;
    public int port;
    public byte[] read_buffer;
    private int read_buffer_head;
    private int read_buffer_tail;
    public String read_header_charset;
    public String read_header_content_language;
    public int read_header_content_length;
    public String read_header_content_type;
    public String read_header_date;
    public String read_header_server;
    public String realm;
    public String resource;
    public int status;
    public String status_text;
    private URL url;
    public String user;
    public String version;
    private char[] write_buffer;
    private int write_buffer_head;
    private int write_buffer_tail;
    public int write_content_length;

    public IPPHttp(String request_url) throws IOException, UnknownHostException {
        this.encrypted = false;
        this.status = 0;
        this.status_text = PdfObject.NOTHING;
        this.version = "1.1";
        this.connected = false;
        this.user = PdfObject.NOTHING;
        this.passwd = PdfObject.NOTHING;
        this.auth_type = PdfObject.NOTHING;
        this.realm = PdfObject.NOTHING;
        this.nonce = PdfObject.NOTHING;
        this.resource = PdfObject.NOTHING;
        this.method = PdfObject.NOTHING;
        try {
            this.url = new URL(request_url);
            this.hostname = this.url.getHost();
            this.port = this.url.getPort();
            this.path = this.url.getPath();
            this.conn = new Socket(this.hostname, this.port);
            this.conn.setSoTimeout(SOCKET_TIMEOUT);
            this.is = new BufferedInputStream(new DataInputStream(this.conn.getInputStream()));
            this.os = new BufferedOutputStream(new DataOutputStream(this.conn.getOutputStream()));
            this.connected = true;
        } catch (UnknownHostException unknownhostexception) {
            throw unknownhostexception;
        } catch (IOException ioexception) {
            throw ioexception;
        }
    }

    public IPPHttp(String request_url, String p_auth_type, String p_user, String p_passwd) throws IOException, UnknownHostException {
        this.encrypted = false;
        this.status = 0;
        this.status_text = PdfObject.NOTHING;
        this.version = "1.1";
        this.connected = false;
        this.user = p_user;
        this.passwd = p_passwd;
        this.auth_type = p_auth_type;
        this.realm = PdfObject.NOTHING;
        this.nonce = PdfObject.NOTHING;
        this.resource = PdfObject.NOTHING;
        this.method = PdfObject.NOTHING;
        try {
            this.url = new URL(request_url);
            this.hostname = this.url.getHost();
            this.port = this.url.getPort();
            this.path = this.url.getPath();
            this.conn = new Socket(this.hostname, this.port);
            this.conn.setSoTimeout(SOCKET_TIMEOUT);
            this.is = new BufferedInputStream(new DataInputStream(this.conn.getInputStream()));
            this.os = new BufferedOutputStream(new DataOutputStream(this.conn.getOutputStream()));
            this.connected = true;
        } catch (UnknownHostException unknownhostexception) {
            throw unknownhostexception;
        } catch (IOException ioexception) {
            throw ioexception;
        }
    }

    public boolean reConnect() throws IOException {
        this.connected = false;
        this.status = 0;
        this.status_text = PdfObject.NOTHING;
        try {
            this.conn = new Socket(this.hostname, this.port);
            this.conn.setSoTimeout(SOCKET_TIMEOUT);
            this.is = new BufferedInputStream(new DataInputStream(this.conn.getInputStream()));
            this.os = new BufferedOutputStream(new DataOutputStream(this.conn.getOutputStream()));
            this.connected = true;
            return this.connected;
        } catch (IOException ioexception) {
            this.connected = false;
            throw ioexception;
        }
    }

    public void setUser(String p_user) {
        this.user = p_user;
    }

    public void setPassword(String p_passwd) {
        this.passwd = p_passwd;
    }

    public int writeHeader(String request, int content_length) throws IOException {
        this.http_request = request;
        this.http_content_length = content_length;
        try {
            String s1 = "POST " + request + " HTTP/1.1\r\n";
            this.os.write(s1.getBytes(), 0, s1.length());
            this.os.write("Content-type: application/ipp\r\n".getBytes(), 0, "Content-type: application/ipp\r\n".length());
            String s12 = "Host: " + this.hostname + CharsetUtil.CRLF;
            this.os.write(s12.getBytes(), 0, s12.length());
            if (this.auth_type.compareTo("basic") == 0) {
                String s13 = "Authorization: Basic " + Base64Coder.encodeString(String.valueOf(this.user) + ":" + this.passwd) + CharsetUtil.CRLF;
                this.os.write(s13.getBytes(), 0, s13.length());
            } else if (this.auth_type.compareTo("digest") == 0) {
                try {
                    String s14 = "Authorization: Digest username=\"" + this.user + "\", " + "realm=\"" + this.realm + "\", " + "nonce=\"" + this.nonce + "\", " + "response=\"" + IPPMD5.getInstance().MD5Digest(this.user, this.passwd, this.realm, "POST", this.path, this.nonce) + "\"\r\n";
                    this.os.write(s14.getBytes(), 0, s14.length());
                } catch (NoSuchAlgorithmException e) {
                    System.out.println("No such algorithm: MD5.");
                }
            }
            String s15 = "Content-length: " + content_length + "\r\n\r\n";
            this.os.write(s15.getBytes(), 0, s15.length());
            this.os.flush();
            int local_status = 0;
            try {
                if (this.is.available() > 0) {
                    StringBuffer http_version = new StringBuffer(32);
                    StringBuffer http_status = new StringBuffer(32);
                    StringBuffer http_text = new StringBuffer(256);
                    this.status = 0;
                    this.is.mark(8192);
                    while (this.is.available() > 0) {
                        String read_buffer2 = read_line();
                        if (read_buffer2.startsWith("HTTP/")) {
                            String s2 = read_buffer2.substring(5);
                            int i = 0;
                            while (i < s2.length() && s2.charAt(i) != ' ') {
                                http_version.append(s2.charAt(i));
                                i++;
                            }
                            while (i < s2.length() && s2.charAt(i) == ' ') {
                                i++;
                            }
                            while (i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                                http_status.append(s2.charAt(i));
                                i++;
                            }
                            while (i < s2.length() && s2.charAt(i) == ' ') {
                                i++;
                            }
                            while (0 < 256 && i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                                http_text.append(s2.charAt(i));
                                i++;
                            }
                            local_status = Integer.parseInt(http_status.toString(), 10);
                        }
                    }
                    this.is.reset();
                }
                switch (local_status) {
                    case HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                        read_header();
                        return local_status;
                    default:
                        return 0;
                }
            } catch (IOException ioexception) {
                this.error = -1;
                throw ioexception;
            }
        } catch (IOException ioexception2) {
            this.error = -1;
            throw ioexception2;
        }
    }

    public int checkForResponse() {
        try {
            if (this.is.available() > 0) {
                StringBuffer http_version = new StringBuffer(32);
                StringBuffer http_status = new StringBuffer(32);
                StringBuffer http_text = new StringBuffer(256);
                int local_status = 0;
                this.status = 0;
                this.is.mark(8192);
                while (this.is.available() > 0) {
                    String read_buffer2 = read_line();
                    if (read_buffer2.startsWith("HTTP/")) {
                        String s2 = read_buffer2.substring(5);
                        int i = 0;
                        while (i < s2.length() && s2.charAt(i) != ' ') {
                            http_version.append(s2.charAt(i));
                            i++;
                        }
                        while (i < s2.length() && s2.charAt(i) == ' ') {
                            i++;
                        }
                        while (i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                            http_status.append(s2.charAt(i));
                            i++;
                        }
                        while (i < s2.length() && s2.charAt(i) == ' ') {
                            i++;
                        }
                        while (0 < 256 && i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                            http_text.append(s2.charAt(i));
                            i++;
                        }
                        local_status = Integer.parseInt(http_status.toString(), 10);
                        this.status = local_status;
                    }
                }
                this.is.reset();
                switch (local_status) {
                    case HTTP_UNAUTHORIZED /*{ENCODED_INT: 401}*/:
                        read_header();
                        return local_status;
                }
            }
            return 0;
        } catch (IOException e) {
            return -1;
        }
    }

    public void write(byte[] bytes) throws IOException {
        try {
            this.os.write(bytes, 0, bytes.length);
            this.os.flush();
        } catch (IOException ioexception) {
            this.error = -1;
            throw ioexception;
        }
    }

    public void write(byte[] bytes, int length) throws IOException {
        try {
            this.os.write(bytes, 0, length);
            this.os.flush();
        } catch (IOException ioexception) {
            this.error = -1;
            throw ioexception;
        }
    }

    public int read_header() throws IOException {
        this.read_header_content_length = 0;
        while (0 == 0) {
            String read_buffer2 = read_line();
            if (read_buffer2.startsWith("HTTP/")) {
                String s2 = read_buffer2.substring(5);
                StringBuffer http_version = new StringBuffer(32);
                StringBuffer http_status = new StringBuffer(32);
                StringBuffer http_text = new StringBuffer(256);
                int i = 0;
                while (i < s2.length() && s2.charAt(i) != ' ') {
                    http_version.append(s2.charAt(i));
                    i++;
                }
                while (i < s2.length() && s2.charAt(i) == ' ') {
                    i++;
                }
                while (i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                    http_status.append(s2.charAt(i));
                    i++;
                }
                while (i < s2.length() && s2.charAt(i) == ' ') {
                    i++;
                }
                while (0 < 256 && i < s2.length() && s2.charAt(i) != '\n' && s2.charAt(i) != '\r' && s2.charAt(i) != ' ') {
                    http_text.append(s2.charAt(i));
                    i++;
                }
                this.version = http_version.toString();
                this.status = Integer.parseInt(http_status.toString(), 10);
                this.status_text = http_text.toString();
            } else if (read_buffer2.startsWith("WWW-Authenticate: Basic")) {
                read_buffer2.substring("WWW-Authenticate: Basic".length());
                this.auth_type = "basic";
            } else if (read_buffer2.startsWith("WWW-Authenticate: Digest")) {
                String s22 = read_buffer2.substring("WWW-Authenticate: Digest".length());
                this.auth_type = "digest";
                parseAuthenticate(s22);
            } else if (read_buffer2.startsWith("Content-Length:")) {
                this.read_header_content_length = Integer.parseInt(read_buffer2.substring(15).trim(), 10);
            } else if (read_buffer2.startsWith("Content-Language:")) {
                this.read_header_content_language = read_buffer2.substring(17).trim();
            } else if (read_buffer2.startsWith("Server:")) {
                this.read_header_server = read_buffer2.substring(7).trim();
            } else if (read_buffer2.startsWith("Date:")) {
                this.read_header_date = read_buffer2.substring(5).trim();
            } else if (read_buffer2.length() == 0) {
                return this.read_header_content_length;
            }
        }
//        return 0;
    }

    public String read_line() throws IOException {
        StringBuffer sb = new StringBuffer();
        int c = 0;
        while (c != -1 && c != 10) {
            try {
                c = this.is.read();
                switch (c) {
                    case -1:
                    case 10:
                    case 13:
                        break;
                    default:
                        sb.append((char) c);
                        break;
                }
            } catch (IOException e) {
                throw e;
            }
        }
        return sb.toString();
    }

    public byte[] read(int count) throws IOException {
        byte[] ac = new byte[count];
        for (int i = 0; i < count; i++) {
            int c = this.is.read();
            if (c == -1) {
                break;
            }
            ac[i] = (byte) c;
        }
        return ac;
    }

    public IPP processResponse() {
        IPP ipp = new IPP();
//        ipp.request = new IPPRequest();
//        int read_buffer_remaining = this.read_buffer.length;
//        int bufferidx = 0;
//        ipp.current = -1;
//        ipp.last = -1;
//        IPPAttribute attr = null;
//        byte[] buffer = this.read_buffer;
//        short gtag = -1;
//        ipp.state = 0;
//        while (ipp.state != 3 && read_buffer_remaining > 0) {
//            switch (ipp.state) {
//                case 0:
//                    ipp.state = (short) (ipp.state + 1);
//                    break;
//                case 2:
//                    int bufferidx2 = bufferidx;
//                    while (true) {
//                        if (read_buffer_remaining > 0) {
//                            bufferidx = bufferidx2 + 1;
//                            short vtag = (short) buffer[bufferidx2];
//                            read_buffer_remaining--;
//                            if (vtag != 3) {
//                                if (vtag >= 16) {
//                                    int n = ((((short) buffer[bufferidx]) & 65280) << 8) | (((short) buffer[bufferidx + 1]) & 255);
//                                    int bufferidx3 = bufferidx + 2;
//                                    read_buffer_remaining -= 2;
//                                    if (n != 0) {
//                                        if (attr != null) {
//                                            ipp.addAttribute(attr);
//                                        }
//                                        StringBuffer s = new StringBuffer();
//                                        int i = 0;
//                                        int bufferidx4 = bufferidx3;
//                                        while (i < n) {
//                                            s.append((char) buffer[bufferidx4]);
//                                            read_buffer_remaining--;
//                                            i++;
//                                            bufferidx4++;
//                                        }
//                                        attr = new IPPAttribute(gtag, vtag, s.toString());
//                                        bufferidx3 = bufferidx4;
//                                    } else if (attr == null) {
//                                        bufferidx2 = bufferidx3;
//                                    } else if (attr.value_tag == 48 || (attr.value_tag >= 53 && attr.value_tag <= 73)) {
//                                        if (vtag != 48) {
//                                            if (vtag >= 53) {
//                                                if (vtag > 73) {
//                                                    bufferidx2 = bufferidx3;
//                                                }
//                                            }
//                                            bufferidx2 = bufferidx3;
//                                        }
//                                    } else if (attr.value_tag != vtag) {
//                                        bufferidx2 = bufferidx3;
//                                    }
//                                    int n2 = ((((short) buffer[bufferidx3]) & 65280) << 8) | (((short) buffer[bufferidx3 + 1]) & 255);
//                                    bufferidx3 += 2;
//                                    read_buffer_remaining -= 2;
//                                    switch (vtag) {
//                                        case 33:
//                                        case 35:
//                                            read_buffer_remaining -= 4;
//                                            attr.addInteger(((buffer[bufferidx3] & -16777216) << 24) | ((buffer[bufferidx3 + 1] & 16711680) << 16) | ((buffer[bufferidx3 + 2] & 65280) << 8) | (buffer[bufferidx3 + 3] & 255));
//                                            bufferidx2 = bufferidx3 + 4;
//                                            break;
//                                        case 34:
//                                            bufferidx2 = bufferidx3 + 1;
//                                            if (buffer[bufferidx3] > 0) {
//                                                attr.addBoolean(true);
//                                            } else {
//                                                attr.addBoolean(false);
//                                            }
//                                            read_buffer_remaining--;
//                                            break;
//                                        case 36:
//                                        case IPPDefs.HOLD_NEW_JOBS:
//                                        case 38:
//                                        case 39:
//                                        case 40:
//                                        case IPPDefs.RESTART_PRINTER:
//                                        case 42:
//                                        case 43:
//                                        case IPPDefs.REPROCESS_JOB:
//                                        case IPPDefs.CANCEL_CURRENT_JOB:
//                                        case 46:
//                                        case 47:
//                                        case 52:
//                                        case 55:
//                                        case 56:
//                                        case 57:
//                                        case 58:
//                                        case 59:
//                                        case 60:
//                                        case 61:
//                                        case 62:
//                                        case 63:
//                                        case 64:
//                                        case 67:
//                                        default:
//                                            if (n2 > 0) {
//                                                read_buffer_remaining -= n2;
//                                                bufferidx2 = bufferidx3 + n2;
//                                                break;
//                                            }
//                                            bufferidx2 = bufferidx3;
//                                            break;
//                                        case 48:
//                                        case IPPDefs.TAG_TEXT:
//                                        case IPPDefs.TAG_NAME:
//                                        case IPPDefs.TAG_KEYWORD:
//                                        case IPPDefs.TAG_URI:
//                                        case IPPDefs.TAG_URISCHEME:
//                                        case IPPDefs.TAG_CHARSET:
//                                        case IPPDefs.TAG_LANGUAGE:
//                                        case IPPDefs.TAG_MIMETYPE:
//                                            StringBuffer s2 = new StringBuffer();
//                                            int i2 = 0;
//                                            bufferidx2 = bufferidx3;
//                                            while (i2 < n2) {
//                                                s2.append((char) buffer[bufferidx2]);
//                                                read_buffer_remaining--;
//                                                i2++;
//                                                bufferidx2++;
//                                            }
//                                            attr.addString(PdfObject.NOTHING, s2.toString());
//                                            break;
//                                        case 49:
//                                            char[] db = new char[11];
//                                            int i3 = 0;
//                                            while (true) {
//                                                bufferidx2 = bufferidx3;
//                                                if (i3 >= 11) {
//                                                    attr.addDate(db);
//                                                    break;
//                                                } else {
//                                                    bufferidx3 = bufferidx2 + 1;
//                                                    db[i3] = (char) buffer[bufferidx2];
//                                                    read_buffer_remaining--;
//                                                    i3++;
//                                                }
//                                            }
//                                        case 50:
//                                            if (read_buffer_remaining >= 9) {
//                                                int bufferidx5 = bufferidx3 + 4;
//                                                int bufferidx6 = bufferidx5 + 4;
//                                                bufferidx2 = bufferidx6 + 1;
//                                                read_buffer_remaining = ((read_buffer_remaining - 4) - 4) - 1;
//                                                attr.addResolution(buffer[bufferidx6], ((buffer[bufferidx3] & -16777216) << 24) | ((buffer[bufferidx3 + 1] & 16711680) << 16) | ((buffer[bufferidx3 + 2] & 65280) << 8) | (buffer[bufferidx3 + 3] & 255), ((buffer[bufferidx5] & -16777216) << 24) | ((buffer[bufferidx5 + 1] & 16711680) << 16) | ((buffer[bufferidx5 + 2] & 65280) << 8) | (buffer[bufferidx5 + 3] & 255));
//                                                break;
//                                            } else {
//                                                return null;
//                                            }
//                                        case 51:
//                                            if (read_buffer_remaining >= 8) {
//                                                int lower = ((buffer[bufferidx3] & -16777216) << 24) | ((buffer[bufferidx3 + 1] & 16711680) << 16) | ((buffer[bufferidx3 + 2] & 65280) << 8) | (buffer[bufferidx3 + 3] & 255);
//                                                int bufferidx7 = bufferidx3 + 4;
//                                                read_buffer_remaining = (read_buffer_remaining - 4) - 4;
//                                                attr.addRange((short) lower, (short) (((buffer[bufferidx7] & -16777216) << 24) | ((buffer[bufferidx7 + 1] & 16711680) << 16) | ((buffer[bufferidx7 + 2] & 65280) << 8) | (buffer[bufferidx7 + 3] & 255)));
//                                                bufferidx2 = bufferidx7 + 4;
//                                                break;
//                                            } else {
//                                                return null;
//                                            }
//                                        case 53:
//                                        case IPPDefs.TAG_NAMELANG:
//                                            int n3 = ((((short) buffer[bufferidx3]) & 65280) << 8) | (((short) buffer[bufferidx3 + 1]) & 255);
//                                            int read_buffer_remaining2 = read_buffer_remaining - 2;
//                                            StringBuffer cs = new StringBuffer();
//                                            int i4 = 0;
//                                            int bufferidx8 = bufferidx3 + 2;
//                                            while (i4 < n3) {
//                                                cs.append((char) buffer[bufferidx8]);
//                                                read_buffer_remaining2--;
//                                                i4++;
//                                                bufferidx8++;
//                                            }
//                                            int n4 = ((((short) buffer[bufferidx8]) & 65280) << 8) | (((short) buffer[bufferidx8 + 1]) & 255);
//                                            read_buffer_remaining = read_buffer_remaining2 - 2;
//                                            StringBuffer tx = new StringBuffer();
//                                            int i5 = 0;
//                                            bufferidx2 = bufferidx8 + 2;
//                                            while (i5 < n4) {
//                                                tx.append((char) buffer[bufferidx2]);
//                                                read_buffer_remaining--;
//                                                i5++;
//                                                bufferidx2++;
//                                            }
//                                            attr.addString(cs.toString(), tx.toString());
//                                            break;
//                                    }
//                                } else {
//                                    if (attr != null) {
//                                        ipp.addAttribute(attr);
//                                    }
//                                    gtag = vtag;
//                                    if (ipp.current_tag == gtag) {
//                                        ipp.addAttribute(new IPPAttribute(0, 0, PdfObject.NOTHING));
//                                        attr = null;
//                                    }
//                                    ipp.current_tag = gtag;
//                                    ipp.current = -1;
//                                    bufferidx2 = bufferidx;
//                                }
//                            } else {
//                                ipp.state = 3;
//                                if (attr != null) {
//                                    ipp.addAttribute(attr);
//                                    attr = null;
//                                }
//                            }
//                        } else {
//                            bufferidx = bufferidx2;
//                        }
//                    }
//                    if (attr != null) {
//                        ipp.addAttribute(attr);
//                        attr = null;null
//                    } else {
//                        continue;
//                    }
//                    break;
//            }
//            if (read_buffer_remaining < 8 || buffer[0] != 1) {
//                return null;
//            }
//            int bufferidx9 = bufferidx + 1;
//            ipp.request.version[0] = (char) buffer[bufferidx];
//            int bufferidx10 = bufferidx9 + 1;
//            ipp.request.version[1] = (char) buffer[bufferidx9];
//            ipp.request.op_status = ((short) (((short) buffer[bufferidx10]) << 8)) | ((short) buffer[bufferidx10 + 1]);
//            int bufferidx11 = bufferidx10 + 2;
//            ipp.status = new IPPStatus(ipp.request.op_status);
//            ipp.request.request_id = (buffer[bufferidx11] << 24) | (buffer[bufferidx11 + 1] << BidiOrder.S) | (buffer[bufferidx11 + 2] << 8) | buffer[bufferidx11 + 3];
//            bufferidx = bufferidx11 + 4;
//            read_buffer_remaining -= 8;
//            ipp.state = 2;
//            ipp.current = -1;
//            ipp.current_tag = 0;
//        }
        return ipp;
    }

    public void parseAuthenticate(String p_auth) {
        String tmp = p_auth;
        while (tmp.length() > 0) {
            while (tmp.length() > 0 && (tmp.charAt(0) == ' ' || tmp.charAt(0) == '\"')) {
                tmp = tmp.substring(1);
            }
            int i = 0;
            if (tmp.startsWith("realm=")) {
                String tmp2 = tmp.substring("realm=".length());
                int i2 = 0;
                while (i2 < tmp2.length() && (tmp2.charAt(i2) == ' ' || tmp2.charAt(i2) == '\"' || tmp2.charAt(i2) == '=')) {
                    i2++;
                }
                StringBuffer val = new StringBuffer(1024);
                while (i2 < tmp2.length() && tmp2.charAt(i2) != '\"') {
                    val.append(tmp2.charAt(i2));
                    i2++;
                }
                this.realm = val.toString();
                tmp = tmp2.substring(i2);
            } else if (tmp.startsWith("nonce=")) {
                String tmp3 = tmp.substring("nonce=".length());
                int i3 = 0;
                while (i3 < tmp3.length() && (tmp3.charAt(i3) == ' ' || tmp3.charAt(i3) == '\"' || tmp3.charAt(i3) == '=')) {
                    i3++;
                }
                StringBuffer val2 = new StringBuffer(1024);
                while (i3 < tmp3.length() && tmp3.charAt(i3) != '\"') {
                    val2.append(tmp3.charAt(i3));
                    i3++;
                }
                this.nonce = val2.toString();
                tmp = tmp3.substring(i3);
            } else if (tmp.startsWith("opaque=")) {
                String tmp4 = tmp.substring("opaque=".length());
                int i4 = 0;
                while (i4 < tmp4.length() && (tmp4.charAt(i4) == ' ' || tmp4.charAt(i4) == '\"' || tmp4.charAt(i4) == '=')) {
                    i4++;
                }
                StringBuffer val3 = new StringBuffer(1024);
                while (i4 < tmp4.length() && tmp4.charAt(i4) != '\"') {
                    val3.append(tmp4.charAt(i4));
                    i4++;
                }
                this.opaque = val3.toString();
                tmp = tmp4.substring(i4);
            } else {
                StringBuffer name = new StringBuffer(256);
                while (i < tmp.length() && (tmp.charAt(i) != ' ' || tmp.charAt(i) != '\"' || tmp.charAt(i) != '=')) {
                    name.append(tmp.charAt(i));
                    i++;
                }
                String tmp5 = tmp.substring(name.toString().length());
                int i5 = 0;
                while (i5 < tmp5.length() && (tmp5.charAt(i5) == ' ' || tmp5.charAt(i5) == '\"' || tmp5.charAt(i5) == '=')) {
                    i5++;
                }
                StringBuffer val4 = new StringBuffer(1024);
                while (i5 < tmp5.length() && tmp5.charAt(i5) != '\"') {
                    val4.append(tmp5.charAt(i5));
                    i5++;
                }
                tmp = tmp5.substring(i5);
            }
        }
    }
}
