package com.example.utils.xml.spring;

import javax.xml.xpath.XPathExpression;

import com.example.utils.xml.xpath.compiler.XPathCompiler;
import org.springframework.cache.Cache;

public class CachingXPathCompilerAdapter implements XPathCompiler {
	XPathCompiler delegate;
	Cache cache;

	@Override
	public XPathExpression compile(String expression) {
		return cache.get(expression, () -> delegate.compile(expression));
	}
}
