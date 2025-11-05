package com.example.utils.xml.config;

import com.example.utils.xml.factory.XmlFactory;
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
public class XmlFactoryInitializer {
	XmlConfig config;

	@PostConstruct
	public void initialize() {
		log.debug("Inicializando XmlFactory com o XmlConfig gerenciado pelo Spring...");
		XmlFactory.initDefaultInstance(config);
	}
}