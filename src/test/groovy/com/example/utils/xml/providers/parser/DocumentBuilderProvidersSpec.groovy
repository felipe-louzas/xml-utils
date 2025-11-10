package com.example.utils.xml.providers.parser

import com.example.utils.pool.Pool
import spock.lang.Specification

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class DocumentBuilderProvidersSpec extends Specification {

    def "FactoryDocumentBuilderProvider uses factory to build and parse"() {
        given:
        def factory = Mock(com.example.utils.xml.providers.factory.XmlFactory)
        def dbf = DocumentBuilderFactory.newInstance()
        def builder = dbf.newDocumentBuilder()
        factory.getDocumentBuilderFactory() >> dbf
        def provider = new FactoryDocumentBuilderProvider(factory)

        when:
        def result = provider.parse { DocumentBuilder b -> assert b != null; return 42 }

        then:
        result == 42
    }

    def "PoolingDocumentBuilderProvider delegates to pool.use"() {
        given:
        def pool = Mock(Pool)
        def provider = new PoolingDocumentBuilderProvider(pool)

        when:
        def r = provider.parse { DocumentBuilder b -> return 10 }

        then:
        1 * pool.use(_ as org.apache.commons.lang3.function.FailableFunction) >> { fn -> fn.apply(Mock(DocumentBuilder)) }
        r == 10
    }
}

