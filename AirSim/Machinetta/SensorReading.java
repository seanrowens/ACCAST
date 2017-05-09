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
 * SensorReading.java
 *
 * Created on September 9, 2005, 10:00 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.Asset.Types;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Util.*;
import java.util.Hashtable;
import java.io.*;


/**
 * SensorReading is a belief class used by proxies to share SARSensor
 * sensor readings with other proxies.  Be careful to avoid confusing
 * this class with the AirSim.SensorReading class, which does not
 * extend Belief.
 *
 * @author pscerri
 */
public class SensorReading extends Belief  {
    
    /** This is ugly overloading, but I am too tired right now to do any better.
     *
     * Obviously the need for most of the fields below depends on this flag.
     */
    public boolean isSAR = false;
    
    public AirSim.Environment.Assets.Asset.Types type = null;
    public PositionMeters loc = null;
    public double heading = 0.0;
    public long time = System.currentTimeMillis();
    public Hashtable <Types, Double> SARProbs = new Hashtable<Types, Double>();
    public ProxyID sensor = null;
    public State state = null;
    public ForceId forceId = ForceId.UNKNOWN;
    
    // @TODO: We probably need a field for recording state as a 0.0
    // (dead) to 1.0 (live), and maybe mobility kill/firepower kill 
    //
    // And also a field for opfor/bluefor/unknown?
    //
    // Don't forget to add to read/write external

//     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
// 	isSAR = in.readBoolean();
// 	type = (Types)in.readObject();
// 	loc = new PositionMeters();	// @TODO: I think this is unnecessary
// 	loc.readExternal(in);
// 	heading = in.readFloat();
// 	time = in.readLong();
// 	sensor = (ProxyID)in.readObject();
// 	state = (State)in.readObject();
// 	forceId = (ForceId)in.readObject();
// 	int size = in.readShort();
// 	Types inType;
// 	double inProb;
// 	for(int loopi = 0; loopi < size; loopi++) {
// 	    inType = (Types)in.readObject();
// 	    inProb = in.readFloat();
// 	    SARProbs.put(inType, inProb);
// 	}
//     }

//     public void writeExternal(ObjectOutput out) throws IOException {
// 	out.writeBoolean(isSAR);
// 	out.writeObject(type);
// 	loc.writeExternal(out);
// 	out.writeFloat((float)heading);
// 	out.writeLong(time);
// 	out.writeObject(sensor);
// 	out.writeObject(state);
// 	out.writeObject(forceId);
// 	out.writeShort((short)SARProbs.size());
// 	double prob = 0.0;
// 	for(Types type: SARProbs.keySet()) {
// 	    prob = SARProbs.get(type);
// 	    out.writeObject(type);
// 	    out.writeFloat((float)prob);
// 	}
//     }

    public SensorReading() {
    }

    /** Creates a new instance of SensorReading */
    public SensorReading(Types type, PositionMeters loc, ProxyID sensor,State state) {
        this.type = type;
        this.loc = loc;
        this.sensor = sensor;
	this.state = state;
    }
    public SensorReading(Types type, PositionMeters loc, ProxyID sensor) {
        this.type = type;
        this.loc = loc;
        this.sensor = sensor;
	this.state = State.UNKNOWN;
    }
    
    public SensorReading(AirSim.SARSensorReading ssr) {
        loc = new PositionMeters(ssr.x, ssr.y, ssr.z);
        SARProbs.putAll(ssr.probs);
        sensor = ssr.sensor;
        isSAR = true;
	state = ssr.state;
    }
    
    public boolean isSAR() { return isSAR; }
    
    public Asset.Types getMostLikely() {
        Asset.Types type = null;
        double p = -1.0;
	for(Asset.Types key: SARProbs.keySet()) {
            if (SARProbs.get(key) > p) {
                p = SARProbs.get(key);
                type = key;
            }
        }
        return type;
    }

    public BeliefID makeID() {
        if (id == null) {
            id = new BeliefNameID(loc+":"+time);
        }
        return id;
    }
    
    public static final long serialVersionUID = 1L;
    
    public String toString() {
        if (isSAR) {
            return "SensorReading: SAR from " + sensor + " at " + loc + " @ " + time + ": " + SARProbs;
        } else {
            return "SensorReading: " + type + " sensed by " + sensor + " at " + loc + " @ " + time;
        }
    }
}
