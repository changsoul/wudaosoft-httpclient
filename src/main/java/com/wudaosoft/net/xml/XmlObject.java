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
package com.wudaosoft.net.xml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import com.wudaosoft.net.utils.BeanUtils;
import com.wudaosoft.net.utils.XmlUtils;

/**
 * @author Changsoul.Wu
 */
public class XmlObject {
	
	private Map<String, Object> map = new HashMap<String, Object>(100);
	
	private XmlObject(){
		
	}
	
	/**
	 * 创建XmlObject对象
	 * @return XmlObject
	 */
	public static XmlObject create() {
		return new XmlObject();
	}
	
	/**
	 * 从XML文档生成XMLObject
	 * @param org.w3c.dom.Document
	 * @return XmlObject
	 */
	public static XmlObject fromDocument(Document doc){
		XmlObject obj = new XmlObject();
		
		if(doc != null){
			obj.fromMap(XmlUtils.convertNodeToMap(doc.getFirstChild()));
		}
		
		return obj;
	}
	
	/**
	 * @param obj
	 * @return XmlObject
	 */
	public static XmlObject fromBean(Object obj){
		XmlObject xobj = new XmlObject();
		
		Map<String, Object> mapTmp = BeanUtils.convertBean2Map(obj, false);
		
		if(mapTmp != null){
			xobj.fromMap(mapTmp);
		}
		
		return xobj;
	}
	
	/**
	 * @param obj
	 * @return XmlObject
	 */
	public static XmlObject fromUpperCaseBean(Object obj){
		XmlObject xobj = new XmlObject();
		
		Map<String, Object> mapTmp = BeanUtils.convertBean2Map(obj, true);
		
		if(mapTmp != null){
			xobj.fromMap(mapTmp);
		}
		
		return xobj;
	}
	
	/**
	 * 从map生成XML
	 * @param map
	 * @return XmlObject
	 */
	public XmlObject fromMap(Map<String, Object> map) {
		if(map != null)
			this.map = map;
		
		return this;
	}
	
	public XmlObject put(String key, Object value) {
		if(key != null){
			if(value instanceof XmlObject){
				map.put(key, ((XmlObject)value).buildMap());
			}else {
				map.put(key, value);
			}
		}
		
		return this;
	}
	
	public Object get(String key) {
		return map.get(key);
	}
	
	public String getString(String key) {
		Object v = map.get(key);
		
		if(v != null){
			return v.toString();
		}
		
		return null;
	}
	
	public int getInt(String key, int defaultValue) {
		Object v = map.get(key);
		if(v != null){
			try {
				return Integer.valueOf(v.toString()).intValue();
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}
	
	public long getLong(String key, long defaultValue) {
		Object v = map.get(key);
		if(v != null){
			try {
				return Long.valueOf(v.toString()).longValue();
			} catch (NumberFormatException e) {
			}
		}
		return defaultValue;
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		Object v = map.get(key);
		if(v != null){
			try {
				return Boolean.valueOf(v.toString()).booleanValue();
			} catch (Exception e) {
			}
		}
		return defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	public XmlObject getXmlObject(String key) {
		Object v = map.get(key);
		if(v != null && v instanceof Map){
			try {
				return XmlObject.create().fromMap((Map<String, Object>)v);
			} catch (Exception e) {
			}
		}
		return XmlObject.create();
	}
	
	public void remove(String key){
		map.remove(key);
	}
	
	/**
	 * 生成xml字符串
	 * @return
	 */
	public String build() {
		StringBuilder xml = new StringBuilder("<xml>");
		
		xml.append(XmlUtils.buildString(map));
		
		return xml.append("</xml>").toString();
	}
	
	/**
	 * 生成Map
	 * @return
	 */
	public Map<String, Object> buildMap() {
		return map;
	}
	
	/**
	 * 生成Bean
	 * @param destClazz
	 * @return
	 */
	public <T> T buildBean(Class<T> destClazz) {
		return BeanUtils.convertMap2Bean(map, destClazz);
	}
	
	/**
	 * @param destClazz
	 * @return
	 */
	public <T> T buildUpperCaseBean(Class<T> destClazz) {
		return BeanUtils.convertMap2Bean(map, destClazz, true);
	}
	
	public int size() {
		return map.size();
	}
	
	public void clear() {
		map = new HashMap<String, Object>();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return build();
	}
	
}
