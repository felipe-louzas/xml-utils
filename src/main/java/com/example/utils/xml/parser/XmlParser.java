package com.example.utils.xml.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.function.FailableFunction;

public interface XmlParser {
	<T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException;

	interface Builder {
		XmlParser build(DocumentBuilderFactory factory);
	}
}
