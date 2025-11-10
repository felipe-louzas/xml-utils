package com.example.utils.xml.config;

import com.example.utils.xml.config.constants.ConfigKeys;
import com.example.utils.xml.providers.factory.DefaultXmlFactory;
import com.example.utils.xml.providers.factory.XmlFactory;
import com.example.utils.xml.providers.loader.DefaultXmlLoader;
import com.example.utils.xml.providers.loader.XmlLoader;
import com.example.utils.xml.providers.parser.DocumentBuilderProvider;
import com.example.utils.xml.providers.parser.FactoryDocumentBuilderProvider;
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
	@ConfigurationProperties(prefix = ConfigKeys.PROP_PREFIX)
	public XmlConfig xmlConfig() {
		return new DefaultXmlConfig();
	}

	@Bean
	@ConditionalOnMissingBean
	public XmlFactory xmlFactory(XmlConfig xmlConfig) {
		return new DefaultXmlFactory(xmlConfig);
	}

	@Bean
	@ConditionalOnMissingBean
	public XmlLoader xmlLoader(DocumentBuilderProvider provider) {
		return new DefaultXmlLoader(provider);
	}

	@Bean
	@ConditionalOnMissingBean
	public DocumentBuilderProvider documentBuilderProvider(XmlFactory factory) {
		return new FactoryDocumentBuilderProvider(factory);
	}
}
