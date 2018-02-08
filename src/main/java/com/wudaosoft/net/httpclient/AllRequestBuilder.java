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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HTTP;

/**
 * @author Changsoul Wu
 * 
 */
class AllRequestBuilder {

	public static HttpUriRequest build(RequestBuilder builder) {

		final HttpRequestBase result;
		URI uriNotNull = builder.getUri() != null ? builder.getUri() : URI.create("/");
		Charset charset = builder.getCharset();
		charset = charset != null ? charset : HTTP.DEF_CONTENT_CHARSET;
		String method = builder.getMethod();
		List<NameValuePair> parameters = builder.getParameters();
		HttpEntity entityCopy = builder.getEntity();

		if (parameters != null && !parameters.isEmpty()) {
			if (entityCopy == null
					&& (HttpPost.METHOD_NAME.equalsIgnoreCase(method) || HttpPut.METHOD_NAME.equalsIgnoreCase(method)
							|| HttpPatch.METHOD_NAME.equalsIgnoreCase(method))) {
				entityCopy = new UrlEncodedFormEntity(parameters, charset);
			} else {
				try {
					uriNotNull = new URIBuilder(uriNotNull)
							.setCharset(charset)
							.addParameters(parameters)
							.build();
				} catch (final URISyntaxException ex) {
					// should never happen
				}
			}
		}

		if (entityCopy == null) {
			result = new InternalRequest(method);
		} else {
			final InternalEntityEclosingRequest request = new InternalEntityEclosingRequest(method);
			request.setEntity(entityCopy);
			result = request;
		}
		result.setProtocolVersion(builder.getVersion());
		result.setURI(uriNotNull);
		// if (builder.headergroup != null) {
		// result.setHeaders(builder.headergroup.getAllHeaders());
		// }
		result.setConfig(builder.getConfig());
		return result;
	}

	static class InternalRequest extends HttpRequestBase {

		private final String method;

		InternalRequest(final String method) {
			super();
			this.method = method;
		}

		@Override
		public String getMethod() {
			return this.method;
		}

	}

	static class InternalEntityEclosingRequest extends HttpEntityEnclosingRequestBase {

		private final String method;

		InternalEntityEclosingRequest(final String method) {
			super();
			this.method = method;
		}

		@Override
		public String getMethod() {
			return this.method;
		}

	}
}
