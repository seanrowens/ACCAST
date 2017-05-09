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
 * Transport.java
 *
 * Created on October 8, 2007, 2:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Machinetta.Messages.*;
import AirSim.Environment.Area;
import AirSim.Environment.Area;
import AirSim.Environment.Assets.C130;
import AirSim.SensorReading;
import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.GroundVehicle;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;
import java.text.DecimalFormat;

/**
 *
 * @author junyounk
 */
public class Transport extends Task {
    
    private boolean isFinished = false;

    private Vector3D destination;
    private Move move;
    private Land land;
    
    /** Creates a new instance of Transport */
    public Transport(Vector3D destination) {
	this.destination = destination;
	move = new Move(destination);
    }
    
    public boolean finished(Asset a) {
        return isFinished;
    }
 
    public void step(AirSim.Environment.Assets.Asset asset, long time) {
	// @TODO: This should be handled differently... the C130's are
	// the only thing that use Deploy/Transport right now, and the
	// Env.txt for the current demo starts them off with a "Land"
	// task so they don't leap right into the fray.  So when they
	// get the task to "deploy" their speed is zero so they go
	// nowhere, so we have to set it here...

	if(isFinished)
	    return;

        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
	double dist = toWaypoint.length();
        
	if(!move.finished(asset)) {
	    setSpeedAndTurnRate(asset,dist);
	    move.step(asset,time);
	}
	else {
            if (asset instanceof Aircraft) {
		if(null == land) {
		    Machinetta.Debugger.debug(1, asset.getID()+": Starting Land part of Transport task");
		    land = new Land(asset, new Vector3D(asset.location.x, asset.location.y, 0), null, Land.OPT_NOTHING);
		}
		land.step(asset,time);
		if(land.finished(asset)) {
		    Machinetta.Debugger.debug(1, asset.getID()+": Finished landing, about to unload all passengers.");
		    unload(asset);
		}
            } else {
		Machinetta.Debugger.debug(1, asset.getID()+": Finished transport, about to unload all passengers.");
		unload(asset);
            }               
            
        }
    }
    private void unload(Asset asset) {
	isFinished = true;
	asset.setSpeed(0);
	asset.holdAll();
	asset.unloadAll();
	TransportAP tap = new TransportAP();
	tap.atTransportDest = true;
	asset.sendToProxy(tap);
    }
    
    // @TODO: Here there be hacks, argh
    private void setSpeedAndTurnRate(Asset asset, double dist) {
	
	// @TODO: Really we should have a dynamics model, however
	// simplistic, associated with each Asset subclass and
	// instance, and base our movement speed/turn rate/etc on
	// current speed and such.  (Possibly two dynamics models for
	// aircraft, one for in the air and one for on the ground.)

	if(asset instanceof GroundVehicle) {
	    asset.setSpeed(asset.getMaxSpeed());
	    return;
	}

	double atLocationMultiple1=7;
	double speedDivisor1=3;
	double atLocationMultiple2=3;
	double speedDivisor2=6;
	
	double distLimit2 = atLocationMultiple2*asset.getAtLocationDistance();
	double speed2 = asset.getMaxSpeed()/speedDivisor2;

	double distLimit1 = atLocationMultiple1*asset.getAtLocationDistance();
	double speed1 = asset.getMaxSpeed()/speedDivisor1;
	
	if(dist < distLimit2) {
	    asset.setSpeed(speed2);
	    //	    Machinetta.Debugger.debug(1, asset.getID()+" at "+asset.location+" dest "+destination+" dist="+Asset.fmt.format(dist)+" < limit="+Asset.fmt.format(distLimit2)+" speed set to "+speed2+", atLocationDist = "+asset.getAtLocationDistance());
	}
	else if(dist < distLimit1) {
	    asset.setSpeed(speed1);
	    //	    Machinetta.Debugger.debug(1, asset.getID()+" at "+asset.location+" dest "+destination+" dist="+Asset.fmt.format(dist)+" < limit="+Asset.fmt.format(distLimit1)+" speed set to "+speed1+", atLocationDist = "+asset.getAtLocationDistance());
	}
	else {
	    asset.setSpeed(asset.getMaxSpeed());
	}
	if(asset instanceof C130) {
	    C130 c130 = (C130)asset;
	    if(c130.getSpeed() >= 150)
		c130.MAX_TURN_RATE = c130.MAX_MAX_TURN_RATE_150MS;
	    else if(c130.getSpeed() >= 100)
		c130.MAX_TURN_RATE = c130.MAX_MAX_TURN_RATE_100MS;
	    else if(c130.getSpeed() >= 50)
		c130.MAX_TURN_RATE = c130.MAX_MAX_TURN_RATE_50MS;
	}
    }


    public String toString() { return "Transport:" + destination; }
    
}
