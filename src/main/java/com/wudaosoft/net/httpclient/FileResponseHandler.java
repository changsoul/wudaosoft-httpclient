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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.Args;

/**
 * @author Changsoul.Wu
 */
public class FileResponseHandler implements ResponseHandler<File> {

	private File file;

	public FileResponseHandler(final File file) {
		Args.notNull(file, "file");
		Args.check(file.canWrite(), "file must be writeable");
		this.file = file;
	}

	@Override
	public File handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();

		if (status != 200) {
			throw new ClientProtocolException("Unexpected response status: " + status);
		}

		HttpEntity entity = response.getEntity();

		if (entity == null || !entity.isStreaming()) {
			throw new ClientProtocolException("Response contains no content");
		}

		InputStream inputStream = entity.getContent();
		FileOutputStream outputStream = new FileOutputStream(file);

		try {
			
			byte[] buff = new byte[2048];
			int size = -1;
			while ((size = inputStream.read(buff)) != -1) {

				outputStream.write(buff, 0, size);
			}

			return file;
			
		} finally {
			try {
				outputStream.close();
			}catch (IOException e) {
			}
		}
		
	}

}
