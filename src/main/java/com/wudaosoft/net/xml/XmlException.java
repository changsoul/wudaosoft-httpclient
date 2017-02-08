/* 
 * Copyright(c)2010-2016 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
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
