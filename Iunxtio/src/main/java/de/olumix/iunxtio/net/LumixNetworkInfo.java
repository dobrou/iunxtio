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

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * @author imac
 * This class is used to store basic network information about the connected Lumix camera
 */

public class LumixNetworkInfo {
	
private static Logger log = Logger.getLogger(LumixNetworkInfo.class.getName());

private String model = null;
private String modelNumber = null;
private boolean connected = false;

	LumixNetworkInfo(InetAddress ip, String _model, String _modelNumber) {
		cam_ip = ip;
		model = _model;
		modelNumber = _modelNumber;
		if (cam_ip != null) {
			connected = true;
		}
	}
	
	LumixNetworkInfo() {
		
	}

	/**
	 * @return the cam_ip
	 */
	public InetAddress getCam_ip() {
		return cam_ip;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the modelNumber
	 */
	public String getModelNumber() {
		return modelNumber;
	}

	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * this will update the object in case a disconnect has occurred
	 */
	public void disconnect() {
		cam_ip = null;
		model = null;
		modelNumber = null;
		connected = false;	
		
		log.info("### disconnected");
	}
	
	private InetAddress cam_ip = null; //the camera ip adress
	/**
	 * @param cam_ip the cam_ip to set
	 */
	public void setCam_ip(InetAddress cam_ip) {
		this.cam_ip = cam_ip;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * @param modelNumber the modelNumber to set
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	/**
	 * @param connected the connected to set
	 */
	public void setConnected(boolean _connected) {
		connected = _connected;
		if ((cam_ip != null) && connected) {
			connected = true;
		} else {
			connected = false;
		}
		
	}

}
