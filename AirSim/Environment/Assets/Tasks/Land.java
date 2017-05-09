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
 * Land.java
 *
 * Created on October 29, 2007, 2:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.SmallUAV;
import AirSim.Environment.Assets.Tasks.AskRefuel;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Messages.LandedAP;
import Machinetta.Debugger;
import Machinetta.Communication.Message;

import java.util.*;

/**
 * Currently for landing task, we are not considering fuel consumption.
 * To add this, we can simply change step function in Land task.
 * Also, we need to change isCapableToRefuel method in SmallUAV
 * like (distance with only x, y difference for moving at the same elevation z + z difference for landing at FAARP)
 *
 * @author junyounk
 */
public class Land extends Task {
    public final static int OPT_NOTHING = 0;
    public final static int OPT_UNLOAD = 1;
    public final static int OPT_REFUEL = 2;

    private boolean isFinished, isFinishedHere = false;
    private Vector3D destination = null;
    private Asset curAsset = null;
    private Asset refuelingAsset = null;
    private int opt = OPT_NOTHING;
    
    /** Creates a new instance of Land */
    public Land(Asset curAsset, Vector3D destination, Asset a, int opt) {
        //Machinetta.Debugger.debug(1, "Land is spawned!!!");
        this.curAsset = curAsset;
        this.destination = destination;
        this.refuelingAsset = a;
        //this.curAsset.setSpeed(Asset.kphToms(((SmallUAV)curAsset).getUavMaxSpeedKph()));
        this.curAsset.setSpeed(Asset.kphToms(100.0));
        this.opt = opt;
    }
    
    public Land(Asset curAsset, Asset a, int opt) {
	this(curAsset, curAsset.location, a, opt);
    }

    public void step(AirSim.Environment.Assets.Asset a, long time) {
        Vector3D heading = destination.toVector(a.location.x, a.location.y, a.location.z);
        
        if (isFinished) {
	    Machinetta.Debugger.debug(1, "  Asset "+a.getID()+ " Land task is finished2.");

            isFinishedHere = true;
            a.setSpeed(0);

            if(opt == OPT_NOTHING) {
                //Do nothing
                Machinetta.Debugger.debug(1, "  Asset "+a.getID()+ " landed, now doing nothing.");
            } else if (opt == OPT_UNLOAD) {
                //drop all things
                Machinetta.Debugger.debug(1, "  *******Unload All for " + a.getID());
                a.unloadAll();                
            } else if (opt == OPT_REFUEL) {
                //ask refuel
                Machinetta.Debugger.debug(1, "  Asset "+a.getID()+ " asking to refuel from "+refuelingAsset.getID());
                AskRefuel askRefuelTask = new AskRefuel(a, refuelingAsset);
                a.addTaskToAsset(askRefuelTask);            
            } else {
                Machinetta.Debugger.debug(1, "  Asset "+a.getID()+ " opt is set to something weird, opt="+opt);
	    }

	    LandedAP lap = new LandedAP(new Vector3D(a.location));
	    lap.setCriticality(Message.Criticality.HIGH);
	    a.sendToProxy(lap);
            
        } else {            
            //only change elevation of z value;
            //a.location.z += heading.z * time;
            a.location.z -= a.getSpeed() * time;
            if(a.location.z <= destination.z) {
                a.location.z = destination.z;
                isFinished = true;
		Machinetta.Debugger.debug(1, "  Asset "+a.getID()+ " Land task is finished1.");
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
