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
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Subscription  implements ShellDependent {
    private static ObjectFactory factory = new ObjectFactory();
    private Shell theShell;
    private String id;
    private WebTarget target;

    public Subscription(String id, WebTarget target) {
        this.id = id;
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="Back to subscriptions context")
    public String exit() {
        int indexOf = target.getUri().getPath().lastIndexOf("/");
        return target.getUri().getPath().subSequence(0, indexOf).toString();
    }

    @Command(description="List summary of subscription.")
    public void ls() {
        System.out.println(target.getUri().toString());
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription;
            try (ChunkedInput<SubscriptionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionType>>() {})) {
                subscription = chunkedInput.read();
            }

            if (subscription != null) {
                System.out.println("requesterId=" + subscription.getRequesterId() + "; version=" + subscription.getVersion().toString() + "; callback=" + subscription.getCallback() + "; href=" + subscription.getHref());
            }
        }
        else {
            System.err.println("list failed (" + response.getStatus() + ")");
        }
        response.close();
    }

    @Command(description="Display subscription entry.")
    public void details() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription;
            try (ChunkedInput<SubscriptionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionType>>() {})) {
                subscription = chunkedInput.read();
            }

            if (subscription != null) {
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createSubscription(subscription)));
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

    @Command(description="Delete this subscription entry.")
    public void delete() {
        Response response = target.request().accept(NsiConstants.NSI_DDS_V1_XML).delete();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SubscriptionType subscription;
            try (ChunkedInput<SubscriptionType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionType>>() {})) {
                subscription = chunkedInput.read();
            }

            if (subscription != null) {
                System.out.println("sucessfully deleted " + subscription.getId());
                System.out.println(DdsParser.getInstance().jaxbToString(factory.createSubscription(subscription)));
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
