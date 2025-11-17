package com.example.utils.xml.services.xpath.provider;

import com.example.utils.xml.services.xpath.evaluator.XPathEvaluator;
import org.w3c.dom.Document;

public interface XPathEvaluatorProvider {
	XPathEvaluator getEvaluator(Document document);
}
