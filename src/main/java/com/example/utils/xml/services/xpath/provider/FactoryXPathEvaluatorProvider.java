package com.example.utils.xml.services.xpath.provider;

import com.example.utils.xml.services.factory.XmlFactory;
import com.example.utils.xml.services.xpath.compiler.DefaultXPathCompiler;
import com.example.utils.xml.services.xpath.context.DefaultNamespaceContext;
import com.example.utils.xml.services.xpath.evaluator.DefaultXPathEvaluator;
import com.example.utils.xml.services.xpath.evaluator.XPathEvaluator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.w3c.dom.Document;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FactoryXPathEvaluatorProvider implements XPathEvaluatorProvider {
	XmlFactory xmlFactory;

	@Override
	public XPathEvaluator getEvaluator(Document document) {
		val xpath = xmlFactory.getXPathFactory().newXPath();
		val compiler = new DefaultXPathCompiler(xpath);
		if (xmlFactory.getConfig().getParser().isNamespaceAware()) {
			xpath.setNamespaceContext(new DefaultNamespaceContext(document, xmlFactory.getConfig()));
		}
		return new DefaultXPathEvaluator(compiler, document);
	}
}
