package com.example.utils.xml.services.parser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.example.utils.xml.services.factory.XmlFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.lang3.function.FailableFunction;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FactoryDocumentBuilderProvider implements DocumentBuilderProvider {
	XmlFactory factory;

	@Override
	public <T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E, ParserConfigurationException {
		val documentBuilder = factory.getDocumentBuilderFactory().newDocumentBuilder();
		return parser.apply(documentBuilder);
	}
}
