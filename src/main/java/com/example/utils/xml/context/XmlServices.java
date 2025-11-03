package com.example.utils.xml.context;

import com.example.utils.xml.formatter.XmlTransformerFactory;
import com.example.utils.xml.parser.XmlParserFactory;
import com.example.utils.xml.validation.SchemaProvider;
import com.example.utils.xml.xpath.compiler.XPathCompiler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * A simple holder bean that aggregates all thread-safe XML services.
 */
@Getter
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlServices {
	XmlParserFactory parserFactory;
	XPathCompiler xPathProvider;
	SchemaProvider schemaProvider;
	XmlTransformerFactory transformerFactory;
}