package com.example.utils.xml.services.factory;

import lombok.experimental.UtilityClass;

/**
 * Constantes de compatibilidade para configuração de parsers XML de terceiros para evitar vulnerabilidades XXE
 */
@UtilityClass
public class XmlConstantsCompat {
	public final String DISALLOW_DTD = "http://apache.org/xml/features/disallow-doctype-decl";
	public final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	public final String LOAD_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
	public final String LOAD_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
}
