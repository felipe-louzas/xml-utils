package com.example.utils.pool.actuator;

import java.util.Map;

import com.example.utils.pool.metrics.PoolMetrics;
import com.example.utils.pool.metrics.PoolMetricsCollector;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/** Custom actuator endpoint exposing pool metrics */
@Endpoint(id = "pools")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PoolsEndpoint {
	PoolMetricsCollector collector;

	@ReadOperation
	public Map<String, PoolMetrics> pools() {
		return collector.snapshot();
	}
}
