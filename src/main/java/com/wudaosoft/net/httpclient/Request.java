/**
 *    Copyright 2009-2018 Wudao Software Studio(wudaosoft.com)
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wudaosoft.net.utils.MediaType;

/**
 * 网络请求工具
 * 
 * @author Changsoul Wu
 * 
 */
public class Request {
	
	private static final Logger log = LoggerFactory.getLogger(Request.class);

	private HostConfig hostConfig;

	private CloseableHttpClient httpClient;

	private SSLContext sslcontext;

	private Class<? extends CookieStore> defaultCookieStoreClass;

	private PoolingHttpClientConnectionManager connManager;

	private ConnectionKeepAliveStrategy keepAliveStrategy;

	private boolean isKeepAlive = true;
	
	private boolean isTrustAll = false;

	private HttpRequestRetryHandler retryHandler;

	private HttpRequestInterceptor requestInterceptor;

	private HttpClientContext defaultHttpContext;

	private Request() {
	}

	/**
	 * @return the hostConfig
	 */
	public HostConfig getHostConfig() {
		return hostConfig;
	}

	/**
	 * @param hostConfig the hostConfig to set
	 */
	void setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
	}

	/**
	 * @param sslcontext the sslcontext to set
	 */
	void setSslcontext(SSLContext sslcontext) {
		this.sslcontext = sslcontext;
	}

	/**
	 * @param defaultCookieStoreClass the defaultCookieStoreClass to set
	 */
	void setDefaultCookieStoreClass(Class<? extends CookieStore> defaultCookieStoreClass) {
		this.defaultCookieStoreClass = defaultCookieStoreClass;
	}

	/**
	 * @param keepAliveStrategy the keepAliveStrategy to set
	 */
	void setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
		this.keepAliveStrategy = keepAliveStrategy;
	}

	/**
	 * @param isKeepAlive the isKeepAlive to set
	 */
	void setKeepAlive(boolean isKeepAlive) {
		this.isKeepAlive = isKeepAlive;
	}

	/**
	 * @param isTrustAll the isTrustAll to set
	 */
	void setTrustAll(boolean isTrustAll) {
		this.isTrustAll = isTrustAll;
	}

	/**
	 * @param retryHandler the retryHandler to set
	 */
	void setRetryHandler(HttpRequestRetryHandler retryHandler) {
		this.retryHandler = retryHandler;
	}

	/**
	 * @param requestInterceptor the requestInterceptor to set
	 */
	void setRequestInterceptor(HttpRequestInterceptor requestInterceptor) {
		this.requestInterceptor = requestInterceptor;
	}

	
	/**
	 * @return
	 */
	public static Request.Builder custom() {
		return new Builder();
	}

	/**
	 * @param hostConfig
	 * @return
	 */
	public static Request createDefault(HostConfig hostConfig) {
		return custom().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}
	
	/**
	 * @param hostConfig
	 * @return
	 */
	public static Request createDefaultAndTrustAll(HostConfig hostConfig) {
		return custom().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).withTrustAll().build();
	}

	/**
	 * @param hostConfig
	 * @return
	 */
	public static Request createWithNoRetry(HostConfig hostConfig) {
		return custom().setHostConfig(hostConfig).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	/**
	 * @param hostConfig
	 * @return
	 */
	public static Request createWithNoKeepAlive(HostConfig hostConfig) {
		return custom().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig))
				.withNoKeepAlive().build();
	}

	/**
	 * @param hostConfig
	 * @return
	 */
	public static Request createWithNoRetryAndNoKeepAlive(HostConfig hostConfig) {
		return custom().setHostConfig(hostConfig).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.withNoKeepAlive().setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	/**
	 * 
	 * @param workerBuilder
	 * @param responseHandler
	 * @return
	 * @throws Exception
	 */
	public <T> T doRequest(WorkerBuilder workerBuilder, ResponseHandler<T> responseHandler) throws Exception {
		
		String method = workerBuilder.getMethod();
		String url = workerBuilder.getUrl();

		Args.notNull(workerBuilder, "WorkerBuilder");
		Args.notEmpty(method, "WorkerBuilder.getMethod()");
		Args.notEmpty(url, "WorkerBuilder.getUrl()");
		Args.notNull(responseHandler, "responseHandler");
		
//		if(!workerBuilder.isAnyHost()) {
		if(!isFullUrl(url)) {
//			notFullUrl(url);
			Args.notEmpty(hostConfig.getHostUrl(), "HostConfig.getHostUrl()");
			url = hostConfig.getHostUrl() + url;
		}
		
		Charset charset = hostConfig.getCharset() == null ? Consts.UTF_8 : hostConfig.getCharset();
		String stringBody = workerBuilder.getStringBody();
		File fileBody = workerBuilder.getFileBody();
		InputStream streamBody = workerBuilder.getStreamBody();
		Map<String, String> params = workerBuilder.getParameters();

		String contentType = null;

		if (responseHandler instanceof JsonResponseHandler) {
			contentType = MediaType.APPLICATION_JSON_VALUE;
		} else if (responseHandler instanceof SAXSourceResponseHandler
				|| responseHandler instanceof XmlResponseHandler) {
			contentType = MediaType.APPLICATION_XML_VALUE;
		} else if (responseHandler instanceof FileResponseHandler
				|| responseHandler instanceof ImageResponseHandler 
				|| responseHandler instanceof OutputStreamResponseHandler) {
			contentType = MediaType.ALL_VALUE;
		} else if (responseHandler instanceof NoResultResponseHandler){
			contentType = ((NoResultResponseHandler)responseHandler).getContentType().getMimeType();
		} else {
			contentType = MediaType.TEXT_PLAIN_VALUE;
		}

		RequestBuilder requestBuilder = RequestBuilder.create(method).setCharset(charset).setUri(url);
		
		if (stringBody != null) {
			
			StringEntity reqEntity = new StringEntity(stringBody, charset);
			reqEntity.setContentType(contentType + ";charset=" + charset.name());
			requestBuilder.setEntity(reqEntity);
			
		} else if (fileBody != null || streamBody != null) {
			
			String filename = workerBuilder.getFilename();

			MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create().setLaxMode();

			if(fileBody != null) {
				Args.check(fileBody.isFile(), "fileBody must be a file");
				Args.check(fileBody.canRead(), "fileBody must be readable");
				
				if (filename == null && streamBody == null)
					filename = fileBody.getName();
				
				FileBody bin = new FileBody(fileBody, ContentType.APPLICATION_OCTET_STREAM, streamBody != null ? fileBody.getName() : filename);
				reqEntity.addPart(workerBuilder.getFileFieldName(), bin);
			}
			
			Args.notEmpty(filename, "filename");
			
			if(streamBody != null)
				reqEntity.addBinaryBody(workerBuilder.getFileFieldName(), streamBody, ContentType.APPLICATION_OCTET_STREAM, filename);
			
			buildParameters(reqEntity, params, charset);
			
			requestBuilder.setEntity(reqEntity.build());
		}
		
		if (fileBody == null && streamBody == null) {
			buildParameters(requestBuilder, params);
		}
		
		if (workerBuilder.getReadTimeout() > -1) {
			
			requestBuilder.setConfig(RequestConfig.copy(this.hostConfig.getRequestConfig())
					.setSocketTimeout(workerBuilder.getReadTimeout()).build());
		}

		HttpUriRequest httpRequest = ParameterRequestBuilder.build(requestBuilder);

		setAcceptHeader(httpRequest, contentType);

		if (workerBuilder.isAjax())
			setAjaxHeader(httpRequest);

		HttpClientContext context= workerBuilder.getContext();
		if (context == null)
			context = defaultHttpContext;

		T result = getHttpClient().execute(httpRequest, responseHandler, context);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Send data to path:[%s]\"%s\". result: %s", method, url, result));
		}

		return result;
	}

	public void setAjaxHeader(HttpRequest resquest) {
		resquest.addHeader("X-Requested-With", "XMLHttpRequest");
	}

	public void setAcceptHeader(HttpRequest resquest, String accept) {
		resquest.addHeader("Accept", accept);
	}
	
	public static String buildReqUrl(String reqUrl, Map<String, String> params) throws URISyntaxException {
		return new URIBuilder(reqUrl).setParameters(buildUrlNameValuePair(params)).build().toString();
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	public static List<NameValuePair> buildUrlNameValuePair(Map<String, String> params) {
		Args.notNull(params, "params");
		
		List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());
		
		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		
		return parameters;
	}
	
	/**
	 * @param params
	 * @param charset
	 * @return
	 */
	public UrlEncodedFormEntity buildUrlEncodedFormEntity(Map<String, String> params, Charset charset) {
		Args.notNull(params, "params");

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return new UrlEncodedFormEntity(parameters, charset);
	}

	/**
	 * @param reqEntity
	 * @param params
	 * @param charset
	 */
	public void buildParameters(MultipartEntityBuilder reqEntity, Map<String, String> params, Charset charset) {
		
		if (params != null && !params.isEmpty()) {
			
			ContentType contentType = ContentType.TEXT_PLAIN.withCharset(charset);
			
			for (Map.Entry<String, String> entry : params.entrySet()) {
				
				if(entry.getKey() == null)
					continue;
				
				String value = entry.getValue();

				if (value == null)
					value = "";

				reqEntity.addPart(entry.getKey(), new StringBody(value, contentType));
			}
		}
	}
	
	/**
	 * 
	 * @param requestBuilder
	 * @param params
	 * @return
	 */
	public void buildParameters(RequestBuilder requestBuilder, Map<String, String> params) {

		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if(entry.getKey() == null)
					continue;
				
				String value = entry.getValue();

				if (value == null)
					value = "";
				
				requestBuilder.addParameter(entry.getKey(), value);
			}
		}
	}

	private boolean isFullUrl(final String suffixUrl) {
		return suffixUrl.indexOf("://") != -1;
	}
	
