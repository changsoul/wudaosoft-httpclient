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
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Changsoul.Wu
 */
public class XmlResponseHandler implements ResponseHandler<Document> {

	@Override
	public Document handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		
        if (status < 200 || status >= 300) {
        	throw new ClientProtocolException("Unexpected response status: " + status);
        }
        
        if (entity == null) {
            throw new ClientProtocolException("Response contains no content");
        }
        
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        dbfac.setIgnoringElementContentWhitespace(true);
        dbfac.setCoalescing(true);
        dbfac.setIgnoringComments(true);
        try {
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            ContentType contentType = ContentType.getOrDefault(entity);
//            if (!contentType.equals(ContentType.APPLICATION_XML)) {
//                throw new ClientProtocolException("Unexpected content type:" +
//                    contentType);
//            }
            Charset charset = contentType.getCharset();
            if (charset == null) {
                charset = Consts.UTF_8;
            }
            return docBuilder.parse(entity.getContent(), charset.name());
        } catch (ParserConfigurationException ex) {
            throw new IllegalStateException(ex);
        } catch (SAXException ex) {
            throw new ClientProtocolException("Malformed XML document", ex);
        }
	}

}
