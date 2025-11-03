package com.example.utils.xml.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.example.utils.xml.exceptions.XmlException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A thread-safe, secure XML document parser. This class is immutable and holds a configured {@link DocumentBuilderFactory}. The factory is
 * used to create new, non-thread-safe {@link DocumentBuilder} instances for each parse operation, making the parse methods thread-safe.
 *
 * @author Gemini
 */
public final class DocumentParser {

	private final DocumentBuilderFactory factory;

	/**
	 * Constructs a parser with a specific factory.
	 *
	 * @param factory A pre-configured, thread-safe DocumentBuilderFactory.
	 */
	public DocumentParser(DocumentBuilderFactory factory) {
		this.factory = factory;
	}

	/**
	 * Creates a new {@link DocumentBuilderFactory} with security-first defaults.
	 *
	 * @param namespaceAware Configures namespace awareness.
	 * @param validating Configures DTD validation.
	 * @param expandEntities Configures entity reference expansion.
	 * @return A new, securely configured factory.
	 */
	public static DocumentBuilderFactory createSecureFactory(boolean namespaceAware, boolean validating, boolean expandEntities) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(namespaceAware);
			dbf.setValidating(validating);

			//
			// === Secure Processing (OWASP XXE Prevention) ===
			//
			// Disable DTDs completely to prevent XXE.
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

			// Disable external general entities.
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);

			// Disable external parameter entities.
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

			// Disable external DTDs.
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			// Set XInclude processing to false.
			dbf.setXIncludeAware(false);

			// And finally, set expand entity references, controlled by user.
			dbf.setExpandEntityReferences(expandEntities);

			// Use the JAXP 1.5 feature for secure processing.
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			return dbf;
		} catch (ParserConfigurationException e) {
			throw new XmlException("Failed to configure secure DocumentBuilderFactory", e);
		}
	}

	/**
	 * Gets a new {@link DocumentBuilder} from the factory. This is thread-safe as the factory is thread-safe.
	 *
	 * @return A new DocumentBuilder.
	 * @throws ParserConfigurationException if configuration fails.
	 */
	private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return factory.newDocumentBuilder();
	}

	/**
	 * Parses content from a {@link Reader}.
	 *
	 * @param reader The reader (e.g., StringReader).
	 * @return The parsed {@link Document}.
	 * @throws SAXException if parsing fails.
	 * @throws IOException if an I/O error occurs.
	 */
	public Document parse(Reader reader) throws SAXException, IOException {
		try {
			return newDocumentBuilder().parse(new InputSource(reader));
		} catch (ParserConfigurationException e) {
			throw new XmlException("Failed to create DocumentBuilder", e);
		}
	}

	/**
	 * Parses content from a {@link File}.
	 *
	 * @param file The file to parse.
	 * @return The parsed {@link Document}.
	 * @throws SAXException if parsing fails.
	 * @throws IOException if an I/O error occurs.
	 */
	public Document parse(File file) throws SAXException, IOException {
		try {
			return newDocumentBuilder().parse(file);
		} catch (ParserConfigurationException e) {
			throw new XmlException("Failed to create DocumentBuilder", e);
		}
	}

	/**
	 * Parses content from an {@link InputStream}. This method closes the stream.
	 *
	 * @param stream The stream to parse.
	 * @return The parsed {@link Document}.
	 * @throws SAXException if parsing fails.
	 * @throws IOException if an I/O error occurs.
	 */
	public Document parse(InputStream stream) throws SAXException, IOException {
		try {
			// The parse method on DocumentBuilder closes the input stream.
			return newDocumentBuilder().parse(stream);
		} catch (ParserConfigurationException e) {
			throw new XmlException("Failed to create DocumentBuilder", e);
		}
	}
}
