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
 * DefendAir.java
 *
 * Created on August 17, 2005, 5:50 PM
 *
 */

package AirSim.Environment.Assets.Tasks;

import Machinetta.Debugger;
import AirSim.SensorReading;
import AirSim.Environment.Waypoint;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Vector3D;
import java.util.Random;

/**
 * @todo SRO Mon Mar 27 15:53:24 EST 2006 - this really needs to be
 * reworked and made more reusable.
 *
 * Notice fireAt* flags, this task may fire at only things on ground
 * or only things in the air, or both.
 *
 * @author pscerri
 */
public class DefendAir extends Task {

    private final static double MIN_DIST_TO_FIRE_METERS = 400;
    private Asset closestAirAsset = null;
    private double distanceToClosestAirAsset = -1.0;
    private Asset closestGroundAsset = null;
    private double distanceToClosestGroundAsset = -1.0;

    private Random rand = new Random();

    private boolean fireAtAir = true;
    private boolean fireAtGround = true;
    // This is used to print out ONE warning the first time step is
    // called, if both fireAtAir and fireAtGround are false
    private boolean cantFireWarning = true;	
    private boolean cantFireAirWarning = true;	
    private boolean cantFireSurfaceWarning = true;	
    
    private RandomMove randomMove = null;

    /** Creates a new instance of DefendAir */
    public DefendAir() {
    }

    public DefendAir(boolean defendAir, boolean defendGround) {
	fireAtAir = defendAir;
	fireAtGround = defendGround;
    }

    public DefendAir(boolean defendAir, boolean defendGround, int xcenter, int ycenter, int width, int height) {
	fireAtAir = defendAir;
	fireAtGround = defendGround;
	randomMove = new RandomMove(xcenter, ycenter, width, height);
    }

    /** The never finishes */
    public boolean finished(AirSim.Environment.Assets.Asset a) {
        return false;
    }

    double spinRate = -0.1;

    private void findClosest(Asset a, SensorReading[] readings, int length) {
	double distanceSqdToClosestAirAsset = -1.0;
	closestAirAsset = null;
	double distanceSqdToClosestGroundAsset = -1.0;
	closestGroundAsset = null;
	for(int loopi = 0; loopi < length; loopi++) {
	    SensorReading reading = readings[loopi];

	    if(null == reading) {
		//		Debugger.debug(a.getID()+".findClosest: ERROR ERROR why is reading "+loopi+" null?", 5, this);
		continue;
	    }
	    if(null == reading.asset) {
		try {
		    Debugger.debug(a.getID()+".findClosest:  ERROR ERROR Why is asset null from SensorReading="+reading.toString(), 5, this);
		}
		catch(Exception e) {
		    Debugger.debug(a.getID()+".findClosest:  ERROR ERROR Why is asset null from SensorReading?", 5, this);
		}
		continue;
	    }
	    if(reading.asset.getForceId() == a.getForceId()) 
		continue;	// same side?
	    if(reading.state == State.DESTROYED)
		continue;	// already dead
	    if(reading.asset instanceof WASM)
		continue;	// Stop trying to shoot down the WASMs
// 	    if(reading.asset instanceof SmallUAV)
// 		continue;	// Stop trying to shoot down the WASMs
            if(reading.asset instanceof Civilian)
                continue;	// Don't shoot civilians

	    double xdiff = a.location.x - (double)reading.x;
	    double ydiff = a.location.y - (double)reading.y;
	    double distSqd = ((xdiff * xdiff) + (ydiff * ydiff));
	    if(reading.asset instanceof Aircraft) {
		if((distanceSqdToClosestAirAsset < 0) || (distSqd < distanceSqdToClosestAirAsset)) {
		    distanceSqdToClosestAirAsset = distSqd;
		    closestAirAsset = reading.asset;
		}
	    }
	    if(reading.asset instanceof GroundVehicle) {
		if((distanceSqdToClosestGroundAsset < 0) || (distSqd < distanceSqdToClosestGroundAsset)) {
		    distanceSqdToClosestGroundAsset = distSqd;
		    closestGroundAsset = reading.asset;
		}
	    }
	}
	if(distanceSqdToClosestAirAsset < 0)
	    distanceToClosestAirAsset = -1;
	else
	    distanceToClosestAirAsset = Math.sqrt(distanceSqdToClosestAirAsset);
	if(distanceSqdToClosestGroundAsset < 0)
	    distanceToClosestGroundAsset = -1;
	else
	    distanceToClosestGroundAsset = Math.sqrt(distanceSqdToClosestGroundAsset);
    }

