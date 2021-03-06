/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.data.elasticsearch;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.testsupport.testcontainers.ElasticsearchContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link ElasticsearchDataAutoConfiguration}.
 *
 * @author Phillip Webb
 * @author Artur Konczak
 * @author Brian Clozel
 */
@Testcontainers
public class ElasticsearchDataAutoConfigurationTests {

	@Container
	public static ElasticsearchContainer elasticsearch = new ElasticsearchContainer();

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ElasticsearchAutoConfiguration.class,
					RestClientAutoConfiguration.class,
					ElasticsearchDataAutoConfiguration.class));

	@Test
	public void defaultTransportBeansAreRegistered() {
		this.contextRunner
				.withPropertyValues(
						"spring.data.elasticsearch.cluster-nodes:localhost:"
								+ elasticsearch.getMappedTransportPort(),
						"spring.data.elasticsearch.cluster-name:docker-cluster")
				.run((context) -> assertThat(context)
						.hasSingleBean(ElasticsearchTemplate.class)
						.hasSingleBean(SimpleElasticsearchMappingContext.class)
						.hasSingleBean(ElasticsearchConverter.class));
	}

	@Test
	public void defaultTransportBeansNotRegisteredIfNoTransportClient() {
		this.contextRunner.run((context) -> assertThat(context)
				.doesNotHaveBean(ElasticsearchTemplate.class));
	}

	@Test
	public void defaultRestBeansRegistered() {
		this.contextRunner.run((context) -> assertThat(context)
				.hasSingleBean(ElasticsearchRestTemplate.class)
				.hasSingleBean(ElasticsearchConverter.class));
	}

	@Test
	public void customTransportTemplateShouldBeUsed() {
		this.contextRunner.withUserConfiguration(CustomTransportTemplate.class)
				.run((context) -> assertThat(context)
						.getBeanNames(ElasticsearchTemplate.class).hasSize(1)
						.contains("elasticsearchTemplate"));
	}

	@Test
	public void customRestTemplateShouldBeUsed() {
		this.contextRunner.withUserConfiguration(CustomRestTemplate.class)
				.run((context) -> assertThat(context)
						.getBeanNames(ElasticsearchRestTemplate.class).hasSize(1)
						.contains("elasticsearchTemplate"));
	}

	@Configuration
	static class CustomTransportTemplate {

		@Bean
		ElasticsearchTemplate elasticsearchTemplate() {
			return mock(ElasticsearchTemplate.class);
		}

	}

	@Configuration
	static class CustomRestTemplate {

		@Bean
		ElasticsearchRestTemplate elasticsearchTemplate() {
			return mock(ElasticsearchRestTemplate.class);
		}

	}

}
