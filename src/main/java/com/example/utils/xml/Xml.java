package com.example.utils.xml;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;

import com.example.utils.xml.context.XmlContext;
import com.example.utils.xml.formatter.XmlFormatter;
import com.example.utils.xml.loader.DefaultXmlLoader;
import com.example.utils.xml.loader.XmlLoader;
import com.example.utils.xml.validation.XmlValidator;
import com.example.utils.xml.xpath.XPathEvaluator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The main entry point for the XML utility library.
 * <p>
 * This class acts as a wrapper for a standard {@link Document} object and provides fluent access to validation, formatting, and XPath
 * evaluation.
 * <p>
 * An instance of this class is intended to be used by a single thread and is not thread-safe, as it holds a mutable {@link Document}
 * object. However, the static methods used to create {@link Xml} instances are thread-safe.
 *
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class Xml {

	/* The context holding the document and related services. Not thread-safe! */
	XmlContext context;

	/* ------ Convenience loader delegates  ------ */

	public static Xml load(Document document) {
		return loader().fromDocument(document);
	}

	public static Xml load(CharSequence xmlContent) {
		return loader().fromString(xmlContent);
	}

	public static Xml load(File file) {
		return loader().fromFile(file);
	}

	public static Xml load(Path path) {
		return loader().fromPath(path);
	}

	public static Xml load(InputStream inputStream) {
		return loader().fromInputStream(inputStream);
	}

	public static Xml load(Reader reader) {
		return loader().fromReader(reader);
	}

	public static XmlLoader loader() {
		return DefaultXmlLoader.getDefaultInstance();
	}

	/* ------ Instance methods  ------ */

	public Document getDocument() {
		return context.getDocument();
	}

	/* ------ XPath Evaluation  ------ */

	public String getStringByXPath(String expression) {
		return xpath().evaluateAsString(expression);
	}

	public Boolean getBooleanByXPath(String expression) {
		return xpath().evaluateAsBoolean(expression);
	}

	public Double getDoubleByXPath(String expression) {
		return xpath().evaluateAsDouble(expression);
	}

	public BigDecimal getDecimalByXPath(String expression) {
		return xpath().evaluateAsDecimal(expression);
	}

	public BigInteger getIntegerByXPath(String expression) {
		return xpath().evaluateAsInteger(expression);
	}

	public Long getLongByXPath(String expression) {
		return xpath().evaluateAsLong(expression);
	}

	public Integer getIntByXPath(String expression) {
		return xpath().evaluateAsInt(expression);
	}

	public Node getNodeByXPath(String expression) {
		return xpath().evaluateAsNode(expression);
	}

	public List<Node> getNodesByXPath(String expression) {
		return xpath().evaluateAsListOfNodes(expression);
	}

	public XPathEvaluator xpath() {
		return context.getXPathEvaluator();
	}

	/* ------ Validation  ------ */

	public XmlValidator validator() {
		return context.getXmlValidator();
	}

	/* ------ Formatting  ------ */

	public String toFormattedString() {
		return formatter().toString();
	}

	@Override
	public String toString() {
		return formatter().toString(false);
	}

	public XmlFormatter formatter() {
		return context.getXmlFormatter();
	}
}
