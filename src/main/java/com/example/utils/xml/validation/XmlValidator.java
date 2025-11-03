package com.example.utils.xml.validation;

import com.example.utils.xml.exceptions.XmlValidationException;

public interface XmlValidator {
	void validate() throws XmlValidationException;

	ValidationResult getValidationResult();

	default boolean isValid() {
		return getValidationResult().isValid();
	}

	void throwIfInvalid() throws XmlValidationException;
}
