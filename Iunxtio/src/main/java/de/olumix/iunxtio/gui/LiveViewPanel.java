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

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import de.olumix.iunxtio.net.LumixNetwork;

public class LiveViewPanel extends JPanel {

	private static final long serialVersionUID = 6146175487416454007L;
	
	//networking stuff - todo: move all to LumixNetwork class
	private LumixNetwork camNetwork;
	DatagramSocket liveViewSocket = null;
	int serverPort = 49199;
        private InetAddress myip;
        
    BufferedImage image;
    BufferedImage bufferedImage = null;
    
    Component parent;
    //we need threading to update the images
    Thread liveThread = null;
    
    
    
    
	public LiveViewPanel(Component _parent, LumixNetwork ln) {
		
		this.camNetwork = ln;
		this.parent = _parent;
		
		
			
			
		try {
			myip = InetAddress.getLocalHost();
            liveViewSocket = new DatagramSocket(serverPort);
            liveViewSocket.setSoTimeout(1000);
			System.out.println("************* UDP Socket on IP address " + myip.getHostAddress() +" on port " + serverPort + " created");
			//enableLiveView();
		} catch (SocketException ExceSocket)
		{
			System.out.println("************* Socket creation error : "+ ExceSocket.getMessage());
		}
                catch(UnknownHostException e) {
			System.out.println("************* Cannot find myip");
		}
		
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
		
		
		DatagramPacket theRecievedPacket;
		byte[] outBuffer;
		byte[] inBuffer; 

		int offset=132;
		int timeouts=0;

		inBuffer = new byte[30000];

		System.out.println("************* Starting Live View");
		
		try {
			camNetwork.enableRecMode();
			camNetwork.startStream();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean loop = true;
		while( loop )
		{
			try {   

				theRecievedPacket = new DatagramPacket(inBuffer, inBuffer.length, myip, serverPort);				
				liveViewSocket.receive(theRecievedPacket);
				outBuffer = theRecievedPacket.getData();

				for (int i = 130; i < 320; i += 1){
					if (outBuffer[i]==-1 && outBuffer[i+1]==-40){
						offset = i; 
					}
				}

				byte [] newBuffer = Arrays.copyOfRange( outBuffer, offset, theRecievedPacket.getLength() );

				bufferedImage = ImageIO.read( new ByteArrayInputStream( newBuffer ) );
				//invalidate();
				this.repaint();
				//parent.repaint();
				
				
				Thread.sleep(20); // sleep for 20 milliseconds
				//parent.validate();
			}  
			catch (SocketTimeoutException ste) {
				System.out.print("*" + timeouts);
				timeouts++;
				//System.out.println("Error with client request : "+ste.getMessage() + " " + timeouts);
				//if (timeouts > 20) loop=false;
			}
			catch (Exception e) {
				System.out.print("*");
				System.out.println("Error with client request : "+e.getMessage());
				loop=false;
			}
		}
		liveThread.stop();

	}
	
	public void stopLiveView() {
		liveViewSocket.close();
	}
	

}
