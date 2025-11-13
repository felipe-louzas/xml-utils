package com.example.utils.xml.services.validation;

import java.util.Collections;
import java.util.List;

public final class ValidationResult {
	private final boolean valid;
	private final List<ValidationProblem> problems;

	public ValidationResult(boolean valid, List<ValidationProblem> problems) {
		this.valid = valid;
		this.problems = problems == null ? Collections.emptyList() : Collections.unmodifiableList(problems);
	}

	public boolean isValid() { return valid; }

	public List<ValidationProblem> getProblems() { return problems; }

	public static final class ValidationProblem {
		public enum Severity {ERROR, WARNING, INFO}

		private final Severity severity;
		private final String message;
		private final int line;
		private final int column;

		public ValidationProblem(Severity severity, String message, int line, int column) {
			this.severity = severity;
			this.message = message;
			this.line = line;
			this.column = column;
		}

		public Severity getSeverity() { return severity; }

		public String getMessage() { return message; }

		public int getLine() { return line; }

		public int getColumn() { return column; }
	}
}

