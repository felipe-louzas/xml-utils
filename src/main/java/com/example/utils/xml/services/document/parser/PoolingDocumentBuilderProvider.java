package com.example.utils.xml.services.document.parser;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.pool.Pool;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.function.FailableFunction;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PoolingDocumentBuilderProvider implements DocumentBuilderProvider {
	Pool<DocumentBuilder> documentBuilderPool;

	@Override
	public <T, E extends Exception> T parse(FailableFunction<DocumentBuilder, T, E> parser) throws E {
		return documentBuilderPool.use(parser);
	}
}
