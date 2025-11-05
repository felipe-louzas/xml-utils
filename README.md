
# **A Definitive Security Configuration for JAXP in Java 17**

## **1\. Analysis: The JAXP Security Model in Java 17**

### **1.1. The "Mess" of Java XML Security**

The Java API for XML Processing (JAXP) is a powerful but notoriously complex API. Its long history, spanning decades, has resulted in a "mess" of varied interfaces (DOM, SAX, StAX), multiple parser implementations, and a patchwork of security features added over time to address newly discovered vulnerabilities.1 This inconsistency in security controls across different parsers makes it exceptionally difficult to achieve a reliably secure configuration.1

This report provides a definitive, expert-level "cheat sheet" 2 for configuring JAXP in a secure-by-default manner on a modern Java 17 runtime. The configurations provided are designed to be explicit, robust, and resilient, addressing the historical inconsistencies of the API.

### **1.2. The Primary Threat Landscape**

This analysis assumes a zero-trust model: all XML input is untrusted. The primary goal is to mitigate a well-understood landscape of XML-based attacks.3

* **XML External Entity (XXE) Injection:** This is the most prominent and dangerous attack.4 It exploits XML parsers that resolve external resources, such as Document Type Definitions (DTDs) or external entities.4 A successful XXE attack can lead to:
    * **Information Disclosure:** The parser can be tricked into reading and exfiltrating sensitive local files, such as /etc/passwd, configuration files, or private keys.4
    * **Server-Side Request Forgery (SSRF):** The parser can be used as a proxy to make arbitrary network requests from the server's trusted IP address. This allows attackers to scan internal networks, interact with internal services, or attack other systems.3
    * **Denial of Service (DoS):** The parser may attempt to resolve external entities from slow or non-responsive remote systems, consuming all available sockets or threads.3
* **Denial of Service (DoS):**
    * **Exponential Entity Expansion:** Also known as the "Billion Laughs" attack, this vulnerability uses nested entity expansion to consume gigabytes of memory from a tiny XML payload, causing an $OutOfMemoryError$.3 The Java 17 runtime includes default processing limits that mitigate this attack.2
    * **Resource Exhaustion:** Attacks can also target other processing limits, such as creating an XML document with an excessive number of attributes 4 or exploiting unbounded maxOccurs declarations in XML schemas.5
* **Insecure Function Execution:** Specific to XSLT and XPath, these standards include constructs that can be configured to call external Java functions. An attacker who can control the XSLT stylesheet or XPath expression could potentially achieve Remote Code Execution (RCE) or perform SSRF-style attacks by calling malicious functions.4

### **1.3. The Configuration Precedence (Why This Report Focuses on the API)**

JAXP properties can be set in multiple ways, which have a strict, well-defined order of precedence.10 Understanding this hierarchy is critical to ensuring a security configuration is not accidentally overridden.

1. **Highest Precedence (API-Level):** Properties set programmatically on a specific factory or processor instance (e.g., using factory.setFeature(), factory.setAttribute(), or factory.setProperty()).4
2. **System Properties:** Properties set on the command line for the entire JVM (e.g., \-Djavax.xml.accessExternalDTD=all).10
3. **jaxp.properties File:** A global configuration file located in the JDK's conf directory.4
4. **Lowest Precedence (Defaults):** The built-in defaults, including those enabled by secure processing.10

This report will *exclusively* focus on the highest-precedence, API-level settings. This is the only method to create a truly robust, portable, and secure-by-default component. Relying on system properties or the jaxp.properties file is fragile; these settings affect the entire JVM and can be modified by other, unrelated code, or by the environment in which the application is deployed. API-level settings ensure that a *specific parser instance* is secure, regardless of the global state.10

## **2\. The Core Security Directives: FSP and External Access Properties (EAPs)**

Secure configuration relies on two main mechanisms: the Feature for Secure Processing (FSP) and the External Access Properties (EAPs).

### **2.1. XMLConstants.FEATURE\_SECURE\_PROCESSING (FSP)**

This feature, represented by the constant javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, is the central, API-defined directive for signaling the *intent* to process XML securely.13 When set to true, it instructs the JAXP processor to apply implementation-defined security restrictions.15 In the Java 17 JDK, this includes:

* Enabling DoS-protection limits (e.g., jdk.xml.entityExpansionLimit).4
* Restricting external resource access.18
* Disabling insecure features like XSLT and XPath extension functions.8

However, FSP is not a universal solution and its default behavior is dangerously inconsistent.1

* For DocumentBuilderFactory (DOM), SAXParserFactory (SAX), and SchemaFactory (Validation), the Java 17 documentation states that FSP is enabled **by default**.4
* Critically, for TransformerFactory (XSLT) and XPathFactory (XPath), FSP is **false by default**.4

This discrepancy exists because these factories have legitimate use cases for extension functions 4, and enabling FSP by default would have been a breaking change. The direct implication is that **TransformerFactory and XPathFactory are insecure-by-default in Java 17\.**

Due to this inconsistency, a "secure-by-default" posture mandates *never* relying on the default. FSP **must** be explicitly set to true on *every* factory instance.

### **2.2. The External Access Properties (EAPs)**

Introduced in JAXP 1.5 (part of Java 8, but back-ported to 7u40) 3, these properties provide explicit, granular control over external resource access.20 They are the modern, preferred mechanism for preventing XXE and SSRF attacks.

The three key properties are:

* javax.xml.XMLConstants.ACCESS\_EXTERNAL\_DTD (System property: javax.xml.accessExternalDTD) 14
* javax.xml.XMLConstants.ACCESS\_EXTERNAL\_SCHEMA (System property: javax.xml.accessExternalSchema) 14
* javax.xml.XMLConstants.ACCESS\_EXTERNAL\_STYLESHEET (System property: javax.xml.accessExternalStylesheet) 14

The value for these properties is a string of comma-separated protocols (e.g., "file,http").20 The default value in the JDK is "all", which permits access to all protocols and is dangerously insecure.4

