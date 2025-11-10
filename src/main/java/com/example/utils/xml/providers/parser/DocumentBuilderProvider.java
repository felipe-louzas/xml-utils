package com.example.utils.xml.providers.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.function.FailableFunction;

public interface DocumentBuilderProvider {
	<T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException;
}
