package com.example.utils.xml.services.factory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

import com.example.utils.xml.config.XmlConfig;

public interface XmlFactory {
	XmlConfig getConfig();

	DocumentBuilderFactory getDocumentBuilderFactory();

	SchemaFactory getSchemaFactory();

	XPathFactory getXPathFactory();

	TransformerFactory getTransformerFactory();
}
