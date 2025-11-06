package com.example.utils.pool;

import com.example.utils.pool.exceptions.PoolException;
import com.example.utils.pool.metrics.PoolMetrics;
import lombok.val;
import org.apache.commons.lang3.function.FailableConsumer;

/**
 * Generic pool abstraction used by application code.
 *
 * @param <T> The type of object managed by the pool.
 */
public interface Pool<T> {
	/**
	 * Borrow a handle that wraps a pooled instance. The handle implements AutoCloseable so callers can use try-with-resources.
	 *
	 * @return A {@link Pool.Handle} wrapping the object.
	 * @throws PoolException if an object cannot be borrowed.
	 */
	Handle<T> borrow() throws PoolException;

	/**
	 * Convenience method to borrow an object, use it, and return it to the pool.
	 *
	 * @param consumer A consumer that uses the pooled object.
	 * @throws PoolException if an object cannot be borrowed.
	 */
	default <E extends Exception> void use(FailableConsumer<T, E> consumer) throws E, PoolException {
		try (val handle = borrow()) {
			consumer.accept(handle.get());
		}
	}

	/**
	 * Get a quick informational name (optional).
	 *
	 * @return The name of the pool.
	 */
	String getName();

	/**
	 * Get the current metrics for this pool.
	 *
	 * @return A {@link PoolMetrics} object containing the pool's metrics.
	 */
	PoolMetrics getMetrics();

	/**
	 * Closes the pool and releases all resources.
	 */
	void close();

	/**
	 * A handle for a borrowed object from a {@link Pool}. Implements {@link AutoCloseable} for use in try-with-resources statements.
	 *
	 * @param <T> The type of object being wrapped.
	 */
	interface Handle<T> extends AutoCloseable {
		/**
		 * Return the pooled object instance for use.
		 *
		 * @return The pooled object.
		 */
		T get();

		/**
		 * Return the object to the pool. This method is idempotent and safe to call multiple times. This is automatically called when used
		 * in a try-with-resources block.
		 */
		@Override
		void close();
	}
}