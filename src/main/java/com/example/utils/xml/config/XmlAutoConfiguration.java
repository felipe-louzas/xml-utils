package com.example.utils.xml.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuração do módulo XML Utils para Spring Boot.
 * <p>
 * Esta configuração cria um bean {@link XmlConfig} com base nas propriedades definidas no arquivo de propriedades do Spring Boot
 * (application.properties ou application.yml) sob o prefixo "utils.xml".
 */
@Configuration
@EnableConfigurationProperties
public class XmlAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "utils.xml")
	public DefaultXmlConfig xmlConfig() {
		return new DefaultXmlConfig();
	}
}
