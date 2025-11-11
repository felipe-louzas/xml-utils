package com.example.utils.pool.config

import com.example.utils.pool.beans.PoolConfigMap
import com.example.utils.pool.beans.PoolProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@EnableConfigurationProperties
@SpringBootTest(classes = [PoolManagerConfig], properties = [
        'pools.config.default.min-idle=2',
        'pools.config.test-pool.max-total=7',
        'pools.config.test-pool.max-idle=3',
        'pools.config.test-pool.max-wait-millis=1234'
])
class PoolsConfigBindingSpec extends Specification {

    @Autowired
    PoolConfigMap poolConfigMap

    def "PoolsConfig binds properties from environment"() {
        when:
        def poolProps = poolConfigMap.getConfig('test-pool')
        def defaults = PoolProperties.getDefaults()

        then:
        poolProps != null
        poolProps.maxTotal == 7
        poolProps.maxIdle == 3
        poolProps.minIdle == 2
        poolProps.maxWaitMillis == 1234L
        poolProps.testOnBorrow == defaults.getTestOnBorrow()
        poolProps.testOnReturn == defaults.getTestOnReturn()
    }

    def "PoolsConfig returns default PoolProperties when missing"() {
        when:
        def poolProps = poolConfigMap.getConfig('nonexistent')
        def defaults = PoolProperties.getDefaults()

        then:
        poolProps != null
        poolProps.maxTotal == defaults.getMaxTotal()
        poolProps.maxIdle == defaults.getMaxIdle()
        poolProps.minIdle == 2
        poolProps.maxWaitMillis == defaults.getMaxWaitMillis()
        poolProps.testOnBorrow == defaults.getTestOnBorrow()
        poolProps.testOnReturn == defaults.getTestOnReturn()

    }
}

