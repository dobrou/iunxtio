package de.olumix.iunxtio;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import de.olumix.iunxtio.gui.*;




/**
 * @author imac
 *
 */
public class Iunxtio {
	
//member variables
	
private static Mainframe mainframe;  //main window frame

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				startApp();
			}
		});
		
		//startApp();
	}
	
	public static void startApp() {
		
		// initialize the window
				mainframe = new Mainframe();
				
		        //Release the resource window handle as we close the frame
		        mainframe.addWindowListener(new WindowAdapter(){
		                public void windowClosing(WindowEvent e) {
		                    System.exit(0);
		                    //todo  we should clean up here
		                }
		            });
		        //imagePanel.add(theServer );
		        //frame.add(imagePanel);
		        //frame.pack();
		        mainframe.setSize(1024, 768);
		        mainframe.pack();
		        mainframe.setVisible(true);
	}

}