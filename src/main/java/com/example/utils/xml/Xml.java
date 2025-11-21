package com.example.utils.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.patterns.LazyInitSingleton;
import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.XmlService;
import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.document.loader.XmlLoader;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

@Slf4j
@UtilityClass
public final class Xml {

	private final LazyInitSingleton<XmlService> DEFAULT_INSTANCE = LazyInitSingleton.of(Xml::buildDefaultInstance);

	/* ------ Default instance initialization and configuration ------ */

	public void initialize(@NonNull XmlService service) {
		log.debug("Inicializando XML Utils com serviços customizados...");
		DEFAULT_INSTANCE.set(service);
	}

	public void initialize(@NonNull XmlConfig config) {
		log.debug("Inicializando XML Utils com configuração customizada...");
		DEFAULT_INSTANCE.set(XmlService.withConfig(config));
	}

	public void reset() {
		log.debug("Limpando configurações do XML Utils");
		DEFAULT_INSTANCE.reset();
	}

	private XmlService buildDefaultInstance() {
		log.debug("Inicializando XML Utils com configuração padrão...");
		return XmlService.withConfig(XmlConfig.getDefault());
	}

	/* ------ Convenience default instance delegates  ------ */

	public XmlService getService() {
		return DEFAULT_INSTANCE.get();
	}

	public XmlConfig getConfig() {
		return getService().getConfig();
	}

	public XmlLoader getLoader() {
		return getService().getLoader();
	}

	public XmlDocument load(Document document) {
		return getLoader().load(document);
	}

	public XmlDocument load(CharSequence xmlContent) {
		return getLoader().load(xmlContent);
	}

	public XmlDocument load(File file) {
		return getLoader().load(file);
	}

	public XmlDocument load(Path path) {
		return getLoader().load(path);
	}

	public XmlDocument load(InputStream inputStream) {
		return getLoader().load(inputStream);
	}

	public XmlDocument load(Reader reader) {
		return getLoader().load(reader);
	}

	/* ------ Custom instances  ------ */

	public XmlService withConfig(XmlConfig config) {
		return XmlService.withConfig(config);
	}

	public XmlLoader getLoader(XmlConfig config) {
		return XmlService.withConfig(config).getLoader();
	}
}


