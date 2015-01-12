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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import de.olumix.iunxtio.camera.Camera;
import de.olumix.iunxtio.camera.Lens;
import de.olumix.iunxtio.net.LumixCommand;
import de.olumix.iunxtio.net.LumixNetwork;
import de.olumix.iunxtio.gui.SplashScreen;

public class Mainframe extends JFrame {
	

private static final long serialVersionUID = -1390195893494380883L;

private static Logger log = Logger.getLogger(Mainframe.class.getName());
private final static String windowTitle = "IUNXTIO Lumix App";

//members

private LumixNetwork camNetwork;
private LumixCommand camCommand;
private Lens lens;
private Camera camera;

//need control over this panel
private LiveViewPanel liveView = null;
private ControlPanel cameraControl = null;

private SplashScreen splash = null;

JLabel infoLine = null;


	/**
	 * Constructor 
	 */
	public Mainframe() {
		super(windowTitle);
		
		//init all objects we need
		initCommunication();
		
		//----------- init Gui ------------------------------------------------
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		getContentPane().add(menuBar, BorderLayout.PAGE_START);
		
		JMenu menuStart = new JMenu("Start");
		menuBar.add(menuStart);
		
		/*
		JMenuItem menuItemConnect = new JMenuItem("Connect");
		menuItemConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
						connectCamera();
				
			}
		});
		
		*/
		
		JMenuItem menuItemExit = new JMenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//todo disconnect camera
				System.exit(0);
			}
		});
		
		// menuStart.add(menuItemConnect);
		
		menuStart.add(menuItemExit);
		
		liveView = new LiveViewPanel(this, camNetwork);
		liveView.setBackground(Color.DARK_GRAY);
		liveView.setPreferredSize(new Dimension(640,480));
		getContentPane().add(liveView, BorderLayout.LINE_START);
		liveView.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		cameraControl = new ControlPanel(camCommand);
		cameraControl.setBackground(Color.GRAY);
		getContentPane().add(cameraControl, BorderLayout.LINE_END);
		
		infoLine = new JLabel();
		infoLine.setText("Connected Camera: ");
		infoLine.setBackground(Color.ORANGE);
		getContentPane().add(infoLine, BorderLayout.PAGE_END);
		
		liveView.enableLiveView();
		pack();
		
	}
	
	private void initCommunication() {
		
		log.info("Init the network communication");
		//init network class to handle connection to Lumix
		camNetwork = new LumixNetwork();
		//init the camCommand, cam & lens objects
		lens = new Lens();
		camera = new Camera();
		camCommand = new LumixCommand(camNetwork, camera, lens);
		
		//staring the timer - we obtain in regular intervals information from cam
		//this is required to keep liveview alive
		//inside LumixCommand the run method will be triggered 
		Timer timer = new Timer("MyTimer");
        timer.scheduleAtFixedRate(camCommand, 0, 500);
		
	}
	
	
	
	//TODO Remove !!!!
	/*
	private void initNetwork() {
		
		log.info("Initialize the camera for remote usage ");

		
		try {
				log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Trying to establish Live View...");
				//sending the start stream command - todo move to liveview panel
				//camNetwork.getState();
				liveView.enableLiveView();
				//infoLine.setText(camNetwork.getCameraInfoString());
				//cameraControl.updateLensInfo();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("camera is not available?....");
				e.printStackTrace();
			}

		
	} */
	
	

} //end of class
