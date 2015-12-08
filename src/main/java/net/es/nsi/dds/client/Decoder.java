/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author hacksaw
 */
public class Decoder {
    public static String decode(String contentTransferEncoding,
            String contentType, String source) throws IOException, SAXException {
        if (Strings.isNullOrEmpty(contentTransferEncoding)) {
            contentTransferEncoding = ContentTransferEncoding._7BIT;
        }

        if (Strings.isNullOrEmpty(contentType)) {
            contentType = ContentType.TEXT;
        }

        try {
            InputStream cteis = ContentTransferEncoding.decode(contentTransferEncoding, source);
            InputStream ctis = ContentType.decode(contentType, cteis);
            Document decoded = DomParser.xml2Dom(ctis);
            return DomParser.prettyPrint(decoded);
        } catch (IOException | SAXException ex) {
            throw ex;
        } catch (ParserConfigurationException | TransformerException | MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
