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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.xml.transform.sax.SAXSource;

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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
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

import com.alibaba.fastjson.JSONObject;
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
	
	private HttpRequestRetryHandler retryHandler;
	
	private HttpRequestInterceptor requestInterceptor;
	
	private HttpClientContext defaultHttpContext;

	private Request() {
	}

	public static Request custom() {
		return new Request();
	}

	public static Request createDefault(HostConfig hostConfig) {
		return new Request().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig))
				.build();
	}

	public static Request createWithNoRetry(HostConfig hostConfig) {
		return new Request().setHostConfig(hostConfig).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	public static Request createWithNoKeepAlive(HostConfig hostConfig) {
		return new Request().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig))
				.setIsKeepAlive(false).build();
	}

	public static Request createWithNoRetryAndNoKeepAlive(HostConfig hostConfig) {
		return new Request().setHostConfig(hostConfig).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.setIsKeepAlive(false).setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	public Request setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
		return this;
	}

	public HostConfig getHostConfig() {
		return hostConfig;
	}

	public Request setSslcontext(SSLContext sslcontext) {
		this.sslcontext = sslcontext;
		return this;
	}

	public Request setDefaultCookieStoreClass(Class<? extends CookieStore> defaultCookieStoreClass) {
		this.defaultCookieStoreClass = defaultCookieStoreClass;
		return this;
	}

	public Request setRetryHandler(HttpRequestRetryHandler myRetryHandler) {
		this.retryHandler = myRetryHandler;
		return this;
	}

	/**
	 * @param keepAliveStrategy
	 *            the keepAliveStrategy to set
	 */
	public Request setKeepAliveStrategy(ConnectionKeepAliveStrategy keepAliveStrategy) {
		this.keepAliveStrategy = keepAliveStrategy;
		return this;
	}

	/**
	 * @param isKeepAlive the isKeepAlive to set
	 */
	public Request setIsKeepAlive(boolean isKeepAlive) {
		this.isKeepAlive = isKeepAlive;
		return this;
	}

	public Request setRequestInterceptor(HttpRequestInterceptor requestInterceptor) {
		this.requestInterceptor = requestInterceptor;
		return this;
	}

	public Request build() {
		try {
			init();
			return this;
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

	protected void init() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			CertificateException, IOException {

		Args.notNull(hostConfig, "Host config");

		SSLConnectionSocketFactory sslConnectionSocketFactory = null;

		if (sslcontext == null) {

			if (hostConfig.getCA() != null) {
				// Trust root CA and all self-signed certs
				SSLContext sslcontext1 = SSLContexts.custom().loadTrustMaterial(hostConfig.getCA(),
						hostConfig.getCAPassword(), new TrustSelfSignedStrategy()).build();

				// Allow TLSv1 protocol only
				sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslcontext1, new String[] { "TLSv1" }, null,
						SSLConnectionSocketFactory.getDefaultHostnameVerifier());
			} else {
				sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
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

		connManager.setMaxTotal(hostConfig.getPoolSize() + 30);
		connManager.setDefaultMaxPerRoute(5);

		if (hostConfig.getHost() != null) {
			connManager.setMaxPerRoute(
					new HttpRoute(hostConfig.getHost(), null,
							!HttpHost.DEFAULT_SCHEME_NAME.equals(hostConfig.getHost().getSchemeName())),
					hostConfig.getPoolSize());
		}
		// connManager.setValidateAfterInactivity(2000);

		// Create socket configuration
		SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true).build();
		connManager.setDefaultSocketConfig(socketConfig);

		// Create connection configuration
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();
		connManager.setDefaultConnectionConfig(connectionConfig);

		new IdleConnectionMonitorThread(connManager).start();

		if (!hostConfig.isMulticlient()) {
			defaultHttpContext = HttpClientContext.create();
			httpClient = create();
		}
	}

	public String getString(final String suffixUrl) throws Exception {

		return getString(suffixUrl, (Map<String, String>) null);
	}

	public String getString(final String suffixUrl, Map<String, String> params) throws Exception {

		return getString(suffixUrl, params, null);
	}

	public String getString(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return getString(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public String getString(final String hostUrl, String urlSuffix) throws Exception {

		return getString(hostUrl, urlSuffix, null);
	}

	public String getString(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return getString(hostUrl, urlSuffix, params, null);
	}

	public String getString(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return get(hostUrl, urlSuffix, params, context, new StringResponseHandler());
	}

	public JSONObject getJson(final String suffixUrl) throws Exception {

		return getJson(suffixUrl, (Map<String, String>) null);
	}

	public JSONObject getJson(final String suffixUrl, Map<String, String> params) throws Exception {

		return getJson(suffixUrl, params, null);
	}

	public JSONObject getJson(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return getJson(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public JSONObject getJson(final String hostUrl, String urlSuffix) throws Exception {

		return getJson(hostUrl, urlSuffix, null);
	}

	public JSONObject getJson(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return getJson(hostUrl, urlSuffix, params, null);
	}

	public JSONObject getJson(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return get(hostUrl, urlSuffix, params, context, new JsonResponseHandler());
	}

	public SAXSource getXml(final String suffixUrl) throws Exception {

		return getXml(suffixUrl, (Map<String, String>) null);
	}

	public SAXSource getXml(final String suffixUrl, Map<String, String> params) throws Exception {

		return getXml(suffixUrl, params, null);
	}

	public SAXSource getXml(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return getXml(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public SAXSource getXml(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return getXml(hostUrl, urlSuffix, params, null);
	}

	public SAXSource getXml(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return get(hostUrl, urlSuffix, params, context, new SAXSourceResponseHandler());
	}

	public JSONObject getJsonAjax(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return getJsonAjax(hostUrl, urlSuffix, params, null);
	}

	public JSONObject getJsonAjax(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return get(hostUrl, urlSuffix, params, context, new JsonResponseHandler(), true);
	}

	public SAXSource getXmlAjax(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return getXmlAjax(hostUrl, urlSuffix, params, null);
	}

	public SAXSource getXmlAjax(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return get(hostUrl, urlSuffix, params, context, new SAXSourceResponseHandler(), true);
	}

	public String postString(final String suffixUrl) throws Exception {

		return postString(suffixUrl, (Map<String, String>) null);
	}

	public String postString(final String suffixUrl, Map<String, String> params) throws Exception {

		return postString(suffixUrl, params, null);
	}

	public String postString(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return postString(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public String postString(final String hostUrl, String urlSuffix) throws Exception {

		return postString(hostUrl, urlSuffix, null);
	}

	public String postString(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return postString(hostUrl, urlSuffix, params, null);
	}

	public String postString(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return post(hostUrl, urlSuffix, params, context, new StringResponseHandler());
	}

	public JSONObject postJson(final String suffixUrl) throws Exception {

		return postJson(suffixUrl, (Map<String, String>) null);
	}

	public JSONObject postJson(final String suffixUrl, Map<String, String> params) throws Exception {

		return postJson(suffixUrl, params, null);
	}

	public JSONObject postJson(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return postJson(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public JSONObject postJson(final String hostUrl, String urlSuffix) throws Exception {

		return postJson(hostUrl, urlSuffix, null);
	}

	public JSONObject postJson(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return postJson(hostUrl, urlSuffix, params, null);
	}

	public JSONObject postJson(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return post(hostUrl, urlSuffix, params, context, new JsonResponseHandler());
	}

	public SAXSource postXml(final String suffixUrl) throws Exception {

		return postXml(suffixUrl, (Map<String, String>) null);
	}

	public SAXSource postXml(final String suffixUrl, Map<String, String> params) throws Exception {

		return postXml(suffixUrl, params, null);
	}

	public SAXSource postXml(final String suffixUrl, Map<String, String> params, HttpClientContext context)
			throws Exception {

		notFullUrl(suffixUrl);

		return postXml(this.hostConfig.getHostUrl(), suffixUrl, params, context);
	}

	public SAXSource postXml(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return postXml(hostUrl, urlSuffix, params, null);
	}

	public SAXSource postXml(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return post(hostUrl, urlSuffix, params, context, new SAXSourceResponseHandler());
	}

	public JSONObject postJsonAjax(final String hostUrl, String urlSuffix, Map<String, String> params)
			throws Exception {

		return postJsonAjax(hostUrl, urlSuffix, params, null);
	}

	public JSONObject postJsonAjax(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return post(hostUrl, urlSuffix, params, context, new JsonResponseHandler(), true);
	}

	public SAXSource postXmlAjax(final String hostUrl, String urlSuffix, Map<String, String> params) throws Exception {

		return postXmlAjax(hostUrl, urlSuffix, params, null);
	}

	public SAXSource postXmlAjax(final String hostUrl, String urlSuffix, Map<String, String> params,
			HttpClientContext context) throws Exception {

		return post(hostUrl, urlSuffix, params, context, new SAXSourceResponseHandler(), true);
	}

	public JSONObject postBodyJson(final String suffixUrl, String data) throws Exception {

		return postBodyJson(suffixUrl, data, (HttpClientContext) null);
	}

	public JSONObject postBodyJson(final String suffixUrl, String data, HttpClientContext context) throws Exception {

		notFullUrl(suffixUrl);

		return postBodyJson(this.hostConfig.getHostUrl(), suffixUrl, data, context);
	}

	public JSONObject postBodyJson(final String hostUrl, String urlSuffix, String data) throws Exception {

		return postBodyJson(hostUrl, urlSuffix, data, null);
	}

	public JSONObject postBodyJson(final String hostUrl, String urlSuffix, String data, HttpClientContext context)
			throws Exception {

		return doRequest(HttpPost.METHOD_NAME, hostUrl, urlSuffix, data, context, new JsonResponseHandler());
	}

	public SAXSource postBodyXml(final String suffixUrl, String data) throws Exception {

		return postBodyXml(suffixUrl, data, (HttpClientContext) null);
	}

	public SAXSource postBodyXml(final String suffixUrl, String data, HttpClientContext context) throws Exception {

		notFullUrl(suffixUrl);

		return postBodyXml(this.hostConfig.getHostUrl(), suffixUrl, data, context);
	}

	public SAXSource postBodyXml(final String hostUrl, String urlSuffix, String data) throws Exception {

		return postBodyXml(hostUrl, urlSuffix, data, null);
	}

	public SAXSource postBodyXml(final String hostUrl, String urlSuffix, String data, HttpClientContext context)
			throws Exception {

		return doRequest(HttpPost.METHOD_NAME, hostUrl, urlSuffix, data, context, new SAXSourceResponseHandler());
	}

	public String postBodyData(final String hostUrl, String urlSuffix, String data, HttpClientContext context)
			throws Exception {

		return doRequest(HttpPost.METHOD_NAME, hostUrl, urlSuffix, data, context, new StringResponseHandler());
	}

	public <T> T get(final String hostUrl, String urlSuffix, Map<String, String> params,
			ResponseHandler<T> responseHandler) throws Exception {

		return get(hostUrl, urlSuffix, params, null, responseHandler);
	}

	public <T> T get(final String hostUrl, String urlSuffix, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler) throws Exception {

		return get(hostUrl, urlSuffix, params, context, responseHandler, false);
	}

	public <T> T get(final String hostUrl, String urlSuffix, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler, boolean isAjax) throws Exception {

		return doRequest(HttpGet.METHOD_NAME, hostUrl, urlSuffix, params, context, responseHandler, isAjax);
	}

	public <T> T post(final String hostUrl, String urlSuffix, Map<String, String> params,
			ResponseHandler<T> responseHandler) throws Exception {

		return post(hostUrl, urlSuffix, params, null, responseHandler);
	}

	public <T> T post(final String hostUrl, String urlSuffix, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler) throws Exception {

		return post(hostUrl, urlSuffix, params, context, responseHandler, false);
	}

	public <T> T post(final String hostUrl, String urlSuffix, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler, boolean isAjax) throws Exception {

		return doRequest(HttpPost.METHOD_NAME, hostUrl, urlSuffix, params, context, responseHandler, isAjax);
	}

	public <T> T doRequest(final String method, final String suffixUrl, Map<String, String> params,
			ResponseHandler<T> responseHandler) throws Exception {

		return doRequest(method, suffixUrl, params, null, responseHandler);
	}

	public <T> T doRequest(final String method, final String suffixUrl, Map<String, String> params,
			HttpClientContext context, ResponseHandler<T> responseHandler) throws Exception {

		notFullUrl(suffixUrl);

		return doRequest(method, hostConfig.getHostUrl(), suffixUrl, params, context, responseHandler);
	}

	public <T> T doRequest(final String method, final String hostUrl, final String urlSuffix,
			Map<String, String> params, HttpClientContext context, ResponseHandler<T> responseHandler)
			throws Exception {

		return doRequest(method, hostUrl, urlSuffix, params, context, responseHandler, false);
	}

	public <T> T doRequest(final String method, final String hostUrl, final String urlSuffix,
			Map<String, String> params, HttpClientContext context, ResponseHandler<T> responseHandler, boolean isAjax)
			throws Exception {

		return doRequest(method, hostUrl, urlSuffix, params, null, context, responseHandler, isAjax);
	}

	public <T> T doRequest(final String method, final String suffixUrl, String data, ResponseHandler<T> responseHandler)
			throws Exception {

		return doRequest(method, suffixUrl, data, null, responseHandler);
	}

	public <T> T doRequest(final String method, final String suffixUrl, String data, HttpClientContext context,
			ResponseHandler<T> responseHandler) throws Exception {

		notFullUrl(suffixUrl);

		return doRequest(method, hostConfig.getHostUrl(), suffixUrl, data, context, responseHandler);
	}

	public <T> T doRequest(final String method, final String hostUrl, final String urlSuffix, String data,
			HttpClientContext context, ResponseHandler<T> responseHandler) throws Exception {

		return doRequest(method, hostUrl, urlSuffix, data, context, responseHandler, false);
	}

	public <T> T doRequest(final String method, final String hostUrl, final String urlSuffix, String data,
			HttpClientContext context, ResponseHandler<T> responseHandler, boolean isAjax) throws Exception {

		Args.notNull(data, "data");
		
		return doRequest(method, hostUrl, urlSuffix, null, data, context, responseHandler, isAjax);
	}

	public <T> T doRequest(final String method, final String hostUrl, final String urlSuffix,
			Map<String, String> params, String data, HttpClientContext context, ResponseHandler<T> responseHandler,
			boolean isAjax) throws Exception {

		Args.notEmpty(hostUrl, "hostUrl");
		Args.notEmpty(urlSuffix, "urlSuffix");
		Args.notNull(responseHandler, "responseHandler");

		Charset charset = hostConfig.getCharset() == null ? Consts.UTF_8 : hostConfig.getCharset();

		final String url = buildReqUrl(hostUrl + urlSuffix, charset);
		// final String url = hostUrl + urlSuffix;

		String contentType = null;

		if (responseHandler instanceof JsonResponseHandler) {
			contentType = MediaType.APPLICATION_JSON_VALUE;
		} else if (responseHandler instanceof SAXSourceResponseHandler
				|| responseHandler instanceof XmlResponseHandler) {
			contentType = MediaType.APPLICATION_XML_VALUE;
		} else if (responseHandler instanceof StringResponseHandler) {
			contentType = MediaType.TEXT_PLAIN_VALUE;
		} else {
			contentType = MediaType.APPLICATION_JSON_VALUE;
		}

		RequestBuilder requestBuilder = RequestBuilder.create(method).setCharset(charset).setUri(url);

		if (data == null) {
			buildParameters(requestBuilder, params);
		} else {
			StringEntity reqEntity = new StringEntity(data, charset);
			reqEntity.setContentType(contentType + "; charset=" + charset.name());
			requestBuilder.setEntity(reqEntity);
		}

		HttpUriRequest httpRequest = AllRequestBuilder.build(requestBuilder);

		setAcceptHeader(httpRequest, contentType);

		if (isAjax)
			setAjaxHeader(httpRequest);

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

	/**
	 * 
	 * @param reqUrl
	 * @return
	 */
	public String buildReqUrl(String reqUrl, Charset charset) {

		return buildReqUrl(reqUrl, null, charset);
	}

	/**
	 * 
	 * @param reqUrl
	 * @param params
	 * @return
	 */
	public String buildReqUrl(String reqUrl, Map<String, String> params, Charset charset) {
		if (reqUrl == null)
			return null;

		if (params == null) {

			if (reqUrl.indexOf("?") == -1)
				return reqUrl;

			params = new HashMap<String, String>();
		}

		String[] reqUrls = reqUrl.split("\\?");
		StringBuilder sp = new StringBuilder(reqUrls[0]);

		if (reqUrls.length == 2 && reqUrls[1].trim().length() != 0) {

			String[] kvs = reqUrls[1].split("&");
			for (String kv : kvs) {
				if (kv == null || kv.length() == 0)
					continue;

				String[] nv = kv.split("=");
				if (nv.length > 2)
					continue;

				if (nv.length == 2) {
					params.put(nv[0], nv[1]);
				} else {
					params.put(nv[0], "");
				}
			}
		}

		if (!params.isEmpty()) {
			sp.append("?");
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());
			for (Map.Entry<String, String> entry : params.entrySet()) {
				parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			sp.append(URLEncodedUtils.format(parameters, charset));
		}
		return sp.toString();
	}

	/**
	 * 
	 * @param params
	 * @return
	 */
	public UrlEncodedFormEntity buildUrlEncodedFormEntity(Map<String, String> params) {
		Args.notNull(params, "params");

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return new UrlEncodedFormEntity(parameters, Consts.UTF_8);
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
				requestBuilder.addParameter(entry.getKey(), entry.getValue());
			}
		}
	}

	private void notFullUrl(final String suffixUrl) {
		Args.notNull(suffixUrl, "suffixUrl");
		Args.check(suffixUrl.indexOf("://") == -1, "suffixUrl must be not contains \"://\".");
	}

	/**
	 * @return
	 */
	public CloseableHttpClient getHttpClient() {

		if (!hostConfig.isMulticlient()) {
			return httpClient;
		}

		return create();
	}

	public CloseableHttpClient create() {

		CookieStore cookieStore = null;

		try {
			if (defaultCookieStoreClass != null)
				cookieStore = defaultCookieStoreClass.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		HttpClientBuilder builder = HttpClients.custom().setConnectionManager(connManager)
				.setDefaultRequestConfig(hostConfig.getRequestConfig())
				.setRetryHandler(retryHandler).setDefaultCookieStore(cookieStore);
		
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

}
