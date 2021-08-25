package com.elk.schedular;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.abnamro.ecs.datahub.fdu.config.Config;
import com.abnamro.ecs.datahub.fdu.service.extract.ISearchService;
import com.abnamro.ecs.datahub.fdu.service.util.ADLSUtil;
import com.microsoft.azure.datalake.store.ADLFileInputStream;
import com.microsoft.azure.datalake.store.ADLStoreClient;
import com.microsoft.azure.datalake.store.DirectoryEntry;

/**
 * Cron job which triggers based on configuration, triggers the upload job -
 * from elasticsearch to ADLS
 */
@Component
public class ADLSUploadTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	private ISearchService elasticSearchService;

	@Resource
	private ADLSUtil adlsUtil;

	@Resource
	private Config adlsCfg;

	@Value("${export-definition-file-path}")
	private String exportDefFile;

	@Value("${config.app.jobexec.storepath}")
	private String jobExecStoreFile;

	private Date lastExecutedTime;

	private static String elasticsearchQuery = "elasticsearch_query";
	private static String storagePath = "adls_storage_path";
	private static String exportDefinitionFile = "export-definition-file.properties";
		
	/**
	 * Execution steps as below 1. Check for export-definition-file.properties, if
	 * not found log error message and abort the execution 2. If
	 * export-definition-file.properties, present the adls path, get the
	 * lastModifiedTime of the file 3. if lastModifiedTime is prior to current time
	 * stamp, execute the job, else abort.
	 */

	@Scheduled(cron = "${config.jobschedule.cron}")
	public void scheduleADLSJob() {
		logger.info("Job started - {}", new Date());
		executeUploadJob();
	}

	private void executeUploadJob() {
		Properties prop = new Properties();

		try {
			ADLStoreClient adlsClient = adlsUtil.getClient();
			logger.info("Downloading export definition-file from {}", exportDefFile);
			ADLFileInputStream fis = adlsClient.getReadStream(exportDefFile);
			if (null == fis) {
				logger.error("File ={} not found in the location {}", exportDefinitionFile,
						adlsCfg.getExportDefinitionFilePath());
				return;
			}
			DirectoryEntry entry = adlsClient.getDirectoryEntry(exportDefFile);
			Date fileModifiedTime = entry.lastModifiedTime;
			logger.info("LastExecuted Job Time= {},  export-definition-file.properties file modifiedTime  = {}",
					lastExecutedTime, fileModifiedTime);
			Date lastExecTime = getLastExecutedTime(lastExecutedTime);
			if (null == lastExecTime || fileModifiedTime.after(lastExecTime)) {
				lastExecutedTime = (null == lastExecTime) ? new Date() : fileModifiedTime;
				updateLastExecutedTime(lastExecutedTime);
				prop.load(fis);
				if (isNonEmpty(prop.getProperty(elasticsearchQuery)) && isNonEmpty(prop.getProperty(storagePath))) {
					elasticSearchService.search(prop.getProperty(elasticsearchQuery), prop.getProperty(storagePath));
				} else {
					logger.error(
							"event=configerror, Either ElasticSearch query or ADLS Storage Path is missing in export-definition-file.properties, Please check & configure");
				}

			} else {
				// reset the lastExecutedTime to current time
				lastExecutedTime = new Date();
				updateLastExecutedTime(lastExecutedTime);
				logger.info("No change to {} since {}, aborting the file upload job", exportDefinitionFile,
						fileModifiedTime);
			}

		} catch (IOException e) {

			logger.error("Error while executing the job {} ", e);
		}

	}

	private boolean isNonEmpty(String key) {
		return (null != key && key.length() > 0);
	}

	/**
	 * get the lastExecutedTime from file, to handle resart issues
	 */
	private Date getLastExecutedTime(Date lastExecutedTime) {
		if (null == lastExecutedTime) {
			StringBuilder fileContent = new StringBuilder();
			String file = FilenameUtils.normalize(jobExecStoreFile);
			try (Scanner sc = new Scanner(new File(file), StandardCharsets.UTF_8.name());) {
				sc.useDelimiter("\\n");
				while (sc.hasNext()) {
					fileContent.append(sc.next());
				}
				return (fileContent.length() == 0) ? null : DateUtils.parseDate(fileContent.toString());

			} catch (FileNotFoundException e) {
				logger.warn("event=error, exception={}", e);
				return null;
			}

		}
		return lastExecutedTime;
	}

	private void updateLastExecutedTime(Date lastExecutedTime) {
		if (null != lastExecutedTime) {
			String file = FilenameUtils.normalize(jobExecStoreFile);
			try {
				byte[] bytes = DateUtils.formatDate(lastExecutedTime).getBytes(StandardCharsets.UTF_8);
				try (OutputStream out = new FileOutputStream(file)) {
					out.write(bytes);
				}

			} catch (IOException e) {
				logger.warn("event=error, exception= {}", e);
			}
		}

	}
}
