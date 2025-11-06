package com.example.utils.pool.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Configuration for a single object pool. Maps to properties under `pools.config.<name>.*`
 * <p>
 * These properties are based on Apache Commons Pool's GenericObjectPoolConfig.
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolProperties {
	/**
	 * The maximum number of active objects that can be allocated from this pool at the same time.
	 */
	int maxTotal = 8;

	/**
	 * The maximum number of idle objects that can sit in the pool.
	 */
	int maxIdle = 8;

	/**
	 * The minimum number of idle objects to maintain in the pool.
	 */
	int minIdle = 0;

	/**
	 * Whether to validate objects before borrowing them from the pool.
	 */
	boolean testOnBorrow = true;

	/**
	 * Whether to validate objects before returning them to the pool.
	 */
	boolean testOnReturn = false;

	/**
	 * The maximum amount of time (in milliseconds) the `borrowObject` method should block before throwing an exception.
	 */
	long maxWaitMillis = -1L; // -1 means block indefinitely

	/**
	 * Whether objects should be validated by the idle object evictor (if one is running).
	 */
	boolean testWhileIdle = false;

	/**
	 * The time (in milliseconds) to sleep between runs of the idle object evictor thread.
	 */
	long timeBetweenEvictionRunsMillis = -1L; // -1 means no eviction thread
}