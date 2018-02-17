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
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

/**
 * @author Changsoul.Wu
 */
public class ImageResponseHandler implements ResponseHandler<BufferedImage> {

	@Override
	public BufferedImage handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();

		if (status != 200) {
			throw new ClientProtocolException("Unexpected response status: " + status);
		}

		HttpEntity entity = response.getEntity();

		if (entity == null || !entity.isStreaming()) {
			throw new ClientProtocolException("Response contains no content");
		}

		BufferedImage buffImg = ImageIO.read(entity.getContent());
		return buffImg;
	}

}
