package com.example.utils.xml.services.validation.schema.resolver;

import java.util.Optional;

import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.validation.schema.SchemaId;

public interface SchemaIdResolver {
	/**
	 * Attempt to derive a SchemaKey for the given XmlDocument. Return Optional.empty() if this resolver cannot determine the key.
	 */
	Optional<SchemaId> resolve(XmlDocument document);
}
