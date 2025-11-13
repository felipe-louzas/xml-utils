package com.example.utils.xml.services.xpath.exceptions;

import com.example.utils.xml.exceptions.XmlException;
import lombok.Getter;

@Getter
public class XPathEvaluationException extends XmlException {
	private final String expression;

	public XPathEvaluationException(String expression, Throwable cause) {
		super("Error evaluating XPath expression: " + expression, cause);
		this.expression = expression;
	}

	public XPathEvaluationException(String message, String expression) {
		this(message, expression, null);
	}

	public XPathEvaluationException(String message, String expression, Throwable cause) {
		super(message + ": " + expression, cause);
		this.expression = expression;
	}
}