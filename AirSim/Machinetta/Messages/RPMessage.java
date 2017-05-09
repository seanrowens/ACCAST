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
 * RPMessage.java
 *
 * Created on July 19, 2005, 1:15 PM
 *
 * RAP->Proxy Message
 */

package AirSim.Machinetta.Messages;

import AirSim.Machinetta.*;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.SARSensorReading;
import AirSim.Environment.Vector3D;

/**
 *
 * @author pscerri
 */
public class RPMessage extends Machinetta.RAPInterface.InputMessages.InputMessage {
    
    public enum MessageTypes {
        AUTHORIZATION_RESPONSE("Authorization Response"),
        GEOLOCATE_OUTPUT("Geolocate Output"),
        GEOLOCATE_REQUEST("Geolocate Request"),
        GEOLOCATE_DATA("Geolocate Data"),
        NAVIGATION_DATA("Navigation Data"),
        RETRY_GEOLOCATE("Retry Geolocate"),
        SEARCH_SENSOR_READING("Search Sensor Reading"),
        EOIR_SENSOR_READING("EO/IR Sensor Reading"),
        RSSI_SENSOR_READING("RSSI Sensor Reading"),
        DIRECTIONAL_RF_SENSOR_READING("Directional RF Sensor Reading"),
        TERMINAL_PROBLEM("Terminal Problem"),        
        FREEFORM("Freeform"),
	LOCATION("Location"),               // Not for L3-Comm work
        SENSOR("Sensor"),                   // Not for L3-Comm work
        SAR_SENSOR("SAR Sensor"),           // Not for L3-Comm work
        REFUEL("Refuel"),
        OUT_OF_FUEL("Out of Fuel"),
	VERIFY_REPEAT("Verify Repeat"),
        AIRDROP("Airdrop status"),
        TRANSPORT("Transport status"),
	    DISMOUNTED_CONTENTS("Dismounted contents/passengers of RAP"),
	    LANDED("Landed at location"),
	    IN_PATROL_AREA("Entering/am in patrol area.")
	;
        
        public final String name;
        
        MessageTypes(String name) {
            this.name = name;
        }
    }
    
    public MessageTypes type = MessageTypes.FREEFORM;
    
    public java.util.Vector<Object> params = new java.util.Vector<Object>();
    

    public RPMessage(Location loc) {
        type = MessageTypes.LOCATION;
        params.add(loc);
    }
     
    public RPMessage(AirSim.SensorReading s) {
        type = MessageTypes.SENSOR;
        params.add(s);
    }
     
    public RPMessage(SARSensorReading s) {
        type = MessageTypes.SAR_SENSOR;
        params.add(s);
    }

    // for refuel
    public RPMessage(MessageTypes t, Location loc) {
        type = t;
        params.add(loc);
    }
    
    // for ouf_of_refuel
    public RPMessage(MessageTypes t, Vector3D loc) {
        type = t;
        params.add(loc);
    }
    
    public RPMessage(MessageTypes t) {
        type = t;
    }
    
    
    public String toString() {
        return "Message " + type.name + " : " + Util.PrintHelpers.vectorToString(params);
	//        return "Message " + type.name; //  + " : " + Util.PrintHelpers.vectorToString(params);
    }
    
    
    static final long serialVersionUID = 1282005L;
}
