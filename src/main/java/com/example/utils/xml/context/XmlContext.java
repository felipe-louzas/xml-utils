package com.example.utils.xml.context;

import com.example.utils.xml.providers.factory.DefaultXmlFactory;
import com.example.utils.xml.providers.formatter.XmlFormatter;
import com.example.utils.xml.providers.validation.XmlValidator;
import com.example.utils.xml.providers.xpath.XPathEvaluator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class XmlContext {
	//final DefaultXmlFactory factory;
	final Document document;

	/* Lazy-init non-thread-safe services */
	XPathEvaluator xPathEvaluator;
	XmlFormatter xmlFormatter;
	XmlValidator xmlValidator;
}
