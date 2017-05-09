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
 * Fuser.java
 *
 * Created Tue Apr 25 20:38:05 EDT 2006
 *
 */

package AirSim.Commander;

import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Environment.Vector3D;
import Machinetta.Debugger;

import java.awt.image.BufferedImage;

import java.util.*;

/**
 * Fuser is the old class used to maintain world state that the new
 * SimUser API is based on. It is not part of the SimUser API, but it
 * may be instructive to review.  It is unclear as of yet whether this
 * class will be used by the SimUser API or replaced by a new class.
 *
 * @author owens
 */
public class Fuser {
    public final static double MAX_FUSION_DISTANCE=2500;
    public final static double MAX_FUSION_DISTANCE_SQD=MAX_FUSION_DISTANCE*MAX_FUSION_DISTANCE;
    public final static double MIN_FUSION_THRESHOLD=0.8;
    private final static double ROUNDING_FACTOR = 500;
    
    private HashMap<String,VehicleData> vehicleMap = new HashMap<String,VehicleData>();

    private long lastExpirationTime=0;

    public VehicleData[] getVehicleData() { 
	return vehicleMap.values().toArray(new VehicleData[0]);
    }

    public VehicleData getVehicleData(String id) { 
	return vehicleMap.get(id);
    }

    public Fuser() {
    }
    
    public VehicleData addBelief(VehicleBelief belief) {
	VehicleData vd = findVehicleData(belief);
	if(null == vd) {
	    return addNewVehicle(belief);
	}
	return vd;
    }

    public VehicleData addImageData(ImageData imageData, BufferedImage img) {
	return addNewVehicle(imageData,img);
    }

    // Remove a vehicleData record from the fuser.
    //
    // @param the vehicleData record to remove.
    // @return void
    public void removeVehicleData(VehicleData vd) {
	vehicleMap.put(vd.getId(), null);
    }

    // Add a new vehicle to our database.  This method should be
    // called only if findVehicleData returns null.
    //
    // @return The created vehicleData
    public VehicleData addNewVehicle(VehicleBelief belief) {
	VehicleData vd = new VehicleData(belief);
	vehicleMap.put(vd.getId(), vd);
	Debugger.debug("addNewVehicle: adding vehicle="+vd.getId()+" at "+vd.getX()+","+vd.getY()+" currently "+vehicleMap.size()+" vehicles", 1, this);	
	return vd;
    }

    // Add a new vehicle to our database.  This method should be
    // called only if findVehicleData returns null.
    //
    // @return The created vehicleData
    public VehicleData addNewVehicle(ImageData imageData, BufferedImage img) {
	VehicleData vd = new VehicleData(imageData,img);
	vehicleMap.put(vd.getId(), vd);
	Debugger.debug("addNewVehicle: adding vehicle="+vd.getId()+" at "+vd.getX()+","+vd.getY()+" currently "+vehicleMap.size()+" vehicles", 1, this);	
	return vd;
    }

    // Add a new vehicle to our database.  This method should be
    // called only if findVehicleData returns null.
    public VehicleData addNewVehicle(VehicleData vd) {
	vehicleMap.put(vd.getId(), vd);
	Debugger.debug("addNewVehicle: adding vehicle="+vd.getId()+" at "+vd.getX()+","+vd.getY()+" currently "+vehicleMap.size()+" vehicles", 1, this);	
	return vd;
    }


    // Find the closest matching vehicle in our database, within a
    // threshold of MIN_FUSION_THRESHOLD, and MAX_FUSION_DISTANCE.
    //
    // @param belief VehicleBelief to match vehicles to.
    // @return The nearest match, or null if no match found.
    // within MIN_FUSION_THRESHOLD found
    static long findCount=0;

    public VehicleData findVehicleData(Vector3D location) {
	findCount++;
	int matchCount = 0;
	VehicleData bestVD = null;
	double bestComparison = 0.0;
	double comparison = 0.0;
	VehicleData[] vehicles = getVehicleData();
	if(vehicles.length > 0) {
	    for(int loopi = 0; loopi < vehicles.length; loopi++) {
		VehicleData vd = vehicles[loopi];
		if(null == vd)
		    continue;
		comparison = vd.compareTo(location,MAX_FUSION_DISTANCE_SQD);
		if(comparison >= MIN_FUSION_THRESHOLD) {
		    matchCount++;
		    if(comparison > bestComparison) {
			bestComparison = comparison;
			bestVD = vd;
		    }
		}
	    }
	}
	return bestVD;
    }

    public VehicleData findVehicleData(VehicleBelief belief) {
	Vector3D location = new Vector3D(belief.getX(), belief.getY(), 0);
	return findVehicleData(location);
    }

    public VehicleData findVehicleData(ImageData imageData) {
	return findVehicleData(imageData.loc);
    }

    public void expireOldSensorReadings(long expirationTimeLimitMs, double expirationConfidenceLimit) {
	VehicleData[] vehicles = getVehicleData();
	int sizeBefore = vehicles.length;
	long now = System.currentTimeMillis();
	long oldestTimeAllowed = now - expirationTimeLimitMs;
	for(int loopi = 0; loopi < vehicles.length; loopi++) {
	    VehicleData vd = vehicles[loopi];
	    if(null == vd)
		continue;
	    if(!vd.isExpirable())
		continue;
	    if(vd.getLastUpdateTimeMs() < oldestTimeAllowed) {
		if(vd.getConfidence() < expirationConfidenceLimit) {
		    removeVehicleData(vd);
		}
	    }
	}
    }
}
