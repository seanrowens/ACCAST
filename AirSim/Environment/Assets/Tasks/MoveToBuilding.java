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
 * MoveToBuilding.java
 *
 * Created on December 3, 2007, 10:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Buildings.*;
import AirSim.Environment.Assets.Tasks.EnterBuilding;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class MoveToBuilding extends Move {
    
    private boolean isFinishedHere = false;
    private Building building = null;
    public Building getDestBuilding() { return building; }
    
    /** Creates a new instance of MoveToBuilding */
    public MoveToBuilding(Vector3D destination, Building b) {
        super(destination);
        this.building = b;        
    }
       
    //@NOTE: After getting FAARP location, request refuel!
    //  In this case, we can set the destination or not.
    public void step(Asset asset, long time) {
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
       
        if (isFinished) {
            //Machinetta.Debugger.debug(1, "*********EnterBuilding is spawned!!!: " + asset.getID() + " got to building location.");
            asset.setSpeed(0);
	    //            asset.heading = null;
            isFinishedHere = true;
            
            EnterBuilding ebTask = new EnterBuilding(true, building);
            asset.addTaskToAsset(ebTask);
            
        } else {
            // System.out.println("Attacking on way to target ... " + toWaypoint.length());
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            
            super.step(asset, time);
        }
    }
    
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
    
    public String toString() { return "MoveToBuilding:" + destination; }
    
}
