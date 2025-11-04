package com.example.utils.xml.factory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Contém instâncias configuradas e thread-safe das fábricas JAXP.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class XmlFactory {

	/** Instância padrão compartilhada (thread-safe). */
	private static volatile XmlFactory defaultInstance;

	DocumentBuilderFactory documentBuilderFactory;
	TransformerFactory transformerFactory;
	XPathFactory xpathFactory;
	SchemaFactory schemaFactory;

	/**
	 * Retorna a instância padrão, inicializando-a se necessário com configurações padrão.
	 */
	public static XmlFactory getDefaultInstance() {
		if (defaultInstance == null) buildDefaultInstance();
		return defaultInstance;
	}

	@Synchronized
	private static void buildDefaultInstance() {
		if (defaultInstance == null)
			defaultInstance = build(new XmlProperties());
	}

	public static XmlFactory build(XmlProperties props) {
		var parserConfig = props.getParser();
		var security = props.getSecurity();
		var transformerConfig = props.getTransformer();

		// ---- DocumentBuilderFactory ----
		val documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(parserConfig.isNamespaceAware());
		documentBuilderFactory.setCoalescing(parserConfig.isCoalescing());
		documentBuilderFactory.setExpandEntityReferences(parserConfig.isExpandEntityReferences());
		documentBuilderFactory.setIgnoringComments(parserConfig.isIgnoreComments());
		documentBuilderFactory.setIgnoringElementContentWhitespace(parserConfig.isIgnoreWhitespace());
		documentBuilderFactory.setXIncludeAware(parserConfig.isXincludeAware());

		// Segurança
		try {
			documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", parserConfig.isDisableDtd());
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", !parserConfig.isDisableExternalEntities());
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", !parserConfig.isDisableExternalEntities());
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, security.isSecureProcessing());
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, security.getAccessExternalDTD());
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, security.getAccessExternalSchema());
		} catch (Exception ignored) {
			// Alguns parsers não suportam certas features — ignore com segurança
		}

		// ---- XPathFactory ----
		val xpathFactory = XPathFactory.newInstance();
		try {
			xpathFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, security.isSecureProcessing());
		} catch (Exception ignored) {

		}

		// ---- TransformerFactory ----
		val transformerFactory = TransformerFactory.newInstance();
		try {
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, transformerConfig.isSecureProcessing());
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, security.getAccessExternalDTD());
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, security.getAccessExternalSchema());
		} catch (Exception ignored) {

		}

		// ---- SchemaFactory ----
		val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, security.isSecureProcessing());
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, security.getAccessExternalDTD());
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, security.getAccessExternalSchema());
		} catch (Exception ignored) {

		}

		return new XmlFactory(documentBuilderFactory, transformerFactory, xpathFactory, schemaFactory);
	}
}
