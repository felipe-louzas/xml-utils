package com.example.utils.xml.xpath.exceptions;

import com.example.utils.xml.exceptions.XmlException;
import lombok.Getter;

@Getter
public class InvalidXPathExpressionException extends XmlException {
	private final String expression;

	public InvalidXPathExpressionException(String expression, Throwable cause) {
		super("Invalid XPath expression: " + expression, cause);
		this.expression = expression;
	}
}
