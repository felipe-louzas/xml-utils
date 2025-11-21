package com.example.utils.xml.services.validation;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

import com.example.utils.xml.services.document.XmlDocument;
import com.example.utils.xml.services.validation.exceptions.InvalidSchemaException;
import com.example.utils.xml.services.validation.exceptions.SchemaNotFoundException;
import com.example.utils.xml.services.validation.schema.SchemaId;
import com.example.utils.xml.services.validation.schema.XmlSchema;

/**
 * Interface principal para validação de documentos XML baseada em um conjunto de schemas XSD. Fornece métodos para gestão de schemas e
 * validação de artefatos XML.
 */
public interface XmlValidator {

	/* -------- GET SCHEMA POR IDENTIFICADOR -------- */

	/**
	 * Busca um schema previamente carregado ou registrado através de seu identificador.
	 *
	 * @param schemaId identificador lógico do schema.
	 * @return um {@link Optional} contendo o {@link XmlSchema} se encontrado, ou vazio caso contrário.
	 */
	Optional<XmlSchema> findSchema(SchemaId schemaId);

	/**
	 * Obtém um schema pelo {@link SchemaId}. Se o schema não estiver em cache, tenta localizá-lo e carregá-lo utilizando os
	 * {@code SchemaResolver} registrados.
	 *
	 * @param schemaId identificador lógico do schema.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws SchemaNotFoundException se o schema não puder ser encontrado ou resolvido.
	 */
	XmlSchema getSchemaOrThrow(SchemaId schemaId) throws SchemaNotFoundException;

	/**
	 * Obtém ou compila um schema a partir de arquivos XSD.
	 * <p>
	 * O {@link SchemaId} será derivado automaticamente do caminho absoluto do primeiro arquivo.
	 *
	 * @param files array de arquivos que compõem o schema XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o conteúdo do arquivo não for um XSD válido ou não puder ser lido.
	 */
	XmlSchema getSchema(File... files) throws InvalidSchemaException;

	/**
	 * Obtém ou compila um schema a partir de caminhos de arquivo (Paths).
	 * <p>
	 * O {@link SchemaId} será derivado automaticamente do caminho absoluto do primeiro path.
	 *
	 * @param paths array de caminhos que apontam para os recursos XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o recurso não for um XSD válido ou não puder ser lido.
	 */
	XmlSchema getSchema(Path... paths) throws InvalidSchemaException;

	/**
	 * Obtém ou compila um schema a partir de URIs (suporta file:, classpath:, jar:, http:, etc).
	 * <p>
	 * O {@link SchemaId} será derivado diretamente da primeira URI fornecida.
	 *
	 * @param uris array de URIs que apontam para os recursos XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o recurso apontado não for válido ou acessível.
	 */
	XmlSchema getSchema(URI... uris) throws InvalidSchemaException;


	/* -------- LOAD SCHEMA POR CONTEÚDO -------- */

	/**
	 * Carrega e compila um schema a partir de uma string em memória.
	 * <p>
	 * O {@link SchemaId} é gerado a partir do hash do conteúdo fornecido.
	 *
	 * @param xmlSchemaContent conteúdo textual do XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o conteúdo textual não representar um XSD válido.
	 */
	XmlSchema loadSchema(CharSequence xmlSchemaContent) throws InvalidSchemaException;

	/**
	 * Carrega e compila um schema a partir de um fluxo de entrada (InputStream).
	 * <p>
	 * O {@link SchemaId} é gerado a partir do hash do conteúdo lido.
	 *
	 * @param inputStream fluxo de dados contendo o XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se ocorrer erro de leitura ou o conteúdo for inválido.
	 */
	XmlSchema loadSchema(InputStream inputStream) throws InvalidSchemaException;

	/**
	 * Carrega e compila um schema a partir de um leitor (Reader).
	 * <p>
	 * O {@link SchemaId} é gerado a partir do hash do conteúdo lido.
	 *
	 * @param reader leitor contendo os dados do XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se ocorrer erro de leitura ou o conteúdo for inválido.
	 */
	XmlSchema loadSchema(Reader reader) throws InvalidSchemaException;


