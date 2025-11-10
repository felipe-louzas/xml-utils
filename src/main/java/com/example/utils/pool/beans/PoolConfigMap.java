package com.example.utils.pool.beans;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.val;

/**
 * Main configuration properties class that holds all pool configurations. Binds to `pools.config.*` in application.yaml/properties.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolConfigMap {
	private static final String DEFAULT_POOL_NAME = "default";

	/**
	 * A map of pool names to their specific configurations. e.g., pools.config.my-string-pool.max-total=10
	 */
	Map<String, PoolProperties> config = new HashMap<>();
	PoolProperties defaults;

	public PoolProperties getConfig(String name) {
		return getConfig(name, getDefaultConfig());
	}

	private PoolProperties getDefaultConfig() {
		if (defaults == null) {
			defaults = getConfig(DEFAULT_POOL_NAME, PoolProperties.getDefaults());
		}
		return defaults;
	}

	private PoolProperties getConfig(String name, PoolProperties defaults) {
		val poolConfig = config.get(name);
		if (poolConfig == null) {
			config.put(name, defaults);
			return defaults;
		}
		poolConfig.applyDefaults(defaults);
		return poolConfig;
	}
}