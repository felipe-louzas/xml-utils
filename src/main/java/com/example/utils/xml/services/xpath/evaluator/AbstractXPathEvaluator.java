package com.example.utils.xml.services.xpath.evaluator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import com.example.utils.xml.services.xpath.exceptions.XPathEvaluationException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base implementation of {@link XPathEvaluator}.
 * <p>
 * Handles all type conversions, mapping utilities, and convenience evaluation methods. Subclasses must implement only
 * {@link #evaluateExpression(String, QName)} to perform the actual XPath evaluation logic.
 */
public abstract class AbstractXPathEvaluator implements XPathEvaluator {

	/* ============================================================
	   Abstract core method — to be implemented by subclasses
	   ============================================================ */

	@Override
	public abstract Object evaluateExpression(String expression, QName returnType);

	protected abstract XPathEvaluator createRelativeEvaluator(Node node);

	/* ============================================================
	   Generic mappers
	   ============================================================ */

	@Override
	public <T> T evaluateWithMapper(String expression, Function<Node, T> mapper) {
		val node = evaluateAsNode(expression);
		return node != null ? mapper.apply(node) : null;
	}

	@Override
	public <T> List<T> evaluateListWithMapper(String expression, Function<Node, T> mapper) {
		val nodes = evaluateAsListOfNodes(expression);
		if (nodes.isEmpty()) return List.of();
		return nodes.stream().map(mapper).toList();
	}

	/* ============================================================
	   Typed evaluation helpers
	   ============================================================ */

	@Override
	public String evaluateAsString(String expression) {
		val result = evaluateExpression(expression, XPathConstants.STRING);
		return Objects.toString(result, null);
	}

	@Override
	public String evaluateAsStringOrNull(String expression) {
		return StringUtils.trimToNull(evaluateAsString(expression));
	}

	@Override
	public boolean evaluateAsBooleanString(String expression) {
		val result = evaluateAsStringOrNull(expression);
		if (result == null) return false;
		switch (result.toLowerCase()) {
			case "true", "1", "yes", "on" -> { return true; }
			case "false", "0", "no", "off" -> { return false; }
			default -> throw parsingError(expression, "Boolean", result, null);
		}
	}

	@Override
	public BigDecimal evaluateAsDecimal(String expression) {
		val result = evaluateAsStringOrNull(expression);
		if (result == null) return null;
		return tryParse(expression, "BigDecimal", BigDecimal::new, result);
	}

	@Override
	public BigInteger evaluateAsBigInt(String expression) {
		val result = evaluateAsStringOrNull(expression);
		if (result == null) return null;
		try {
			val bd = new BigDecimal(result.trim());
			return bd.toBigIntegerExact();
		} catch (Exception ex) {
			throw parsingError(expression, "BigInteger", result, ex);
		}
	}

	@Override
	public Long evaluateAsLong(String expression) {
		val result = evaluateAsNumber(expression);
		if (result == null) return null;
		if (result instanceof Long l) return l;
		if (result instanceof Number n) return n.longValue();
		if (result instanceof String str)
			return tryParse(expression, "Long", Long::parseLong, str);
		throw conversionError(expression, "Long", result);
	}

	@Override
	public Integer evaluateAsInt(String expression) {
		val result = evaluateAsNumber(expression);
		if (result == null) return null;
		if (result instanceof Integer i) return i;
		if (result instanceof Number n) return n.intValue();
		if (result instanceof String str)
			return tryParse(expression, "Integer", Integer::parseInt, str);
		throw conversionError(expression, "Integer", result);
	}

	@Override
	public Node evaluateAsNode(String expression) {
		val result = evaluateExpression(expression, XPathConstants.NODE);
		return (result instanceof Node node) ? node : null;
	}

	@Override
	public NodeList evaluateAsNodeList(String expression) {
		val result = evaluateExpression(expression, XPathConstants.NODESET);
		return (result instanceof NodeList nodes) ? nodes : null;
	}

	@Override
	public List<Node> evaluateAsListOfNodes(String expression) {
		val list = evaluateAsNodeList(expression);
		if (list == null || list.getLength() == 0) return List.of();
		return IntStream.range(0, list.getLength())
			.mapToObj(list::item)
			.filter(Objects::nonNull)
			.toList();
	}

	@Override
	public boolean existsNode(String expression) {
		return evaluateAsNode(expression) != null;
	}

	@Override
	public boolean evaluateAsTrue(String expression) {
		return evaluateAsBoolean(expression);
	}

	@Override
	public boolean evaluateAsFalse(String expression) {
		return !evaluateAsBoolean(expression);
	}

	/* ============================================================
	   Relative evaluation
	   ============================================================ */

	@Override
	public Optional<XPathEvaluator> findNode(String expression) {
		val node = evaluateAsNode(expression);
		return Optional.ofNullable(node).map(this::createRelativeEvaluator);
	}

	@Override
	public Stream<XPathEvaluator> findNodes(String expression) {
		return evaluateAsListOfNodes(expression)
			.stream()
			.map(this::createRelativeEvaluator)
			.filter(Objects::nonNull);
	}

	/* ============================================================
	   Utility methods
	   ============================================================ */

	private boolean evaluateAsBoolean(String expression) {
		val result = evaluateExpression(expression, XPathConstants.BOOLEAN);
		if (result == null) return false;
		if (result instanceof Boolean b) return b;
		if (result instanceof Number n) return n.doubleValue() != 0d;
		if (result instanceof String s) return Boolean.parseBoolean(s.trim());
		throw conversionError(expression, "Boolean", result);
	}

	private Object evaluateAsNumber(String expression) {
		val result = evaluateExpression(expression, XPathConstants.NUMBER);
		if (result == null) return null;
		if (result instanceof Double d && Double.isNaN(d))
			return evaluateAsStringOrNull(expression);
		return result;
	}

	private <T> T tryParse(String expr, String targetType, Function<String, T> parser, String str) {
		try {
			return parser.apply(str.trim());
		} catch (Exception ex) {
			throw parsingError(expr, targetType, str, ex);
		}
	}

	protected XPathEvaluationException parsingError(String expr, String targetType, Object value, Exception cause) {
		return new XPathEvaluationException(
			"Cannot convert " + value + " to " + targetType + " for expression", expr, cause);
	}

	protected XPathEvaluationException conversionError(String expr, String targetType, Object value) {
		return new XPathEvaluationException(
			"Cannot convert result of type " + value.getClass().getSimpleName() + " to " + targetType + " for expression", expr);
	}
}