**The secure-by-default value for all EAPs is an empty string (""), which denies all external resource access**.15

### **2.3. The FSP-EAP Relationship: The "Defense-in-Depth" Mandate**

In modern JDKs (Java 8 and later), setting setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true) has an implicit *side effect*: it automatically sets the EAPs (ACCESS\_EXTERNAL\_DTD and ACCESS\_EXTERNAL\_STYLESHEET) to "".18

It is tempting to rely on this side effect and only set FSP. **This is a critical mistake.**

1. **Implementation-Specific:** This side effect is an *implementation detail* of the JDK's internal parser, not a JAXP specification guarantee.
2. **The "Classpath Pollution" Vulnerability:** The JAXP lookup mechanism 10 allows third-party parsers (e.g., an old xercesImpl.jar) to be present on the application classpath.23 If JAXP selects this older parser, it may not support the JAXP 1.5 EAPs at all 24, or its FSP implementation may not have the EAP-setting side effect.

Therefore, a truly robust, secure-by-default configuration *cannot* rely on implicit side effects. It must be explicit. This "belt-and-suspenders" approach is the only one that defends against both insecure defaults and classpath pollution:

1. **Always** set FEATURE\_SECURE\_PROCESSING to true.
2. **Always** *manually* set all relevant EAPs to "".
3. **Always** *also* set the older, deprecated SAX/Xerces features as a final layer of defense.

## **3\. Secure Configuration Profile: DocumentBuilderFactory (DOM)**

### **3.1. FSP and EAP Configuration**

The DocumentBuilderFactory is used for creating DOM parsers.

* **Default FSP Status:** true.4

This factory demonstrates the first major API inconsistency. FSP is a boolean *feature* and is set with setFeature(String, boolean).26 The EAPs are string-valued *properties*, but DocumentBuilderFactory has no setProperty method. They must be set using setAttribute(String, Object).20

**Modern Configuration:**

Java

DocumentBuilderFactory dbf \= DocumentBuilderFactory.newInstance();

// 1\. Set FSP to true, even though it's the default.  
dbf.setFeature(javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

// 2\. Set EAPs using setAttribute() to deny all external access.  
dbf.setAttribute(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_DTD, "");  
dbf.setAttribute(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");

*Note: Setting ACCESS\_EXTERNAL\_SCHEMA is for defense-in-depth. While schema validation is not the primary purpose of this factory, the parser may still attempt to resolve xsi:schemaLocation attributes.*

### **3.2. Deprecated/Compatibility Configuration (Defense-in-Depth)**

This layer is *critical* for defending against old Xerces parsers (xercesImpl.jar) that may be on the classpath.24 These old parsers *do not* recognize the XMLConstants EAPs and *only* understand their own http://... features.

**Deprecated (but essential) Configuration:**

Java

// 3\. Set deprecated features for compatibility with older parsers.  
// These are the most important settings for XXE.  
dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);  
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);  
dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

// 4\. As an extra precaution, disable external DTDs for non-validating parsers.  
dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

3

### **3.3. Additional Security Controls**

Finally, disable other features that can be used for external entity inclusion.

Java

// 5\. Disable XInclude, another vector for external file inclusion.  
dbf.setXIncludeAware(false);

// 6\. Disable entity reference expansion (mitigates DoS and XXE).  
dbf.setExpandEntityReferences(false);

// 7\. Recommended for correct XML processing.  
dbf.setNamespaceAware(true);

2

### **3.4. Final Code (DocumentBuilderFactory)**

A secure-by-default factory method should apply all of these settings.

Java

import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.ParserConfigurationException;  
import javax.xml.XMLConstants;

public class SecureDomFactory {

    public static DocumentBuilderFactory createFactory() {  
        DocumentBuilderFactory dbf \= DocumentBuilderFactory.newInstance();  
          
        try {  
            // \--- Modern (JAXP 1.5+) Configuration \---

            // 1\. Set FSP. Must be set to true.  
            dbf.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

            // 2\. Set EAPs using setAttribute(). Set to empty string "" to deny.  
            dbf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");  
            dbf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");

            // \--- Deprecated/Compatibility (Defense-in-Depth) \---  
            // These are critical for defending against "classpath pollution"  
            // from older Xerces/JAXP 1.4 implementations.

            // 3\. Disallow DOCTYPE declarations entirely. (Most important)  
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);  
              
            // 4\. Disable external general and parameter entities.  
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);  
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            // 5\. Disable external DTD loading.  
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            // \--- Additional Hardening \---

            // 6\. Disable XInclude processing.  
            dbf.setXIncludeAware(false);

            // 7\. Disable entity reference expansion.  
            dbf.setExpandEntityReferences(false);  
              
            // 8\. Set namespace awareness (recommended).  
            dbf.setNamespaceAware(true);

        } catch (ParserConfigurationException e) {  
            // This happens if a feature is not supported.  
            // Log or throw a runtime exception.  
            throw new IllegalStateException("Failed to configure secure DocumentBuilderFactory", e);  
        }

        return dbf;  
    }  
}

## **4\. Secure Configuration Profile: SAXParserFactory (SAX)**

### **4.1. FSP and EAP Configuration**

The SAXParserFactory is used for creating event-based SAX parsers.

* **Default FSP Status:** true.4

This factory exposes the second major API inconsistency. The SAXParserFactory API has a setFeature method, but it **lacks both setAttribute and setProperty**.34

This means **the JAXP 1.5 EAPs (ACCESS\_EXTERNAL\_DTD, etc.) cannot be set at the factory level.** While they *can* be set on the SAXParser or XMLReader object *after* it is created 6, this is a brittle, two-step process that is easily forgotten.

### **4.2. Deprecated/Compatibility Configuration (Primary Defense)**

Because the modern EAPs cannot be set on the factory, the deprecated http://... features *become the primary security controls* for SAXParserFactory. Fortunately, they are all supported via setFeature.

**Secure Configuration (Deprecated but Essential):**

