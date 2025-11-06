package com.example.utils.pool.actuator;

import java.util.Map;
import java.util.stream.Collectors;

import com.example.utils.pool.Pool;
import com.example.utils.pool.manager.PoolManager;
import com.example.utils.pool.metrics.PoolMetrics;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/** Collects metrics from all registered pools */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PoolMetricsCollector {
	PoolManager manager;

	public Map<String, PoolMetrics> snapshot() {
		return manager.all()
			.stream()
			.collect(Collectors.toMap(Pool::getName, Pool::getMetrics));
	}
}

