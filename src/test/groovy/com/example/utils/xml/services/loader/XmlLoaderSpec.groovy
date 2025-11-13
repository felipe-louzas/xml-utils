package com.example.utils.xml.services.loader

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import com.example.utils.xml.config.XmlAutoConfiguration
import com.example.utils.xml.exceptions.XmlException
import com.example.utils.xml.services.document.XmlDocument
import org.springframework.boot.test.context.SpringBootTest
import org.w3c.dom.Document
import org.xml.sax.SAXParseException
import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration])
class XmlLoaderSpec extends Specification {

    Path tempFile
    XmlLoader loader

    def setup() {
        tempFile = Files.createTempFile("test", ".xml")
        loader = Xml.loader()
    }

    def cleanup() {
        Xml.reset()
        Files.deleteIfExists(tempFile)
    }

    def "should load valid XML from string"() {
        given:
        String xml = "<root><value>Hello</value></root>"

        when:
        XmlDocument doc = loader.load(xml)

        then:
        doc != null
        doc.getStringByXPath("/root/value") == "Hello"
    }

    def "should load valid XML from File"() {
        given:
        Files.writeString(tempFile, "<root><n>42</n></root>")

        when:
        XmlDocument doc = loader.load(tempFile.toFile())

        then:
        doc.getIntByXPath("/root/n") == 42
    }

    def "should load valid XML from Path"() {
        given:
        Files.writeString(tempFile, "<root><flag>true</flag></root>")

        when:
        XmlDocument doc = loader.load(tempFile)

        then:
        doc.getBooleanByXPath("/root/flag")
    }

    def "should load valid XML from InputStream"() {
        given:
        def xml = "<root><num>123.45</num></root>"
        InputStream stream = new ByteArrayInputStream(xml.bytes)

        when:
        XmlDocument doc = loader.load(stream)

        then:
        doc.getDoubleByXPath("/root/num") == 123.45d
    }

    def "should load valid XML from Reader"() {
        given:
        def xml = "<root><amount>987654321</amount></root>"
        Reader reader = new StringReader(xml)

        when:
        XmlDocument doc = loader.load(reader)

        then:
        doc.getLongByXPath("/root/amount") == 987654321L
    }

    def "should load valid XML from Document"() {
        given:
        def xml = "<root><data>ok</data></root>"
        def docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        Document dom = docBuilder.parse(new ByteArrayInputStream(xml.bytes))

        when:
        XmlDocument doc = loader.load(dom)

        then:
        doc.getStringByXPath("/root/data") == "ok"
    }

    /* ------------------- Edge Cases ------------------- */

    def "should throw exception when XML string is null"() {
        when:
        loader.load(null as String)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception when XML string is empty"() {
        when:
        loader.load("")

        then:
        def ex = thrown(XmlException)
        ex.cause instanceof SAXParseException
    }

    def "should throw exception when file does not exist"() {
        given:
        def missing = new File("nonexistent.xml")

        when:
        loader.load(missing)

        then:
        def ex = thrown(XmlException)
        ex.cause instanceof FileNotFoundException
    }

    def "should throw exception when path does not exist"() {
        given:
        def missing = Paths.get("does_not_exist.xml")

        when:
        loader.load(missing)

        then:
        def ex= thrown(XmlException)
        ex.cause instanceof NoSuchFileException
    }

    def "should throw exception when InputStream is null"() {
        when:
        loader.load(null as InputStream)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception when Reader is null"() {
        when:
        loader.load(null as Reader)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception when Document is null"() {
        when:
        loader.load(null as Document)

        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception for malformed XML string"() {
        given:
        def xml = "<root><unclosed></root>"

        when:
        loader.load(xml)

        then:
        def e = thrown(Exception)
        e.message =~ /(?i)Parse|Malformed|XML/
    }

    def "should throw exception for invalid XML encoding"() {
        given:
        byte[] badBytes = [(byte) 0xC3, (byte) 0x28] // Invalid UTF-8
        InputStream is = new ByteArrayInputStream(badBytes)

        when:
        loader.load(is)

        then:
        thrown(Exception)
    }

    def "should throw exception for empty XML file"() {
        given:
        Files.writeString(tempFile, "")

        when:
        loader.load(tempFile.toFile())

        then:
        def ex = thrown(XmlException)
        ex.cause instanceof SAXParseException
    }

    /* ------------------- XPath Evaluation ------------------- */

    def "should evaluate various XPath return types correctly"() {
        given:
        def xml = """
            <root>
                <s>abc</s>
                <i>10</i>
                <d>2.5</d>
                <b>true</b>
                <list><item>1</item><item>2</item></list>
            </root>
        """
        XmlDocument doc = loader.load(xml)

        expect:
        doc.getStringByXPath("/root/s") == "abc"
        doc.getIntByXPath("/root/i") == 10
        doc.getDoubleByXPath("/root/d") == 2.5d
        doc.getDecimalByXPath("/root/d") == new BigDecimal("2.5")
        doc.getBooleanByXPath("/root/b")
        doc.getNodesByXPath("/root/list/item").size() == 2
    }

    def "should return null or empty list for non-existent XPath"() {
        given:
        def xml = "<root><a>1</a></root>"
        XmlDocument doc = loader.load(xml)

        expect:
        doc.getStringByXPath("/root/missing").isEmpty()
        doc.getBigIntByXPath("/root/missing") == null
        doc.getDoubleByXPath("/root/missing") == null
        doc.getLongByXPath("/root/missing") == null
        doc.getNodeByXPath("/root/missing") == null
        doc.getNodesByXPath("/root/missing").isEmpty()
    }

    def "should handle namespaces if supported"() {
        given:
        def xml = """
            <ns:root xmlns:ns="http://example.com/ns">
                <ns:value>42</ns:value>
            </ns:root>
        """

        when:
        XmlDocument doc = loader.load(xml)

        then:
        // If namespaces are properly handled by XPath evaluator
        doc.getIntByXPath("/*[local-name()='root']/*[local-name()='value']") == 42
    }

    def "should support nested XPath evaluation"() {
        given:
        def xml = "<root><parent><child>ok</child></parent></root>"
        XmlDocument doc = loader.load(xml)

        when:
        def childValue = doc.findNodeByXPath("/root/parent")
                .evaluateAsString("child")

        then:
        childValue == "ok"
    }
}

