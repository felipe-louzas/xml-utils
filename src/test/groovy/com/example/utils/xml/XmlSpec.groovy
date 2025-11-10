package com.example.utils.xml

import com.example.utils.xml.config.DefaultXmlConfig
import com.example.utils.xml.providers.document.XmlDocument
import spock.lang.Specification

class XmlSpec extends Specification {

    def "load from string produces XmlDocument"() {
        when:
        def xml = Xml.load('<r><a>1</a></r>')

        then:
        xml instanceof XmlDocument
    }

    def "loader with custom config returns independent instance"() {
        given:
        def cfg = DefaultXmlConfig.builder().parserConfig { it.namespaceAware(false) }.build()

        when:
        def loader = Xml.loader(cfg)
        def doc = loader.fromString('<root/>')

        then:
        doc instanceof XmlDocument
    }
}

