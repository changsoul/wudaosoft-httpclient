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

import java.nio.charset.Charset;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wudaosoft.net.httpclient.HostConfig;
import com.wudaosoft.net.httpclient.HostConfigBuilder;
import com.wudaosoft.net.httpclient.Request;

/**
 * @author changsoul.wu
 *
 */

@Configuration
@EnableConfigurationProperties(WudaoHttpclientProperties.class)
@ConditionalOnClass(Request.class)
public class WudaoHttpclientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(Request.class)
	@ConditionalOnProperty(prefix = "wudaosoft.httpclient", value = "host-url", matchIfMissing = false)
	public Request defaultRequest(WudaoHttpclientProperties properties) {

		HostConfig config = HostConfigBuilder.create(properties.getHostUrl())
				.setIsMulticlient(properties.isMulticlient())
				.setCharset(properties.getCharset() != null ? Charset.forName(properties.getCharset()) : null)
				.setConnectionRequestTimeout(properties.getConnectionRequestTimeout())
				.setConnectTimeout(properties.getConnectTimeout())
				.setSocketTimeout(properties.getSocketTimeout())
				.setPoolSize(properties.getPoolSize()).build();
		
		Request.Builder builder = Request.custom().setHostConfig(config);
		
		if(!properties.isKeepAlive())
			builder.withNoKeepAlive();
		
		if(properties.isTrustAll())
			builder.withTrustAll();
		
		return builder.build();
	}
	
	@Bean
	@ConditionalOnMissingBean(Request.class)
	@ConditionalOnProperty(prefix = "wudaosoft.httpclient", value = "host-url", havingValue = "false", matchIfMissing = true)
	public Request anyHostRequest(WudaoHttpclientProperties properties) {
		
		HostConfig config = HostConfigBuilder.create()
				.setIsMulticlient(properties.isMulticlient())
				.setCharset(properties.getCharset() != null ? Charset.forName(properties.getCharset()) : null)
				.setConnectionRequestTimeout(properties.getConnectionRequestTimeout())
				.setConnectTimeout(properties.getConnectTimeout())
				.setSocketTimeout(properties.getSocketTimeout())
				.setPoolSize(properties.getPoolSize()).build();
		
		Request.Builder builder = Request.custom().setHostConfig(config);
		
		if(!properties.isKeepAlive())
			builder.withNoKeepAlive();
		
		if(properties.isTrustAll())
			builder.withTrustAll();
		
		return builder.build();
	}

}
