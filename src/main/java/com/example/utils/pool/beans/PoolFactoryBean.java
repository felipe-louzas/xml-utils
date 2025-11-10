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

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolFactoryBean<T> implements FactoryBean<Pool<T>>, ApplicationContextAware {

	final String poolBeanName;
	final PoolObjectFactory<T> factory;

	PoolProvider provider;
	PoolManager manager;
	PoolConfigMap configMap;

	@Override
	public void setApplicationContext(@NonNull ApplicationContext ctx) {
		this.provider = ctx.getBean(PoolProvider.class);
		this.manager = ctx.getBean(PoolManager.class);
		this.configMap = ctx.getBean(PoolConfigMap.class);
	}

	@Override
	public Pool<T> getObject() {
		val poolName = Words.toKebabCase(poolBeanName);

		val pool = createPool(poolName);
		if (pool == null) return null;

		try {
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
			var props = configMap.getConfig(poolName);
			return provider.createPool(poolName, props, factory);
		} catch (Exception ex) {
			log.error("Houve um erro ao criar o pool '{}'", poolBeanName, ex);
			return null;
		}
	}

	@Override
	public Class<?> getObjectType() {
		return Pool.class;
	}
}
