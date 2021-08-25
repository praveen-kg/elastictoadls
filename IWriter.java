package com.elk.service.load;

import java.util.List;

import org.elasticsearch.search.SearchHit;


/**
 *  Interface for the file upload api
 */
public interface IWriter {

	/**
	 *  method to upload list of elasticsearch docs to target
	 * @param documents docuements from elastic search
	 * @param destinationPath adls storage path
	 */
	public void uploadDocuments(List<SearchHit> documents, String destinationPath);
}
