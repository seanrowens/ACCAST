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
 * Flee.java
 *
 * Created on September 21, 2007, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Area;
import AirSim.SensorReading;
import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.GroundVehicle;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class Flee extends Move {
    
    /*
    private Asset closestAirAsset = null;
    private double distanceToClosestAirAsset = -1.0;
    private Asset closestGroundAsset = null;
    private double distanceToClosestGroundAsset = -1.0;
    */
    private Asset closestEnemyAsset = null;
    private double distanceToClosestAsset = -1.0;
    
    private boolean fireAtAir = true;
    private boolean fireAtGround = true;
    
    /** Creates a new instance of Flee */
    public Flee(Vector3D destination) {
        super(destination);
    }
        
    //TODO: after finding the closest enemy asset, calculate the opposite direction to run away.
    //  In this case, we can set the destination or not.
    public void step(Asset asset, long time) {
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
        
        // if (!isFinished) System.out.println("Attacking toWaypoint: " + toWaypoint + ", dest " + destination + ", asset " + asset.location);
        
        if (isFinished) {
            // Nada, shouldn't really be getting called
        } else {
            // System.out.println("Attacking on way to target ... " + toWaypoint.length());
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            
            super.step(asset, time);
        }
    }
    
    public boolean finished(Asset a) {
        return isFinished;
    }
    
    public String toString() { return "Flee:" + destination; }
    
}
