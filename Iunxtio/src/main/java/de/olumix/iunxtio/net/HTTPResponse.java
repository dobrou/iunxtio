/**
 * Copyright 2014 Dr. Holger Kremmin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License") for any non comercial use;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 * For any commercial use please obtain a license from Dr. Holger Kremmin (holger.kremmin@gmail.com)
 */
package de.olumix.iunxtio.net;

/**
 * @author hkremmin
 *
 */
public class HTTPResponse {
	
private int responseCode = -1;
private String message = null; //the HTTP response Message
private String body = null; // only the body of the HTTP message with removed HTML header elements

	/**
	 * 
	 */
	public HTTPResponse(int code, String m) {
		responseCode = code;
		message = m;
	}
	
	public int getReturnCode() {
		return responseCode;
	}
	
	public boolean isOK() {
		if (responseCode == 200) return true; else return false;
	}
	
	public String getBody() {
		return message;
	}

}
