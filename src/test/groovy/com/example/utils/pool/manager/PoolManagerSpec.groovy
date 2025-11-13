package com.example.utils.pool.manager

import com.example.utils.pool.TestObjectFactoryConfig
import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.exceptions.PoolException
import com.example.utils.pool.providers.commons.GenericObjectPoolAdapter
import spock.lang.Specification

class PoolManagerSpec extends Specification {

    def "can register and close pools"() {
        given:
        def manager = new PoolManager()
        def pool = new GenericObjectPoolAdapter('p1', PoolProperties.getDefaults(), new TestObjectFactoryConfig.TestFactory());

        expect:
        manager.size() == 0
        !manager.hasPool('p1')

        when:
        manager.register('p1', pool)

        then:
        manager.size() == 1
        manager.hasPool('p1')
        manager.get('p1').getName() == 'p1'
        !pool.closed

        when:
        manager.close()

        then:
        noExceptionThrown()
        pool.closed
    }

    def "duplicate registration throws exception"() {
        given:
        def manager = new PoolManager()
        def pool = new GenericObjectPoolAdapter('p1', PoolProperties.getDefaults(), new TestObjectFactoryConfig.TestFactory());

        expect:
        manager.size() == 0
        !manager.hasPool('p1')

        when:
        manager.register('p1', pool)

        then:
        manager.size() == 1
        manager.hasPool('p1')

        when: 'duplicate registration should fail'
        manager.register('p1', pool)

        then:
        thrown(PoolException)
    }
}

