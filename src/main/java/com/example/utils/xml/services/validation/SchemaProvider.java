package com.example.utils.xml.services.validation;

import javax.xml.validation.Schema;

/**
 * Interface for a service that provides compiled Schema objects.
 * This allows swapping between Spring-managed and default cache implementations.
 */
public interface SchemaProvider {
    /**
     * Retrieves a compiled Schema from the cache.
     *
     * @param schemaClasspathPath The classpath path to the .xsd file.
     * @return A thread-safe, compiled Schema object.
     */
    Schema get(String schemaClasspathPath);
}
