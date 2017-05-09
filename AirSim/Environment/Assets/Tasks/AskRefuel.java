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
 * AskRefuel.java
 *
 * Created on October 23, 2007, 6:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.FAARP;
import AirSim.Environment.Assets.Tasks.Launch;
import AirSim.Environment.Vector3D;
import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class AskRefuel extends Task {
    
    private boolean isFinished, isFinishedHere = false;
    private boolean isQueuingDone = false;
    private FAARP faarp = null;
    
    /** Creates a new instance of AskRefuel */
    public AskRefuel(Asset askAsset, Asset refuelingAsset) {
        faarp = (FAARP) refuelingAsset;
        isQueuingDone = faarp.addToRefuelQueue(askAsset);
        Machinetta.Debugger.debug(1, "AskRefuel is spawned!!!");
    }
    
    /** The never finishes */
    public boolean finished(Asset a) {
        return isFinishedHere;
    }
 
    //@NOTE: after adding to FAARP's queue, continuously check that is done or not.
    //      However, if addToRefuelQueue task is failed, asset tries that task first.
    public void step(AirSim.Environment.Assets.Asset a, long time) {
        if(isQueuingDone) {
            if(isFinished) {
                // currently Do nothing!
                // but adding like launch or going to random position.
                isFinishedHere = true;
                
		Machinetta.Debugger.debug(1, a.getID() +" is done refueling, spawning launch task");
                //For test
                Launch launchTask = new Launch(a, new Vector3D(a.location.x, a.location.y, 1600));
                a.addTaskToAsset(launchTask);
                
            } else {
                if(a.getRefuelDone()) {
                    isFinished = true;
                } else {
                    isFinished = false;
                }
            }
        } else {
            faarp.addToRefuelQueue(a);
        }
    }
    
}
