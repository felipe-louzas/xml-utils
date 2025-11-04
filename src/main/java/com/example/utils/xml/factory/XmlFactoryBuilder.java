package com.example.utils.xml.factory;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathFactory;

import lombok.experimental.UtilityClass;

/**
 * Responsável por construir e configurar uma instância padrão de {@link XmlFactory}
 * com base nas propriedades definidas em {@link XmlContextProperties}.
 * <p>
 * Pode ser usada tanto em ambiente Spring quanto standalone.
 */
@UtilityClass
public class XmlFactoryBuilder {


    /**
     * Inicializa explicitamente a instância padrão com as propriedades fornecidas.
     * Usado em ambientes Spring.
     */
    public static XmlFactory initialize(XmlContextProperties props) {
        synchronized (XmlFactoryBuilder.class) {
            DEFAULT_FACTORIES = build(props);
            return DEFAULT_FACTORIES;
        }
    }

    /**
     * Constrói uma nova instância de {@link XmlFactory} a partir das propriedades fornecidas.
     */

}
