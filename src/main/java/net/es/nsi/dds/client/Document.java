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
public class Document  implements ShellDependent {
    private static ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private WebTarget target;

    public Document(WebTarget target) {
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to document type context")
    public String exit() {
        return "..";
    }

    @Command(description="List summary of all documents.")
    public void ls() {
        System.out.println(target.getUri().toString());
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {});
        DocumentType document = chunkedInput.read();

        if (document != null) {
            System.out.println("version=" + document.getVersion().toString() + "; expires=" + document.getExpires().toString() + "; href=" + document.getHref());
        }
    }

    @Command(description="Display for document entry.")
    public void details() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
            return;
        }

        final ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {});
        if (chunkedInput == null) {
            System.err.println("details returned empty results.");
            return;
        }

        DocumentType document = chunkedInput.read();

        if (document != null) {
            System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocument(document)));
        }
        else {
            System.err.println("details returned empty results.");
        }
    }

    @Command(description="Delete this document entry.")
    public void delete() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).delete();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("delete failed (" + response.getStatusInfo().getReasonPhrase() + ")");
            return;
        }

        final ChunkedInput<DocumentType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentType>>() {});
        if (chunkedInput == null) {
            System.err.println("details returned empty results.");
            return;
        }

        DocumentType document = chunkedInput.read();

        if (document != null) {
            System.out.println("sucessfully deleted " + document.getId());
            System.out.println(DdsParser.getInstance().jaxbToString(factory.createDocument(document)));
        }
        else {
            System.err.println("details returned empty results.");
        }
    }
}
