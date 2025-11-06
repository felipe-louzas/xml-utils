package com.example.utils.xml.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.lang3.function.FailableFunction;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentBuilderXmlParser implements XmlParser {
	DocumentBuilderFactory factory;

	@Override
	public <T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException {
		val documentBuilder = factory.newDocumentBuilder();
		return parser.apply(documentBuilder);
	}
}
