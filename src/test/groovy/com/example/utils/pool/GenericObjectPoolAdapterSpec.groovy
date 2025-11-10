package com.example.utils.pool

import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.providers.commons.GenericObjectPoolAdapter
import spock.lang.Specification

class GenericObjectPoolAdapterSpec extends Specification {

    static class TestFactory implements PoolObjectFactory<String> {
        int created = 0
        int destroyed = 0
        int resetCount = 0

        @Override
        String create() {
            created++
            return "obj-" + created
        }

        @Override
        void destroy(String object) {
            destroyed++
        }

        @Override
        void reset(String object) {
            resetCount++
        }

        @Override
        boolean validate(String object) {
            return true
        }
    }

    def "borrow and return from GenericObjectPoolAdapter updates metrics"() {
        given:
        def props = PoolProperties.getDefaults()
        props.maxTotal = 4
        def factory = new TestFactory()
        def pool = new GenericObjectPoolAdapter<String>("test", props, factory)

        when:
        def lease = pool.borrow()
        def value = lease.get()
        lease.close()

        then:
        value != null
        pool.getMetrics().getCreatedCount() >= 1
        pool.getMetrics().getDestroyedCount() == 0
        pool.getMetrics().getBorrowedCount() == 1
        pool.getMetrics().getReturnedCount() == 1
        pool.getMetrics().getNumActive() == 0

        cleanup:
        pool.close()
    }
}
