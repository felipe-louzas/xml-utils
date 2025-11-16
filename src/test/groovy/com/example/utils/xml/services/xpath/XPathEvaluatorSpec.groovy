package com.example.utils.xml.services.xpath

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import com.example.utils.xml.config.XmlAutoConfiguration
import com.example.utils.xml.services.xpath.exceptions.InvalidXPathExpressionException
import com.example.utils.xml.services.xpath.exceptions.XPathEvaluationException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import spock.lang.Specification
import spock.lang.Unroll

import javax.xml.xpath.XPathConstants
import java.util.function.Function
import java.util.stream.Collectors

@DirtiesContext
@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration], properties = [
        "utils.xml.parser.namespace-aware=false",
])
class XPathEvaluatorSpec extends Specification {

    def sampleXml = '''
        <root xmlns="https://example.com/ns" xmlns:test="https://example.com/test">
            <person id="1" status="active">
                <name type="primary">Maria</name>
                <age>30</age>
                <active>true</active>
                <salary>1234.56</salary>
                <permissions>
                    <perm>admin</perm>
                    <perm>user</perm>
                </permissions>
            </person>
            <person id="2" status="inactive">
                <name>João</name>
                <age>42</age>
                <active>false</active>
                <salary>0.00</salary>
                <permissions>
                    <perm>user</perm>
                </permissions>
            </person>
            <test:namespacedNode>Namespaced Value</test:namespacedNode>
            <emptyTag/>
            <tagWithEmptyText></tagWithEmptyText>
            <other>
                <boolVal>1</boolVal>
                <boolVal>0</boolVal>
                <boolVal>yes</boolVal>
                <boolVal>no</boolVal>
                <boolVal>INVALID</boolVal>
            </other>
        </root>
    '''

    // Helper mapper function for tests
    Function<Node, String> idMapper = { Node n -> n.getAttributes().getNamedItem("id").getNodeValue() } as Function

    // --- Base evaluateExpression ---

    def "evaluateExpression returns correct XPathConstants types"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "Text content of existing node"
        ev.evaluateExpression("/root/person[1]/name/text()", XPathConstants.STRING) == "Maria"
        ev.evaluateExpression("/root/person[1]/name", XPathConstants.STRING) == "Maria"

        and: "Non-existing node returns empty string"
        ev.evaluateExpression("/root/person[1]/nonexist", XPathConstants.STRING) == ""

        and: "Boolean returns true for existing nodes and boolean expressions"
        ev.evaluateExpression("/root/person[1]/active/text() = 'true'", XPathConstants.BOOLEAN) == true
        ev.evaluateExpression("/root/person[1]/name", XPathConstants.BOOLEAN) == true
        ev.evaluateExpression("/root/person[1]/nonexist", XPathConstants.BOOLEAN) == false

        and: "Boolean does not coerce string values"
        ev.evaluateExpression("/root/person[2]/active/text() = 'false'", XPathConstants.BOOLEAN) == true
        ev.evaluateExpression("/root/person[2]/active/text() = 'true'", XPathConstants.BOOLEAN) == false
        ev.evaluateExpression("/root/person[2]/active", XPathConstants.BOOLEAN) == true

        and: "Number always returns a double or NaN for non-numeric or nonexistent"
        ev.evaluateExpression("/root/person[1]/age", XPathConstants.NUMBER) == 30.0d
        ev.evaluateExpression("/root/person[1]/name", XPathConstants.NUMBER) == Double.NaN
        ev.evaluateExpression("/root/nonexistent", XPathConstants.NUMBER) == Double.NaN

        and: "Node returns found node or null"
        ev.evaluateExpression("/root/person[1]", XPathConstants.NODE) instanceof Node
        ev.evaluateExpression("/root/nonexistent", XPathConstants.NODE) == null

