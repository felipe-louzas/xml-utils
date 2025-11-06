package com.example.utils.pool.config;

import java.util.Map;

import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.manager.PoolManager;
import com.example.utils.pool.provider.PoolProvider;
import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration that discovers all {@link PoolObjectFactory} beans, creates corresponding pools using the configured
 * {@link PoolProvider}, and registers them into the central {@link PoolManager}.
 *
 * <p>Each factory bean name is used to derive a pool name by removing the
 * "Factory" suffix. Corresponding pool configuration is loaded from {@link PoolsConfig}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnBean({PoolProvider.class, PoolObjectFactory.class})
public class PoolManagerConfig implements SmartInitializingSingleton, DisposableBean {

	private static final String FACTORY_SUFFIX = "Factory";

	PoolProvider provider;
	PoolsConfig config;
	Map<String, PoolObjectFactory<?>> factories;
	PoolManager poolManager;

	/**
	 * Provides the default {@link PoolManager} bean if none exists.
	 */
	@Bean
	@Primary
	@ConditionalOnMissingBean
	public PoolManager poolManager() {
		return new PoolManager();
	}

	/**
	 * Invoked by Spring once all singletons have been instantiated. Responsible for registering all discovered pools.
	 */
	@Override
	public void afterSingletonsInstantiated() {
		if (factories == null || factories.isEmpty()) {
			log.warn("No PooledObjectFactory beans found — no pools will be registered.");
			return;
		}

		factories.forEach((beanName, factory) -> {
			val poolName = derivePoolName(beanName);
			val props = config.getConfig(poolName);

			if (poolManager.hasPool(poolName)) {
				log.warn("Pool '{}' already registered — skipping duplicate factory '{}'.", poolName, beanName);
				return;
			}

			log.debug("Creating pool '{}' from factory '{}'", poolName, beanName);
			val pool = provider.createPool(poolName, props, factory);
			poolManager.register(poolName, pool);
		});

		log.info("PoolManager initialized with {} object pools...", poolManager.size());
	}

	@Override
	public void destroy() {
		log.info("Shutting down PoolManager — closing all pools...");
		poolManager.closeAll();
	}

	/**
	 * Derives the logical pool name from the factory bean name. Removes the standard suffix "Factory" if present.
	 */
	@Nonnull
	private String derivePoolName(@Nonnull String beanName) {
		return Strings.CS.removeEnd(beanName, FACTORY_SUFFIX);
	}
}
