/* 
 * Copyright(c)2010-2018 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
 */ 
 
package com.wudaosoft.net.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;

/** 
 * @author Changsoul Wu
 * 
 */
public class NoResultResponseHandler implements ResponseHandler<Integer> {
	
	private ContentType contentType;
	
	public NoResultResponseHandler(ContentType contentType) {
		this.contentType = contentType;
	}
	
	public ContentType getContentType() {
		return contentType;
	}
	
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/* (non-Javadoc)
	 * @see org.apache.http.client.ResponseHandler#handleResponse(org.apache.http.HttpResponse)
	 */
	@Override
	public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		return response.getStatusLine().getStatusCode();
	}

}
