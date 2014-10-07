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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import net.es.nsi.dds.api.jaxb.SubscriptionListType;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Subscriptions implements ShellDependent {
    private final ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private WebTarget target;

    public Subscriptions(WebTarget target) {
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to root context.")
    public String exit() {
        int indexOf = target.getUri().getPath().lastIndexOf("/");
        return target.getUri().getPath().subSequence(0, indexOf).toString();
    }

    @Command(description="List all subscriptions.")
    public void ls() throws CLIException {
        WebTarget path = target.path("subscriptions");
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionListType subscriptions;
            try (ChunkedInput<SubscriptionListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionListType>>() {})) {
                subscriptions = chunkedInput.read();
            }

            if (subscriptions != null) {
                for (SubscriptionType subscription : subscriptions.getSubscription()) {
                    System.out.println(subscription.getId());
                }
            }
        }
        else {
            System.err.println("list failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }

        response.close();
    }

    @Command(description="List summary of all subscriptions.")
    public void list() throws CLIException {
        WebTarget path = target.path("subscriptions");
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionListType subscriptions;
            try (ChunkedInput<SubscriptionListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionListType>>() {})) {
                subscriptions = chunkedInput.read();
            }

            if (subscriptions != null) {
                for (SubscriptionType subscription : subscriptions.getSubscription()) {
                    System.out.println("id=" + subscription.getId() + "; requesterId=" + subscription.getRequesterId() + "; callback=" + subscription.getCallback());
                }
            }
        }
        else {
            System.err.println("list failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }

        response.close();
    }

    @Command(description="Get details of subscription.")
    public void details(@Param(name="id", description="Subscription identifier to show details") String id) throws CLIException {
        WebTarget path = target.path("subscriptions").path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_JSON).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription = response.readEntity(new GenericType<SubscriptionType>() {});
            if (subscription != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createSubscription(subscription)));
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Get details of all subscriptions.")
    public void details() throws Exception {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionListType subscriptions = response.readEntity(new GenericType<SubscriptionListType>() {});
            if (subscriptions != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createSubscriptions(subscriptions)));
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }

        response.close();
    }

    @Command(description="Delete a specific subscription.")
    public void delete(@Param(name="id", description="Subscription identifier to delete")  String id) throws CLIException {
        WebTarget path = target.path("subscriptions").path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_JSON).delete();
        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
    }

    @Command(description="Set subscription context.")
    public void cd(@Param(name="subscriptionId", description="Subscription identifier of focus.") String subscriptionId) throws IOException {
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            System.err.println("cd failed (must specify subscription identifier)");
            return;
        }

        // Confirm that this is a valid subscription identifier.
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("cd failed (" + response.getStatusInfo().getReasonPhrase() + ")");
            response.close();
            return;
        }

        SubscriptionListType subscriptions = response.readEntity(new GenericType<SubscriptionListType>() {});
        response.close();

        for (SubscriptionType subscription : subscriptions.getSubscription()) {
            if (subscriptionId.equalsIgnoreCase(subscription.getId())) {
                WebTarget path = target.path(subscription.getId());
                response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    ShellFactory.createSubshell(subscription.getId(), theShell, path.getUri().toASCIIString(), new Subscription(subscription.getId(), path)).commandLoop();
                }
                else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                    System.out.println(subscriptionId + " not found.");
                }
                else {
                    System.err.println("subscription focus failed (" + response.getStatus() + ")");
                }
                response.close();
                return;
            }
        }

        System.out.println(subscriptionId + " not found.");
    }
}
