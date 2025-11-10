package com.example.utils.pool.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.utils.pool.Pool;
import com.example.utils.pool.exceptions.PoolException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Central registry and lifecycle manager for all application pools.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PoolManager implements AutoCloseable {
	Map<String, Pool<?>> pools = new ConcurrentHashMap<>();

	public <T> void register(String name, Pool<T> pool) {
		if (pools.putIfAbsent(name, pool) != null) {
			throw new PoolException("Pool with name '" + name + "' already registered");
		}
		log.debug("Registered pool '{}' ({}) with PoolManager", name, pool.getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	public <T> Pool<T> get(String name) {
		return (Pool<T>) pools.get(name);
	}

	public Collection<Pool<?>> all() {
		return Collections.unmodifiableCollection(pools.values());
	}

	@Override
	public void close() {
		pools.forEach((name, pool) -> pool.close());
		pools.clear();
	}

	public boolean hasPool(String poolName) {
		return pools.containsKey(poolName);
	}

	public int size() { return pools.size(); }
}

