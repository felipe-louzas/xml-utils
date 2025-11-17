package com.example.utils.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.patterns.LazyInitSingleton;
import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.document.loader.XmlLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Xml {

	private static final LazyInitSingleton<Xml> DEFAULT_INSTANCE = LazyInitSingleton.of(Xml::buildDefaultInstance);

	@Getter(AccessLevel.PRIVATE)
	XmlLoader loader;

	/* ------ Default instance initialization and configuration ------ */

	public static void initialize(@NonNull XmlLoader loader) {
		DEFAULT_INSTANCE.set(new Xml(loader));
	}

	public static void configure(@NonNull XmlConfig config) {
		log.debug("Inicializando XML Utils com configuração customizada...");
		DEFAULT_INSTANCE.set(new Xml(XmlLoader.withConfig(config)));
	}

	public static void reset() {
		log.debug("Limpando configurações do XML Utils");
		DEFAULT_INSTANCE.reset();
	}

	private static Xml buildDefaultInstance() {
		log.debug("Inicializando XML Utils com configuração padrão...");
		return new Xml(XmlLoader.withConfig(XmlConfig.getDefault()));
	}

	private static Xml getDefaultInstance() {
		return DEFAULT_INSTANCE.get();
	}

	/* ------ Convenience default instance delegates  ------ */

	public static XmlLoader loader() {
		return getDefaultInstance().getLoader();
	}

	public static XmlDocument load(Document document) {
		return loader().load(document);
	}

	public static XmlDocument load(CharSequence xmlContent) {
		return loader().load(xmlContent);
	}

	public static XmlDocument load(File file) {
		return loader().load(file);
	}

	public static XmlDocument load(Path path) {
		return loader().load(path);
	}

	public static XmlDocument load(InputStream inputStream) {
		return loader().load(inputStream);
	}

	public static XmlDocument load(Reader reader) {
		return loader().load(reader);
	}

	public static XmlConfig getConfig() {
		return loader().getConfig();
	}

	/* ------ Create instance with independent configuration  ------ */

	public static XmlLoader loader(XmlConfig config) {
		return new Xml(XmlLoader.withConfig(config)).getLoader();
	}
}
