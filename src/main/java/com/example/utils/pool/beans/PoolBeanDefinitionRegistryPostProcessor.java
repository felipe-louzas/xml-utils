package com.example.utils.pool.beans;

import com.example.utils.pool.PoolObjectFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.ResolvableType;

/**
 * Registers one {@link PoolFactoryBean} definition per discovered {@link PoolObjectFactory} bean definition.
 * <p>
 * Runs during the bean definition phase — no PoolObjectFactory instances are created yet. The actual Pool beans are created lazily by
 * {@link PoolFactoryBean} once dependencies are available.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PoolBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private static final String FACTORY_SUFFIX = "Factory";

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		val beanNames = registry.getBeanDefinitionNames();

		for (String factoryBeanName : beanNames) {
			val definition = registry.getBeanDefinition(factoryBeanName);
			if (isPoolObjectFactoryBean(definition)) {
				registerPoolBeanDefinition(factoryBeanName, registry);
			}
		}
	}

	private boolean isPoolObjectFactoryBean(BeanDefinition def) {
		return ResolvableType.forClass(PoolObjectFactory.class)
			.isAssignableFrom(def.getResolvableType());
	}

	private void registerPoolBeanDefinition(String factoryBeanName, BeanDefinitionRegistry registry) {
		val poolBeanName = Strings.CS.removeEnd(factoryBeanName, FACTORY_SUFFIX);

		if (registry.containsBeanDefinition(poolBeanName)) {
			log.warn("Já existe um bean de pool registrado com o nome '{}', ignorando o registro do pool para a fábrica '{}'", poolBeanName, factoryBeanName);
			return;
		}

		val poolBeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(PoolFactoryBean.class)
			.addConstructorArgValue(poolBeanName)
			.addConstructorArgReference(factoryBeanName)
			.getBeanDefinition();

		registry.registerBeanDefinition(poolBeanName, poolBeanDefinition);
	}
}
