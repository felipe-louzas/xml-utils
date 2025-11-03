package com.example.utils.xml.xpath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface XPathEvaluator {

	/* --------------- Base evaluate implementation -------------- */

	Object evaluate(String expression, QName returnType);

	/* -------------------- Generic evaluation ------------------- */

	<T> T evaluateWithMapper(String expression, Function<Node, T> mapper);

	<T> List<T> evaluateListWithMapper(String expression, Function<Node, T> mapper);

	/* ---------------- Convenience typed evaluation ---------------- */

	String evaluateAsString(String expression);

	Boolean evaluateAsBoolean(String expression);

	Double evaluateAsDouble(String expression);

	BigDecimal evaluateAsDecimal(String expression);

	BigInteger evaluateAsInteger(String expression);

	Long evaluateAsLong(String expression);

	Integer evaluateAsInt(String expression);

	Node evaluateAsNode(String expression);

	NodeList evaluateAsNodeList(String expression);

	List<Node> evaluateAsListOfNodes(String expression);

	/* ------------- Node-relative evaluation ------------- */

	XPathEvaluator findNode(String expression);

	Stream<XPathEvaluator> findNodes(String expression);
}
