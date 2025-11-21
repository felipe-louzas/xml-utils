package com.example.utils.xml

import com.example.utils.xml.config.XmlConfig
import com.example.utils.xml.services.XmlService
import com.example.utils.xml.services.document.XmlDocument
import com.example.utils.xml.services.document.loader.XmlLoader
import org.w3c.dom.Document
import spock.lang.Specification

import java.nio.file.Path

class XmlSpec extends Specification {

    XmlDocument doc = Mock()
    XmlLoader loader = Mock()
    XmlService service = Mock() {
        getLoader() >> loader
    }

    def cleanup() {
        Xml.reset()
    }

    /* ------------------------------------------------------------
       Initialization and configuration lifecycle
       ------------------------------------------------------------ */

    def "initialize(loader) should set default instance and use provided loader"() {
        when:
        Xml.initialize(service)

        then:
        Xml.service.is(service)
    }

    def "configure(config) should initialize Xml with XmlLoader created from config"() {
        given:
        def config = Mock(XmlConfig)

        when:
        Xml.initialize(config)

        then:
        Xml.config.is(config)
        Xml.service.config.is(config)
    }

    def "reset() should rebuild default instance with default configuration"() {
        given:
        def customConfig = Mock(XmlConfig)
        def defaultConfig = XmlConfig.getDefault()

        when:
        Xml.initialize(customConfig)

        then:
        Xml.config == customConfig
        Xml.service.config == customConfig

        when:
        def oldService = Xml.service
        Xml.reset()

        then:
        !Xml.service.is(oldService)

        and:
        Xml.config != customConfig
        Xml.service.config != customConfig

        and:
        Xml.config == defaultConfig
        Xml.service.config == defaultConfig
    }

    def "cannot initialize Xml with null loader"() {
        when:
        Xml.initialize(null as XmlService)

        then:
        thrown(IllegalArgumentException)
    }

    def "cannot configure Xml with null config"() {
        when:
        Xml.initialize(null as XmlConfig)

        then:
        thrown(IllegalArgumentException)
    }

    def "reset() with uninitialized does not throw exception"() {
        when:
        Xml.reset()
        Xml.reset()

        then:
        noExceptionThrown()

        and:
        Xml.config == XmlConfig.getDefault()
        Xml.service.config == XmlConfig.getDefault()
    }

    def "getService() without prior initialization should set default instance"() {
        when:
        def service = Xml.service

        then:
        service != null
        Xml.config == XmlConfig.getDefault()
        service.config == XmlConfig.getDefault()
    }

    def "getConfig() without prior initialization should set default instance"() {
        when:
        def config = Xml.getConfig()

        then:
        config == XmlConfig.getDefault()
        Xml.service.config == XmlConfig.getDefault()
    }

    def "cannot initialize Xml with loader after prior initialization"() {
        given:
        Xml.initialize(service)

        when:
        Xml.initialize(service)

        then:
        thrown(IllegalStateException)
    }

    def "cannot configure Xml with config after prior initialization"() {
        given:
        def config = Mock(XmlConfig)
        Xml.initialize(service)
        config
        when:
        Xml.initialize(config)

        then:
        thrown(IllegalStateException)
    }

    def "cannot configure Xml with config after prior configuration"() {
        given:
        def config = Mock(XmlConfig)
        Xml.initialize(config)

        when:
        Xml.initialize(config)

        then:
        thrown(IllegalStateException)
    }

    def "cannot initialize Xml after prior configuration"() {
        given:
        def config = Mock(XmlConfig)
        Xml.initialize(config)

        when:
        Xml.initialize(service)

        then:
        thrown(IllegalStateException)
    }

    /* ------------------------------------------------------------
       Delegation methods
       ------------------------------------------------------------ */

    def "load(Document) should delegate to loader.load(Document)"() {
        given:
        def dom = Mock(Document)
        loader.load(dom) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load(dom)

        then:
        result.is(doc)
    }

    def "load(CharSequence) should delegate to loader.load(CharSequence)"() {
        given:
        loader.load(_ as String) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load("xml")

        then:
        result.is(doc)
    }

    def "load(File) should delegate to loader.load(File)"() {
        given:
        def file = Mock(File)
        loader.load(file) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load(file)

        then:
        result.is(doc)
    }

    def "load(Path) should delegate to loader.load(Path)"() {
        given:
        def path = Mock(Path)
        loader.load(path) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load(path)

        then:
        result.is(doc)
    }

    def "load(InputStream) should delegate to loader.load(InputStream)"() {
        given:
        def inputStream = new ByteArrayInputStream("<xml/>".bytes)
        loader.load(inputStream) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load(inputStream)

        then:
        result.is(doc)
    }

    def "load(Reader) should delegate to loader.load(Reader)"() {
        given:
        def reader = new StringReader("<xml/>")
        loader.load(reader) >> doc

        when:
        Xml.initialize(service)
        def result = Xml.load(reader)

        then:
        result.is(doc)
    }

    /* ------------------------------------------------------------
       Loader and config access
       ------------------------------------------------------------ */

    def "withConfig(XmlConfig) should create new XmlService with the provided config"() {
        given:
        def config = Mock(XmlConfig)
        def defaultService = Xml.service
        def customService = Xml.withConfig(config)

        expect:
        !defaultService.is(customService)

        and:
        defaultService.config != config
        defaultService.config == Xml.getConfig()
        defaultService.config == XmlConfig.getDefault()

        and:
        customService.config != Xml.getConfig()
        customService.config != XmlConfig.getDefault()
        customService.config == config
    }

    def "loader(XmlConfig) should create new XmlLoader with the provided config"() {
        given:
        def config = Mock(XmlConfig)
        def defaultLoader = Xml.getLoader()
        def customLoader = Xml.getLoader(config)

        expect:
        !defaultLoader.is(customLoader)
    }

    def "getConfig() should return current service config"() {
        given:
        def config = Mock(XmlConfig)
        service.getConfig() >> config

        when:
        Xml.initialize(service)

        then:
        Xml.config == config
    }
}
