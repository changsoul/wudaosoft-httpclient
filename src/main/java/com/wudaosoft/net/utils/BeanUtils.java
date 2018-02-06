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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Changsoul.Wu
 */
public class BeanUtils {

	/**
	 * @param map
	 * @param destClazz
	 * @return
	 */
	public static <T> T convertMap2Bean(Map<String, Object> map,	Class<T> destClazz) {
		return convertMap2Bean(map, destClazz, false);
	}

	/**
	 * @param map
	 * @param destClazz
	 * @param isFirstUpperCase
	 * @return
	 */
	public static <T> T convertMap2Bean(Map<String, Object> map, Class<T> destClazz, boolean isFirstUpperCase) {

		try {
			T dest = destClazz.newInstance();
			BeanInfo beanInfo = Introspector.getBeanInfo(destClazz);

			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();

			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();

				if (isFirstUpperCase)
					key = toUpperFirstCase(key);

				if (map.containsKey(key)) {
					try {
						Object value = map.get(key);
						
						Method setter = property.getWriteMethod();
						
						if(setter != null) {
							setter.invoke(dest, value);
						}
					} catch (Exception e) {
					}
				}
			}
			return dest;
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IntrospectionException e1) {
			e1.printStackTrace();
		}

		return null;
	}

	/**
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> convertBean2Map(Object obj) {
		return convertBean2Map(obj, false);
	}

	/**
	 * @param obj
	 * @param isFirstUpperCase
	 * @return
	 */
	public static Map<String, Object> convertBean2Map(Object obj, boolean isFirstUpperCase) {
		if (obj == null)
			return null;
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());

			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			
			Map<String, Object> map = new HashMap<String, Object>(propertyDescriptors.length -1);
			
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();

				if (isFirstUpperCase)
					key = toUpperFirstCase(key);

				if (!key.equals("class")) {
					try {
						Object value = property.getReadMethod().invoke(obj);
						map.put(key, value);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
			
			return map;
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @param str
	 * @return
	 */
	public static String toUpperFirstCase(String str) {
		if (str == null)
			return null;
		return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
	}
}
