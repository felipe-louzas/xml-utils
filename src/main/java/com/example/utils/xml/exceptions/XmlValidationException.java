package com.example.utils.xml.exceptions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * A specialized exception thrown during XML schema validation. It captures all validation errors, not just the first one, allowing for
 * user-friendly error reporting.
 */
@Getter
public class XmlValidationException extends RuntimeException {

	/**
	 * A list of all validation errors found.
	 */
	private final List<String> validationErrors;

	public XmlValidationException(List<String> validationErrors) {
		super(buildErrorMessage(validationErrors));
		this.validationErrors = Collections.unmodifiableList(validationErrors);
	}

	private static String buildErrorMessage(List<String> errors) {
		if (errors == null || errors.isEmpty()) {
			return "XML validation failed with no specific errors listed.";
		}
		return "XML validation failed with " + errors.size() + " error(s): \n" +
			errors.stream()
				.map(e -> "  - " + e)
				.collect(Collectors.joining("\n"));
	}
}
