package com.example.utils.xml.context;

import com.example.utils.xml.formatter.XmlFormatter;
import com.example.utils.xml.validation.XmlValidator;
import com.example.utils.xml.xpath.XPathEvaluator;
import com.example.xml.config.XmlProperties;
import com.example.xml.internal.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.w3c.dom.Document;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Static Service Locator for {@link XmlServices}.
 *
 * This class provides a static accessor for the core XmlServices,
 * allowing the {@link Xml} class to be used with a simple API (e.g., {@code Xml.load(...)})
 * in both Spring-managed and non-managed environments.
 *
 * 1. In a Spring App: {@link XmlContextInitializer} will auto-load the managed bean.
 * 2. In a non-Spring App: User can call {@code XmlContext.init(...)} manually.
 * 3. If uninitialized: A default, secure {@link XmlServices} instance is lazy-loaded.
 */
@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class XmlContext {

	Document document;

	XPathEvaluator xPathEvaluator;
	XmlFormatter xmlFormatter;
	XmlValidator xmlValidator;


    private static final AtomicReference<XmlServices> servicesHolder = new AtomicReference<>();

    // Private constructor to prevent instantiation
    private XmlContext() {}

    /**
     * Manually initializes the XmlContext with a custom {@link XmlServices} instance.
     * This is intended for use in non-Spring environments.
     *
     * @param services The services instance to use globally.
     */
    public static void init(XmlContextProperties services) {
        if (servicesHolder.compareAndSet(null, services)) {
            log.info("XmlContext manually initialized.");
        } else {
            log.warn("XmlContext was already initialized. Ignoring subsequent init call.");
        }
    }

    /**
     * Gets the globally configured {@link XmlServices} instance.
     * If not initialized, this method will create and cache a default, secure
     * instance on the first call.
     *
     * @return The singleton XmlServices instance.
     */
    public static XmlServices get() {
        XmlServices services = servicesHolder.get();
        if (services == null) {
            // Not initialized. Use compareAndSet to lazy-load the default.
            val defaultServices = createDefaultServices();
            if (servicesHolder.compareAndSet(null, defaultServices)) {
                log.info("XmlContext not initialized by Spring or manually. " +
                         "Using secure defaults (this is normal for non-Spring use).");
                services = defaultServices;
            } else {
                // Another thread won the race, use its instance
                services = servicesHolder.get();
            }
        }
        return services;
    }

	/**
	 * Creates a default, secure {@link XmlServices} instance using default {@link XmlProperties}.
	 */
	private static XmlServices createDefaultServices() {
		return null;
	}

	public static void initDefaultContext(XmlContextProperties properties) {}



	public XPathEvaluator getXPathEvaluator() {}

	public XmlFormatter getXmlFormatter() {}

	public XmlValidator getXmlValidator() {}

	public Document getDocument() {}
}
