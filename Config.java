package com.elk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Config utility class, for all configuration loading
 */

@Configuration("config")
@ConfigurationProperties("config")
public class Config {

	@Value("${export-definition-file-path}")
	private String exportDefFile;

		
	private final ElasticSearchConfig elasticsearch = new ElasticSearchConfig();
	private final ADLSConfig adls = new ADLSConfig();

	public ElasticSearchConfig getElasticsearch() {
		return elasticsearch;
	}

	public ADLSConfig getAdls() {
		return adls;
	}
	
	public String getExportDefinitionFilePath() {
		return exportDefFile;
	}

	
}
