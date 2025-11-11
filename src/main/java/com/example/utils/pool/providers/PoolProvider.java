package com.example.utils.pool.providers;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.beans.PoolProperties;

@FunctionalInterface
public interface PoolProvider {
	String PROP_PROVIDER_NAME_PREFIX = "pools.provider.";

	<T> Pool<T> createPool(String poolName, PoolProperties props, PoolObjectFactory<T> factory);
}
