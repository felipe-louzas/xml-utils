package com.example.utils.xml.services.document.loader

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import com.example.utils.xml.config.XmlAutoConfiguration
import com.example.utils.xml.exceptions.XmlException
import com.example.utils.xml.services.document.XmlDocument
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.w3c.dom.Document
import org.xml.sax.SAXParseException
import spock.lang.Specification
import spock.lang.Unroll

import javax.xml.parsers.DocumentBuilderFactory
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths

@DirtiesContext
@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration], properties = [
        "utils.xml.security.secure-processing=false",
        "utils.xml.security.disable-dtd=false"
])
class XmlLoaderSpec extends Specification {

    Path tempFile
    XmlLoader loader

    def setup() {
        tempFile = Files.createTempFile("test", ".xml")
        loader = Xml.loader
    }

    def cleanup() {
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
        doc.getDecimalByXPath("/root/num") == new BigDecimal("123.45")
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

    /* ------------------- Fluxo Negativos  ------------------- */

    def "should throw exception when XML string is null"() {
        when:
        loader.load(null as String)

        then:
        thrown(IllegalArgumentException)
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
        def ex = thrown(XmlException)
        ex.cause instanceof NoSuchFileException
    }

    def "should throw exception for malformed XML string"() {
        given:
        def xml = "<root><unclosed></root>"

        when:
        loader.load(xml)

        then:
        def e = thrown(XmlException)
        e.message =~ /(?i)Parse|Malformed|XML/
    }

    def "should throw exception for invalid XML encoding"() {
        given:
        byte[] badBytes = [(byte) 0xC3, (byte) 0x28] // Invalid UTF-8
        InputStream is = new ByteArrayInputStream(badBytes)

        when:
        loader.load(is)

        then:
        thrown(XmlException)
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

    @Unroll
    def "should throw exception for XML document without root element"() {
        given:
        InputStream is = new ByteArrayInputStream(xml.bytes)

        when:
        loader.load(is)

        then:
        def ex = thrown(XmlException)
        ex.cause instanceof SAXParseException

        where:
        xml << [
                """<?xml version="1.0" encoding="UTF-8"?>""",
                """<!-- Just a comment -->""",
                """     \n\t     """,
                """<!-- Comment 1 -->\n<!-- Comment 2 -->""",
                """<!-- Comment 1 -->      <!-- Comment 2 -->""",
                """<?processing instruction?>""",
                """<![CDATA[ hello world ]]>""",
                """<?xml version="1.0" encoding="UTF-8"?><![CDATA[ hello world ]]>""",
                """<?xml version="1.0" encoding="UTF-8"?><!-- Just a comment --><![CDATA[ hello world ]]>""",
                """<root>abc""",
                """<?xml version="1.0" encoding="UTF-8"?><!-- Just a comment -->""",
                """<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE note SYSTEM "Note.dtd">""",
                """<?xml version="1.0" encoding="UTF-8"?><!DOCTYPE note [<!ELEMENT note ANY>]>""",
                """<!DOCTYPE note [\n<!ELEMENT note ANY>\n<!ENTITY author "John Doe">\n]>\n""",
        ]
    }
}

