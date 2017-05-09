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
package AirSim.Machinetta;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import AirSim.Environment.Waypoint;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Messages.NextWaypointPA;
import Gui.LatLonUtil;
import Machinetta.ConfigReader;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.RAPInterface.RAPInterfaceImplementation;

// @TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:TODO:
//
// Basically, since PathPlanner has a thread and calls a lot of our
// public methods, and we either update things in the thread from
// stateChanged _or_ in our own thread, we need to add synchronization
// all over the place.
//
// I _think_ I've done this everywhere necessary but this code should
// get another thorough review just to make sure.
//
// Ok, so.... interactions.
//
// UAVRI updates our location periodically - or our thread that polls
// VCBridge does it - either way we get a periodic update of uav
// location.
//
// 	When we update our location we also check if we need to
// 	send a new waypoint, and send it if necessary.
//
// 	When we're coming near the end of our currPath, we notify
// 	PathPlanner that we need a next path - need some way to avoid
// 	redundant notifications and confusion caused by them,
// 	i.e. don't have PathPlanner provide us with 2 or 3 new paths
// 	repeatedly.
//
//	When we reach the end of currpath, we switch in the nextpath,
//	or if it is not available we create a holding pattern.
//
// When planner provides us with a next path, we need to check if
// we're in a holding pattern and if so switch in the new path,
// otherwise just stash it for later when we need it.
//
// When this proxy gets a new role, the pathplanner plans a new path,
// then forces autopilot to drop out current path and next path and
// start the new path.
//
// When a path is in conflict, again, pathplanner plans a new path and
// cancels the plan in conflict - note this MAY be the currPath or the
// nextPath so it's different than a new role.

public class Autopilot implements Runnable,StateChangeListener {
    private static DecimalFormat fmt = new DecimalFormat("0.00000");
    private static DecimalFormat fmt2 = new DecimalFormat("0.0");
   
    private RAPInterfaceImplementation rapInt = null;
    private MiniWorldState miniWorldState = null;
    private SmallUAVModel uavModel = null;

    private PathPlanner planner = null;

    private boolean navDataValid = true;
    private VCBridgeClient vcBridgeClient = null;
    private Waypoint prevWP = null;
    private Waypoint currWaypoint = null;
    private boolean currIsHoldingPattern = false;
    private BeliefID currPathID = null;
    private Path3D currPath = null;
    public synchronized Path3D getCurrPath() {return currPath;}
    private BeliefID nextPathID = null;
    private Path3D nextPath = null;
    //    public synchronized Path3D getNextPath() { return nextPath; }
    private Object firstUpdateLock = new Object();
    private boolean firstUpdate = true;
    public boolean getFirstUpdate() {
	synchronized(firstUpdateLock) {
	    return firstUpdate; 
	}
    }
    private void setFirstUpdate(boolean value) { 
	synchronized(firstUpdateLock) {
	    firstUpdate = value; 
	}
    }

    private String currXYString = "? ?";
    private String currXYZString = "? ? ?";
    public synchronized String getCurrXYString() {return currXYString;}
    public synchronized String getCurrXYZString() {return currXYZString;}

    private Vector3D currLocation=new Vector3D(0.0,0.0,0.0);
    public synchronized Vector3D getCurrLocation() { return currLocation; }
    private synchronized void setCurrLocation(Vector3D value) { 
	currLocation = value; 
	currXYString = fmt2.format(currLocation.x) + " " + fmt2.format(currLocation.y); 
	currXYZString = fmt2.format(currLocation.x) + " " + fmt2.format(currLocation.y)+ " " + fmt2.format(currLocation.z);
	setRemainingDist();
    }

    private double remainingDist = 0.0;
    public synchronized double getRemainingDist() {return remainingDist;}
    private synchronized void setRemainingDist() {
	if(null == currPath)
	    remainingDist = 0.0;
	else
	    remainingDist = currPath.getRemainingDist(currX, currY, currZ);
    }

    private double currX = 0;
    private double currY = 0;
    private double currZ = 0;
   
    private double getXYDistSqd(double x, double y) {
	Vector3D loc = getCurrLocation();
	double xdiff = loc.x - x;
	double ydiff = loc.y - y;
	return (xdiff * xdiff + ydiff * ydiff);
    }

    public double getXYDist(double x, double y) {
	return Math.sqrt(getXYDistSqd(x,y));
    }

