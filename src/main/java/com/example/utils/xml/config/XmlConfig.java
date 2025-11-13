package com.example.utils.xml.config;

import java.util.Map;

/**
 * Interface pública e imutável para as configurações do módulo XML Utils.
 * <p>
 * Define um contrato de acesso (somente leitura) às propriedades de configuração do parser, cache, pool e outros componentes.
 */
public interface XmlConfig {
	/**
	 * Inicia a construção de uma nova instância de XmlConfigProperties.
	 *
	 * @return O construtor para XmlConfigProperties.
	 */
	static XmlConfigProperties.XmlConfigPropertiesBuilder builder() {
		return XmlConfigProperties.builder();
	}

	/**
	 * @return A configuração padrão recomendada para a maioria dos casos de uso.
	 */
	static XmlConfig getDefault() { return builder().build(); }

	/**
	 * @return Configurações do parser XML (DocumentBuilderFactory).
	 */
	ParserConfig getParser();

	/**
	 * @return Configurações de cache para XPath e Schema (objetos imutáveis).
	 */
	CacheConfig getCache();

	/**
	 * @return Configurações de object pool para componentes não thread-safe.
	 */
	PoolConfig getPool();

	/**
	 * @return Configurações de formatação e saída XML.
	 */
	FormatterConfig getFormatter();

	/**
	 * @return Configurações do Transformer / XSLT.
	 */
	TransformerConfig getTransformer();

	/**
	 * @return Configurações de segurança e limites do parser.
	 */
	SecurityConfig getSecurity();

	/**
	 * @return Configurações de validação XML Schema.
	 */
	ValidationConfig getValidation();

	/**
	 * @return Configurações de avaliação XPath.
	 */
	XPathConfig getXpath();

	// --- Interfaces Aninhadas ---

	/**
	 * Configurações do parser XML. Controla o comportamento de leitura, segurança e normalização.
	 */
	interface ParserConfig {
		/** @return true se o reconhecimento de namespaces estiver habilitado. */
		boolean isNamespaceAware();

		/** @return true se as seções CDATA devem ser consolidadas em texto simples. */
		boolean isCoalescing();

		/** @return true se as referências de entidade devem ser expandidas (false é mais seguro). */
		boolean isExpandEntityReferences();

		/** @return true se os comentários devem ser ignorados no DOM. */
		boolean isIgnoreComments();

		/** @return true se espaços em branco entre elementos devem ser ignorados. */
		boolean isIgnoreWhitespace();

		/** @return true se o processamento XInclude estiver habilitado (desativado por segurança). */
		boolean isXincludeAware();
	}

	/**
	 * Configurações de cache para componentes thread-safe (XPath, Schema, Templates). O cache é opcional e geralmente ativado apenas em
	 * ambientes Spring.
	 */
	interface CacheConfig {
		/** @return Configuração do cache para XPathExpression compilados. */
		ComponentCacheConfig getXpath();

		/** @return Configuração do cache para Schema compilados. */
		ComponentCacheConfig getSchema();
	}

	/**
	 * Configuração individual de cache.
	 */
	interface ComponentCacheConfig {
		/** @return true se o cache do componente estiver habilitado. */
		boolean isEnabled();

		/** @return O nome lógico do cache (usado por gerenciadores de cache externos). */
		String getCacheName();
	}

	/**
	 * Configurações de pool para objetos não thread-safe (DocumentBuilder, Transformer, Validator). O pool reaproveita instâncias,
	 * reduzindo custo de criação em ambientes concorrentes.
	 */
	interface PoolConfig {
		/** @return Configuração do pool para DocumentBuilder. */
		ComponentPoolConfig getDocumentBuilder();

		/** @return Configuração do pool para Validator. */
		ComponentPoolConfig getValidator();

		/** @return Configuração do pool para Transformer. */
		ComponentPoolConfig getTransformer();
	}

	/**
	 * Configuração individual de pool.
	 */
	interface ComponentPoolConfig {
		/** @return true se o pool estiver habilitado. Se desativado, uma nova instância é criada a cada uso. */
		boolean isEnabled();

		/** @return O tamanho máximo do pool. */
		int getMaxPoolSize();
	}

	/**
	 * Configurações de formatação da saída XML. Controla indentação, normalização e espaçamento.
	 */
	interface FormatterConfig {
		/** @return true se a saída XML deve ser indentada. */
		boolean isIndent();

		/** @return A quantidade de espaços por nível de indentação. */
		int getIndentAmount();

		/** @return true se a declaração XML inicial deve ser omitida. */
		boolean isOmitXmlDeclaration();

		/** @return O separador de linha usado na saída (ex: "\n"). */
		String getLineSeparator();

		/** @return true se espaços em branco no conteúdo devem ser preservados. */
		boolean isPreserveWhitespace();

		/** @return true se espaços consecutivos em texto devem ser normalizados. */
		boolean isNormalizeWhitespace();
	}

	/**
	 * Configurações do Transformer (XSLT ou serialização XML).
	 */
	interface TransformerConfig {
		/** @return A codificação padrão de saída (ex: "UTF-8"). */
		String getEncoding();

		/** @return O método de transformação: "xml", "html" ou "text". */
		String getMethod();
	}

	/**
	 * Configurações de segurança gerais para processadores XML.
	 */
	interface SecurityConfig {
		/** @return true para ativar o modo de processamento seguro (recomendado). */
		boolean isSecureProcessing();

		/** @return true para desativar declarações DOCTYPE (previne XXE). */
		boolean isDisableDtd();

		/** @return true para restringir entidades externas apenas pelos protocolos especificados (previne XXE). */
		boolean isRestrictExternalEntities();

		/** @return Protocolos permitidos para DTD externos (ex: "file,http"). Vazio = nenhum. */
		String getProtocolsExternalDTD();

		/** @return Protocolos permitidos para Schemas externos. */
		String getProtocolsExternalSchema();

		/** @return Protocolos permitidos para Stylesheets externos. */
		String getProtocolsExternalStylesheet();

		/**
		 * Lógica de negócio derivada: DTD está desativado se o modo seguro estiver ativo ou se a desativação explícita estiver ligada.
		 *
		 * @return true se o DTD estiver efetivamente desabilitado.
		 */
		default boolean isDtdDisabled() {
			return isSecureProcessing() || isDisableDtd();
		}

		/**
		 * Lógica de negócio derivada: Entidades externas só são permitidas se o modo seguro E a restrição explícita estiverem desligados.
		 *
		 * @return true se entidades externas são permitidas.
		 */
		default boolean isAllowExternalEntities() {
			return !isSecureProcessing() && !isRestrictExternalEntities();
		}
	}

	/**
	 * Configurações de validação XML (XSD, Schema).
	 */
	interface ValidationConfig {
		/** @return true se warnings de validação devem ser tratados como erros. */
		boolean isFailOnWarning();
	}

	/**
	 * Configurações para avaliação de expressões XPath.
	 */
	interface XPathConfig {
		/** @return Mapa (imutável) de prefixo -> namespace padrão para contexto XPath. */
		Map<String, String> getDefaultNamespaceMappings();

		/** @return O nome da classe personalizada para resolver funções XPath (opcional). */
		String getFunctionResolverClass();

		/** @return O nome da classe personalizada para resolver variáveis XPath (opcional). */
		String getVariableResolverClass();
	}
}