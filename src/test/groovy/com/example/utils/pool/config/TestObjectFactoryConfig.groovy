package com.example.utils.pool.config

import com.example.utils.pool.PoolObjectFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestObjectFactoryConfig {
    @Bean
    PoolObjectFactory firstPoolFactory() {
        return new TestFactory(false, false)
    }

    @Bean
    PoolObjectFactory secondPoolFactory() {
        return new SimpleTestFactory()
    }

    @Bean
    // Deve causar conflito de nome na criação dos pools e emitir warning nos testes
    PoolObjectFactory secondPOOLFactory() {
        return new TestFactory(false, false)
    }

    @Bean
    PoolObjectFactory badResetFactory() {
        return new TestFactory(true, false)
    }

    @Bean
    PoolObjectFactory badDestroyFactory() {
        return new TestFactory(false, true)
    }


    static class SimpleTestFactory implements PoolObjectFactory<String> {
        @Override
        String create() throws Exception { "pooled-string" }
    }

    static class TestFactory implements PoolObjectFactory<TestPoolObject> {
        boolean throwOnReset = false
        boolean throwOnDestroy = false

        TestFactory() {
            this(false, false)
        }

        TestFactory(boolean throwOnReset, boolean throwOnDestroy) {
            this.throwOnReset = throwOnReset
            this.throwOnDestroy = throwOnDestroy
        }

        @Override
        TestPoolObject create() throws Exception { new TestPoolObject() }

        @Override
        void destroy(TestPoolObject object) throws Exception {
            if (throwOnDestroy) throw new Exception("Test error during destroy")
            object.destroy()
        }

        @Override
        void reset(TestPoolObject object) throws Exception {
            if (throwOnReset) throw new Exception("Test error during reset")
            object.reset()
        }

        @Override
        boolean validate(TestPoolObject object) { object.validate() }
    }

    static class TestPoolObject {
        boolean destroyed = false
        boolean active = false

        void use() { active = true }

        void reset() { active = false }

        void destroy() { destroyed = true }

        boolean validate() { !active && !destroyed }
    }
}
