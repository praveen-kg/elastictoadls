package com.elk.service.util;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.abnamro.ecs.datahub.fdu.config.Config;

/**
 *  Elasticsearch client utility, to create REST connection.
 */
@Service("elasticSearchClient")
public class ElasticSearchClient {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private Config config;

	private static final String COMMA = ",";
	
	/**
	  * create RestHighLevelClient 
	 */
   @Bean
	public RestHighLevelClient getClient()  {

		try {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
					config.getElasticsearch().getUsername(), config.getElasticsearch().getPassword()));

			String keyStore = FilenameUtils.normalize(config.getElasticsearch().getSslKeystore());
			String trustStore = FilenameUtils.normalize(config.getElasticsearch().getSslTruststore());

			SSLContext sslContext = SSLContexts.custom()
					.loadKeyMaterial(new File(keyStore),
							config.getElasticsearch().getSslKeystorePassword().toCharArray(),
							config.getElasticsearch().getSslKeyPassword().toCharArray())
					.loadTrustMaterial(new File(trustStore),
							config.getElasticsearch().getTrustStorePassword().toCharArray(),
							new TrustSelfSignedStrategy())
					.build();

			RestClientBuilder builder = getElasticsearchClient()
					.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
							.setDefaultCredentialsProvider(credentialsProvider).setSSLContext(sslContext));
			return new RestHighLevelClient(builder);
		} catch ( NoSuchAlgorithmException |  KeyManagementException | CertificateException| UnrecoverableKeyException | KeyStoreException | IOException e) {
			logger.error("Error while create REST Client - {}",e);
		}
		return null;

	}
 
	private RestClientBuilder getElasticsearchClient() {

		String strHosts = config.getElasticsearch().getHosts();
		Assert.hasText(strHosts, " At least one host must be set.");
		List<HttpHost> httpHosts = new ArrayList<>();
		for (String host : strHosts.split(COMMA)) {
			httpHosts.add(new HttpHost(host, config.getElasticsearch().getPort(), config.getElasticsearch().getProtocol()));
		}
		RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()]));
		return builder;
	}
}
