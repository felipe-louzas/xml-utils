package com.example.utils.xml.services.xpath.evaluator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;

import com.example.utils.xml.services.xpath.exceptions.XPathEvaluationException;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base implementation of {@link XPathEvaluator}.
 * <p>
 * Handles all type conversions, mapping utilities, and convenience evaluation methods. Subclasses must implement only
 * {@link #evaluate(String, QName)} to perform the actual XPath evaluation logic.
 */
public abstract class AbstractXPathEvaluator implements XPathEvaluator {

	/* ============================================================
	   Abstract core method — to be implemented by subclasses
	   ============================================================ */

	@Override
	public abstract Object evaluate(String expression, QName returnType);

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
		val result = evaluate(expression, XPathConstants.STRING);
		return result != null ? result.toString() : null;
	}

	@Override
	public Boolean evaluateAsBoolean(String expression) {
		val result = evaluate(expression, XPathConstants.BOOLEAN);
		if (result == null) return null;
		if (result instanceof Boolean b) return b;
		if (result instanceof Number n) return n.doubleValue() != 0d;
		if (result instanceof String s) return Boolean.parseBoolean(s.trim());
		throw conversionError(expression, "Boolean", result);
	}

	@Override
	public Double evaluateAsDouble(String expression) {
		val result = evaluate(expression, XPathConstants.NUMBER);
		if (result == null) return null;
		if (result instanceof Double d) return d;
		if (result instanceof Number n) return n.doubleValue();
		if (result instanceof String str && StringUtils.isNotBlank(str))
			return tryParse(expression, "Double", Double::parseDouble, str);
		throw conversionError(expression, "Double", result);
	}

	@Override
	public BigDecimal evaluateAsDecimal(String expression) {
		val result = evaluate(expression, XPathConstants.NUMBER);
		if (result == null) return null;
		if (result instanceof BigDecimal bd) return bd;
		if (result instanceof Double d) return BigDecimal.valueOf(d);
		if (result instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
		if (result instanceof String str && StringUtils.isNotBlank(str))
			return tryParse(expression, "BigDecimal", BigDecimal::new, str);
		throw conversionError(expression, "BigDecimal", result);
	}

	@Override
	public BigInteger evaluateAsBigInt(String expression) {
		val num = evaluateAsDecimal(expression);
		return num != null ? num.toBigInteger() : null;
	}

	@Override
	public Long evaluateAsLong(String expression) {
		val result = evaluate(expression, XPathConstants.NUMBER);
		if (result == null) return null;
		if (result instanceof Long l) return l;
		if (result instanceof Number n) return n.longValue();
		if (result instanceof String str && NumberUtils.isParsable(str))
			return tryParse(expression, "Long", Long::parseLong, str);
		throw conversionError(expression, "Long", result);
	}

	@Override
	public Integer evaluateAsInt(String expression) {
		val result = evaluate(expression, XPathConstants.NUMBER);
		if (result == null) return null;
		if (result instanceof Integer i) return i;
		if (result instanceof Number n) return n.intValue();
		if (result instanceof String str && NumberUtils.isParsable(str))
			return tryParse(expression, "Integer", Integer::parseInt, str);
		throw conversionError(expression, "Integer", result);
	}

	@Override
	public Node evaluateAsNode(String expression) {
		val result = evaluate(expression, XPathConstants.NODE);
		return (result instanceof Node node) ? node : null;
	}

	@Override
	public NodeList evaluateAsNodeList(String expression) {
		val result = evaluate(expression, XPathConstants.NODESET);
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

	/* ============================================================
	   Relative evaluation
	   ============================================================ */

	@Override
	public XPathEvaluator findNode(String expression) {
		val node = evaluateAsNode(expression);
		return node != null ? createRelativeEvaluator(node) : null;
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

	private <T> T tryParse(String expr, String targetType, Function<String, T> parser, String str) {
		try {
			return parser.apply(str.trim());
		} catch (Exception ex) {
			throw new XPathEvaluationException(
				"Cannot convert " + str + " to " + targetType + " for expression", expr, ex);
		}
	}

	protected RuntimeException conversionError(String expr, String targetType, Object value) {
		return new XPathEvaluationException(
			"Cannot convert result of type " + value.getClass().getSimpleName() + " to " + targetType + " for expression", expr);
	}
}
