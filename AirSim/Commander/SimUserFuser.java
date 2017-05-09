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
 * SimUserFuser.java
 *
 * Created Tue Apr 25 20:38:05 EDT 2006
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Environment.Vector3D;
import Machinetta.Debugger;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.ProxyState;

import java.awt.image.BufferedImage;

import java.util.*;

/**
 * This Fuser is based off the old fuser but the plan is to make it a
 * little cleaner.  The ONLY things this fuser does are;
 *
 * 1) Retrieve StateData objects via keys - a key will be either a
 * generated key or a proxyId if available.
 *
 * 2) Expire StateData instances that are set expirable.  I.e. we will
 * not expire StateData instances for bluefor, for opfor units that
 * are seen to be 'dead'.  We also use a confidence limit to prevent
 * high confidence instances from being expired, although I'm not sure
 * yet if that's wise.
 * 
 * 3) Fuse new sensor readings - i.e. based on a time, location,
 * heading, and type with confidence, find the nearest matching
 * StateData.  This _may_ be a stateData based on an AssetStateBelief,
 * i.e. a bluefor asset.
 *
 * In particular this class doesn't convert other BeleifTypes into
 * StateData types.  That is handled by WorldStateMgr.
 *
 * @author owens
 */
public class SimUserFuser {
    public final static double MAX_FUSION_DISTANCE=500;
    public final static double MAX_FUSION_DISTANCE_SQD=MAX_FUSION_DISTANCE*MAX_FUSION_DISTANCE;
    public final static double MIN_FUSION_THRESHOLD=0.8;

    private ProxyState state;

    private HashMap<String,StateData> keyMap = new HashMap<String,StateData>();
    private HashMap<ProxyID,StateData> pidMap = new HashMap<ProxyID,StateData>();

    private long lastExpirationTimeMs=0;

    public StateData[] getStateData() { 
	return keyMap.values().toArray(new StateData[0]);
    }

    public StateData get(String key) { 
	return keyMap.get(key);
    }

    public StateData get(ProxyID pid) { 
	return pidMap.get(pid);
    }

    public SimUserFuser() {
	state = new ProxyState();
    }
    
    public void addStateData(StateData stateData) {
	keyMap.put(stateData.getKey(),stateData);
	if(null != stateData.getPid())
	    pidMap.put(stateData.getPid(),stateData);
    }

    // Remove a stateData record from the fuser.
    //
    // @param the stateData record to remove.
    // @return void
    public void removeStateData(StateData stateData) {
	keyMap.remove(stateData.getKey());
	if(stateData.getPid() != null)
	    pidMap.remove(stateData.getPid());
    }

    // Remove a stateData record from the fuser.
    //
    // @param the stateData record to remove.
    // @return void
    public void removeStateData(String key) {
	StateData stateData = keyMap.remove(key);
	if(stateData.getPid() != null)
	    pidMap.remove(stateData.getPid());
    }

    // Remove a stateData record from the fuser.
    //
    // @param the stateData record to remove.
    // @return void
    public void removeStateData(ProxyID pid) {
	StateData stateData = pidMap.remove(pid);
	if(null != stateData)
	    keyMap.remove(stateData.getKey());
    }

    // @TODO: Make this take into account heading, type, etc.
    private double compare(StateData stateData, 
			   long timeMs, 
			   Vector3D location, 
			   Vector3D heading, 
			   Asset.Types type, 
			   double confidence) {
	double locationRatio = 0.0;
	
	if(null == location)
	    return 0;

	Vector3D sdLocation = stateData.estimateLocation(timeMs);
	if(null == sdLocation) 
	    return 0.0;
	Vector3D toVector = location.toVector(sdLocation);
	double distanceSqd = toVector.lengthSqd();
	
	if(distanceSqd >  MAX_FUSION_DISTANCE_SQD)
	    locationRatio = 0;
	else
	    locationRatio = (MAX_FUSION_DISTANCE_SQD - distanceSqd)/MAX_FUSION_DISTANCE_SQD;

// 	double headingRatio = 0.0;

// 	double typeRatio = 0.0;

	return locationRatio;
    }

    // Find the closest matching vehicle in our database, within a
    // threshold of MIN_FUSION_THRESHOLD, and MAX_FUSION_DISTANCE.
    //
    // @param timeMs - time in ms of this location/heading.
    // @param location - location to match 
    // @param heading - heading to match
    // @param type - type to match
    // @param confidence - confidence of type to match
    // 
    // @return The nearest match, or null if no match within
    // MIN_FUSION_THRESHOLD found

    public StateData findStateData(long timeMs, Vector3D location, Vector3D heading, Asset.Types type, double confidence) {
	StateData bestStateData = null;
	double bestComparison = 0.0;
	double comparison = 0.0;
	StateData[] vehicles = getStateData();
	if(vehicles.length > 0) {
	    for(int loopi = 0; loopi < vehicles.length; loopi++) {
		StateData stateData = vehicles[loopi];
		if(null == stateData)
		    continue;
		if(!stateData.isFusable())
		    continue;
		comparison = compare(stateData, timeMs, location,  heading, type, confidence);
		if(comparison >= MIN_FUSION_THRESHOLD) {
		    if(comparison > bestComparison) {
			bestComparison = comparison;
			bestStateData = stateData;
		    }
		}
	    }
	}
	return bestStateData;
    }

    public ArrayList<StateData> expireOld(long timeNow, long expirationTimeLimitMs, double expirationConfidenceLimit) {
	ArrayList<StateData> removed = new ArrayList<StateData>();
	
	StateData[] stateAry = getStateData();

	int sizeBefore = stateAry.length;

	long oldestTimeAllowed = timeNow - expirationTimeLimitMs;
	for(int loopi = 0; loopi < stateAry.length; loopi++) {
	    StateData sd = stateAry[loopi];
	    if(null == sd)
		continue;
	    if(!sd.isExpirable())
		continue;
	    if(sd.getLastUpdateTimeMs() < oldestTimeAllowed) {
		if(sd.getConfidence() < expirationConfidenceLimit) {
		    removeStateData(sd);
		    removed.add(sd);
		    Vector3D loc = sd.getLocation();
		    if(null != loc) {
			VehicleBelief b = new VehicleBelief(sd.getLastUpdateTimeMs(),
							    sd.getType(),
							    sd.getConfidence(),
							    (int)loc.x,
							    (int)loc.y,
							    (int)loc.z,
							    sd.getState());
			b.setSensor(sd.getPid());
			b.setForceId(sd.getForceId());
			b.setKey(sd.getKey());
			b.setDelete(true);
			b.setLocallySensed(true);
			state.addBelief(b);
			state.notifyListeners();
		    }
		}
	    }
	}

	return removed;
    }
}
