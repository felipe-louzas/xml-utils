package com.example.utils.pool.providers.adapters;

import com.example.utils.pool.Pool;
import com.example.utils.pool.beans.PoolProperties;
import com.example.utils.pool.exceptions.PoolException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractPoolAdapter<T> implements Pool<T> {
	String name;
	PoolProperties props;

	@NonFinal
	volatile boolean closed = false;

	@NonFinal
	int borrowedCount = 0;

	@NonFinal
	int returnedCount = 0;

	protected abstract T getObject() throws Exception;

	protected abstract void returnObject(T object) throws Exception;

	protected abstract void shutdown() throws Exception;

	@Override
	public final Pool.Lease<T> borrow() throws PoolException {
		if (closed) throw new PoolException("Pool " + name + " is closed");
		return ReturningLease.borrowing(this::trackGet, this::trackReturn);
	}

	@Override
	public final void close() {
		try {
			closed = true;
			shutdown();
		} catch (Exception ex) {
			log.warn("Failed to close pool {}", name, ex);
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	// Exceções são tratadas pelo ReturningLease
	private T trackGet() throws Exception {
		T obj = getObject();
		borrowedCount++;
		return obj;
	}

	// Exceções são tratadas pelo ReturningLease
	private void trackReturn(T object) throws Exception {
		returnedCount++;
		returnObject(object);
	}

}
