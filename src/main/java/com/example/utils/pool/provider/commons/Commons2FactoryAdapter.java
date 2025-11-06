package com.example.utils.pool.provider.commons;

import com.example.utils.pool.PoolObjectFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.pool2.PooledObjectFactory;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Commons2FactoryAdapter<T> extends PooledObjectFactory<T> {
	PoolObjectFactory<T> delegate;
}
