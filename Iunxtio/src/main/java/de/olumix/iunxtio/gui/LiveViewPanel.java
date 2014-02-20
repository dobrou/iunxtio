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
package de.olumix.iunxtio.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import de.olumix.iunxtio.net.LumixNetwork;

public class LiveViewPanel extends JPanel {

private static final long serialVersionUID = 6146175487416454007L;

private static Logger log = Logger.getLogger(LiveViewPanel.class.getName());

//networking stuff - todo: move all to LumixNetwork class
private LumixNetwork camNetwork;
private DatagramSocket liveViewSocket = null;
private final int serverPort = 49199;
private InetAddress myip;
    

private BufferedImage bufferedImage = null;

private Component parent;
//we need threading to update the images
Thread liveThread = null;
    
    
    
    
	public LiveViewPanel(Component _parent, LumixNetwork ln) {
		
		this.camNetwork = ln;
		this.parent = _parent;
	/*
		try {
			myip = InetAddress.getLocalHost();
            liveViewSocket = new DatagramSocket(serverPort);
            liveViewSocket.setSoTimeout(1000);
			log.info("************* UDP Socket on IP address " + myip.getHostAddress() +" on port " + serverPort + " created");
			//enableLiveView();
		} catch (SocketException ExceSocket)
		{
			log.info("************* Socket creation error : "+ ExceSocket.getMessage());
		}
                catch(UnknownHostException e) {
			log.info("************* Cannot find myip");
		}
		*/
		
		liveThread = new Thread() {
			public void run() {
				startLiveView();
			};
		};	
                
	}
	
	
	public Dimension getPreferredSize() {
		return new Dimension(640,480);
	}

	public void paint(Graphics g) {
		if ( bufferedImage != null )
			g.drawImage(bufferedImage, 0, 0, null);
	}
	
	
		
	public void enableLiveView() {
		liveThread.start();
	}
        
	public void startLiveView() {
		
		//ensure that everything is ready for live stream
		try {
			camNetwork.prepareLiveView();
			camNetwork.enableRecMode();
			camNetwork.startStream();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.info(e1.toString());
		}
		
		int timeouts=0;
		boolean loop = true;
		
		while( loop ) {
			
			try {
				//bufferedImage = camNetwork.getImageStream();
				Thread.sleep(33); // expecting a frame rate around 25 fps so we can wait a little bit here for next frame
			} catch (Exception e) {
				timeouts++;
				if ((timeouts % 20) == 0) {
					log.info("*" + timeouts);
				}
				log.info(e.toString());
			}
			//invalidate();
			this.repaint();
			//parent.repaint();
		}
		
		
		liveThread.stop();

	}
	
	public void stopLiveView() {
		liveViewSocket.close();
	}
	

}
