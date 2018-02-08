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

import org.apache.http.HttpHost;

import com.alibaba.fastjson.JSONObject;

/** 
 * @author Changsoul Wu
 * 
 */
public class Test {
	
	private HostConfig config = new DefaultHostConfig() {
		
		HttpHost host = HttpHost.create("http://58.248.169.117:8086");
		
		@Override
		public String getHostUrl() {
			return host.toURI();
		}
		
		@Override
		public HttpHost getHost() {
			return host;
		}
	};

	public void test() throws Exception {
		
		Request request = Request.createDefault(config);
		
		JSONObject obj = request.getJson("/mobile/list.json?id=1");
		
		System.out.println(obj);
	}
	
	public static void main(String[] args) throws Exception {
		
		new Test().test();
	}
	
}
