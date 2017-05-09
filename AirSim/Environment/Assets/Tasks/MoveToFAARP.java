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
 * MoveToFAARP.java
 *
 * Created on October 23, 2007, 5:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Assets.Tasks.Land;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class MoveToFAARP extends Move {
    
    private Asset refuelingAsset = null;
    private boolean isFinishedHere = false;

    /** Creates a new instance of Refuel */
    public MoveToFAARP(Vector3D destination, Asset a) {
        super(destination);
        this.refuelingAsset = a;
        //Machinetta.Debugger.debug(1, "MoveToFAARP is spawned!!!");
    }
       
    //@NOTE: After getting to FAARP location, request refuel!
    //  In this case, we can set the destination or not.
    public void step(Asset asset, long time) {
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
       
        if (isFinished) {
            Machinetta.Debugger.debug(1, "Asset "+asset.getID()+" arrived at FAARP, spawning land task.");
	    //            asset.setSpeed(0);
            isFinishedHere = true;
            
            //For testing
            Land landTask = new Land(asset, new Vector3D(asset.location.x, asset.location.y, 0), refuelingAsset, Land.OPT_REFUEL);
            asset.addTaskToAsset(landTask);
        } else {
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            super.step(asset, time);
        }
    }
    
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
    
    public String toString() { return "MoveToFAARP:" + destination; }
    
}
