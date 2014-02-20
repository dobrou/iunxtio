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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;

import de.olumix.iunxtio.camera.Lens;
import de.olumix.iunxtio.net.LumixCommand;
import de.olumix.iunxtio.net.LumixNetworkInfo;
import de.olumix.iunxtio.util.LumixUtils.Focus;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 * @author hkremmin
 *
 */
public class ControlPanel extends JPanel {
	

private static final long serialVersionUID = -6531131110490528420L;

private static Logger log = Logger.getLogger(ControlPanel.class.getName());
	
private LumixCommand cameraCommand = null;
private JLabel apertureLabel;
private JSlider apertureSlider;
private JLabel lblfocusValue; 
private int focusValue = -1;

	/**
	 * @wbp.parser.constructor
	 * 
	 */
	public ControlPanel(LumixCommand lm) {
		
		//handle to cameraCommand object
		
		cameraCommand = lm;
		
		
		setBackground(new Color(210, 180, 140));
		setLayout(null);
		
		JButton btnNewButton = new JButton("Shutter Release");
		btnNewButton.setIcon(new ImageIcon(getImage("images/camera-photo.png")));
		btnNewButton.setFont(new Font("Tahoma", Font.BOLD, 18));
		btnNewButton.setBackground(new Color(135, 206, 250));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cameraCommand.singleShot();
			}
		});
		btnNewButton.setBounds(59, 384, 326, 80);
		add(btnNewButton);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(12, 359, 426, 12);
		add(separator);
		
		lblfocusValue = new JLabel("focusValue");
		lblfocusValue.setBounds(255, 331, 130, 16);
		add(lblfocusValue);
		
		
		JLabel lblAdjustFocus = new JLabel("Adjust Focus");
		lblAdjustFocus.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAdjustFocus.setBounds(29, 294, 190, 16);
		add(lblAdjustFocus);
		
		JComboBox shutterCombo = new JComboBox();
		shutterCombo.setBounds(12, 196, 179, 22);
		add(shutterCombo);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(12, 182, 426, 2);
		add(separator_1);
		
		JLabel lblShutterSpeed = new JLabel("Shutter Speed");
		lblShutterSpeed.setBounds(22, 167, 179, 16);
		add(lblShutterSpeed);
		
		JLabel lblApertur = new JLabel("Apertur");
		lblApertur.setBounds(29, 241, 201, 16);
		add(lblApertur);
		
		JComboBox programCombo = new JComboBox();
		programCombo.setBounds(12, 133, 179, 22);
		add(programCombo);
		
		JLabel lblProgramMode = new JLabel("Program Mode");
		lblProgramMode.setBounds(12, 101, 179, 16);
		add(lblProgramMode);
		
		JLabel lblAfMode = new JLabel("AF Mode");
		lblAfMode.setBounds(221, 101, 201, 16);
		add(lblAfMode);
		
		JComboBox afCombo = new JComboBox();
		afCombo.setBounds(221, 133, 201, 22);
		add(afCombo);
		
		apertureLabel = new JLabel("11");
		apertureLabel.setBounds(411, 264, 61, 29);
		add(apertureLabel);
		
		
		apertureSlider = new JSlider();
		apertureSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent event)  {
				updateSliderLabel(apertureSlider.getValue());
			}
		});
		
		apertureSlider.setBounds(6, 264, 393, 29);
		apertureSlider.setValue(0);
		add(apertureSlider);
		
		JButton btnGetLensInfo = new JButton("Get lens Info");
		btnGetLensInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateLensInfo();
			}
		});
		btnGetLensInfo.setBounds(12, 6, 117, 29);
		add(btnGetLensInfo);
		
		JButton btnSetAperture = new JButton("Set Aperture");
		btnSetAperture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cameraCommand.setAperture(apertureSlider.getValue());
			}
		});
		btnSetAperture.setBounds(496, 264, 117, 29);
		add(btnSetAperture);
		
		JButton btnNearfast = new JButton();
		btnNearfast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				focusValue = cameraCommand.setFocus(Focus.NEARFAST);
				lblfocusValue.setText(String.valueOf(focusValue));
			}
		});
		btnNearfast.setIcon(new ImageIcon(getImage("images/media-seek-backward.png")));
		btnNearfast.setBounds(12, 318, 41, 41);
		add(btnNearfast);
		
		JButton btnNear = new JButton();
		btnNear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				focusValue = cameraCommand.setFocus(Focus.NEAR);
				lblfocusValue.setText(String.valueOf(focusValue));
			}
		});
		btnNear.setIcon(new ImageIcon(getImage("images/media-playback-back.png")));
		btnNear.setBounds(59, 318, 41, 41);
		add(btnNear);
		
		JButton btnFar = new JButton();
		btnFar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				focusValue = cameraCommand.setFocus(Focus.FAR);
				lblfocusValue.setText(String.valueOf(focusValue));
			}
		});
		btnFar.setIcon(new ImageIcon(getImage("images/media-playback-start.png")));
		btnFar.setBounds(130, 318, 41, 41);
		add(btnFar);
		
		JButton btnFarfast = new JButton();
		btnFarfast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				focusValue = cameraCommand.setFocus(Focus.FARFAST);
				lblfocusValue.setText(String.valueOf(focusValue));
			}
		});
		btnFarfast.setIcon(new ImageIcon(getImage("images/media-seek-forward.png")));
		btnFarfast.setBounds(183, 318, 41, 41);
		add(btnFarfast);
		
		JLabel lblFocusText = new JLabel("Focus Value");
		lblFocusText.setBounds(255, 295, 130, 16);
		add(lblFocusText);
		
		
		
		// init stuff
		//cameraCommand.lensInfo();
		//adjustApertureSlider();
	}
	
	public void updateLensInfo() {
		cameraCommand.lensInfo();
		adjustApertureSlider();
	}
	
	
	private void updateSliderLabel(int value) {
		Lens lens = cameraCommand.getLens();
		apertureLabel.setText(lens.getAperture(value));
		
	}
	
	/**
	 * this method is used to adjust the min & max slider value of the aperture slider
	 * @todo should be updated on a regular basis for zoom lenses, lens change etc.
	 */
	public void adjustApertureSlider() {
		Lens lens = cameraCommand.getLens();
		apertureSlider.setMinimum(lens.getMaxAperture());
		apertureSlider.setMaximum(lens.getMinAperture());
		
		log.info("### Slider adjusted");
		
	}
	

	/**
	 * @param layout
	 */
	public ControlPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param isDoubleBuffered
	 */
	public ControlPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param layout
	 * @param isDoubleBuffered
	 */
	public ControlPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}
	
	//quick hack for the layout
	public Dimension getPreferredSize() {
        return new Dimension(640,445);
	}
	
	
	//helper methods
	
	private Image getImage(final String pathAndFileName) {
	    final URL url = Thread.currentThread().getContextClassLoader().getResource(pathAndFileName);
	    return Toolkit.getDefaultToolkit().getImage(url);
	}
}
