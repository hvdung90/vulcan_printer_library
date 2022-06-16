package org.cups;

import com.itextpdf.text.pdf.BidiOrder;

public class Base64Coder {
    private static char[] map1 = new char[64];
    private static byte[] map2 = new byte[128];

    static {
        int i;
        int i2 = 0;
        char c = 'A';
        while (true) {
            i = i2;
            if (c > 'Z') {
                break;
            }
            i2 = i + 1;
            map1[i] = c;
            c = (char) (c + 1);
        }
        char c2 = 'a';
        while (c2 <= 'z') {
            map1[i] = c2;
            c2 = (char) (c2 + 1);
            i++;
        }
        char c3 = '0';
        while (c3 <= '9') {
            map1[i] = c3;
            c3 = (char) (c3 + 1);
            i++;
        }
        int i3 = i + 1;
        map1[i] = '+';
        int i4 = i3 + 1;
        map1[i3] = '/';
        for (int i5 = 0; i5 < map2.length; i5++) {
            map2[i5] = -1;
        }
        for (int i6 = 0; i6 < 64; i6++) {
            map2[map1[i6]] = (byte) i6;
        }
    }

    public static String encodeString(String s) {
        return new String(encode(s.getBytes()));
    }

    public static char[] encode(byte[] in) {
        return encode(in, in.length);
    }

    public static char[] encode(byte[] in, int iLen) {
        int i1;
        int ip;
        int i2;
        int oDataLen = ((iLen * 4) + 2) / 3;
        char[] out = new char[(((iLen + 2) / 3) * 4)];
        int ip2 = 0;
        int op = 0;
        while (ip2 < iLen) {
            int ip3 = ip2 + 1;
            int i0 = in[ip2] & 255;
            if (ip3 < iLen) {
                ip = ip3 + 1;
                i1 = in[ip3] & 255;
            } else {
                i1 = 0;
                ip = ip3;
            }
            if (ip < iLen) {
                ip2 = ip + 1;
                i2 = in[ip] & 255;
            } else {
                i2 = 0;
                ip2 = ip;
            }
            int o2 = ((i1 & 15) << 2) | (i2 >>> 6);
            int o3 = i2 & 63;
            int op2 = op + 1;
            out[op] = map1[i0 >>> 2];
            int op3 = op2 + 1;
            out[op2] = map1[((i0 & 3) << 4) | (i1 >>> 4)];
            out[op3] = op3 < oDataLen ? map1[o2] : '=';
            int op4 = op3 + 1;
            out[op4] = op4 < oDataLen ? map1[o3] : '=';
            op = op4 + 1;
        }
        return out;
    }

    public static String decodeString(String s) {
        return new String(decode(s));
    }

    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    public static byte[] decode(char[] in) {
        char c;
        char c2;
        int ip;
        int iLen = in.length;
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iLen - 1] == '=') {
            iLen--;
        }
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int op = 0;
        int ip2 = 0;
        while (ip2 < iLen) {
            int ip3 = ip2 + 1;
            char c3 = in[ip2];
            int ip4 = ip3 + 1;
            char c4 = in[ip3];
            if (ip4 < iLen) {
                c = in[ip4];
                ip4++;
            } else {
                c = 'A';
            }
            if (ip4 < iLen) {
                ip = ip4 + 1;
                c2 = in[ip4];
            } else {
                c2 = 'A';
                ip = ip4;
            }
            if (c3 > 127 || c4 > 127 || c > 127 || c2 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            byte b = map2[c3];
            byte b2 = map2[c4];
            byte b3 = map2[c];
            byte b4 = map2[c2];
            if (b < 0 || b2 < 0 || b3 < 0 || b4 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int o1 = ((b2 & BidiOrder.B) << 4) | (b3 >>> 2);
            int o2 = ((b3 & 3) << 6) | b4;
            int op2 = op + 1;
            out[op] = (byte) ((b << 2) | (b2 >>> 4));
            if (op2 < oLen) {
                op = op2 + 1;
                out[op2] = (byte) o1;
            } else {
                op = op2;
            }
            if (op < oLen) {
                out[op] = (byte) o2;
                op++;
                ip2 = ip;
            } else {
                ip2 = ip;
            }
        }
        return out;
    }

    private Base64Coder() {
    }
}
