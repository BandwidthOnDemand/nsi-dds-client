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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import net.es.nsi.dds.api.jaxb.SubscriptionListType;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.glassfish.jersey.client.ChunkedInput;

/**
 *
 * @author hacksaw
 */
public class Subscriptions implements ShellDependent {
    private Shell theShell;
    private WebTarget target;

    public Subscriptions(WebTarget target) {
        this.target = target;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.theShell = theShell;
    }

    @Command(description="List all subscriptions.")
    public void list() throws CLIException {
        WebTarget path = target.path("subscriptions");
        Response response = path.queryParam("summary", "true").request().accept(NsiConstants.NSI_DDS_V1_XML).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("list failed (" + response.getStatusInfo().getReasonPhrase() + ")");
            return;
        }

        final ChunkedInput<SubscriptionListType> chunkedInput = response.readEntity(new GenericType<ChunkedInput<SubscriptionListType>>() {});
        SubscriptionListType subscriptions = chunkedInput.read();

        if (subscriptions != null) {
            for (SubscriptionType subscription : subscriptions.getSubscription()) {
                System.out.println("id=" + subscription.getId() + "; requesterId=" + subscription.getRequesterId());
            }
        }
    }

    @Command(description="Get details of subscription.")
    public void details(@Param(name="id", description="Subscription identifier to show details") String id) throws CLIException {
        WebTarget path = target.path("subscriptions").path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_JSON).get();
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
            return;
        }

        String json = response.readEntity(new GenericType<String>() {});
        if (json != null && !json.isEmpty()) {
            System.out.println(json);
        }
    }

    @Command(description="Delete a specific subscription.")
    public void delete(@Param(name="id", description="Subscription identifier to delete")  String id) throws CLIException {
        WebTarget path = target.path("subscriptions").path(id);
        Response response = path.request().accept(NsiConstants.NSI_DDS_V1_JSON).delete();
        if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            System.err.println("details failed (" + response.getStatusInfo().getReasonPhrase() + ")");
        }
    }
}
