package com.example.utils.xml.config;

import com.example.utils.xml.config.constants.ConfigKeys;
import com.example.utils.xml.services.factory.DefaultXmlFactory;
import com.example.utils.xml.services.factory.XmlFactory;
import com.example.utils.xml.services.document.loader.DefaultXmlLoader;
import com.example.utils.xml.services.document.loader.XmlLoader;
import com.example.utils.xml.services.document.parser.DocumentBuilderProvider;
import com.example.utils.xml.services.document.parser.FactoryDocumentBuilderProvider;
import com.example.utils.xml.services.factory.XmlProviders;
import com.example.utils.xml.services.xpath.provider.FactoryXPathEvaluatorProvider;
import com.example.utils.xml.services.xpath.provider.XPathEvaluatorProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe de configuração automática para os componentes XML.
 */
@Configuration
@EnableConfigurationProperties
public class XmlAutoConfiguration {
	@Bean
	@ConfigurationProperties(prefix = ConfigKeys.PROP_PREFIX)
	public XmlConfig xmlConfig() {
		return new XmlConfigProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public XmlFactory xmlFactory(XmlConfig xmlConfig) {
		return new DefaultXmlFactory(xmlConfig);
	}

	@Bean
	@ConditionalOnMissingBean
	public DocumentBuilderProvider documentBuilderProvider(XmlFactory factory) {
		return new FactoryDocumentBuilderProvider(factory);
	}

	@Bean
	@ConditionalOnMissingBean
	public XPathEvaluatorProvider xPathEvaluatorProvider(XmlFactory factory) {
		return new FactoryXPathEvaluatorProvider(factory);
	}

	@Bean
	public XmlProviders xmlProviders(XmlConfig config,
	                                 XmlFactory factory,
	                                 DocumentBuilderProvider documentBuilderProvider,
	                                 XPathEvaluatorProvider xPathEvaluatorProvider) {
		return new XmlProviders(config, factory, documentBuilderProvider, xPathEvaluatorProvider);
	}

	@Bean
	@ConditionalOnMissingBean
	public XmlLoader xmlLoader(XmlProviders providers) {
		return new DefaultXmlLoader(providers);
	}
}
