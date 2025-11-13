package com.example.utils.xml.services.providers;

import com.example.utils.xml.config.XmlConfig;
import com.example.utils.xml.services.factory.XmlFactory;
import com.example.utils.xml.services.parser.DocumentBuilderProvider;
import com.example.utils.xml.services.xpath.XPathEvaluatorProvider;
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
