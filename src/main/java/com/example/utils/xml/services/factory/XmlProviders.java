package com.example.utils.xml.services.factory;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.document.parser.DocumentBuilderProvider;
import com.example.utils.xml.services.xpath.provider.XPathEvaluatorProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class XmlProviders {
	XmlConfig config;
	XmlFactory xmlFactory;
	DocumentBuilderProvider documentBuilderProvider;
	XPathEvaluatorProvider xPathEvaluatorProvider;
}
