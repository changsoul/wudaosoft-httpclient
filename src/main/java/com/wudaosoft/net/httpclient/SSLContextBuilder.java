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

import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.util.Args;

/**
 * @author changsoul.wu
 *
 */
public class SSLContextBuilder {

	private String password;
	
	private URL cert;

	/**
	 * @param password the password to set
	 */
	public SSLContextBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	/**
	 * @param cert the cert to set
	 */
	public SSLContextBuilder setCert(URL cert) {
		this.cert = cert;
		return this;
	}

	public SSLContext buildPKCS12() {

		Args.notEmpty(password, "password");
		Args.notNull(cert, "cert");

		char[] pwd = password.toCharArray();

		try {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			
			ks.load(cert.openStream(), pwd);

			// 实例化密钥库 & 初始化密钥工厂
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, pwd);

			// 创建 SSLContext
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
			
			return sslContext;
		} catch (Exception e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			throw new RuntimeException(e);
		}
	}

}
