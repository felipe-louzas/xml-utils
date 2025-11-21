package com.example.utils.xml.services.validation.schema.resolver;

import javax.xml.transform.Source;

import com.example.utils.xml.services.validation.schema.SchemaId;

public interface SchemaResolver {
	/**
	 * Return one or more Sources for the given schemaKey or for a systemId referenced in an import/include. Return null if not known.
	 */
	Source[] resolve(SchemaId key);
}
