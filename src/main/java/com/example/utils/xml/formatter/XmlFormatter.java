package com.example.utils.xml.formatter;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

public interface XmlFormatter {
	String toString(boolean prettyPrint);

	CharSequence toCharSequence(boolean prettyPrint);

	void writeTo(Writer writer, boolean prettyPrint);

	void writeTo(OutputStream out, boolean prettyPrint);

	void writeTo(File file, boolean prettyPrint);

	void writeTo(Path path, boolean prettyPrint);
}
