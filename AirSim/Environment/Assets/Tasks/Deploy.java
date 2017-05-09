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
 * Deploy.java
 *
 * Created on October 8, 2007, 2:49 PM
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
public class Deploy extends Move {
    
    private Asset mountedAsset = null;
    private boolean isFinishedHere = false;
    
    /** Creates a new instance of Deploy */
    public Deploy(Vector3D destination) {
        super(destination);
    }
    
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
 
    public void step(AirSim.Environment.Assets.Asset asset, long time) {
	// @TODO: This should be handled differently... the C130's are
	// the only thing that use Deploy right now, and the Env.txt
	// for the current demo starts them off with a "Land" task so
	// they don't leap right into the fray.  So when they get the
	// task to "deploy" their speed is zero so they go nowhere, so
	// we have to set it here...
	if(asset.getSpeed() <= 0)
	    asset.setSpeed(asset.getMaxSpeed());
	
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
        
	// TODO: Difference between deploy and transport is, we're not
	// stopping - i.e.  don't set speed to 0, don't execute land.
	// Doing this in a rush right now - think about it more later.
        if (isFinished) {
            //dismount all assets
            isFinishedHere = true;
            Machinetta.Debugger.debug(1, asset.getID() + ": Finished moving! Deploying/Unload All invoke at transport loc "+asset.location.toString()+" !!!");
	    asset.unloadAll();                
	    
        } else {
            // System.out.println("Attacking on way to target ... " + toWaypoint.length());
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            
            super.step(asset, time);
        }
    }
    
    public String toString() { return "Deploy:" + destination; }
    
}
