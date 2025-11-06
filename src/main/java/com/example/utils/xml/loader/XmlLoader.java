package com.example.utils.xml.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

import com.example.utils.xml.Xml;
import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.factory.XmlFactory;
import org.w3c.dom.Document;

public interface XmlLoader {
	Xml fromString(CharSequence xml);

	Xml fromFile(File file);

	Xml fromPath(Path path);

	Xml fromInputStream(InputStream inputStream);

	Xml fromReader(Reader reader);

	Xml fromDocument(Document document);

	XmlLoader withFactory(XmlFactory xmlFactory);

	default XmlLoader withConfig(XmlConfig config) {
		return withFactory(XmlFactory.withConfig(config));
	}
}
