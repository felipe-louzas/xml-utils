package com.example.utils.pool

import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.exceptions.PoolException
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.metrics.PoolMetrics
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter
import spock.lang.Specification

class PoolManagerSpec extends Specification {

    static class DummyPool extends AbstractPoolAdapter<String> {
        DummyPool(String name, PoolProperties props) { super(name, props) }

        @Override
        protected String getObject() {
            return "x"
        }

        @Override
        protected void returnObject(String object) {}

        @Override
        PoolMetrics getMetrics() {
            return PoolMetrics.builder()
                    .numActive(0)
                    .numIdle(0)
                    .numWaiters(0)
                    .borrowedCount(getBorrowedCount())
                    .returnedCount(getReturnedCount())
                    .createdCount(0)
                    .destroyedCount(0)
                    .properties(getProps())
                    .build()
        }

        @Override
        void shutdown() {
            // no resources to close in test stub
        }
    }

    def "register, get and closeAll behavior"() {
        given:
        def manager = new PoolManager()
        def p = new DummyPool('p1', new PoolProperties())

        expect:
        manager.size() == 0
        !manager.hasPool('p1')

        when:
        manager.register('p1', p)

        then:
        manager.size() == 1
        manager.hasPool('p1')
        manager.get('p1').getName() == 'p1'

        when:
        manager.close()

        then:
        noExceptionThrown()

        when: 'duplicate registration should fail'
        manager.register('p1', p)

        then:
        thrown(PoolException)
    }
}
