package com.example.utils.xml.services.validation;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.example.utils.xml.services.validation.exceptions.XmlValidationException;
import lombok.Getter;
import lombok.Value;

@Getter
@Value(staticConstructor = "of")
public class ValidationResult {

	public enum Severity {ERROR, WARNING, INFO}

	boolean valid;
	List<Problem> problems;

	public static ValidationResult of(boolean valid, Collection<Problem> problems) {
		return new ValidationResult(valid, List.copyOf(problems));
	}

	public void throwIfInvalid() {
		throwIfInvalid(XmlValidationException::new);
	}

	public <E extends Exception> void throwIfInvalid(Supplier<E> exceptionSupplier) throws E {
		throwIfInvalid(problems -> exceptionSupplier.get());
	}

	public <E extends Exception> void throwIfInvalid(Function<List<Problem>, E> exceptionSupplier) throws E {
		if (!isValid())
			throw exceptionSupplier.apply(problems);
	}

	@Value
	public static class Problem {
		Severity severity;
		String message;
		int line;
		int column;
	}
}