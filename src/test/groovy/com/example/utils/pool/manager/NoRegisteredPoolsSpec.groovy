package com.example.utils.pool.manager


import com.example.utils.pool.config.PoolManagerConfig
import com.example.utils.pool.config.TestPoolProviderConfig
import com.example.utils.pool.providers.PoolProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = [TestPoolProviderConfig, PoolManagerConfig])
class NoRegisteredPoolsSpec extends Specification {

    @Autowired
    PoolManager manager

    @Autowired
    PoolProvider provider

    def "When no pool factories are registered, no errors are thrown"() {
        expect:
        manager != null
        provider != null

        and:
        manager.all().isEmpty()
    }
}

