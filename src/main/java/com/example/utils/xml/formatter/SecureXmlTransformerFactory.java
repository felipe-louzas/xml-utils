package com.example.utils.xml.formatter;

import com.example.xml.config.XmlProperties;
import com.example.xml.exceptions.XmlRuntimeException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Creates securely configured Transformer instances for formatting (pretty-printing).
 * TransformerFactory is thread-safe.
 * Transformer is *not* thread-safe.
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecureXmlTransformerFactory {

    TransformerFactory tf;
    XmlProperties.Formatter formatterProps;

    public SecureXmlTransformerFactory(XmlProperties properties) {
        this.formatterProps = properties.getFormatter();
        this.tf = TransformerFactory.newInstance();
        try {
            // Secure the factory against external entity resolution
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            // Factory might not support these attributes, but we try.
        }
    }

    /**
     * Gets a new, non-thread-safe Transformer configured for formatting.
     */
    public Transformer getFormattingTransformer() {
        try {
            // TransformerFactory.newTransformer() is thread-safe
            Transformer transformer = tf.newTransformer();

            // Configure formatting properties
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    formatterProps.isOmitXmlDeclaration() ? "yes" : "no");
            transformer.setOutputProperty(OutputKeys.INDENT,
                    formatterProps.isIndent() ? "yes" : "no");
            if (formatterProps.isIndent()) {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                        String.valueOf(formatterProps.getIndentAmount()));
            }
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new XmlRuntimeException("Failed to create formatting Transformer", e);
        }
    }
}
