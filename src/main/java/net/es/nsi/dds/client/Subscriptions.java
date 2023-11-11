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
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import net.es.nsi.dds.api.jaxb.SubscriptionListType;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Subscriptions implements ShellDependent, Commands {
    private final ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private final WebTarget target;

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
    @Override
    public void ls() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
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
    @Override
    public void list() {
        Response response = target.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
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
    public void details(@Param(name="id", description="Subscription identifier to show details") String id) {
        WebTarget path = target.path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription = response.readEntity(new GenericType<SubscriptionType>() {});
            if (subscription != null) {
                System.out.println(Parser.getInstance().jaxbToString(factory.createSubscription(subscription)));
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Get details of all subscriptions.")
    @Override
    public void details() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionListType subscriptions = response.readEntity(new GenericType<SubscriptionListType>() {});
            if (subscriptions != null) {
                System.out.println(Parser.getInstance().jaxbToString(factory.createSubscriptions(subscriptions)));
            }
        }
        else {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }

        response.close();
    }

    @Command(description="Decocde details of subscription.")
    public void decode(@Param(name="id", description="Subscription identifier to show details") String id) {
        WebTarget path = target.path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription = response.readEntity(new GenericType<SubscriptionType>() {});
            if (subscription != null) {
                System.out.println(Formatter.subscription(subscription));
            }
        }
        else {
            System.err.println("decode failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
        response.close();
    }

    @Command(description="Decode all subscriptions.")
    @Override
    public void decode() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionListType subscriptions = response.readEntity(new GenericType<SubscriptionListType>() {});
            if (subscriptions != null) {
                System.out.println(Formatter.subscriptions(subscriptions));
            }
        }
        else {
            System.err.println("decode failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }

        response.close();
    }

    @Command(description="Display details of a subscription.")
    public void contents(@Param(name="id", description="Subscription identifier to show details") String id) {
        details(id);
    }

    @Command(description="Display details for all subscriptions.")
    @Override
    public void contents() {
        details();
    }

    @Command(description="Delete a specific subscription.")
    public void delete(@Param(name="id", description="Subscription identifier to delete")  String id) {
        WebTarget path = target.path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_XML).delete();
        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
    }

    @Override
    public void delete() {
        System.err.println("Delete not supported on this resource");
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
                    ShellFactory.createSubshell(subscription.getId(), theShell, path.getUri().toASCIIString(), new Subscription(path)).commandLoop();
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
