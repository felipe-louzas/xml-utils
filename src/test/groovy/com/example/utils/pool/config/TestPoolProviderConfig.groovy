package com.example.utils.pool.config

import com.example.utils.pool.Pool
import com.example.utils.pool.PoolObjectFactory
import com.example.utils.pool.beans.PoolProperties
import com.example.utils.pool.metrics.PoolMetrics
import com.example.utils.pool.providers.PoolProvider
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter
import com.example.utils.pool.providers.adapters.PoolObjectFactoryAdapter
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Fallback

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@TestConfiguration
class TestPoolProviderConfig {

    @Bean
    @Fallback
    PoolProvider poolProvider() {
        return new TestProvider()
    }

    static class TestProvider implements PoolProvider {
        int poolsCreated = 0

        @Override
        <T> Pool<T> createPool(String poolName, PoolProperties props, PoolObjectFactory<T> factory) {
            poolsCreated++
            return new TestPool<>(poolName, props, factory)
        }
    }

    static class TestPool<T> extends AbstractPoolAdapter<T> implements Pool<T> {
        final PoolObjectFactoryAdapter<T> factory
        final BlockingQueue<T> idle
        final AtomicInteger waiters = new AtomicInteger(0)

        TestPool(String poolName, PoolProperties props, PoolObjectFactory<T> factory) {
            super(poolName, props)
            this.factory = new PoolObjectFactoryAdapter<>(factory)
            this.idle = new LinkedBlockingQueue<>(props.getMaxTotal())
        }

        protected T getObject() throws Exception {
            T obj = idle.poll()
            if (obj == null && factory.getCreatedCount() < props.getMaxTotal()) {
                obj = factory.create()
            } else if (obj == null) {
                waiters.incrementAndGet()
                try {
                    obj = idle.poll(props.maxWaitMillis, TimeUnit.MILLISECONDS)
                    if (obj == null) {
                        throw new Exception("Pool ${getName()} exhausted after ${props.maxWaitMillis}ms")
                    }
                } finally {
                    waiters.decrementAndGet()
                }
            }
            factory.validate(obj)
            return obj;
        }

        protected void returnObject(T object) throws Exception {
            try {
                factory.reset(object)
                if (props.getTestOnReturn() && !factory.validate(object)) {
                    factory.destroy(object)
                    return
                }
                if (closed) {
                    factory.destroy(object)
                    return
                }
                idle.offer(object)
            } catch (Exception ex) {
                factory.destroy(object)
                throw ex
            }
        }

        @Override
        PoolMetrics getMetrics() {
            PoolMetrics.builder()
                    .numActive(getBorrowedCount() - getReturnedCount())
                    .numIdle(idle.size())
                    .numWaiters(waiters.get())
                    .borrowedCount(getBorrowedCount())
                    .returnedCount(getReturnedCount())
                    .createdCount(factory.getCreatedCount())
                    .destroyedCount(factory.getDestroyedCount())
                    .properties(props)
                    .build()
        }

        @Override
        void shutdown() {
            idle.each { factory.destroy(it) }
            idle.clear()
        }
    }
}
