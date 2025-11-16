package com.example.utils.xml.services.xpath

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import com.example.utils.xml.config.XmlAutoConfiguration
import com.example.utils.xml.services.xpath.exceptions.InvalidXPathExpressionException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification
import spock.lang.Unroll

@DirtiesContext
@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration], properties = [
        "utils.xml.xpath.default-namespace-mappings.loc=https://example.com/local",
        "utils.xml.xpath.default-namespace-mappings.d=https://example.com/document",
        "utils.xml.xpath.default-namespace-mappings.h=https://example.com/hr",
        "utils.xml.xpath.default-namespace-mappings.f=https://example.com/finance",
        "utils.xml.xpath.default-namespace-mappings.def=https://example.com/default"
])
class NSXPathEvaluatorSpec extends Specification {

    def sampleXml = '''
        <doc:root xmlns:doc="https://example.com/document"
                  xmlns:hr="https://example.com/hr"
                  xmlns:fin="https://example.com/finance"
                  xmlns="https://example.com/default">

            <person id="1" hr:status="pending">
                <name>Default Maria</name>
                <age>30</age>
            </person>

            <hr:person id="2" hr:status="Active">
                <hr:name>HR João</hr:name>
            </hr:person>

            <fin:report type="Q4">
                <fin:salary>90000</fin:salary>
            </fin:report>

            <globalNode xmlns="">No Namespace Here</globalNode>
            
            <localNamespaceNode xmlns="https://example.com/local" xmlns:inner="https://example.com/inner">
                <child>No Namespace Child</child>
                <inner:child>Inner Namespaced Child</inner:child>
            </localNamespaceNode>
        </doc:root>
    '''

    def "should select nodes using explicitly prefixed namespaces (hr, fin)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsString("/d:root/h:person/h:name") == "HR João"
        ev.evaluateAsString("/doc:root/hr:person/hr:name") == "HR João"
        ev.evaluateAsString("/d:root/f:report/f:salary") == "90000"
        ev.evaluateAsString("/doc:root/fin:report/fin:salary") == "90000"

        and:
        ev.existsNode("/d:root/h:person")
        ev.existsNode("/doc:root/hr:person")
        ev.existsNode("/doc:root/fin:report")
    }

    def "should select default namespace nodes using a *mapped prefix*"() {
        given: "XML where <person> is in the 'http://example.com/default' namespace"
        def ev = Xml.load(sampleXml).xpath()

        when: "Querying using the mapped prefix 'def'"
        def personName = ev.evaluateAsString("/d:root/def:person/def:name")
        def personAge = ev.evaluateAsInt("/d:root/def:person/def:age")

        then: "The nodes are found"
        personName == "Default Maria"
        personAge == 30
        ev.existsNode("/d:root/def:person")
    }

    def "should select default namespace nodes using the *default prefix*"() {
        given: "XML where <person> is in the default namespace"
        def ev = Xml.load(sampleXml).xpath()

        when: "Querying using the default prefix"
        def personName = ev.evaluateAsString("/d:root/:person/:name")
        def personAge = ev.evaluateAsInt("/d:root/:person/:age")

        then: "The nodes are found"
        personName == "Default Maria"
        personAge == 30
        ev.existsNode("/d:root/:person")
    }

    def "should *not* select default namespace nodes without a prefix"() {
        given: "XML where <person> is in a default namespace"
        def ev = Xml.load(sampleXml).xpath()

        when: "Querying *without* a prefix (the common mistake)"
        def personName = ev.evaluateAsString("/d:root/person/name")
        def exists = ev.existsNode("/d:root/person")

        then: "No node is found, because unprefixed paths refer to the 'no namespace'"
        personName == ""
        !exists
    }

    def "should select global (no-namespace) nodes using no prefix"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "The 'globalNode' is found without a prefix"
        ev.evaluateAsString("/d:root/globalNode") == "No Namespace Here"
        ev.existsNode("/d:root/globalNode")
    }

    @Unroll
    def "should select namespaced and non-namespaced attributes (Path: #path)"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect:
        ev.evaluateAsString(path) == expected

        where:
        path                              | expected  | description
        "/d:root/def:person/@id"          | "1"       | "Non-namespaced attribute on default-ns node"
        "/doc:root/h:person/@id"          | "2"       | "Non-namespaced attribute on prefixed node"
        "/d:root/f:report/@type"          | "Q4"      | "Non-namespaced attribute on prefixed node"
        "/doc:root/def:person/@hr:status" | "pending" | "Namespaced attribute on default-ns node"
        "/d:root/hr:person/@h:status"     | "Active"  | "Namespaced attribute on prefixed node"
        "/doc:root/def:person/@status"    | ""        | "FAIL: Attribute is namespaced, query is not"
    }

    def "should select nodes in a locally defined namespace"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "Find node using the locally defined prefix"
        ev.evaluateAsString("/doc:root/loc:localNamespaceNode/loc:child") == "No Namespace Child"

        and: "Fail to find node without prefix (no default namespace defined for it)"
        ev.evaluateAsString("/doc:root/loc:localNamespaceNode/child") == ""

        when: "Inner prefix was not defined in the document root or config"
        ev.evaluateAsString("/d:root/loc:localNamespaceNode/inner:child") == ""

        then:
        def ex = thrown(InvalidXPathExpressionException)
        ex.message.contains("/d:root/loc:localNamespaceNode/inner:child")

        and:
        ex.cause.message.containsIgnoreCase("Prefix")
        ex.cause.message.contains("inner")
    }

    def "should handle complex predicates mixing namespaces"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        expect: "Find default-ns person based on namespaced attribute"
        ev.evaluateAsString("/d:root/def:person[@h:status='pending']/def:name") == "Default Maria"

        and: "Find prefixed-ns person based on non-namespaced attribute"
        ev.evaluateAsString("/d:root/h:person[@id='2']/h:name") == "HR João"

        and: "Count all nodes with a 'status' attribute from the 'h' namespace"
        ev.evaluateAsInt("count(//@h:status)") == 2

        and: "Count all 'name' nodes, regardless of namespace (using local-name())"
        ev.evaluateAsInt("count(//*[local-name()='name'])") == 2
    }

    def "findNode and findNodes should work with namespace-aware paths"() {
        given:
        def ev = Xml.load(sampleXml).xpath()

        when: "Find a namespaced node"
        def hrPersonEval = ev.findNode("/d:root/h:person").get()

        then: "Relative paths from that node also use the namespace context"
        hrPersonEval.evaluateAsString("h:name") == "HR João"
        hrPersonEval.evaluateAsString("@h:status") == "Active"
        hrPersonEval.evaluateAsString("@id") == "2"
        !hrPersonEval.existsNode("name") // 'name' (no prefix) does not exist

        when: "Find a default namespace node"
        def defPersonEval = ev.findNode("/d:root/def:person").get()

        then: "Relative paths work"
        defPersonEval.evaluateAsString("def:name") == "Default Maria"
        defPersonEval.evaluateAsInt("def:age") == 30
        defPersonEval.evaluateAsString("@h:status") == "pending"
        defPersonEval.evaluateAsString("@id") == "1"
    }
}