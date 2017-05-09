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
 * SmallUAVModel.java
 *
 * Created on March 8, 2006, 11:43 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Waypoint;
import AirSim.Environment.Vector3D;
import java.text.DecimalFormat;

/**
 */
public class SmallUAVModel   {
    private final static DecimalFormat fmt = new DecimalFormat("0.00");

    private final static boolean TESTING = true;

    // From Env.java/Asset.java.  Should really be constructor args.
    private int stepsPerSecond = 10;
    private int msPerStep = 100;
    private double SPEED_SCALE = 1.0;

    String id="";
    double speedMS = 0.0;
    double maxSpeedMS = 0.0;
    double maxTurnRate = 0.0;
    double maxClimbRate=0.0;
    double maxDescendRate=0.0;

    Vector3D lastNavLocation=new Vector3D(0.0,0.0,0.0);
    Vector3D lastNavHeading=new Vector3D(0.0,0.0,0.0);
    long lastNavTime=0;

    Waypoint prevWaypoint=null;
    long prevWaypointSimTimeMs=0;

    Waypoint curWaypoint=null;
    long curWaypointSimTimeMs=0;

    // These are updated either every time we get a nav update or
    // every time someone requests current data and we then
    // approximate them.
    //
    // Question... what happens if we get a new waypoint... beforehand
    // we were modifying these values to go to the old waypoint, now
    // we're modifying them to get to the new waypoint.  Will this
    // cause a problem? Maybe.  We could just ignore it for now and
    // see how much error it causes. Blah.
    Vector3D location = new Vector3D(0.0,0.0,0.0);
    Vector3D heading = new Vector3D(0.0,0.0,0.0);
    long lastEstimateSimTimeMs=0;
    
    synchronized public long getLastEstimateSimTimeMs() { 
	return lastEstimateSimTimeMs;
    }

    /** Creates a new instance of SmallUAVModel */
    public SmallUAVModel(String id, double speedMperS, double maxTurnRate, double maxClimbRate, double maxDescendRate) {
	this.id = id;
	this.speedMS = speedMperS;
	this.maxSpeedMS = speedMperS;
	this.maxClimbRate = maxClimbRate;
	this.maxDescendRate = maxDescendRate;
	this.maxTurnRate = maxTurnRate;
    }

    synchronized public Waypoint getExpectedWaypoint(long atSimTimeMs) {
	if((prevWaypoint != null) && (curWaypoint != null))
	    return Waypoint.interpolate(prevWaypoint,curWaypoint,atSimTimeMs);	
	else
	    return null;
    }

    StringBuffer checkLog = new StringBuffer();
    private void checkExpectedAgainstModeled() {
	Waypoint expectedWp = getExpectedWaypoint(lastEstimateSimTimeMs);
	if(null == expectedWp)
	    return;
	
	Vector3D toExpected = location.toVector(expectedWp);
	double distFromExpected = toExpected.length();
	checkLog.append(" ("+lastEstimateSimTimeMs
			+" "+fmt.format(distFromExpected)
			+" ("
			+fmt.format(expectedWp.x)
			+" "+fmt.format(expectedWp.y)
			+" "+fmt.format(expectedWp.z)
			+")"
			+" ("
			+fmt.format(location.x)
			+" "+fmt.format(location.y)
			+" "+fmt.format(location.z)
			+")"
			+")");
    }
    private void printLog() {
	Machinetta.Debugger.debug("MODEL: LOG "+id+" "+checkLog.toString(),-1,this);
	checkLog.setLength(0);
    }

    private void simulateUntil(long currentSimTimeMs) {
	while(true) {
	    if((lastEstimateSimTimeMs+msPerStep) > currentSimTimeMs)
		break;
	    lastEstimateSimTimeMs += msPerStep;

	    Vector3D toWaypoint = curWaypoint.toVector(location.x, location.y, location.z);

	    // first update heading
	    double nz = 0.0;
	    if (toWaypoint.z > 0) {
		nz = Math.min(maxClimbRate, toWaypoint.z);
	    } else {
		nz = Math.max(-maxDescendRate, toWaypoint.z);
	    }
	    heading.z = nz;

	    double reqdTurn = heading.angleToXY(toWaypoint);
	    if (reqdTurn > 0) 
		reqdTurn = Math.min(maxTurnRate, reqdTurn);
	    else if (reqdTurn < 0)
		reqdTurn = Math.max(-maxTurnRate, reqdTurn);
	    heading.turn(reqdTurn);

	    // then perform movement
	    double distanceMoved = SPEED_SCALE * speedMS/stepsPerSecond;
	    heading.setLength(distanceMoved);

            location.x += heading.x;
            location.y += heading.y;
            location.z += heading.z;

	    checkExpectedAgainstModeled();
	}
    }

