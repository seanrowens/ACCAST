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
 * RSSIReading.java
 *
 * Created on July 7, 2006, 11:56 AM
 *
 */

package AirSim.Machinetta.Beliefs;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import java.io.Serializable;

/**
 * RSSIReading is used to share beliefs about a Received Signal
 * Strength Indicator sensor reading that is used to locate emitters.
 * (I.e. radio transmitters.)  This is used primarily by the
 * UAV/Operator filtering code.  
 *
 * @author pscerri
 */
public class RSSIReading extends Belief implements Comparable, Serializable {
    

    public int channel;
    public double x, y, z, strength;
    public long time;
    public ProxyID sensor = null;
    public int sharingTTL = 3;
    
    /**
     * This is only for the ParticleFilter and should be moved from this class.
     *
     * @deprecated This shouldn't be here.
     */    
    public double entropyChange = 0.0;
    
    /**
     * This is only for the ParticleFilter and should be moved from this class.
     *
     * @deprecated This shouldn't be here.
     */    
    public double importance = 0.0;        
    
    /** Creates a new instance of RSSIReading */
    public RSSIReading() {
    }

    public RSSIReading(double x, double y, double z, double strength, int channel) {
	this.x = x;
	this.y = y;
	this.z = z;
	this.strength = strength;
	this.channel = channel;
    }

    public BeliefID makeID() {
	return new BeliefNameID("RSSI:" + sensor + "@" + time+"."+channel);
    }

    public int compareTo(Object o) {
	//         if (!(o instanceof RSSIReading)) return 0;
	//         else return (int)(((RSSIReading)o).importance - importance);
 	RSSIReading r = (RSSIReading)o;
 	if(this.strength < r.strength)
 	    return -1;
 	if(this.strength > r.strength)
 	    return 1;
 	return 0; 
    }
    
    public String toString() {
        return "RSSI@<" + x +"," + y + "," + z + "> (id="+getID()+" channel="+channel+" strength = " + strength;
    }
    
}
