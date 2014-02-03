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

/**
 * @author hkremmin
 *
 */
public class Lens {
	
//object state
private boolean initialized = false;
	
//lens properties
	
private int minAperture; //aperture is nearly closed. Values are linear integers
private int maxAperture; //aperture is open

private boolean fixedLens = false;
private boolean powerZoom = false;
private boolean nativeLens = false;

private int minFocalLength; // for a fixed lens this is used as focal length
private int maxFocalLength; // for a fixed lens this is used as focal length

private int increment = 256; //have to divide the aperture values to calculate a light value

	

	/**
	 * 
	 */
	public Lens() {
		// TODO Auto-generated constructor stub
	}
	
	public void init(int _minAperture, int _maxAperture, int _minFocal, int _maxFocal, boolean isPowerZoom, boolean isNativeLens ) {
		minAperture = _minAperture;
		maxAperture = _maxAperture;
		minFocalLength = _minFocal;
		maxFocalLength = _maxFocal;
		powerZoom = isPowerZoom;
		nativeLens = isNativeLens;
		
		if (minFocalLength == maxFocalLength) {
			fixedLens = true;
		}
		
		initialized = true;
	}
	
	
	public String getAperture(int value) {
		double d, lv;
		
		System.out.println("value =  "+ value);
		
		d =  value/ (double) increment;
		System.out.println("d =  "+ d);
		lv = Math.sqrt(Math.pow(2, d));
		System.out.println("lv = " + lv);
		return String.format("%.1f", lv); 
		
	}
	
	
	/**
	 * @return the initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * @return the minAperture
	 */
	public int getMinAperture() {
		return minAperture;
	}

	/**
	 * @return the maxAperture
	 */
	public int getMaxAperture() {
		return maxAperture;
	}

	/**
	 * @return the fixedLens
	 */
	public boolean isFixedLens() {
		return fixedLens;
	}

	/**
	 * @return the powerZoom
	 */
	public boolean isPowerZoom() {
		return powerZoom;
	}

	/**
	 * @return the nativeLens
	 */
	public boolean isNativeLens() {
		return nativeLens;
	}

	/**
	 * @return the minFocalLength
	 */
	public int getMinFocalLength() {
		return minFocalLength;
	}

	/**
	 * @return the maxFocalLength
	 */
	public int getMaxFocalLength() {
		return maxFocalLength;
	}

	public String toString() {
		
		return minAperture + "," + maxAperture + "," + minFocalLength + "," + maxFocalLength + "," + powerZoom + "," + nativeLens;
		
	}

}
