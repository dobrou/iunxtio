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

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;

import de.olumix.iunxtio.util.LumixUtils.Focus;




/**
 * @author Holger Kremmin
 * (c) 2014
 * This class is used to establish and maintain all basic networking with the Lumix cam
 */

public class LumixNetwork {
	
private static Logger log = Logger.getLogger(LumixNetwork.class.getName());
	
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

//Focus
private final String FOCUS = "cam.cgi?mode=camctrl&type=focus&value=";
private final String NEARFAST = "wide-fast";
private final String NEAR = "wide-normal";
private final String FAR = "tele-normal";
private final String FARFAST = "tele-fast";


private final String INCREMENT = "%2F256";

//allowed external actions
public enum Actions {SINGLESHOT, APERTURE, SHUTTER, FOCUS, ZOOM}
public enum Capabilities {LENS, CAMERA}





//Cling
private LumixRegistryListener listener = null;
private UpnpService upnpService = null;

private LumixNetworkInfo info = null;

private Thread lookupThread = null;

//Image retrieval 

private BufferedImage bufferedImage = null;
private DatagramSocket liveViewSocket = null;
private final int serverPort = 49199; //Port for live view images

private DatagramPacket theRecievedPacket;
private byte[] outBuffer;
private byte[] inBuffer = new byte[30000];
private int timeouts=0;  //for handling of timeouts


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
        log.info("Starting Cling...");
        upnpService = new UpnpServiceImpl(listener);

        // Send a search message to all devices and services, they should respond soon
        // todo - only lookup the Lumix cams...
        upnpService.getControlPoint().search(new STAllHeader());

