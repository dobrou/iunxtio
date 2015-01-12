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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import de.olumix.iunxtio.util.LumixUtils.Focus;




/**
 * @author Holger Kremmin
 * (c) 2014
 * This class is used to establish and maintain all basic networking with the Lumix cam
 */

public class LumixNetwork implements RegistryListener {
	
private static Logger log = Logger.getLogger(LumixNetwork.class.getName());
	
private InetAddress cam_ip = null; //the camera ip adress


//----- Lumix constants ------
private final String MODEL = "LUMIX";

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
//private LumixRegistryListener listener = null;
private UpnpService upnpService = null;
private UDN udn = null;

//internal stuff
private LumixNetworkInfo info = null;


//Image retrieval 

private BufferedImage bufferedImage = null;
private DatagramSocket liveViewSocket = null;
private final int serverPort = 49199; //Port for live view images

private DatagramPacket theRecievedPacket;
private byte[] outBuffer;
private byte[] inBuffer = new byte[30000];
private int timeouts=0;  //for handling of timeouts

private BufferedImage notConnectedImage = null;


//---------------------------------------------------------

	/**
	 * Constructor method
	 *
	 */

	public LumixNetwork() {
		
		//our info object which stores basic information about rg. network
		info = new LumixNetworkInfo();
        
        // load a special image to display the not connected state
        notConnectedImage = getImage("images/NotConnected.png");
        
        //in case we need a new lookup we implement a simple timer task which checks every 20 sec 
        Timer timer = new Timer();

        // Start search immediately  then every 3 seconds when not connected
        timer.schedule( new TimerTask() {
			public void run() {
				//we only start a lookup in case we are not connected
				if (!info.isConnected()) {
					
					log.info("Searching Lumix camera...");
					createUpnpService();
					upnpService.getControlPoint().search(new STAllHeader());
					}
				}
			}, 0, 20000 );
        
        lookUpLocalIp();
        
	} 
	
	protected void createUpnpService() {
		// This will create necessary network resources for UPnP right away
        log.info("Starting Cling...");
        upnpService = new UpnpServiceImpl(this);
		
	}

	/**
	 * returns NetworkInof
	 * @return connection
	 */
	
