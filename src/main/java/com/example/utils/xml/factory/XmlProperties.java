package com.example.utils.xml.factory;

import java.util.Collections;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Configurações principais do módulo XML Utils.
 * <p>
 * Pode ser mapeada a partir de <code>application.yaml</code> (ex: <code>utils.xml.cache.xpath.enabled</code>)
 * ou configurada manualmente via métodos fluidos.
 * <p>
 * Define opções seguras por padrão e ajustáveis para ambientes de alta concorrência e baixa latência.
 */
@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XmlProperties {

	/** Configurações do parser XML (DocumentBuilderFactory). */
	ParserConfig parser = new ParserConfig();

	/** Configurações de cache para XPath e Schema (objetos imutáveis). */
	CacheConfig cache = new CacheConfig();

	/** Configurações de pool para componentes não thread-safe. */
	PoolConfig pool = new PoolConfig();

	/** Configurações de formatação e saída XML. */
	FormatterConfig formatter = new FormatterConfig();

	/** Configurações do Transformer / XSLT. */
	TransformerConfig transformer = new TransformerConfig();

	/** Configurações de segurança e limites do parser. */
	SecurityConfig security = new SecurityConfig();

	/** Configurações de validação XML Schema. */
	ValidationConfig validation = new ValidationConfig();

	/** Configurações de avaliação XPath. */
	XPathConfig xpath = new XPathConfig();

	// ------------------------------------------------------------------------
	// Nested Config Classes
	// ------------------------------------------------------------------------

	/**
	 * Configurações do parser XML.
	 * Controla o comportamento de leitura, segurança e normalização.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ParserConfig {
		/** Desativa declarações DOCTYPE (previne XXE). */
		boolean disableDtd = true;

		/** Desativa entidades externas (previne XXE). */
		boolean disableExternalEntities = true;

		/** Habilita reconhecimento de namespaces. */
		boolean namespaceAware = true;

		/** Consolida seções CDATA em texto simples. */
		boolean coalescing = true;

		/** Expande referências de entidade (false é mais seguro). */
		boolean expandEntityReferences = false;

		/** Ignora comentários no DOM. */
		boolean ignoreComments = true;

		/** Ignora espaços em branco entre elementos. */
		boolean ignoreWhitespace = false;

		/** Habilita processamento XInclude (desativado por segurança). */
		boolean xincludeAware = false;
	}

	/**
	 * Configurações de cache para componentes thread-safe (XPath, Schema, Templates).
	 * Cache é opcional e geralmente ativado apenas em ambientes Spring.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class CacheConfig {
		ComponentCacheConfig xpath = new ComponentCacheConfig(false, "utils.xml.xpath");
		ComponentCacheConfig schema = new ComponentCacheConfig(false, "utils.xml.schema");
	}

	/**
	 * Configuração individual de cache.
	 */
	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ComponentCacheConfig {
		/** Habilita o cache do componente. */
		boolean enabled;
		/** Nome lógico do cache (usado por gerenciadores de cache externos). */
		String cacheName;
	}

	/**
	 * Configurações de pool para objetos não thread-safe (DocumentBuilder, Transformer, Validator).
	 * O pool reaproveita instâncias, reduzindo custo de criação em ambientes concorrentes.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class PoolConfig {
		ComponentPoolConfig documentBuilder = new ComponentPoolConfig(false, 16);
		ComponentPoolConfig validator = new ComponentPoolConfig(false, 16);
		ComponentPoolConfig transformer = new ComponentPoolConfig(false, 16);
	}

	/**
	 * Configuração individual de pool.
	 */
	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ComponentPoolConfig {
		/** Se desativado, uma nova instância é criada a cada uso. */
		boolean enabled;
		/** Tamanho máximo do pool. */
		int maxPoolSize;
	}

	/**
	 * Configurações de formatação da saída XML.
	 * Controla indentação, normalização e espaçamento.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class FormatterConfig {
		/** Indenta o XML de saída. */
		boolean indent = true;
		/** Quantidade de espaços por nível de indentação. */
		int indentAmount = 2;
		/** Remove a declaração XML inicial. */
		boolean omitXmlDeclaration = false;
		/** Separador de linha usado na saída. */
		String lineSeparator = "\n";
		/** Preserva espaços em branco no conteúdo. */
		boolean preserveWhitespace = false;
		/** Normaliza espaços consecutivos em texto. */
		boolean normalizeWhitespace = false;
	}

	/**
	 * Configurações do Transformer (XSLT ou serialização XML).
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class TransformerConfig {
		/** Codificação padrão de saída. */
		String encoding = "UTF-8";
		/** Método de transformação: xml, html ou text. */
		String method = "xml";
		/** Ativa processamento seguro (limita scripts, entidades, etc). */
		boolean secureProcessing = true;
	}

	/**
	 * Configurações de segurança gerais para processadores XML.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class SecurityConfig {
		/** Protocolos permitidos para DTD externos (ex: "file,http,jar"). Vazio = nenhum. */
		String accessExternalDTD = "";
		/** Protocolos permitidos para Schemas externos. */
		String accessExternalSchema = "";
		/** Ativa o modo de processamento seguro (recomendado). */
		boolean secureProcessing = true;
	}

	/**
	 * Configurações de validação XML (XSD, Schema).
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class ValidationConfig {
		/** Se verdadeiro, warnings de validação são tratados como erros. */
		boolean failOnWarning = false;
	}

	/**
	 * Configurações para avaliação de expressões XPath.
	 */
	@Data
	@Accessors(chain = true)
	@FieldDefaults(level = AccessLevel.PRIVATE)
	public static class XPathConfig {
		/** Mapeamento prefixo -> namespace padrão para contexto XPath. */
		Map<String, String> defaultNamespaceMappings = Collections.emptyMap();
		/** Classe personalizada para resolver funções XPath. */
		String functionResolverClass;
		/** Classe personalizada para resolver variáveis XPath. */
		String variableResolverClass;
	}

	// ------------------------------------------------------------------------
	// Métodos utilitários para configuração fluente fora do Spring
	// ------------------------------------------------------------------------

	public XmlProperties withParser(java.util.function.Consumer<ParserConfig> c) {
		c.accept(parser);
		return this;
	}

	public XmlProperties withCache(java.util.function.Consumer<CacheConfig> c) {
		c.accept(cache);
		return this;
	}

	public XmlProperties withPool(java.util.function.Consumer<PoolConfig> c) {
		c.accept(pool);
		return this;
	}

	public XmlProperties withFormatter(java.util.function.Consumer<FormatterConfig> c) {
		c.accept(formatter);
		return this;
	}

	public XmlProperties withTransformer(java.util.function.Consumer<TransformerConfig> c) {
		c.accept(transformer);
		return this;
	}

	public XmlProperties withSecurity(java.util.function.Consumer<SecurityConfig> c) {
		c.accept(security);
		return this;
	}

	public XmlProperties withValidation(java.util.function.Consumer<ValidationConfig> c) {
		c.accept(validation);
		return this;
	}

	public XmlProperties withXPath(java.util.function.Consumer<XPathConfig> c) {
		c.accept(xpath);
		return this;
	}
}
