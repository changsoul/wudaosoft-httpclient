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
package com.wudaosoft.net.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.wudaosoft.net.xml.XmlObject;

/**
 * @author Changsoul.Wu
 */
public class XmlUtils {
	private static final Logger Log = LoggerFactory.getLogger(XmlUtils.class);
	
	private static DocumentBuilderFactory dbFactory;
	
	public static Document readXml(InputStream in) {
		
		if(dbFactory == null){
			dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setIgnoringElementContentWhitespace(true);
			dbFactory.setCoalescing(true);
			dbFactory.setIgnoringComments(true);
		}
		
		if(in == null)
			return null;
		
		try {
			return dbFactory.newDocumentBuilder().parse(in);
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		}
		
		return null;
	}
	
	public static Document readXml(String xmltext) {
		
		if(dbFactory == null){
			dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setIgnoringElementContentWhitespace(true);
			dbFactory.setCoalescing(true);
			dbFactory.setIgnoringComments(true);
		}
		
		if(xmltext == null || xmltext.trim().equals(""))
			return null;
		
		try {
			StringReader sr = new StringReader(xmltext);
			InputSource is = new InputSource(sr);
			return dbFactory.newDocumentBuilder().parse(is);
		} catch (SAXException e) {
		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
		}
		
		return null;
	}
    
    public static String getVaue(Document doc, String key){
    	
    	if(doc == null){
    		Log.error("XML Document is null");
    		return null;
    	}
    	
    	Node node = getNode(doc, key);
    	
    	if(node != null){
    		
    		return node.getFirstChild().getNodeValue();
    	}
    	
    	return null;
    }
    
    public static NamedNodeMap getAttributes(Document doc, String key){
    	
    	if(doc == null){
    		Log.error("XML Document is null");
    		return null;
    	}
    	
    	Node node = getNode(doc, key);
    	
    	if(node != null){
    		
    		return node.getAttributes();
    	}
    	
    	return null;
    }
    
    public static String getAttribute(NamedNodeMap n, String key){
    	
    	if(n == null){
    		return null;
    	}
    	
    	Node node = n.getNamedItem(key);
    	
    	if(node != null){
    		return node.getNodeValue();
    	}
    	
    	return null;
    }

    public static String getAttribute(Document doc, String key, String key2){
    	
    	if(doc == null){
    		return null;
    	}
    	
    	return getAttribute(getAttributes(doc, key), key2);
    }
    
    private static Node getNode(Document doc, String key){
    	
    	if(doc == null){
    		Log.error("XML Document is null");
    		return null;
    	}
    	
    	NodeList nList = doc.getElementsByTagName(key);
    	
    	if(nList.getLength() > 0){
    		
    		Node node = nList.item(0);
    		return node;
    	}
    	
    	return null;
    }
    
    public static String toString(Document doc) {
    	Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    	StreamResult result = new StreamResult(new StringWriter());
	    	DOMSource source = new DOMSource(doc);
	    	transformer.transform(source, result);
	    	String xmlString = result.getWriter().toString();
	    	return xmlString.toString();
		} catch (TransformerConfigurationException e) {
			Log.error(e.getMessage(), e);
		} catch (TransformerFactoryConfigurationError e) {
			Log.error(e.getMessage(), e);
		} catch (TransformerException e) {
			Log.error(e.getMessage(), e);
		}
		return null;
    }
    
    public static Map<String, Object> convertNodeToMap(Node node){
		NodeList nodeList = node.getChildNodes();
    	
    	Map<String, Object> map = new HashMap<String, Object>(nodeList.getLength());	
		
		for(int i = 0; i < nodeList.getLength(); i++){
			Node nodec = nodeList.item(i);
			
			String key = nodec.getNodeName();
			
			Object value = null;
			if(nodec.hasChildNodes()){
				
				NodeList nodeListc = nodec.getChildNodes();
				if(nodeListc.getLength() == 1){
					Node noded = nodeListc.item(0);
					
					short type = noded.getNodeType();
					
					if(type == 3 || type == 4){
						value = noded.getNodeValue();
					}
					
					if(noded.getNodeType() == 1){
						value = convertNodeToMap(nodec);
					}
				}else{
					value = convertNodeToMap(nodec);
				}
			}
			
			map.put(key, value);
		}
		
		return map;
    }
	
    public static String append(String key, Object value) {
		StringBuilder x = new StringBuilder();
		x.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
		return x.toString();
	}
	
    public static String appendCDATA(String key, Object value) {
		StringBuilder x = new StringBuilder();
		x.append("<").append(key).append("><![CDATA[").append(value).append("]]></").append(key).append(">");
		return x.toString();
	}
    
    public static String appendStartTag(String key) {
		StringBuilder x = new StringBuilder();
		x.append("<").append(key).append(">");
		return x.toString();
	}
    
    public static String appendEndTag(String key) {
		StringBuilder x = new StringBuilder();
		x.append("</").append(key).append(">");
		return x.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String buildString(Map<String, Object> mapc) {
		StringBuilder xx = new StringBuilder();
		
		for(Map.Entry<String, Object> entry : mapc.entrySet()){
			Object v = entry.getValue();
			
			if(v !=null){
			    if (v instanceof Map) {
					
					xx.append(append(entry.getKey(), buildString((Map<String, Object>)v)));
				}else if (v instanceof List<?>) {
					
					List<?> list = (List<?>)v;
					
					xx.append(appendStartTag(entry.getKey()));
					
					if(list.size() > 0) {
						Object type = list.get(0);
						
						if(type instanceof XmlObject){
							List<XmlObject> listXmlObject = (List<XmlObject>) list ;
							for(XmlObject x : listXmlObject){
								xx.append(buildString(x.buildMap()));
							}
						}else if(type instanceof Map){
							List<Map<String, Object>> listMapObject = (List<Map<String, Object>>) list ;
							for(Map<String, Object> m : listMapObject){
								xx.append(buildString(m));
							}
						}
					}
					
					xx.append(appendEndTag(entry.getKey()));
				}else if (isNumeric(v.toString())) {
					
					xx.append(append(entry.getKey(), v));
				} else {
					
					xx.append(appendCDATA(entry.getKey(), v));
				}
			}else{
				xx.append(append(entry.getKey(), ""));
			}
		}
		return xx.toString();
	}
	
	public static boolean isNumeric(String str) {
	     Pattern pattern = Pattern.compile("[0-9]*");
	     return pattern.matcher(str).matches();   
	}
    

}
