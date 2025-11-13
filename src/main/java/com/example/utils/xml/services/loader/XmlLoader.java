package com.example.utils.xml.services.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.factory.DefaultXmlFactory;
import com.example.utils.xml.services.parser.FactoryDocumentBuilderProvider;
import com.example.utils.xml.services.providers.XmlProviders;
import com.example.utils.xml.services.xpath.FactoryXPathEvaluatorProvider;
import lombok.val;
import org.w3c.dom.Document;

public interface XmlLoader {

	/** Create a new XmlLoader with the given configuration. */
	static XmlLoader withConfig(XmlConfig config) {
		val factory = new DefaultXmlFactory(config);
		val documentBuilderProvider = new FactoryDocumentBuilderProvider(factory);
		val xPathEvaluatorProvider = new FactoryXPathEvaluatorProvider(factory);
		val providers = new XmlProviders(config, factory, documentBuilderProvider, xPathEvaluatorProvider);
		return new DefaultXmlLoader(providers);
	}

	XmlProviders getProviders();

	XmlConfig getConfig();

	XmlDocument load(Document document);

	XmlDocument load(CharSequence xmlContent);

	XmlDocument load(File file);

	XmlDocument load(Path path);

	XmlDocument load(InputStream inputStream);

	XmlDocument load(Reader reader);

}
