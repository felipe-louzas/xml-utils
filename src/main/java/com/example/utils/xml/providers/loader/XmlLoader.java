package com.example.utils.xml.providers.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.xml.providers.document.XmlDocument;
import org.w3c.dom.Document;

public interface XmlLoader {
	XmlDocument fromString(CharSequence xml);

	XmlDocument fromFile(File file);

	XmlDocument fromPath(Path path);

	XmlDocument fromInputStream(InputStream inputStream);

	XmlDocument fromReader(Reader reader);

	XmlDocument fromDocument(Document document);
}
