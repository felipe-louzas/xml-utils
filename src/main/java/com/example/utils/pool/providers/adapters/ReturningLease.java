package com.example.utils.pool.providers.adapters;

import com.example.utils.pool.Pool;
import com.example.utils.pool.exceptions.PoolException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableSupplier;

/**
 * A lease that returns the leased object to the pool upon closing.
 *
 * @param <T> the type of the leased object
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReturningLease<T> implements Pool.Lease<T> {
	final FailableConsumer<T, ? extends Exception> returner;
	T object;

	public static <T, E extends Exception> Pool.Lease<T> of(T object, FailableConsumer<T, E> returner) {
		return new ReturningLease<>(returner, object);
	}

	public static <T, E1 extends Exception, E2 extends Exception> Pool.Lease<T> borrowing(
		FailableSupplier<T, E1> borrower,
		FailableConsumer<T, E2> returner
	) {
		try {
			return new ReturningLease<>(returner, borrower.get());

		} catch (PoolException ex) {
			throw ex;

		} catch (Exception ex) {
			throw new PoolException("Failed to borrow object from pool", ex);
		}
	}

	@Override
	public T get() {
		return object;
	}

	@Override
	public void close() {
		val ref = object;
		object = null;
		try {
			returner.accept(ref);
		} catch (Exception ex) {
			log.warn("Failed to return leased object to pool", ex);
		}
	}
}