/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;
import java.io.IOException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.DocumentListType;
import net.es.nsi.dds.api.jaxb.DocumentType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Type  implements ShellDependent {
    private Shell theShell;
    private WebTarget target;

    public Type(WebTarget target) {
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to NSA context.")
    public String exit() {
        int indexOf = target.getUri().getPath().lastIndexOf("/");
        return target.getUri().getPath().subSequence(0, indexOf).toString();
    }

    @Command(description="List identifiers of all documents for this NSA and type.")
    public void ls() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }
            if (documents != null) {
                for (DocumentType document : documents.getDocument()) {
                    System.out.println(document.getId());
                }
            }
        }
        else {
            System.err.println("list failed (" + response.getStatus() + ")");
        }
        response.close();
    }

    @Command(description="List summary of all documents for this NSA and type.")
    public void list() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }

            if (documents != null) {
                for (DocumentType document : documents.getDocument()) {
                    System.out.println("id=" + document.getId() + "; version=" + document.getVersion().toString() +
                            "; expires=" + document.getExpires().toString());
                }
            }

        }
        else {
            System.err.println("list failed (" + response.getStatus() + ")");
        }

        response.close();
    }

    @Command(description="Set document context.")
    public void cd(@Param(name="id", description="Document identifier of focus.") String id) throws IOException {
        WebTarget path = target.path(id);
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ShellFactory.createSubshell(id, theShell, path.getUri().toASCIIString(), new Document(path)).commandLoop();
        }
        else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println(id + " not found.");
        }
        else {
            System.err.println("document focus failed (" + response.getStatus() + ")");
        }
        response.close();
    }

    @Command(description="Set document context.")
    public void cd() throws IOException {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }

            if (documents != null && documents.getDocument().size() == 1) {
                String id = documents.getDocument().get(0).getId();
                ShellFactory.createSubshell(id, theShell, target.path(id).getUri().toString(), new Document(target.path(id))).commandLoop();
            }
            else if (documents != null) {
                System.out.println("cd ambiguous (" + documents.getDocument().size() + ")");
            }
            else {
                System.err.println("cd failed");
            }
        }
        else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println("no docuemnts not found.");
        }
        else {
            System.err.println("document focus failed (" + response.getStatus() + ")");
        }
        response.close();
    }
}
