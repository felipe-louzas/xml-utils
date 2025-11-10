package com.example.utils.xml.providers.xpath

import com.example.utils.xml.providers.xpath.compiler.DefaultXPathCompiler
import com.example.utils.xml.providers.xpath.exceptions.InvalidXPathExpressionException
import spock.lang.Specification

import javax.xml.xpath.XPath
import javax.xml.xpath.XPathFactory

class DefaultXPathCompilerSpec extends Specification {

    def "compile valid expression returns XPathExpression"() {
        given:
        def xpath = XPathFactory.newInstance().newXPath()
        def compiler = new DefaultXPathCompiler(xpath)

        when:
        def expr = compiler.compile("/root/item[1]")

        then:
        expr != null
        expr.evaluate("<root><item/></root>").trim() == '' // evaluation returns string by default
    }

    def "compile invalid expression throws InvalidXPathExpressionException"() {
        given:
        def xpath = XPathFactory.newInstance().newXPath()
        def compiler = new DefaultXPathCompiler(xpath)

        when:
        compiler.compile("/root[" )

        then:
        def ex = thrown(InvalidXPathExpressionException)
        ex.expression == '/root['
        ex.message.contains('Invalid XPath expression')
    }
}

