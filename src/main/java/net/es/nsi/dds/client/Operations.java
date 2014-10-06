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
        NSA, TYPE, DOCUMENT
    }

    public void list(Level level) {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {});
        DocumentListType documents = chunkedInput.read();
        chunkedInput.close();

        if (documents != null) {
            for (DocumentType document : documents.getDocument()) {
                switch (level) {
                    case NSA:
                        System.out.println("nsa=" + document.getNsa() + "; type=" + document.getType() + "id=" + document.getId() + "; version=" + document.getVersion().toString());
                        break;
                    case TYPE:
                        System.out.println("type=" + document.getType() + "; id=" + document.getId() + "; version=" + document.getVersion().toString());
                        break;
                    case DOCUMENT:
                        System.out.println("id=" + document.getId() + "; version=" + document.getVersion().toString() + "; expires=" + document.getExpires().toString());
                        break;
                }
            }
        }
    }

    public void details(String nsaId, String type, String id) throws Exception {
        WebTarget path = target.path(nsaId).path(type).path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        else {
            DocumentType document = response.readEntity(new GenericType<DocumentType>() {});
            if (document != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocument(document)));
            }
        }
        response.close();
    }

    public void details(String nsaId, String type) throws Exception {
        WebTarget path = target.path(nsaId).path(type);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        else {
            DocumentListType documents = response.readEntity(new GenericType<DocumentListType>() {});
            if (documents != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocuments(documents)));
            }
        }
        response.close();
    }

    public void details(String nsaId) throws Exception {
        WebTarget path = target.path(nsaId);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        else {
            DocumentListType documents = response.readEntity(new GenericType<DocumentListType>() {});
            if (documents != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocuments(documents)));
            }
        }
        response.close();
    }

    public void details() throws Exception {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        else {
            DocumentListType documents = response.readEntity(new GenericType<DocumentListType>() {});
            if (documents != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocuments(documents)));
                response.close();
            }
        }
        response.close();
    }
}
