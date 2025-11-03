package com.example.utils.xml.parser;

import com.example.xml.config.XmlProperties;
import com.example.xml.exceptions.XmlRuntimeException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Creates securely configured DocumentBuilder instances.
 * This is a Spring Service, configured by XmlProperties.
 * It is thread-safe. DocumentBuilderFactory is thread-safe.
 * DocumentBuilder is *not* thread-safe, so we create a new one per request.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SecureXmlParserFactory {

    XmlProperties properties;
    DocumentBuilderFactory dbf;

    /**
     * Constructor initializes the thread-safe DocumentBuilderFactory
     * with secure defaults from properties.
     */
    public SecureXmlParserFactory(XmlProperties properties) {
        this.properties = properties;
        this.dbf = DocumentBuilderFactory.newInstance();
        configureSecurity();
    }

    private void configureSecurity() {
        try {
            var parserProps = properties.getParser();
            dbf.setNamespaceAware(parserProps.isNamespaceAware());
            
            // Secure Defaults: Disable DTDs and External Entities to prevent XXE
            if (parserProps.isDisableDtd()) {
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                log.debug("XML Parser: DOCTYPE declarations disabled.");
            }
            if (parserProps.isDisableExternalEntities()) {
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                dbf.setExpandEntityReferences(false);
                log.debug("XML Parser: External entity loading disabled.");
            }
        } catch (ParserConfigurationException e) {
            log.error("Unable to configure secure XML parser factory", e);
            throw new XmlRuntimeException("Failed to initialize secure DocumentBuilderFactory", e);
        }
    }

    /**
     * Gets a new, non-thread-safe DocumentBuilder for a single use.
     */
    private DocumentBuilder getDocumentBuilder() {
        try {
            // DocumentBuilderFactory.newDocumentBuilder() is thread-safe
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlRuntimeException("Failed to create new DocumentBuilder", e);
        }
    }

    /**
     * Parses an XML string into a DOM Document.
     */
    public Document parse(String xmlString) {
        try (var reader = new StringReader(xmlString)) {
            var source = new InputSource(reader);
            return getDocumentBuilder().parse(source);
        } catch (Exception e) {
            throw new XmlRuntimeException("Failed to parse XML string", e);
        }
    }

    /**
     * Parses an InputStream into a DOM Document.
     */
    public Document parse(InputStream inputStream) {
        try {
            var source = new InputSource(inputStream);
            return getDocumentBuilder().parse(source);
        } catch (Exception e) {
            throw new XmlRuntimeException("Failed to parse XML from InputStream", e);
        } finally {
            // We do not close the input stream, the caller must do that.
        }
    }

    /**
     * Parses an XML file into a DOM Document.
     */
    public Document parse(File file) {
        try {
            return getDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new XmlRuntimeException("Failed to parse XML file: " + file.getPath(), e);
        }
    }
    
    /**
     * Creates a new, empty DOM Document.
     */
    public Document newDocument() {
        return getDocumentBuilder().newDocument();
    }
}
