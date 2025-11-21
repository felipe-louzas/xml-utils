package com.example.utils.xml.services.document.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.xml.services.document.XmlDocument;
import org.w3c.dom.Document;

public interface XmlLoader {
	XmlDocument load(Document document);

	XmlDocument load(CharSequence xmlContent);

	XmlDocument load(File file);

	XmlDocument load(Path path);

	XmlDocument load(InputStream inputStream);

	XmlDocument load(Reader reader);
}
