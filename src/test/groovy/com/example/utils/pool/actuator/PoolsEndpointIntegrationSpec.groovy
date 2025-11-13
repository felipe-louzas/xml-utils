package com.example.utils.pool.actuator

import com.example.utils.pool.Pool
import com.example.utils.pool.TestObjectFactoryConfig
import com.example.utils.pool.config.PoolActuatorConfig
import com.example.utils.pool.config.PoolManagerConfig
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.providers.commons.Commons2Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

@SpringBootTest(classes = [TestObjectFactoryConfig, Commons2Config, PoolManagerConfig, PoolActuatorConfig])
class PoolsEndpointIntegrationSpec extends Specification {

    @Autowired
    @Qualifier('firstPool')
    Pool<TestObjectFactoryConfig.TestPoolObject> firstPool;

    @Autowired
    @Qualifier('secondPool')
    Pool<TestObjectFactoryConfig.TestPoolObject> secondPool;

    @Autowired
    PoolsEndpoint endpoint

    @Autowired
    PoolManager manager;

    @DirtiesContext
    def "PoolsEndpoint.pools() retorna snapshot do PoolMetricsCollector"() {
        when:
        firstPool.borrow()
        firstPool.borrow()
        def lease = firstPool.borrow()
        lease.close()
        secondPool.borrow()
        def result = endpoint.pools()

        then:
        manager.size() == 4
        result instanceof Map
        result.containsKey('first-pool')
        result.containsKey('second-pool')

        when:
        def metrics1 = result.get('first-pool')

        then:
        metrics1.numActive == 2
        metrics1.numIdle == 1
        metrics1.createdCount == 3
        metrics1.borrowedCount == 3
        metrics1.returnedCount == 1

        when:
        def metrics2 = result.get('second-pool')

        then:
        metrics2.numActive == 1
        metrics2.numIdle == 0
        metrics2.createdCount == 1
        metrics2.borrowedCount == 1
        metrics2.returnedCount == 0
    }
}

