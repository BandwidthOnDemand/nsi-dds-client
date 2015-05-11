package net.es.nsi.dds.client;

import java.net.URL;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author hacksaw
 */
public class DdsClient {
    private final RestClient restClient;
    private final WebTarget path;

    public DdsClient(URL ddsURL, boolean debug) {
        restClient = new RestClient(debug);
        path = restClient.get().target(ddsURL.toString());
    }

    public void invoke(Command cmd, String resource) {
        Commands api = getDdsApi(resource);
        switch(cmd) {
            case LS:
                api.ls();
                break;

            case LIST:
                api.list();
                break;

            case DETAILS:
                api.details();
                break;

            case DELETE:
                api.delete();
                break;

            default:
                System.err.println("Unsupported command " + cmd);
                throw new IllegalArgumentException("Unsupported command " + cmd);
        }
    }

    private Commands getDdsApi(String resource) throws IllegalArgumentException {
        ResourceType type = ResourceType.getResource(resource);
        WebTarget target = path.path(resource);

        switch (type) {
            case COLLECTION:
                Root root = new Root(restClient, target);
                return root;

            case SUBSCRIPTIONS:
                Subscriptions subscriptions = new Subscriptions(target);
                return subscriptions;

            case SUBSCRIPTION:
                Subscription subscription = new Subscription(target);
                return subscription;

            case DOCUMENTS:
                Documents documents = new Documents(target);
                return documents;

            case NSA:
                Nsa nsa = new Nsa(target);
                return nsa;

            case TYPE:
                Type aType = new Type(target);
                return aType;

            case DOCUMENT:
                Document document = new Document(target);
                return document;
        }

        System.err.println("Resource type not found " + resource);
        throw new IllegalArgumentException("Resource type not found " + resource);
    }
}
