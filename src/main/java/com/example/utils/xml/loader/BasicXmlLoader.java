package com.example.utils.xml.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.parsers.DocumentBuilderFactory;

import com.example.utils.xml.Xml;
import com.example.utils.xml.exceptions.XmlException;
import com.example.utils.xml.parser.DocumentParser;
import lombok.val;
import org.w3c.dom.Document;

/**
 * A builder-style class for loading and parsing XML documents. This class allows for configuration of the parser before loading the XML
 * from a specific source (e.g., String, File, InputStream).
 * <p>
 * It creates a new, securely-configured {@link DocumentParser} internally for each parsing operation.
 *
 * @author Gemini
 */
public final class BasicXmlLoader implements XmlLoader {

	/* Cache a lazy-init XmlBuilder instance with default config for default parse methods */
	private static final AtomicReference<BasicXmlLoader> defaultLoaderRef = new AtomicReference<>();

	private boolean namespaceAware = true;
	private boolean validating = false;
	private boolean expandEntityReferences = false;

	/**
	 * Package-private constructor. Use {@link Xml#read()}.
	 */
	public BasicXmlLoader() {
		// Default settings are applied
	}

	public static BasicXmlLoader getDefaultLoader() {

		val defaultLoader = defaultLoaderRef.get();
		if (defaultLoader != null) return defaultLoader;

		val newBuilder = new BasicXmlLoader();
		if (defaultLoaderRef.compareAndSet(null, newBuilder)) {
			return newBuilder;
		} else {
			/* Set by another thread */
			return defaultLoaderRef.get();
		}

	}

	public static XmlLoader getDefaultInstance() {
		return null;
	}

	/**
	 * Configures the parser to be namespace-aware (default: true).
	 *
	 * @param isAware true to enable namespace awareness, false to disable.
	 * @return This {@link BasicXmlLoader} instance for chaining.
	 */
	public BasicXmlLoader withNamespaceAware(boolean isAware) {
		this.namespaceAware = isAware;
		return this;
	}

	/**
	 * Configures the parser to validate the document (default: false). Note: This enables DTD validation, not XSD validation.
	 *
	 * @param isValidating true to enable DTD validation, false to disable.
	 * @return This {@link BasicXmlLoader} instance for chaining.
	 */
	public BasicXmlLoader withDtdValidation(boolean isValidating) {
		this.validating = isValidating;
		return this;
	}

	/**
	 * Configures the parser to expand entity references (default: false). For security, the default is false to help prevent XXE attacks.
	 *
	 * @param expandEntities true to enable entity expansion, false to disable.
	 * @return This {@link BasicXmlLoader} instance for chaining.
	 */
	public BasicXmlLoader withEntityReferenceExpansion(boolean expandEntities) {
		this.expandEntityReferences = expandEntities;
		return this;
	}

	/**
	 * Parses an XML document from a String.
	 *
	 * @param xmlContent The XML content as a string.
	 * @return A new {@link Xml} instance.
	 * @throws XmlException if parsing fails.
	 */
	public Xml fromString(CharSequence xmlContent) {
		DocumentParser parser = new DocumentParser(buildFactory());
		try (StringReader reader = new StringReader(xmlContent)) {
			Document doc = parser.parse(reader);
			return new Xml(doc);
		} catch (Exception e) {
			throw new XmlException("Failed to parse XML from string", e);
		}
	}

	/**
	 * Parses an XML document from a {@link File}.
	 *
	 * @param file The file containing the XML content.
	 * @return A new {@link Xml} instance.
	 * @throws XmlException if parsing fails or the file cannot be read.
	 */
	public Xml fromFile(File file) {
		DocumentParser parser = new DocumentParser(buildFactory());
		try {
			Document doc = parser.parse(file);
			return new Xml(doc);
		} catch (Exception e) {
			throw new XmlException("Failed to parse XML from file: " + file.getPath(), e);
		}
	}

	/**
	 * Parses an XML document from an {@link InputStream}. The stream will be closed by the underlying parser.
	 *
	 * @param inputStream The input stream containing the XML content.
	 * @return A new {@link Xml} instance.
	 * @throws XmlException if parsing fails or the stream cannot be read.
	 */
	public Xml fromInputStream(InputStream inputStream) {
		DocumentParser parser = new DocumentParser(buildFactory());
		try {
			// The parser.parse() method closes the stream
			Document doc = parser.parse(inputStream);
			return new Xml(doc);
		} catch (Exception e) {
			throw new XmlException("Failed to parse XML from input stream", e);
		}
	}

	/**
	 * Internal method to build a securely configured factory based on the builder's settings.
	 */
	private DocumentBuilderFactory buildFactory() {
		return DocumentParser.createSecureFactory(namespaceAware, validating, expandEntityReferences);
	}

	/* ----------- Load / Parse (terminal) ----------- */

	Xml fromString(CharSequence xml);

	Xml fromFile(File file);

	Xml fromPath(Path path);

	Xml fromInputStream(InputStream inputStream);

	Xml fromReader(Reader reader);

	Xml fromUrl(URL url);

	Xml fromSource(Source source);

	/**
	 * Wrap an existing {@link Document} inside the library's Xml wrapper without reparsing.
	 */
	Xml fromDocument(Document document);

}
