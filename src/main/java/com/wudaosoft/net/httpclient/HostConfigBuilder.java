/**
 *    Copyright 2009-2018 Wudao Software Studio(wudaosoft.com)
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.wudaosoft.net.httpclient;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.util.Args;

/**
 * @author changsoul.wu
 *
 */
public class HostConfigBuilder {
	
	private int connectionRequestTimeout = 500;
	
	private int connectTimeout = 10000;
	
	private int socketTimeout = 10000;
	
	private int poolSize = 70;
	
	private boolean multiclient = false;
	
	private String userAgent = "Wudaosoft Http Tools/1.0";
	
	private String referer;
	
	private Charset charset;
	
	private URL ca;
	
	private char[] caPassword;
	
	private HttpHost httpHost;
	
	private String hostUrl;
	
	private RequestConfig requestConfig;
	
	HostConfigBuilder () {
		
	}
	
	/**
	 * @param connectionRequestTimeout the connectionRequestTimeout to set
	 */
	public HostConfigBuilder setConnectionRequestTimeout(int connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
		return this;
	}

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public HostConfigBuilder setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	/**
	 * @param socketTimeout the socketTimeout to set
	 */
	public HostConfigBuilder setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		return this;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	public HostConfigBuilder setPoolSize(int poolSize) {
		this.poolSize = poolSize;
		return this;
	}

	/**
	 * @param isMulticlient the isMulticlient to set
	 */
	public HostConfigBuilder setIsMulticlient(boolean isMulticlient) {
		this.multiclient = isMulticlient;
		return this;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public HostConfigBuilder setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	/**
	 * @param referer the referer to set
	 */
	public HostConfigBuilder setReferer(String referer) {
		this.referer = referer;
		return this;
	}

	/**
	 * @param charset the charset to set
	 */
	public HostConfigBuilder setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * @param ca the ca to set
	 */
	public HostConfigBuilder setCa(URL ca) {
		this.ca = ca;
		return this;
	}

	/**
	 * @param caPassword the caPassword to set
	 */
	public HostConfigBuilder setCaPassword(char[] caPassword) {
		this.caPassword = caPassword;
		return this;
	}

	/**
	 * @param httpHost the httpHost to set
	 */
	public HostConfigBuilder setHttpHost(HttpHost httpHost) {
		Args.notNull(httpHost, "httpHost");
		
		this.httpHost = httpHost;
		
		if (this.hostUrl == null) {
			this.hostUrl = httpHost.toURI();
		}
		return this;
	}

	/**
	 * @param hostUrl the hostUrl to set
	 */
	public HostConfigBuilder setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
		return this;
	}

	/**
	 * @param requestConfig the requestConfig to set
	 */
	public HostConfigBuilder setRequestConfig(RequestConfig requestConfig) {
		this.requestConfig = requestConfig;
		return this;
	}

	public static HostConfigBuilder create() {
		return new HostConfigBuilder();
	}
	
	public static HostConfigBuilder create(String hostUrl) {
		return create().setHttpHost(HttpHost.create(hostUrl));
	}
	
	public static HostConfigBuilder create(HttpHost httpHost) {
		return create().setHttpHost(httpHost);
	}
	
	public static HostConfig buildNoHost() {
		return create().build();
	}
	
	public HostConfig build() {
		
		requestConfig = RequestConfig.custom()
				.setExpectContinueEnabled(false)
				// .setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
				.setConnectionRequestTimeout(connectionRequestTimeout).setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout)
				.build();
		
		SimpleHostConfig hostCofing = new SimpleHostConfig(requestConfig);
		hostCofing.setCA(ca);
		hostCofing.setCaPassword(caPassword);
		hostCofing.setCharset(charset);
		hostCofing.setHost(httpHost);
		hostCofing.setHostUrl(hostUrl);
		hostCofing.setIsMulticlient(multiclient);
		hostCofing.setPoolSize(poolSize);
		hostCofing.setReferer(referer);
		hostCofing.setUserAgent(userAgent);
		
		return hostCofing;
	}

}
