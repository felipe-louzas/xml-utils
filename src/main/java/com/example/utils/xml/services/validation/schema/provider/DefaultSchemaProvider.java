package com.example.utils.xml.services.validation.schema.provider;

import com.example.utils.xml.services.validation.schema.SchemaId;
import com.example.utils.xml.services.validation.schema.XmlSchema;
import com.example.utils.xml.services.validation.schema.compiler.SchemaCompiler;
import com.example.utils.xml.services.validation.schema.resolver.SchemaResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultSchemaProvider implements SchemaProvider {
	SchemaCompiler xmlFactory;
	SchemaResolver resolver;

	@Override
	public XmlSchema get(SchemaId schemaId) {
		val sources = resolver.resolve(schemaId);
		return null;
	}
}
