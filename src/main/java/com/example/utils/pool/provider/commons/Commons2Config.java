package com.example.utils.pool.provider.commons;

import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.provider.PoolProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(PoolObjectFactory.class)
public class Commons2Config {
	private static final String PROVIDER_GENERIC_OBJECT_POOL = "commons-pool2";
	private static final String PROVIDER_SOFT_REF_OBJECT_POOL = "commons-pool2-soft";

	@Bean
	@ConditionalOnProperty(name = PoolProvider.PROP_PROVIDER_NAME, havingValue = PROVIDER_GENERIC_OBJECT_POOL)
	public PoolProvider genericObjectPoolProvider() {
		return GenericPoolAdapter::new;
	}

	@Bean
	@ConditionalOnProperty(name = PoolProvider.PROP_PROVIDER_NAME, havingValue = PROVIDER_SOFT_REF_OBJECT_POOL)
	public PoolProvider softReferenceObjectPoolProvider() {
		return SoftReferencePoolAdapter::new;
	}
}
