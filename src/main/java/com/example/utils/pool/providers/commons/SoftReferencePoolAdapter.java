package com.example.utils.pool.providers.commons;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.beans.PoolProperties;
import com.example.utils.pool.metrics.PoolMetrics;
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SoftReferencePoolAdapter<T> extends AbstractPoolAdapter<T> implements Pool<T> {

	SoftReferenceObjectPool<T> delegate;
	Commons2FactoryAdapter<T> factoryAdapter;

	public SoftReferencePoolAdapter(String name, PoolProperties props, PoolObjectFactory<T> factory) {
		super(name, props);
		this.factoryAdapter = new Commons2FactoryAdapter<>(factory);
		this.delegate = new SoftReferenceObjectPool<>(factoryAdapter);
	}

	@Override
	protected T getObject() throws Exception {
		return delegate.borrowObject();
	}

	@Override
	protected void returnObject(T object) throws Exception {
		delegate.returnObject(object);
	}

	@Override
	public PoolMetrics getMetrics() {
		return PoolMetrics.builder()
			.numActive(delegate.getNumActive())
			.numIdle(delegate.getNumIdle())
			.numWaiters(0) // Implementation does not support waiting
			.borrowedCount(getBorrowedCount())
			.returnedCount(getReturnedCount())
			.createdCount(factoryAdapter.getCreatedCount())
			.destroyedCount(factoryAdapter.getDestroyedCount())
			.properties(getProps())
			.build();
	}

	@Override
	public void shutdown() {
		delegate.close();
	}
}