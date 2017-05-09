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
 * Launch.java
 *
 * Created on October 29, 2007, 3:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.SmallUAV;
import AirSim.Environment.Assets.Tasks.Move;
import AirSim.Environment.Assets.Tasks.RandomMove;
import AirSim.Environment.Vector3D;
import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class Launch extends Task {

    private boolean isFinished, isFinishedHere = false;
    Vector3D destination = null;
    Asset curAsset = null;
    
    /** Creates a new instance of Launch */
    public Launch(Asset curAsset, Vector3D destination) {
        this.destination = destination;
        this.curAsset = curAsset;
        curAsset.setSpeed(curAsset.getMaxSpeed());
    }
    
    public void step(AirSim.Environment.Assets.Asset a, long time) {
        Vector3D heading = destination.toVector(a.location.x, a.location.y, a.location.z);
       
        if (isFinished) {            
            //Do nothing
            isFinishedHere = true;
            RandomMove randomMove = new RandomMove((int)a.location.x, (int)a.location.y, 5000, 5000);
            a.addTaskToAsset(randomMove);
            Machinetta.Debugger.debug(1, "Launch of "+a.getID()+" is finished2.");
	    a.resumeInterrupt();
        } else {
            //only change elevation of z value;
            //a.location.z += heading.z * time;
            a.location.z += a.getSpeed() * time;
            if(a.location.z >= destination.z) {
                a.location.z = destination.z;
                isFinished = true;
		Machinetta.Debugger.debug(1, a.getID() +" launch task is finished1.");
            }
            
            //Machinetta.Debugger.debug(1, "  *******UAV Height: " + a.location.z);
            
            a.moveToConfiguredDest();
        }        
    }
    
    /** The never finishes */
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
  
}

