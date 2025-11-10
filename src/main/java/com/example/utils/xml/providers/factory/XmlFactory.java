package com.example.utils.xml.providers.factory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

public interface XmlFactory {
	DocumentBuilderFactory getDocumentBuilderFactory();

	SchemaFactory getSchemaFactory();

	XPathFactory getXPathFactory();

	TransformerFactory getTransformerFactory();
}
