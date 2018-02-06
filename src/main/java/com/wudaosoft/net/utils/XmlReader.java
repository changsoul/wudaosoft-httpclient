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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.wudaosoft.net.xml.XmlException;

/** 
 * @author Changsoul Wu
 * 
 */
public class XmlReader {
	
	private static final ConcurrentMap<Class<?>, JAXBContext> jaxbContexts = new ConcurrentHashMap<Class<?>, JAXBContext>(64);
	
	private static final Logger logger = LoggerFactory.getLogger(XmlReader.class);
	
	public static <T> T readFromXmlString(Class<? extends T> clazz, String xml) throws IOException {
		return readFromSource(clazz, new StreamSource(new StringReader(xml)));
	}
	
	public static <T> T readFromInputStream(Class<? extends T> clazz, InputStream inputStream) throws IOException {
		return readFromSource(clazz, new StreamSource(inputStream));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T readFromSource(Class<? extends T> clazz, Source source) throws IOException {
		try {
			source = processSource(source);
			Unmarshaller unmarshaller = createUnmarshaller(clazz);
			if (clazz.isAnnotationPresent(XmlRootElement.class)) {
				return (T)unmarshaller.unmarshal(source);
			}
			else {
				JAXBElement<?> jaxbElement = unmarshaller.unmarshal(source, clazz);
				return (T)jaxbElement.getValue();
			}
		}
		catch (NullPointerException ex) {
			throw new XmlException("NPE while unmarshalling. " +
					"This can happen on JDK 1.6 due to the presence of DTD " +
					"declarations, which are disabled.", ex);
		}
		catch (UnmarshalException ex) {
			throw new XmlException("Could not unmarshal to [" + clazz + "]: " + ex.getMessage(), ex);
			
		}
		catch (JAXBException ex) {
			throw new XmlException("Could not instantiate JAXBContext: " + ex.getMessage(), ex);
		}
	}

	protected static Source processSource(Source source) {
		if (source instanceof StreamSource) {
			StreamSource streamSource = (StreamSource) source;
			InputSource inputSource = new InputSource(streamSource.getInputStream());
			try {
				XMLReader xmlReader = XMLReaderFactory.createXMLReader();
				xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				String featureName = "http://xml.org/sax/features/external-general-entities";
				xmlReader.setFeature(featureName, false);
				xmlReader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
				return new SAXSource(xmlReader, inputSource);
			}
			catch (SAXException ex) {
				logger.warn("Processing of external entities could not be disabled", ex);
				return source;
			}
		}
		else {
			return source;
		}
	}
	
	/**
	 * Create a new {@link Unmarshaller} for the given class.
	 * @param clazz the class to create the unmarshaller for
	 * @return the {@code Unmarshaller}
	 * @throws XmlException in case of JAXB errors
	 */
	protected static final Unmarshaller createUnmarshaller(Class<?> clazz) throws JAXBException {
		try {
			JAXBContext jaxbContext = getJaxbContext(clazz);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			customizeUnmarshaller(unmarshaller);
			return unmarshaller;
		}
		catch (JAXBException ex) {
			throw new XmlException(
					"Could not create Unmarshaller for class [" + clazz + "]: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Customize the {@link Unmarshaller} created by this
	 * message converter before using it to read the object from the input.
	 * @param unmarshaller the unmarshaller to customize
	 * @since 4.0.3
	 * @see #createUnmarshaller(Class)
	 */
	protected static void customizeUnmarshaller(Unmarshaller unmarshaller) {
	}
	
	/**
	 * Return a {@link JAXBContext} for the given class.
	 * @param clazz the class to return the context for
	 * @return the {@code JAXBContext}
	 * @throws XmlException in case of JAXB errors
	 */
	protected static final JAXBContext getJaxbContext(Class<?> clazz) {
		JAXBContext jaxbContext = jaxbContexts.get(clazz);
		if (jaxbContext == null) {
			try {
				jaxbContext = JAXBContext.newInstance(clazz);
				jaxbContexts.putIfAbsent(clazz, jaxbContext);
			}
			catch (JAXBException ex) {
				throw new XmlException(
						"Could not instantiate JAXBContext for class [" + clazz + "]: " + ex.getMessage(), ex);
			}
		}
		return jaxbContext;
	}
	
	private static final EntityResolver NO_OP_ENTITY_RESOLVER = new EntityResolver() {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return new InputSource(new StringReader(""));
		}
	};
}
