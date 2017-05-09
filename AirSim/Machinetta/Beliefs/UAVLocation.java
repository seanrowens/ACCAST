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
 * UAVLocation.java
 *
 * Created on February 18, 2006, 2:04 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Environment.Vector3D;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;

/**
 * Used by UAV proxies that wish to share their current
 * location/heading/speed with other proxies.
 *
 * @author pscerri
 */
public class UAVLocation extends Belief {
            
    public double latitude, longtitude, altitude, heading, groundSpeed;
    public ProxyID id;
    private long time = System.currentTimeMillis();
    public int isMounted = 0; //0:unmounted, 1:mounted
    public int isLive = 1; //0:dead, 1:live
    
    /** Creates a new instance of UAVLocation */    
    public UAVLocation(ProxyID id, double latitude, double longtitude, double altitude, double heading, double groundSpeed) {
        this.id = id;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.altitude = altitude;
        this.heading = heading;
        this.groundSpeed = groundSpeed;
        this.isMounted = 0;
        this.isLive = 1;
    }
    
    public UAVLocation(ProxyID id, double latitude, double longtitude, double altitude, double heading, double groundSpeed, int isMounted, int isLive) {
        this.id = id;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.altitude = altitude;
        this.heading = heading;
        this.groundSpeed = groundSpeed;
        this.isMounted = isMounted;
        this.isLive = isLive;
    }
    
    public Vector3D asVector() {
        return new Vector3D(longtitude, latitude, altitude);
    }
    
    public BeliefID makeID() {
        return new BeliefNameID("Loc:" + id + "@" + time);
    }

    public long getTime() {
        return time;
    }
    
    public String toString() {
        return id + "@" + " " + longtitude + ", " + latitude;
    }
    
}
