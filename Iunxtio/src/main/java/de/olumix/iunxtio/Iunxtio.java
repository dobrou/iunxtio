package de.olumix.iunxtio;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import de.olumix.iunxtio.gui.*;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author imac
 *
 */
public class Iunxtio {
	
//member variables
	
	public Iunxtio() {
		super();
		// TODO Auto-generated constructor stub
	}

private static Mainframe mainframe;  //main window frame
private static Logger log = Logger.getLogger(Iunxtio.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		log.info("### Starting Iunxtio ... ");
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				startApp();
			}
		});
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
		        mainframe.setSize(1024, 768);
		        mainframe.pack();
		        mainframe.setVisible(true);
	}

}