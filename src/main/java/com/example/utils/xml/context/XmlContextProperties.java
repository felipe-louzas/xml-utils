package com.example.utils.xml.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the XML Utility library, mappable from application.yaml (e.g., "xml.utils.caching.enabled").
 * <p>
 * Provides sensible, secure-by-default settings.
 */
@Data
@ConfigurationProperties(prefix = "utils.xml")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XmlContextProperties {

	/** Settings for the XML parser. */
	ParserConfig parser = new ParserConfig();

	/** XPath and Schema caching configuration */
	CacheConfig cache = new CacheConfig();

	/** Settings for XML formatting. */
	FormatterConfig formatter = new FormatterConfig();

	@Data
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ParserConfig {
		/** Disables DOCTYPE declarations. Secure default (true) helps prevent XXE attacks. */
		boolean disableDtd = true;

		/** Disables external entity loading. Secure default (true) helps prevent XXE attacks. */
		boolean disableExternalEntities = true;

		/** Enables namespace awareness in the parser. */
		boolean namespaceAware = true;
	}

	@Data
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class CacheConfig {
		/** Cache config for compiled XPath expressions. */
		ComponentCacheConfig xpath = new ComponentCacheConfig(false, "xml-xpath");

		/** Cache config for compiled Schemas. */
		ComponentCacheConfig schema = new ComponentCacheConfig(false, "xml-schema");
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ComponentCacheConfig {
		/** Whether caching is enabled for this component. */
		boolean enabled;

		/** The name of the cache to use. */
		String cacheName;
	}

	@Data
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class FormatterConfig {
		/** Whether to indent the output. */
		boolean indent = true;

		/** The number of spaces to use for indentation. */
		int indentAmount = 2;

		/** Whether to omit the <?xml ...?> declaration. */
		boolean omitXmlDeclaration = false;
	}
}

