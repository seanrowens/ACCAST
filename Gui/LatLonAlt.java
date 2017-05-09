/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package Gui;

// @author      Sean R. Owens
// @version     $Id: LatLonAlt.java,v 1.1 2008/01/28 02:17:01 owens Exp $ 
import java.text.DecimalFormat;

public class LatLonAlt {
    // 1 degree lat is at most 111.799 km, 1 degree lon is at most
    // 111.321 km.  So 6 decimal places means we can get down to close
    // to one meter.
    private static DecimalFormat fmt = new DecimalFormat("0.000000");
    private static DecimalFormat fmt2 = new DecimalFormat("0.000");
    public double lat = 0.0;	// decimal degrees
    public double lon = 0.0;	// decimal degrees
    public double alt = 0.0;	// meters (above mean sea level?)

    public LatLonAlt(double lat, double lon, double alt) {
	this.lat = lat;
	this.lon = lon;
	this.alt = alt;
    }
    
    public String toString() { 
	return fmt.format(lat)+","+fmt.format(lon)+","+fmt2.format(alt);
    }

    // add x meters in the east/west axis, y meters in the north/south axis.
    //
    // Note, this does not take into account 'great circle' distances,
    // and hence will not work right for large distances.  Probably it
    // will be ok for distances < 100km.  (Curvature of the earth
    // gives an altitude drop of approximatley 1 meter for 25km.)
    public void addMeters(double metersX, double metersY, double metersZ, LatLonAlt result) {
	result.alt = alt+metersZ;
	result.lon = lon + LatLonUtil.kmToDegreesLon(lat, metersX/1000.0);
	result.lat = lat + LatLonUtil.kmToDegreesLat(lat, metersY/1000.0);
    }
}
