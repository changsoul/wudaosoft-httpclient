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

/** 
 * @author Changsoul Wu
 * 
 */
public class XmlException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3472079661190010563L;

	/**
	 * 
	 */
	public XmlException() {
	}

	/**
	 * @param message
	 */
	public XmlException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public XmlException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public XmlException(String message, Throwable cause) {
		super(message, cause);
	}

}
