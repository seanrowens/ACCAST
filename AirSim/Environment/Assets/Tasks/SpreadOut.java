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
 * SpreadOut.java
 *
 * Created on November 6, 2007, 1:33 PM
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
import AirSim.Environment.Assets.IntelligentMine;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class SpreadOut extends Move {
    
    private boolean isFinishedHere = false;
    
    /** Creates a new instance of SpreadOut */
    public SpreadOut(Vector3D dest) {
        super(dest);
    }
    
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
    
    public void step(AirSim.Environment.Assets.Asset asset, long time) {
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
        
	// @TODO: Change this from random walk for a single mine to
	// spreading into an area (i.e. modelign entire minefield as a
	// single Asset)

        if (isFinished) {
	    //	    Machinetta.Debugger.debug(1, asset.getID()+" Spreadout task is finished, activating");
            asset.setSpeed(0);
            asset.heading.setLength(0.0);

            ((IntelligentMine)asset).isActivate = true;
            isFinishedHere = true;           
        } else {
	    if(asset.getSpeed() <= 0)
		asset.setSpeed(asset.getMaxSpeed());
	    //	    Machinetta.Debugger.debug(1, asset.getID()+" Spreadout task is moving to destination ="+destination+" speed="+asset.getSpeed());
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            
            super.step(asset, time);
        }
    }
    
    public String toString() { return "SpreadOut:" + destination; }
    
}
