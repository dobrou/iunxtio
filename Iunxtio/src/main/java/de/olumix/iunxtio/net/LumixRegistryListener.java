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
import java.net.UnknownHostException;

import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

/**
 * @author Holger Kremmin
 *
 */
public class LumixRegistryListener implements RegistryListener {

private final String MODEL = "LUMIX";
private LumixNetworkInfo info = null; //this class is used to store the ip of the lumix etc.	
	
	
	public LumixRegistryListener(LumixNetworkInfo _info) {
		info = _info;
		
	}

	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice device) {
		System.out.println(
				"Discovery started: " + device.getDisplayString()
				);
	}

	public void remoteDeviceDiscoveryFailed(Registry registry,
			RemoteDevice device,
			Exception ex) {
		System.out.println(
				"Discovery failed: " + device.getDisplayString() + " => " + ex
				);
	}

	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		
		DeviceDetails dd = device.getDetails();
		ManufacturerDetails md = dd.getManufacturerDetails();
		ModelDetails mod = dd.getModelDetails();
		
		UDN udn = null;
		
		//debug stuff ...
		System.out.println(
				"Remote device available: " + mod.getModelName() + " with Number " + mod.getModelNumber() + " and description " + mod.getModelDescription()
				);
		System.out.println(
				"ManufacturerURL: " + md.getManufacturerURI()
				);
		System.out.println(
				"UPC: " + dd.getUpc() + "PresentationURI " + dd.getPresentationURI() + "Base URL " + dd.getBaseURL()
				);
		
		System.out.println(
				"UDN: " + device.getIdentity().getUdn().toString()
				);
		
		System.out.println(
				"IP: " + device.getIdentity().getDescriptorURL().getHost()
				);
		// -------------------
		
		String ip = null;
		String model = null;
		String modelNumber = null;
		InetAddress cam_ip;
		
		if (device != null) {
			//get the model name - we expect Lumix here
			model= device.getDetails().getModelDetails().getModelName();
			//Camera type - for later purposes
			modelNumber= device.getDetails().getModelDetails().getModelNumber();
			
			if (model.equals(MODEL)) {
				System.out.println(
						"-->Found camera: " + model + " " + modelNumber
						);
				ip = device.getIdentity().getDescriptorURL().getHost();
				
				try {
					cam_ip = InetAddress.getByName(ip);
					
					//update the info object
					info.setCam_ip(cam_ip);
					info.setModel(model);
					info.setModelNumber(modelNumber);
					info.setConnected(true);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
			} else {
				System.out.println(
						"Found something else: " + model + " " + modelNumber 
						);
				
			}
		}
	}

	
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
		System.out.println(
				"Remote device updated: " + device.getDisplayString()
				);
	}

	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		System.out.println(
				"Remote device removed: " + device.getDisplayString()
				);
	}

	
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		//normally we should not care about any local devices
		System.out.println(
				"Local device added: " + device.getDisplayString()
				);
	}

	public void localDeviceRemoved(Registry registry, LocalDevice device) {
		//normally we should not care about any local devices
		System.out.println(
				"Local device removed: " + device.getDisplayString()
				);
	}

	public void beforeShutdown(Registry registry) {
		System.out.println(
				"Before shutdown, the registry has devices: "
						+ registry.getDevices().size()
				);
	}

	public void afterShutdown() {
		System.out.println("Shutdown of registry complete!");

	}

}
