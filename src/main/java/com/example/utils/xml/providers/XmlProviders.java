package com.example.utils.xml.providers;

import java.util.function.Supplier;

import com.example.utils.patterns.LazyInitSingleton;
import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.providers.factory.DefaultXmlFactory;
import com.example.utils.xml.providers.factory.XmlFactory;
import com.example.utils.xml.providers.loader.DefaultXmlLoader;
import com.example.utils.xml.providers.loader.XmlLoader;
import com.example.utils.xml.providers.parser.FactoryDocumentBuilderProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class XmlProviders {
	private static final LazyInitSingleton<XmlProviders> DEFAULT_PROVIDER = LazyInitSingleton.of(XmlProviders::buildDefaultProviders);

	XmlFactory xmlFactory;
	XmlLoader xmlLoader;

	public static XmlProviders getDefaultProviders() {
		return DEFAULT_PROVIDER.get();
	}

	public static void initializeProviders(Supplier<XmlProviders> initializer) {
		DEFAULT_PROVIDER.set(initializer);
	}

	private static XmlProviders buildDefaultProviders() {
		log.debug("Inicializando provedores padrão do XML Utils com configuração padrão...");
		return XmlProviders.of(XmlConfig.builder().build());
	}

	public static XmlProviders of(XmlConfig config) {
		val factory = new DefaultXmlFactory(config);
		val documentBuilderProvider = new FactoryDocumentBuilderProvider(factory);
		val xmlLoader = new DefaultXmlLoader(documentBuilderProvider);

		return new XmlProviders(factory, xmlLoader);
	}
}