    private double getXYZDistSqd(double x, double y, double z) {
	double xdiff = currX - x;
	double ydiff = currY - y;
	double zdiff = currZ - z;
	return (xdiff * xdiff + ydiff * ydiff + zdiff * zdiff);
    }

    private double getXYZDist(double x, double y, double z) {
	return Math.sqrt(getXYZDistSqd(x,y,z));
    }

    private double getDistFromCurrWaypoint() {
	if(null == currWaypoint)
	    return -1;
	return getXYZDist(currWaypoint.x,currWaypoint.y,currWaypoint.z);
    }

    private ProxyState state = new ProxyState();
    {
        state.addChangeListener(this);
    }

    private Thread vcBridgePollingThread = null;

    private long expectedSimTimePrevWaypoint = 0;
    private long actualSimTimePrevWaypoint = 0;


    public Autopilot(RAPInterfaceImplementation rapInt, SmallUAVModel uavModel, MiniWorldState miniWorldState) {
	this.rapInt = rapInt;
        this.uavModel = uavModel;
        this.miniWorldState = miniWorldState;
	if(UAVRI.USE_VIRTUAL_COCKPIT) {
	    Machinetta.Debugger.debug(1, "Creating VCBridgeClient");
	    // Would be better to use proxyid but I had some issues with
	    // that in the field and couldn't take time out to fix it.
	    //
	    //	String vcBridgeClientName = state.getSelf().getProxyID().toString();
	    String vcBridgeClientName = "PROCID"+UAVRI.PROCERUS_UAV_ID;
	    vcBridgeClient = new VCBridgeClient(UAVRI.VC_BRIDGE_SERVER_IP_ADDRESS, UAVRI.VC_BRIDGE_SERVER_PORT, UAVRI.PROCERUS_UAV_ID,vcBridgeClientName);
	    Machinetta.Debugger.debug(1, "Done creating VCBridgeClient");
	    vcBridgeClient.start();
	    vcBridgePollingThread = new Thread(this);
	    vcBridgePollingThread.start();
	}
    }

    public void setPathPlanner(PathPlanner planner) {
	Machinetta.Debugger.debug(1, "setPathPlanner: entered.");
	synchronized(this) {
	    this.planner = planner;
	}
    }

    private void pollVCBridgeClient() {
	if(!UAVRI.USE_VIRTUAL_COCKPIT)
	    return;
	//	NavData navData = autopilot.updateLocation();
	NavData navData = vcBridgeClient.updateLocation();
	if(navData.procerusUAVAddress == UAVRI.PROCERUS_UAV_ID)
	    navDataValid = true;
	else
	    navDataValid = false;
	
	if(navDataValid){
	    Point2D.Double location = new Point2D.Double();
	    // @TODO: Should get this from somewhere else.  Maybe a
	    // config parameter in UAVRI?
	    location.x = LatLonUtil.lonToLocalXMeters(VCAutopilot.ORIGIN_LAT, VCAutopilot.ORIGIN_LON, navData.longitude); 
	    location.y = LatLonUtil.latToLocalYMeters(VCAutopilot.ORIGIN_LAT, VCAutopilot.ORIGIN_LON, navData.latitude);
	    Machinetta.Debugger.debug(1,"autopilot thinks we are at "+location.toString()+", navDataValid="+navDataValid);
	    currX = location.x;
	    currY = location.y;
	    Machinetta.Debugger.debug(1, "Updating currX currY to "+currX+" "+currY);
	}
	
	Vector3D loc = new Vector3D(currX, currY, currZ);
	setCurrLocation(loc);

	// @TODO: Firstupdate... we use firstUpdate to avoid doing
	// planning/path creation until we actually have some idea
	// where we are (i.e. before the first location update we
	// believe we're at 0,0,0).  So this should really be an
	// upcall/command back to PathPlanner.
	//
	// So really, PathPlanner will only create a nextPath when we
	// tell it to, so all we really need to do is to tell it not
	// to until we've had our first update.
	if(navDataValid) {
	    fireFirstUpdate();
	}
    }

    // This is for if we're using Virtual Cockpit instead of Sanjaya -
    // for sanjaya, we don't need to 'poll' for location, we receive
    // messages.  For VCBridge we need to poll... although we really
    // shouldn't have to since we already have a thread polling in the
    // vcbridge code, so we should change VCBridgeClient to an
    // observable or or something.
    public void run() {
	Machinetta.Debugger.debug(1, "run: entered.");
	while(true) {
	    pollVCBridgeClient();
	    try { Thread.sleep(100); } catch(Exception e2) {}
	}
    }

