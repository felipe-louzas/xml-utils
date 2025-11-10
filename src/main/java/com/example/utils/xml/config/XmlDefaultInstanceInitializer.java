package com.example.utils.xml.config;

import com.example.utils.xml.providers.XmlProviders;
import com.example.utils.xml.providers.factory.XmlFactory;
import com.example.utils.xml.providers.loader.XmlLoader;
import jakarta.annotation.PostConstruct;
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
	XmlFactory defaultFactory;
	XmlLoader defaultLoader;

	@PostConstruct
	public void initialize() {
		log.debug("Inicializando provedores padrão do XML Utils com beans gerenciado pelo Spring...");
		XmlProviders.initializeProviders(() -> new XmlProviders(
			defaultFactory,
			defaultLoader));
	}
}