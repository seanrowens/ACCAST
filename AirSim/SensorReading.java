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
 * Created on June 20, 2004, 11:42 AM
 */

package AirSim;

import java.io.*;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Env;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  paul
 */
public class SensorReading implements java.io.Serializable {
    
    private static Env env = new Env();

    public int x = -1, y = -1, z = -1;
    public double heading = 0.0;
    public String id = null;
    /** The type of the thing we think we are seeing - probably need to change this. */
    public Asset.Types type = null;
    public State state = null;
    public ForceId forceId = null;
    public ProxyID sensor = null;    
    public long time = env.getSimTimeMs();
    // NOTE: This is transient and hence will never get serialized
    public transient Asset asset = null;
    
    public SensorReading() {
        
    }
    
    /** Creates a new instance of SensorReading */
    public SensorReading(int x, int y, int z, double heading, String id, Asset.Types type, State state, ProxyID sensor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
        this.id = id;
        this.type = type;
        this.state = state;
        this.sensor = sensor;
    }
    
    public void setState(State value) { state = value;}
    public State getState() { return state; }
    
    public int getLocationX() {
        return this.x;
    }
    
    public int getLocationY() {
        return this.y;
    }
    
    public String toString() { return "SensorReading: type: "+ type + " id: " + id + " loc:" + "(" + x + "," + y + "," + z + ") state: " + state + " forceId: "+forceId+" sensor: "+sensor+" heading "+heading; }
    
    public final static long serialVersionUID = 1L;
}
