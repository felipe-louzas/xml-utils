package com.example.utils.xml.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest(classes = [XmlAutoConfiguration], properties = [
        "utils.xml.formatter.indent-amount=4",
        "utils.xml.formatter.indent=false",
        "utils.xml.formatter.omit-xml-declaration=true",
        "utils.xml.formatter.line-separator=\\r\\n",

        // Pool – sobrescreve apenas as configurações do documentBuilder, mantendo o validator com valores padrão
        // (define um valor máximo negativo para testar um edge case)
        "utils.xml.pool.document-builder.enabled=true",
        "utils.xml.pool.document-builder.max-pool-size=8",
        "utils.xml.pool.validator.max-pool-size=-5",

        // Cache – sobrescreve apenas o cache de XPath
        "utils.xml.cache.xpath.enabled=true",
        "utils.xml.cache.xpath.cache-name=my-xpath-cache",

        // Transformer
        "utils.xml.transformer.encoding=ISO-8859-1",

        // Segurança – desativa os padrões seguros para exercitar a lógica derivada
        "utils.xml.security.secure-processing=false",
        "utils.xml.security.disable-dtd=false",
        "utils.xml.security.restrict-external-entities=false",
        "utils.xml.security.protocols-external-dtd=file,http",

        // Validação
        "utils.xml.validation.fail-on-warning=true",

        // Mapeamentos e resolvers de XPath
        "utils.xml.xpath.default-namespace-mappings.ns1=http://example.com/ns1",
        "utils.xml.xpath.function-resolver-class=com.example.CustomFunctionResolver",
        "utils.xml.xpath.variable-resolver-class=com.example.CustomVariableResolver",

        // Parser
        "utils.xml.parser.namespace-aware=false",
        "utils.xml.parser.coalescing=false",
        "utils.xml.parser.expand-entity-references=true",
        "utils.xml.parser.ignore-comments=false",
        "utils.xml.parser.ignore-whitespace=true",
        "utils.xml.parser.xinclude-aware=true"
])
class XmlConfigBindingSpec extends Specification {

    @Autowired
    XmlConfig xmlConfig

    def "XmlConfig vincula propriedades explícitas de formatter e pool"() {
        expect:
        xmlConfig.formatter.indentAmount == 4
        !xmlConfig.formatter.indent
        xmlConfig.formatter.omitXmlDeclaration
        xmlConfig.formatter.lineSeparator == "\r\n"

        xmlConfig.pool.documentBuilder.enabled
        xmlConfig.pool.documentBuilder.maxPoolSize == 8

        // O pool de validator não foi habilitado explicitamente — deve permanecer desativado,
        // mas o valor máximo ainda precisa ser vinculado
        !xmlConfig.pool.validator.enabled
        xmlConfig.pool.validator.maxPoolSize == -5
    }

    def "XmlConfig mantém valores padrão para componentes não sobrescritos"() {
        expect:
        // O método do Transformer deve continuar sendo o padrão "xml",
        // já que apenas o encoding foi sobrescrito
        xmlConfig.transformer.method == "xml"

        // O cache de schema deve usar o nome padrão definido em DefaultXmlConfig
        xmlConfig.cache.schema.cacheName == "utils.xml.schema"

        // Formatter deve preservar os padrões de preservação/normalização de espaços em branco
        !xmlConfig.formatter.preserveWhitespace
        !xmlConfig.formatter.normalizeWhitespace

        // Valores padrão do pool de transformer
        !xmlConfig.pool.transformer.enabled
        xmlConfig.pool.transformer.maxPoolSize == 16
    }

    def "XmlConfig vincula corretamente as propriedades do parser"() {
        expect:
        !xmlConfig.parser.namespaceAware
        !xmlConfig.parser.coalescing
        xmlConfig.parser.expandEntityReferences
        !xmlConfig.parser.ignoreComments
        xmlConfig.parser.ignoreWhitespace
        xmlConfig.parser.xincludeAware
    }

    def "XmlConfig vincula corretamente o mapa e as classes de resolvers de XPath"() {
        expect:
        xmlConfig.xpath.defaultNamespaceMappings.size() == 1
        xmlConfig.xpath.defaultNamespaceMappings['ns1'] == 'http://example.com/ns1'
        xmlConfig.xpath.functionResolverClass == 'com.example.CustomFunctionResolver'
        xmlConfig.xpath.variableResolverClass == 'com.example.CustomVariableResolver'
    }

