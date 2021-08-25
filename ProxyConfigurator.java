package com.elk.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * public class for configuring proxy
 *
 */
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfigurator {

    private Map<String, String> proxyConfiguration = new HashMap<>();

    /**
     * Set the proxy if we have some entries in the configuration filte
     */
    @PostConstruct
    private void setProxy() {
        proxyConfiguration.forEach(System::setProperty);
    }

    /**
     * @return returns the proxy configuration
     */
    public Map<String, String> getProxyConfiguration() {
        return proxyConfiguration;
    }
}