    /**
     * The assets spin around, pretending to look.  They are only
     * spinning to make the graphics look nicer, it makes no 
     * difference to their sensing.
     */
    public void step(AirSim.Environment.Assets.Asset a, long time) {
        if (spinRate < 0) spinRate = rand.nextDouble();
        a.setSpeed(0.0000001);        
        a.heading.turn(spinRate);       
        a.heading.normalize();
	
	if(!fireAtAir && !fireAtGround) {
	    if(cantFireWarning) {
		Debugger.debug("WARNING: " +a.getID()+": both fireAtAir and fireAtGround are false, so we're never going to fire anything.", 3, this);
		cantFireWarning = false;
	    }
	}

	if(fireAtAir && !a.isFireAtAirCapable()) {
	    if(cantFireAirWarning) {
		Debugger.debug("WARNING: " +a.getID()+": fireAtAir is true but asset.isFireAtAirCapable() is false", 3, this);
		cantFireAirWarning = false;
	    }
	}

	if(fireAtGround && !a.isFireAtSurfaceCapable()) {
	    if(cantFireSurfaceWarning) {
		Debugger.debug("WARNING: " +a.getID()+": fireAtGround is true but asset.isFireAtSurfaceCapable() is false", 3, this);
		cantFireSurfaceWarning = false;
	    }
	}

	// @todo: SRO Thu Apr 13 22:50:56 EDT 2006
	// 
	// I think the methods for firing may have been generalized
	// enough (moved into Assets) so that the asset is no longer
	// required to be a Tank.
	if(!(a instanceof Tank)) {
	    Debugger.debug(a.getID()+": Only know how fire a tank, this is not a tank (this is "+a.getClass().getName()+"), giving up.", 5, this);
	    return;
	}
	Tank t = (Tank)a;
	
	if(null != randomMove) {
	    //	    Debugger.debug(a.getID()+": stepping random move.", 1, this);
	    randomMove.step(a, time);
	}

	if(a.isReadyToFire()) {
	    //	    Debugger.debug(a.getID()+": Too soon to fire, lastFired="+a.getStepLastFired()+" stepsBetweenFiring="+a.getStepsBetweenFiring()+", currently step "+a.steps, 1, this);	    
	    return;
	}

	SensorReading[] readings = a.getMemory().getAllKnownAssets();

	if(null == readings)
	    return;
	if(null == readings[0])
	    return;

	findClosest(a, readings, readings.length);

	if((null == closestAirAsset) && (null == closestGroundAsset)) {
	    // 	    Debugger.debug(a.getID()+": Nothing to shoot at.", 1, this);
	    return;
 	}
	if(null != closestAirAsset && fireAtAir) {
	    if(distanceToClosestAirAsset < t.getFireDist()) {
		if(distanceToClosestAirAsset >= MIN_DIST_TO_FIRE_METERS) {
		    Debugger.debug(a.getID()+": Shooting SA missile at " +closestAirAsset.getID(), 1, this);
		    t.fireSAMissile((Aircraft)closestAirAsset);
		    return;
		}
	    }
	}
	if(null != closestGroundAsset && fireAtGround) {
	    if(distanceToClosestGroundAsset < t.getFireDist()) {
		if(distanceToClosestGroundAsset >= MIN_DIST_TO_FIRE_METERS) {
		    Debugger.debug(a.getID()+": Shooting SS missile at " +closestGroundAsset.getID(), 1, this);
		    t.fireSSMissile((GroundVehicle)closestGroundAsset);
		    return;
		}
	    }
	}
    }

    public String toString() {
	return "TASK DEFENDAIR fireAtAir="+fireAtAir+", fireAtGround="+fireAtGround;
    }
    
}
