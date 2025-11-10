package com.example.utils.pool

import com.example.utils.pool.metrics.PoolMetricsCollector
import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.metrics.PoolMetrics
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter
import spock.lang.Specification

class PoolMetricsCollectorSpec extends Specification {
    static class DummyPool extends AbstractPoolAdapter<String> {
        DummyPool(String name, PoolProperties props) { super(name, props) }

        @Override
        protected String getObject() { return "x" }

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
        void shutdown() {}
    }

    def "snapshot returns metrics map keyed by pool name"() {
        given:
        def manager = new PoolManager()
        def p = new DummyPool('p1', new PoolProperties())
        manager.register('p1', p)
        def collector = new PoolMetricsCollector(manager)

        when:
        def snap = collector.snapshot()

        then:
        snap.containsKey('p1')
        snap.get('p1') != null
    }
}
