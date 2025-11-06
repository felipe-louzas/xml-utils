package com.example.utils.xml.factory;

import java.util.concurrent.atomic.AtomicReference;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.parser.DocumentBuilderXmlParser;
import com.example.utils.xml.parser.XmlParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * Contém instâncias configuradas e thread-safe das fábricas JAXP.
 * <p>
 * Ver <a href="https://docs.oracle.com/en/java/javase/17/security/java-api-xml-processing-jaxp-security-guide.html">Java API for XML
 * Processing (JAXP) Security Guide</a> para referência de práticas de segurança ao utilizar JAXP.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlFactory {

	/* Instância padrão compartilhada (thread-safe). */
	private static final AtomicReference<XmlFactory> DEFAULT_INSTANCE = new AtomicReference<>(null);

	XmlConfig config;
	XmlParser.Builder parserBuilder;

	/* Lazy-init thread-safe services */
	@NonFinal
	XmlParser parser;

	@NonFinal
	SchemaFactory schemaFactory;

	@NonFinal
	XPathFactory xpathFactory;

	@NonFinal
	TransformerFactory transformerFactory;

	/**
	 * Retorna a instância padrão, inicializando-a se necessário com configurações padrão.
	 */
	public static XmlFactory getDefaultInstance() {
		if (DEFAULT_INSTANCE.get() == null) {
			val defaultConfig = XmlConfig.builder().build();
			setDefaultInstance(XmlFactory.withConfig(defaultConfig));
		}
		return DEFAULT_INSTANCE.get();
	}

	/**
	 * Inicializa explicitamente a instância padrão com as propriedades fornecidas.
	 */
	public static void setDefaultInstance(@NonNull XmlFactory factory) {
		DEFAULT_INSTANCE.compareAndSet(null, factory);
	}

	/**
	 * Cria uma nova instância independente de XmlFactory com as propriedades fornecidas (não afeta a instância padrão).
	 */
	public static XmlFactory withConfig(@NonNull XmlConfig props) {
		return new XmlFactory(props, DocumentBuilderXmlParser::new);
	}

	// ---- Use factory resources ---

	public XmlParser getParser() {
		return ObjectUtils.getIfNull(parser, this::initXmlParser);
	}

	private SchemaFactory getSchemaFactory() {
		return ObjectUtils.getIfNull(schemaFactory, this::initSchemaFactory);
	}

	private XPathFactory getXPathFactory() {
		return ObjectUtils.getIfNull(xpathFactory, this::initXPathFactory);
	}

	private TransformerFactory getTransformerFactory() {
		return ObjectUtils.getIfNull(transformerFactory, this::initTransformerFactory);
	}


	// ---- Lazy init methods ----

	@Synchronized
	private XmlParser initXmlParser() {
		if (parser == null) {
			val parserConfig = config.getParser();
			val securityConfig = config.getSecurity();

			val documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(parserConfig.isNamespaceAware());
			documentBuilderFactory.setCoalescing(parserConfig.isCoalescing());
			documentBuilderFactory.setExpandEntityReferences(parserConfig.isExpandEntityReferences());
			documentBuilderFactory.setIgnoringComments(parserConfig.isIgnoreComments());
			documentBuilderFactory.setIgnoringElementContentWhitespace(parserConfig.isIgnoreWhitespace());
			documentBuilderFactory.setXIncludeAware(parserConfig.isXincludeAware());

			// FSP e EAPs
			tryQuietly(documentBuilderFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, securityConfig.isSecureProcessing());
			tryQuietly(documentBuilderFactory::setAttribute, XMLConstants.ACCESS_EXTERNAL_DTD, securityConfig.getProtocolsExternalDTD());
			tryQuietly(documentBuilderFactory::setAttribute, XMLConstants.ACCESS_EXTERNAL_SCHEMA, securityConfig.getProtocolsExternalSchema());

			// Configurações de compatibilidade com parsers de terceiros
			tryQuietly(documentBuilderFactory::setFeature, XmlConstantsCompat.DISALLOW_DTD, securityConfig.isDisableDtd());
			tryQuietly(documentBuilderFactory::setFeature, XmlConstantsCompat.LOAD_EXTERNAL_DTD, securityConfig.isAllowExternalEntities());
			tryQuietly(documentBuilderFactory::setFeature, XmlConstantsCompat.LOAD_EXTERNAL_GENERAL_ENTITIES, securityConfig.isAllowExternalEntities());
			tryQuietly(documentBuilderFactory::setFeature, XmlConstantsCompat.LOAD_EXTERNAL_PARAMETER_ENTITIES, securityConfig.isAllowExternalEntities());

			parser = parserBuilder.build(documentBuilderFactory);
		}

		return parser;
	}

	@Synchronized
	private SchemaFactory initSchemaFactory() {
		if (schemaFactory == null) {
			val securityConfig = config.getSecurity();

			schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			tryQuietly(schemaFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, securityConfig.isSecureProcessing());
			tryQuietly(schemaFactory::setProperty, XMLConstants.ACCESS_EXTERNAL_DTD, securityConfig.getProtocolsExternalDTD());
			tryQuietly(schemaFactory::setProperty, XMLConstants.ACCESS_EXTERNAL_SCHEMA, securityConfig.getProtocolsExternalSchema());

			tryQuietly(schemaFactory::setFeature, XmlConstantsCompat.DISALLOW_DTD, securityConfig.isDisableDtd());
		}
		return schemaFactory;
	}

	@Synchronized
	private XPathFactory initXPathFactory() {
		if (xpathFactory == null) {
			val securityConfig = config.getSecurity();

			xpathFactory = XPathFactory.newInstance();
			tryQuietly(xpathFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, securityConfig.isSecureProcessing());
		}
		return xpathFactory;
	}

	// ---- Init methods (Synchronized para garantir que apenas um thread inicialize o factory) ----

	@Synchronized
	private TransformerFactory initTransformerFactory() {
		if (transformerFactory == null) {
			val securityConfig = config.getSecurity();

			transformerFactory = TransformerFactory.newInstance();
			tryQuietly(transformerFactory::setFeature, XMLConstants.FEATURE_SECURE_PROCESSING, securityConfig.isSecureProcessing());
			tryQuietly(transformerFactory::setAttribute, XMLConstants.ACCESS_EXTERNAL_DTD, securityConfig.getProtocolsExternalDTD());
			tryQuietly(transformerFactory::setAttribute, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, securityConfig.getProtocolsExternalStylesheet());
		}
		return transformerFactory;
	}

	private <T> void tryQuietly(FailableBiConsumer<String, T, ? extends Exception> setter, String option, T value) {
		try {
			setter.accept(option, value);
		} catch (Exception ex) {
			log.warn("Could not set XML option '{}' to value '{}': {}", option, value, ex.getMessage());
		}
	}
}
