package com.example.utils.pool.provider;

import com.example.utils.pool.Pool;
import com.example.utils.pool.config.PoolProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableSupplier;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class AbstractPoolAdapter<T> implements Pool<T> {
	String name;
	PoolProperties props;

	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ReturningHandle<T> implements Handle<T> {
		final FailableConsumer<T, ?> returner;
		T object;

		public <E1 extends Exception, E2 extends Exception> ReturningHandle(FailableSupplier<T, E1> borrower, FailableConsumer<T, E2> returner) {
			try {
				this.object = borrower.get();
			} catch (Exception ex) {

			}

			this.returner = returner;
		}

		@Override
		public T get() {
			return object;
		}

		@Override
		public void close() {
			val ref = object;
			object = null;
			returner.accept(ref);
		}
	}
}
