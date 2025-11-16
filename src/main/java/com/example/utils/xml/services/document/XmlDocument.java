package com.example.utils.xml.services.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.example.utils.xml.services.context.XmlContext;
import com.example.utils.xml.services.xpath.evaluator.XPathEvaluator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.w3c.dom.Node;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlDocument {
	XmlContext context;

	/* ------ XPath Evaluation  ------ */

	public XPathEvaluator xpath() {
		return context.getXPathEvaluator();
	}

	public String getStringByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsStringOrNull);
	}

	public Boolean getBooleanByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsBooleanString);
	}

	public BigDecimal getDecimalByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsDecimal);
	}

	public BigInteger getBigIntByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsBigInt);
	}

	public Long getLongByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsLong);
	}

	public Integer getIntByXPath(String expression) {
		return getByXPath(expression, XPathEvaluator::evaluateAsInt);
	}

	public Node getNodeByXPath(String expression) {
		return xpath().evaluateAsNode(expression);
	}

	public List<Node> getNodesByXPath(String expression) {
		return xpath().evaluateAsListOfNodes(expression);
	}

	public Optional<XPathEvaluator> findNodeByXPath(String expression) {
		return xpath().findNode(expression);
	}

	public Stream<XPathEvaluator> findNodesByXPath(String expression) {
		return xpath().findNodes(expression);
	}

	public <T> T getByXPath(String expression, Function<XPathEvaluator, T> mapper) {
		return xpath()
			.findNode(expression)
			.map(mapper)
			.orElse(null);
	}
}
