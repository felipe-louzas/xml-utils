package com.example.utils.pool.provider.commons;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.config.PoolProperties;

public class SoftReferencePoolAdapter<T> implements Pool<T> {
	public GenericPoolAdapter(String name, PoolProperties props, PoolObjectFactory<T> factory) {
	}