Java

SAXParserFactory spf \= SAXParserFactory.newInstance();

// 1\. Set FSP to true, even though it's the default.  
spf.setFeature(javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

// 2\. Set deprecated features. This is the PRIMARY defense for SAXParserFactory.  
spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);  
spf.setFeature("http://xml.org/sax/features/external-general-entities", false);  
spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

3

### **4.3. Additional Security Controls**

Java

// 3\. Disable XInclude processing.  
spf.setXIncludeAware(false);

// 4\. Recommended for correct XML processing.  
spf.setNamespaceAware(true);

28

### **4.4. Final Code (SAXParserFactory)**

Java

import javax.xml.parsers.SAXParserFactory;  
import javax.xml.parsers.ParserConfigurationException;  
import org.xml.sax.SAXNotRecognizedException;  
import org.xml.sax.SAXNotSupportedException;  
import javax.xml.XMLConstants;

public class SecureSaxFactory {

    public static SAXParserFactory createFactory() {  
        SAXParserFactory spf \= SAXParserFactory.newInstance();

        try {  
            // 1\. Set FSP. Must be set to true.  
            spf.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

            // \--- Primary Defense for SAX \---  
            // SAXParserFactory lacks setAttribute/setProperty, so  
            // the EAPs (ACCESS\_EXTERNAL\_\*) cannot be set at the factory level.  
            // Therefore, the deprecated features are the main defense.

            // 2\. Disallow DOCTYPE declarations entirely. (Most important)  
            spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 3\. Disable external general and parameter entities.  
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);  
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            // \--- Additional Hardening \---

            // 4\. Disable XInclude processing.  
            spf.setXIncludeAware(false);  
              
            // 5\. Set namespace awareness (recommended).  
            spf.setNamespaceAware(true);

        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {  
            // This happens if a feature is not supported.  
            throw new IllegalStateException("Failed to configure secure SAXParserFactory", e);  
        }  
          
        return spf;  
    }  
}

## **5\. Secure Configuration Profile: XMLInputFactory (StAX)**

### **5.1. The StAX API Mismatch**

The Streaming API for XML (StAX), part of the javax.xml.stream package, is the "odd one out."

* Its factory, XMLInputFactory, does not recognize setFeature or setAttribute.37
* It uses its own API, setProperty(String, Object).3
* It does *not* recognize FEATURE\_SECURE\_PROCESSING. Any attempt to set FSP will fail.38

All security knowledge from DOM and SAX is invalid for StAX. This factory has its own unique, non-obvious property strings for security.

### **5.2. Secure Configuration (StAX-Specific)**

The most effective control is to disable DTD support entirely.

**Secure Configuration:**

Java

XMLInputFactory xif \= XMLInputFactory.newInstance();

// 1\. Primary Control: Disable DTDs entirely.  
// This is the most effective way to prevent XXE in StAX.  
xif.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT\_DTD, false);

// 2\. Defense-in-Depth: Explicitly disable external entity support.  
xif.setProperty(javax.xml.stream.XMLInputFactory.IS\_SUPPORTING\_EXTERNAL\_ENTITIES, false);

