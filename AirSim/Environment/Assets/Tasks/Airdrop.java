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
 * Airdrop.java
 *
 * Created on October 8, 2007, 2:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Machinetta.Messages.*;
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
public class Airdrop extends Move {
    
    private Asset mountedAsset = null;
    private boolean airdropFinished = false;
    private boolean returningToBase = false;
    private boolean landing = false;
    private Land land;
    private Vector3D base;
    private double atDestRange;
    
    /** Creates a new instance of Airdrop */
    public Airdrop(Vector3D destination, Vector3D base, double atDestRange) {
        super(destination);
	this.base = base;
	// @TODO: Don't use this - move uses
	// asset.getAtLocationDistance() - we could use atDestRange in
	// step() to setAtLocationDistance() on the asset - may do
	// that if we have too.
	this.atDestRange = atDestRange;
    }
    
    public boolean finished(Asset a) {
        return airdropFinished;
    }
 
    public void step(AirSim.Environment.Assets.Asset asset, long time) {
	// @TODO: This should be handled differently... the C130's are
	// the only thing that use Airdrop right now, and the Env.txt
	// for the current demo starts them off with a "Land" task so
	// they don't leap right into the fray.  So when they get the
	// task to "Airdrop" their speed is zero so they go nowhere, so
	// we have to set it here...
	if(asset.getSpeed() <= 0)
	    asset.setSpeed(asset.getMaxSpeed());
	
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
        
	Vector3D dest2d = new Vector3D(destination.x, destination.y, asset.location.z);
	double distanceToDest = dest2d.toVector(asset.location).length();

	if(landing) {
	    land.step(asset,time);
	    if(land.finished(asset)) {
		// This stops the Airdrop task from ever being stepped() again.
		airdropFinished = true;	
		// And inform our proxy.
		AirdropAP aap = new AirdropAP();
		aap.atAirdropPoint = true;
		aap.airdropExecuted = true;
		aap.returnedToBase = true;
		aap.time = time;
		asset.sendToProxy(aap);
	    }
	    return;
	}

	if(distanceToDest < atDestRange) {
	    if(returningToBase) {
		land = new Land(asset, base, null, Land.OPT_NOTHING);
		landing = true;
	    }
	    else {
		//dismount all assets
		Machinetta.Debugger.debug(1, "At destination, airdropping/Unload All invoke for " + asset.getID() + " at transport loc "+asset.location.toString()+" !!!");
		asset.unloadAll();
		asset.setSpeed(asset.getMaxSpeed());
		returningToBase = true;
		destination = base;
		isFinished = false;

		// Inform our proxy we have dropped (and are returning
		// to base)
		AirdropAP aap = new AirdropAP();
		aap.atAirdropPoint = true;
		aap.airdropExecuted = true;
		aap.returnedToBase = false;
		aap.time = time;
		asset.sendToProxy(aap);
	    }
	}

	// System.out.println("Attacking on way to target ... " + toWaypoint.length());
	if(asset instanceof Aircraft)
	    turnToVector((Aircraft)asset, toWaypoint);
	else
	    asset.heading = toWaypoint;
	asset.heading.normalize2d();
	super.step(asset, time);
    }
    
    public String toString() { return "Airdrop:" + destination; }
    
}
