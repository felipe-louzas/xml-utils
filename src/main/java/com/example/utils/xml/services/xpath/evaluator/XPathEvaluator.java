package com.example.utils.xml.services.xpath.evaluator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface XPathEvaluator {

	String CURRENT_NODE = ".";


	/* --------------- Base evaluate implementation -------------- */

	Object evaluateExpression(String expression, QName returnType);

	/* -------------------- Generic evaluation ------------------- */

	<T> T evaluateWithMapper(String expression, Function<Node, T> mapper);

	<T> List<T> evaluateListWithMapper(String expression, Function<Node, T> mapper);

	/* ---------------- Convenience typed evaluation ---------------- */

	String evaluateAsString(String expression);

	String evaluateAsStringOrNull(String expression);

	boolean evaluateAsBooleanString(String expression);

	BigDecimal evaluateAsDecimal(String expression);

	BigInteger evaluateAsBigInt(String expression);

	Long evaluateAsLong(String expression);

	Integer evaluateAsInt(String expression);

	Node evaluateAsNode(String expression);

	NodeList evaluateAsNodeList(String expression);

	List<Node> evaluateAsListOfNodes(String expression);

	/* ------------- Existence and truth evaluation ------------- */

	boolean existsNode(String expression);

	boolean evaluateAsTrue(String expression);

	boolean evaluateAsFalse(String expression);

	/* ------------- Node-relative evaluation ------------- */

	Optional<XPathEvaluator> findNode(String expression);

	Stream<XPathEvaluator> findNodes(String expression);

	/* ------------- Default current node evaluator ------------- */

	default String evaluateAsString() {
		return evaluateAsString(CURRENT_NODE);
	}

	default String evaluateAsStringOrNull() {
		return evaluateAsStringOrNull(CURRENT_NODE);
	}

	default boolean evaluateAsBooleanString() {
		return evaluateAsBooleanString(CURRENT_NODE);
	}

	default BigDecimal evaluateAsDecimal() {
		return evaluateAsDecimal(CURRENT_NODE);
	}

	default BigInteger evaluateAsBigInt() {
		return evaluateAsBigInt(CURRENT_NODE);
	}

	default Long evaluateAsLong() {
		return evaluateAsLong(CURRENT_NODE);
	}

	default Integer evaluateAsInt() {
		return evaluateAsInt(CURRENT_NODE);
	}
}