	public LumixNetworkInfo getLumixNetworkInfo() {
		return info;
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
		
		//local_ip = null;
		InetAddress tmp_ip, local_ip = null;
		
		byte[] cam, local;
		
		if (!info.isConnected()) {  // if we are not connected we just return a valid ip
			try {
				local_ip = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				log.info("Can't retrieve local ip adress!");
			}
		} else {  //ok, we are connected and several local ip's may exist. We have to return the right one
			
			cam = cam_ip.getAddress();
			
			Enumeration<NetworkInterface> nets;
			try {
				nets = NetworkInterface.getNetworkInterfaces();
				for (NetworkInterface netint : Collections.list(nets)) {
		        	Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		                log.info("InetAddress:" + inetAddress.toString());
		                tmp_ip = inetAddress;
		                local = inetAddress.getAddress();
		                log.info("TeilAdress:" + local[0] + local[1] + local[2]  + " Cam = " + cam[0] + cam[1] + cam[2] );
		                if ((cam[0]==local[0]) && (cam[1]==local[1]) && (cam[2]==local[2])) {
		                	local_ip = inetAddress;
		                	log.info("############################################################################################## Calculated local InetAddress:" + local_ip.toString());
		                	break;
		                }
		            }
		        }

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
   
		log.info("#################Using the following local IP: " + local_ip.toString() + " Object is evaluated to ..." + local_ip.getHostAddress());
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
	private void closeSocket() {
		
        try {
        	liveViewSocket.disconnect();
    		liveViewSocket.close();
    		info.setReconnect(false); // so we do not call this again
			//enableRecMode();
			log.info("Socket closed");
		}
        catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
		
	}
	
	public BufferedImage getImageStream() throws Exception {

		int offset=132;
		
		
		if (!info.isConnected()) {
			//log.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ not Connected returnung default picture");
			return this.notConnectedImage;
		}

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
		
		if (!info.isConnected()) {return new HTTPResponse(404, "not connected");}

		//Construct the requeststring for the cmd and create an URL object
		String request = "http:/" + info.getCam_ip().toString() + "/" + cmd;
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
		
		
		// listener classes
		  
		  public void remoteDeviceDiscoveryStarted(Registry registry,
					RemoteDevice device) {
				log.info("Discovery started: " + device.getDisplayString());
			}

			public void remoteDeviceDiscoveryFailed(Registry registry,
					RemoteDevice device,
					Exception ex) {
				log.info("Discovery failed: " + device.getDisplayString() + " => " + ex);
			}

			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				
				DeviceDetails dd = device.getDetails();
				ManufacturerDetails md = dd.getManufacturerDetails();
				ModelDetails mod = dd.getModelDetails();
		
				//debug stuff ...
				
				//log.info("Local IP is " + this.lookUpLocalIp().toString());
				log.info("Remote device available: " + mod.getModelName() + " with Number " + mod.getModelNumber() + " and description " + mod.getModelDescription());
				//log.info("ManufacturerURL: " + md.getManufacturerURI());
				//log.info("UPC: " + dd.getUpc() + "PresentationURI " + dd.getPresentationURI() + "Base URL " + dd.getBaseURL());
				//log.info("UDN: " + device.getIdentity().getUdn().toString());
				//log.info("IP: " + device.getIdentity().getDescriptorURL().getHost());
				// -------------------
				
				String ip = null;
				String model = null;
				String modelNumber = null;
				
				
				if (device != null) {
					//get the model name - we expect Lumix here
					model= device.getDetails().getModelDetails().getModelName();
					//Camera type - for later purposes
					modelNumber= device.getDetails().getModelDetails().getModelNumber();
					
					if (model.equals(MODEL)) {
						log.info("-->Found camera: " + model + " " + modelNumber);
						ip = device.getIdentity().getDescriptorURL().getHost();
						
						try {
							cam_ip = InetAddress.getByName(ip);
							
							//first we check if we already have had a connection
							if (info.isReconnect()) {
								log.info("trying a reconnect with the camera");
							}
							//storing the device UDN
							udn = device.getIdentity().getUdn();
							
							//update the info object
							info.setCam_ip(cam_ip);
							info.setModel(model);
							info.setModelNumber(modelNumber);
							info.setConnected(true);
							info.setReconnect(true); // need this flag for reconnect liveview
							
							//calling the camera 
							this.getState(); // so the camera recognize that connection is established
							this.prepareLiveView();
							this.enableRecMode();
							this.startStream();
							
							//debug stuff
							RemoteDevice  foundDevice = (RemoteDevice) registry.getDevice(udn, true);
							log.info("------------------------------>>>>>>>still in Registry? " + foundDevice.getDetails().getModelDetails().getModelName());
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							log.info(e.toString());
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.info(e.toString());
							
						} 	
						
					} else {
						log.info("Found something else: " + model + " " + modelNumber);
						
					}
				}				
			}

			
			public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {

				//log.info("Remote device updated: " + device.getDisplayString());
			}

			public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
				
				String ip = null;
				log.info("Remote device removed: " + device.getDisplayString());
				ip = device.getIdentity().getDescriptorURL().getHost();
				
				//was the Lumix removed?
				
				try {
					if (InetAddress.getByName(ip).equals(info.getCam_ip()) ) {
						log.info("------------------------------>>>>>>>Lumix Camera was removed or is no longer reachable");
						info.disconnect();
						//close the liveview socket
						closeSocket();
						
					}
				} catch (UnknownHostException e) {
					
					log.info(e.toString());
				}
				
			}

			
			public void localDeviceAdded(Registry registry, LocalDevice device) {
				//normally we should not care about any local devices

				log.info("Local device added: " + device.getDisplayString());
			}

			public void localDeviceRemoved(Registry registry, LocalDevice device) {
				//normally we should not care about any local devices

				log.info("Local device removed: " + device.getDisplayString());
			}

			public void beforeShutdown(Registry registry) {

				log.info("Before shutdown, the registry has devices: "+ registry.getDevices().size());
			}

			public void afterShutdown() {

				log.info("Shutdown of registry complete!");

			}
			
			
			//-------- helper methods -------------
			
			private BufferedImage getImage(final String pathAndFileName) {
				try {
					final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
					return ImageIO.read(url );
					//return (BufferedImage) Toolkit.getDefaultToolkit().getImage(url);
				} catch (Exception e) {
					log.info("Image not found ");
					log.info(e.toString());
					return null;
				}
			}
		
}
