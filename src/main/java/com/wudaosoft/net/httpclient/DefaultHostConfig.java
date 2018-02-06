/**
 *    Copyright 2009-2017 Wudao Software Studio(wudaosoft.com)
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        https://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.wudaosoft.net.httpclient;

import java.net.URL;
import java.util.Arrays;

import org.apache.http.HttpHost;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;

/**
 * @author Changsoul Wu
 * 
 */
public abstract class DefaultHostConfig implements HostConfig {

	private String userAgent = "Wudaosoft http tools/1.0";
	private RequestConfig defaultRequestConfig;
	
	public DefaultHostConfig() {
		defaultRequestConfig = org.apache.http.client.config.RequestConfig.custom()
				.setExpectContinueEnabled(false)
				// .setStaleConnectionCheckEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
				.setConnectionRequestTimeout(500).setConnectTimeout(10000).setSocketTimeout(10000)
				.build();
	}

	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getUserAgent()
	 */
	@Override
	public String getUserAgent() {
		return userAgent;
	}

	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getReferer()
	 */
	@Override
	public String getReferer() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getHost()
	 */
	@Override
	abstract public HttpHost getHost();
	
	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getHostUrl()
	 */
	@Override
	abstract public String getHostUrl();

	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getRequestConfig()
	 */
	@Override
	public RequestConfig getRequestConfig() {
		return defaultRequestConfig;
	}

	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getCA()
	 */
	@Override
	public URL getCA() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#getCAPassword()
	 */
	@Override
	public char[] getCAPassword() {
		return "nopassword".toCharArray();
	}
	
	/* (non-Javadoc)
	 * @see com.wudaosoft.traintickets.net.HostConfig#isMulticlient()
	 */
	@Override
	public boolean isMulticlient() {
		return false;
	}

	@Override
	public int getPoolSize() {
		
		return 150;
	}
	
	
}
