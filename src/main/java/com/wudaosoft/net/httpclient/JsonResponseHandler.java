/* Copyright(c)2010-2014 WUDAOSOFT.COM
 * Email:changsoul.wu@gmail.com
 * QQ:275100589
 */ 
 
package com.wudaosoft.weixin.httpclient;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * <p> </p>
 * @author Changsoul.Wu
 * @param <T>
 * @date 2014-3-29 下午8:48:41
 */
public class JsonResponseHandler implements ResponseHandler<JSONObject> {

	@Override
	public JSONObject handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            try {
            	return entity != null ? JSON.parseObject(EntityUtils.toString(entity, Consts.UTF_8)) : null;
			} catch (JSONException e) {
				throw new ClientProtocolException("Json format error: " + e.getMessage());
			}
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
	}

}
