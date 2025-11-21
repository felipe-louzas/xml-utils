package com.example.utils.xml.services.validation.schema;

import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.validation.ValidationResult;

public interface XmlSchema {
	ValidationResult validate(XmlDocument document);
}
