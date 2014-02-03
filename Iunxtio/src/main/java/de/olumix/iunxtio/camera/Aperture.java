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
package de.olumix.iunxtio.camera;

import java.math.*;
import java.text.DecimalFormat;
/**
 * @author hkremmin
 *
 */
public class Aperture {
	
private int lumixValue = 0;
private int increment = 0;
private double lightValue = 0; //0 = f1.0, 9 = f22
private double apertureValue = 0;
private String displayAperture = null;

	/**
	 * 
	 */
	public Aperture(int _lumixValue, int _increment) {
		lumixValue = _lumixValue;
		increment = _increment;
		calculateValues();
	}
	
	public Aperture(int _lumixValue) {
		lumixValue = _lumixValue;
		increment = 256; //maybe this value might never change, but who knows...
		calculateValues();
	}
	
	// private methods
	
	private void calculateValues() {
		
		if ((lumixValue != 0) && (increment != 0)) {
			//calculate the light value first
			lightValue = lumixValue/increment;
			apertureValue = Math.sqrt(Math.pow(2, lightValue));
			DecimalFormat df = new DecimalFormat("0.0");
			displayAperture = df.format(apertureValue);
		}
		
	}
	
	
	
	public double getAperture() {
		
		return apertureValue;
		
	}

}
