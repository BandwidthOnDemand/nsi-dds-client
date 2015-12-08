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
public class Type implements ShellDependent, Commands {
    private Shell theShell;
    private final WebTarget target;
    private final Operations operations;

    public Type(WebTarget target) {
        this.target = target;
        operations = new Operations(target);
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
    @Override
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
    @Override
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

    @Command(description="Get details of specific document.")
    public void details(@Param(name="id", description="Document identifier") String id) {
        try {
            operations.details(id);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of all available documents.")
    @Override
    public void details() {
        try {
            operations.details();
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Decode a specific document.")
    public void decode(@Param(name="id", description="Document identifier") String id) {
        try {
            operations.decode(id);
        }
        catch (Exception ex) {
            System.err.println("decode failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Decode all available documents.")
    @Override
    public void decode() {
        try {
            operations.decode();
        }
        catch (Exception ex) {
            System.err.println("decode failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Display contents of a specific document.")
    public void contents(@Param(name="id", description="Document identifier") String id) {
        try {
            operations.contents(id);
        }
        catch (Exception ex) {
            System.err.println("decode failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Display contents of all available documents.")
    @Override
    public void contents() {
        try {
            operations.contents();
        }
        catch (Exception ex) {
            System.err.println("decode failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Override
    public void delete() {
        System.err.println("Delete not supported on this resource.");
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
