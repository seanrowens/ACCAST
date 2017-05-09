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
 * FlyZone.java
 *
 * Created on April 28, 2006, 1:36 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import Machinetta.State.BeliefType.ProxyID;
import AirSim.Environment.Area;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import java.text.DecimalFormat;

/**
 * FlyZone beliefs are used for deconflicting airspace for UAVs - UAVs
 * request blocks of airspace to fly in and are either approved or
 * denied by the air traffic controller.
 *
 * @author pscerri
 */
public class FlyZone extends Belief {
    private final static DecimalFormat fmt = new DecimalFormat("0.0");
    
    /** Negative value indicates that the field has not been set */
    public double 
            latitude1 = -Double.MAX_VALUE, 
            latitude2 = -Double.MAX_VALUE, 
            longtitude1 = -Double.MAX_VALUE, 
            longtitude2 = -Double.MAX_VALUE, 
            altitude1 = -Double.MAX_VALUE, 
            altitude2 = -Double.MAX_VALUE;
    
    public boolean approved = false;
    public ProxyID pid;

    /** Creates a new instance of FlyZone */
    public FlyZone(ProxyID pid, double lon1, double lat1, double alt1, double lon2, double lat2, double alt2) {
	this.pid = pid;
	this.longtitude1 = lon1;
	this.latitude1 = lat1;
	this.altitude1 = alt1;
	this.longtitude2 = lon2;
	this.latitude2 = lat2;
	this.altitude2 = alt2;
    }

    public FlyZone(FlyZone fz) {
	this.pid = fz.pid;
	this.longtitude1 = fz.longtitude1;
	this.latitude1 = fz.latitude1;
	this.altitude1 = fz.altitude1;
	this.longtitude2 = fz.longtitude2;
	this.latitude2 = fz.latitude2;
	this.altitude2 = fz.altitude2;
	this.approved = fz.approved;
    }

    public FlyZone(){
    }

    public boolean sameSpace(FlyZone fz) {
	if(this.latitude1 != fz.latitude1
	   || this.latitude2 != fz.latitude2
	   || this.longtitude1 != fz.longtitude1
	   || this.longtitude2 != fz.longtitude2
	   || this.altitude1 != fz.altitude1
	   || this.altitude2 != fz.altitude2)
	    return false;
	else
	    return true;
    }

    public boolean intersects(FlyZone fz) {
	boolean overlapx = false;
	boolean overlapy = false;
	boolean overlapz = false;
	if((this.longtitude1 <= fz.longtitude2) && (this.longtitude1 >= fz.longtitude1)) {
	    overlapx = true; 
	    Machinetta.Debugger.debug("intersects: overlaps X this.lon1 "+this.longtitude1+"  <= other.lon2 "+fz.longtitude2,1,this);
	}
	else if((this.longtitude2 <= fz.longtitude2) && (this.longtitude2 >= fz.longtitude1)) {
	    Machinetta.Debugger.debug("intersects: overlaps X this.lon2 "+this.longtitude2+"  >= other.lon1 "+fz.longtitude1,1,this);
	    overlapx = true;
	}
	if((this.latitude1 <= fz.latitude2) && (this.latitude1 >= fz.latitude1)) {
	    Machinetta.Debugger.debug("intersects: overlaps X this.lat1 "+this.latitude1+"  <= other.lat2 "+fz.latitude2,1,this);
	    overlapy = true;
	}
	else if((this.latitude2 <= fz.latitude2) && (this.latitude2 >= fz.latitude1)) {
	    Machinetta.Debugger.debug("intersects: overlaps X this.lat2 "+this.latitude2+"  >= other.lat1 "+fz.latitude1,1,this);
	    overlapy = true;
	}
	if((this.altitude1 <= fz.altitude2) && (this.altitude1 >= fz.altitude1)) {
	    Machinetta.Debugger.debug("intersects: overlaps X this.alt1 "+this.altitude1+"  <= other.alt2 "+fz.altitude2,1,this);
	    overlapz = true;
	}
	else if((this.altitude2 <= fz.altitude2) && (this.altitude2 >= fz.altitude1)) {
	    overlapz = true;
	    Machinetta.Debugger.debug("intersects: overlaps X this.alt2 "+this.altitude2+"  >= other.alt1 "+fz.altitude1,1,this);
	}

	if(overlapx && overlapy && overlapz)
	    return true;
	return false;

    }

    public BeliefID makeID() {
        return new BeliefNameID(toString());
    }
    
    public String toString() {
	double width = longtitude2 - longtitude1;
	double length = latitude2 - latitude1;
	double height = altitude2 - altitude1;
	return "flyzone: pid="+pid
	    +" approved="+approved
	    +" size "+fmt.format(width)
	    +", "+fmt.format(length)
	    +", "+fmt.format(height)
	    +" lon1="+fmt.format(longtitude1)
	    +" lon2="+fmt.format(longtitude2)
	    +" lat1="+fmt.format(latitude1)
	    +" lat2="+fmt.format(latitude2)
	    +" alt1="+fmt.format(altitude1)
	    +" alt2="+fmt.format(altitude2);
    }
}
