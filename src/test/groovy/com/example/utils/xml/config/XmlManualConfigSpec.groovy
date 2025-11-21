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
        Xml.initialize(customConfig)

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
        Xml.initialize(customConfig)
        def defaultService = Xml.getService()
        def customService = Xml.withConfig(otherConfig)

        then:
        Xml.config == customConfig
        Xml.config != XmlConfig.getDefault()
        Xml.config != otherConfig

        and:
        defaultService.config == customConfig
        defaultService.config != XmlConfig.getDefault()
        defaultService.config != otherConfig

        and:
        customService.config != customConfig
        customService.config != XmlConfig.getDefault()
        customService.config != Xml.config
        customService.config == otherConfig
    }
}