    synchronized public void setWaypoint(Waypoint newWaypoint, long simTimeMsSent) {
	Machinetta.Debugger.debug("MODEL: WAYPOINT "+id+" ("+newWaypoint+") at simtime "+simTimeMsSent,-1,this);
	if(curWaypoint != null)
	    simulateUntil(simTimeMsSent);
	printLog();
	prevWaypoint = curWaypoint;
	prevWaypointSimTimeMs = curWaypointSimTimeMs;
	curWaypoint = newWaypoint;
	curWaypointSimTimeMs = simTimeMsSent;

	double distFromWaypoint = location.toVector(newWaypoint).length();
	double timeToGetThere = newWaypoint.time - simTimeMsSent;
	double speedExpected = distFromWaypoint/(timeToGetThere/1000);

	double newSpeed = speedExpected;
	if(speedExpected > (maxSpeedMS*2))
	    newSpeed = (maxSpeedMS*2);
	else if(speedExpected <0)
	    newSpeed = maxSpeedMS/2;
	speedMS = newSpeed;
	Machinetta.Debugger.debug("MODEL: SPEED "+id+" speed set to "+newSpeed+" m/s at simtime "+simTimeMsSent,-1,this);
    }

    // assumptions we make: (perhaps bad ones)
    //
    // 1) caller is always asking for a time later than the time in
    // the latest nav data
    // 
    // 2) caller will never ask for an earlier time after asking for a
    // later time
    synchronized public Vector3D getEstimatedLocation(long atSimTimeMs) {
	if(null == curWaypoint) {
	    return location;
	}
	simulateUntil(atSimTimeMs);
	return location;
    }
    synchronized public Vector3D getEstimatedHeading(long atSimTimeMs) {
	if(null == curWaypoint) {
	    return heading;
	}
	simulateUntil(atSimTimeMs);
	return heading;
    }

    synchronized public void updateNav(double newLat, double newLon, double newAlt, double newHeading, double newVertVel,long simTimeMs) {
	
	//	Machinetta.Debugger.debug("MODEL: UPDATE "+id+" new nav data at simtime "+simTimeMs,1,this);
	if(TESTING) {
	    // we can't compare model against reality if model has no
	    // waypoint to use to simulate
	    if(null != curWaypoint) {
		double distFromExpected=-1;
		Waypoint expectedWp = getExpectedWaypoint(simTimeMs);
		if(null != expectedWp) {
		    Vector3D toExpected = location.toVector(expectedWp);
		    distFromExpected = toExpected.length();
		}
		simulateUntil(simTimeMs);
		double angle = heading.angle();
		Vector3D newLoc = new Vector3D(newLon,newLat,newAlt);
		Vector3D toNewLoc = location.toVector(newLoc);
		double locDiff = toNewLoc.length();
		Vector3D newHeadingVec = new Vector3D(0,0,0);
		newHeadingVec.z = newVertVel;
		newHeadingVec.setXYHeading(newHeading);
		double headingDiff = heading.angleToXY(newHeadingVec);
		if(headingDiff < .0001)
		    headingDiff = 0;
		//Machinetta.Debugger.debug("MODEL: "+id+" dist from exp "+fmt.format(distFromExpected)+" At sim time "+lastEstimateSimTimeMs+" cur wp "+curWaypoint+" last wp "+prevWaypoint+" exp wp "+expectedWp+" model loc "+location+" model heading "+heading+" (angle "+fmt.format(angle)+") new loc "+newLoc+" new heading "+newHeadingVec+" (angle "+fmt.format(newHeading)+") dist from model to new loc "+fmt.format(locDiff)+" angle from model to new heading "+fmt.format(headingDiff),1,this);
	    }
	}	    

	lastNavLocation.x = newLon;
	lastNavLocation.y = newLat;
	lastNavLocation.z = newAlt;
	lastNavHeading.z = newVertVel;
	lastNavHeading.setXYHeading(newHeading);
	lastNavTime = simTimeMs;
	
	location.x = newLon;
	location.y = newLat;
	location.z = newAlt;
	heading.z = newVertVel;
	heading.setXYHeading(newHeading);
	lastEstimateSimTimeMs = simTimeMs;
    }
}
