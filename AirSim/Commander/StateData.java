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
 * To change this template, choose Tools | Templates
import Machinetta.State.BeliefType.ProxyID;
 * and open the template in the editor.
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;
import AirSim.Machinetta.Point2D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.SimTime;
import Machinetta.State.BeliefType.Belief;
import Machinetta.Debugger;
import Machinetta.State.BeliefType.ProxyID;
import Gui.ForceIds;
import Gui.UnitTypes;
import Util.*;

import java.util.*;
import java.text.DecimalFormat;
import java.awt.image.BufferedImage;


/**
 *
 * 
 * This class represents the general data structure to store and keep
 * current state data for a single entity - either our own assets or
 * enemy assets.
 *
 * @author junyounk
 */
public class StateData {
    private final static long ASSUMED_DEAD_AFTER_NO_ASSET_STATE_BELIEF_MS = 20000;

    private final static DecimalFormat fmt = new DecimalFormat("000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    // Keep n most recent beliefs
    private final static int NUM_BELIEFS_TO_KEEP = 20;

    private static Integer idCounter = 1;

    /**
     * Generates a new id with the given belief type - note, we do not
     * always generate id's, we use proxyid wherever possible,
     * i.e. for our own assets.
     * 
     * 
     * @param belieftype To indicate a belief type
     * @return Generated id
     */
    private String makeId() { 
	String newid=null;
	synchronized(idCounter) {
	    if(null != type)
		newid = type+"."+fmt.format(idCounter++);
	    else 
		newid = Asset.Types.UNKNOWN+"."+fmt.format(idCounter++);
	}
	return newid;
    }

    private String key = makeId();
    public String getKey() { 
	if(null == key)
	    key = makeId();
	return key; 
    }
    public void setKey(String value) { key = value; }
    private ProxyID pid = null;
    public ProxyID getPid() { return pid; }
    public void setPid(ProxyID value) { pid = value; expirable = false; }
    public boolean expirable = true;
    public boolean isExpirable() { return expirable; }

    public boolean fusable = true;
    public boolean isFusable() { return fusable; }
    public void setFusable(boolean value) { fusable = value; }


    // We calculate heading from the most recent two successive
    // locations.  When we are checking to see if a new observation
    // matches this one, we can calcuate a 'presumed' heading from the
    // difference between the new observation location and the most
    // recent observation location, then check if that matches this
    // vehicle for speed and heading.
    public LinkedList<Belief> beliefs = new LinkedList<Belief>();
    public LinkedList<Vector3D> trace = new LinkedList<Vector3D>();

    private long lastUpdateTimeMs = System.currentTimeMillis();
    public long getLastUpdateTimeMs() { return lastUpdateTimeMs; }
    public void setLastUpdateTimeMs(long value) { lastUpdateTimeMs = value; }
    public Asset.Types type = Asset.Types.UNKNOWN;
    public Asset.Types getType() { return type; }
    public void setType(Asset.Types value) { type = value; }
    public double confidence = -1;
    public double getConfidence() { return confidence; }
    public void setConfidence(double value) { confidence = value; }
    public Vector3D location = null;
    public Vector3D getLocation() { return location; }
    public void setLocation(Vector3D value) { location = value; }
    
    private Vector3D normalizedHeading;
    private double headingDegrees = 0.0;
    public double getHeadingDegrees() { return headingDegrees; }
    public void setHeadingDegrees(double value) { 
	headingDegrees = value; 
	normalizedHeading = new Vector3D(1.0,0,0);
	normalizedHeading.setXYHeading(headingDegrees);
	normalizedHeading.normalize();
    }
    public void setBothHeadings(double value, Vector3D nHeading) { 
	headingDegrees = value;
	normalizedHeading = nHeading;
    }

    public double speedMetersPerSecond = 0.0;
    public double getSpeedMetersPerSecond() { return speedMetersPerSecond; }
    public void setSpeedMetersPerSecond(double value) { speedMetersPerSecond = value; }
    public double ugsRange = 0.0;
    public boolean ugsPresent = false;
    public BufferedImage img = null;
    public PlannedPath plannedPath = null;
    public UGSSensorReading lastUGSReading = null;

    public Point2D center = null;
    public double width = 0;
    public double height = 0;

    public boolean plannedPathConflict = false;
    public boolean isPlannedPathConflict() { return plannedPathConflict; }
    public void setPlannedPathConflict(boolean value) { plannedPathConflict = value; }
    public boolean isMounted = false;
    public double armor = -1.0;
    public double damage = -1.0;
    public State state = State.UNKNOWN;
    public State getState() { return state; }
    public void setState(State value) { state = value; }
    public ForceId forceId = ForceId.UNKNOWN;
    public ForceId getForceId() { return forceId; }
    public void setForceId(ForceId value) { forceId = value; }

    public Vector3D destLocation = null;

    public BasicRole basicRole = null;

    // @TODO: really this is probably not complicated enough - we're
    // trying to balance "have we heard from them lately" against
    // possibly incorrect sensor reports from their teammates.
    public boolean assumedDead = false;

    // This is only set by AssetStateBelief's, not by sensor reports
    // by third parties about them. @TODO: Should we also set this if
    // we receive a sensor report FROM the asset?  (Not ABOUT the
    // asset.)  Might be a good idea but there could be some confusion
    // if VehicleBeliefs are being randomly shared and bouncing
    // around...  well, we can't really since VehicleBelief's don't
    // have an owner proxyID.  Bleh.  
    private long lastReportedTimeMs = System.currentTimeMillis();
    public long getLastReportedTimeMs() { return lastReportedTimeMs; }
    public void setLastReportedTimeMs(long value) { 
	if(value > lastReportedTimeMs) {
	    Debugger.debug(1,"StateData: pid "+pid+" no longer assumed dead after "+(System.currentTimeMillis() - lastReportedTimeMs));
	    lastReportedTimeMs = value; 
	    assumedDead = false;
	}
    }

    public boolean checkAssumedDead() { 
	long simTimeNow = SimTime.getEstimatedTime();
	long expected = ASSUMED_DEAD_AFTER_NO_ASSET_STATE_BELIEF_MS + lastReportedTimeMs;
	if(expected < simTimeNow) {
	    assumedDead = true;
	}
	if(State.DESTROYED == state)
	    assumedDead = true;

	return assumedDead;
    }

    // @TODO: This just returns the current location, at the moment.
    // Near term plans include using the heading and speed to
    // extrapolate future locations.  If we had a list of old
    // locations we could estimate the location at any point along
    // that list by interpolating.  We could also do clever things
    // like checking if they are on a road and if so extrapolating
    // along the road.
    public Vector3D estimateLocation(long timeMs) {
	boolean useLastLoc = true;

	if(useLastLoc) 
	    return location;

	if((speedMetersPerSecond == 0.0)
	   || (null == normalizedHeading)
	   || (null == location)
	   )
	    return location;
	
	long timeDiffMs = timeMs - lastUpdateTimeMs;
	double length = (timeDiffMs * speedMetersPerSecond)/1000;
	Vector3D estLocation = new Vector3D(location);
	// @TODO: there should be a better way to do this, perhaps
	// built into Vector3D.
	estLocation.x = normalizedHeading.x * length;
	estLocation.y = normalizedHeading.y * length;
	estLocation.z = normalizedHeading.z * length;
	return estLocation;
    }



    /**
     * Creates a instance of StateData
     */
    public StateData() {
    }


    public String toString() {
	return "pid " +((null == pid) ? "null" : pid)
	    +" key "+((null == key) ? "null" : key )
	    +" loc "+((null == location) ? "null" : location.toString())
	    +" type "+((null == type) ? "null" : type)
	    +" conf "+confidence 
	    + " state "+((null == state) ? "null" : state )
	    +" forceid "+((null == forceId) ? "null" : forceId)
	    ;
    }
}
