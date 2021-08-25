package com.elk.service.util;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.abnamro.ecs.datahub.fdu.config.Config;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.oauth2.AccessTokenProvider;
import com.microsoft.azure.datalake.store.oauth2.ClientCredsTokenProvider;

/**
 * ADLS client utility, it open ADLS connection.
 */
@Component
@DependsOn("config")
public class ADLSUtil {


	@Resource
	private Config adlsCfg;



	/**
	 * Create ADLStoreClient object
	 */
	@PostConstruct
	public ADLStoreClient getClient() throws IOException {

		AccessTokenProvider provider = new ClientCredsTokenProvider(adlsCfg.getAdls().getAuthTokenEndpoint(),
				adlsCfg.getAdls().getClientId(), adlsCfg.getAdls().getClientKey());
		return ADLStoreClient.createClient(adlsCfg.getAdls().getAccountFQDN(), provider);
		
	}

	
}