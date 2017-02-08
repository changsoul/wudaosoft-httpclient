/* Copyright(c)2010-2014 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
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
 * <p> </p>
 * 
 * @author Changsoul.Wu
 * @date 2014年3月31日 上午11:24:33
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
