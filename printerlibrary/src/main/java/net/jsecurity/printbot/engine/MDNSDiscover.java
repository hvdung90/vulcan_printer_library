package net.jsecurity.printbot.engine;

import com.itextpdf.text.pdf.codec.TIFFConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class MDNSDiscover {
    static final short CLASS_FLAG_MULTICAST = 0;
    static final short CLASS_FLAG_UNICAST = Short.MIN_VALUE;
    private static final boolean DEBUG = false;
    private static final String MULTICAST_GROUP_ADDRESS = "224.0.0.251";
    private static final int PORT = 5353;
    static final short QCLASS_INTERNET = 1;
    private static final short QTYPE_A = 1;
    static final short QTYPE_PTR = 12;
    static final short QTYPE_SRV = 33;
    static final short QTYPE_TXT = 16;

    public static class A extends Record {
        public String ipaddr;
    }

    public interface Callback {
        void onResult(Result result);
    }

    public static class Record {
        public String fqdn;
        public int ttl;
    }

    public static class Result {
        public A a;
        public InetAddress source;
        public SRV srv;
        public TXT txt;
    }

    public static class SRV extends Record {
        public int port;
        public int priority;
        public String target;
        public int weight;
    }

    public static class TXT extends Record {
        public Map<String, String> dict;
    }

    private static byte[] discoverPacket(String serviceType) throws IOException {
        return queryPacket(serviceType, -32767, 12);
    }

    public static void discover(String serviceType, Callback callback, int timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP_ADDRESS);
        MulticastSocket sock = new MulticastSocket();
        try {
            byte[] data = discoverPacket(serviceType);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, (int) PORT);
            sock.setTimeToLive(255);
            sock.send(packet);
            byte[] buf = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(buf, buf.length);
            long endTime = 0;
            if (timeout != 0) {
                endTime = System.currentTimeMillis() + ((long) timeout);
            }
            while (true) {
                if (timeout != 0) {
                    int remaining = (int) (endTime - System.currentTimeMillis());
                    if (remaining <= 0) {
                        break;
                    }
                    sock.setSoTimeout(remaining);
                }
                try {
                    sock.receive(packet2);
                    Result result = decode(packet2.getData(), packet2.getLength());
                    if (callback != null) {
                        callback.onResult(result);
                    }
                } catch (SocketTimeoutException e) {
                }
            }
        } finally {
            sock.close();
        }
    }

    static byte[] queryPacket(String serviceName, int qclass, int... qtypes) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(0);
        dos.writeShort(qtypes.length);
        dos.writeShort(0);
        dos.writeShort(0);
        dos.writeShort(0);
        int fqdnPtr = -1;
        for (int qtype : qtypes) {
            if (fqdnPtr == -1) {
                fqdnPtr = dos.size();
                writeFQDN(serviceName, dos);
            } else {
                dos.write((fqdnPtr >> 8) | 192);
                dos.write(fqdnPtr & TIFFConstants.TIFFTAG_OSUBFILETYPE);
            }
            dos.writeShort(qtype);
            dos.writeShort(qclass);
        }
        dos.close();
        return bos.toByteArray();
    }

    public static Result resolve(String serviceName, int timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }
        InetAddress group = InetAddress.getByName(MULTICAST_GROUP_ADDRESS);
        Result result = new Result();
        MulticastSocket sock = new MulticastSocket();
        try {
            byte[] data = queryPacket(serviceName, 1, 33, 16);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, (int) PORT);
            sock.setTimeToLive(255);
            sock.send(packet);
            byte[] buf = new byte[1024];
            DatagramPacket packet2 = new DatagramPacket(buf, buf.length);
            long endTime = 0;
            if (timeout != 0) {
                endTime = System.currentTimeMillis() + ((long) timeout);
            }
            while (true) {
                if (result.srv != null && result.txt != null) {
                    break;
                }
                if (timeout != 0) {
                    int remaining = (int) (endTime - System.currentTimeMillis());
                    if (remaining <= 0) {
                        break;
                    }
                    sock.setSoTimeout(remaining);
                }
                sock.receive(packet2);
                if (result.source == null) {
                    result.source = packet2.getAddress();
                }
                decode(packet2.getData(), packet2.getLength(), result);
            }
            return result;
        } finally {
            sock.close();
        }
    }

    private static void writeFQDN(String name, OutputStream out) throws IOException {
        String[] split = name.split("\\.");
        for (String part : split) {
            out.write(part.length());
            out.write(part.getBytes());
        }
        out.write(0);
    }

    private static void hexdump(byte[] data, int offset, int length) {
        char c;
        while (offset < length) {
            System.out.printf("%08x", Integer.valueOf(offset));
            int col = 0;
            while (col < 16 && offset < length) {
                System.out.printf(" %02x", Integer.valueOf(data[offset] & 255));
                col++;
                offset++;
            }
            while (col < 16) {
                System.out.printf("   ", new Object[0]);
                col++;
            }
            System.out.print(" ");
            offset = offset;
            int col2 = 0;
            while (col2 < 16 && offset < length) {
                byte val = data[offset];
                if (val < 32 || val >= Byte.MAX_VALUE) {
                    c = '.';
                } else {
                    c = (char) val;
                }
                System.out.printf("%c", Character.valueOf(c));
                col2++;
                offset++;
            }
            System.out.println();
        }
    }

    static Result decode(byte[] packet, int packetLength) throws IOException {
        Result result = new Result();
        decode(packet, packetLength, result);
        return result;
    }

    static void decode(byte[] packet, int packetLength, Result result) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet, 0, packetLength));
        dis.readShort();
        dis.readShort();
        int questions = dis.readUnsignedShort();
        int answers = dis.readUnsignedShort();
        int authorityRRs = dis.readUnsignedShort();
        int additionalRRs = dis.readUnsignedShort();
        for (int i = 0; i < questions; i++) {
            decodeFQDN(dis, packet, packetLength);
            dis.readShort();
            dis.readShort();
        }
        for (int i2 = 0; i2 < answers + authorityRRs + additionalRRs; i2++) {
            String fqdn = decodeFQDN(dis, packet, packetLength);
            short type = dis.readShort();
            dis.readShort();
            int ttl = dis.readInt();
            byte[] data = new byte[dis.readUnsignedShort()];
            dis.readFully(data);
            Record record = null;
            record = null;
            switch (type) {
                case 1:
                    A decodeA = decodeA(data);
                    result.a = decodeA;
                    record = decodeA;
                    break;
                case 12:
                    decodePTR(data, packet, packetLength);
                    break;
                case 16:
                    TXT decodeTXT = decodeTXT(data);
                    result.txt = decodeTXT;
                    record = decodeTXT;
                    break;
                case 33:
                    SRV decodeSRV = decodeSRV(data, packet, packetLength);
                    result.srv = decodeSRV;
                    record = decodeSRV;
                    break;
            }
            if (record != null) {
                record.fqdn = fqdn;
                record.ttl = ttl;
            }
        }
    }

    private static SRV decodeSRV(byte[] srvData, byte[] packetData, int packetLength) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(srvData));
        SRV srv = new SRV();
        srv.priority = dis.readUnsignedShort();
        srv.weight = dis.readUnsignedShort();
        srv.port = dis.readUnsignedShort();
        srv.target = decodeFQDN(dis, packetData, packetLength);
        return srv;
    }

    private static String typeString(short type) {
        switch (type) {
            case 1:
                return "A";
            case 12:
                return "PTR";
            case 16:
                return "TXT";
            case 33:
                return "SRV";
            default:
                return "Unknown";
        }
    }

    private static String decodePTR(byte[] ptrData, byte[] packet, int packetLength) throws IOException {
        return decodeFQDN(new DataInputStream(new ByteArrayInputStream(ptrData)), packet, packetLength);
    }

    private static A decodeA(byte[] data) throws IOException {
        if (data.length < 4) {
            throw new IOException("expected 4 bytes for IPv4 addr");
        }
        A a = new A();
        a.ipaddr = (data[0] & 255) + "." + (data[1] & 255) + "." + (data[2] & 255) + "." + (data[3] & 255);
        return a;
    }

    private static TXT decodeTXT(byte[] data) throws IOException {
        String key;
        TXT txt = new TXT();
        txt.dict = new HashMap();
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        while (true) {
            try {
                byte[] segmentBytes = new byte[dis.readUnsignedByte()];
                dis.readFully(segmentBytes);
                String segment = new String(segmentBytes);
                int pos = segment.indexOf(61);
                String value = null;
                if (pos != -1) {
                    key = segment.substring(0, pos);
                    value = segment.substring(pos + 1);
                } else {
                    key = segment;
                }
                if (!txt.dict.containsKey(key)) {
                    txt.dict.put(key, value);
                }
            } catch (EOFException e) {
                return txt;
            }
        }
    }

    private static String decodeFQDN(DataInputStream r8, byte[] r9, int r10) throws IOException {
        /*
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r0 = 0
        L_0x0006:
            r3 = 0
        L_0x0007:
            int r1 = r8.readUnsignedByte()
            if (r1 != 0) goto L_0x0012
            java.lang.String r6 = r4.toString()
            return r6
        L_0x0012:
            r6 = r1 & 192(0xc0, float:2.69E-43)
            r7 = 192(0xc0, float:2.69E-43)
            if (r6 != r7) goto L_0x003d
            int r3 = r3 + 1
            int r6 = r3 * 2
            if (r6 < r10) goto L_0x0026
            java.io.IOException r6 = new java.io.IOException
            java.lang.String r7 = "cyclic empty references in domain name"
            r6.<init>(r7)
            throw r6
        L_0x0026:
            r1 = r1 & 63
            int r6 = r1 << 8
            int r7 = r8.readUnsignedByte()
            r2 = r6 | r7
            java.io.DataInputStream r8 = new java.io.DataInputStream
            java.io.ByteArrayInputStream r6 = new java.io.ByteArrayInputStream
            int r7 = r10 - r2
            r6.<init>(r9, r2, r7)
            r8.<init>(r6)
            goto L_0x0007
        L_0x003d:
            byte[] r5 = new byte[r1]
            r8.readFully(r5)
            if (r0 == 0) goto L_0x0049
            r6 = 46
            r4.append(r6)
        L_0x0049:
            r0 = 1
            java.lang.String r6 = new java.lang.String
            r6.<init>(r5)
            r4.append(r6)
            int r6 = r4.length()
            if (r6 <= r10) goto L_0x0006
            java.io.IOException r6 = new java.io.IOException
            java.lang.String r7 = "cyclic non-empty references in domain name"
            r6.<init>(r7)
            throw r6
        */
        StringBuilder r4 = new StringBuilder();
        int r1 = r8.readUnsignedByte();
        if (r1 != 0)
            return r4.toString();
        return "";
    }
}
