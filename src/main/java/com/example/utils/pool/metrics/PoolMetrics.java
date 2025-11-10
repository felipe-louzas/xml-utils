package com.example.utils.pool.metrics;

import com.example.utils.pool.beans.PoolProperties;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PoolMetrics {
	/** Number of objects currently borrowed from the pool. */
	int numActive;

	/** Number of objects currently idle in the pool. */
	int numIdle;

	/** Number of threads blocked waiting for an object. */
	int numWaiters;

	/** TTotal number of objects borrowed. */
	long borrowedCount;

	/** Total number of objects returned. */
	long returnedCount;

	/** Total number of objects created. */
	long createdCount;

	/** Total number of objects destroyed. */
	long destroyedCount;

	/** The configuration properties of the pool. */
	PoolProperties properties;
}

