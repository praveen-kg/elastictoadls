package com.elk.service.load;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import com.abnamro.ecs.datahub.fdu.config.Config;
import com.abnamro.ecs.datahub.fdu.service.util.ADLSUtil;
import com.microsoft.azure.datalake.store.ADLException;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.IfExists;

/**
 * This class uploads the elasticsearch results as json file to ADLS conatianer
 * 
 */
@Component("adlsWriter")
@DependsOn("config")
public class ADLSWriter implements IWriter {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Resource
	private Config config;

	@Resource
	private ADLSUtil adlsUtil;

		
	/**
	 * Uploads the local file to Azure Data Lake Store.
	 * @param documents   List of elasticsearch document
	 * @param destinationPath  where in ADLS document needs to be stored
	 */
	public void uploadDocuments(List<SearchHit> documents, String destinationPath) {
		logger.info("Initiating upload  to {}", destinationPath);
		try {
			if (CollectionUtils.isNotEmpty(documents)) {
				assert (adlsUtil.getClient() != null);
				ADLStoreClient client = adlsUtil.getClient();
				documents.parallelStream().forEach(doc -> {
					retryTemplate().execute(context -> {
						uploadToAzureDataLakeStore(doc, destinationPath,client);
						return null;
					});

				});
			}
		} catch (IOException e) {
			logger.error("event=error,Error during writing to ADLS= {}", e);
		}
	}

	/**
	 * Uploads the local file to Azure Data Lake Store.
	 * 
	 * @param doc    this is a elasticsearch Hit object
	 * @param destinationPath target adls path
	 * @throws IOException
	 */
	@Retryable
	private void uploadToAzureDataLakeStore(SearchHit doc, String destinationPath,ADLStoreClient client)  {
		
		String destination = getContainer(doc.getId(), destinationPath);
		String fileContent = doc.toString();
		String file = doc.getId();
		long startTime = System.currentTimeMillis();
		logger.debug("Initiating upload of {} to {}", file, destination);
		try (OutputStream stream = client.createFile(destination, IfExists.OVERWRITE)) {
			stream.write(fileContent.getBytes(StandardCharsets.UTF_8.name()));
			logger.info("event=fileUploaded, file={}, path={},timeSpent={} ", file, destination,
					(System.currentTimeMillis() - startTime));

		} catch (IOException  ex) {
			logger.error("event=, documentId={}, path={} ,exception={}", doc.getId(), destination, ex);
		}	
	}

	@Recover
	private void recover(ADLException t, String docId) {
		logger.error("event=retryFailed,msg=unable to upload the document= {}", docId);
	}

	private String getContainer(String fileName, String basePath) {
		if (!basePath.endsWith("/")) {
			basePath = basePath + "/";
		}
		String file = basePath + fileName + ".json";
		return file;
	}

	/**
	 * this method implements the retry logic for configured exception and number of times
	 * @return RetryTemplate
	 */
	@Bean
	public RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
		fixedBackOffPolicy.setBackOffPeriod(config.getAdls().getBackoffMs());
		retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

		Map<Class<? extends Throwable>, Boolean> retryExp = new HashMap<>();
		retryExp.put(ADLException.class, true);
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(config.getAdls().getRetries(), retryExp);
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.registerListener(new ADLSRetryListener());
		return retryTemplate;
	}
	
	/**
	 * Listener class  for every retry
	 *
	 */
	public class ADLSRetryListener extends RetryListenerSupport {

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            logger.warn("Retry attempt: {} for  upload of the document  ",context.getRetryCount());
            super.onError(context, callback, throwable);
        }
    }
}
