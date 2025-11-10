package com.example.utils.pool.providers.commons

import com.example.utils.pool.PoolObjectFactory
import com.example.utils.pool.exceptions.PoolException
import spock.lang.Specification
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject

class Commons2FactoryAdapterExceptionSpec extends Specification {
    static class BadFactory implements PoolObjectFactory<String> {
        @Override String create() throws Exception { throw new RuntimeException('boom') }
        @Override void destroy(String object) throws Exception {}
        @Override void reset(String object) throws Exception {}
        @Override boolean validate(String object) { true }
    }

    def "makeObject wraps delegate exceptions into PoolException"() {
        given:
        def bad = new BadFactory()
        def adapter = new Commons2FactoryAdapter<String>(bad)

        when:
        adapter.makeObject()

        then:
        thrown(PoolException)
    }

    def "validateObject returns true for valid delegate and false when delegate throws"() {
        given:
        def good = new BadFactory() {
            @Override String create() { 'ok' }
            @Override boolean validate(String object) { throw new RuntimeException('vfail') }
        }
        def adapter = new Commons2FactoryAdapter<String>(good)
        def pooled = new DefaultPooledObject<String>('ok')

        when:
        adapter.validateObject(pooled)

        then:
        thrown(PoolException)
    }
}