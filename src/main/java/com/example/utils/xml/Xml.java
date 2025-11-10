package com.example.utils.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.providers.XmlProviders;
import com.example.utils.xml.providers.document.XmlDocument;
import com.example.utils.xml.providers.loader.XmlLoader;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

/**
 * The main entry point for the XML utility library.
 * <p>
 * This class acts as a wrapper for a standard {@link Document} object and provides fluent access to validation, formatting, and XPath
 * evaluation.
 * <p>
 * An instance of this class is intended to be used by a single thread and is not thread-safe, as it holds a mutable {@link Document}
 * object. However, the static methods used to create {@link Xml} instances are thread-safe.
 *
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Xml {

	/* ------ Initialize with custom config  ------ */

	public static void initialize(XmlConfig config) {
		log.debug("Inicializando provedores padrão do XML Utils com configuração personalizada...");
		XmlProviders.initializeProviders(() -> XmlProviders.of(config));
	}

	public static XmlProviders config(XmlConfig config) {
		return XmlProviders.of(config);
	}

	/* ------ Convenience loader delegates  ------ */

	public static XmlDocument load(Document document) {
		return loader().fromDocument(document);
	}

	public static XmlDocument load(CharSequence xmlContent) {
		return loader().fromString(xmlContent);
	}

	public static XmlDocument load(File file) {
		return loader().fromFile(file);
	}

	public static XmlDocument load(Path path) {
		return loader().fromPath(path);
	}

	public static XmlDocument load(InputStream inputStream) {
		return loader().fromInputStream(inputStream);
	}

	public static XmlDocument load(Reader reader) {
		return loader().fromReader(reader);
	}

	public static XmlLoader loader() {
		return XmlProviders.getDefaultProviders().getXmlLoader();
	}

	public static XmlLoader loader(XmlConfig config) {
		return config(config).getXmlLoader();
	}

	/* ------ Validation  ------ */
	/*
	public XmlValidator validator() {
		return context.getXmlValidator();
	}
	*/

	/* ------ Formatting  ------ */
	/*
	public String toFormattedString() {
		return formatter().toString();
	}

	@Override
	public String toString() {
		return formatter().toString(false);
	}

	public XmlFormatter formatter() {
		return context.getXmlFormatter();
	}
	*/
}
