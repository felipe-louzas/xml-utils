package com.example.utils.xml.xpath.compiler;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import com.example.utils.xml.xpath.exceptions.InvalidXPathExpressionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultXPathCompiler implements XPathCompiler {
	XPath xpath;

	@Override
	public XPathExpression compile(String expression) {
		try {
			return xpath.compile(expression);
		} catch (XPathExpressionException ex) {
			throw new InvalidXPathExpressionException(expression, ex);
		}
	}
}
