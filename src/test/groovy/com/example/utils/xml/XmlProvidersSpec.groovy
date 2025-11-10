package com.example.utils.xml

import com.example.utils.xml.config.DefaultXmlConfig
import com.example.utils.xml.config.XmlConfig
import com.example.utils.xml.providers.XmlProviders
import spock.lang.Specification

class XmlProvidersSpec extends Specification {

    def "default providers singleton initializes lazily and returns loader and factory"() {
        when:
        def p1 = XmlProviders.getDefaultProviders()
        def p2 = XmlProviders.getDefaultProviders()

        then:
        p1.is(p2) // singleton
        p1.xmlFactory != null
        p1.xmlLoader != null
    }

    def "custom config produces distinct providers"() {
        given:
        def custom = XmlConfig.builder()
                .parserConfig { it.namespaceAware(false).ignoreComments(false) }
                .formatterConfig { it.indent(false).indentAmount(4) }
                .build()

        when:
        def providers = XmlProviders.of(custom)

        then:
        !providers.is(XmlProviders.getDefaultProviders())
        providers.xmlFactory.documentBuilderFactory == null // lazy not yet initialized

        when: "trigger lazy init"
        def dbf = providers.xmlFactory.getDocumentBuilderFactory()

        then:
        dbf.isNamespaceAware() == false
        dbf.isIgnoringComments() == false
    }

    def "Xml.initialize replaces default providers"() {
        given:
        def cfg = DefaultXmlConfig.builder().parserConfig { it.namespaceAware(false) }.build()

        when:
        Xml.initialize(cfg)
        def providers = XmlProviders.getDefaultProviders()

        then:
        providers.xmlFactory.getDocumentBuilderFactory().isNamespaceAware() == false
    }
}

