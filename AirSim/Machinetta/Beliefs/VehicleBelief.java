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
 * VehicleBelief.java
 *
 * Created on March 20, 2006, 8:43 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import java.text.DecimalFormat;

/**
 * VehicleBelief is used by proxes to share (possibly fused) location
 * and type beliefs about vehicles detected by their RAP's sensors.
 *
 * @author pscerri
 */
public class VehicleBelief extends Belief {
    private final static DecimalFormat fmt = new DecimalFormat("0.000");
    
    private int x = -1;
    private int y = -1;
    private int z = -1;
    private double heading = 0.0;
    public double getHeading() { return heading; }
    public void setHeading(double value) { heading = value; }

    private double latitude = -1;
    public double getLatitude() { return latitude; }
    public void setLatitude(double value) { latitude = value; }
    private double longitude = -1;
    public double getLongitude() { return longitude; }
    public void setLongitude(double value) { longitude = value; }
    
    private ProxyID sensor = null;
    public ProxyID getSensor() { return sensor; }
    public void setSensor(ProxyID value) { sensor = value; }

    private Asset.Types type = null;
    public Asset.Types getTypes() { return type; }
    public void setTypes(Asset.Types value) { type = value; }
    private double confidence = 0.0;
    public double getConfidence() { return confidence; }
    public void setConfidence(double value) { confidence = value; }
    private ForceId forceId = ForceId.UNKNOWN;
    public ForceId getForceId() { return forceId; }
    public void setForceId(ForceId value) { forceId = value; }
    private State state = null;
    public State getState() { return state; }
    public void setState(State value) { state = value; }
    private long timeMs = 0;    
    public long getTimeMs() { return timeMs; }
    public void setTimeMs(long value) { timeMs = value; }

    /// For use for falconview plugin interface
    private String key = null;
    public String getKey() { return key; }
    public void setKey(String value) { key = value; }
    private boolean delete = false;
    public boolean getDelete() { return delete; }
    public void setDelete(boolean value) { delete = value; }

    /** Creates a new instance of VehicleBelief */
    public VehicleBelief(long timeMs, Asset.Types type,  double confidence, int x, int y, int z, State state) {
	this.timeMs = timeMs;
        this.type = type;
        this.confidence = confidence;
        this.x = x;
        this.y = y;
        this.z = z;
	this.state = state;
    }

    public BeliefID makeID() {
        return new BeliefNameID("VID:"+getType()+getX()+getY());
    }

    public Asset.Types getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
        
    public String toString() { 
	return 
	    "Report: " + type + " @ (" + x + ", " + y + ", "+z+" )"
	    +" Confidence: " + fmt.format(confidence)
	    +" type: "+type
	    +" heading: "+ fmt.format(heading)
	    +" sensor pid: "+sensor
	    +" forceId: "+forceId
	    +" state: "+state
	    +" key: "+key
	    +" delete: "+delete
	    ; 
    }
}
