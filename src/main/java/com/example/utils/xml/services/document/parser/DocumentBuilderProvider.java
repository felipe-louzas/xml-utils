package com.example.utils.xml.services.document.parser;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.function.FailableFunction;
import org.xml.sax.SAXException;

public interface DocumentBuilderProvider {
	<T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException, IOException, SAXException;
}
