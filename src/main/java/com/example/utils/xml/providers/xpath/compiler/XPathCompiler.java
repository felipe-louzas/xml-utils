package com.example.utils.xml.providers.xpath.compiler;

import javax.xml.xpath.XPathExpression;

/**
 * Interface for a service that provides compiled XPathExpression objects. This allows swapping between Spring-managed and default cache
 * implementations.
 */
public interface XPathCompiler {
	/**
	 * Retrieves a compiled XPathExpression.
	 *
	 * @param expression The XPath string.
	 * @return A thread-safe, compiled XPathExpression.
	 */
	XPathExpression compile(String expression);
}