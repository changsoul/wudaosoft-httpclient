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
package com.wudaosoft.net.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author changsoul.wu
 *
 */
@ConfigurationProperties(prefix = "wudaosoft.httpclient")
public class WudaoHttpclientProperties {

	private String hostUrl;
	
	private String charset = "UTF-8";

	private int connectionRequestTimeout = 500;

	private int connectTimeout = 10000;

	private int socketTimeout = 10000;

	private int poolSize = 50;

	private boolean multiclient = false;

	private boolean keepAlive = true;

	private boolean trustAll = false;

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * @return the hostUrl
	 */
	public String getHostUrl() {
		return hostUrl;
	}

	/**
	 * @param hostUrl the hostUrl to set
	 */
	public void setHostUrl(String hostUrl) {
		this.hostUrl = hostUrl;
	}

	/**
	 * @return the connectionRequestTimeout
	 */
	public int getConnectionRequestTimeout() {
		return connectionRequestTimeout;
	}

	/**
	 * @param connectionRequestTimeout the connectionRequestTimeout to set
	 */
	public void setConnectionRequestTimeout(int connectionRequestTimeout) {
		this.connectionRequestTimeout = connectionRequestTimeout;
	}

	/**
	 * @return the connectTimeout
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * @param connectTimeout the connectTimeout to set
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * @return the socketTimeout
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * @param socketTimeout the socketTimeout to set
	 */
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	/**
	 * @return the poolSize
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @return the multiclient
	 */
	public boolean isMulticlient() {
		return multiclient;
	}

	/**
	 * @param multiclient the multiclient to set
	 */
	public void setMulticlient(boolean multiclient) {
		this.multiclient = multiclient;
	}

	/**
	 * @return the keepAlive
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * @param keepAlive the keepAlive to set
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	/**
	 * @return the trustAll
	 */
	public boolean isTrustAll() {
		return trustAll;
	}

	/**
	 * @param trustAll the trustAll to set
	 */
	public void setTrustAll(boolean trustAll) {
		this.trustAll = trustAll;
	}
	

}
