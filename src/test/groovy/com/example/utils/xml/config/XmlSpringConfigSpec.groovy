package com.example.utils.xml.config

import com.example.utils.xml.Xml
import com.example.utils.xml.beans.XmlDefaultInstanceInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

@DirtiesContext
@SpringBootTest(classes = [XmlDefaultInstanceInitializer, XmlAutoConfiguration], properties = [
        "utils.xml.formatter.indent-amount=4",
        "utils.xml.formatter.indent=false",
        "utils.xml.formatter.omit-xml-declaration=true"
])
class XmlSpringConfigSpec extends Specification {
    def "Xml foi inicializado pelo Spring"() {
        expect:
        Xml.config.formatter.indentAmount == 4
        !Xml.config.formatter.indent
        Xml.config.formatter.omitXmlDeclaration
        Xml.config != XmlConfig.builder().build()
    }
}
