/* Copyright(c)2010-2014 WUDAOSOFT.COM
 * Email:changsoul.wu@gmail.com
 * QQ:275100589
 */ 
 
package com.wudaosoft.net.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p> </p>
 * @author Changsoul.Wu
 * @date 2014-3-29 下午9:07:23
 */
public class SAXSourceResponseHandler implements ResponseHandler<SAXSource> {

	@Override
	public SAXSource handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		
        if (status < 200 || status >= 300) {
        	throw new ClientProtocolException("Unexpected response status: " + status);
        }
        
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
        
        return readSAXSource(entity.getContent());
	}
	
	private SAXSource readSAXSource(InputStream body) throws IOException {
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
			reader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
			return new SAXSource(reader, new InputSource(body));
		}
		catch (SAXException ex) {
			throw new ClientProtocolException("Could not parse document: " + ex.getMessage(), ex);
		}
	}
	
	private static final EntityResolver NO_OP_ENTITY_RESOLVER = new EntityResolver() {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return new InputSource(new StringReader(""));
		}
	};
}
