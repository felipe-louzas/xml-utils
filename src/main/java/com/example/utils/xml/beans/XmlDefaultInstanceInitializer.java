package com.example.utils.xml.beans;

import com.example.utils.xml.Xml;
import com.example.utils.xml.services.loader.XmlLoader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlDefaultInstanceInitializer {
	XmlLoader defaultLoader;

	@PostConstruct
	public void initialize() {
		log.debug("Inicializando XML Utils com beans gerenciado pelo Spring...");
		Xml.initialize(defaultLoader);
	}

	@PreDestroy
	public void destroy() {
		log.debug("Limpando configurações do XML Utils antes de destruir o bean gerenciado pelo Spring...");
		Xml.reset();
	}
}