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
 * Strike.java
 *
 * Created on September 27, 2007, 6:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;

/**
 *
 * @author junyounk
 */
public class Attack extends Move {

    protected int STRIKE_RADIUS = 500;
    protected boolean strikeMode = false;
        
    public Attack(Vector3D destination) {
        super(destination);
    }
    
    public Attack(Vector3D destination, double speed) {
        super(destination, speed);
    }
    
    public void step(Asset asset, long time) {
        if (isFinished)
	    return;

	// @TODO: This should be handled differently... the C130's are
	// the only thing that use Deploy/Transport right now, and the
	// Env.txt for the current demo starts them off with a "Land"
	// task so they don't leap right into the fray.  So when they
	// get the task to "deploy" their speed is zero so they go
	// nowhere, so we have to set it here...
	//
	// HACK HACK HACK - the goal here is to slow the ugv down to
	// infantry speeds when it attacks, so it doesn't leave the
	// infantry behind.  It doesn't seem to work and should be
	// deleted soon.
	if(asset.getSpeed() <= 0) {
	    if(asset.getType() == Asset.Types.AUGV) 
	       asset.setSpeed(Infantry.MAX_SPEED_METERS_PER_SEC);
	    else 
	       asset.setSpeed(asset.getMaxSpeed());
	}
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);

	if (toWaypoint.length() < STRIKE_RADIUS) {
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
            attack(asset, destination, 5);
        } else {
            // still trying to get there ....
	    //            Machinetta.Debugger.debug(1, asset.getID()+".attack.step(): moving towards attack dest="+destination.toString());
            super.step(asset, time);
	    
        }
    }
    
    protected void attack(Asset asset, Vector3D destination, double dist) {
    }
    
    public boolean finished(Asset a) {
        return isFinished;
    }
    
    public String toString() { return "ATTACK:" + destination; }
    
}
