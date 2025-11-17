package com.example.utils.pool.providers.commons;

import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.providers.PoolProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Fallback;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass(PoolObjectFactory.class)
public class Commons2Config {
	private static final String PROVIDER_GENERIC_OBJECT_POOL = "commons-pool2";
	private static final String PROVIDER_SOFT_REF_OBJECT_POOL = "commons-pool2-soft";

	@Lazy
	@Primary
	@Bean(PoolProvider.PROP_PROVIDER_NAME_PREFIX + PROVIDER_GENERIC_OBJECT_POOL)
	public PoolProvider genericObjectPoolProvider() {
		return GenericObjectPoolAdapter::new;
	}

	@Lazy
	@Fallback
	@Bean(PoolProvider.PROP_PROVIDER_NAME_PREFIX + PROVIDER_SOFT_REF_OBJECT_POOL)
	public PoolProvider softReferenceObjectPoolProvider() {
		return SoftReferencePoolAdapter::new;
	}
}
