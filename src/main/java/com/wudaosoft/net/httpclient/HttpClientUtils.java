/* Copyright(c)2010-2014 WUDAOSOFT.COM
 * Email:changsoul.wu@gmail.com
 * QQ:275100589
 */

package com.wudaosoft.net.httpclient;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.sax.SAXSource;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.alibaba.fastjson.JSONObject;
import com.wudaosoft.net.utils.XmlReader;

/**
 * <p>
 * 微信HttpClient工具类
 * </p>
 * HttpClient版本:4.5.2
 * 
 * @author Changsoul.Wu
 * @date 2014-3-29 下午6:10:49
 */
public class HttpClientUtils {

	private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

	public static final ContentType UTF8_TEXT_PLAIN_CONTENT_TYPE = ContentType.create("text/plain", Consts.UTF_8);
	public static final ContentType DEFAULT_BINARY_CONTENT_TYPE = ContentType.create("application/octet-stream", Consts.UTF_8);

	private static final String ACCEPT_LANGUAGE = "zh-CN,zh;q=0.8";
	private static final String USER_AGENT = "Wudaosoft WebKit/1.0";
	public static final String JSON_CONTENT_TYPE = "application/json; encoding=utf-8";
	public static final String XML_CONTENT_TYPE = "application/xml; encoding=utf-8";
	public static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain; encoding=utf-8";
	private static final int TIME_OUT = 15 * 1000;

	private static PoolingHttpClientConnectionManager connManager = null;
	private static CloseableHttpClient httpClient = null;

