/* 
 * Copyright(c)2010-2018 WUDAOSOFT.COM
 * 
 * Email:changsoul.wu@gmail.com
 * 
 * QQ:275100589
 */ 
 
package com.wudaosoft.net.utils;

import java.io.Serializable;


/** 
 * @author Changsoul Wu
 * 
 */
public class MediaType implements Serializable {

	private static final long serialVersionUID = -3339482597298033034L;

	/**
	 * A String equivalent of {@link MediaType#ALL}.
	 */
	public static final String ALL_VALUE = "*/*";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_ATOM_XML}.
	 */
	public final static String APPLICATION_ATOM_XML_VALUE = "application/atom+xml";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_FORM_URLENCODED}.
	 */
	public final static String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_JSON}.
	 * @see #APPLICATION_JSON_UTF8_VALUE
	 */
	public final static String APPLICATION_JSON_VALUE = "application/json";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_JSON_UTF8}.
	 */
	public final static String APPLICATION_JSON_UTF8_VALUE = APPLICATION_JSON_VALUE + ";charset=UTF-8";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_OCTET_STREAM}.
	 */
	public final static String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_PDF}.
	 * @since 4.3
	 */
	public final static String APPLICATION_PDF_VALUE = "application/pdf";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_RSS_XML}.
	 * @since 4.3.6
	 */
	public final static String APPLICATION_RSS_XML_VALUE = "application/rss+xml";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_XHTML_XML}.
	 */
	public final static String APPLICATION_XHTML_XML_VALUE = "application/xhtml+xml";

	/**
	 * A String equivalent of {@link MediaType#APPLICATION_XML}.
	 */
	public final static String APPLICATION_XML_VALUE = "application/xml";

	/**
	 * A String equivalent of {@link MediaType#IMAGE_GIF}.
	 */
	public final static String IMAGE_GIF_VALUE = "image/gif";

	/**
	 * A String equivalent of {@link MediaType#IMAGE_JPEG}.
	 */
	public final static String IMAGE_JPEG_VALUE = "image/jpeg";

	/**
	 * A String equivalent of {@link MediaType#IMAGE_PNG}.
	 */
	public final static String IMAGE_PNG_VALUE = "image/png";

	/**
	 * A String equivalent of {@link MediaType#MULTIPART_FORM_DATA}.
	 */
	public final static String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

	/**
	 * A String equivalent of {@link MediaType#TEXT_EVENT_STREAM}.
	 * @since 4.3.6
	 */
	public final static String TEXT_EVENT_STREAM_VALUE = "text/event-stream";

	/**
	 * A String equivalent of {@link MediaType#TEXT_HTML}.
	 */
	public final static String TEXT_HTML_VALUE = "text/html";

	/**
	 * A String equivalent of {@link MediaType#TEXT_MARKDOWN}.
	 * @since 4.3
	 */
	public final static String TEXT_MARKDOWN_VALUE = "text/markdown";

	/**
	 * A String equivalent of {@link MediaType#TEXT_PLAIN}.
	 */
	public final static String TEXT_PLAIN_VALUE = "text/plain";

	/**
	 * A String equivalent of {@link MediaType#TEXT_XML}.
	 */
	public final static String TEXT_XML_VALUE = "text/xml";

}