        and: "NodeList returns correct counts or empty"
        (ev.evaluateExpression("/root/person", XPathConstants.NODESET) as NodeList).getLength() == 2
        (ev.evaluateExpression("/root/nonexistent", XPathConstants.NODESET) as NodeList).getLength() == 0
    }

    // --- Generic Mappers ---

    def "evaluateWithMapper maps a single node"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when:
        def mapped = ev.evaluateWithMapper("/root/person[1]", idMapper)

        then:
        mapped == "1"
    }

    def "evaluateWithMapper returns null if node does not exist"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when:
        def mapped = ev.evaluateWithMapper("/root/person[99]", idMapper)

        then:
        mapped == null
    }

    def "evaluateListWithMapper maps a list of nodes"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when:
        def mappedList = ev.evaluateListWithMapper("/root/person", idMapper)

        then:
        mappedList == ["1", "2"]
    }

    def "evaluateListWithMapper returns empty list if no nodes match"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when:
        def mappedList = ev.evaluateListWithMapper("/root/nonexistent", idMapper)

        then:
        mappedList == []
    }

    // --- String Conveniences ---

    def "evaluateAsString and evaluateAsStringOrNull handle existing, empty, and non-existing nodes"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "Existing node returns text"
        ev.evaluateAsString("/root/person[1]/name") == "Maria"
        ev.evaluateAsStringOrNull("/root/person[1]/name") == "Maria"

        and: "Non-existing node"
        ev.evaluateAsString("/root/person[3]/name") == ""
        ev.evaluateAsStringOrNull("/root/person[3]/name") == null

        and: "Self-closing empty tag"
        ev.evaluateAsString("/root/emptyTag") == ""
        ev.evaluateAsStringOrNull("/root/emptyTag") == null

        and: "Tag with empty text"
        ev.evaluateAsString("/root/tagWithEmptyText") == ""
        ev.evaluateAsStringOrNull("/root/tagWithEmptyText") == null
    }

    // --- Boolean Conveniences ---

    @Unroll
    def "evaluateAsBooleanString behavior for '#value' (path: #path)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsStringOrNull(path) == (value.startsWith("n/a") ? null : value)
        ev.evaluateAsBooleanString(path) == expectedResult

        where:
        path                     | value              | expectedResult
        "/root/person[1]/active" | "true"             | true
        "/root/person[2]/active" | "false"            | false
        "/root/other/boolVal[1]" | "1"                | true
        "/root/other/boolVal[2]" | "0"                | false
        "/root/other/boolVal[3]" | "yes"              | true
        "/root/other/boolVal[4]" | "no"               | false
        "/root/nonexistent"      | "n/a (non-exist)"  | false
        "/root/emptyTag"         | "n/a (empty)"      | false
        "/root/tagWithEmptyText" | "n/a (empty text)" | false
    }

    def "evaluateAsBooleanString throws exception for invalid (non-true/false) string"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def path = "/root/other/boolVal[5]" // Value is "INVALID"

        when:
        ev.evaluateAsBooleanString(path)

        then:
        def ex = thrown(XPathEvaluationException)
        ex.message.contains("INVALID")
        ex.message.contains("Boolean")
        ex.message.contains(path)
    }

    // --- Numeric Conveniences ---

    @Unroll
    def "evaluateAsDecimal correctly parses various number formats '#value'"() {
        given:
        def xml = "<r><v>${value}</v></r>"
        def ev = Xml.load(xml).xpath()

        when:
        def result = ev.evaluateAsDecimal("/r/v")

        then:
        result.scale() == expectedScale
        Objects.equals(result.unscaledValue(), new BigInteger(expectedUnscaled))

        where:
        value     | expectedScale | expectedUnscaled
        "1"       | 0             | "1"
        "1.5"     | 1             | "15"
        "0"       | 0             | "0"
        "-3.14"   | 2             | "-314"
        "1E3"     | -3            | "1"
        "1234.56" | 2             | "123456"
    }

    @Unroll
    def "Numeric conversions correctly handles insignificant decimals '#value'"() {
        given:
        def xml = "<r><v>${value}</v></r>"
        def ev = Xml.load(xml).xpath()
        def expr = "/r/v"

        expect:
        Objects.equals(ev.evaluateAsBigInt(expr), new BigDecimal(expected).toBigInteger())
        ev.evaluateAsInt(expr) == new BigDecimal(expected).intValue()
        ev.evaluateAsLong(expr) == new BigDecimal(expected).longValue()

        where:
        value     || expected
        "1"       || "1"
        "1.0"     || "1.0"
        "0"       || "0"
        "0.00"    || "0.00"
        "1234.00" || "1234.00"
    }


    @Unroll
    def "Numeric conversions return null for empty or non-existent nodes (Type: #type)"() {
        given:
        def xpath = Xml.load(sampleXml).xpath()

        when:
        def emptyResult = evaluator(xpath, "/root/emptyTag")
        def emptyTextResult = evaluator(xpath, "/root/tagWithEmptyText")
        def nonexistentResult = evaluator(xpath, "/root/nonexistent")

        then:
        emptyResult == null
        emptyTextResult == null
        nonexistentResult == null

        where:
        type      | evaluator
        "Decimal" | { ev, path -> ev.evaluateAsDecimal(path) }
        "BigInt"  | { ev, path -> ev.evaluateAsBigInt(path) }
        "Long"    | { ev, path -> ev.evaluateAsLong(path) }
        "Int"     | { ev, path -> ev.evaluateAsInt(path) }
    }

    @Unroll
    def "Numeric conversions throw XPathEvaluationException for non-numeric text (Type: #type)"() {
        given:
        def eval = Xml.load(sampleXml).xpath()
        def path = "/root/person[1]/name" // Value is "Maria"

        when:
        evaluator.call(eval, path)

        then:
        def ex = thrown(XPathEvaluationException)
        ex.message.contains("Maria")
        ex.message.contains(typeName)
        ex.message.contains(path)
        ex.cause instanceof NumberFormatException

        where:
        type      | typeName     | evaluator
        "Decimal" | "BigDecimal" | { ev, p -> ev.evaluateAsDecimal(p) }
        "BigInt"  | "BigInteger" | { ev, p -> ev.evaluateAsBigInt(p) }
        "Long"    | "Long"       | { ev, p -> ev.evaluateAsLong(p) }
        "Int"     | "Integer"    | { ev, p -> ev.evaluateAsInt(p) }
    }

    def "Numeric conversions throw XPathEvaluationException when converting decimal to BigInt"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def path = "/root/person[1]/salary" // Value is "1234.56"

        when:
        ev.evaluateAsBigInt(path)

        then:
        def ex = thrown(XPathEvaluationException)
        ex.message.contains("1234.56")
        ex.message.contains("BigInteger")
        ex.message.contains(path)
        ex.cause instanceof ArithmeticException
    }

    def "Numeric narrowing conversions from decimal to Integer and Long don't throw"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def path = "/root/person[1]/salary" // Value is "1234.56"

        expect:
        ev.evaluateAsInt(path) == 1234
        ev.evaluateAsLong(path) == 1234L
    }

    // --- Node Conveniences ---

    def "evaluateAsNode returns node or null"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsNode("/root/person[1]") instanceof Node
        ev.evaluateAsNode("/root/person[99]") == null
    }

    def "evaluateAsNodeList returns NodeList (empty or populated)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsNodeList("/root/person").getLength() == 2
        ev.evaluateAsNodeList("/root/nonexistent").getLength() == 0
    }

    def "evaluateAsListOfNodes returns List (empty or populated)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsListOfNodes("/root/person").size() == 2
        ev.evaluateAsListOfNodes("/root/nonexistent").size() == 0
    }

    // --- Existence & Truthiness ---

    @Unroll
    def "existsNode correctly checks for node presence (path: #path)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.existsNode(path) == expected

        where:
        path                     | expected
        "/root/person[1]"        | true
        "/root/person[1]/name"   | true
        "/root/emptyTag"         | true // The tag exists, even if empty
        "/root/tagWithEmptyText" | true // The tag exists
        "/root/person[99]"       | false
        "/root/nonexistent"      | false
    }

    @Unroll
    def "evaluateAsTrue and evaluateAsFalse use XPath boolean() rules (expression: #expr)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsTrue(expr) == expectedTrue
        ev.evaluateAsFalse(expr) == !expectedTrue

        where:
        expr                            | expectedTrue
        "/root/person[1]"               | true  // Node-set not empty
        "/root/person[99]"              | false // Node-set empty
        "1"                             | true  // Number non-zero
        "0"                             | false // Number zero
        "'hello'"                       | true  // String not empty
        "''"                            | false // String empty
        "/root/person[1]/age > 20"      | true  // boolean expression
        "/root/person[1]/age < 20"      | false // boolean expression
        "/root/person[2]/active/text()" | true  // string 'false' is not empty
    }

    // --- Node-Relative Navigation ---

    def "findNode returns Optional<XPathEvaluator> for relative context"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when: "Find an existing node"
        def childEvalOpt = ev.findNode("/root/person[2]")

        then: "Optional is present and relative paths work"
        childEvalOpt.isPresent()
        def childEval = childEvalOpt.get()
        childEval.evaluateAsString("name") == "João" // relative path
        childEval.evaluateAsInt("age") == 42        // relative path
        childEval.evaluateAsString("/root/person[1]/name") == "Maria" // absolute path still works

        when: "Find a non-existing node"
        def missingEvalOpt = ev.findNode("/root/person[99]")

        then: "Optional is empty"
        !missingEvalOpt.isPresent()
    }

    def "findNodes returns Stream<XPathEvaluator> for relative contexts"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when: "Find existing nodes"
        def stream = ev.findNodes("/root/person")
        def names = stream.map({ e -> e.evaluateAsString("name") }).collect(Collectors.toList())

        then:
        names == ["Maria", "João"]

        when: "Find non-existing nodes"
        def emptyStream = ev.findNodes("/root/nonexistent")

        then:
        emptyStream.collect(Collectors.toList()) == []
    }

    def "findNodes stream can only be consumed once (standard Java Stream behavior)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def stream = ev.findNodes("/root/person")

        when: "First consumption"
        def firstCollect = stream.map({ e -> e.evaluateAsString("name") }).collect(Collectors.toList())

        then:
        firstCollect == ["Maria", "João"]

        when: "Second consumption"
        stream.map({ e -> e.evaluateAsString("name") }).collect(Collectors.toList())

        then: "Stream is already closed"
        thrown(IllegalStateException)
    }

    // --- Default (Current Node) Methods ---

    def "Default methods evaluate on the current node context"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def nameEval = ev.findNode("/root/person[1]/name").get()
        def ageEval = ev.findNode("/root/person[1]/age").get()
        def salaryEval = ev.findNode("/root/person[1]/salary").get()
        def activeEval = ev.findNode("/root/person[1]/active").get()

        expect:
        nameEval.evaluateAsString() == "Maria"
        nameEval.evaluateAsStringOrNull() == "Maria"

        and:
        ageEval.evaluateAsInt() == 30
        ageEval.evaluateAsLong() == 30L
        Objects.equals(ageEval.evaluateAsBigInt(), new BigInteger("30"))

        and:
        Objects.equals(salaryEval.evaluateAsDecimal(), new BigDecimal("1234.56"))

        and:
        activeEval.evaluateAsBooleanString()
    }

    def "Default methods on a complex node return concatenated text content"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def person1Eval = ev.findNode("/root/person[1]").get()
        def person2Eval = ev.findNode("/root/person[2]").get()

        when:
        def person1Text = person1Eval.evaluateAsString()
        def person2Text = person2Eval.evaluateAsString()

        then: "string(.) returns all descendant text nodes, concatenated"
        person1Text.contains("Maria")
        person1Text.contains("30")
        person1Text.contains("true")
        person1Text.contains("1234.56")
        person1Text.contains("admin")
        person1Text.contains("user")

        and:
        person2Text.contains("João")
        person2Text.contains("42")
        person2Text.contains("false")
        person2Text.contains("0.00")
        person2Text.contains("user")
        !person2Text.contains("admin")
    }

    // --- Complex XPath (Functions & Predicates) ---

    @Unroll
    def "Complex XPath functions are evaluated correctly (expression: #expr)"() {
        given:
        def eval = Xml.load(sampleXml).xpath()

        expect:
        Objects.equals(evaluator.call(eval, expr), expected) // Use Objects.equals to handle BigDecimal comparison

        where:
        expr                                       | evaluator                            | expected
        "concat('Mr. ', /root/person[2]/name)"     | { ev, e -> ev.evaluateAsString(e) }  | "Mr. João"
        "string-length(/root/person[1]/name)"      | { ev, e -> ev.evaluateAsInt(e) }     | 5
        "count(/root/person)"                      | { ev, e -> ev.evaluateAsInt(e) }     | 2
        "count(/root/person/permissions/perm)"     | { ev, e -> ev.evaluateAsInt(e) }     | 3
        "sum(/root/person/age)"                    | { ev, e -> ev.evaluateAsDecimal(e) } | new BigDecimal("72")
        "sum(/root/person/age)"                    | { ev, e -> ev.evaluateAsInt(e) }     | 72
        "sum(/root/person/age)"                    | { ev, e -> ev.evaluateAsString(e) }  | "72"
        "starts-with(/root/person[1]/name, 'Mar')" | { ev, e -> ev.evaluateAsTrue(e) }    | true
        "contains(/root/person[1]/name, 'ria')"    | { ev, e -> ev.evaluateAsTrue(e) }    | true
        "name(/root/person[1])"                    | { ev, e -> ev.evaluateAsString(e) }  | "person"
    }

    @Unroll
    def "Complex XPath expressions (predicates, axes) are evaluated correctly"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsString(expr) == expected

        where:
        expr                                             | expected
        "/root/person[@id='2']/name"                     | "João"
        "/root/person[@status='active']/name"            | "Maria"
        "/root/person[age > 40]/name"                    | "João"
        "/root/person[permissions/perm = 'admin']/name"  | "Maria"
        "/root/person[1]/name/@type"                     | "primary"
        "/root/person[1]/following-sibling::person/name" | "João"
        "/root/person[2]/preceding-sibling::person/name" | "Maria"
        "/root/person[1]/permissions/perm[2]"            | "user"
    }

    // --- Edge Cases & Exception Handling ---

    def "Invalid XPath syntax throws InvalidXPathExpressionException"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def invalidExpr = "///@@bad[xpath"

        when:
        ev.evaluateExpression(invalidExpr, XPathConstants.STRING)

        then:
        def ex = thrown(InvalidXPathExpressionException)
        ex.expression == invalidExpr
    }

    def "Exception in mapper function propagates"() {
        given:
        def ev = Xml.load(sampleXml).xpath()
        def failingMapper = { Node n -> throw new IllegalStateException("mapper failed") } as Function<Node, String>

        when:
        ev.evaluateWithMapper("/root/person[1]", failingMapper)

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "mapper failed"

        when:
        ev.evaluateListWithMapper("/root/person", failingMapper)

        then:
        def ex2 = thrown(IllegalStateException)
        ex2.message == "mapper failed"
    }

    def "With a non-namespace-aware parser, XPath can reference namespaced nodes without prefixes"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "non-prefixed access works"
        ev.evaluateAsString("/root/namespacedNode") == "Namespaced Value"
        ev.existsNode("/root/namespacedNode")

        and: "prefixed access does not work"
        ev.evaluateAsString("/root/test:namespacedNode") == ""
        !ev.existsNode("/root/test:namespacedNode")
    }
}