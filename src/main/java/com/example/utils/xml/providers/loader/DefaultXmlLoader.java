package com.example.utils.xml.providers.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.xml.context.XmlContext;
import com.example.utils.xml.exceptions.XmlException;
import com.example.utils.xml.providers.document.XmlDocument;
import com.example.utils.xml.providers.parser.DocumentBuilderProvider;
import lombok.AccessLevel;
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

	DocumentBuilderProvider provider;

	@Override
	public XmlDocument fromString(@NonNull CharSequence xml) {
		return parseWithBuilder("string", builder -> builder.parse(new InputSource(new StringReader(xml.toString()))));
	}

	@Override
	public XmlDocument fromFile(@NonNull File file) {
		return parseWithBuilder("file: " + file, builder -> builder.parse(file));
	}

	@Override
	public XmlDocument fromPath(@NonNull Path path) {
		return parseWithBuilder("path: " + path, builder -> {
			try (val inputStream = Files.newInputStream(path)) {
				return builder.parse(inputStream);
			}
		});
	}

	@Override
	public XmlDocument fromInputStream(@NonNull InputStream inputStream) {
		return parseWithBuilder("stream", builder -> builder.parse(inputStream));
	}

	@Override
	public XmlDocument fromReader(@NonNull Reader reader) {
		return parseWithBuilder("reader", builder -> builder.parse(new InputSource(reader)));
	}

	@Override
	public XmlDocument fromDocument(@NonNull Document document) {
		return createXmlInstance(document);
	}

	private <E extends Exception> XmlDocument parseWithBuilder(String source, FailableFunction<DocumentBuilder, Document, E> parser) {
		try {
			val document = provider.parse(parser);
			return createXmlInstance(document);
		} catch (Exception ex) {
			log.debug("Failed to parse XML from {}: {}", source, ex.getMessage());
			throw new XmlException("Failed to parse XML from " + source, ex);
		}
	}

	private XmlDocument createXmlInstance(@NonNull Document document) {
		val context = new XmlContext(document);
		return new XmlDocument(context);
	}
}
