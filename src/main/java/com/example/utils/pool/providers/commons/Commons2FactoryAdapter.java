package com.example.utils.pool.providers.commons;

import java.util.Optional;

import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.providers.adapters.PoolObjectFactoryAdapter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Commons2FactoryAdapter<T> extends PoolObjectFactoryAdapter<T> implements PooledObjectFactory<T> {

	public Commons2FactoryAdapter(PoolObjectFactory<T> delegate) {
		super(delegate);
	}

	@Override
	public PooledObject<T> makeObject() throws Exception {
		val obj = create();
		return new DefaultPooledObject<>(obj);
	}

	@Override
	public void destroyObject(PooledObject<T> obj) throws Exception {
		destroy(unwrap(obj));
	}

	@Override
	public boolean validateObject(PooledObject<T> obj) {
		return validate(unwrap(obj));
	}

	@Override
	public void activateObject(PooledObject<T> obj) throws Exception {
		// Activation is performed before an object is borrowed from the pool.
		// The PoolObjectFactory implements only a reset method that is called when the object is returned to the pool
	}

	@Override
	public void passivateObject(PooledObject<T> obj) throws Exception {
		// Passivation is performed when an object is returned to the pool.
		reset(unwrap(obj));
	}

	private T unwrap(PooledObject<T> obj) {
		return Optional.ofNullable(obj)
			.map(PooledObject::getObject)
			.orElse(null);
	}
}
