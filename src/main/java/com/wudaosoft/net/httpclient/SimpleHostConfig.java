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

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;

/**
 * @author changsoul.wu
 *
 */
public class SimpleHostConfig implements HostConfig{
	
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
	
	
	public SimpleHostConfig(RequestConfig requestConfig) {
		super();
		this.requestConfig = requestConfig;
	}

	@Override
	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public boolean isMulticlient() {
		return multiclient;
	}

	public void setIsMulticlient(boolean isMulticlient) {
		this.multiclient = isMulticlient;
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String getReferer() {
		return referer;
	}

	public void setReferer(String referer) {
		this.referer = referer;
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	@Override
	public URL getCA() {
		return ca;
	}

	public void setCA(URL ca) {
		this.ca = ca;
	}

	@Override
	public char[] getCAPassword() {
		return caPassword;
	}

	public void setCaPassword(char[] caPassword) {
		this.caPassword = caPassword;
	}

	@Override
	public HttpHost getHost() {
		return httpHost;
	}

	public void setHost(HttpHost httpHost) {
		this.httpHost = httpHost;
	}

	@Override
	public String getHostUrl() {
		return hostUrl;
	}

	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	@Override
	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	public void setRequestConfig(RequestConfig requestConfig) {
		this.requestConfig = requestConfig;
	}

}
