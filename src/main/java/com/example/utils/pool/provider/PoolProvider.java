package com.example.utils.pool.provider;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.config.PoolProperties;

@FunctionalInterface
public interface PoolProvider {
	String PROP_PROVIDER_NAME = "pools.provider";

	<T> Pool<T> createPool(String poolName, PoolProperties props, PoolObjectFactory<T> factory);
}
