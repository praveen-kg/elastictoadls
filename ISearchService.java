package com.elk.service.extract;

/**
 *  interface for search
 */
public interface ISearchService {
	
	/**
	 * method to search elasticsearch based on provided query
	 * @param query elasticsearch query
	 * @param destinationPath adls storage path
	 */
	public void search(String query,String destinationPath);

}
