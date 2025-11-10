package com.example.utils.xml.providers.document

import com.example.utils.xml.context.XmlContext
import com.example.utils.xml.providers.xpath.XPathEvaluator
import org.w3c.dom.Node
import spock.lang.Specification

class XmlDocumentSpec extends Specification {

    def evaluator = Mock(XPathEvaluator)

    def "delegates convenience methods to XPathEvaluator"() {
        given:
        def context = Stub(XmlContext) {
            getXPathEvaluator() >> evaluator
        }
        def doc = new XmlDocument(context)

        when:
        doc.getStringByXPath('/r/a/text()')
        doc.getBooleanByXPath('/flag')
        doc.getDoubleByXPath('/num')
        doc.getDecimalByXPath('/price')
        doc.getIntegerByXPath('/int')
        doc.getLongByXPath('/long')
        doc.getIntByXPath('/int2')
        doc.getNodeByXPath('/node')
        doc.getNodesByXPath('/nodes/node')

        then:
        1 * evaluator.evaluateAsString(_)
        1 * evaluator.evaluateAsBoolean(_)
        1 * evaluator.evaluateAsDouble(_)
        1 * evaluator.evaluateAsDecimal(_)
        1 * evaluator.evaluateAsInteger(_)
        1 * evaluator.evaluateAsLong(_)
        1 * evaluator.evaluateAsInt(_)
        1 * evaluator.evaluateAsNode(_) >> Stub(Node)
        1 * evaluator.evaluateAsListOfNodes(_) >> []
    }
}

