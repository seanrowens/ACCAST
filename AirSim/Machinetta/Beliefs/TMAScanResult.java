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
/*
 * TMAScanResult.java
 *
 * Created on February 13, 2006, 5:29 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Machinetta.Messages.GeoLocateDataAP;
import AirSim.Machinetta.Messages.SearchSensorReadingAP;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.Matchable;
import java.util.Vector;

/**
 * TMAScanResult is used for TDOA (Time Difference Of Arrival) emitter
 * geolocation coordination.  
 *
 * @author pscerri
 */
public class TMAScanResult extends Belief implements Matchable {
    
    public boolean isDiscriminated = false;
    
    public String sensorID = "Unknown";
    
    public long readingID = -1L;
    
    public short blockCounter = -1, blockCount = 0;
    
    public byte [] data = null;
    
    public boolean isGeolocateData = false;
    public BeliefID requestID = null;
    
    /** Creates a new instance of TMAScanResult */
    public TMAScanResult() {
    }
    
    public BeliefID makeID() {
        // @fix
        
        return new BeliefNameID("TMAScan"+(new java.util.Random()).nextInt());
    }
    
    public void setData(SearchSensorReadingAP rpMsg) {
        sensorID = rpMsg.sensorID;
        readingID = rpMsg.readingID;
        blockCount = rpMsg.blockCount;
        blockCounter = rpMsg.blockCounter;
        data = rpMsg.data;
    }
    
    public void setData(GeoLocateDataAP rpMsg) {
        sensorID = rpMsg.sensorID;
        readingID = rpMsg.readingID;
        blockCount = rpMsg.blockCount;
        blockCounter = rpMsg.blockCounter;
        data = rpMsg.data;
    }
    
    /**
     * Generates a belief represenation according to the specified template
     *
     * @param keys An array of attribute keys that the belief should fill in when generating the string
     * @return A string with the specified keys filled in
     */
    public String matchString(Vector keys) {
        StringBuffer sb = new StringBuffer();
        for (Object o: keys) {
            if (o.toString().equalsIgnoreCase("class")) {
                sb.append(getClass().toString().substring(6) + " ");
            } else if (o.toString().equalsIgnoreCase("isDiscriminated")) {
                sb.append(isDiscriminated + " ");
            } else if (o.toString().equalsIgnoreCase("id")) {
                sb.append(id.toString() + " ");
            } else if (o.toString().equalsIgnoreCase("isGeolocateData")) {
                sb.append(isGeolocateData + " ");
            }
        }
        // Machinetta.Debugger.debug("Returning: " + sb.toString(), 1, this);
        return sb.toString().trim();
    }
    
}
