package com.example.utils.xml.services.xpath.evaluator;

import javax.xml.namespace.QName;

import com.example.utils.xml.services.xpath.compiler.XPathCompiler;
import com.example.utils.xml.services.xpath.exceptions.XPathEvaluationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.w3c.dom.Node;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultXPathEvaluator extends AbstractXPathEvaluator implements XPathEvaluator {
	XPathCompiler compiler;
	Node contextNode;

	@Override
	public Object evaluate(String expression, QName returnType) {
		val compiledExpression = compiler.compile(expression);

		try {
			return compiledExpression.evaluate(contextNode, returnType);
		} catch (Exception ex) {
			throw new XPathEvaluationException(expression, ex);
		}
	}

	@Override
	protected XPathEvaluator createRelativeEvaluator(Node node) {
		return new DefaultXPathEvaluator(compiler, node);
	}
}
