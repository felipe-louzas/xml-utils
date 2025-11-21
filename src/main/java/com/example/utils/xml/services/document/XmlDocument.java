package com.example.utils.xml.services.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.example.utils.xml.services.validation.ValidationResult;
import com.example.utils.xml.services.validation.XmlValidator;
import com.example.utils.xml.services.validation.exceptions.SchemaNotFoundException;
import com.example.utils.xml.services.validation.schema.XmlSchema;
import com.example.utils.xml.services.xpath.evaluator.XPathEvaluator;
import com.example.utils.xml.services.xpath.provider.XPathEvaluatorProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlDocument {
	Document document;
	XmlValidator xmlValidator;
	XPathEvaluatorProvider xPathEvaluatorProvider;

	@NonFinal
	XPathEvaluator xPathEvaluator;

	@NonFinal
	ValidationResult validationResult;

	/* ------ Document utilities  ------ */

	public String getRootQName() {
		val root = document.getDocumentElement();
		Objects.requireNonNull(root, "Document has no root element");

		val localName = root.getLocalName();
		if (localName == null) return root.getNodeName();

		val ns = root.getNamespaceURI();
		return ns == null
			? localName
			: "{" + ns + "}" + localName;
	}

	/* ------ XPath Evaluation  ------ */

	public XPathEvaluator xpath() {
		return getXPathEvaluator();
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

	private XPathEvaluator getXPathEvaluator() {
		if (xPathEvaluator == null) {
			xPathEvaluator = xPathEvaluatorProvider.getEvaluator(document);
		}
		return xPathEvaluator;
	}

	/* ------ Validation  ------ */

	public ValidationResult validate() throws SchemaNotFoundException {
		val schema = xmlValidator.resolveSchemaOrThrow(this);
		return validate(schema);
	}

	public ValidationResult validate(XmlSchema schema) {
		validationResult = schema.validate(this);
		return validationResult;
	}

	public ValidationResult getValidationResult() throws SchemaNotFoundException {
		if (validationResult == null) {
			validationResult = validate();
		}
		return validationResult;
	}

	public boolean isValid() throws SchemaNotFoundException {
		return getValidationResult().isValid();
	}

	public boolean isValidated() {
		return validationResult != null;
	}
}
