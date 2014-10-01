/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import asg.cliche.CLIException;
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
public class Documents implements ShellDependent {
    private final ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private WebTarget target;
    private Operations operations;

    public Documents(WebTarget target) {
        this.target = target;
        operations = new Operations(target);
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="List available resource types.")
    public void ls() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {});
        if (chunkedInput == null) {
            System.err.println("list returned empty results.");
            return;
        }

        DocumentListType documents = chunkedInput.read();

        Map<String, Integer> map = new HashMap<>();
        if (documents != null) {
            for (DocumentType document : documents.getDocument()) {
                Integer count = map.get(document.getNsa());
                if (count == null) {
                    count = new Integer(1);
                    map.put(document.getNsa(), count);
                }
                else {
                    count++;
                    map.put(document.getNsa(), count);
                }
            }
            Set<Map.Entry<String, Integer>> entrySet = map.entrySet();
            for (Map.Entry<String, Integer> entry : entrySet) {
                System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
            }
        }
    }

    @Command(description="List summary of all documents.")
    public void list() {
        try {
            operations.list(Operations.Level.NSA);
        }
        catch (Exception ex) {
            System.err.println("list failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of specific document.")
    public void details(
            @Param(name="nsaId", description="NSA identifier identifier owning document") String nsaId,
            @Param(name="type", description="Document type") String type,
            @Param(name="id", description="Document identifier") String id) throws CLIException {
        try {
            operations.details(nsaId, type, id);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of documents of a specific type under an NSA.")
    public void details(
            @Param(name="nsaId", description="NSA identifier identifier owning document") String nsaId,
            @Param(name="type", description="Document type") String type) throws CLIException {
        try {
            operations.details(nsaId, type);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of documents under an NSA.")
    public void details(
            @Param(name="nsaId", description="NSA identifier identifier owning document") String nsaId) throws CLIException {
        try {
            operations.details(nsaId);
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Get details of all available documents.")
    public void details() throws CLIException {
        try {
            operations.details();
        }
        catch (Exception ex) {
            System.err.println("details failed with exception\n" + ex.getLocalizedMessage());
        }
    }

    @Command(description="Set NSA context.")
    public void cd(@Param(name="nsaId", description="NSA identifier of focus.") String nsaId) throws IOException {
        WebTarget path = target.path(nsaId);
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ShellFactory.createSubshell(nsaId, theShell, path.getUri().toString(), new Nsa(nsaId, path)).commandLoop();
        }
        else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println(nsaId + " not found.");
        }
        else {
            System.err.println("nsa focus failed (" + response.getStatus() + ")");
        }
    }
}