// 3\. Defense-in-Depth: Set the JAXP 1.5 EAP.  
// This may not be supported by all third-party StAX parsers \[40\],  
// but should be set for the default JDK provider.  
// SUPPORT\_DTD=false is the more reliable control.  
xif.setProperty(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

3

### **5.3. Final Code (XMLInputFactory)**

Java

import javax.xml.stream.XMLInputFactory;  
import javax.xml.XMLConstants;

public class SecureStaxFactory {

    public static XMLInputFactory createFactory() {  
        XMLInputFactory xif \= XMLInputFactory.newInstance();

        // StAX (XMLInputFactory) uses a completely different API.  
        // It does NOT support FEATURE\_SECURE\_PROCESSING.  
        // All configuration is done via setProperty().

        try {  
            // 1\. Primary Control: Disable DTD support entirely.  
            // This is the most effective and direct way to stop XXE.  
            xif.setProperty(XMLInputFactory.SUPPORT\_DTD, false);

            // 2\. Defense-in-Depth: Explicitly disable external entities.  
            xif.setProperty(XMLInputFactory.IS\_SUPPORTING\_EXTERNAL\_ENTITIES, false);

            // 3\. Defense-in-Depth: Set the JAXP 1.5 EAP property.  
            // This is a "belt-and-suspenders" approach.  
            xif.setProperty(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

        } catch (IllegalArgumentException e) {  
            // This can happen if a property is not supported.  
            // Log or throw.  
            throw new IllegalStateException("Failed to configure secure XMLInputFactory", e);  
        }

        return xif;  
    }  
}

## **6\. Secure Configuration Profile: TransformerFactory (XSLT)**

### **6.1. The Insecure-by-Default Warning**

**CRITICAL WARNING:** The TransformerFactory is **insecure-by-default** in Java 17\.

* **Default FSP Status:** **false**.4

This is by design, to allow XSLT extension functions and constructs like xsl:import and the document() function. These features, however, are a significant security risk, enabling SSRF, file access, and potential RCE.4 **This factory must always be manually secured.**

### **6.2. FSP and EAP Configuration**

The TransformerFactory API is similar to DocumentBuilderFactory:

* FSP is set with setFeature(String, boolean).16
* EAPs are set with setAttribute(String, Object).27

**Secure Configuration:**

Java

TransformerFactory tf \= TransformerFactory.newInstance();

// 1\. MANDATORY: Set FSP to true. This is the \*only\* way to disable  
// insecure external functions. This factory is insecure by default.  
tf.setFeature(javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

// 2\. Set EAPs using setAttribute() to deny all external access.  
tf.setAttribute(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

// 3\. CRITICAL: This EAP disables xsl:import, xsl:include, and document(),  
// which are common vectors for SSRF in XSLT.  
tf.setAttribute(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_STYLESHEET, "");

2

### **6.3. The ACCESS\_EXTERNAL\_SCHEMA Trap**

Following the pattern from DocumentBuilderFactory, a developer might attempt to set all three EAPs. **Do not do this.**

* Attempting to set tf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, ""); will result in a java.lang.IllegalArgumentException.48
* The TransformerFactory is not responsible for schema validation and does not recognize this property. This is a design "gotcha" that breaks the configuration pattern.

### **6.4. Final Code (TransformerFactory)**

Java

import javax.xml.transform.TransformerFactory;  
import javax.xml.transform.TransformerConfigurationException;  
import javax.xml.XMLConstants;

public class SecureTransformerFactory {

    public static TransformerFactory createFactory() {  
        TransformerFactory tf \= TransformerFactory.newInstance();

        try {  
            // \--- CRITICAL: This factory is INSECURE BY DEFAULT \---  
            // FSP is 'false' by default, allowing external functions.

            // 1\. MANDATORY: Set FSP to true to disable external functions (RCE/SSRF).  
            tf.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

            // 2\. Set EAPs using setAttribute() to deny external DTDs (XXE).  
            tf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

            // 3\. Set EAP to deny external stylesheets (xsl:import, document(), etc).  
            // This is a critical SSRF mitigation.  
            tf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_STYLESHEET, "");

            // 4\. DO NOT SET ACCESS\_EXTERNAL\_SCHEMA.  
            // tf.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");  
            // This will throw a java.lang.IllegalArgumentException.\[48\]

        } catch (TransformerConfigurationException e) {  
            // This happens if a feature is not supported.  
            throw new IllegalStateException("Failed to configure secure TransformerFactory", e);  
        }

        return tf;  
    }  
}

## **7\. Secure Configuration Profile: SchemaFactory (Validation)**

### **7.1. FSP and EAP Configuration**

The SchemaFactory is used to compile XML Schemas (XSDs).

* **Default FSP Status:** true.4

This factory exposes the *third* distinct API pattern for configuration.1

* FSP is set with setFeature(String, boolean).2
* EAPs are set with setProperty(String, Object).3

This inconsistent use of setProperty (for EAPs) versus setAttribute (on DOM/XSLT factories) is a common source of developer error.

**Secure Configuration:**

Java

SchemaFactory sf \= SchemaFactory.newInstance(XMLConstants.W3C\_XML\_SCHEMA\_NS\_URI);

// 1\. Set FSP to true, even though it's the default.  
sf.setFeature(javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

// 2\. Set EAPs using setProperty() to deny all external access.  
// This prevents XXE from DTDs \*within\* the schema file.  
sf.setProperty(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

// 3\. CRITICAL: This EAP disables schema \<import\> and \<include\>  
// from resolving to external network locations.  
sf.setProperty(javax.xml.XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");

2

### **7.2. Deprecated/Compatibility Configuration (Defense-in-Depth)**

As with other factories, setting the deprecated disallow-doctype-decl feature provides a robust defense against older parsers.

**Deprecated (but essential) Configuration:**

Java

// 4\. Set deprecated feature for compatibility with older parsers.  
sf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

52

### **7.3. Final Code (SchemaFactory)**

Java

import javax.xml.validation.SchemaFactory;  
import org.xml.sax.SAXNotRecognizedException;  
import org.xml.sax.SAXNotSupportedException;  
import javax.xml.XMLConstants;

public class SecureSchemaFactory {

    public static SchemaFactory createFactory() {  
        SchemaFactory sf \= SchemaFactory.newInstance(XMLConstants.W3C\_XML\_SCHEMA\_NS\_URI);

        try {  
            // \--- Modern (JAXP 1.5+) Configuration \---

            // 1\. Set FSP. Must be set to true (even though default).  
            sf.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

            // 2\. Set EAPs using setProperty(). Set to empty string "" to deny.  
            // Denies DTDs \*within\* the XSD.  
            sf.setProperty(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");

            // 3\. CRITICAL: Denies external schema resolution via \<import\> or \<include\>.  
            sf.setProperty(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");

            // \--- Deprecated/Compatibility (Defense-in-Depth) \---

            // 4\. Disallow DOCTYPE declarations entirely.  
            sf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {  
            // This happens if a feature or property is not supported.  
            throw new IllegalStateException("Failed to configure secure SchemaFactory", e);  
        }

        return sf;  
    }  
}

## **8\. Secure Configuration Profile: XPathFactory (XPath)**

### **8.1. The Insecure-by-Default Warning**

**CRITICAL WARNING:** Like TransformerFactory, the XPathFactory is **insecure-by-default** in Java 17\.

* **Default FSP Status:** **false**.4

This is to allow XPath extension functions, which are a security risk.9 **This factory must always be manually secured.**

### **8.2. FSP Configuration**

The XPathFactory API is simple: it only supports setFeature.

* Setting setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true) is the *only* security control available on the factory.
* Its sole purpose is to disable external functions. When true, any reference to an external function will immediately throw an XPathFunctionException, and the XPathFunctionResolver will not be called.9

**Secure Configuration:**

Java

XPathFactory xpf \= XPathFactory.newInstance();

// 1\. MANDATORY: Set FSP to true. This is the \*only\* way to disable  
// insecure external functions. This factory is insecure by default.  
xpf.setFeature(javax.xml.XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

4

### **8.3. The Contextual Security Trap: Necessary but Not Sufficient**

Securing the XPathFactory is **necessary but not sufficient.**

An XPathFactory *evaluates* an XPath expression against an *existing* data model, typically a DOM Document object.3 It does *not* parse the XML file itself.

If an attacker supplies malicious XML (e.g., with an XXE payload), the vulnerability is exploited by the DocumentBuilderFactory when the XML is *parsed*. By the time the XPathFactory is used to query the resulting Document, the XXE attack (e.g., data exfiltration) has **already occurred.**

A common vulnerability pattern is to use a securely configured XPathFactory on a Document created by an *insecure, unconfigured* DocumentBuilderFactory.50

**Therefore, any use of XPathFactory *must* be paired with a Document object that was parsed using the secure DocumentBuilderFactory configuration from Section 3\.**

### **8.4. Final Code (XPathFactory)**

Java

import javax.xml.xpath.XPathFactory;  
import javax.xml.xpath.XPathFactoryConfigurationException;  
import javax.xml.XMLConstants;

public class SecureXpathFactory {

    /\*\*  
     \* Creates a secure XPathFactory.  
     \* WARNING: This only secures the XPath \*evaluation\*. The Document  
     \* it runs against MUST have been parsed by a secure DocumentBuilderFactory.  
     \*/  
    public static XPathFactory createFactory() {  
        XPathFactory xpf \= XPathFactory.newInstance();

        try {  
            // \--- CRITICAL: This factory is INSECURE BY DEFAULT \---  
            // FSP is 'false' by default, allowing external functions.

            // 1\. MANDATORY: Set FSP to true to disable external functions.  
            xpf.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);

        } catch (XPathFactoryConfigurationException e) {  
            // This happens if a feature is not supported.  
            throw new IllegalStateException("Failed to configure secure XPathFactory", e);  
        }

        return xpf;  
    }  
}

## **9\. Java 17 DoS Protection and Deprecated Property Reference**

### **9.1. Modern DoS Protection (The jdk.xml.\* Properties)**

The FEATURE\_SECURE\_PROCESSING (FSP) flag acts as a master switch for a set of modern, JDK-specific processing limits. When FSP is true, the Java 17 runtime enables the following default limits to mitigate DoS attacks 4:

* **jdk.xml.entityExpansionLimit**: 64000
    * Mitigates "Billion Laughs" attacks by limiting the total number of entity expansions.
* **jdk.xml.elementAttributeLimit**: 10000
    * Mitigates attacks that use an excessive number of attributes on a single element.
* **jdk.xml.maxOccurLimit**: 5000
    * Mitigates DoS from schema validation where maxOccurs is exploited.5
* **jdk.xml.xpathExprGrpLimit**: 10 58
    * Limits the number of groups in an XPath expression.
* **jdk.xml.xpathExprOpLimit**: 100 58
    * Limits the number of operators in an XPath expression.
* **jdk.xml.xpathTotalOpLimit**: 10000 58
    * Limits the total number of XPath operators in an XSLT stylesheet.

These limits can be overridden via System.setProperty() (e.g., setting them to 0 or \-1 to disable the limit) 59, but doing so is highly discouraged and re-opens DoS vulnerabilities. The secure-by-default configuration relies on FSP being true to enforce these safe defaults.

### **9.2. Deprecated Properties (The "Compatibility Shield")**

The query specifically asks for deprecated properties. They fall into two categories:

1\. Deprecated System Properties (Do Not Use):  
These properties have been superseded by the jdk.xml.\* properties listed above.12

* entityExpansionLimit (Use jdk.xml.entityExpansionLimit)
* elementAttributeLimit (Use jdk.xml.elementAttributeLimit)
* maxOccurLimit (Use jdk.xml.maxOccurLimit)

**Recommendation:** Do not set these. Rely on setting FEATURE\_SECURE\_PROCESSING to true, which enables the modern, default-limited jdk.xml.\* properties.

2\. Deprecated Feature Flags (Must Use):  
These are the Xerces/SAX-specific features.

* http://apache.org/xml/features/disallow-doctype-decl 60
* http://xml.org/sax/features/external-general-entities 3
* http://xml.org/sax/features/external-parameter-entities 3
* http://apache.org/xml/features/nonvalidating/load-external-dtd 3

**Recommendation:** These **MUST** be set on DocumentBuilderFactory, SAXParserFactory, and SchemaFactory as detailed in Sections 3, 4, and 7\.

**Rationale (The "Classpath Pollution" Defense):** This is the "defense-in-depth" posture. The JAXP lookup mechanism 10 allows a third-party parser (like an old xercesImpl.jar) to be on the application's classpath. If present, JAXP may select it instead of the JDK's default, secure parser. This old parser *does not support* the JAXP 1.5 EAPs (XMLConstants.ACCESS\_EXTERNAL\_DTD, etc.).24 It *only* understands its own, older http://... features.

Setting *only* the modern EAPs will leave the application vulnerable if this "classpath pollution" occurs. Setting *both* the modern EAPs and the deprecated features ensures the application is secure regardless of which parser JAXP selects.

## **10\. Definitive Configuration Summary: The JAXP Security Matrix**

This matrix summarizes the severe inconsistencies in the JAXP API and provides a single, actionable reference for secure configuration.

| JAXP Factory | Default FSP (Java 17\) | API Methods for Security | Secure-by-Default Configuration (Code Snippets) |
| :---- | :---- | :---- | :---- |
| **DocumentBuilderFactory** | **true** 4 | setFeature()setAttribute() | factory.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);factory.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");factory.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);factory.setFeature("http://xml.org/sax/features/external-general-entities", false);factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);factory.setXIncludeAware(false);factory.setExpandEntityReferences(false); |
| **SAXParserFactory** | **true** 4 | setFeature() only(Lacks setAttribute / setProperty) | factory.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);factory.setFeature("http://xml.org/sax/features/external-general-entities", false);factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);factory.setXIncludeAware(false); |
| **XMLInputFactory** (StAX) | N/A (FSP not supported) | setProperty() only | factory.setProperty(XMLInputFactory.SUPPORT\_DTD, false);factory.setProperty(XMLInputFactory.IS\_SUPPORTING\_EXTERNAL\_ENTITIES, false);factory.setProperty(XMLConstants.ACCESS\_EXTERNAL\_DTD, ""); |
| **TransformerFactory** (XSLT) | **false (WARNING)** 4 | setFeature()setAttribute() | factory.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);factory.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");factory.setAttribute(XMLConstants.ACCESS\_EXTERNAL\_STYLESHEET, "");*(Do NOT set ACCESS\_EXTERNAL\_SCHEMA)* |
| **SchemaFactory** (Validation) | **true** 4 | setFeature()setProperty() | factory.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);factory.setProperty(XMLConstants.ACCESS\_EXTERNAL\_DTD, "");factory.setProperty(XMLConstants.ACCESS\_EXTERNAL\_SCHEMA, "");factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); |
| **XPathFactory** (XPath) | **false (WARNING)** 4 | setFeature() only | factory.setFeature(XMLConstants.FEATURE\_SECURE\_PROCESSING, true);*(Warning: Must also use a secure DocumentBuilderFactory to parse the XML first)* |

## **11\. Final Recommendations**

The complexity and inconsistency of the JAXP API 1 make secure configuration a significant challenge. A robust, "secure-by-default" posture in Java 17 requires adherence to the following principles:

1. **Trust Nothing.** Assume all XML input is malicious.
2. **Be Explicit.** Manually set all security controls (FSP, EAPs, and deprecated features) on *every* factory instance. Never rely on platform defaults 4 or implicit implementation side effects.18
3. **Assume an Insecure-by-Default API.** The fact that TransformerFactory and XPathFactory are insecure-by-default 4 proves that developers cannot assume a "safe" baseline.
4. **Employ Defense-in-Depth.** Always set *both* the modern JAXP 1.5 EAPs (XMLConstants.ACCESS\_...) *and* the deprecated SAX/Xerces features (http://...). This is the only way to defend against the JAXP lookup mechanism loading an insecure, older parser from the classpath.24
5. **Master the API Inconsistencies.** The method for setting EAPs is different across factories (e.g., setAttribute vs. setProperty). This must be known and correctly applied for each factory type, as summarized in the matrix above.

#### **Referências citadas**

1. XML Security in Java | Semgrep, acessado em novembro 5, 2025, [https://semgrep.dev/blog/2023/xml-security-in-java/](https://semgrep.dev/blog/2023/xml-security-in-java/)
2. Prevent XML External Entity Vulnerabilities for Java \- xxe \- Semgrep, acessado em novembro 5, 2025, [https://semgrep.dev/docs/cheat-sheets/java-xxe](https://semgrep.dev/docs/cheat-sheets/java-xxe)
3. XML External Entity Prevention \- OWASP Cheat Sheet Series, acessado em novembro 5, 2025, [https://cheatsheetseries.owasp.org/cheatsheets/XML\_External\_Entity\_Prevention\_Cheat\_Sheet.html](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html)
4. 12 Java API for XML Processing (JAXP) Security Guide \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/security/java-api-xml-processing-jaxp-security-guide.html](https://docs.oracle.com/en/java/javase/17/security/java-api-xml-processing-jaxp-security-guide.html)
5. 12 Java API for XML Processing (JAXP) Security Guide \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/11/security/java-api-xml-processing-jaxp-security-guide.html](https://docs.oracle.com/en/java/javase/11/security/java-api-xml-processing-jaxp-security-guide.html)
6. How to Configure Your Java XML Parsers to Prevent XXE Attacks | foojay, acessado em novembro 5, 2025, [https://foojay.io/today/how-to-configure-your-java-xml-parsers-to-prevent-xxe-attacks/](https://foojay.io/today/how-to-configure-your-java-xml-parsers-to-prevent-xxe-attacks/)
7. XSLT Security and Server Side Request Forgery, acessado em novembro 5, 2025, [https://blog.compass-security.com/2015/06/xslt-security-and-server-side-request-forgery/](https://blog.compass-security.com/2015/06/xslt-security-and-server-side-request-forgery/)
8. Java API for XML Processing (JAXP) Security Guide \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/24/security/java-api-xml-processing-jaxp-security-guide.html](https://docs.oracle.com/en/java/javase/24/security/java-api-xml-processing-jaxp-security-guide.html)
9. XPathFactory.SetFeature(String, Boolean) Method (Javax.Xml.Xpath) | Microsoft Learn, acessado em novembro 5, 2025, [https://learn.microsoft.com/en-us/dotnet/api/javax.xml.xpath.xpathfactory.setfeature?view=net-android-34.0](https://learn.microsoft.com/en-us/dotnet/api/javax.xml.xpath.xpathfactory.setfeature?view=net-android-34.0)
10. java.xml (Java SE 17 & JDK 17 \[ad-hoc build\]), acessado em novembro 5, 2025, [https://cr.openjdk.org/\~joehw/jdk17/8261673/spec\_02/java.xml/module-summary.html](https://cr.openjdk.org/~joehw/jdk17/8261673/spec_02/java.xml/module-summary.html)
11. java.xml (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/module-summary.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/module-summary.html)
12. Module java.xml, acessado em novembro 5, 2025, [https://download.java.net/java/early\_access/loom/docs/api/java.xml/module-summary.html](https://download.java.net/java/early_access/loom/docs/api/java.xml/module-summary.html)
13. Java API for XML Processing (JAXP) Security Guide \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/8/docs/technotes/guides/security/jaxp/jaxp.html](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jaxp/jaxp.html)
14. 12 Java API for XML Processing (JAXP) Security Guide \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/21/security/java-api-xml-processing-jaxp-security-guide.html](https://docs.oracle.com/en/java/javase/21/security/java-api-xml-processing-jaxp-security-guide.html)
15. XMLConstants (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/XMLConstants.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/XMLConstants.html)
16. TransformerFactory (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/transform/TransformerFactory.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/transform/TransformerFactory.html)
17. SchemaFactory.SetFeature(String, Boolean) Method (Javax.Xml.Validation), acessado em novembro 5, 2025, [https://learn.microsoft.com/en-us/dotnet/api/javax.xml.validation.schemafactory.setfeature?view=net-android-35.0](https://learn.microsoft.com/en-us/dotnet/api/javax.xml.validation.schemafactory.setfeature?view=net-android-35.0)
18. Does setting FEATURE\_SECURE\_PROCESSING in transformerFactory update other security features as well? \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/29924050/does-setting-feature-secure-processing-in-transformerfactory-update-other-securi](https://stackoverflow.com/questions/29924050/does-setting-feature-secure-processing-in-transformerfactory-update-other-securi)
19. XML Entity Expansion in Java | SecureFlag Security Knowledge Base, acessado em novembro 5, 2025, [https://knowledge-base.secureflag.com/vulnerabilities/xml\_injection/xml\_entity\_expansion\_java.html](https://knowledge-base.secureflag.com/vulnerabilities/xml_injection/xml_entity_expansion_java.html)
20. JEP 185: Restrict Fetching of External XML Resources \- OpenJDK, acessado em novembro 5, 2025, [https://openjdk.org/jeps/185](https://openjdk.org/jeps/185)
21. XMLConstants (Java SE 11 & JDK 11 ) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/11/docs/api/java.xml/javax/xml/XMLConstants.html](https://docs.oracle.com/en/java/javase/11/docs/api/java.xml/javax/xml/XMLConstants.html)
22. Property Settings in the JDK, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/tutorial/jaxp/properties/propSettings.html](https://docs.oracle.com/javase/tutorial/jaxp/properties/propSettings.html)
23. setXIncludeAware is not supported on JAXP implementation \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/78824632/setxincludeaware-is-not-supported-on-jaxp-implementation](https://stackoverflow.com/questions/78824632/setxincludeaware-is-not-supported-on-jaxp-implementation)
24. Java and Xerces: can't find property XMLConstants.ACCESS\_EXTERNAL\_DTD, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/53299280/java-and-xerces-cant-find-property-xmlconstants-access-external-dtd](https://stackoverflow.com/questions/53299280/java-and-xerces-cant-find-property-xmlconstants-access-external-dtd)
25. Remove Apache Xerces Dependencies \- Bloomreach Experience Manager (PaaS/Self-Hosted) \- The Fast and Flexible Headless CMS, acessado em novembro 5, 2025, [https://xmdocumentation.bloomreach.com/library/upgrade-minor-versions/remove-apache-xerces.html](https://xmdocumentation.bloomreach.com/library/upgrade-minor-versions/remove-apache-xerces.html)
26. DocumentBuilderFactory (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/parsers/DocumentBuilderFactory.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/parsers/DocumentBuilderFactory.html)
27. TransformerFactory (Java Platform SE 8 ) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/8/docs/api/javax/xml/transform/TransformerFactory.html](https://docs.oracle.com/javase/8/docs/api/javax/xml/transform/TransformerFactory.html)
28. how to fix 'Disable XML external entity (XXE) processing' vulnerabilities in java, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/56777287/how-to-fix-disable-xml-external-entity-xxe-processing-vulnerabilities-in-jav](https://stackoverflow.com/questions/56777287/how-to-fix-disable-xml-external-entity-xxe-processing-vulnerabilities-in-jav)
29. documentbuilderfactory-disallow-doctype-decl-missing | Semgrep, acessado em novembro 5, 2025, [https://semgrep.dev/r?q=java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing.documentbuilderfactory-disallow-doctype-decl-missing](https://semgrep.dev/r?q=java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing.documentbuilderfactory-disallow-doctype-decl-missing)
30. Features \- Apache Xerces, acessado em novembro 5, 2025, [https://xerces.apache.org/xerces2-j/features.html](https://xerces.apache.org/xerces2-j/features.html)
31. external-parameter-entities Feature | Microsoft Learn, acessado em novembro 5, 2025, [https://learn.microsoft.com/en-us/previous-versions/windows/desktop/ms761356(v=vs.85)](https://learn.microsoft.com/en-us/previous-versions/windows/desktop/ms761356\(v=vs.85\))
32. Features \- Apache Xerces, acessado em novembro 5, 2025, [https://xerces.apache.org/xerces-j/features.html](https://xerces.apache.org/xerces-j/features.html)
33. Question regarding Java upgrade from 11 to 17 and its effect on XML parsing using Java DOM document \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/79173407/question-regarding-java-upgrade-from-11-to-17-and-its-effect-on-xml-parsing-usin](https://stackoverflow.com/questions/79173407/question-regarding-java-upgrade-from-11-to-17-and-its-effect-on-xml-parsing-usin)
34. SAXParserFactory (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/parsers/SAXParserFactory.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/parsers/SAXParserFactory.html)
35. How to configure Java's SaxParserFactory to disable entity checking? \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/1995614/how-to-configure-javas-saxparserfactory-to-disable-entity-checking](https://stackoverflow.com/questions/1995614/how-to-configure-javas-saxparserfactory-to-disable-entity-checking)
36. Solve security issue parsing xml using SAX parser \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/10837706/solve-security-issue-parsing-xml-using-sax-parser](https://stackoverflow.com/questions/10837706/solve-security-issue-parsing-xml-using-sax-parser)
37. XMLInputFactory (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/stream/XMLInputFactory.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/stream/XMLInputFactory.html)
38. How to set FEATURE\_SECURE\_PROCESSING in XMLReaderFactory? \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/23237755/how-to-set-feature-secure-processing-in-xmlreaderfactory](https://stackoverflow.com/questions/23237755/how-to-set-feature-secure-processing-in-xmlreaderfactory)
39. WARNING: JAXP feature XMLConstants.FEATURE\_SECURE\_PROCESSING on Jersey2.x Client \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/18942307/warning-jaxp-feature-xmlconstants-feature-secure-processing-on-jersey2-x-client](https://stackoverflow.com/questions/18942307/warning-jaxp-feature-xmlconstants-feature-secure-processing-on-jersey2-x-client)
40. XMLInputFactory (Java Platform SE 8 ) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/XMLInputFactory.html](https://docs.oracle.com/javase/8/docs/api/javax/xml/stream/XMLInputFactory.html)
41. XML parsing vulnerable to XEE \- Datadog Docs, acessado em novembro 5, 2025, [https://docs.datadoghq.com/security/code\_security/static\_analysis/static\_analysis\_rules/java-security/xml-parsing-xee/](https://docs.datadoghq.com/security/code_security/static_analysis/static_analysis_rules/java-security/xml-parsing-xee/)
42. how to Prevent XML Entity Injection with JAXB \- java \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/58725043/how-to-prevent-xml-entity-injection-with-jaxb](https://stackoverflow.com/questions/58725043/how-to-prevent-xml-entity-injection-with-jaxb)
43. Java XXE vulnerability \- xml \- Information Security Stack Exchange, acessado em novembro 5, 2025, [https://security.stackexchange.com/questions/260956/java-xxe-vulnerability](https://security.stackexchange.com/questions/260956/java-xxe-vulnerability)
44. Prevent XXE Attack with JAXB \- java \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/12977299/prevent-xxe-attack-with-jaxb](https://stackoverflow.com/questions/12977299/prevent-xxe-attack-with-jaxb)
45. Transform Features \- Apache XML, acessado em novembro 5, 2025, [https://xml.apache.org/xalan-j/features.html](https://xml.apache.org/xalan-j/features.html)
46. TransformerFactory.newInstance(...) is vulnerable to XML and XSLT External Entity attacks \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/78127208/transformerfactory-newinstance-is-vulnerable-to-xml-and-xslt-external-entit](https://stackoverflow.com/questions/78127208/transformerfactory-newinstance-is-vulnerable-to-xml-and-xslt-external-entit)
47. java:S2755 has wrong solution for \*\*TransformerFactory \- Sonar Community, acessado em novembro 5, 2025, [https://community.sonarsource.com/t/java-s2755-has-wrong-solution-for-transformerfactory/115236](https://community.sonarsource.com/t/java-s2755-has-wrong-solution-for-transformerfactory/115236)
48. java.lang.IllegalArgumentException: Not supported: http://javax.xml.XMLConstants/property/accessExternalDTD \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/75815506/java-lang-illegalargumentexception-not-supported-http-javax-xml-xmlconstants](https://stackoverflow.com/questions/75815506/java-lang-illegalargumentexception-not-supported-http-javax-xml-xmlconstants)
49. XML parsers should not be vulnerable to XXE attacks \- Sonar Community, acessado em novembro 5, 2025, [https://community.sonarsource.com/t/xml-parsers-should-not-be-vulnerable-to-xxe-attacks/36424](https://community.sonarsource.com/t/xml-parsers-should-not-be-vulnerable-to-xxe-attacks/36424)
50. SchemaFactory (Java SE 17 & JDK 17\) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/validation/SchemaFactory.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/validation/SchemaFactory.html)
51. SchemaFactory Sonar Disable access to external entities in XML parsing \- how to fix?, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/75746364/schemafactory-sonar-disable-access-to-external-entities-in-xml-parsing-how-to](https://stackoverflow.com/questions/75746364/schemafactory-sonar-disable-access-to-external-entities-in-xml-parsing-how-to)
52. SchemaFactory (Java Platform SE 8 ) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/8/docs/api/javax/xml/validation/SchemaFactory.html](https://docs.oracle.com/javase/8/docs/api/javax/xml/validation/SchemaFactory.html)
53. False positive with XXE\_SCHEMA\_FACTORY (and possibly similar rules) when setting properties in separate method · Issue \#3008 \- GitHub, acessado em novembro 5, 2025, [https://github.com/spotbugs/spotbugs/issues/3008](https://github.com/spotbugs/spotbugs/issues/3008)
54. XPathFactory (XML Standard API) \- Apache Xerces, acessado em novembro 5, 2025, [https://xerces.apache.org/xerces2-j/javadocs/api/javax/xml/xpath/XPathFactory.html](https://xerces.apache.org/xerces2-j/javadocs/api/javax/xml/xpath/XPathFactory.html)
55. XPathFactory (Java Platform SE 8 ) \- Oracle Help Center, acessado em novembro 5, 2025, [https://docs.oracle.com/javase/8/docs/api/javax/xml/xpath/XPathFactory.html](https://docs.oracle.com/javase/8/docs/api/javax/xml/xpath/XPathFactory.html)
56. encapsulate org.w3c.dom.Document and javax.xml.xpath.XPath to parse a small xml document (needs more testing, not thread safe, ...), acessado em novembro 5, 2025, [https://gist.github.com/99ce9fb9b9f67bf61419](https://gist.github.com/99ce9fb9b9f67bf61419)
57. java \- limit set by 'FEATURE\_SECURE\_PROCESSING' \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/72401149/limit-set-by-feature-secure-processing](https://stackoverflow.com/questions/72401149/limit-set-by-feature-secure-processing)
58. XMLConstants.FEATURE\_SECURE\_PROCESSING cannot be disabled \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/72575400/xmlconstants-feature-secure-processing-cannot-be-disabled](https://stackoverflow.com/questions/72575400/xmlconstants-feature-secure-processing-cannot-be-disabled)
59. \[SAXParseException: DOCTYPE is disallowed when the feature "http://apache.org/xml/features/disallow-doctype-decl" set to true.\] \- Google Groups, acessado em novembro 5, 2025, [https://groups.google.com/g/play-framework/c/2mtqhoKLn4Q](https://groups.google.com/g/play-framework/c/2mtqhoKLn4Q)
60. Mule 4 DOCTYPE Disallowed When "http://apache.org/xml/features/disallow-doctype-decl" Set to True \- Salesforce Help, acessado em novembro 5, 2025, [https://help.salesforce.com/s/articleView?id=001123013\&language=en\_US\&type=1](https://help.salesforce.com/s/articleView?id=001123013&language=en_US&type=1)
61. Ignore DTD specification in scala \- xml \- Stack Overflow, acessado em novembro 5, 2025, [https://stackoverflow.com/questions/11315439/ignore-dtd-specification-in-scala](https://stackoverflow.com/questions/11315439/ignore-dtd-specification-in-scala)