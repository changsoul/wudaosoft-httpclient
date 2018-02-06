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

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author Changsoul.Wu
 */
public class JsonUtils {

	public static String getString(JSONObject json, String key) {
		if (json == null || key == null)
			return null;

		try {
			return json.getString(key);
		} catch (JSONException e) {
		}
		return null;
	}

	public static int getInt(JSONObject json, String key) {
		return getInt(json, key, -Integer.MAX_VALUE);
	}

	public static int getInt(JSONObject json, String key, int defaultValue) {
		int rs = defaultValue;

		if (json != null && key != null) {

			try {
				rs = json.getIntValue(key);
			} catch (JSONException e) {
			}
		}
		return rs;
	}

	public static boolean getBoolean(JSONObject json, String key) {
		return getBoolean(json, key, false);
	}

	public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
		boolean rs = defaultValue;

		if (json != null && key != null) {
			try {
				rs = json.getBoolean(key);
			} catch (JSONException e) {
			}
		}
		return rs;
	}
	
}
