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

import javax.xml.transform.sax.SAXSource;

import org.apache.http.Consts;
import org.apache.http.client.utils.URIBuilder;

import com.alibaba.fastjson.JSONObject;
import com.wudaosoft.net.utils.XmlReader;

/** 
 * @author Changsoul Wu
 * 
 */
public class Test {
	
	private HostConfig config = HostCofingBuilder.create("http://58.248.169.117:8086").build();
	
	private Request request = Request.createDefault(config);

	public void test() throws Exception {
		
		JSONObject obj = request.get("/mobile/list.json?id=1").json();
		
		System.out.println(obj);
	}
	
	public void testXml() throws Exception {
		
		SAXSource obj = request.get("/mobile/list.xml?id=1").sax();
		
		System.out.println(XmlReader.readFromSource(String.class, obj));
	}
	
	public void testSSL() throws Exception {
		
		String obj = request.get("https://www.baidu.com/").withAnyHost().execute();
		
		System.out.println(obj);
	}
	
	public static void main(String[] args) throws Exception {
		
		Test test = new Test();
		test.test();
		test.testSSL();
		//test.testXml();
		
		System.out.println(new URIBuilder("https://www.baidu.com/?pp=中文").setCharset(Consts.UTF_8).build());
		System.out.println(new URIBuilder("https://www.baidu.com/?pp=jkdafow8ewqr").setCharset(Consts.UTF_8).addParameter("ccs", "中文").addParameter("ooo", "英文").build());
	}
	
}
