/**
 *    Copyright 2009-2017 Wudao Software Studio(wudaosoft.com)
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
import java.nio.charset.Charset;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Changsoul.Wu
 * @param <T>
 */
public class JsonResponseHandler implements ResponseHandler<JSONObject> {

	@Override
	public JSONObject handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            Charset charset = ContentType.getOrDefault(entity).getCharset();
			if (charset == null) {
				charset = Consts.UTF_8;
			}
            try {
            	return entity != null ? JSON.parseObject(EntityUtils.toString(entity, charset)) : new JSONObject();
			} catch (JSONException e) {
				throw new ClientProtocolException("Json format error: " + e.getMessage());
			}
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
	}

}
