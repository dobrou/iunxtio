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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;




/**
 * @author Holger Kremmin
 * (c) 2014
 * This class is used to establish and maintain all basic networking with the Lumix cam
 */

public class LumixNetwork {
	
private InetAddress cam_ip = null; //the camera ip adress
private InetAddress local_ip = null; //the local ip adress required for UDP Server


//----- HTTP ------------

private final String USER_AGENT = "Mozilla/5.0";

//Cam Info
private final String STATE ="cam.cgi?mode=getstate";
private final String CAPABILITIES = "cam.cgi?mode=getinfo&type=capability";
private final String LENSINFO ="cam.cgi?mode=getinfo&type=lens";

//Cam Network
private final String STARTSTREAM ="cam.cgi?mode=startstream&value=49199";


//Cam Commands
private final String RECMODE = "cam.cgi?mode=camcmd&value=recmode";
private final String SHUTTERSTART = "cam.cgi?mode=camcmd&value=capture";
private final String SHUTTERSTOP = "cam.cgi?mode=camcmd&value=capture_cancel";
private final String SETAPERTURE = "cam.cgi?mode=setsetting&type=focal&value=";


private final String INCREMENT = "%2F256";

//allowed external actions
public enum Actions {SINGLESHOT, APERTURE, SHUTTER, FOCUS, ZOOM}
public enum Capabilities {LENS, CAMERA}


private final int serverPort = 49199; //Port for live view images


//Cling
private LumixRegistryListener listener = null;
private UpnpService upnpService = null;
private DatagramSocket theSocket;
private LumixNetworkInfo info = null;



//---------------------------------------------------------

	/**
	 * Constructor method
	 *
	 */

	public LumixNetwork() {
		
		//our info object which stores basic information about rg. network
		info = new LumixNetworkInfo();
		
		// Create a new listener for UPNP  
		listener = new LumixRegistryListener(info);
		
        // This will create necessary network resources for UPnP right away
        System.out.println("Starting Cling...");
        upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
        // todo - only lookup the Lumix cams...
        upnpService.getControlPoint().search(new STAllHeader());

        // Let's wait 10 seconds for them to respond
        System.out.println("Waiting 10 seconds before shutting down...");
        try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}

	
	/**
	 * Lookup of the local IP adress. This adress is required to init the UDP Server
	 *
	 */
	public InetAddress lookUpLocalIp () {
		
		local_ip = null;
		try {
			local_ip = InetAddress.getLocalHost();
            theSocket = new DatagramSocket(serverPort);
			System.out.println("Local UDP Socket on IP address " + local_ip.getHostAddress() +" on port " + serverPort + " created");
		} catch (SocketException ExceSocket)
		{
			System.out.println("Socket creation error : "+ ExceSocket.getMessage());
		}
                catch(UnknownHostException e) {
			System.out.println("Cannot find an ip");
		}
		
		return local_ip;
		
	}
	
	
	/**
	 * The connect to the camera will be handled by the listener. So we will just check if the info object
	 * has the status connected. 
	 * @todo maybe we can remove this method
	 */
	public String lookUpLumixIp() throws IOException {
		
		
		while (!info.isConnected()) {
			
			//should do something more inteligent here....
			System.out.println("-->please connect the Camera!!");
		}
		cam_ip = info.getCam_ip();
		try {
			startStream();
			getState();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info.getCam_ip().getHostAddress();
		}
	
	
	
	// closing upnp etc.
	@Override 
	protected void finalize() throws Throwable {
	  try {
		  	// Release all resources and advertise BYEBYE to other UPnP devices
	        System.out.println("Stopping Cling...");
	        if (upnpService != null) {
	        	upnpService.shutdown();
	        }
	  }
	  finally {
	    super.finalize();
	  }
	}

//------------------- HTTP Networking ---------------------
	
	// HTTP GET request
		private HTTPResponse sendGet(String cmd) throws Exception {
	 
			//Construct the requeststring for the cmd and create an URL object
			String request = "http:/" + cam_ip.toString() + "/" + cmd;
			System.out.println("###########Url request = " + request);
			
			URL url = new URL(request);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
	 
			// we will send a GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());
			
			return new HTTPResponse(con.getResponseCode(), response.toString());
	 
		}
		
		public void startStream() throws Exception {
			 
			sendGet(STARTSTREAM);
	 
		}
	  
		public void getCapabilities() throws Exception {
			 
			sendGet(CAPABILITIES);
	 
		}
		
		public void getState() throws Exception {
			 
			sendGet(STATE);
	 
		}
		
		public void enableRecMode() throws Exception {
			sendGet(RECMODE);
			
		}
		
		public boolean doSingleShot() throws Exception{
			HTTPResponse respStart = null;
			HTTPResponse respStop = null;
			
			//perfom shot
			respStart = sendGet(SHUTTERSTART);
			//if ok we will send immediatly send an stop shutter
			if (respStart.isOK()) {
				respStop = sendGet(SHUTTERSTOP);
				if (!respStop.isOK()) return false;
				
			} else // something went wrong while sending the shutter start command
			{
				return false;
			}
			
			return true;
		}
		
		public boolean setAperture(int value) throws Exception{
			HTTPResponse resp = null;
			String tmp = this.SETAPERTURE + Integer.toString(value) + this.INCREMENT; 
			
			//perfom shot
			resp = sendGet(tmp);
			//if ok we will send immediatly send an stop shutter
			
			
			return resp.isOK();
		}
		
		
		public String getLensInfo() throws Exception{
			HTTPResponse resp = null;
			resp = sendGet(LENSINFO);
			
			return resp.getBody();
		}
		
			
		
		public boolean executeAction (Actions action, int value) throws Exception{
			
			boolean ok = false;
			
			switch (action) {
				case SINGLESHOT:
					System.out.println("Single Shot");
					ok = doSingleShot();
					break;
				case APERTURE:
					System.out.println("Aperture");
					ok = setAperture(value);
					break;
				case SHUTTER:
					System.out.println("Shutter");
					break;
				case ZOOM:
					System.out.println("Zoom");
					break;        
				case FOCUS:
					System.out.println("Focus");
					break;
			}
            return ok;
    }
		
		public String getCapabilities (Capabilities capa) throws Exception{
			String results = null;
			HTTPResponse resp = null;
			
			switch (capa) {
			case LENS:
				System.out.println("Getting Lens informatiom");
				results = getLensInfo();
				break;
			case CAMERA:
				System.out.println("Aperture");
				break;
			
			}
			return results;
			
		}
		
}
