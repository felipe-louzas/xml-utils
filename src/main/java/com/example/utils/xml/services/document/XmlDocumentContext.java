package com.example.utils.xml.services.document;

import com.example.utils.xml.services.factory.XmlProviders;
import com.example.utils.xml.services.formatter.XmlFormatter;
import com.example.utils.xml.services.validation.XmlValidator;
import com.example.utils.xml.services.xpath.evaluator.XPathEvaluator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class XmlDocumentContext {
	final XmlProviders providers;
	final Document document;

	/* Lazy-init non-thread-safe services */
	XPathEvaluator xPathEvaluator;
	XmlFormatter xmlFormatter;
	XmlValidator xmlValidator;

	public XPathEvaluator getXPathEvaluator() {
		if (xPathEvaluator == null) {
			xPathEvaluator = providers.getXPathEvaluatorProvider().getEvaluator(document);
		}
		return xPathEvaluator;
	}
}
