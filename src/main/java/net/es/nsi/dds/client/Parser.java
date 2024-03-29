package net.es.nsi.dds.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import net.es.nsi.dds.api.jaxb.DdsConfigurationType;
import net.es.nsi.dds.api.jaxb.DocumentType;
import net.es.nsi.dds.api.jaxb.NmlNSAType;
import net.es.nsi.dds.api.jaxb.ObjectFactory;
import org.apache.commons.io.FileUtils;

/**
 * This class loads an NML XML based NSA object from a specified file.  This is
 * a singleton class that optimizes loading of a JAXB parser instance that may
 * take an extremely long time (on the order of 10 seconds).
 *
 * @author hacksaw
 */
public class Parser {
    private final ObjectFactory factory = new ObjectFactory();

    // The JAXB context we load pre-loading in this singleton.
    private static JAXBContext jaxbContext = null;

    /**
     * Private constructor loads the JAXB context once and prevents
     * instantiation from other classes.
     */
    private Parser() {
        try {
            // Load a JAXB context for the NML NSAType parser.
            jaxbContext = JAXBContext.newInstance("net.es.nsi.dds.api.jaxb", net.es.nsi.dds.api.jaxb.ObjectFactory.class.getClassLoader());
        }
        catch (JAXBException jaxb) {
            System.err.println("NmlParser: Failed to load JAXB instance: " + jaxb.getLocalizedMessage());
        }
    }

    /**
     * An internal static class that invokes our private constructor on object
     * creation.
     */
    private static class DdsParserHolder {
        public static final Parser INSTANCE = new Parser();
    }

    /**
     * Returns an instance of this singleton class.
     *
     * @return An NmlParser object of the NSAType.
     */
    public static Parser getInstance() {
            return DdsParserHolder.INSTANCE;
    }

    /**
     * Parse an topology configuration file from the specified file.
     *
     * @param file File containing the XML formated topology configuration.
     * @return A JAXB compiled ConfigurationType object.
     * @throws JAXBException If the XML contained in the file is not valid.
     * @throws FileNotFoundException If the specified file was not found.
     */
    @SuppressWarnings("unchecked")
    public DdsConfigurationType parse(String file) throws JAXBException, IOException {
        // Make sure we initialized properly.
        if (jaxbContext == null) {
            throw new JAXBException("parse: Failed to load JAXB instance");
        }

        // Parse the specified file.
        JAXBElement<DdsConfigurationType> configurationElement;

        try {
            Object result;
            try (FileInputStream fileInputStream = new FileInputStream(file); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                result = jaxbContext.createUnmarshaller().unmarshal(bufferedInputStream);
            }

            if (result instanceof JAXBElement<?> && ((JAXBElement<?>) result).getValue() instanceof DdsConfigurationType) {
                configurationElement = (JAXBElement<DdsConfigurationType>) result;
            }
            else {
                throw new IllegalArgumentException("Expected DdsConfigurationType from " + file);
            }
        }
        catch (JAXBException | IOException ex) {
            System.err.println("parse: unmarshall error from file " + file + "\n" + ex.getLocalizedMessage());
            throw ex;
        }

        // Return the NSAType object.
        return configurationElement.getValue();
    }

    @SuppressWarnings("unchecked")
    public DocumentType readDocument(String file) throws JAXBException, IOException {
        // Make sure we initialized properly.
        if (jaxbContext == null) {
            throw new JAXBException("readDocument: Failed to load JAXB instance");
        }

        // Parse the specified file.
        JAXBElement<DocumentType> document;
        try {
            Object result;
            try (FileInputStream fileInputStream = new FileInputStream(file); BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
                result = jaxbContext.createUnmarshaller().unmarshal(bufferedInputStream);
            }

            if (result instanceof JAXBElement<?> && ((JAXBElement<?>) result).getValue() instanceof DocumentType) {
                document = (JAXBElement<DocumentType>) result;
            }
            else {
                throw new IllegalArgumentException("Expected DocumentType from " + file);
            }
        }
        catch (JAXBException | IOException ex) {
            System.err.println("parse: unmarshall error from file " + file + "\n" + ex.getLocalizedMessage());
            throw ex;
        }
        // Return the NSAType object.
        return document.getValue();
    }

    @SuppressWarnings("unchecked")
    public void writeDocument(String file, DocumentType document) throws JAXBException, IOException {
        // Make sure we initialized properly.
        if (jaxbContext == null) {
            throw new JAXBException("writeDocument: Failed to load JAXB instance");
        }

        // Parse the specified file.
        JAXBElement<DocumentType> element = factory.createDocument(document);

        File fd = new File(file);
        if (!fd.exists()) {
            System.err.println("Creating file " + fd.getAbsolutePath());
            FileUtils.touch(fd);
        }

        try (FileOutputStream fs = new FileOutputStream(fd)) {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(element, fs);
            fs.flush();
        }
    }

    public Object stringToJaxb(String xml) throws JAXBException {
        // Make sure we initialized properly.
        if (jaxbContext == null) {
            throw new JAXBException("jaxbFromString: Failed to load JAXB PCE API instance");
        }

        // Parse the specified XML string.
        StringReader reader = new StringReader(xml);

        @SuppressWarnings("unchecked")
        JAXBElement<?> jaxbElement = (JAXBElement<?>) jaxbContext.createUnmarshaller().unmarshal(reader);

        // Return the NSAType object.
        return jaxbElement;
    }

    public String jaxbToString(JAXBElement<?> jaxbElement) {

        // Make sure we are given the correct input.
        if (jaxbElement == null) {
            return null;
        }

        // We will write the XML encoding into a string.
        StringWriter writer = new StringWriter();

        try {
            // Marshal the object.
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(jaxbElement, writer);
        } catch (Exception e) {
            // Something went wrong so get out of here.
            System.err.println("jaxbToString: Error marshalling object " + jaxbElement.getClass() + ": " + e.getMessage());
            return null;
        }

        // Return the XML string.
        return writer.toString();
	}

    /**
     * Parse an NML NSA object from the specified string.
     *
     * @param xml String containing the XML formated NSA object.
     * @return A JAXB compiled NSAType object.
     * @throws JAXBException If the XML contained in the string is not valid.
     * @throws JAXBException If the XML is not well formed.
     */
    public NmlNSAType parseNsaFromString(String xml) throws JAXBException {
        // Make sure we initialized properly.
        if (jaxbContext == null) {
            throw new JAXBException("parseNsaFromString: Failed to load JAXB NSA instance");
        }

        // Parse the specified XML string.
        StringReader reader = new StringReader(xml);

        @SuppressWarnings("unchecked")
        JAXBElement<NmlNSAType> nsaElement = (JAXBElement<NmlNSAType>) jaxbContext.createUnmarshaller().unmarshal(reader);

        // Return the NSAType object.
        return nsaElement.getValue();
    }
}