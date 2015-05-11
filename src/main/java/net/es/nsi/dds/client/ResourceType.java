package net.es.nsi.dds.client;

import com.google.common.base.Optional;
import java.util.StringTokenizer;

/**
 *
 * @author hacksaw
 */
public enum ResourceType {
    COLLECTION,
    SUBSCRIPTIONS,
    SUBSCRIPTION,
    DOCUMENTS,
    NSA,
    TYPE,
    DOCUMENT;

    private final static String SUBSCRIPTIONS_RESOURCE = "subscriptions";
    private final static String DOCUMENTS_RESOURCE = "documents";
    private final static String LOCAL_RESOURCE = "local";

    public static ResourceType getResource(String resource) throws IllegalArgumentException {
        StringTokenizer st = new StringTokenizer(resource, "/");
        Optional<String> directory = Optional.absent();
        if (st.hasMoreTokens()) {
            directory = Optional.of(st.nextToken());
        }

        if (!directory.isPresent()) {
            // This is the root case.
            return COLLECTION;
        }
        else if (SUBSCRIPTIONS_RESOURCE.equalsIgnoreCase(directory.get())) {
            // We have a subscription related query.
            return getSubscriptions(st);
        }
        else if (LOCAL_RESOURCE.equalsIgnoreCase(directory.get())) {
            // We have a local related query.
            return getLocal(st);
        }
        else if (DOCUMENTS_RESOURCE.equalsIgnoreCase(directory.get())) {
            // We have a documents related query.
            return getDocuments(st);
        }
        else {
            System.err.println("Error: Unknown resource type " + directory.get());
            throw new IllegalArgumentException("Error: Unknown resource type " + directory.get());
        }
    }

    private static ResourceType getSubscriptions(StringTokenizer st) {
        if (st.countTokens() > 0) {
            return SUBSCRIPTION;
        }

        return SUBSCRIPTIONS;
    }

    private static ResourceType getDocuments(StringTokenizer st) {
        int depth = st.countTokens();
        switch(depth) {
            case 0:
                return DOCUMENTS;
            case 1:
                return NSA;
            case 2:
                return TYPE;
            default:
                return DOCUMENT;
        }
    }

    private static ResourceType getLocal(StringTokenizer st) {
        int depth = st.countTokens();
        if (0 <= depth && depth <= 1) {
            return DOCUMENTS;
        }
        return DOCUMENT;
    }
}
