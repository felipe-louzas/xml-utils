package com.example.utils.xml.services.xpath.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import com.example.utils.xml.config.XmlConfig;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultNamespaceContext implements NamespaceContext {
	Map<String, String> prefixToUriMappings;
	Map<String, String> uriToPrefixMappings;

	public DefaultNamespaceContext(@NonNull Document document, @NonNull XmlConfig config) {
		prefixToUriMappings = new HashMap<>(config.getXpath().getDefaultNamespaceMappings());

		// Extract namespace declarations from the document element
		Optional.ofNullable(document.getDocumentElement())
			.map(Element::getAttributes)
			.map(attrs -> IntStream.range(0, attrs.getLength()).mapToObj(attrs::item))
			.orElseGet(Stream::empty)
			.filter(attr -> XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI()))
			.forEach(attr -> prefixToUriMappings.putIfAbsent(
				attr.getLocalName().equals(XMLConstants.XMLNS_ATTRIBUTE) ? XMLConstants.DEFAULT_NS_PREFIX : attr.getLocalName(),
				attr.getNodeValue()
			));

		uriToPrefixMappings = prefixToUriMappings.entrySet().stream()
			.collect(HashMap::new, (m, e) -> m.put(e.getValue(), e.getKey()), HashMap::putAll);
	}

	@Override
	public String getNamespaceURI(@NonNull String prefix) {
		return prefixToUriMappings.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
	}

	@Override
	public String getPrefix(@NonNull String namespaceURI) {
		return uriToPrefixMappings.get(namespaceURI);
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return prefixToUriMappings.keySet().iterator();
	}
}
