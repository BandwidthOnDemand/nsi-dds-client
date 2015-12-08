/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.nsi.dds.client;

import java.io.IOException;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.es.nsi.dds.api.jaxb.CollectionType;
import net.es.nsi.dds.api.jaxb.ContentType;
import net.es.nsi.dds.api.jaxb.DocumentEventType;
import net.es.nsi.dds.api.jaxb.DocumentListType;
import net.es.nsi.dds.api.jaxb.DocumentType;
import net.es.nsi.dds.api.jaxb.FilterAndType;
import net.es.nsi.dds.api.jaxb.FilterCriteriaType;
import net.es.nsi.dds.api.jaxb.FilterOrType;
import net.es.nsi.dds.api.jaxb.FilterType;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import net.es.nsi.dds.api.jaxb.SubscriptionListType;
import net.es.nsi.dds.api.jaxb.SubscriptionType;
import org.xml.sax.SAXException;

/**
 *
 * @author hacksaw
 */
public class Formatter {
    private static final ObjectFactory factory = new ObjectFactory();

    public static String collection(CollectionType collection) {
        StringBuilder sb = new StringBuilder();
        sb.append(subscriptions(collection.getSubscriptions()));
        sb.append(documents("Local Documents:", collection.getLocal()));
        sb.append(documents("Documents:", collection.getDocuments()));
        return sb.toString();
    }

    public static String subscriptions(SubscriptionListType subscriptions) {
        StringBuilder sb = new StringBuilder();
        sb.append("subscriptions:");
        for (SubscriptionType subscription : subscriptions.getSubscription()) {
            sb.append(subscription(subscription));
        }
        return sb.toString();
    }

    public static String subscription(SubscriptionType subscription) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  id:          "); sb.append(subscription.getId());
        sb.append("\n  version:     "); sb.append(subscription.getVersion());
        sb.append("\n  href:        "); sb.append(subscription.getHref());
        sb.append("\n  requesterId: "); sb.append(subscription.getRequesterId());
        sb.append("\n  callback:    "); sb.append(subscription.getCallback());
        sb.append("\n  filer:       "); sb.append(subscription.getFilter());
        sb.append("\n");
        return sb.toString();
    }

    public static String filter(FilterType filter) {
        StringBuilder sb = new StringBuilder("filter={ ");

        for (FilterCriteriaType include: filter.getInclude()) {
            sb.append("include={ ");
            sb.append(criteria(include));
            sb.append("}");
        }

        for (FilterCriteriaType exclude: filter.getExclude()) {
            sb.append("exclude={ ");
            sb.append(criteria(exclude));
            sb.append("}");
        }

        sb.append(" }");

        return sb.toString();
    }

    public static String criteria(FilterCriteriaType criteria) {
        StringBuilder sb = new StringBuilder();
        for (DocumentEventType event : criteria.getEvent()) {
            sb.append("event={ ");
            sb.append(event.value());
            sb.append(" },");
        }
        for (FilterAndType and : criteria.getAnd()) {
            sb.append("and={ id: ");
            sb.append(and.getId());
            sb.append(", nsa: ");
            sb.append(and.getNsa());
            sb.append(", type: ");
            sb.append(and.getType());
            sb.append(" },");
        }
        for (FilterOrType or : criteria.getOr()) {
            sb.append("or={ ");
            for (JAXBElement<String> str : or.getNsaOrTypeOrId()) {
                sb.append(str.getName());
                sb.append("=");
                sb.append(str.getValue());
                sb.append(", ");
            }
            sb.append(" },");
        }

        return sb.toString();
    }

    public static String documents(String label, DocumentListType documents) {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        documents.getDocument().stream().forEach((document) -> {
            sb.append(document(document));
        });
        return sb.toString();
    }

    public static String document(DocumentType document) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  id:          "); sb.append(document.getId());
        sb.append("\n  type:        "); sb.append(document.getType());
        sb.append("\n  nsa:         "); sb.append(document.getNsa());
        sb.append("\n  version:     "); sb.append(document.getVersion());
        sb.append("\n  expires:     "); sb.append(document.getExpires());
        sb.append("\n  href:        "); sb.append(document.getHref());
        sb.append("\n  attributes:  "); sb.append(attributes(document.getOtherAttributes()));
        sb.append("\n  signature:   "); sb.append(content(document.getSignature()));
        sb.append("\n  content:     "); sb.append(content(document.getContent()));
        sb.append("\n");
        return sb.toString();
    }

    public static String attributes(Map<QName, String> attributes) {
        StringBuilder sb = new StringBuilder();
        attributes.forEach((qname, value) -> {
                sb.append("{ ");
                sb.append(qname);
                sb.append(", ");
                sb.append(value);
                sb.append(" }, ");
        });
        return sb.toString();
    }

    public static String content(ContentType content) {
        StringBuilder sb = new StringBuilder();
        sb.append("contentType=\"");
        sb.append(content.getContentType());
        sb.append("\", contentTransferEncoding=\"");
        sb.append(content.getContentTransferEncoding());
        sb.append("\"\n");
        try {
            sb.append(Decoder.decode(content.getContentTransferEncoding(), content.getContentType(), content.getValue()));
        } catch (IOException | SAXException | RuntimeException ex) {
            sb.append("    Could not decode contents: ");
            sb.append(ex.getLocalizedMessage());
        }
        return sb.toString();
    }

    public static String simpleContent(CollectionType collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("Subscriptions\n");
        sb.append("============================================================\n");
        sb.append(Parser.getInstance().jaxbToString(factory.createSubscriptions(collection.getSubscriptions())));
        sb.append("============================================================\n");
        sb.append("Local Documents\n");
        sb.append("============================================================\n");
        sb.append(simpleContent(collection.getLocal()));
        sb.append("============================================================\n");
        sb.append("Documents\n");
        sb.append("============================================================\n");
        sb.append(simpleContent(collection.getDocuments()));
        return sb.toString();
    }

    public static String simpleContent(DocumentListType documents) {
        StringBuilder sb = new StringBuilder();
        documents.getDocument().stream().forEach((document) -> {
            sb.append(simpleContent(document.getContent()));
            sb.append("\n");
        });
        return sb.toString();
    }

    public static String simpleContent(DocumentType document) {
        return simpleContent(document.getContent());
    }

    public static String simpleContent(ContentType content) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(Decoder.decode(content.getContentTransferEncoding(), content.getContentType(), content.getValue()));
        } catch (IOException | SAXException | RuntimeException ex) {
            sb.append("Could not decode contents: ");
            sb.append(ex.getLocalizedMessage());
        }
        return sb.toString();
    }
}