    public void stateChanged(BeliefID[] b) {
//  	for(int loopi = 0; loopi < b.length; loopi++) {
//              Belief bel = state.getBelief(b[loopi]);
//  	    Machinetta.Debugger.debug(1,"stateChanged: Belief class "+bel.getClass().getName()+" = "+bel.toString());
//  	}
	// @TODO: add code to deal with new beliefs - particularly
	// updateLocation and path conflicts
    }

    // Called directly by UAVRI
    public void updateLocation(double x, double y, double z) {
	//	Machinetta.Debugger.debug(1, "updateLocation: entered.");
	if(UAVRI.USE_VIRTUAL_COCKPIT) {
	    pollVCBridgeClient();
	    return;
	}
        
        // Update location
	currX = x;
	currY = y;
	currZ = z;
	Vector3D loc = new Vector3D(currX, currY, currZ);
	setCurrLocation(loc);
	fireFirstUpdate();
	checkForSendNextWaypoint();
    }

    public void fireFirstUpdate() {
	if(getFirstUpdate()) {
	    setFirstUpdate(false);
	    Machinetta.Debugger.debug(1, "fireFirstUpdate: Received first update!  curr location of UAV="+getCurrXYZString());
	}
    }

    private void sendCurrent() {
	Waypoint currWp = currWaypoint;
        if(null == currWp) {
	    Machinetta.Debugger.debug(3, "sendCurrent: ERROR: Couldn't send new waypoint, currWayPoint was null.");
	    return;
	}

	Machinetta.Debugger.debug(1, "sendCurrent: simtime="+SimTime.getEstimatedTime()+", uav at ("+fmt2.format(currX)+","+fmt2.format(currY)+","+fmt2.format(currZ)+") sending new waypoint to Vehicle " + currWp);
	Waypoint[] path = new Waypoint[1];
	path[0] = currWp;
	if(UAVRI.USE_VIRTUAL_COCKPIT) {
	    if(navDataValid){
		Machinetta.Debugger.debug(1, "sendCurrent: navData is valid attempting to send to VCAutopilot/VCBridge");
		vcBridgeClient.sendCurrent(path, UAVRI.PROCERUS_UAV_ID);
	    }
	    else {
		Machinetta.Debugger.debug(1, "sendCurrent: navData is invalid, NOT attempting to waypoint="+currWp+" to VCAutopilot/VCBridge");
	    }
	    return;
	}
	    
        NextWaypointPA msg = new NextWaypointPA();
	msg.longtitude = currWp.x;
	msg.latitude = currWp.y;
	msg.altitude = currWp.z;
            
	msg.heading = -1.0;
	// @TODO: DEBUG ONLY: for debugging - hopefully planid will be correct
	msg.debugExpectedArrivalTime = currWp.getTime();
	msg.debugPlanid = (null!= currPathID) ? currPathID.toString():"NO_PATH_ID";
            
	msg.clear = false;
	if(null != uavModel) uavModel.setWaypoint(currWp,SimTime.getEstimatedTime());
	rapInt.sendMessage(msg);
	Stats.waypointsSent(1);
    }

    // @TODO: I'm not sure why this is needed - I think this is
    // leftover from Robin's integration of the VirtualCockpit stuff.
    public void sendCurrentIfNotPrev() {
	if(UAVRI.USE_VIRTUAL_COCKPIT) {
	    if(navDataValid) {
		if (prevWP != currWaypoint) {
		    sendCurrent();
		    prevWP = currWaypoint;
		}
	    }
	    else {
		Machinetta.Debugger.debug(1, "navDataValid is false - going into holding pattern");
		synchronized(this) {
		    currIsHoldingPattern = true;
		}
	    }
	}
	else {
	    sendCurrent();
	}
    }

