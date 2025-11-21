package com.example.utils.xml.services;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.document.loader.DefaultXmlLoader;
import com.example.utils.xml.services.document.loader.XmlLoader;
import com.example.utils.xml.services.document.parser.FactoryDocumentBuilderProvider;
import com.example.utils.xml.services.factory.DefaultXmlFactory;
import com.example.utils.xml.services.factory.XmlFactory;
import com.example.utils.xml.services.validation.XmlValidator;
import com.example.utils.xml.services.xpath.provider.FactoryXPathEvaluatorProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlService {
	XmlConfig config;
	XmlFactory factory;
	XmlLoader loader;
	XmlValidator validator;

	/** Create a new XmlService with the given configuration. */
	public static XmlService withConfig(XmlConfig config) {
		val factory = new DefaultXmlFactory(config);

		val documentBuilderProvider = new FactoryDocumentBuilderProvider(factory);
		val xPathEvaluatorProvider = new FactoryXPathEvaluatorProvider(factory);
		val xmlValidator = (XmlValidator) null; // Placeholder for XmlValidator implementation

		val loader = new DefaultXmlLoader(xmlValidator, documentBuilderProvider, xPathEvaluatorProvider);

		return new XmlService(
			config,
			factory,
			loader,
			xmlValidator
		);
	}

}
