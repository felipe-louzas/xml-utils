package com.example.utils.patterns;

import java.util.Objects;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * Suporte para um holder singleton, seguro para threads e inicialização lazy.
 * <p>
 * Uso:
 * <ul>
 *     <li> fallback por supplier: DefaultSingleton.of(() -> new DefaultImpl())
 *     <li> fallback por classe: DefaultSingleton.of(DefaultImpl.class)
 *     <li> inicialização explícita: holder.set(instance) ou holder.setIfAbsent(instance)
 * </ul>
 * O holder permite inicialização explícita antes da primeira chamada a get(). A implementação usa checagem inicial sem bloqueio seguida de
 * uma atualização sincronizada (compare-and-set) para garantir segurança em concorrência.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class LazyInitSingleton<T> {

	final Supplier<? extends T> defaultSupplier;
	volatile T instance;

	/**
	 * Cria um holder que irá instanciar {@code defaultClass} preguiçosamente quando necessário.
	 */
	public static <T> LazyInitSingleton<T> of(@NonNull Class<? extends T> defaultClass) {
		Objects.requireNonNull(defaultClass, "defaultClass não pode ser nulo");
		return new LazyInitSingleton<>(() -> newInstance(defaultClass));
	}

	/**
	 * Cria um holder usando um Supplier como fallback de criação.
	 */
	public static <T> LazyInitSingleton<T> of(@NonNull Supplier<? extends T> defaultSupplier) {
		Objects.requireNonNull(defaultSupplier, "defaultSupplier não pode ser nulo");
		return new LazyInitSingleton<>(defaultSupplier);
	}

	private static <T> T newInstance(Class<? extends T> clazz) {
		try {
			return ConstructorUtils.invokeConstructor(clazz);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Não foi possível instanciar a classe " + clazz.getName()
				+ ". Verifique se existe um construtor padrão e se a classe é acessível.", ex);
		}
	}

	/**
	 * Obtém a instância singleton, inicializando-a se necessário.
	 */
	public T get() {
		if (instance == null) {
			setInstance(null, defaultSupplier);
		}
		return instance;
	}

	/**
	 * Define explicitamente a instância antes da inicialização. Lança se já inicializado. Útil para frameworks de DI que registram o bean
	 * antecipadamente.
	 */
	public void set(@NonNull T value) {
		set(() -> value);
	}

	public void set(@NonNull Supplier<T> supplier) {
		boolean set = false;
		if (instance == null) {
			set = setInstance(null, supplier);
		}
		if (!set) throw new IllegalStateException("Singleton já foi inicializado e não pode ser alterado.");
	}

	/**
	 * Tenta definir a instância apenas se ausente. Retorna true se definiu, false caso já exista.
	 */
	public boolean setIfAbsent(@NonNull T value) {
		return setIfAbsent(() -> value);
	}

	/**
	 * Tenta definir a instância apenas se ausente. Retorna true se definiu, false caso já exista.
	 */
	public boolean setIfAbsent(@NonNull Supplier<T> supplier) {
		if (instance != null) return false;
		return setInstance(null, supplier);
	}

	/**
	 * Indica se a instância já foi inicializada.
	 */
	public boolean isInitialized() {
		return instance != null;
	}

	/**
	 * Reseta o holder para não inicializado. Uso restrito (testes ou casos especiais).
	 */
	public void reset() {
		T current = instance;
		if (current == null) return;
		setInstance(current, () -> null);
	}

	/**
	 * Operação sincronizada de compare-and-set.
	 * <ul>
	 * <li> Se o valor atual for igual a expected, aplica o supplier e retorna true.
	 * <li> Caso contrário, não modifica e retorna false.
	 * </ul>
	 */
	@Synchronized
	private boolean setInstance(T expected, @NonNull Supplier<? extends T> supplier) {
		if (Objects.equals(instance, expected)) {
			val value = supplier.get();
			if (Objects.equals(instance, value))
				throw new IllegalStateException("Tentativa de reinicializar singleton com a mesma instância existente.");
			instance = value;
			return true;
		}
		return false;
	}
}