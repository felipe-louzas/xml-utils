package com.example.utils.core;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class Words {
	public String toKebabCase(String input) {
		if (StringUtils.isBlank(input)) return input;
		val words = StringUtils.splitByCharacterTypeCamelCase(input);
		return StringUtils.join(words, '-').toLowerCase();
	}
}
