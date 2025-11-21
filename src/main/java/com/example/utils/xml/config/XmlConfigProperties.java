package com.example.utils.xml.config;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class XmlConfigProperties implements XmlConfig {
	@Builder.Default
	ParserConfig parser = ParserConfig.builder().build();

	@Builder.Default
	CacheConfig cache = CacheConfig.builder().build();

	@Builder.Default
	PoolConfig pool = PoolConfig.builder().build();

	@Builder.Default
	FormatterConfig formatter = FormatterConfig.builder().build();

	@Builder.Default
	TransformerConfig transformer = TransformerConfig.builder().build();

	@Builder.Default
	SecurityConfig security = SecurityConfig.builder().build();

	@Builder.Default
	ValidationConfig validation = ValidationConfig.builder().build();

	@Builder.Default
	XPathConfig xpath = XPathConfig.builder().build();

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ParserConfig implements XmlConfig.ParserConfig {
		@Builder.Default
		boolean namespaceAware = true;

		@Builder.Default
		boolean coalescing = true;

		@Builder.Default
		boolean expandEntityReferences = false;

		@Builder.Default
		boolean ignoreComments = true;

		@Builder.Default
		boolean ignoreWhitespace = false;

		@Builder.Default
		boolean xincludeAware = false;
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class CacheConfig implements XmlConfig.CacheConfig {
		private static final String DEFAULT_XPATH_CACHE_NAME = "utils.xml.xpath";
		private static final String DEFAULT_SCHEMA_CACHE_NAME = "utils.xml.schema";

		@Builder.Default
		ComponentCacheConfig xpath = ComponentCacheConfig.builder().cacheName(DEFAULT_XPATH_CACHE_NAME).build();

		@Builder.Default
		ComponentCacheConfig schema = ComponentCacheConfig.builder().cacheName(DEFAULT_SCHEMA_CACHE_NAME).build();

		public static class CacheConfigBuilder {
			public CacheConfigBuilder xpathConfig(Function<ComponentCacheConfig.ComponentCacheConfigBuilder, ComponentCacheConfig.ComponentCacheConfigBuilder> configurer) {
				return xpath(configurer.apply(ComponentCacheConfig.builder().cacheName(DEFAULT_XPATH_CACHE_NAME)).build());
			}

			public CacheConfigBuilder schemaConfig(Function<ComponentCacheConfig.ComponentCacheConfigBuilder, ComponentCacheConfig.ComponentCacheConfigBuilder> configurer) {
				return schema(configurer.apply(ComponentCacheConfig.builder().cacheName(DEFAULT_SCHEMA_CACHE_NAME)).build());
			}
		}
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ComponentCacheConfig implements XmlConfig.ComponentCacheConfig {
		@Builder.Default
		boolean enabled = false;

		String cacheName;
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class PoolConfig implements XmlConfig.PoolConfig {
		@Builder.Default
		ComponentPoolConfig documentBuilder = ComponentPoolConfig.builder().build();

		@Builder.Default
		ComponentPoolConfig validator = ComponentPoolConfig.builder().build();

		@Builder.Default
		ComponentPoolConfig transformer = ComponentPoolConfig.builder().build();

		public static class PoolConfigBuilder {
			public PoolConfigBuilder documentBuilderConfig(Function<ComponentPoolConfig.ComponentPoolConfigBuilder, ComponentPoolConfig.ComponentPoolConfigBuilder> configurer) {
				return documentBuilder(configurer.apply(ComponentPoolConfig.builder()).build());
			}

			public PoolConfigBuilder validatorConfig(Function<ComponentPoolConfig.ComponentPoolConfigBuilder, ComponentPoolConfig.ComponentPoolConfigBuilder> configurer) {
				return validator(configurer.apply(ComponentPoolConfig.builder()).build());
			}

			public PoolConfigBuilder transformerConfig(Function<ComponentPoolConfig.ComponentPoolConfigBuilder, ComponentPoolConfig.ComponentPoolConfigBuilder> configurer) {
				return transformer(configurer.apply(ComponentPoolConfig.builder()).build());
			}
		}
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ComponentPoolConfig implements XmlConfig.ComponentPoolConfig {
		@Builder.Default
		boolean enabled = false;

		@Builder.Default
		int maxPoolSize = 16;
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class FormatterConfig implements XmlConfig.FormatterConfig {
		@Builder.Default
		boolean indent = true;

		@Builder.Default
		int indentAmount = 2;

		@Builder.Default
		boolean omitXmlDeclaration = false;

		@Builder.Default
		String lineSeparator = "\n";

		@Builder.Default
		boolean preserveWhitespace = false;

		@Builder.Default
		boolean normalizeWhitespace = false;
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class TransformerConfig implements XmlConfig.TransformerConfig {
		@Builder.Default
		String encoding = "UTF-8";

		@Builder.Default
		String method = "xml";
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class SecurityConfig implements XmlConfig.SecurityConfig {
		@Builder.Default
		boolean secureProcessing = true;

		@Builder.Default
		boolean disableDtd = true;

		@Builder.Default
		boolean restrictExternalEntities = true;

		@Builder.Default
		String protocolsExternalDTD = StringUtils.EMPTY;

		@Builder.Default
		String protocolsExternalSchema = StringUtils.EMPTY;

		@Builder.Default
		String protocolsExternalStylesheet = StringUtils.EMPTY;

		public boolean isDisableDtd() {
			return secureProcessing || disableDtd;
		}

		public boolean isAllowExternalEntities() {
			return !secureProcessing && !restrictExternalEntities;
		}
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class ValidationConfig implements XmlConfig.ValidationConfig {
		@Builder.Default
		boolean failOnWarning = false;
	}

	@Data
	@Builder
	@NoArgsConstructor(access = AccessLevel.PACKAGE)
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	public static class XPathConfig implements XmlConfig.XPathConfig {
		@Builder.Default
		Map<String, String> defaultNamespaceMappings = Collections.emptyMap();

		String functionResolverClass;

		String variableResolverClass;

		public Map<String, String> getDefaultNamespaceMappings() {
			return Collections.unmodifiableMap(defaultNamespaceMappings);
		}
	}

	public static class XmlConfigPropertiesBuilder {

		public XmlConfigPropertiesBuilder parserConfig(UnaryOperator<ParserConfig.ParserConfigBuilder> configurer) {
			return parser(configurer.apply(ParserConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder cacheConfig(UnaryOperator<CacheConfig.CacheConfigBuilder> configurer) {
			return cache(configurer.apply(CacheConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder poolConfig(UnaryOperator<PoolConfig.PoolConfigBuilder> configurer) {
			return pool(configurer.apply(PoolConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder formatterConfig(UnaryOperator<FormatterConfig.FormatterConfigBuilder> configurer) {
			return formatter(configurer.apply(FormatterConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder transformerConfig(UnaryOperator<TransformerConfig.TransformerConfigBuilder> configurer) {
			return transformer(configurer.apply(TransformerConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder securityConfig(UnaryOperator<SecurityConfig.SecurityConfigBuilder> configurer) {
			return security(configurer.apply(SecurityConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder validationConfig(UnaryOperator<ValidationConfig.ValidationConfigBuilder> configurer) {
			return validation(configurer.apply(ValidationConfig.builder()).build());
		}

		public XmlConfigPropertiesBuilder xpathConfig(UnaryOperator<XPathConfig.XPathConfigBuilder> configurer) {
			return xpath(configurer.apply(XPathConfig.builder()).build());
		}
	}
}
