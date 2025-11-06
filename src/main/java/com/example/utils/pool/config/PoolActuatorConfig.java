package com.example.utils.pool.config;

import com.example.utils.pool.actuator.PoolMetricsCollector;
import com.example.utils.pool.actuator.PoolsEndpoint;
import com.example.utils.pool.manager.PoolManager;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Endpoint.class)
public class PoolActuatorConfig {
	@Bean
	public PoolMetricsCollector poolMetricsCollector(PoolManager manager) {
		return new PoolMetricsCollector(manager);
	}

	@Bean
	public PoolsEndpoint poolsEndpoint(PoolMetricsCollector collector) {
		return new PoolsEndpoint(collector);
	}
}

