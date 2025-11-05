package com.example.utils.xml.context;

import com.example.utils.xml.factory.XmlFactory;
import com.example.utils.xml.formatter.XmlFormatter;
import com.example.utils.xml.validation.XmlValidator;
import com.example.utils.xml.xpath.XPathEvaluator;
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
	final XmlFactory factory;
	final Document document;

	/* Lazy-init non-thread-safe services */
	XPathEvaluator xPathEvaluator;
	XmlFormatter xmlFormatter;
	XmlValidator xmlValidator;
}