        // Let's wait 10 seconds for them to respond
        log.info("Waiting 10 seconds before shutting down...");
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}   
        
        //now staring the Lumix lookup thread, I don't want to stop the GUI init
        lookupThread = new Thread() {
			public void run() {
				try {
					lookUpLumixIp();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.info("starting the lookup thread failed!!!");
					log.info (e.toString());
				}
			};
		};	
		
		lookupThread.start();
	}

	
	
	/**
	 * returns whether the camera is connected or not
	 * @return connection
	 */
	
	public boolean isConnected() {
		return info.isConnected();
	}
	
	
	/**
	 * Just for displaying an info line
	 * @return the textual representation of the camera info
	 */
	public String getCameraInfoString() {
		return info.toString();
	}
	
	/**
	 * Lookup of the local IP adress. This adress is required to init the UDP Server
	 *
	 */
	public InetAddress lookUpLocalIp () {
		
		local_ip = null;
		
			try {
				local_ip = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				log.info("Can't retrieve local ip adress!");
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
			
			//should do something more intelligent here....
			//log.info("-->please connect the Camera!!");
			//System.out.print(".");
		}
		cam_ip = info.getCam_ip();
		try {
			startStream();
			getState();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
		return info.getCam_ip().getHostAddress();
		}
	
	
	
	// closing upnp etc.
	@Override 
	protected void finalize() throws Throwable {
	  try {
		  	// Release all resources and advertise BYEBYE to other UPnP devices
	        log.info("Stopping Cling...");
	        if (upnpService != null) {
	        	upnpService.shutdown();
	        }
	  }
	  finally {
	    super.finalize();
	  }
	}

//UDP, Image retrieval etc.
	
	
	
	/**
	 * This method is used to create the UDP socket for live view
	 * @return
	 */
	public boolean prepareLiveView() {
		
		boolean socketCreated = false;
		
		try {
			
	        liveViewSocket = new DatagramSocket(serverPort);
	        liveViewSocket.setSoTimeout(2000);
	        theRecievedPacket = new DatagramPacket(inBuffer, inBuffer.length, lookUpLocalIp (), serverPort);
			log.info("************* UDP Socket on IP address " + lookUpLocalIp () +" on port " + serverPort + " created");
			socketCreated = true;
		} catch (SocketException se)
		{
			log.info("************* Socket creation error : "+ se.getMessage());
		}
		
		return socketCreated;
	}
	
	/**
	 * In case we get to many socket timeouts the socket connection is not recovering.
	 * As a workaround the socket will be closed and opened again.
	 */
	private void socketReconnect() {
		
        try {
        	liveViewSocket.disconnect();
    		liveViewSocket.close();
    		
    		Thread.sleep(1000);
    		
    		liveViewSocket = new DatagramSocket(serverPort);
			liveViewSocket.setSoTimeout(2000);
			enableRecMode();
			log.info("Socket recreated due to massive timeouts");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
        catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
		
	}
	
	public BufferedImage getImageStream() throws Exception {

		int offset=132;
		

		

		try {   
			liveViewSocket.receive(theRecievedPacket);
			outBuffer = theRecievedPacket.getData();

			for (int i = 130; i < 320; i += 1){
				if (outBuffer[i]==-1 && outBuffer[i+1]==-40){
					offset = i; 
				}
			}
			byte [] newBuffer = Arrays.copyOfRange( outBuffer, offset, theRecievedPacket.getLength() );
			bufferedImage = ImageIO.read( new ByteArrayInputStream( newBuffer ) );
			
		}  
		catch (SocketTimeoutException ste) {	
			timeouts++;
			if ((timeouts % 10) == 0) {
				socketReconnect();
				log.info("to many timeouts, reset the connection..." + timeouts);
			}
			throw ste;
			
		}
		catch (Exception e) {
			log.info("Error with client request : "+e.getMessage());
		}
		return bufferedImage;
	}
	
	
	
//------------------- HTTP Networking ---------------------
	
	// HTTP GET request
	private HTTPResponse sendGet(String cmd) throws Exception {
		
		HTTPResponse resp = null;

		//Construct the requeststring for the cmd and create an URL object
		String request = "http:/" + cam_ip.toString() + "/" + cmd;
		log.info("###########Url request = " + request);
		try {
			URL url = new URL(request);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(2000);

			// we will send a GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			log.info("\nSending 'GET' request to URL : " + url);
			log.info("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			resp = new HTTPResponse(con.getResponseCode(), response.toString());

			//print result
			log.info(response.toString());
		} catch (Exception e) {
			log.info(e.toString());

		}

		return resp;

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
			//if ok we will send immediately send an stop shutter
			
			
			return resp.isOK();
		}
		
		
		public String getLensInfo() throws Exception{
			HTTPResponse resp = null;
			resp = sendGet(LENSINFO);
			
			return resp.getBody();
		}
		
		public String setFocus(Focus focus) throws Exception{
			HTTPResponse resp = null;
			String tmp = FOCUS;
			switch (focus) {
			case NEARFAST:
				tmp = tmp + this.NEARFAST;
				log.info("Focus Nearfast " + tmp);
				resp = sendGet(tmp);
				break;
			case NEAR:
				tmp = tmp + this.NEAR;
				log.info("Focus Near" + tmp);
				resp = sendGet(tmp);
				break;
			case FAR:
				tmp = tmp + this.FAR;
				log.info("Focus Far" + tmp);
				resp = sendGet(tmp);
				break;
			case FARFAST:
				tmp = tmp + this.FARFAST;
				log.info("Focus FastFar" + tmp);
				resp = sendGet(tmp);
				break;        
			}
			return resp.getBody();
		}
		
			
		
		public boolean executeAction (Actions action, int value) throws Exception{
			
			boolean ok = false;
			
			switch (action) {
				case SINGLESHOT:
					log.info("Single Shot");
					ok = doSingleShot();
					break;
				case APERTURE:
					log.info("Aperture");
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
				log.info("Getting Lens informatiom");
				results = getLensInfo();
				break;
			case CAMERA:
				log.info("Aperture");
				break;
			
			}
			return results;
			
		}
		
}
