package com.example.utils.pool.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration properties class that holds all pool configurations. Binds to `pool.*` in application.yaml/properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pools")
public class PoolsConfig {
	/**
	 * A map of pool names to their specific configurations. e.g., pools.myStringPool.maxTotal=10
	 */
	Map<String, PoolProperties> config = new HashMap<>();

	public PoolProperties getConfig(String name) {
		return config.computeIfAbsent(name, n -> new PoolProperties());
	}
}