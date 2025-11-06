package com.example.utils.pool.provider.commons;

import java.time.Duration;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.config.PoolProperties;
import com.example.utils.pool.exceptions.PoolException;
import com.example.utils.pool.metrics.PoolMetrics;
import com.example.utils.pool.provider.AbstractPoolAdapter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/** Commons Pool2 backed implementation of Pool */
@Slf4j
public class GenericPoolAdapter<T> extends AbstractPoolAdapter<T> implements Pool<T> {
	private final GenericObjectPool<T> delegate;

	public GenericPoolAdapter(String name, PoolProperties props, PoolObjectFactory<T> factory) {
		super(name, props);

		val cfg = new GenericObjectPoolConfig<T>();
		cfg.setMaxTotal(props.getMaxTotal());
		cfg.setMaxIdle(props.getMaxIdle());
		cfg.setMinIdle(props.getMinIdle());
		cfg.setTestOnBorrow(props.isTestOnBorrow());
		cfg.setTestOnReturn(props.isTestOnReturn());
		cfg.setTestWhileIdle(props.isTestWhileIdle());
		cfg.setMaxWait(Duration.ofMillis(props.getMaxWaitMillis()));
		cfg.setTimeBetweenEvictionRuns(Duration.ofMillis(props.getTimeBetweenEvictionRunsMillis()));

		this.delegate = new GenericObjectPool<>(new Commons2FactoryAdapter<>(factory), cfg);
	}

	@Override
	public Pool.Handle<T> borrow() throws PoolException {
		try {
			return new ReturningHandle(
				delegate.borrowObject(),
				obj -> {
					try {
						delegate.returnObject(obj);
					} catch (Exception e) {
						log.warn("Failed to return object to pool '{}'", name, e);
					}
				}
			)
			T obj = delegate.borrowObject();

		} catch (Exception ex) {
			throw new PoolException("Failed to borrow object from pool '" + name + "'", e);
		}
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public PoolMetrics getMetrics() {
		return PoolMetrics.builder()
			.numActive(delegate.getNumActive())
			.numIdle(delegate.getNumIdle())
			.numWaiters
			.borrowedCount
			.returnedCount
			.createdCount
			.destroyedCount
			.properties(getProps())
			.build();
	}
}

