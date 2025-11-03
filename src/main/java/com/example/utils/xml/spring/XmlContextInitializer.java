package com.example.utils.xml.spring;

import com.example.utils.xml.context.XmlContext;
import com.example.utils.xml.context.XmlContextProperties;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A Spring component responsible for automatically initializing the {@link XmlContext} with the Spring-managed {@link XmlContextProperties}
 * bean at application startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlContextInitializer {
	XmlContextProperties properties;

	@PostConstruct
	public void initialize() {
		log.info("Initializing XmlContext with Spring-managed XmlContextProperties bean...");
		XmlContext.initDefaultContext(properties);
	}
}