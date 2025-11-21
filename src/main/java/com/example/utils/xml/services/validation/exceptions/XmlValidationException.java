package com.example.utils.xml.services.validation.exceptions;

import java.util.List;

import com.example.utils.xml.services.validation.ValidationResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlValidationException extends RuntimeException {
	List<ValidationResult.Problem> problems;

	public XmlValidationException(List<ValidationResult.Problem> problems) {
		super(buildErrorMessage(problems));
		this.problems = problems;
	}

	private static String buildErrorMessage(List<ValidationResult.Problem> problems) {
		val sb = new StringBuilder("O XML fornecido não é válido:\n");
		for (ValidationResult.Problem problem : problems) {
			sb.append(String.format(" - [%s] Line %d, Column %d: %s%n",
				problem.getSeverity(),
				problem.getLine(),
				problem.getColumn(),
				problem.getMessage()));
		}
		return sb.toString();
	}
}