	/* -------- VALIDAÇÃO DE DOCUMENTOS -------- */

	/**
	 * Identifica o {@link XmlSchema} adequado para o documento fornecido utilizando os {@code SchemaIdResolver} registrados.
	 *
	 * @param document documento XML a ser analisado.
	 * @return um {@link Optional} contendo o schema resolvido, ou vazio se nenhum schema aplicável for encontrado.
	 */
	Optional<XmlSchema> resolveSchema(XmlDocument document);

	/**
	 * Identifica o {@link XmlSchema} adequado para o documento fornecido utilizando os {@code SchemaIdResolver} registrados.
	 *
	 * @param document documento XML a ser analisado.
	 * @return um {@link XmlSchema} contendo o schema resolvido.
	 * @throws SchemaNotFoundException se nenhum schema aplicável for encontrado para o documento.
	 */
	XmlSchema resolveSchemaOrThrow(XmlDocument document);

	/**
	 * Realiza a validação de um {@link XmlDocument}.
	 * <p>
	 * O schema a ser utilizado é determinado automaticamente via {@link #resolveSchema(XmlDocument)}.
	 *
	 * @param document documento XML a ser validado.
	 * @return objeto {@link ValidationResult} contendo o status da validação e lista de erros/avisos.
	 * @throws SchemaNotFoundException se o documento exigir um schema que não pôde ser resolvido ou encontrado.
	 */
	ValidationResult validate(XmlDocument document) throws SchemaNotFoundException;


	/* -------- DEFINIÇÃO DE SCHEMAS -------- */

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de arquivos. Substitui qualquer schema
	 * anterior associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param files arquivos contendo a definição XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se os arquivos não compuserem um schema válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, File... files) throws InvalidSchemaException;

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de caminhos (Paths). Substitui qualquer schema
	 * anterior associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param paths caminhos para os recursos XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se os recursos não compuserem um schema válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, Path... paths) throws InvalidSchemaException;

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de URIs. Substitui qualquer schema anterior
	 * associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param uris URIs apontando para os recursos XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se os recursos não compuserem um schema válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, URI... uris) throws InvalidSchemaException;

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de conteúdo textual. Substitui qualquer schema
	 * anterior associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param xmlSchemaContent string contendo a definição XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o conteúdo não for um XSD válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, CharSequence xmlSchemaContent) throws InvalidSchemaException;

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de um InputStream. Substitui qualquer schema
	 * anterior associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param inputStream fluxo contendo a definição XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o conteúdo lido não for um XSD válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, InputStream inputStream) throws InvalidSchemaException;

	/**
	 * Registra explicitamente um schema associando-o ao {@link SchemaId} informado, a partir de um Reader. Substitui qualquer schema
	 * anterior associado ao mesmo ID.
	 *
	 * @param schemaId identificador lógico para o novo schema.
	 * @param reader leitor contendo a definição XSD.
	 * @return a instância compilada de {@link XmlSchema}.
	 * @throws InvalidSchemaException se o conteúdo lido não for um XSD válido.
	 */
	XmlSchema defineSchema(SchemaId schemaId, Reader reader) throws InvalidSchemaException;


	/* -------- REMOÇÃO DE SCHEMAS -------- */

	/**
	 * Remove do cache o schema associado ao {@link SchemaId} fornecido.
	 *
	 * @param schemaId identificador do schema a ser removido.
	 * @return {@code true} se o schema existia e foi removido, {@code false} caso contrário.
	 */
	boolean removeSchema(SchemaId schemaId);

	/**
	 * Remove todos os schemas armazenados no cache interno.
	 * <p>
	 * Atenção: Isso forçará a recompilação de schemas em requisições futuras.
	 */
	void clearSchemas();
}