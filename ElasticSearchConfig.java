package com.elk.config;

import java.io.Serializable;


/**
 * Config Object, loads the Elasticsearch related configuration
 */


public class ElasticSearchConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String hosts;
	private String username;
	private String password;
	private String sslKeystore;
	private String sslTruststore;
	private String sslKeystorePassword;
	private String sslKeyPassword;
	private String trustStorePassword;
	private int port;
	private String index;
	private int fetchSize;
	private int sliceCount;
	private int totalSlice;
	private String protocol;
	
	
	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSslKeystore() {
		return sslKeystore;
	}

	public void setSslKeystore(String sslKeystore) {
		this.sslKeystore = sslKeystore;
	}

	public String getSslTruststore() {
		return sslTruststore;
	}

	public void setSslTruststore(String sslTruststore) {
		this.sslTruststore = sslTruststore;
	}

	public String getSslKeystorePassword() {
		return sslKeystorePassword;
	}

	public void setSslKeystorePassword(String sslKeystorePassword) {
		this.sslKeystorePassword = sslKeystorePassword;
	}

	public String getSslKeyPassword() {
		return sslKeyPassword;
	}

	public void setSslKeyPassword(String sslKeyPassword) {
		this.sslKeyPassword = sslKeyPassword;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public int getSliceCount() {
		return sliceCount;
	}

	public void setSliceCount(int sliceCount) {
		this.sliceCount = sliceCount;
	}

	public int getTotalSlice() {
		return totalSlice;
	}

	public void setTotalSlice(int totalSlice) {
		this.totalSlice = totalSlice;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
     
	
}
