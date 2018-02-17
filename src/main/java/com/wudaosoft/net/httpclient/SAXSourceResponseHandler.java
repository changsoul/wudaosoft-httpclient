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
 * @author Changsoul.Wu
 */
public class SAXSourceResponseHandler implements ResponseHandler<SAXSource> {

	@Override
	public SAXSource handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		
        if (status < 200 || status >= 300) {
        	throw new ClientProtocolException("Unexpected response status: " + status);
        }
        
        HttpEntity entity = response.getEntity();
        
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
