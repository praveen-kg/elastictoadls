package com.elk.service.extract;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.Resource;

import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.slice.SliceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.abnamro.ecs.datahub.fdu.config.Config;
import com.abnamro.ecs.datahub.fdu.service.load.ADLSWriter;
import com.abnamro.ecs.datahub.fdu.service.util.ElasticSearchClient;

/**
 *   Service class implemented search on elasticsearch
 */
@Service("elasticSearchService")
public class ElasticSearchServiceImpl implements ISearchService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource
	private ElasticSearchClient client;

	@Resource
	private Config config;

	@Resource
	private ADLSWriter adlsWriter;

	/**
	 * @param query elasticsearch query
	 * @param destinationPath adls storage path
	 */
	public void search(String query, String destinationPath) {
		logger.info("event=queryReceived, query ={}", query);
		SearchResponse searchResponse;
		try {
			final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10L));
			long uploadStartTime = System.currentTimeMillis();
			SearchRequest searchRequest = getSearchRequestBuilder(query);
			searchRequest.scroll(scroll);
			searchResponse = client.getClient().search(searchRequest, RequestOptions.DEFAULT);
			logger.info("event=queryProcessed, totalDocuments={}, queryTime={}", searchResponse.getHits().totalHits,searchResponse.getTook());
			String scrollId = searchResponse.getScrollId();
			SearchHit[] searchHits = searchResponse.getHits().getHits();
			if (searchHits == null || searchHits.length == 0) {
				logger.error(
						" ############################## No Documents Found for the query={}  #######################################",
						query);
			}
			while (searchHits != null && searchHits.length > 0) {
				adlsWriter.uploadDocuments(Arrays.asList(searchHits), destinationPath);
				SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
				//log statement
				scrollRequest.scroll(scroll);
				searchResponse = client.getClient().scroll(scrollRequest, RequestOptions.DEFAULT);
				scrollId = searchResponse.getScrollId();
				searchHits = searchResponse.getHits().getHits();

			}
			ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
			clearScrollRequest.addScrollId(scrollId);
			client.getClient().clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

			logger.info("event=uploadComplete,path= {}, timeSpent={}", destinationPath,
					(System.currentTimeMillis() - uploadStartTime));
			client.getClient().close();
		} catch (IOException e) {
			logger.error("event=processingError,exception={}", e);
		}

	}

	private SearchRequest getSearchRequestBuilder(String query) {
		SliceBuilder sliceBuilder = new SliceBuilder(config.getElasticsearch().getSliceCount(),
				config.getElasticsearch().getTotalSlice());
		SearchRequest searchRequest = new SearchRequest(config.getElasticsearch().getIndex());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().slice(sliceBuilder);
		searchSourceBuilder.query(QueryBuilders.wrapperQuery(query));
		searchSourceBuilder.size(config.getElasticsearch().getFetchSize());
		searchRequest.source(searchSourceBuilder);
		return searchRequest;
	}

}
