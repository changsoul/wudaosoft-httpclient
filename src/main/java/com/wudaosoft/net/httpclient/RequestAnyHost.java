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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
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

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
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
 * @author Changsoul Wu
 * 
 */
public class RequestAnyHost {

	private static final Logger log = LoggerFactory.getLogger(RequestAnyHost.class);

	private HostConfig hostConfig;
	private CloseableHttpClient httpClient;
	private SSLContext sslcontext;
	private Class<? extends CookieStore> defaultCookieStoreClass;
	private PoolingHttpClientConnectionManager connManager;
	private ConnectionKeepAliveStrategy myKeepAliveStrategy;
	private HttpRequestRetryHandler retryHandler;
	private HttpRequestInterceptor requestInterceptor;

	private RequestAnyHost() {
	}

	public static RequestAnyHost custom() {
		return new RequestAnyHost();
	}

	public static RequestAnyHost createDefault(HostConfig hostConfig) {
		return new RequestAnyHost().setHostConfig(hostConfig).setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	public static RequestAnyHost createDefaultWithNoRetry(HostConfig hostConfig) {
		HttpRequestRetryHandler retryHandler1 = new DefaultHttpRequestRetryHandler(0, false);
		return new RequestAnyHost().setHostConfig(hostConfig).setRetryHandler(retryHandler1).setRequestInterceptor(new SortHeadersInterceptor(hostConfig)).build();
	}

	public RequestAnyHost setHostConfig(HostConfig hostConfig) {
		this.hostConfig = hostConfig;
		return this;
	}

	public RequestAnyHost setSslcontext(SSLContext sslcontext) {
		this.sslcontext = sslcontext;
		return this;
	}

	public RequestAnyHost setDefaultCookieStoreClass(Class<? extends CookieStore> defaultCookieStoreClass) {
		this.defaultCookieStoreClass = defaultCookieStoreClass;
		return this;
	}

	public RequestAnyHost setRetryHandler(HttpRequestRetryHandler myRetryHandler) {
		this.retryHandler = myRetryHandler;
		return this;
	}

	public RequestAnyHost setRequestInterceptor(HttpRequestInterceptor requestInterceptor) {
		this.requestInterceptor = requestInterceptor;
		return this;
	}

	public RequestAnyHost build() {
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

		myKeepAliveStrategy = new ConnectionKeepAliveStrategy() {

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
				// if ("kyfw.12306.cn".equalsIgnoreCase(target.getHostName())) {
				// // Keep alive for 5 seconds only
				// return 3 * 1000;
				// } else {
				// // otherwise keep alive for 30 seconds
				// return 30 * 1000;
				// }

				return 30 * 1000;
			}

		};

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

		connManager = new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory> create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", sslConnectionSocketFactory).build());

		connManager.setMaxTotal(hostConfig.getPoolSize() + 50);
		connManager.setDefaultMaxPerRoute(8);

//		if (hostConfig.getHost() != null) {
//			connManager.setMaxPerRoute(
//					new HttpRoute(hostConfig.getHost(), null,
//							!HttpHost.DEFAULT_SCHEME_NAME.equals(hostConfig.getHost().getSchemeName())),
//					hostConfig.getPoolSize());
//		}
		
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
			httpClient = create();
		}
	}

	public <T> T get(final String surl, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler) throws Exception {
		String url = buildReqUrl(surl, params);

		HttpGet httpGet = new HttpGet(url);

		T rs = getHttpClient().execute(httpGet, responseHandler, context);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Get data from path:\"%s\". result: %s", url, rs));
		}

		return rs;
	}

	public <T> T post(final String surl, Map<String, String> params, HttpClientContext context,
			ResponseHandler<T> responseHandler) throws Exception {

		String url = buildReqUrl(surl);
		HttpPost httpPost = new HttpPost(url);

		if (params != null) {
			UrlEncodedFormEntity postEntity = buildUrlEncodedFormEntity(params);
			httpPost.setEntity(postEntity);
		}

		T rs = getHttpClient().execute(httpPost, responseHandler, context);

		if (log.isDebugEnabled()) {
			log.debug(String.format("Post data to path:\"%s\". result: %s", url, rs));
		}

		return rs;
	}

	public String postBodyData(String url, String data) throws Exception {

		HttpPost post = new HttpPost(buildReqUrl(url));

		post.setHeader("User-Agent", hostConfig.getUserAgent());

		if (data != null) {
			StringEntity reqEntity = new StringEntity(data, Consts.UTF_8);
			reqEntity.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
			post.setEntity(reqEntity);
		}

		String rs = getHttpClient().execute(post, new StringResponseHandler());

		if (log.isDebugEnabled()) {

			log.debug(String.format("Post data to path:\"%s\". result: %s", url, rs));
		}

		return rs;
	}

	/**
	 * 
	 * @param reqUrl
	 * @return
	 */
	public String buildReqUrl(String reqUrl) {

		return buildReqUrl(reqUrl, null);
	}

	/**
	 * 
	 * @param reqUrl
	 * @param params
	 * @return
	 */
	public String buildReqUrl(String reqUrl, Map<String, String> params) {
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
			sp.append(URLEncodedUtils.format(parameters, Consts.UTF_8));
		}
		return sp.toString();
	}

	/**
	 * 
	 * @param params
	 * @return
	 * @throws ClientProtocolException
	 */
	public UrlEncodedFormEntity buildUrlEncodedFormEntity(Map<String, String> params) throws ClientProtocolException {
		if (params == null)
			throw new ClientProtocolException("Params is null");

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return new UrlEncodedFormEntity(parameters, Consts.UTF_8);
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
				.setKeepAliveStrategy(myKeepAliveStrategy).setDefaultRequestConfig(hostConfig.getRequestConfig())
				// .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
				.setRetryHandler(retryHandler).setDefaultCookieStore(cookieStore);

		if (requestInterceptor != null) {
			builder.addInterceptorLast(requestInterceptor);
		}

		CloseableHttpClient httpClient = builder.build();

		return httpClient;
	}
}
