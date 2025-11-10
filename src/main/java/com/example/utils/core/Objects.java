package com.example.utils.core;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;

@UtilityClass
public class Objects {
	public String shortIdString(Object obj) {
		if (obj == null) return "null";
		return obj.getClass().getSimpleName() + "@" + ObjectUtils.identityHashCodeHex(obj);
	}
}
