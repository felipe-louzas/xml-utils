package com.example.utils.pool.manager

import com.example.utils.pool.Pool
import com.example.utils.pool.config.PoolManagerConfig
import com.example.utils.pool.config.TestObjectFactoryConfig
import com.example.utils.pool.config.TestPoolProviderConfig
import com.example.utils.pool.exceptions.PoolException
import com.example.utils.pool.providers.PoolProvider
import org.apache.commons.lang3.function.FailableConsumer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [TestPoolProviderConfig, TestObjectFactoryConfig, PoolManagerConfig])
class PoolManagerSpec extends Specification {

    @Autowired
    PoolManager manager

    @Autowired
    PoolProvider provider

    @Autowired
    @Qualifier('secondPool')
    Pool<TestObjectFactoryConfig.TestPoolObject> secondPool

    def "Pools from discovered factories are created through provider, registered with the manager and available for autowiring"() {
        expect:
        manager != null
        provider != null

        and:
        (provider instanceof TestPoolProviderConfig.TestProvider)
        manager.hasPool('first-pool')
        manager.hasPool('second-pool')
        (provider as TestPoolProviderConfig.TestProvider).poolsCreated == manager.size() + 1 // +1 for secondPool bean that was created but not registered due to naming conflict

        when:
        def pool1 = manager.get('first-pool') as TestPoolProviderConfig.TestPool

        then:
        pool1.name == 'first-pool'

        when:
        def pool2 = manager.get('second-pool')

        then:
        pool2.is(secondPool)
    }

    def "Borrow and return should update metrics correctly"() {
        given:
        def pool = manager.get('first-pool') as TestPoolProviderConfig.TestPool

        when:
        def lease = pool.borrow()
        def metricsDuringBorrow = pool.metrics

        then:
        metricsDuringBorrow.numActive == 1
        metricsDuringBorrow.numIdle == 0
        metricsDuringBorrow.borrowedCount == 1
        metricsDuringBorrow.returnedCount == 0

        when:
        lease.close()
        def metricsAfterReturn = pool.metrics

        then:
        metricsAfterReturn.numActive == 0
        metricsAfterReturn.numIdle == 1
        metricsAfterReturn.returnedCount == 1
        !pool.closed
    }

    def "Pool should respect maxSize and track numWaiters when exhausted"() {
        given:
        def pool = manager.get('second-pool') as TestPoolProviderConfig.TestPool
        def props = pool.props
        props.maxTotal = 1
        props.maxWaitMillis = 300

        def firstLease = pool.borrow()
        def waiterStarted = new CountDownLatch(1)
        def waiterDone = new CountDownLatch(1)

        when: "A second borrow blocks because the pool is exhausted"
        Thread.start {
            waiterStarted.countDown()
            try {
                pool.borrow()
            } catch (ignored) {
            }
            waiterDone.countDown()
        }

        waiterStarted.await(100, TimeUnit.MILLISECONDS)
        Thread.sleep(50)
        def metricsWhileWaiting = pool.metrics

        then:
        metricsWhileWaiting.numWaiters == 1
        metricsWhileWaiting.numActive == 1

        when: "First lease is returned, second borrower proceeds"
        firstLease.close()
        waiterDone.await(400, TimeUnit.MILLISECONDS)
        def metricsAfterRelease = pool.metrics

        then:
        metricsAfterRelease.numWaiters == 0
        metricsAfterRelease.numActive == 1
        metricsAfterRelease.numIdle == 0
    }

    def "Pool should throw PoolExhaustedException after maxWaitMillis"() {
        given:
        def pool = manager.get('first-pool') as TestPoolProviderConfig.TestPool
        pool.props.maxTotal = 1
        pool.props.maxWaitMillis = 100

        def lease1 = pool.borrow()

        when:
        pool.borrow() // should time out

        then:
        thrown(PoolException)

        cleanup:
        lease1.close()
    }

    @DirtiesContext
    def "Closing a pool destroys idle objects and prevents further borrows"() {
        given:
        def pool = manager.get('first-pool') as TestPoolProviderConfig.TestPool
        def lease = pool.borrow()
        lease.close()
        def metricsBefore = pool.metrics
        metricsBefore.numIdle == 1

        when:
        pool.close()

        then:
        pool.closed
        pool.idle.isEmpty()

        when:
        pool.borrow()

        then:
        thrown(PoolException)
    }

    @DirtiesContext
    def "Pool metrics should reflect created and destroyed counts correctly"() {
        given:
        def pool = manager.get('first-pool') as TestPoolProviderConfig.TestPool
        pool.props.maxTotal = 2

        when:
        def l1 = pool.borrow()
        def l2 = pool.borrow()
        l1.close()
        l2.close()
        pool.close()

        then:
        with(pool.metrics) {
            createdCount == 2
            destroyedCount == 2
            borrowedCount == 2
            returnedCount == 2
            createdCount == pool.factory.createdCount
            destroyedCount == pool.factory.destroyedCount
            numIdle == 0
            numActive == 0
        }
    }

    @DirtiesContext
    def "Manager can close all registered pools"() {
        given:
        def p1 = manager.get('first-pool') as TestPoolProviderConfig.TestPool
        def p2 = secondPool as TestPoolProviderConfig.TestPool
        def p3 = manager.get('bad-reset') as TestPoolProviderConfig.TestPool
        def p4 = manager.get('bad-destroy') as TestPoolProviderConfig.TestPool

        def consumer = {} as FailableConsumer<?, ? extends Exception>
        p1.use(consumer)
        p2.use(consumer)
        p3.use(consumer)
        p4.use(consumer)

        expect:
        p1 != null
        p2 != null

        when:
        manager.close()

        then:
        p1.closed
        p2.closed
        p3.closed
        p4.closed

        and:
        manager.all().isEmpty()
    }

    def "Pool should destroy object if reset() throws"() {
        given:
        def pool = manager.get('bad-reset') as TestPoolProviderConfig.TestPool

        when:
        def lease = pool.borrow()
        lease.close()

        then:
        pool.metrics.returnedCount == 1
        pool.factory.destroyedCount == 1
    }
}

