package com.example.utils.pool.config;

import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.beans.PoolBeanDefinitionRegistryPostProcessor;
import com.example.utils.pool.beans.PoolConfigMap;
import com.example.utils.pool.manager.PoolManager;
import com.example.utils.pool.providers.PoolProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class PoolManagerConfig {

	@Bean
	@ConditionalOnBean({PoolProvider.class, PoolObjectFactory.class})
	public static PoolBeanDefinitionRegistryPostProcessor poolBeanDefinitionRegistryPostProcessor() {
		return new PoolBeanDefinitionRegistryPostProcessor();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "pools")
	public PoolConfigMap poolConfigMap() {
		return new PoolConfigMap();
	}

	/**
	 * Provides the default {@link PoolManager} bean if none exists.
	 */
	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public PoolManager poolManager() {
		return new PoolManager();
	}
}
