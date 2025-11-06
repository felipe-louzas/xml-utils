package com.example.utils.xml.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.function.FailableFunction;

@FieldDefaults
public class PoolingXmlParser implements XmlParser {
	@Override
	public <T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException {
		return null;
	}
}
