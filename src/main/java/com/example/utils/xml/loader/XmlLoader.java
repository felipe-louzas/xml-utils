package com.example.utils.xml.loader;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;

import javax.xml.transform.Source;

import com.example.utils.xml.Xml;
import org.w3c.dom.Document;

public interface XmlLoader {
	Xml fromString(CharSequence xml);

	Xml fromFile(File file);

	Xml fromPath(Path path);

	Xml fromInputStream(InputStream inputStream);

	Xml fromReader(Reader reader);

	Xml fromUrl(URL url);

	Xml fromSource(Source source);

	Xml fromDocument(Document document);
}