    // @TODO: optimization - change this to be called whenever we
    // update location, and set a flag if true, i.e. cache the result.
    // Then change the external API to just check the flag.  Set the
    // flag false when we get a nextPath.
    public synchronized boolean currPathExistsAndIsReadyForNextPath() {
	//	Machinetta.Debugger.debug(1, "currPathExistsAndIsReadyForNextPath: entered.");
	double currPathRemainingDist = 0.0;
	if(currPath != null) {
	    currPathRemainingDist = getRemainingDist();
	    // Work out whether we need to start planning the next path
	    if (currPathRemainingDist < UAVRI.PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH && nextPath == null) {
		Stats.pathsFinished(1);
		Machinetta.Debugger.debug(1, "currPathExistsAndIsReadyForNextPath: returning true.");
		return true;
	    }
	}
	//	Machinetta.Debugger.debug(1, "currPathExistsAndIsReadyForNextPath: returning false.");
	return false;
    }

    public boolean atWaypoint(double tolerance) {
        return atWaypoint(currWaypoint, tolerance);
    }
    public boolean atWaypoint(double x, double y, double tolerance) {
	return atWaypoint(new Vector3D(x,y, currZ),tolerance);
    }
    
    public boolean atWaypoint(Vector3D wp, double tolerance) {
        if (wp == null) {
            Machinetta.Debugger.debug(3, "atWaypoint: No waypoint provided for atWaypoint?");
            return true;
	}
	// @TODO: VCBridge code doesn't report altitudes yet, and
	// also for doing walking tests altitude is also going to
	// be a problem.  So ignore Z for now.
	//
	// double dist = getXYZDist(wp.x, wp.y);
	double dist = getXYDist(wp.x, wp.y);

 	Machinetta.Debugger.debug(0, "atWaypoint: uav at "+getCurrXYZString()
 				  +" waypoint "+wp
 				  +" dist " + fmt2.format(dist));
	boolean retval = false;
	if(dist > tolerance)
	    return false;

	Stats.waypointsReached(1);
	long actualTime = SimTime.getEstimatedTime();
	long expectedTime = 0;
	if(wp instanceof Waypoint) {
	    expectedTime = ((Waypoint)wp).getTime();
	}
	long diff = actualTime - expectedTime;

	Machinetta.Debugger.debug(1, "atWaypoint: AT WAYPOINT uav at " +getCurrXYZString()
				  +" waypoint "+wp 
				  +" dist "+fmt2.format(dist)
				  +" expected time "+expectedTime
				  +" actual time "+actualTime
				  +" diff in time "+diff);
	return true;
    }

    public synchronized boolean needCurrentPath() {
	if(currPath == null ) {
	    Machinetta.Debugger.debug(1, "needCurrentPath: currPath is null, returning true.");
	    return true;
	}
	if ( currPath.getNext() == null ) {
	    Machinetta.Debugger.debug(1, "needCurrentPath: currPath.getNext() is null, returning true.");
	    return true;
	}
	if ( currIsHoldingPattern) {
	    Machinetta.Debugger.debug(1, "needCurrentPath: currIsHoldingPattern is true, returning true.");
	    return true;
	}
	//	Machinetta.Debugger.debug(1, "needCurrentPath: returning false.");
	return false;
    }

    public synchronized boolean needNextPath() {
        return( nextPath == null );
    }

    private void logWaypointTimeData() {
	// We're within range of the current waypoint 
	// Log some data about how far we are from the waypoint
	if(!(currWaypoint instanceof Waypoint))
	    return;

	long expectedSimTime = ((Waypoint)currWaypoint).getTime();
	long actualSimTime = SimTime.getEstimatedTime();
	long expectedElapsed = expectedSimTime - expectedSimTimePrevWaypoint;
	long actualElapsed = actualSimTime - actualSimTimePrevWaypoint;
	long expectedActualDiff = expectedElapsed - actualElapsed;
	double ratio = (double)expectedActualDiff / (double)expectedElapsed;
                                
	double dist = getDistFromCurrWaypoint();
	Machinetta.Debugger.debug(1, "AUTOPILOT TOLERANCE At est simtime="+actualSimTime+", dist to waypoint="+dist+", expected time="+expectedSimTime+", actual elapsed= "+actualElapsed+", expected elapsed= "+expectedElapsed+", diff = "+expectedActualDiff+", ratio diff/expected= "+fmt.format(ratio));
                                
	expectedSimTimePrevWaypoint = expectedSimTime;
	actualSimTimePrevWaypoint = actualSimTime;
    }

    private synchronized void enterHoldingPattern() {
	// This should probably only happen at startup
	// Create some initial path while waiting for next path to be deconflicted
	currPath = Path3D.makeLoop(currX, currY, currZ,UAVRI.UAV_SPEED_METERS_PER_SEC*10);
	// TODO: set asset id properly
	currIsHoldingPattern = true;
    }

