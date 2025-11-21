package com.example.utils.xml.services.validation.schema;

import org.apache.commons.lang3.StringUtils;

public record SchemaId(
	String namespaceUri,
	String localName,
	String version,
	String schemaLocation
) {
	public static SchemaId byQName(String namespaceUri, String localName) {
		return SchemaId.byQName(namespaceUri, localName, StringUtils.EMPTY);
	}

	public static SchemaId byQName(String namespaceUri, String localName, String version) {
		return new SchemaId(namespaceUri, localName, version, StringUtils.EMPTY);
	}

	public static SchemaId byLocation(String schemaLocation) {
		return new SchemaId(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, schemaLocation);
	}
}
