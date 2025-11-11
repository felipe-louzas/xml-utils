package com.example.utils.pool.beans;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Configuration for a single object pool. Maps to properties under `pools.config.<name>.*`
 * <p>
 * These properties are based on Apache Commons Pool's GenericObjectPoolConfig.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolProperties {
	/**
	 * The provider to use for this pool.
	 */
	String provider;

	/**
	 * The maximum number of active objects that can be allocated from this pool at the same time.
	 */
	Integer maxTotal;

	/**
	 * The maximum number of idle objects that can sit in the pool.
	 */
	Integer maxIdle;

	/**
	 * The minimum number of idle objects to maintain in the pool.
	 */
	Integer minIdle;

	/**
	 * Whether to validate objects before borrowing them from the pool.
	 */
	Boolean testOnBorrow;

	/**
	 * Whether to validate objects before returning them to the pool.
	 */
	Boolean testOnReturn;

	/**
	 * The maximum amount of time (in milliseconds) the `borrowObject` method should block before throwing an exception.
	 */
	Long maxWaitMillis; // -1 means block indefinitely

	/**
	 * Whether objects should be validated by the idle object evictor (if one is running).
	 */
	Boolean testWhileIdle;

	/**
	 * The time (in milliseconds) to sleep between runs of the idle object evictor thread.
	 */
	Long timeBetweenEvictionRunsMillis; // -1 means no eviction thread

	public static PoolProperties getDefaults() {
		return new PoolProperties(
			null,
			Runtime.getRuntime().availableProcessors() * 4,
			Runtime.getRuntime().availableProcessors(),
			0,
			true,
			false,
			5000L,
			false,
			-1L
		);
	}

	public void applyDefaults(PoolProperties defaults) {
		maxTotal = ObjectUtils.getIfNull(maxTotal, defaults.maxTotal);
		maxIdle = ObjectUtils.getIfNull(maxIdle, defaults.maxIdle);
		minIdle = ObjectUtils.getIfNull(minIdle, defaults.minIdle);
		testOnBorrow = ObjectUtils.getIfNull(testOnBorrow, defaults.testOnBorrow);
		testOnReturn = ObjectUtils.getIfNull(testOnReturn, defaults.testOnReturn);
		maxWaitMillis = ObjectUtils.getIfNull(maxWaitMillis, defaults.maxWaitMillis);
		testWhileIdle = ObjectUtils.getIfNull(testWhileIdle, defaults.testWhileIdle);
		timeBetweenEvictionRunsMillis = ObjectUtils.getIfNull(timeBetweenEvictionRunsMillis, defaults.timeBetweenEvictionRunsMillis);
	}
}