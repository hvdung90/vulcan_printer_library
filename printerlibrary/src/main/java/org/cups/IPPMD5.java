package org.cups;

import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IPPMD5 {
    private static final char[] hexChars = {'0', '1', PdfWriter.VERSION_1_2, PdfWriter.VERSION_1_3, PdfWriter.VERSION_1_4, PdfWriter.VERSION_1_5, PdfWriter.VERSION_1_6, PdfWriter.VERSION_1_7, '8', '9', 'a', 'b', Barcode128.CODE_AB_TO_C, Barcode128.CODE_AC_TO_B, Barcode128.CODE_BC_TO_A, Barcode128.FNC1_INDEX};
    private static IPPMD5 md5 = null;
    public MessageDigest md;

    private IPPMD5() throws NoSuchAlgorithmException {
        this.md = null;
        this.md = MessageDigest.getInstance("MD5");
    }

    public static IPPMD5 getInstance() throws NoSuchAlgorithmException {
        if (md5 == null) {
            md5 = new IPPMD5();
        }
        return md5;
    }

    public String hashData(byte[] dataToHash) {
        return hexStringFromBytes(calculateHash(dataToHash));
    }

    private byte[] calculateHash(byte[] dataToHash) {
        this.md.update(dataToHash, 0, dataToHash.length);
        return this.md.digest();
    }

    public String hexStringFromBytes(byte[] b) {
        String hex = PdfObject.NOTHING;
        for (int i = 0; i < b.length; i++) {
            hex = String.valueOf(hex) + hexChars[(b[i] & 255) / 16] + hexChars[(b[i] & 255) % 16];
        }
        return hex;
    }

    public String MD5Digest(String user, String passwd, String realm, String method, String resource, String nonce) {
        try {
            this.md = MessageDigest.getInstance("MD5");
            String A1 = hexStringFromBytes(this.md.digest((String.valueOf(user) + ":" + realm + ":" + passwd).getBytes()));
            String tmp = String.valueOf(method) + ":" + resource;
            this.md = MessageDigest.getInstance("MD5");
            String tmp2 = String.valueOf(A1) + ":" + nonce + ":" + hexStringFromBytes(this.md.digest(tmp.getBytes()));
            this.md = MessageDigest.getInstance("MD5");
            return hexStringFromBytes(this.md.digest(tmp2.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            return PdfObject.NOTHING;
        }
    }
}
