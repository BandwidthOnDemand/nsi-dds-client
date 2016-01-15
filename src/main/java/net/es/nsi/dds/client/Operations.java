/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.DocumentListType;
import net.es.nsi.dds.api.jaxb.DocumentType;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Operations {
    private final ObjectFactory factory = new ObjectFactory();
    private WebTarget target;

    public Operations(WebTarget target) {
        this.target = target;
    }

    public enum Level {
        NSA, TYPE
    }

    public void list(Level level) {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }

            if (documents != null) {
                for (DocumentType document : documents.getDocument()) {
                    switch (level) {
                        case NSA:
                            System.out.println("nsa=" + document.getNsa() + "; type=" + document.getType() + "id=" + document.getId() + "; version=" + document.getVersion().toString());
                            break;
                        case TYPE:
                            System.out.println("type=" + document.getType() + "; id=" + document.getId() + "; version=" + document.getVersion().toString());
                            break;
                    }
                }
            }
        }
        else {
            System.err.println("list failed (" + response.getStatus() + ")");
        }
        response.close();
    }

    public void details(String... segment) throws Exception {
        WebTarget wt = target;
        for (String path : segment) {
            wt = wt.path(path.trim());
        }

        Response response = wt.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }
            if (documents != null) {
                System.out.println(Parser.getInstance().jaxbToString(factory.createDocuments(documents)));
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    public void decode(String... segment) throws Exception {
        System.out.println("Here: " + segment.length);
        WebTarget wt = target;
        for (String path : segment) {
            wt = wt.path(path.trim());
        }

        Response response = wt.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }
            if (documents != null) {
                System.out.println(Formatter.documents("Documents:", documents));
            }
        }
        else {
            System.err.println("decode failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    public void contents(String... segment) throws Exception {
        WebTarget wt = target;
        for (String path : segment) {
            wt = wt.path(path.trim());
        }

        Response response = wt.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }
            if (documents != null) {
                System.out.println(Formatter.simpleContent(documents));
            }
        }
        else {
            System.err.println("contents failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }
}
