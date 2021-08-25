package com.elk.config;

import java.io.Serializable;


/**
 * Config Object, loads the adls related configuration
 */
public class ADLSConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3359001007779525664L;

	private String accountFQDN;
	private String clientId;
	private String authTokenEndpoint;
	private String clientKey;
	private int maxPoolSize;
    private int retries;
    private int backoffMs;
	
	

	public String getAccountFQDN() {
		return accountFQDN;
	}

	public void setAccountFQDN(String accountFQDN) {
		this.accountFQDN = accountFQDN;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getAuthTokenEndpoint() {
		return authTokenEndpoint;
	}

	public void setAuthTokenEndpoint(String authTokenEndpoint) {
		this.authTokenEndpoint = authTokenEndpoint;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}


	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getBackoffMs() {
		return backoffMs;
	}

	public void setBackoffMs(int backoffMs) {
		this.backoffMs = backoffMs;
	}

	
	
}
