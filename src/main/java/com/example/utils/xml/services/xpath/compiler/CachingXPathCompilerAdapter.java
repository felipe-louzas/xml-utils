package com.example.utils.xml.services.xpath.compiler;

import javax.xml.xpath.XPathExpression;

import org.springframework.cache.Cache;

public class CachingXPathCompilerAdapter implements XPathCompiler {
	XPathCompiler delegate;
	Cache cache;

	@Override
	public XPathExpression compile(String expression) {
		return cache.get(expression, () -> delegate.compile(expression));
	}
}
