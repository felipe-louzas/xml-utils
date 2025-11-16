package com.example.utils.xml.config

import com.example.utils.xml.Xml
import spock.lang.Specification

class XmlManualConfigSpec extends Specification {

    def cleanup() {
        Xml.reset()
    }

    def "Xml pode ser inicializado com configurações padrão"() {
        given:
        def defaultConfig = XmlConfig.getDefault()

        expect:
        Xml.config == defaultConfig
    }

    def "Xml pode ser inicializado com configuração customizada"() {
        given:
        def customConfig = XmlConfig.builder()
                .parserConfig(p -> p.namespaceAware(false))
                .formatterConfig(f -> f.indent(false).indentAmount(4))
                .build()

        when:
        Xml.configure(customConfig)

        then:
        Xml.config == customConfig
        Xml.config != XmlConfig.getDefault()
    }

    def "Xml pode criar uma instância configurada independente"() {
        given:
        def customConfig = XmlConfig.builder()
                .parserConfig(p -> p.namespaceAware(false))
                .formatterConfig(f -> f.indent(false).indentAmount(4))
                .build()
        def otherConfig = XmlConfig.builder()
                .parserConfig(p -> p.namespaceAware(true))
                .formatterConfig(f -> f.indent(true).indentAmount(6))
                .build()

        when:
        Xml.configure(customConfig)
        def defaultLoader = Xml.loader()
        def customLoader = Xml.loader(otherConfig)

        then:
        Xml.config == customConfig
        Xml.config != XmlConfig.getDefault()
        Xml.config != otherConfig

        and:
        defaultLoader.config == customConfig
        defaultLoader.config != XmlConfig.getDefault()
        defaultLoader.config != otherConfig

        and:
        customLoader.config != customConfig
        customLoader.config != XmlConfig.getDefault()
        customLoader.config != Xml.config
        customLoader.config == otherConfig
    }
}
