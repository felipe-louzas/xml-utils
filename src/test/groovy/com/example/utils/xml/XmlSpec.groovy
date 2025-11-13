package com.example.utils.xml

import com.example.utils.xml.config.XmlConfig
import com.example.utils.xml.services.document.XmlDocument
import com.example.utils.xml.services.loader.XmlLoader
import org.w3c.dom.Document
import spock.lang.Specification

import java.nio.file.Path

class XmlSpec extends Specification {

    XmlLoader loader = Mock()
    XmlDocument doc = Mock()

    def cleanup() {
        Xml.reset()
    }

    /* ------------------------------------------------------------
       Initialization and configuration lifecycle
       ------------------------------------------------------------ */

    def "initialize(loader) should set default instance and use provided loader"() {
        when:
        Xml.initialize(loader)

        then:
        Xml.loader().is(loader)
    }

    def "configure(config) should initialize Xml with XmlLoader created from config"() {
        given:
        def config = Mock(XmlConfig)

        when:
        Xml.configure(config)

        then:
        Xml.config.is(config)
        Xml.loader().config.is(config)
        Xml.loader().providers.config.is(config)
        Xml.loader().providers.xmlFactory.config.is(config)
    }

    def "reset() should rebuild default instance with default configuration"() {
        given:
        def customConfig = Mock(XmlConfig)
        def defaultConfig = XmlConfig.getDefault()

        when:
        Xml.configure(customConfig)

        then:
        Xml.config == customConfig
        Xml.loader().config == customConfig

        when:
        def oldLoader = Xml.loader()
        Xml.reset()

        then:
        !Xml.loader().is(oldLoader)

        and:
        Xml.config != customConfig
        Xml.loader().config != customConfig

        and:
        Xml.config == defaultConfig
        Xml.loader().config == defaultConfig
    }

    def "cannot initialize Xml with null loader"() {
        when:
        Xml.initialize(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "cannot configure Xml with null config"() {
        when:
        Xml.configure(null)

        then:
        thrown(IllegalArgumentException)
    }

    def "reset() without prior initialization should set default instance"() {
        when:
        Xml.reset()

        then:
        Xml.config == XmlConfig.getDefault()
        Xml.loader().config == XmlConfig.getDefault()
    }

    def "loader() without prior initialization should set default instance"() {
        when:
        def loader = Xml.loader()

        then:
        loader != null
        Xml.config == XmlConfig.getDefault()
        loader.config == XmlConfig.getDefault()
    }

    def "getConfig() without prior initialization should set default instance"() {
        when:
        def config = Xml.getConfig()

        then:
        config == XmlConfig.getDefault()
        Xml.loader().config == XmlConfig.getDefault()
    }

    def "cannot initialize Xml with loader after prior initialization"() {
        given:
        Xml.initialize(loader)

        when:
        Xml.initialize(loader)

        then:
        thrown(IllegalStateException)
    }

    def "cannot configure Xml with config after prior initialization"() {
        given:
        def config = Mock(XmlConfig)
        Xml.initialize(loader)

        when:
        Xml.configure(config)

        then:
        thrown(IllegalStateException)
    }

    def "cannot configure Xml with config after prior configuration"() {
        given:
        def config = Mock(XmlConfig)
        Xml.configure(config)

        when:
        Xml.configure(config)

        then:
        thrown(IllegalStateException)
    }

    /* ------------------------------------------------------------
       Delegation methods
       ------------------------------------------------------------ */

    def "load(Document) should delegate to loader.load(Document)"() {
        given:
        def dom = Mock(Document)
        Xml.initialize(loader)
        loader.load(dom) >> doc

        expect:
        Xml.load(dom) == doc
    }

    def "load(CharSequence) should delegate to loader.load(CharSequence)"() {
        given:
        Xml.initialize(loader)
        loader.load("xml") >> doc

        expect:
        Xml.load("xml") == doc
    }

    def "load(File) should delegate to loader.load(File)"() {
        given:
        def file = Mock(File)
        Xml.initialize(loader)
        loader.load(file) >> doc

        expect:
        Xml.load(file) == doc
    }

    def "load(Path) should delegate to loader.load(Path)"() {
        given:
        def path = Mock(Path)
        Xml.initialize(loader)
        loader.load(path) >> doc

        expect:
        Xml.load(path) == doc
    }

    def "load(InputStream) should delegate to loader.load(InputStream)"() {
        given:
        def inputStream = new ByteArrayInputStream("<xml/>".bytes)
        Xml.initialize(loader)
        loader.load(inputStream) >> doc

        expect:
        Xml.load(inputStream) == doc
    }

    def "load(Reader) should delegate to loader.load(Reader)"() {
        given:
        def reader = new StringReader("<xml/>")
        Xml.initialize(loader)
        loader.load(reader) >> doc

        expect:
        Xml.load(reader) == doc
    }

    /* ------------------------------------------------------------
       Loader and config access
       ------------------------------------------------------------ */

    def "loader(XmlConfig) should create new XmlLoader with the provided config"() {
        given:
        def config = Mock(XmlConfig)
        def defaultLoader = Xml.loader()
        def customLoader = Xml.loader(config)

        expect:
        defaultLoader != customLoader

        and:
        defaultLoader.config != config
        defaultLoader.config == Xml.getConfig()

        and:
        customLoader.config != Xml.getConfig()
        customLoader.config == config
    }

    def "getConfig() should return current loader config"() {
        given:
        def config = Mock(XmlConfig)
        Xml.initialize(loader)
        loader.getConfig() >> config

        expect:
        Xml.getConfig() == config
    }
}
