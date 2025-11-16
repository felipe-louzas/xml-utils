package com.example.utils.xml.services.document

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import com.example.utils.xml.config.XmlAutoConfiguration
import com.example.utils.xml.services.xpath.exceptions.XPathEvaluationException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.w3c.dom.Node
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@DirtiesContext
@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration])
class XmlDocumentBlackBoxSpec extends Specification {

    def sampleXml = '''
        <root>
            <person id="1">
                <name>Maria</name>
                <age>30</age>
                <active>true</active>
                <salary>1234.56</salary>
            </person>
            <person id="2">
                <name>João</name>
                <age>42</age>
                <active>false</active>
                <salary>0.00</salary>
            </person>
            <empty/>
            <tagWithEmptyText></tagWithEmptyText>
        </root>
    '''

    @Subject
    XmlDocument document

    def setup() {
        document = Xml.load(sampleXml)
    }

    @Unroll
    def "#methodName should retrieve the *value* of a specific node"() {
        expect:
        Objects.equals(method.call(document, path), expectedValue)

        where:
        methodName          | path                     | method                                 | expectedValue
        "getStringByXPath"  | "/root/person[1]/name"   | { doc, p -> doc.getStringByXPath(p) }  | "Maria"
        "getBooleanByXPath" | "/root/person[1]/active" | { doc, p -> doc.getBooleanByXPath(p) } | true
        "getBooleanByXPath" | "/root/person[2]/active" | { doc, p -> doc.getBooleanByXPath(p) } | false
        "getDecimalByXPath" | "/root/person[1]/salary" | { doc, p -> doc.getDecimalByXPath(p) } | new BigDecimal("1234.56")
        "getBigIntByXPath"  | "/root/person[1]/age"    | { doc, p -> doc.getBigIntByXPath(p) }  | new BigInteger("30")
        "getLongByXPath"    | "/root/person[1]/age"    | { doc, p -> doc.getLongByXPath(p) }    | 30L
        "getIntByXPath"     | "/root/person[1]/age"    | { doc, p -> doc.getIntByXPath(p) }     | 30
    }

    @Unroll
    def "#methodName should return null for non-existent or empty-valued nodes"() {
        expect:
        method.call(document, path) == null

        where:
        methodName         | path                     | method
        "getStringByXPath" | "/root/nonexistent"      | { doc, p -> doc.getStringByXPath(p) }
        "getStringByXPath" | "/root/empty"            | { doc, p -> doc.getStringByXPath(p) }
        "getStringByXPath" | "/root/tagWithEmptyText" | { doc, p -> doc.getStringByXPath(p) }
        "getIntByXPath"    | "/root/nonexistent"      | { doc, p -> doc.getIntByXPath(p) }
        "getIntByXPath"    | "/root/empty"            | { doc, p -> doc.getIntByXPath(p) }
    }

    def "get*ByXPath methods throw exception for non-node-selecting XPath functions"() {
        given:
        def eval = document.xpath()

        when: "--- Using the raw XPath evaluator (evaluate*) ---"
        def evaluate = eval.evaluateAsString(expr)

        then: "The raw evaluator *correctly* computes the function"
        evaluate == expectedValue

        when: "An exception is thrown for getIntByXPath(...)"
        document.getIntByXPath(expr)
        then:
        thrown(XPathEvaluationException)

        when: "An exception is thrown for getDecimalByXPath(...)"
        document.getDecimalByXPath(expr)
        then:
        thrown(XPathEvaluationException)

        when: "An exception is thrown for getStringByXPath(...)"
        document.getStringByXPath(expr)
        then:
        thrown(XPathEvaluationException)

        where:
        expr                                  | expectedValue
        "count(/root/person)"                 | "2"
        "sum(/root/person/age)"               | "72"
        "string-length(/root/person[2]/name)" | "4"
    }

    def "getBooleanByXPath must return null for boolean-returning functions"() {
        given:
        def eval = document.xpath()
        def trueExpr = "count(/root/person) = 2"
        def falseExpr = "1 = 0"

        when: "Using the raw XPath evaluator (evaluateAsTrue)"
        def evalTrue = eval.evaluateAsTrue(trueExpr)
        def evalFalse = eval.evaluateAsFalse(falseExpr)

        then: "The raw evaluator works as expected"
        evalTrue
        evalFalse

        when: "Using the XmlDocument 'get*' helper method"
        document.getBooleanByXPath(trueExpr)
        then:
        thrown(XPathEvaluationException)

        when:
        document.getBooleanByXPath(falseExpr)
        then:
        thrown(XPathEvaluationException)
    }

    def "getNodeByXPath should return a real (non-null) node"() {
        when:
        def node = document.getNodeByXPath("/root/person[1]")

        then:
        node != null
        node instanceof Node
        node.getNodeName() == "person"
    }

    def "getNodeByXPath should return null for non-existent node"() {
        when:
        def node = document.getNodeByXPath("/root/nonexistent")

        then:
        node == null
    }

    def "getNodesByXPath should return a real list of nodes"() {
        when:
        def nodes = document.getNodesByXPath("/root/person")

        then:
        nodes != null
        nodes.size() == 2
        nodes[0] instanceof Node
        nodes[0].getNodeName() == "person"
    }

    def "getNodesByXPath should return an empty list for no matches"() {
        when:
        def nodes = document.getNodesByXPath("/root/nonexistent")

        then:
        nodes != null
        nodes.size() == 0
    }

    def "findNodeByXPath should return an evaluator that can be used"() {
        when:
        def optEval = document.findNodeByXPath("/root/person[1]")

        then:
        optEval.isPresent()
        // And we can use it for relative queries
        optEval.get().evaluateAsString("name") == "Maria"
        optEval.get().evaluateAsInt("age") == 30
    }

    def "findNodesByXPath should return a usable stream"() {
        when:
        def stream = document.findNodesByXPath("/root/person")

        then:
        def names = stream.map({ eval -> eval.evaluateAsString("name") }).collect()
        names == ["Maria", "João"]
    }
}