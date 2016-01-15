package net.es.nsi.dds.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.mail.MessagingException;
import org.xml.sax.SAXException;

/**
 *
 * @author hacksaw
 */
public class Convert {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
        }

        if ("-decode".equalsIgnoreCase(args[0])) {
            System.out.println(decode(args[1]));
        }
        else if ("-encode".equalsIgnoreCase(args[0])) {
            encode(args[1], System.out);
        }
        else {
            usage();
        }
    }

    static void usage() {
        System.err.println("Usage: convert [-decode filename | -encode filename]");
        System.exit(1);
    }

    static String decode(String filename) throws IOException, SAXException, UnsupportedEncodingException, MessagingException {
        return ContentType.decode2String(ContentType.XGZIP, ContentTransferEncoding.decode(ContentTransferEncoding.BASE64, new FileInputStream(new File(filename))));
    }

    static void encode(String filename, OutputStream os) throws IOException, MessagingException {
        String contents = read(filename);
        try (OutputStream gzip = ContentType.encode(ContentType.XGZIP, ContentTransferEncoding.encode(ContentTransferEncoding.BASE64, os))) {
            gzip.write(contents.getBytes(Charset.forName("UTF-8")));
            gzip.flush();
        }
    }

    static String read(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                sb.append(line.trim());
            }
        }

        return sb.toString();
    }
}