    def "Propriedades de segurança refletem corretamente os valores configurados"() {
        expect:
        // secure-processing e disable-dtd foram desativados; portanto, o DTD não deve ser desabilitado
        !xmlConfig.security.secureProcessing
        !xmlConfig.security.disableDtd
        !xmlConfig.security.restrictExternalEntities

        // O helper derivado deve refletir a combinação de flags
        // DefaultXmlConfig.SecurityConfig.isDisableDtd() retorna secureProcessing || disableDtd
        !xmlConfig.security.disableDtd
        xmlConfig.security.allowExternalEntities

        // As strings de protocolos devem ser vinculadas corretamente
        xmlConfig.security.protocolsExternalDTD == 'file,http'
    }

    def "Propriedades de validação são vinculadas corretamente"() {
        expect:
        xmlConfig.validation.failOnWarning
    }

    def "DefaultXmlConfig usa valores padrão seguros e coerentes"() {
        when:
        def cfg = XmlConfig.builder().build()

        then:
        // Segurança: deve usar padrões seguros
        cfg.security.secureProcessing
        cfg.security.disableDtd
        !cfg.security.isAllowExternalEntities()

        // Pools: tamanhos padrão
        cfg.pool.documentBuilder.maxPoolSize == 16
        cfg.pool.validator.maxPoolSize == 16
        cfg.pool.transformer.maxPoolSize == 16

        // Formatter: indentação e espaços
        cfg.formatter.indent
        cfg.formatter.indentAmount == 2
        cfg.formatter.lineSeparator == "\n"

        // Transformer: encoding e método padrão
        cfg.transformer.encoding == "UTF-8"
        cfg.transformer.method == "xml"

        // Cache: nomes de cache padrão
        cfg.cache.xpath.cacheName == "utils.xml.xpath"
        cfg.cache.schema.cacheName == "utils.xml.schema"
    }

    def "Builders aplicam corretamente funções configuradoras"() {
        when:
        def cfg = XmlConfig.builder()
                .parserConfig(p -> p.namespaceAware(false))
                .securityConfig(s -> s.secureProcessing(false).disableDtd(false))
                .poolConfig(p -> p.documentBuilderConfig(d -> d.enabled(true).maxPoolSize(8)))
                .formatterConfig(f -> f.indent(false).indentAmount(4))
                .transformerConfig(t -> t.encoding("ISO-8859-1"))
                .cacheConfig(c -> c.xpathConfig(x -> x.enabled(true).cacheName("xpath-test-cache")))
                .validationConfig(v -> v.failOnWarning(true))
                .xpathConfig(x -> x.functionResolverClass("com.example.MyResolver"))
                .build()

        then:
        !cfg.parser.namespaceAware
        !cfg.security.secureProcessing
        !cfg.security.disableDtd
        cfg.pool.documentBuilder.enabled
        cfg.pool.documentBuilder.maxPoolSize == 8
        !cfg.formatter.indent
        cfg.formatter.indentAmount == 4
        cfg.transformer.encoding == "ISO-8859-1"
        cfg.cache.xpath.enabled
        cfg.cache.xpath.cacheName == "xpath-test-cache"
        cfg.validation.failOnWarning
        cfg.xpath.functionResolverClass == "com.example.MyResolver"
    }

    @Unroll
    def "Comportamento derivado de segurança é consistente para secureProcessing=#secureProcessing, disableDtd=#disableDtd, restrictExternalEntities=#restrictExternalEntities"() {
        given:
        def security = new XmlConfigProperties.SecurityConfig(
                secureProcessing,
                disableDtd,
                restrictExternalEntities,
                "", "", ""
        )

        expect:
        security.isDisableDtd() == expectedDisableDtd
        security.isAllowExternalEntities() == expectedAllowExternalEntities

        where:
        secureProcessing | disableDtd | restrictExternalEntities || expectedDisableDtd | expectedAllowExternalEntities
        true             | false      | true                     || true               | false
        true             | false      | true                     || true               | false
        false            | false      | true                     || false              | false
        false            | false      | false                    || false              | true
    }

    def "Mapas de XPath devem ser imutáveis por padrão ao criar config pelo builder"() {
        given:
        def cfg = XmlConfig.builder().build()

        when:
        cfg.xpath.defaultNamespaceMappings.put("x", "http://x")

        then:
        thrown(UnsupportedOperationException)
    }

    def "Mapas de XPath devem ser imutáveis por padrão ao criar o config pelo property binding"() {
        when:
        xmlConfig.xpath.defaultNamespaceMappings.put("x", "http://x")

        then:
        thrown(UnsupportedOperationException)
    }

}
