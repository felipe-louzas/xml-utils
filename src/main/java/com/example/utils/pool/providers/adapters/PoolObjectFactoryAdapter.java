package com.example.utils.pool.providers.adapters;

import com.example.utils.core.Objects;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.exceptions.PoolException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PoolObjectFactoryAdapter<T> implements PoolObjectFactory<T> {
	final PoolObjectFactory<T> delegate;

	@Getter
	int createdCount = 0;

	@Getter
	int destroyedCount = 0;

	public final T create() {
		return handleException("criar objeto", () -> {
			val obj = delegate.create();
			createdCount++;
			log.atDebug()
				.addKeyValue("factory", () -> Objects.shortIdString(delegate))
				.log(() -> "create: " + Objects.shortIdString(obj));
			return obj;
		});
	}

	public final void destroy(T object) {
		handleException("destruir objeto", () -> {
			delegate.destroy(object);
			destroyedCount++;
			log.atDebug()
				.addKeyValue("factory", () -> Objects.shortIdString(delegate))
				.log(() -> "destroy: " + Objects.shortIdString(object));
		});
	}

	public final void reset(T object) {
		handleException("resetar objeto", () -> {
			delegate.reset(object);
			log.atDebug()
				.addKeyValue("factory", () -> Objects.shortIdString(delegate))
				.log(() -> "reset: " + Objects.shortIdString(object));
		});
	}

	public final boolean validate(T object) {
		return handleException("validar objeto", () -> {
			boolean valid = delegate.validate(object);
			log.atDebug()
				.addKeyValue("factory", () -> Objects.shortIdString(delegate))
				.addKeyValue("valid", valid)
				.log(() -> "validate: " + Objects.shortIdString(object));
			return valid;
		});
	}

	private <E extends Exception> void handleException(String action, FailableRunnable<E> runnable) {
		handleException(action, () -> {
			runnable.run();
			return null;
		});
	}

	private <R, E extends Exception> R handleException(String action, FailableSupplier<R, E> supplier) {
		try {
			return supplier.get();
		} catch (PoolException ex) {
			log.warn("PoolException ao {}: {}", action, ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			val name = ex.getClass().getSimpleName();
			log.warn("{} ao {}: {}", name, action, ex.getMessage());
			throw new PoolException(name + " ao " + action, ex);
		}
	}
}
