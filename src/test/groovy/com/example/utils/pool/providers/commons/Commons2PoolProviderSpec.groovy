package com.example.utils.pool.providers.commons

import com.example.utils.pool.Pool
import com.example.utils.pool.config.PoolManagerConfig
import com.example.utils.pool.config.TestObjectFactoryConfig
import com.example.utils.pool.config.TestPoolProviderConfig
import com.example.utils.pool.manager.AbstractPoolManagerSpec
import com.example.utils.pool.manager.PoolManager
import com.example.utils.pool.providers.PoolProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [TestPoolProviderConfig, TestObjectFactoryConfig, Commons2Config, PoolManagerConfig], properties = [
        'pools.config.default.provider=commons-pool2'
])
class Commons2PoolProviderSpec extends AbstractPoolManagerSpec {

    @Autowired
    PoolManager manager

    @Autowired
    PoolProvider provider

    @Autowired
    @Qualifier('secondPool')
    Pool<TestObjectFactoryConfig.TestPoolObject> secondPool

    PoolManager getManager() { return manager }

    PoolProvider getProvider() { return provider }

    Pool<TestObjectFactoryConfig.TestPoolObject> getSecondPool() { return secondPool }

    def "Injected pool type is GenericObjectPoolAdapter"() {
        expect:
        (secondPool instanceof GenericObjectPoolAdapter) == true
    }
}
