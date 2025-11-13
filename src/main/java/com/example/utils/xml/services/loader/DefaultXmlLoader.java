package com.example.utils.xml.services.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.exceptions.XmlException;
import com.example.utils.xml.services.context.XmlContext;
import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.providers.XmlProviders;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.FailableFunction;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Concrete XmlLoader implementation. It uses a thread-safe XmlFactory to create a per-call DocumentBuilder and parse XML from various
 * sources. The loader can use the global default XmlFactory or a specific factory/config provided by the user. Instances are lightweight
 * and reusable by multiple threads, but the returned Xml objects are not thread-safe.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class DefaultXmlLoader implements XmlLoader {

	@Getter
	XmlProviders providers;

	@Override
	public XmlConfig getConfig() {
		return providers.getConfig();
	}

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
		return createXmlInstance(document);
	}

	private <E extends Exception> XmlDocument parseWithBuilder(String source, FailableFunction<DocumentBuilder, Document, E> parser) {
		try {
			val document = providers.getDocumentBuilderProvider().parse(parser);
			return createXmlInstance(document);
		} catch (Exception ex) {
			log.debug("Failed to parse XML from {}: {}", source, ex.getMessage());
			throw new XmlException("Failed to parse XML from " + source, ex);
		}
	}

	private XmlDocument createXmlInstance(@NonNull Document document) {
		val context = new XmlContext(providers, document);
		return new XmlDocument(context);
	}
}