	public static CloseableHttpClient getHttpClient() {

		if (httpClient == null) {

			try {
				ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

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
						HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
						if ("file.api.weixin.qq.com".equalsIgnoreCase(target.getHostName())) {
							// Keep alive for 5 seconds only
							return 3 * 1000;
						} else {
							// otherwise keep alive for 30 seconds
							return 30 * 1000;
						}
					}

				};

				HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

					public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
						if (executionCount >= 3) {
							// 如果已经重试了3次，就放弃
							return false;
						}
						if (exception instanceof InterruptedIOException) {
							// 超时
							return false;
						}
						if (exception instanceof UnknownHostException) {
							// 目标服务器不可达
							return false;
						}
						if (exception instanceof ConnectTimeoutException) {
							// 连接被拒绝
							return false;
						}
						if (exception instanceof SSLException) {
							// ssl握手异常
							return false;
						}
						HttpClientContext clientContext = HttpClientContext.adapt(context);
						HttpRequest request = clientContext.getRequest();
						boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
						if (idempotent) {
							// 如果请求是幂等的，就再次尝试
							return true;
						}
						return false;
					}

				};

				// Trust own CA and all self-signed certs
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				// Allow TLSv1 protocol only

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

				sslcontext.init(null, trustAllCerts, null);

				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
						NoopHostnameVerifier.INSTANCE);

				Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
						.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE)
						.register("https", sslsf).build();

				connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
				// 将最大连接数增加到200
				connManager.setMaxTotal(200);
				// 将每个路由基础的连接增加到20
				connManager.setDefaultMaxPerRoute(50);
				// 将目标主机的最大连接数增加到100
				HttpHost weiXinApiHost = new HttpHost("api.weixin.qq.com", 443);
				connManager.setMaxPerRoute(new HttpRoute(weiXinApiHost), 100);
				// connManager.setValidateAfterInactivity(2000);

				// Create socket configuration
				SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
				connManager.setDefaultSocketConfig(socketConfig);

				// Create connection configuration
				ConnectionConfig connectionConfig = ConnectionConfig.custom()
						.setMalformedInputAction(CodingErrorAction.IGNORE)
						.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();
				connManager.setDefaultConnectionConfig(connectionConfig);

				// Use custom cookie store if necessary.
				// CookieStore cookieStore = new BasicCookieStore();
				// Create global request configuration
				RequestConfig defaultRequestConfig = RequestConfig.custom()
						// .setCookieSpec(CookieSpecs.BEST_MATCH)
						.setExpectContinueEnabled(false)
						// .setStaleConnectionCheckEnabled(true)
						.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
						.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
						.setConnectionRequestTimeout(TIME_OUT).setConnectTimeout(TIME_OUT).setSocketTimeout(TIME_OUT)
						.build();

				HttpRequestInterceptor requestInterceptor = new HttpRequestInterceptor() {

					public void process(final HttpRequest request, final HttpContext context)
							throws HttpException, IOException {
						if (!request.containsHeader(HttpHeaders.ACCEPT_ENCODING)) {
							request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
						}

						request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANGUAGE);
						request.addHeader(HttpHeaders.USER_AGENT, USER_AGENT);

					}
				};

				HttpResponseInterceptor gizpResponseInterceptor = new HttpResponseInterceptor() {

					public void process(final HttpResponse response, final HttpContext context)
							throws HttpException, IOException {
						HttpEntity entity = response.getEntity();
						if (entity != null) {
							Header ceheader = entity.getContentEncoding();
							if (ceheader != null) {
								HeaderElement[] codecs = ceheader.getElements();
								for (int i = 0; i < codecs.length; i++) {
									if (codecs[i].getName().equalsIgnoreCase("gzip")) {
										response.setEntity(new GzipDecompressingEntity(response.getEntity()));
										return;
									}
								}
							}
						}
					}
				};

				httpClient = HttpClients.custom().setConnectionManager(connManager).setKeepAliveStrategy(myStrategy)
						// .setDefaultCookieStore(cookieStore)
						.setDefaultRequestConfig(defaultRequestConfig).setRetryHandler(myRetryHandler)
						.addInterceptorFirst(requestInterceptor).addInterceptorFirst(gizpResponseInterceptor).build();

			} catch (KeyManagementException e) {
				log.error(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage(), e);
			}
		}

		return httpClient;
	}

	/**
	 * GET提交数据,并返回JSON格式的结果数据
	 * 
	 * @param url
	 *            请求URL
	 * @return JSONObject or null if error or no response
	 */
	public static JSONObject getForJsonResult(String reqUrl) {

		return getForJsonResult(reqUrl, null);
	}

	/**
	 * GET提交数据,并返回JSON格式的结果数据
	 * 
	 * @param url
	 *            请求URL
	 * @param 请求参数MAP
	 * @return JSONObject or null if error or no response
	 */
	public static JSONObject getForJsonResult(String reqUrl, Map<String, String> params) {
		try {
			reqUrl = buildReqUrl(reqUrl, params);

			return getHttpClient().execute(new HttpGet(reqUrl), new JsonResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * GET提交并返回XML数据
	 * 
	 * @param url
	 *            请求URL
	 * @return org.w3c.dom.Document or null
	 */
	public static Document getForXmlResult(String reqUrl) {

		return getForXmlResult(reqUrl, null);
	}

	/**
	 * GET提交并返回XML数据
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数MAP
	 * @return org.w3c.dom.Document or null
	 */
	public static Document getForXmlResult(String reqUrl, Map<String, String> params) {
		try {
			reqUrl = buildReqUrl(reqUrl, params);

			return getHttpClient().execute(new HttpGet(reqUrl), new XmlResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * GET提交并返回String数据
	 * 
	 * @param url
	 *            请求URL
	 * @return String or null
	 */
	public static String getForStringResult(String reqUrl) {

		return getForStringResult(reqUrl, null);
	}

	/**
	 * GET提交并返回String数据
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数MAP
	 * @return String or null
	 */
	public static String getForStringResult(String reqUrl, Map<String, String> params) {
		try {
			reqUrl = buildReqUrl(reqUrl, params);

			return getHttpClient().execute(new HttpGet(reqUrl), new StringResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * POST提交数据,并返回JSON格式的结果数据
	 * 
	 * @param url
	 *            请求URL
	 * @return JSONObject or null if error or no response
	 */
	public static JSONObject postForJsonResult(String url) {

		return postForJsonResult(url, null);
	}

	/**
	 * POST提交数据,并返回JSON格式的结果数据
	 * 
	 * @param url
	 *            请求URL
	 * @param 请求参数MAP
	 * @return JSONObject or null if error or no response
	 */
	public static JSONObject postForJsonResult(String url, Map<String, String> params) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (params != null) {
				post.setEntity(buildUrlEncodedFormEntity(params));
			}
			return getHttpClient().execute(post, new JsonResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * POST提交JSON数据,并返回JSON格式的结果数据
	 * 
	 * @param url
	 *            请求URL
	 * @param jsonDataStr
	 *            jsonData String
	 * @return JSONObject or null if error or no response
	 */
	public static JSONObject postJsonDataForJsonResult(String url, String jsonDataStr) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (jsonDataStr != null) {
				StringEntity reqEntity = new StringEntity(jsonDataStr, Consts.UTF_8);
				reqEntity.setContentType(JSON_CONTENT_TYPE);
				post.setEntity(reqEntity);
			}
			return getHttpClient().execute(post, new JsonResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static String postDataForStringResult(String url, String data) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (data != null) {
				StringEntity reqEntity = new StringEntity(data, Consts.UTF_8);
				reqEntity.setContentType(TEXT_PLAIN_CONTENT_TYPE);
				post.setEntity(reqEntity);
			}
			return getHttpClient().execute(post, new StringResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static SAXSource postXmlForSAXSource(String url, String data) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (data != null) {
				StringEntity reqEntity = new StringEntity(data, Consts.UTF_8);
				reqEntity.setContentType(XML_CONTENT_TYPE);
				post.setEntity(reqEntity);
			}
			return getHttpClient().execute(post, new SAXSourceResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static <T> T postXmlForUnmarshalResult(String url, String data, Class<? extends T> clazz) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (data != null) {
				StringEntity reqEntity = new StringEntity(data, Consts.UTF_8);
				reqEntity.setContentType(XML_CONTENT_TYPE);
				post.setEntity(reqEntity);
			}

			CloseableHttpResponse response = getHttpClient().execute(post);

			int status = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			T result = null;
			try {
				if (status < 200 || status >= 300) {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}

				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}

				result = XmlReader.readFromInputStream(clazz, entity.getContent());
			} finally {
				try {
					EntityUtils.consume(entity);
				} catch (Exception e) {
				}

				response.close();
			}

			return result;
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static <T> T postXmlForUnmarshalResult(String url, Map<String, String> params, Class<? extends T> clazz) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (params != null) {
				post.setEntity(buildUrlEncodedFormEntity(params));
			}

			CloseableHttpResponse response = getHttpClient().execute(post);

			int status = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			T result = null;
			try {
				if (status < 200 || status >= 300) {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}

				if (entity == null) {
					throw new ClientProtocolException("Response contains no content");
				}

				result = XmlReader.readFromInputStream(clazz, entity.getContent());
			} finally {
				try {
					EntityUtils.consume(entity);
				} catch (Exception e) {
				}

				response.close();
			}

			return result;
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * POST提交并返回XML数据
	 * 
	 * @param url
	 *            请求URL
	 * @return org.w3c.dom.Document or null
	 */
	public static Document postForXmlResult(String url) {

		return postForXmlResult(url, null);
	}

	/**
	 * POST提交并返回XML数据
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数MAP
	 * @return org.w3c.dom.Document or null
	 */
	public static Document postForXmlResult(String url, Map<String, String> params) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (params != null) {
				post.setEntity(buildUrlEncodedFormEntity(params));
			}
			return getHttpClient().execute(post, new XmlResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * POST提交并返回String数据
	 * 
	 * @param url
	 *            请求URL
	 * @return String or null
	 */
	public static String postForStringResult(String url) {

		return postForStringResult(url, null);
	}

	/**
	 * POST提交并返回String数据
	 * 
	 * @param url
	 *            请求URL
	 * @param params
	 *            请求参数MAP
	 * @return String or null
	 */
	public static String postForStringResult(String url, Map<String, String> params) {
		try {
			HttpPost post = new HttpPost(buildReqUrl(url));

			if (params != null) {
				post.setEntity(buildUrlEncodedFormEntity(params));
			}
			return getHttpClient().execute(post, new StringResponseHandler());
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 上传微信媒体文件
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static String postWeiXinMedia(String url, File file) {
		return postMultipartForm(url, "media", file);
	}

	/**
	 * 文件上传
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static String postMultipartForm(String url, File file) {
		return postMultipartForm(url, "upFile", file);
	}

	/**
	 * 文件上传
	 * 
	 * @param url
	 * @param keyName
	 * @param file
	 * @return
	 */
	public static String postMultipartForm(String url, String keyName, File file) {

		return postMultipartForm(url, keyName, file, null);
	}

	/**
	 * 文件上传
	 * 
	 * @param url
	 * @param keyName
	 * @param file
	 * @param fileName
	 * @return
	 */
	public static String postMultipartForm(String url, String keyName, File file, String fileName) {

		return postMultipartForm(url, keyName, file, fileName, null);
	}

	/**
	 * 文件上传(使用表单)
	 * 
	 * @param url
	 *            请求URL
	 * @param keyName
	 *            上传表单的文件域名称
	 * @param file
	 *            要上传的文件
	 * @param fileName
	 *            文件名
	 * @param params
	 *            请求参数MAP
	 * @return String or null
	 */
	public static String postMultipartForm(String url, String keyName, File file, String fileName,
			Map<String, String> params) {

		if (file == null || !file.isFile() || !file.canRead()) {
			log.error("File is null or file can't read!");
			return null;
		}

		if (keyName == null) {
			log.error("keyName is null!");
			return null;
		}

		try {
			HttpPost httpPost = new HttpPost(buildReqUrl(url));

			if (fileName == null)
				fileName = file.getName();

			FileBody bin = new FileBody(file, DEFAULT_BINARY_CONTENT_TYPE, fileName);

			MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();

			reqEntity.addPart(keyName, bin);
			// reqEntity.addPart("filename", new StringBody(file.getName(),
			// UTF8_TEXT_PLAIN_CONTENT_TYPE));
			// reqEntity.addTextBody("filelength", file.length()+"");

			if (params != null) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					String value = entry.getValue();

					if (value != null)
						reqEntity.addPart(entry.getKey(), new StringBody(value, UTF8_TEXT_PLAIN_CONTENT_TYPE));
				}
			}

			httpPost.setEntity(reqEntity.build());

			CloseableHttpResponse response = getHttpClient().execute(httpPost);

			try {
				HttpEntity resEntity = response.getEntity();

				String resp = null;

				if (resEntity != null) {
					resp = EntityUtils.toString(resEntity, Consts.UTF_8);
				}
				EntityUtils.consume(resEntity);

				return resp;
			} finally {
				response.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	public static String buildReqUrl(String reqUrl) {

		return buildReqUrl(reqUrl, null);
	}

	public static String buildReqUrl(String reqUrl, Map<String, String> params) {
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

	public static UrlEncodedFormEntity buildUrlEncodedFormEntity(Map<String, String> params)
			throws ClientProtocolException {
		if (params == null)
			throw new ClientProtocolException("Params is null");

		List<NameValuePair> parameters = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {
			parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return new UrlEncodedFormEntity(parameters, Consts.UTF_8);
	}

	/**
	 * 关闭过期连接
	 */
	public static void closeExpiredConnections() {
		if (connManager != null) {
			// Close expired connections
			connManager.closeExpiredConnections();
			// Optionally, close connections
			// that have been idle longer than 5 sec
			connManager.closeIdleConnections(39, TimeUnit.SECONDS);
		}
	}

}
