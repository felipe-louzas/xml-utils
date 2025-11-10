package com.example.utils.pool

import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.metrics.PoolMetrics
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class PoolManagerConcurrencySpec extends Specification {
    static class DummyPool extends AbstractPoolAdapter<String> {
        DummyPool(String name, PoolProperties props) { super(name, props) }

        @Override
        protected String getObject() { 'x' }

        @Override
        protected void returnObject(String object) {}

        @Override
        PoolMetrics getMetrics() { null }

        @Override
        void shutdown() {}
    }

    def "only one thread can register a pool with the same name under contention"() {
        given:
        def manager = new PoolManager()
        int threads = 50
        def startLatch = new CountDownLatch(1)
        def doneLatch = new CountDownLatch(threads)
        def successes = new AtomicInteger(0)
        def failures = new AtomicInteger(0)
        def poolSupplier = { -> new DummyPool('p', new PoolProperties()) }
        def executor = Executors.newFixedThreadPool(threads)

        when:
        (1..threads).each {
            executor.submit({ ->
                try {
                    startLatch.await()
                    manager.register('same', poolSupplier())
                    successes.incrementAndGet()
                } catch (Exception ex) {
                    failures.incrementAndGet()
                } finally {
                    doneLatch.countDown()
                }
            } as Runnable)
        }

        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()

        then:
        successes.get() == 1
        failures.get() == threads - 1
    }
}

