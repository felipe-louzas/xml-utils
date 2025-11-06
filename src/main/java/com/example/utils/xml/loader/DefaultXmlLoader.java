package com.example.utils.xml.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.xml.Xml;
import com.example.utils.xml.context.XmlContext;
import com.example.utils.xml.exceptions.XmlException;
import com.example.utils.xml.factory.XmlFactory;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
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

	private static volatile DefaultXmlLoader defaultInstance;

	XmlFactory factory;

	public static XmlLoader getDefaultInstance() {
		return ObjectUtils.getIfNull(defaultInstance, DefaultXmlLoader::initDefaultInstance);
	}

	@Synchronized
	private static DefaultXmlLoader initDefaultInstance() {
		if (defaultInstance == null) {
			defaultInstance = new DefaultXmlLoader(XmlFactory.getDefaultInstance());
		}
		return defaultInstance;
	}

	@Override
	public XmlLoader withFactory(@NonNull XmlFactory xmlFactory) {
		return new DefaultXmlLoader(xmlFactory);
	}

	@Override
	public Xml fromString(@NonNull CharSequence xml) {
		return parseWithBuilder("string", builder -> builder.parse(new InputSource(new StringReader(xml.toString()))));
	}

	@Override
	public Xml fromFile(@NonNull File file) {
		return parseWithBuilder("file: " + file, builder -> builder.parse(file));
	}

	@Override
	public Xml fromPath(@NonNull Path path) {
		return parseWithBuilder("path: " + path, builder -> {
			try (val inputStream = Files.newInputStream(path)) {
				return builder.parse(inputStream);
			}
		});
	}

	@Override
	public Xml fromInputStream(@NonNull InputStream inputStream) {
		return parseWithBuilder("stream", builder -> builder.parse(inputStream));
	}

	@Override
	public Xml fromReader(@NonNull Reader reader) {
		return parseWithBuilder("reader", builder -> builder.parse(new InputSource(reader)));
	}

	@Override
	public Xml fromDocument(@NonNull Document document) {
		return createXmlInstance(factory, document);
	}

	private <E extends Exception> Xml parseWithBuilder(String source, FailableFunction<DocumentBuilder, Document, E> parser) {
		try {
			val document = factory.documentBuilder().apply(parser);
			return createXmlInstance(factory, document);
		} catch (Exception ex) {

		}

		val provider = fac.getDocumentBuilderProvider();
		DocumentBuilder builder = null;
		try {
			builder = provider.acquire();
			val doc = action.parse(builder);
			return wrap(fac, doc);
		} catch (Exception e) {
			log.debug("XML parse failed on operation '{}': {}", op, e.getMessage());
			throw new XmlException("Failed to parse XML from " + op, e);
		} finally {
			provider.release(builder);
		}
	}

	private Xml createXmlInstance(XmlFactory factory, @NonNull Document document) {
		val context = new XmlContext(factory, document);
		return new Xml(context);
	}
}
