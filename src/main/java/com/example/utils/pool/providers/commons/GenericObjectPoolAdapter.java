package com.example.utils.pool.providers.commons;

import java.time.Duration;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.beans.PoolProperties;
import com.example.utils.pool.metrics.PoolMetrics;
import com.example.utils.pool.providers.adapters.AbstractPoolAdapter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/** Commons Pool2 GenericObjectPool-backed implementation of Pool */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenericObjectPoolAdapter<T> extends AbstractPoolAdapter<T> implements Pool<T> {
	GenericObjectPool<T> delegate;

	public GenericObjectPoolAdapter(String name, PoolProperties props, PoolObjectFactory<T> factory) {
		super(name, props);

		val cfg = new GenericObjectPoolConfig<T>();
		cfg.setMaxTotal(props.getMaxTotal());
		cfg.setMaxIdle(props.getMaxIdle());
		cfg.setMinIdle(props.getMinIdle());
		cfg.setTestOnBorrow(props.getTestOnBorrow());
		cfg.setTestOnReturn(props.getTestOnReturn());
		cfg.setTestWhileIdle(props.getTestWhileIdle());
		cfg.setMaxWait(Duration.ofMillis(props.getMaxWaitMillis()));
		cfg.setTimeBetweenEvictionRuns(Duration.ofMillis(props.getTimeBetweenEvictionRunsMillis()));

		this.delegate = new GenericObjectPool<>(new Commons2FactoryAdapter<>(factory), cfg);
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
	public void shutdown() {
		delegate.close();
	}

	@Override
	public PoolMetrics getMetrics() {
		return PoolMetrics.builder()
			.numActive(delegate.getNumActive())
			.numIdle(delegate.getNumIdle())
			.numWaiters(delegate.getNumWaiters())
			.borrowedCount(delegate.getBorrowedCount())
			.returnedCount(delegate.getReturnedCount())
			.createdCount(delegate.getCreatedCount())
			.destroyedCount(delegate.getDestroyedCount())
			.properties(getProps())
			.build();
	}
}

