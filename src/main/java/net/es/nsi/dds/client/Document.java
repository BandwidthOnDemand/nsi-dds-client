/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.DocumentType;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Document implements ShellDependent, Commands {
    private static final ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private final WebTarget target;

    public Document(WebTarget target) {
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to document type context")
    public String exit() {
        int indexOf = target.getUri().getPath().lastIndexOf("/");
        return target.getUri().getPath().subSequence(0, indexOf).toString();
    }

    @Command(description="List summary of document.")
    @Override
    public void ls() {
        System.out.println(target.getUri().toString());
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentType document;
            try (ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {})) {
                document = chunkedInput.read();
            }

            if (document != null) {
                System.out.println("version=" + document.getVersion().toString() + "; expires=" + document.getExpires().toString() + "; href=" + document.getHref());
            }
        }
        else {
            System.err.println("list failed (" + response.getStatus() + ")");
        }
        response.close();
    }

    @Command(description="List summary of document.")
    @Override
    public void list() {
        ls();
    }

    @Command(description="Display document entry.")
    @Override
    public void details() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentType document;
            try (ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {})) {
                document = chunkedInput.read();
            }

            if (document != null) {
                System.out.println(Parser.getInstance().jaxbToString(factory.createDocument(document)));
            }
            else {
                System.err.println("details returned empty results.");
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Decode document entry.")
    @Override
    public void decode() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentType document;
            try (ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {})) {
                document = chunkedInput.read();
            }

            if (document != null) {
                System.out.println(Formatter.document(document));
            }
            else {
                System.err.println("decode returned empty results.");
            }
        }
        else {
            System.err.println("decode failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Display contents of document entry.")
    @Override
    public void contents() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentType document;
            try (ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {})) {
                document = chunkedInput.read();
            }

            if (document != null) {
                System.out.println(Formatter.simpleContent(document));
            }
            else {
                System.err.println("contents returned empty results.");
            }
        }
        else {
            System.err.println("contents failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Delete this document entry.")
    @Override
    public void delete() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).delete();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentType document;
            try (ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {})) {
                document = chunkedInput.read();
            }

            if (document != null) {
                System.out.println("sucessfully deleted " + document.getId());
                System.out.println(Parser.getInstance().jaxbToString(factory.createDocument(document)));
            }
            else {
                System.err.println("delete returned empty results.");
            }
        }
        else {
            System.err.println("delete failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }
}