    // @TODO: Possible synchronization issues here... in theory.
    // Maybe not at the moment but possibly later on.  The thread in
    // PathPlanner calls, checkForSendNextWaypoint(), and the only
    // other place that calls this is the method, in Autopilot,
    // cancelCurrPath(), which is again called by PathPlanner.  But
    // it's possible some other thread in the future would call either
    // of those.  So we really should add synchronization.
    public void checkForSendNextWaypoint() {
	//	Machinetta.Debugger.debug(1, "checkForSendNextWaypoint: entered.");

	boolean atCurrWaypoint = false;
	if(null == currWaypoint)
	    atCurrWaypoint = true;
	else {
	    if (atWaypoint(UAVRI.PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS)) {
		logWaypointTimeData();
		atCurrWaypoint = true;
	    }
	}
	if(!atCurrWaypoint) 
	    return;

	// If we're at currWaypoint then we need to get the next
	// waypoint and send it on to the UAV.
	
	synchronized(this) {
	    if((null == currPath) || (null == currPath.getNext()))  {
		// If currPath is null, we need to move on to the
		// nextPath, which has hopefully already been planned
		// and is waiting for us in nextPath
		Machinetta.Debugger.debug(1, "checkForSendNextWaypoint: Switching to nextPath="+( (null == nextPath) ?  "null" : nextPath.toString()));
		currIsHoldingPattern = false;
		currPath = nextPath;
		currPathID = nextPathID;
                // update the remaining distance for the newly switched path
                setRemainingDist();
		nextPath = null;
		nextPathID = null;
	    }

	    if((null == currPath) || (null == currPath.getNext()))  {
		// Ok, there WAS no nextPath, so all we can do now is
		// go into a holding pattern.
		Machinetta.Debugger.debug(1, "checkForSendNextWaypoint: No nextPath, creating holdingPattern loop.");
		enterHoldingPattern();
		// @TODO: Send command to PathPlanner telling it to
		// give us a next path.
	    }

	    // Grab the next waypoint from currPath.  (Which we know
	    // is not null, because worst case if it was null above
	    // then we generated a loop holding pattern.)
	    currWaypoint = currPath.getNext();

	    // Remove currWaypoint from head of path - at some point
	    // in the past this was throwing exceptions so we threw a
	    // try catch around it to make it keep running while still
	    // printing the exception to the log file.  
	    try {
		if (currPath.getNext() != null) currPath.removeFirst();
	    } catch(RuntimeException e) {
		Machinetta.Debugger.debug(1, "checkForSendNextWaypoint: exception in Path3D.removeFirst, e="+e);
		e.printStackTrace();
	    }

	    // if the path is done, null it out - next time we get
	    // called we'll either use the nextPath or create a
	    // holding pattern loop.
	    if(currPath.size() == 0)
		currPath = null;

	}	// 	synchronized(this)

	Machinetta.Debugger.debug(1, "checkForSendNextWaypoint: Sending to UAV new currWaypoint="+currWaypoint);

	sendCurrentIfNotPrev();
    }

    // @TODO: for when we finish a role - synchronization issues?
    public synchronized void cancelCurrPath() {
	Machinetta.Debugger.debug(1, "cancelCurrPath: entering");

	currWaypoint = null;
	currPath = null;
	checkForSendNextWaypoint();
    }

    public synchronized void setCurrPath(Path3D currPath) {
	Machinetta.Debugger.debug(1, "setCurrPath: setting currPath to "+currPath.toString());
	this.currPath = currPath;
	if(null != miniWorldState)
	    miniWorldState.setPath(currPath);
    }

    public synchronized void setNextPath(Path3D nextPath,BeliefID nextPathID) {
	Machinetta.Debugger.debug(1, "setNextPath: setting nextPath to "+nextPath.toString());
	this.nextPath = nextPath;
	this.nextPathID = nextPathID;
	// Force use of nextPath if in currently in holding pattern
	if(currIsHoldingPattern) {
	    Machinetta.Debugger.debug(1, "setNextPath: currPath is holding pattern, removing currPath and currWaypoint to force use of nextPath.");
	    currPath = null;
	    currWaypoint = null;
	    checkForSendNextWaypoint();
	}
    }
}
