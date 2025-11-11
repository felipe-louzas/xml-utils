package com.example.utils.pool.beans;

import com.example.utils.core.Words;
import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.manager.PoolManager;
import com.example.utils.pool.providers.PoolProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolFactoryBean<T> implements FactoryBean<Pool<T>>, ApplicationContextAware {

	final String poolBeanName;
	final PoolObjectFactory<T> factory;

	ApplicationContext ctx;

	@Override
	public void setApplicationContext(@NonNull ApplicationContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public Pool<T> getObject() {
		val poolName = Words.toKebabCase(poolBeanName);

		val pool = createPool(poolName);
		if (pool == null) return null;

		try {
			val manager = ctx.getBean(PoolManager.class);
			manager.register(poolName, pool);
			return pool;
		} catch (Exception ex) {
			log.error("Houve um erro ao registrar o pool '{}': {}", poolBeanName, ex.getLocalizedMessage());
			pool.close();
			return null;
		}
	}

	private Pool<T> createPool(String poolName) {
		try {
			log.debug("Criando pool '{}' com object factory do tipo '{}'", poolBeanName, factory.getClass().getSimpleName());

			val configMap = ctx.getBean(PoolConfigMap.class);
			val config = configMap.getConfig(poolName);
			val provider = lookupProvider(config.getProvider());

			return provider.createPool(poolName, config, factory);
		} catch (Exception ex) {
			log.error("Houve um erro ao criar o pool '{}'", poolBeanName, ex);
			return null;
		}
	}

	private PoolProvider lookupProvider(@Nullable String providerBeanName) {
		if (providerBeanName != null)
			return ctx.getBean(PoolProvider.PROP_PROVIDER_NAME_PREFIX + providerBeanName, PoolProvider.class);
		return ctx.getBeanProvider(PoolProvider.class).stream()
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Nenhum PoolProvider registrado no contexto"));
	}

	@Override
	public Class<?> getObjectType() {
		return Pool.class;
	}
}
