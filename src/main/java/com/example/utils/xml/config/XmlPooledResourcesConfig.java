package com.example.utils.xml.config;

import javax.xml.parsers.DocumentBuilder;

import com.example.utils.pool.Pool;
import com.example.utils.pool.PoolObjectFactory;
import com.example.utils.pool.manager.PoolManager;
import com.example.utils.xml.config.constants.ConfigKeys;
import com.example.utils.xml.services.document.parser.DocumentBuilderProvider;
import com.example.utils.xml.services.document.parser.PoolingDocumentBuilderProvider;
import com.example.utils.xml.services.factory.XmlFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(PoolManager.class)
public class XmlPooledResourcesConfig {
	XmlFactory xmlFactory;

	@Bean
	@ConditionalOnBooleanProperty(name = ConfigKeys.PROP_POOL_DOCUMENT_BUILDER_ENABLED)
	public PoolObjectFactory<DocumentBuilder> documentBuilderPoolFactory() {
		return new PoolObjectFactory<>() {
			@Override
			public DocumentBuilder create() throws Exception {
				return xmlFactory.getDocumentBuilderFactory().newDocumentBuilder();
			}

			@Override
			public void reset(DocumentBuilder documentBuilder) {
				documentBuilder.reset();
			}
		};
	}

	@Bean
	@ConditionalOnBooleanProperty(name = ConfigKeys.PROP_POOL_DOCUMENT_BUILDER_ENABLED)
	@ConditionalOnBean(value = DocumentBuilder.class, parameterizedContainer = Pool.class)
	public DocumentBuilderProvider poolingXmlParserBuilder(Pool<DocumentBuilder> documentBuilderPool) {
		return new PoolingDocumentBuilderProvider(documentBuilderPool);
	}
}
