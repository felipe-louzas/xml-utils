package com.example.utils.xml.validation;

import com.example.utils.xml.factory.XmlProperties;
import com.example.xml.exceptions.XmlRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URL;

/**
 * A Spring-managed service that uses the @Cacheable abstraction
 * to cache compiled Schema objects.
 *
 * This bean is only activated if xml.utils.caching.enabled=true
 * AND a CacheManager bean is present.
 */
@Service
@ConditionalOnProperty(name = "xml.utils.caching.enabled", havingValue = "true")
@ConditionalOnBean(CacheManager.class)
@RequiredArgsConstructor
@Slf4j
public class SpringCacheSchemaProvider implements SchemaService {

    private final SchemaFactory schemaFactory;
    private final XmlProperties properties;

    public SpringCacheSchemaProvider(XmlProperties properties) {
        this.properties = properties;
        this.schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            log.debug("SchemaFactory secured against external access.");
        } catch (Exception e) {
            log.warn("Could not set security properties on SchemaFactory", e);
        }
        log.debug("Activating Spring-Cacheable SchemaService. Cache name: {}",
                properties.getCaching().getSchemaCacheName());
    }

    /**
     * Compiles and caches a Schema using Spring's cache abstraction.
     * The method body will only execute if the path is not already in the cache.
     */
    @Override
    @Cacheable(value = "${xml.utils.caching.schema-cache-name:xml-schema}")
    public Schema get(String schemaClasspathPath) {
        log.debug("Compiling and caching schema from path: {}", schemaClasspathPath);
        try {
            URL schemaUrl = getClass().getClassLoader().getResource(schemaClasspathPath);
            if (schemaUrl == null) {
                throw new XmlRuntimeException("Cannot find schema resource on classpath: " + schemaClasspathPath);
            }
            return schemaFactory.newSchema(new StreamSource(schemaUrl.toExternalForm()));
        } catch (SAXException e) {
            throw new XmlRuntimeException("Failed to parse schema: " + schemaClasspathPath, e);
        }
    }
}
