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
import java.net.URL;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.es.nsi.dds.api.jaxb.CollectionType;
import net.es.nsi.dds.api.jaxb.DocumentListType;
import net.es.nsi.dds.api.jaxb.DocumentType;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class DdsCommands implements ShellDependent {
    private static ObjectFactory factory = new ObjectFactory();
    private URL ddsURL;
    private RestClient restClient;
    private WebTarget path;

    public DdsCommands(URL ddsURL, boolean debug) {
        this.ddsURL = ddsURL;
        restClient = new RestClient(debug);
        path = restClient.get().target(ddsURL.toString());
    }

    private Shell theShell;

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Set the DDS server URL.")
    public void server(@Param(name="url", description="URL of the DDS server.") String url) {
        path = restClient.get().target(url);
        System.out.println(path.getUri().toASCIIString());
    }

    @Command(description="Exit the DDS command shell.")
    public String exit() {
        return "Exiting...";
    }

    @Command(description="Ping DDS server.")
    public void ping() {
        System.out.print("ping... ");
        WebTarget target = path.path("ping");
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_JSON).get();
        if (response.getStatus() != Status.OK.getStatusCode()) {
            System.err.println("failed (" + response.getStatus() + ")");
        }
        else {
            System.out.println("pong!");
        }
    }

    @Command(description="List available resource types.")
    public void ls() {
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<CollectionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<CollectionType>>() {});
        if (chunkedInput == null) {
            System.err.println("list returned empty results.");
            return;
        }

        CollectionType collection = chunkedInput.read();

        if (collection != null) {
            System.out.println("/subscriptions (" + collection.getSubscriptions().getSubscription().size() + ")");
            System.out.println("/local (" + collection.getLocal().getDocument().size() + ")");
            System.out.println("/documents (" + collection.getDocuments().getDocument().size() + ")");
        }
        else {
            System.err.println("list returned empty results.");
        }
    }

    @Command(description="List summary information for all subscriptions and documents.")
    public void list() {
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<CollectionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<CollectionType>>() {});
        if (chunkedInput == null) {
            System.err.println("list returned empty results.");
            return;
        }

        CollectionType collection = chunkedInput.read();

        if (collection != null) {
            System.out.println("/subscriptions (" + collection.getSubscriptions().getSubscription().size() + ")");
            for (SubscriptionType subscription : collection.getSubscriptions().getSubscription()) {
                System.out.println("    id=" + subscription.getId() + "; requesterId=" + subscription.getRequesterId() + "; version=" + subscription.getVersion().toString());
            }

            System.out.println("/local (" + collection.getLocal().getDocument().size() + ")");
            for (DocumentType document : collection.getLocal().getDocument()) {
                System.out.println("    nsa=" + document.getNsa() + "; type=" + document.getType() + "; id=" + document.getId() + "; version=" + document.getVersion().toString());
            }

            System.out.println("/documents (" + collection.getDocuments().getDocument().size() + ")");
            for (DocumentType document : collection.getDocuments().getDocument()) {
                System.out.println("    nsa=" + document.getNsa() + "; type=" + document.getType() + "; id=" + document.getId() + "; version=" + document.getVersion().toString());
            }
        }
    }

    @Command(description="Set resource context.")
    public void cd(@Param(name="resource", description="Resource type of focus.") String resource) throws IOException {
        WebTarget target = path.path(resource);
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            if ("subscriptions".equalsIgnoreCase(resource)) {
                ShellFactory.createSubshell(resource, theShell, target.getUri().toString(), new Subscriptions(target)).commandLoop();
            }
            else if ("documents".equalsIgnoreCase(resource)) {
                ShellFactory.createSubshell(resource, theShell, target.getUri().toString(), new Documents(target)).commandLoop();
            }
            else if ("local".equalsIgnoreCase(resource)) {
                final ChunkedInput<DocumentListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<DocumentListType>>() {});
                if (chunkedInput == null) {
                    System.err.println("cd failed");
                    return;
                }

                DocumentListType documents = chunkedInput.read();
                if (documents != null && !documents.getDocument().isEmpty()) {
                    ShellFactory.createSubshell(resource, theShell, target.getUri().toString(), new Nsa(documents.getDocument().get(0).getNsa(), target)).commandLoop();
                }
            }
            else {
                System.err.println("resource not found (" + resource + ")");
            }
        }
        else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println(resource + " not found.");
        }
        else {
            System.err.println("resource focus failed (" + response.getStatus() + ")");
        }
    }

    @Command(description="Dump DDS collection containing detailed document and subscription information.")
    public void details() {
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatus() + ")");
            return;
        }

        final ChunkedInput<CollectionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<CollectionType>>() {});
        if (chunkedInput == null) {
            System.err.println("details returned empty results.");
            return;
        }

        CollectionType collection = chunkedInput.read();

        if (collection != null) {
            System.out.println(DdsParser.getInstance().jaxbToString(factory.createCollection(collection)));
        }
    }
}
