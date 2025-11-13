package com.example.utils.pool.providers.commons

import com.example.utils.pool.Pool
import com.example.utils.pool.TestObjectFactoryConfig
import com.example.utils.pool.config.PoolManagerConfig
import com.example.utils.pool.exceptions.PoolException
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.providers.PoolProvider
import org.apache.commons.lang3.function.FailableConsumer
import org.apache.commons.lang3.function.FailableFunction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [TestObjectFactoryConfig, Commons2Config, PoolManagerConfig], properties = [
        'pools.config.default.provider=commons-pool2',
        'pools.config.first-pool.provider=commons-pool2-soft',
        'pools.config.second-pool.max-total=1',
        'pools.config.second-pool.max-wait-millis=300',
])
class Commons2PoolProviderSpec extends Specification {

    @Autowired
    PoolManager manager

    @Autowired
    PoolProvider provider

    @Autowired
    @Qualifier('secondPool')
    Pool<TestObjectFactoryConfig.TestPoolObject> secondPool

    def "Injected pool type is GenericObjectPoolAdapter"() {
        expect:
        (secondPool instanceof GenericObjectPoolAdapter) == true
    }

    def "Pools from discovered factories are created through provider, registered with the manager and available for autowiring"() {
        expect:
        manager != null
        provider != null

        and:
        manager.hasPool('first-pool')
        manager.hasPool('second-pool')

        when:
        def pool1 = manager.get('first-pool')

        then:
        pool1.name == 'first-pool'

        when:
        def pool2 = manager.get('second-pool')

        then:
        pool2.is(secondPool)
    }

    @DirtiesContext
    def "Pools return same instance when idle"() {
        given:
        def pool = manager.get('first-pool')

        when:
        def lease1 = pool.borrow()
        def obj1 = lease1.get()
        lease1.close()

        def lease2 = pool.borrow()
        def obj2 = lease2.get()
        lease2.close()

        then:
        obj1.is(obj2)

        and:
        pool.metrics.createdCount == 1
    }

    @DirtiesContext
    def "Lease returns null after closed"() {
        when:
        def lease1 = secondPool.borrow()
        lease1.close()

        then:
        lease1.get() == null
    }

    @DirtiesContext
    def "Borrow and return should update metrics correctly"() {
        given:
        def pool = manager.get('first-pool')

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

    @DirtiesContext
    def "Pool should respect maxSize and track numWaiters when exhausted"() {
        given:
        def pool = secondPool

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

    @DirtiesContext
    def "Pool should throw PoolExhaustedException after maxWaitMillis"() {
        given:
        def pool = secondPool
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
        def pool = manager.get('first-pool')

        when:
        def lease = pool.borrow()
        lease.close()

        then:
        lease.get() == null
        !pool.closed
        pool.metrics.numIdle == 1

        when:
        pool.close()

        then:
        pool.closed
        pool.metrics.numIdle == 0

        when:
        pool.borrow()

        then:
        thrown(PoolException)
    }

    @DirtiesContext
    def "Pool metrics should reflect created and destroyed counts correctly"() {
        given:
        def pool = manager.get('first-pool')

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
            numIdle == 0
            numActive == 0
        }
    }

    @DirtiesContext
    def "Manager can closes all registered pools on shutdown"() {
        given:
        def p1 = manager.get('first-pool') as Pool<TestObjectFactoryConfig.TestPoolObject>
        def p2 = secondPool
        def p3 = manager.get('bad-reset')
        def p4 = manager.get('bad-destroy')

        and: "Use pools to have some activity"
        def consumer = {} as FailableConsumer<TestObjectFactoryConfig.TestPoolObject, ? extends Exception>
        def func = { it.hashCode() } as FailableFunction<TestObjectFactoryConfig.TestPoolObject, ?, ? extends Exception>
        p1.use(consumer)
        p2.use(consumer)
        def hash1 = p3.use(func)
        def hash2 = p4.use(func)

        expect:
        p1 != null
        p2 != null
        hash1 != null
        hash2 != null

        when:
        manager.close()

        then:
        p1.closed
        p1.metrics.numIdle == 0
        p1.metrics.numActive == 0
        p1.metrics.destroyedCount == 1

        p2.closed
        p2.metrics.numIdle == 0
        p2.metrics.numActive == 0
        p2.metrics.destroyedCount == 1

        p3.closed
        p3.metrics.numIdle == 0
        p3.metrics.numActive == 0
        p3.metrics.destroyedCount == 1

        p4.closed
        p4.metrics.numIdle == 0
        p4.metrics.numActive == 0
        p4.metrics.destroyedCount == 1

        and:
        manager.all().isEmpty()
    }

    @DirtiesContext
    def "Pool should destroy object if reset() throws"() {
        given:
        def pool = manager.get('bad-reset')

        when:
        def lease = pool.borrow()
        lease.close()

        then:
        pool.metrics.returnedCount == 1
        pool.metrics.destroyedCount == 1
    }
}