//	private void notFullUrl(final String suffixUrl) {
//		Args.check(!isFullUrl(suffixUrl), "suffixUrl must be not contains \"://\".");
//	}

	/**
	 * @return
	 */
	public CloseableHttpClient getHttpClient() {

		if (!hostConfig.isMulticlient()) {
			return httpClient;
		}

		return create();
	}

	protected void init() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			CertificateException, IOException {

		Args.notNull(hostConfig, "Host config");

		SSLConnectionSocketFactory sslConnectionSocketFactory = null;

		if (sslcontext == null) {

			if (hostConfig.getCA() != null) {
				// Trust root CA and all self-signed certs
				SSLContext sslcontext1 = SSLContexts.custom().loadTrustMaterial(hostConfig.getCA(),
						hostConfig.getCAPassword(), TrustSelfSignedStrategy.INSTANCE).build();

				// Allow TLSv1 protocol only
				sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext1, new String[] { "TLSv1" }, null,
						SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			} else {
				
				if (isTrustAll) {
					
					SSLContext sslcontext1 = SSLContext.getInstance("TLS");

					TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {

						}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {
						}

					} };

					sslcontext1.init(null, trustAllCerts, null);

					sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext1, NoopHostnameVerifier.INSTANCE);
				} else {
					sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
				}
			}
		} else {

			sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
					SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		}

		if (keepAliveStrategy == null) {
			keepAliveStrategy = new ConnectionKeepAliveStrategy() {

				public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
					// Honor 'keep-alive' header
					HeaderElementIterator it = new BasicHeaderElementIterator(
							response.headerIterator(HTTP.CONN_KEEP_ALIVE));
					while (it.hasNext()) {
						HeaderElement he = it.nextElement();
						String param = he.getName();
						String value = he.getValue();
						if (value != null && param.equalsIgnoreCase("timeout")) {
							try {
								return Long.parseLong(value) * 1000;
							} catch (NumberFormatException ignore) {
							}
						}
					}
					// HttpHost target = (HttpHost)
					// context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
					// if
					// ("xxxxx".equalsIgnoreCase(target.getHostName()))
					// {
					// // Keep alive for 5 seconds only
					// return 3 * 1000;
					// } else {
					// // otherwise keep alive for 30 seconds
					// return 30 * 1000;
					// }

					return 30 * 1000;
				}

			};
		}

		if (retryHandler == null) {
			retryHandler = new HttpRequestRetryHandler() {

				public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
					if (executionCount >= 3) {
						// Do not retry if over max retry count
						return false;
					}
					if (exception instanceof InterruptedIOException) {
						// Timeout
						return false;
					}
					if (exception instanceof UnknownHostException) {
						// Unknown host
						return false;
					}
					if (exception instanceof ConnectTimeoutException) {
						// Connection refused
						return false;
					}
					if (exception instanceof SSLException) {
						// SSL handshake exception
						return false;
					}
					HttpClientContext clientContext = HttpClientContext.adapt(context);
					HttpRequest request = clientContext.getRequest();
					boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
					if (idempotent) {
						// Retry if the request is considered idempotent
						return true;
					}
					return false;
				}
			};
		}

		connManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", sslConnectionSocketFactory).build());

		

		if (hostConfig.getHost() != null) {
			
			connManager.setMaxTotal(hostConfig.getPoolSize() + 60);
			
			connManager.setMaxPerRoute(
					new HttpRoute(hostConfig.getHost(), null,
							!HttpHost.DEFAULT_SCHEME_NAME.equals(hostConfig.getHost().getSchemeName())),
					hostConfig.getPoolSize());
			
			connManager.setDefaultMaxPerRoute(20);
		} else {
			connManager.setMaxTotal(hostConfig.getPoolSize());
			int hostCount = hostConfig.getHostCount() == 0 ? 10 : hostConfig.getHostCount();
			connManager.setDefaultMaxPerRoute(hostConfig.getPoolSize() / hostCount);
		}
		
		// connManager.setValidateAfterInactivity(2000);

		// Create socket configuration
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(isKeepAlive).build();
		connManager.setDefaultSocketConfig(socketConfig);

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(hostConfig.getCharset() == null ? Consts.UTF_8 : hostConfig.getCharset()).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		new IdleConnectionMonitorThread(connManager).start();

		if (requestInterceptor == null) {
			requestInterceptor = new SortHeadersInterceptor(hostConfig);
		}
		
		if (!hostConfig.isMulticlient()) {
			defaultHttpContext = HttpClientContext.create();
			httpClient = create();
		}
	}

	private CloseableHttpClient create() {

		CookieStore cookieStore = null;

		try {
			if (defaultCookieStoreClass != null)
				cookieStore = defaultCookieStoreClass.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager)
				.setDefaultRequestConfig(hostConfig.getRequestConfig()).setRetryHandler(retryHandler)
				.setDefaultCookieStore(cookieStore);

		if (isKeepAlive) {
			builder.setKeepAliveStrategy(keepAliveStrategy);
		} else {
			builder.setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE);
		}

		if (requestInterceptor != null) {
			builder.addInterceptorLast(requestInterceptor);
		}

		CloseableHttpClient httpClient = builder.build();

		return httpClient;
	}

	public void shutdown() {
		connManager.shutdown();
	}
	
	public WorkerBuilder worker() {
		return new WorkerBuilder(this);
	}
	
	public WorkerBuilder get(String url) {
		return new WorkerBuilder(this, HttpGet.METHOD_NAME, url);
	}
	
	public WorkerBuilder get(String url, Map<String, String> parameters) {
		return get(url).withParameters(parameters);
	}
	
	public WorkerBuilder post(String url) {
		return new WorkerBuilder(this, HttpPost.METHOD_NAME, url);
	}
	
	public WorkerBuilder post(String url, Map<String, String> parameters) {
		return post(url).withParameters(parameters);
	}
	
	public WorkerBuilder post(String url, String stringBody) {
		return post(url).withStringBody(stringBody);
	}
	
	public WorkerBuilder post(String url, Map<String, String> parameters, String stringBody) {
		return post(url).withParameters(parameters).withStringBody(stringBody);
	}
	
	public WorkerBuilder post(String url, File fileBody, String fileFieldName) {
		return post(url, null, fileBody, fileFieldName);
	}
	
	public WorkerBuilder post(String url, Map<String, String> parameters, File fileBody, String fileFieldName) {
		return post(url, parameters, fileBody, fileFieldName, null);
	}
	
	public WorkerBuilder post(String url, Map<String, String> parameters, File fileBody, String fileFieldName, String filename) {
		return post(url).withParameters(parameters).withFileBody(fileBody).withFileFieldName(fileFieldName).withFilename(filename);
	}
	
	public WorkerBuilder post(String url, InputStream streamBody, String fileFieldName, String filename) {
		return post(url, null, streamBody, fileFieldName, filename);
	}
	
	public WorkerBuilder post(String url, Map<String, String> parameters, InputStream streamBody, String fileFieldName, String filename) {
		return post(url).withParameters(parameters).withStreamBody(streamBody).withFileFieldName(fileFieldName).withFilename(filename);
	}
	
	public WorkerBuilder put(String url) {
		return new WorkerBuilder(this, HttpPut.METHOD_NAME, url);
	}
	
	public WorkerBuilder put(String url, Map<String, String> parameters) {
		return put(url).withParameters(parameters);
	}
	
	public WorkerBuilder put(String url, String stringBody) {
		return put(url).withStringBody(stringBody);
	}
	
	public WorkerBuilder put(String url, Map<String, String> parameters, String stringBody) {
		return put(url).withParameters(parameters).withStringBody(stringBody);
	}
	
	public WorkerBuilder patch(String url) {
		return new WorkerBuilder(this, HttpPatch.METHOD_NAME, url);
	}
	
	public WorkerBuilder patch(String url, Map<String, String> parameters) {
		return patch(url).withParameters(parameters);
	}
	
	public WorkerBuilder patch(String url, String stringBody) {
		return patch(url).withStringBody(stringBody);
	}
	
	public WorkerBuilder patch(String url, Map<String, String> parameters, String stringBody) {
		return patch(url).withParameters(parameters).withStringBody(stringBody);
	}
	
	public WorkerBuilder delete(String url) {
		return new WorkerBuilder(this, HttpDelete.METHOD_NAME, url);
	}
	
	public static class Builder {

		private HostConfig hostConfig;

		private SSLContext sslcontext;

		private Class<? extends CookieStore> cookieStoreClass;

		private ConnectionKeepAliveStrategy keepAliveStrategy;

		private boolean isKeepAlive = true;
		
		private boolean isTrustAll = false;

		private HttpRequestRetryHandler retryHandler;

		private HttpRequestInterceptor requestInterceptor;

		Builder() {

		}

		public Builder setHostConfig(HostConfig hostConfig) {
			this.hostConfig = hostConfig;
			return this;
		}

		public Builder setSslcontext(SSLContext sslcontext) {
			this.sslcontext = sslcontext;
			return this;
		}

		public Builder setCookieStoreClass(Class<? extends CookieStore> cookieStoreClass) {
			this.cookieStoreClass = cookieStoreClass;
			return this;
		}

		public Builder setRetryHandler(HttpRequestRetryHandler myRetryHandler) {
			this.retryHandler = myRetryHandler;
			return this;
		}

		public Builder setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
			this.keepAliveStrategy = keepAliveStrategy;
			return this;
		}

		public Builder withNoKeepAlive() {
			this.isKeepAlive = false;
			return this;
		}

		public Builder withTrustAll() {
			this.isTrustAll = true;
			return this;
		}

		public Builder setRequestInterceptor(HttpRequestInterceptor requestInterceptor) {
			this.requestInterceptor = requestInterceptor;
			return this;
		}

		public Request build() {
			
			Args.notNull(hostConfig, "HostConfig");
			
			try {
				Request request = new Request();
				request.setDefaultCookieStoreClass(cookieStoreClass);
				request.setHostConfig(hostConfig);
				request.setTrustAll(isTrustAll);
				request.setKeepAlive(isKeepAlive);
				request.setKeepAliveStrategy(keepAliveStrategy);
				request.setRequestInterceptor(requestInterceptor);
				request.setRetryHandler(retryHandler);
				request.setSslcontext(sslcontext);
				
				request.init();
				return request;
			} catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				throw new RuntimeException(e);
			}
		}
	}


}
