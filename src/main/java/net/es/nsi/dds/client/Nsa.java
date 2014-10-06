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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
public class Nsa  implements ShellDependent {
    private final ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private String nsaId;
    private WebTarget target;
    private Operations operations;

    public Nsa(String nsaId, WebTarget target) {
        this.nsaId = nsaId;
        this.target = target;
        operations = new Operations(target);
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to documents context.")
    public String exit() {
        int indexOf = target.getUri().getPath().lastIndexOf("/");
        return target.getUri().getPath().subSequence(0, indexOf).toString();
    }

    @Command(description="List available resource types.")
    public void ls() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
        }
        else {
            DocumentListType documents;
            try (ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {})) {
                documents = chunkedInput.read();
            }

            Map<String, Integer> map = new HashMap<>();
            if (documents != null) {
                for (DocumentType document : documents.getDocument()) {
                    Integer count = map.get(document.getType());
                    if (count == null) {
                        count = new Integer(1);
                        map.put(document.getType(), count);
                    }
                    else {
                        count++;
                        map.put(document.getType(), count);
                    }
                }
                Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
                for (Map.Entry<String, Integer> entry : entrySet) {
                    System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
                }
            }
        }
        
        response.close();
    }

    @Command(description="List summary of all documents for this NSA.")
    public void list() {
        try {
            operations.list(Operations.Level.TYPE);
        }
        catch (Exception ex) {
            System.err.println("list failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of specific document.")
    public void details(
            @Param(name="type", description="Document type") String type,
            @Param(name="id", description="Document identifier") String id) {
        try {
            operations.details(nsaId, type, id);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of documents of a specific type.")
    public void details(
            @Param(name="type", description="Document type") String type) {
        try {
            operations.details(nsaId, type);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of all available documents.")
    public void details() {
        try {
            operations.details(nsaId);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Set type context.")
    public void cd(@Param(name="type", description="Type identifier of focus.") String type) throws IOException {
        WebTarget path = target.path(type);
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ShellFactory.createSubshell(type, theShell, path.getUri().toString(), new Type(path)).commandLoop();
        }
        else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println(type + " not found.");
        }
        else {
            System.err.println("type focus failed (" + response.getStatus() + ")");
        }
    }
}
