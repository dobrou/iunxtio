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
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import de.olumix.iunxtio.camera.Camera;
import de.olumix.iunxtio.camera.Lens;
import de.olumix.iunxtio.net.LumixNetwork.Actions;
import de.olumix.iunxtio.net.LumixNetwork.Capabilities;
import de.olumix.iunxtio.util.LumixUtils.Focus;

/**
 * @author hkremmin
 * This class handles all commands which were sent to the camera an processes the results. It is meant to handle commands 
 * from a Photograph's perspective.
 * It does not handle any low level network functionality. It also ensures that the Camera and Lens state is updated
 * based on the capabilities of the camera/lens combination.
 * It extends TimerTAsk so the network, camera & lens state can be checked on a regular basis.
 */
public class LumixCommand extends TimerTask {
	
private LumixNetwork camNetwork = null; //this class encapsulates basic network handling
private LumixNetworkInfo info = null; // to get the network state
private Camera camera = null; //camera object which stores all capabilities of the camera
private Lens lens = null; //lens object which stores all capabilities of the attached lens

private static Logger log = Logger.getLogger(HTTPResponse.class.getName());



	/**
	 * Standard constructor
	 * @param ln basic network handling
	 * @param c camera object which stores all capabilities of the camera
	 * @param l lens object which stores all capabilities of the attached lens
	 */
	public LumixCommand(LumixNetwork ln, Camera c, Lens l) {
		//handle to the network class, camera and lens object
		camNetwork = ln;
		camera = c;
		lens = l;
		info = camNetwork.getLumixNetworkInfo();
	} 
	
	//init/update the camera and lens objects
	
	
	
	
	public boolean singleShot(){
		
		boolean ok = false;

		try {
			ok = camNetwork.executeAction(Actions.SINGLESHOT, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.out);
			log.severe(e.toString());
			;
		}
		
		return ok;
	}
	
	public boolean setAperture(int value){
		
		boolean ok = false;
		
		try {
			ok = camNetwork.executeAction(Actions.APERTURE, value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.severe(e.toString());
		}
		
		return ok;
	}
	
	public void updateLensInfo(){
		String result = null;
		String tmp = null;
		
		int minAperture, maxAperture, minFocal, maxFocal = 0;
		boolean isPower, isNative = false;
		
		
		try {
			result = camNetwork.getCapabilities(Capabilities.LENS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
		
		//now parsing the result string
		//example result is: ok,2304/256,434/256,3072/256,0/256,0,off,45,45,on,128/1024
		//first we will split the string
		String[] values = result.split( Pattern.quote( "," ) );
		//log.info("Lens value count " + values.length);
		
		if (values[0].equals("ok")) {
			//get the value for closed aperture
			tmp = subStringBefore(values[1], "/");
			minAperture = Integer.parseInt(tmp);
			tmp = subStringBefore(values[2], "/");
			maxAperture = Integer.parseInt(tmp);
			
			//values[3] = ???
			//values[4] = ????
			//values[5] = ?????
			
			if (values[6].equals("off")) {
				isPower = false;
			} else {
				isPower = true;
			}
			
			minFocal = Integer.parseInt(values[7]);
			maxFocal = Integer.parseInt(values[8]);
			
			if (values[9].equals("off")) {
				isNative = false;
			} else {
				isNative = true;
			}
			
			//values[10] = ???????
			lens.init(minAperture, maxAperture, minFocal, maxFocal, isPower, isNative);
			//log.info("Lens Info = " + lens.toString());
			
			
		} else {
			log.info("Can't read lens information!!!!!");
		}
		
		
	}
	
	public int setFocus(Focus focus) {
		int newFocus = -1;
		String result = null;
		
		try {
			result = camNetwork.setFocus(focus);
			String[] values = result.split( Pattern.quote( "," ) );
			// the fist value should always be ok 
			if (values[0].equals("ok")) {
				newFocus = Integer.parseInt(values[1]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info(e.toString());
		}
		
		
		return newFocus;
	}
	
	
	//simple getter/setter
	
	public Lens getLens() {
		return lens;
	}
	
	
	
	// ------- Tasks --------
	
	//check if any lens info has changed
	// update camera state
	// update network state
	
	@Override
    public void run() {
		
		//log.info("Lumix Command is executing timer");
		
		info = camNetwork.getLumixNetworkInfo();
        //update LensInfo
		if (info.isConnected()) {
			updateLensInfo();
		}
		
   }
	
	//helper class for parsing
	
	/**
	   * Returns the substring before the first occurrence of a delimiter. The
	   * delimiter is not part of the result.
	   *
	   * @param string    String to get a substring from.
	   * @param delimiter String to search for.
	   * @return          Substring before the first occurrence of the delimiter.
	   */
	  private  String subStringBefore( String string, String delimiter )
	  {
	    int pos = string.indexOf( delimiter );

	    return pos >= 0 ? string.substring( 0, pos ) : string;
	  }
	  
  

}
