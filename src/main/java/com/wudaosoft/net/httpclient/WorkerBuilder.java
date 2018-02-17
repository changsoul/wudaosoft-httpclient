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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.apache.http.client.protocol.HttpClientContext;

import com.alibaba.fastjson.JSONObject;
import com.wudaosoft.net.utils.XmlReader;
import com.wudaosoft.net.xml.XmlObject;

/**
 * @author changsoul.wu
 *
 */
public class WorkerBuilder {
	
	private String method;
    private String url;
    private HttpClientContext context;
    private Map<String, String> parameters;
    private String stringBody;
    private String fileFieldName = "upfile";
    private String filename;
    private File fileBody;
    private boolean isAjax = false;
    private boolean isAnyHost = false;
    
    private Request request;
    
    WorkerBuilder(Request request) {
    	super();
    	this.request = request;
    }
    
    WorkerBuilder(Request request, final String method, final String url) {
        this(request);
        this.method = method;
        this.url = url;
    }

	/**
	 * @param method the method to set
	 */
	public WorkerBuilder withMethod(String method) {
		this.method = method;
		return this;
	}

	/**
	 * @param url the url to set
	 */
	public WorkerBuilder withUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @param context the context to set
	 */
	public WorkerBuilder withContext(HttpClientContext context) {
		this.context = context;
		return this;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public WorkerBuilder withParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 * @param stringBody the stringBody to set
	 */
	public WorkerBuilder withStringBody(String stringBody) {
		this.stringBody = stringBody;
		return this;
	}

	/**
	 * @param fileFieldName the fileFieldName to set
	 */
	public WorkerBuilder withFileFieldName(String fileFieldName) {
		this.fileFieldName = fileFieldName;
		return this;
	}

	/**
	 * @param filename the filename to set
	 */
	public WorkerBuilder withFilename(String filename) {
		this.filename = filename;
		return this;
	}

	/**
	 * @param fileBody the fileBody to set
	 */
	public WorkerBuilder withFileBody(File fileBody) {
		this.fileBody = fileBody;
		return this;
	}

	/**
	 * @param isAjax the isAjax to set
	 */
	public WorkerBuilder withAjax() {
		this.isAjax = true;
		return this;
	}

	/**
	 * @param isAnyHost the isAnyHost to set
	 */
	public WorkerBuilder withAnyHost() {
		this.isAnyHost = true;
		return this;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return the context
	 */
	public HttpClientContext getContext() {
		return context;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @return the stringBody
	 */
	public String getStringBody() {
		return stringBody;
	}

	/**
	 * @return the fileFieldName
	 */
	public String getFileFieldName() {
		return fileFieldName;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @return the fileBody
	 */
	public File getFileBody() {
		return fileBody;
	}

	/**
	 * @return the isAjax
	 */
	public boolean isAjax() {
		return isAjax;
	}

	/**
	 * @return the isAnyHost
	 */
	public boolean isAnyHost() {
		return isAnyHost;
	}

	
	/**
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject json() throws Exception {
		
		return request.doRequest(this, new JsonResponseHandler());
	}
	
	/**
	 * @return SAXSource
	 * @throws Exception
	 */
	public SAXSource sax() throws Exception {
		return request.doRequest(this, new SAXSourceResponseHandler());
	}
	
	/**
	 * @return XmlObject
	 * @throws Exception
	 */
	public XmlObject xml() throws Exception {
		return request.doRequest(this, new XmlResponseHandler());
	}
	
	/**
	 * @return String
	 * @throws Exception
	 */
	public String execute() throws Exception {
		return request.doRequest(this, new StringResponseHandler());
	}
	
	/**
	 * @param file
	 * @return File
	 * @throws Exception
	 */
	public File file(final File file) throws Exception {
		return request.doRequest(this, new FileResponseHandler(file));
	}
	
	/**
	 * @return BufferedImage
	 * @throws Exception
	 */
	public BufferedImage image() throws Exception {
		return request.doRequest(this, new ImageResponseHandler());
	}
	
	/**
	 * @param clazz<T>
	 * @return JavaObject
	 * @throws Exception
	 */
	public <T> T javaObject(Class<? extends T> clazz) throws Exception {
		return json().toJavaObject(clazz);
	}
	
	/**
	 * @param clazz<T>
	 * @return JavaObject
	 * @throws Exception
	 */
	public <T> T javaObjectFromXml(Class<? extends T> clazz) throws Exception {
		return XmlReader.readFromSource(clazz, sax());
	}

}
