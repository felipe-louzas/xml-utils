package com.example.utils.xml.services.validation.schema.provider;

import com.example.utils.xml.services.validation.schema.SchemaId;
import com.example.utils.xml.services.validation.schema.XmlSchema;

public interface SchemaProvider {
	XmlSchema get(SchemaId schemaId);
}
