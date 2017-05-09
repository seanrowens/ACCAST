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
 * OtherVehicleCostMap.java
 *
 * Created on July 26, 2006, 5:29 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import AirSim.Machinetta.Path3D;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.ProxyID;

import java.awt.Rectangle;
import java.util.*;

/**
 *
 * @author pscerri
 */
public class OtherVehicleCostMap implements CostMap {
    
    final private static double COST_SCALE = 100.0;
    final private static double CONFLICT_COST = 10000.0;

    private double avoidRange = 100.0;
    private double avoidRangeSqd;
    private double conflictRange = 100.0;
    private double conflictRangeSqd;
    private double conflictZRange = 100.0;

    private ProxyID proxyid;

    HashMap<ProxyID,Path3D>otherPaths = new HashMap<ProxyID,Path3D>();
    HashMap<ProxyID,Vector3D[]>otherPathsAry = new HashMap<ProxyID,Vector3D[]>();
    
    /** Creates a new instance of OtherVehicleCostMap 
     * @param avoidRange maximum range between a point we're getting cost
     * for and a point on any path.  Costs will be scaled from 0 to
     * max avoidRange.
     **/
    public OtherVehicleCostMap(double avoidRange, double conflictRange, double conflictZRange) {
	this.avoidRange = avoidRange;
	this.avoidRangeSqd = avoidRange*avoidRange;
	this.conflictRange = conflictRange;
	this.conflictRangeSqd = conflictRange*conflictRange;
	this.conflictZRange = conflictZRange;
    }

    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
	long startTime = Math.min(t1,t2);
	long endTime = Math.max(t1,t2);

        int numPoints = 10;
	Waypoint[] points = new Waypoint[numPoints];
	double[] dSqd = new double[numPoints];
	Waypoint[] nearest = new Waypoint[numPoints];
	for (int loopi = 1; loopi < (numPoints-1); loopi++) {
	    nearest[loopi] = null;
	}

	points[0] = new Waypoint(x1,y1,z1,t1);
	points[numPoints-1] = new Waypoint(x2,y2,z2,t2);
	for (int loopi = 1; loopi < (numPoints-1); loopi++) {
	    points[loopi] = Waypoint.interpolate(points[0],points[numPoints-1],((double)loopi)/((double)(numPoints-1)));
	    //	    Machinetta.Debugger.debug("points["+((int)loopi)+"] = "+points[loopi].toString(),1,this);
	}

	synchronized(otherPathsAry) {
	    for(ProxyID key: otherPathsAry.keySet()) {

		if((null != proxyid) && (key != null))
		    if(key.equals(proxyid))
			continue;

		// @TODO: Need to (maybe?) make this interpolate the otherpath
		// points... might be really slow if we do that.
	    
		// probably should just force this to be a waypoint...
		Vector3D pathp;
		Vector3D[] path = otherPathsAry.get(key);
		for(int loopi = 0; loopi < path.length; loopi++) {
		    pathp = path[loopi];
		    if(null == pathp)
			continue;
		    if(pathp instanceof Waypoint) {
			if((((Waypoint)pathp).getTime() < startTime) 
			   || (((Waypoint)pathp).getTime() < endTime))
			    continue;
		    }
		    for(int loopj = 0; loopj < numPoints; loopj++) {
			Waypoint wp = points[loopj];
			if((wp.z < (pathp.z - (conflictZRange*1.5))) || (wp.z > (pathp.z + (conflictZRange*1.5))))
			    continue;

			double distSquare = wp.toVectorLengthSqd(pathp);
			if(null == nearest[loopj]) {
			    dSqd[loopj] = distSquare;
			    nearest[loopj] = wp;
			}
			else if(distSquare < dSqd[loopj]) {
			    dSqd[loopj] = distSquare;
			    nearest[loopj] = wp;
			}
		    }
		}
	    }
	}

	// Now we have zero or more non null nearest points.  We
	// calculate cost in numPoints pieces.  Any point that doesn't
	// have a nearest neighbor is simply 0 cost.  (Although that
	// should only happen when we have no paths at all in this
	// map.)
	//
	// If a point is > avoidRange away from nearest path point then cost is 0.
	//
	// If a point is < conflictRange away from nearest path point
	// then cost is CONFLICT_COST - something really high
	//
	// Otherwise cost is (1 - (dist/avoidRange))/numPoints - i.e. the
	// closer the distance is to avoidRange, the closer the cost
	// goes to 0, and the closer the distance is to 0, the closer
	// the cost is to (1.0/numPoints)
	//
	// @todo: In general, the entire "really high conflict cost"
	// thing makes sense, but in general I wonder if it isn't a
	// good idea to require all 'getCost' functions to return a
	// value between 0.0 and 1.0.

	double scaledCost = 0.0;
	for(int loopi = 0; loopi < numPoints; loopi++) {
	    if(null != nearest[loopi]) {
		if(dSqd[loopi] > avoidRangeSqd)
		    continue;
		if(dSqd[loopi] < conflictRangeSqd)
		    scaledCost += CONFLICT_COST;
		double dist = Math.sqrt(dSqd[loopi]);
		scaledCost += (1.0 - (dist/avoidRange))/numPoints;
	    }
	}

        return scaledCost;
    }

    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {        
        return getCost(x1,y1,0.0,t1,x2,y2,0.0,t2);
    }

    public void timeElapsed(long t) {
        // Nothing to do here.
    }

    public ArrayList<Rectangle> getGoodAreas(long t) {
        // Nothing to return here.
        return null;
    }

    public ArrayList<Rectangle> getBadAreas(long t) {
        // Maybe implement later
        return null;
    }        
    
    public void addPath(Path3D path) {
        Machinetta.Debugger.debug("Adding path for "+path.getAssetID(),-1,this);
	otherPaths.put(path.getAssetID(),path);
	synchronized(otherPathsAry) {
	    otherPathsAry.put(path.getAssetID(), path.getWaypointsAry());
	}
    }
    
    public void removePath(Path3D path) {
        Machinetta.Debugger.debug("Removing path for "+path.getAssetID(),1,this);
        otherPaths.remove(path.getAssetID());
	synchronized(otherPathsAry) {
	    otherPathsAry.remove(path.getAssetID());
	}
    }

    public Iterator<Path3D> getIterator() {
	return otherPaths.values().iterator();
    }

    
}
