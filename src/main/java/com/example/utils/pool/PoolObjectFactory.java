package com.example.utils.pool;

/**
 * A factory interface for creating, validating, and destroying objects
 * managed by a {@link Pool}.
 *
 * Clients of the pooling library must provide a Spring Bean implementing
 * this interface for each pool they wish to create.
 *
 * @param <T> The type of object this factory creates.
 */
public interface PoolObjectFactory<T> {
    /**
     * Creates a new instance of T.
     *
     * @return A new object.
     * @throws Exception if creation fails.
     */
    T create() throws Exception;

    /**
     * Destroys an object, releasing any resources it holds.
     *
     * @param object The object to destroy.
     * @throws Exception if destruction fails.
     */
    void destroy(T object) throws Exception;

    /**
     * Resets an object to a clean state, preparing it for reuse.
     * This is called when an object is returned to the pool.
     *
     * @param object The object to reset.
     * @throws Exception if reset fails.
     */
    void reset(T object) throws Exception;

    /**
     * Validates an object before it is borrowed from the pool.
     *
     * @param object The object to validate.
     * @return true if the object is valid, false otherwise.
     */
    boolean validate(T object);
}