package com.example.utils.xml.services.document.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.xml.exceptions.XmlException;
import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.document.parser.DocumentBuilderProvider;
import com.example.utils.xml.services.validation.XmlValidator;
import com.example.utils.xml.services.xpath.provider.XPathEvaluatorProvider;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.FailableFunction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * Concrete XmlLoader implementation. It uses a thread-safe XmlFactory to create a per-call DocumentBuilder and parse XML from various
 * sources. The loader can use the global default XmlFactory or a specific factory/config provided by the user. Instances are lightweight
 * and reusable by multiple threads, but the returned Xml objects are not thread-safe.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DefaultXmlLoader implements XmlLoader {

	XmlValidator xmlValidator;
	DocumentBuilderProvider documentBuilderProvider;
	XPathEvaluatorProvider xPathEvaluatorProvider;

	@Override
	public XmlDocument load(@NonNull CharSequence xml) {
		return parseWithBuilder("string", builder -> builder.parse(new InputSource(new StringReader(xml.toString()))));
	}

	@Override
	public XmlDocument load(@NonNull File file) {
		return parseWithBuilder("file: " + file, builder -> builder.parse(file));
	}

	@Override
	public XmlDocument load(@NonNull Path path) {
		return parseWithBuilder("path: " + path, builder -> {
			try (val inputStream = Files.newInputStream(path)) {
				return builder.parse(inputStream);
			}
		});
	}

	@Override
	public XmlDocument load(@NonNull InputStream inputStream) {
		return parseWithBuilder("stream", builder -> builder.parse(inputStream));
	}

	@Override
	public XmlDocument load(@NonNull Reader reader) {
		return parseWithBuilder("reader", builder -> builder.parse(new InputSource(reader)));
	}

	@Override
	public XmlDocument load(@NonNull Document document) {
		return createXmlDocument(document);
	}

	private <E extends Exception> XmlDocument parseWithBuilder(String source, FailableFunction<DocumentBuilder, Document, E> parser) {
		try {
			val document = documentBuilderProvider.parse(parser);
			return createXmlDocument(document);

		} catch (SAXParseException ex) {
			log.error("SAXParseException ao criar XmlDocument a partir de {}: '{}' na linha {}, coluna {}",
				source, ex.getMessage(), ex.getLineNumber(), ex.getColumnNumber());

			val message = String.format("Erro de sintaxe no documento XML na linha %d, coluna %d.", ex.getLineNumber(), ex.getColumnNumber());
			throw new XmlException(message, ex);

		} catch (Exception ex) {
			log.error("Erro ao criar XmlDocument a partir de {}", source, ex);
			throw new XmlException("Não foi possível processar o documento XML. Verifique é um documento XML válido.", ex);
		}
	}

	private XmlDocument createXmlDocument(@NonNull Document document) {
		return new XmlDocument(document, xmlValidator, xPathEvaluatorProvider);
	}
}
