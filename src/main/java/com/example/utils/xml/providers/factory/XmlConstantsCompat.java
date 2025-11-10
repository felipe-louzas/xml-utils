package com.example.utils.xml.providers.factory;

/**
 * Constantes de compatibilidade para configuração de parsers XML de terceiros para evitar vulnerabilidades XXE
 */
public interface XmlConstantsCompat {
	String DISALLOW_DTD = "http://apache.org/xml/features/disallow-doctype-decl";
	String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	String LOAD_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	String LOAD_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
}
