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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.Args;

/**
 * @author changsoul.wu
 *
 */
public class OutputStreamResponseHandler implements ResponseHandler<Object> {

	private OutputStream out;
	
	public OutputStreamResponseHandler(OutputStream out) {
		this.out = out;
	}
	
	
	/**
	 * always return null.
	 */
	@Override
	public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		
		Args.notNull(out, "OutputStream");
		
		int status = response.getStatusLine().getStatusCode();

		if (status != 200) {
			throw new ClientProtocolException("Unexpected response status: " + status);
		}

		HttpEntity entity = response.getEntity();

		if (entity == null || !entity.isStreaming()) {
			throw new ClientProtocolException("Response contains no content");
		}

		InputStream inputStream = entity.getContent();

		try {
			
			byte[] buff = new byte[4096];
			int l = -1;
			while ((l = inputStream.read(buff)) != -1) {

				out.write(buff, 0, l);
			}

			out.flush();
			
			return null;
		} finally {
			try {
				out.close();
			}catch (IOException e) {
			}
		}
	}

}
