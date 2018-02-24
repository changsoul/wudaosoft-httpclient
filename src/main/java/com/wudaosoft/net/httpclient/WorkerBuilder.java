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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.util.Args;

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
    private InputStream streamBody;
    private boolean isAjax = false;
    private boolean isAnyHost = false;
    private int readTimeout = -1;
    
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
	 * @param streamBody the streamBody to set
	 */
	public WorkerBuilder withStreamBody(InputStream streamBody) {
		this.streamBody = streamBody;
		return this;
	}

	/**
	 */
	public WorkerBuilder withAjax() {
		this.isAjax = true;
		return this;
	}
	
//	public WorkerBuilder asFullUrl() {
//		this.isAnyHost = true;
//		return this;
//	}
	
	/**
	 * 读取返回数据中两个相邻报文之间的间隔超时时间，不是读取全部数据流的超时时间。单位：毫秒
	 * 设为0将无限等待
	 * 
	 * @param readTimeout the readTimeout to set
	 */
	public WorkerBuilder withReadTimeout(int readTimeout) {
		this.readTimeout = Args.notNegative(readTimeout, "readTimeout");
		return this;
	}

	/**
	 * @return the method
	 */
	String getMethod() {
		return method;
	}

	/**
	 * @return the url
	 */
	String getUrl() {
		return url;
	}

	/**
	 * @return the context
	 */
	HttpClientContext getContext() {
		return context;
	}

	/**
	 * @return the parameters
	 */
	Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @return the stringBody
	 */
	String getStringBody() {
		return stringBody;
	}

	/**
	 * @return the fileFieldName
	 */
	String getFileFieldName() {
		return fileFieldName;
	}

	/**
	 * @return the filename
	 */
	String getFilename() {
		return filename;
	}

	/**
	 * @return the fileBody
	 */
	File getFileBody() {
		return fileBody;
	}

	/**
	 * @return the streamBody
	 */
	InputStream getStreamBody() {
		return streamBody;
	}

	/**
	 * @return the isAjax
	 */
	boolean isAjax() {
		return isAjax;
	}

	/**
	 * @return the isAnyHost
	 */
	boolean isAnyHost() {
		return isAnyHost;
	}
	
	/**
	 * @return the readTimeout
	 */
	int getReadTimeout() {
		return readTimeout;
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
	
	public int noResult() throws Exception {
		return request.doRequest(this, new NoResultResponseHandler(ContentType.APPLICATION_JSON)).intValue();
	}
	
	public int noResult(ContentType contentType) throws Exception {
		return request.doRequest(this, new NoResultResponseHandler(contentType)).intValue();
	}
	
	/**
	 * @param file 待写入的文件
	 * @return File 写入完成后的文件
	 * @throws Exception
	 */
	public File file(final File file) throws Exception {
		return request.doRequest(this, new FileResponseHandler(file));
	}
	
	/**
	 * @param dir 存放下载文件的文件夹路径，且文件夹必需存在。
	 * @return File 下载完成后的文件
	 * @throws Exception
	 */
	public File file(final String dir) throws Exception {
		return request.doRequest(this, new FileResponseHandler(dir));
	}
	
	/**
	 * @return BufferedImage
	 * @throws Exception
	 */
	public BufferedImage image() throws Exception {
		return request.doRequest(this, new ImageResponseHandler());
	}
	
	/**
	 * @param out The OutputStream to write
	 * @throws Exception
	 */
	public void stream(OutputStream out) throws Exception {
		request.doRequest(this, new OutputStreamResponseHandler(out));
	}
	
	/**
	 * @param clazz
	 * @return JavaObject
	 * @throws Exception
	 */
	public <T> T javaObject(Class<? extends T> clazz) throws Exception {
		return json().toJavaObject(clazz);
	}
	
	/**
	 * @param clazz
	 * @return JavaObject
	 * @throws Exception
	 */
	public <T> T javaObjectFromXml(Class<? extends T> clazz) throws Exception {
		return XmlReader.readFromSource(clazz, sax());
	}

}
