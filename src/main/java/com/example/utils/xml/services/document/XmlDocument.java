package com.example.utils.xml.services.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

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

	public BigInteger getBigIntByXPath(String expression) {
		return xpath().evaluateAsBigInt(expression);
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

	public XPathEvaluator findNodeByXPath(String expression) {
		return xpath().findNode(expression);
	}
}
