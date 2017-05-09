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
 * MainFrame.java
 *
 * Created on June 19, 2004, 12:38 PM
 */

package AirSim.Environment.GUI;

import AirSim.Environment.*;
import AirSim.ProxyServer;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Assets.Tasks.*;
import java.text.DecimalFormat;

import Gui.*;

import javax.swing.JOptionPane;
import java.util.*;
import java.io.*;

// This stuff is for the GUI, so it has to be moved out to wherever
// we're going to have the GUI code living when we move the GUI code.
import java.awt.Rectangle;
/**
 *
 * @author  paul
 */
public class MainFrame {
    /***
     *
     *
     *
     *
     *  Need to configure variable below to match whatever the UAVs are using.
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    public final static DecimalFormat fmt = new DecimalFormat("0.00");
    
    public static boolean assetCollisionDetectionOn = true;
    public static double assetCollisionRangeMeters = 2000;
    public static double assetCollisionMinZDistMeters = 100;
    public static double assetCollisionRangeSqdMeters = assetCollisionRangeMeters*assetCollisionRangeMeters;
    public static int REPAINT_INTERVAL_MS = 1000/15;
    public static boolean SHOW_GUI = false;
    public static boolean SNAPSHOT_GUI = true;	// If this is true, it sets the GUI visibile even if it SHOW_GUI is false.
    
    public final static int MIN_SLEEP_RESOLUTION = 20;
    //    public final static long RUNNING_TIME_LIMIT_MS = 1000 * 60 * 200;
    public final static long RUNNING_TIME_LIMIT_MS = 20 * 60 * 1000;	// twenty wallclock minutes
    /** If this is negative, it is ignored. */
    //    public final static long RUNNING_TIME_LIMIT_STEPS = 100 * 60 * 10;	// one hundred sim minutes
    public final static long RUNNING_TIME_LIMIT_STEPS = 20 * 60 * 10;	// twenty sim minutes
    public final static int DELAY_BEFORE_UNPAUSE = 8000;
    //    public final static int DELAY_BEFORE_UNPAUSE = 20000;
        
    public final static int MAX_STEPS_PER_SECOUND = 200;
    public final static int DEFAULT_SPEED_SLIDER_VAL = 10;
    public static int UPDATE_RATE = MapView.calcUpdateRate(DEFAULT_SPEED_SLIDER_VAL);
    public static int OLD_UPDATE_RATE = 0;
    // Update fast until an 'event' happens
    public static int FAST_UPDATE_RATE = 1;
    public static boolean FAST_UPDATE_ON = false;

    public static boolean STEP_TIME_FIXED = false; // true;
    public static long FIXED_MS_PER_STEP = 100;

    // Make the simulation default to not paused.
    public static volatile boolean paused = false;
    public void unpause() {
	paused = false;
	try {
	    if(null != mainFrameGUI)
		mainFrameGUI.unpause();
	}
	catch (Exception e) {};
    }

    public static long stepCount = 0;
    
    public final static int UPDATE_RATE_PRINT =100;
    public static int updateRateCounter = 0;
    
    public final boolean actAsController = false;
    
    // Access to what is going on
    Env env = new Env();
    
    private static boolean experimentMode = false;
    
    // If true then we are running in interactive mode,
    // i.e. interacting with the user.  If false then we should avoid
    // throwing up any dialogs or such that require user interaction.
    private static boolean interactiveMode = false;

    public static Rectangle BLUEFORFlag = new Rectangle(0,0,5000,5000);
    public static Rectangle OPFORFlag = new Rectangle(45000,45000,5000,5000);
    
    // Various end game conditions - one or more may be true - if they
    // are then we check if the simulation run is 'over', and if any
    // of the tests come up true then the sim exits.
    public static boolean END_CONDITION_CAPTURE_THE_FLAG = false;
    public static boolean END_CONDITION_ALL_HMMMVS_DEAD = false;
    public static boolean END_CONDITION_ALL_OPFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_BLUEFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_EMITTERS_DETECTED = false;

    // Everyone infantry on either OPFOR side or BLUEFOR side is dead.
    public static boolean END_CONDITION_ALL_INFANTRY_DEAD = true;

    // Every goal point is 'held' by at least one BLUEFOR DI for some
    // amount of time, where 'held' means no OPFOR DI are within some
    // range.
    public static boolean END_CONDITION_ALL_GOALS_HELD = true;
    public static ArrayList<Vector3D> endConditionGoals = new ArrayList<Vector3D>();
    private final static double AT_GOAL_RANGE = 50.0;
    private final static long HELD_GOAL_TIME_LIMIT_MS = 180000;

    private class GoalPoint {
	Vector3D point;
	long heldSince;
	public String toString() {
	    return "point="+point+" heldSince="+heldSince;
	}
    }
    public static ArrayList<GoalPoint> endConditionGoalPoints = null;
    

    private MainFrameGUI mainFrameGUI = null;

    // Given a time, get a string representing the current time in
    // format YYYY_MM_DD_HH_MM_SS, i.e. 2005_02_18_23_16_24.  Using
    // this format results in timestamp strings (or filenames) that
    // will sort to date order.  If 'time' is null it will be
    // filled in with the current time.
    public static String getTimeStamp(Calendar time) {
        String timestamp = "";
        String part = null;
        if(null == time)
            time = Calendar.getInstance();
        
        timestamp = Integer.toString(time.get(time.YEAR));
        
        part = Integer.toString((time.get(time.MONTH)+1));
        if(1 == part.length())
            part = "0"+part;
        timestamp += "_"+part;
        
        part = Integer.toString(time.get(time.DAY_OF_MONTH));
        if(1 == part.length())
            part = "0"+part;
        timestamp += "_"+part;
        
        part = Integer.toString(time.get(time.HOUR_OF_DAY));
        if(1 == part.length())
            part = "0"+part;
        timestamp += "_"+part;
        
        part = Integer.toString(time.get(time.MINUTE));
        if(1 == part.length())
            part = "0"+part;
        timestamp += "_"+part;
        
        part = Integer.toString(time.get(time.SECOND));
        if(1 == part.length())
            part = "0"+part;
        timestamp += "_"+part;
        
        return timestamp;
    }
    
    // @todo: the below is pretty shoddy - its only use is as a way
    // the batch scripts that run experiments can detect that the
    // experiment is done, so they can move onto the next experiment.
    // And it was done in a hurry.
    private String lockfileName = "/usr1/logs/sanjayalock";
    public void makeLockfile() {
	try {
	    File file = new File(lockfileName);
    
	    // Create file if it does not exist
	    if(file.exists()) {
		Machinetta.Debugger.debug("Created lockfile="+lockfileName+" (already exists)", 1, this);
	    }
	    else {
		boolean success = file.createNewFile();
		if (success) {
		    Machinetta.Debugger.debug("Created lockfile="+lockfileName, 1, this);
		} else {
		    Machinetta.Debugger.debug("FAILED to create lockfile="+lockfileName, 1, this);
		}
	    }
	} catch (IOException e) {
	}
    }

    public void deleteLockfile() {
	Machinetta.Debugger.debug("Attempting to delete lockfile="+lockfileName, 1, this);
	boolean success = (new File(lockfileName)).delete();
	if (!success) {
	    Machinetta.Debugger.debug("FAILED to delete lockfile="+lockfileName, 1, this);
	}
	else
	    Machinetta.Debugger.debug("Succeeded to delete lockfile="+lockfileName, 1, this);
    }
    public void shutdown(String shutdownReason) {
        
        if(experimentMode){
            Machinetta.Debugger.debug("SHUTTING DOWN: "+shutdownReason, 3, this);
            env.dumpStats();
            env.dumpPerAssetStats();
            
            if(SNAPSHOT_GUI) {
		String timestamp = getTimeStamp(null);
		// @todo: FIX THIS - hardcoding is bad
		String filename = "/usr1/logs/l3com/"+timestamp+"_END_SNAP";
		if(null == mainFrameGUI) 
		    mainFrameGUI = new MainFrameGUI(this,REPAINT_INTERVAL_MS,null,null);
		mainFrameGUI.snapshot(filename);
            }
	    deleteLockfile();
            System.exit(0);
        } else {
            Machinetta.Debugger.debug("Shutting down: " + shutdownReason, 3, this);
            System.exit(0);
        }
    }
    
    /** Creates a new instance of MainFrame */
    public MainFrame(boolean stepTimeFixed, long fixedMsPerStep, int repaintInterval, boolean showGui, boolean snapshotGui, String switchHost, String switchGroup) {
	if(Asset.NO_COMMS) {
	    Machinetta.Debugger.debug("ERROR: (really just a warning) Asset.NO_COMMS is true", 3, this);
	}

        this.STEP_TIME_FIXED = stepTimeFixed;
	this.FIXED_MS_PER_STEP = fixedMsPerStep;
	if(null != switchHost) {
	    Machinetta.Configuration.UDP_SWITCH_IP_STRING = switchHost;
	    Machinetta.Debugger.debug("Setting UDP_SWITCH_IP_STRING to "+Machinetta.Configuration.UDP_SWITCH_IP_STRING, 3, this);
	}
	if(null != switchGroup) {
	    Machinetta.Configuration.UDP_SWITCH_GROUP = switchGroup;
	    Machinetta.Debugger.debug("Setting UDP_SWITCH_GROUP to "+Machinetta.Configuration.UDP_SWITCH_GROUP, 3, this);
	}

        REPAINT_INTERVAL_MS = repaintInterval;
	//        SHOW_GUI = showGui;
        SNAPSHOT_GUI = snapshotGui;
	Machinetta.Debugger.debug("SHOW_GUI="+SHOW_GUI, 3, this);
	if(SNAPSHOT_GUI && !SHOW_GUI) 
	    Machinetta.Debugger.debug("SHOW_GUI is FALSE and SNAPSHOT_GUI is TRUE, GUI will be off for most of the run (saving CPU cycles) but will be instantiated for snapshot just before exit. ", 3, this);

	makeLockfile();
        
        new Thread() {
            public void run() {
                if(experimentMode){
		    Machinetta.Debugger.debug("Sleeping for "+DELAY_BEFORE_UNPAUSE+"ms before unpausing simulation", 2, this);
                    try {
                        sleep(DELAY_BEFORE_UNPAUSE);
                    } catch (InterruptedException e) {}
                    Machinetta.Debugger.debug("Unpausing simulation", 2, this);
		    unpause();
                }
            }
        }.start();
        if(experimentMode){
            new Thread() {
                public void run() {
                    long timeNow = System.currentTimeMillis();
                    long sleepFor = RUNNING_TIME_LIMIT_MS;
		    Machinetta.Debugger.debug("Sleeping for "+RUNNING_TIME_LIMIT_MS+"ms before forcing exit of simulation", 2, this);
                    while(true) {
                        try {
                            sleep(sleepFor);
                        } catch (InterruptedException e) {}
                        sleepFor = RUNNING_TIME_LIMIT_MS - (System.currentTimeMillis() - timeNow);
                        if(sleepFor <= 0) {
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "Time limit for simulation ("+RUNNING_TIME_LIMIT_MS+") has been exceeded, shutting down.");
			    }
                            shutdown("Time limit "+RUNNING_TIME_LIMIT_MS+" reached since start of simulation.");
                        }
                    }
                }
		}.start();
        }
        
	Machinetta.Debugger.debug("Creating ProxyServer",1, this);
        new ProxyServer();

	if(SHOW_GUI) {

	    Machinetta.Debugger.debug("Creating GUI",1, this);
	    mainFrameGUI = new MainFrameGUI(this,REPAINT_INTERVAL_MS, null, null);
	    if (actAsController) {
		Machinetta.Debugger.debug("Creating Sanjaya Controller",1, this);
		new Controller();
		Machinetta.Debugger.debug("Done creating Sanjaya Controller",1, this);
	    }
	}
	Machinetta.Debugger.debug("Starting update thread",1, this);
	updateThread.start();
	//	env.writeTerrainCostMap("/afs/cs.cmu.edu/user/owens/sanjaya000_terrain_cost_map", 500,50000, 50000);
    }
    
    public static int calcUpdateRate(int value) {
	// Paul's original calculation
	//        return (int)(1000.0 * Math.pow(Math.E, -value/10.0));

	// Howabout we make it simple - you tell us how many steps per
	// second you want, we tell you how long to sleep between
	// steps to get that.
	return (int)(1000.0 / value);
    }

    public void setUpdateRate(int value) {
	MainFrame.UPDATE_RATE = calcUpdateRate(value);
	if (MainFrame.UPDATE_RATE < 100)
	    MapView.UPDATE_RATE = 100;
	else
	    MapView.UPDATE_RATE = MainFrame.UPDATE_RATE;
	// System.out.println("Set update rate to : " + MainFrame.UPDATE_RATE);
    }

    private class ConflictInfo {
	String key;
	SmallUAV uav1;
	SmallUAV uav2;
	long startTime=0;
	long mostRecentStep=0;
	double smallestDistance=Double.MAX_VALUE;
	public ConflictInfo(SmallUAV uav1, SmallUAV uav2, double dist) {
	    this.uav1 = uav1;
	    this.uav2 = uav2;
	    this.smallestDistance = dist;
	    key = uav1.getID() + uav2.getID();
	    startTime = env.getSimTimeMs();
	    mostRecentStep = env.getStep();
	}
    }

    private HashMap<String,ConflictInfo> conflictMap = new HashMap<String,ConflictInfo>();
    private void checkForSmallUAVCollisions() {
	//			Machinetta.Debugger.debug("Checking for asset collisions, collision range="+assetCollisionRangeMeters,1,this);

	long simTimeMsNow = env.getSimTimeMs();
	Object[] assets = env.getAllAssets().toArray(new Object[1]);
	for(int loopi = 0; loopi < assets.length; loopi++) {
	    Asset asset1 = (Asset) assets[loopi];
	    // @TODO: NOTE, WE ARE ONLY INTERESTED IN COLLIDING
	    // SMALLUAVS - MAY HAVE TO CHANGE THIS LATER
	    if(null == asset1)
		continue;
	    if(!(asset1 instanceof SmallUAV)) {
		assets[loopi] = null;
		continue;
	    }
	    for(int loopj = loopi; loopj < assets.length; loopj++) {
		Asset asset2 = (Asset) assets[loopj];
		if(asset2 == null) 
		    continue;
		if(asset1 == asset2) 
		    continue;
		if(!(asset2 instanceof SmallUAV)) {
		    assets[loopj] = null;
		    continue;
		}
				
		SmallUAV uav1 = ((SmallUAV)asset1);
		SmallUAV uav2 = ((SmallUAV)asset2);
		String id1 = uav1.getID();
		String id2 = uav2.getID();
		String key = id1+"."+id2;

		if(!asset1.withinConflictRangeSqd(assetCollisionRangeSqdMeters,assetCollisionMinZDistMeters, asset2)) {
		    // no conflict between these two assets - see if there was one before.
		    //
		    ConflictInfo ci = conflictMap.get(key);
		    if(null != ci) {
			// There was a conflictMap entry, but these two
			// uavs are not in conflict.  Hence they've been
			// in conflict for a while but are no longer, so
			// remove them from map and print out info about
			// how long they were in conflict and how close
			// they came.
			conflictMap.remove(key);
			long duration = env.getSimTimeMs() - ci.startTime;
			Machinetta.Debugger.debug("CONFLICT ENDED between "+id1+" and "+id2+" at simtime "+simTimeMsNow+" ms, step "+stepCount+", duration "+duration+" closest approach "+ci.smallestDistance,3,this);
		    }
		    continue;
		}

		asset1.setCollision(true);
		asset2.setCollision(true);
		Vector3D loc1 = uav1.location;
		Vector3D loc2 = uav2.location;
		Vector3D actualToVector = loc1.toVector(loc2);
		double actualDist = actualToVector.length();

		ConflictInfo ci = conflictMap.get(key);
		if(null != ci) {
		    // Conflict has already been detected, update some info
		    ci.mostRecentStep = env.getStep();
		    if(actualDist < ci.smallestDistance) {
			ci.smallestDistance = actualDist;
		    }
		    continue;
		}

		// new conflict, record some info and print out a debug message.
		ci = new ConflictInfo(uav1,uav2, actualDist);
		conflictMap.put(key,ci);
		long end1 = uav1.getEndOfPathAtStep();
		long end2 = uav2.getEndOfPathAtStep();

		Waypoint wp1 = uav1.getExpectedWaypoint(simTimeMsNow);
		Waypoint wp2 = uav2.getExpectedWaypoint(simTimeMsNow);
		if((null != wp1) && (null != wp2)) {
		    Waypoint wp1Last = uav1.lastWaypoint;
		    long wp1LastTime = uav1.lastWaypointRcvdSimTimeMs;
		    String lastplanid1 = uav1.lastWaypointPlanId;

		    Waypoint wp1Cur = uav1.curWaypoint;
		    long wp1CurTime = uav1.curWaypointRcvdSimTimeMs;
		    String curplanid1 = uav1.curWaypointPlanId;

		    Waypoint wp2Last = uav2.lastWaypoint;
		    long wp2LastTime = uav2.lastWaypointRcvdSimTimeMs;
		    String lastplanid2 = uav2.lastWaypointPlanId;

		    Waypoint wp2Cur = uav2.curWaypoint;
		    long wp2CurTime = uav2.curWaypointRcvdSimTimeMs;
		    String curplanid2 = uav2.curWaypointPlanId;

		    Vector3D expectedToVector = wp1.toVector(wp2);
		    double expectedDist = expectedToVector.length();
		    Vector3D slopVector1 = loc1.toVector(wp1);
		    Vector3D slopVector2 = loc2.toVector(wp2);
		    double slopDist1 = slopVector1.length();
		    double slopDist2 = slopVector2.length();

		    Machinetta.Debugger.debug("CONFLICT DETECTED between "+id1+" and "+id2+" at simtime "+simTimeMsNow+" ms, step "+stepCount+", expected dist="+expectedDist+", actualDist = "+actualDist+", "+id1+" actual loc "+loc1+" (last move done at "+end1+") expected loc "+wp1+", diff="+slopDist1+", "+id2+" actual loc "+loc2+" (last move done at "+end2+") expected loc "+wp2+", diff="+slopDist2+", "+id1+" last "+wp1Last+" rcvd "+wp1LastTime+" planid "+lastplanid1+", cur "+wp1Cur+", rcvd "+wp1CurTime+" planid "+curplanid1+", "+id2+" last "+wp2Last+" rcvd "+wp2LastTime+" planid "+lastplanid2+", cur "+wp2Cur+", rcvd "+wp2CurTime+" planid "+curplanid2, 3, this);
		}
		else {
		    Machinetta.Debugger.debug("CONFLICT DETECTED between "+id1+" and "+id2+" at simtime "+simTimeMsNow+" ms, step "+stepCount+", expected dist=unknown, actualDist = "+actualDist+", "+id1+" actual loc "+loc1+" (last move done at "+end1+") expected loc unknown, diff=unknown, "+id2+" actual loc "+loc2+" (last move done at "+end2+") expected loc unknown, diff=unknown "+id1+" last unknown rcvd unknown, cur unknown, rcvd unknown, "+id2+" last unknown rcvd unknown, cur unknown, rcvd unknown", 3, this);
		}
		Machinetta.Debugger.debug("CONFLICT: Waypoints: " + id1 + " " + uav1.getCurrentTask() + ",  " + id2 + " " + uav2.getCurrentTask(), 1, this);
	    }
	}
	//			Machinetta.Debugger.debug("Done checking for asset collisions",1,this);

    }

    private void checkAllInfantryDead() {
	if(env.allOpforInfantryDead()) {
	    deleteLockfile();
	    if(interactiveMode) {
		JOptionPane.showMessageDialog(null, "BLUEFOR WINS: All OPFOR infantry are dead");
	    }
	    shutdown("BLUEFOR WINS: All OPFOR infantry are dead, simTimeMs="+env.getSimTimeMs());
	}
	if(env.allBlueforInfantryDead()) {
	    deleteLockfile();
	    if(interactiveMode) {
		JOptionPane.showMessageDialog(null, "OPFOR WINS: All BLUEFOR infantry are dead");
	    }
	    shutdown("OPFOR WINS: All BLUEFOR infantry are dead, simTimeMs="+env.getSimTimeMs());
	}
    }

    private void checkAllGoalsHeld() {
	if(endConditionGoals.size() <= 0) {
 	    Machinetta.Debugger.debug(4, "checkAllGoalsHeld: There are no goals to check!  Double check your Env.txt for END_CONDITION_GOAL lines!");
	    return;
	}
	if(null == endConditionGoalPoints) {
	    endConditionGoalPoints = new ArrayList<GoalPoint>();
	    for(Vector3D goal: endConditionGoals) {
		GoalPoint gp = new GoalPoint();
		gp.point = goal;
		gp.heldSince = -1;
		endConditionGoalPoints.add(gp);
	    }
	}

	Vector<Asset> assets = env.getAllAssets();
	ArrayList<Infantry> opAssets = new ArrayList(assets.size());
	ArrayList<Infantry> blueAssets = new ArrayList(assets.size());
	for(int loopi = 0; loopi < assets.size(); loopi++) {
	    Asset asset = assets.get(loopi);
	    if(!(asset instanceof Infantry))
		continue;
	    if(asset.state == State.DESTROYED)
		continue;
	    if(asset.getForceId() == ForceId.BLUEFOR)
		blueAssets.add((Infantry)asset);
	    if(asset.getForceId() == ForceId.OPFOR)
		opAssets.add((Infantry)asset);
	}

	long timeNow = env.getSimTimeMs();

	Machinetta.Debugger.debug(1, "checkAllGoalsHeld: Checking for all goals ("+endConditionGoalPoints.size()+") held at time "+timeNow);

	// Check that each and eveyr goal has been 'held',  i.e.
	// a) live bluefor DI within AT_GOAL_RANGE
	// b) no live opfor DI within SEE_ID_DIST
	// c) held for longer than HELD_GOAL_TIME_LIMIT_MS
	// if a or b fail then the clock is restarted on that goal.
	int numGoalsDone = 0;
	boolean allHeld = true;
	for(int loopi = 0; loopi < endConditionGoalPoints.size(); loopi++) {
	    GoalPoint gp = endConditionGoalPoints.get(loopi);

	    boolean held = false;
	    Infantry blueforHolding = null;
	    for(int loopj = 0; loopj < blueAssets.size(); loopj++) {
		// If one bluefor is near enough to the goal, then it
		// is held.  if we haven't started the clock then
		// start the clock.
		Infantry blueAsset = blueAssets.get(loopj);
		if(gp.point.toVectorLengthSqd(blueAsset.location) < (AT_GOAL_RANGE*AT_GOAL_RANGE)) {
		    blueforHolding = blueAsset;
		    held = true;
		    break;
		}
	    }

	    if(!held) {
		allHeld = false;
		gp.heldSince = -1;
		Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     not held, NO bluefor at goal for GoalPoint "+gp);
		continue;
	    }

	    // Start the clock if it wasn't started earlier
	    if(gp.heldSince == -1) {
		gp.heldSince = timeNow;
	    }
	    Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     bluefor "+blueforHolding.getID()+" at goal for GoalPoint "+gp);


	    boolean opposed = false;
	    Infantry opforOpposed = null;
	    for(int loopj = 0; loopj < opAssets.size(); loopj++) {
		// If one opfor is near enough to see ID (and hence
		// forceid), then it is NOT held.  if we have
		// started the clock then set it back to -1.
		Infantry opAsset = opAssets.get(loopj);
		if(gp.point.toVectorLengthSqd(opAsset.location) < (opAsset.SEE_ID_DIST*opAsset.SEE_ID_DIST)) {
		    opposed = true;
		    opforOpposed = opAsset;
		    break;
		}
	    }

	    if(opposed) {
		// Reset/stop the clock!
		gp.heldSince = -1;
		allHeld = false;
		Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     goal not held, opfor "+opforOpposed+" dist "+fmt.format(gp.point.toVectorLength(opforOpposed.location))+" m is less than SEE_ID_DIST "+opforOpposed.SEE_ID_DIST+" from GoalPoint "+gp);
		continue;
	    }

	    Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     goal held, no opfor near goal for GoalPoint "+gp);

	    long timeHeld = (timeNow - gp.heldSince);
	    if(timeHeld < HELD_GOAL_TIME_LIMIT_MS) {
		allHeld = false;
		Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     Goal "+gp+" held for "+timeHeld+" sim ms, has not yet reached time limit "+HELD_GOAL_TIME_LIMIT_MS+" sim ms.");
		continue;
	    }
	    numGoalsDone++;
	    Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     Time limit is UP for this goal "+gp+", now how about the others?");
	}

	if(!allHeld) {
	    Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     Final verdict, at sim time "+timeNow+", "+numGoalsDone+" out of "+endConditionGoalPoints.size()+" held, not all goals held simultaneously for "+HELD_GOAL_TIME_LIMIT_MS+" sim ms, game continues.");
	    return;
	}

	Machinetta.Debugger.debug(1, "checkAllGoalsHeld:     Final verdict, at sim time "+timeNow+", ALL HELD "+numGoalsDone+" out of "+endConditionGoalPoints.size()+" held for "+HELD_GOAL_TIME_LIMIT_MS+" sim ms, game over man!");
	
	deleteLockfile();
	if(interactiveMode) {
	    JOptionPane.showMessageDialog(null, "BLUEFOR WINS: All Goals held for (simulated) "+HELD_GOAL_TIME_LIMIT_MS+" ms without seeing OPFOR DI.");
	}
	shutdown("BLUEFOR WINS: All Goals held for "+HELD_GOAL_TIME_LIMIT_MS+" ms or more without seeing OPFOR DI, simTimeMs="+env.getSimTimeMs());
    }

    Thread updateThread = new Thread() {
        private long lastUpdate = System.currentTimeMillis();
        public void run() {
	    Machinetta.Debugger.debug("Entering run in update thread",1, this);
	
	    int pausedCount = 0;

	    long timeElapsedProc = 0;
	    double processingTimeLastNSteps = 0;
	    long timeBeforeStep;
	    long timeAfterStep;
	    long actualSleepTimeThisStep;
	    long totalTimeThisStep;
	    long totalTimeLastNSteps=0;
	    long leftOverSleepTime = 0;
	    if(SHOW_GUI) {
		long now = System.currentTimeMillis();

		if((now - lastUpdate) >= REPAINT_INTERVAL_MS) {
		    mainFrameGUI.updateMapDB();
		}
	    }
            env.dumpStats();
            while (true) {
                //System.out.println("Updating sim");
                if (!paused) {
		    timeBeforeStep = System.currentTimeMillis();
		    // System.out.println("Environment was stepped");
		    env.clearEvent();
                    env.step();
		    if(FAST_UPDATE_ON && env.getEvent()) {
			Machinetta.Debugger.debug(1,"FAST_UPDATE: Back to regular update speed due to event on asset: "+env.eventString());
			UPDATE_RATE = OLD_UPDATE_RATE;
			FAST_UPDATE_ON = false;
		    }

		    stepCount++;
		    if(stepCount % 1000 == 0) {
			Machinetta.Debugger.debug("Simulation stepped for "+stepCount+" steps...",1,this);
		    }

		    if((stepCount % 100) == 0) {
			env.dumpStats();
		    }
		    if((stepCount % 300) == 0) {
			env.dumpPerAssetStats();
		    }

		    if(assetCollisionDetectionOn) {
			checkForSmallUAVCollisions();
		    }

		    if(SHOW_GUI) {
			long now = System.currentTimeMillis();
			if((now - lastUpdate) >= REPAINT_INTERVAL_MS) {
			    mainFrameGUI.updateMapDB();
			    lastUpdate = System.currentTimeMillis();
			}
		    }

                    if (END_CONDITION_CAPTURE_THE_FLAG) {
                        // Check whether someone won the game in the "proper" way
                        // System.out.println("Checking for CTF finish ... actually, unimplemented");
                        boolean winner = false;
                        String msg = "";
                        for(Object o: env.getAllAssets()) {
                            Asset a = (Asset)o;
			    if(a.isDead())
				continue;
			    if(a instanceof Truck) 
				continue;
                            if (a.getForceId() == ForceId.BLUEFOR && a instanceof GroundVehicle && OPFORFlag.contains(a.location.x, a.location.y)) {
                                System.out.println("Blue wins !!!");
                                msg = "Blue wins!";
                                winner = true;
                            } else if (a.getForceId() != ForceId.BLUEFOR && a instanceof GroundVehicle && BLUEFORFlag.contains(a.location.x, a.location.y)) {
                                System.out.println("Red wins !!!");
                                msg = "Red wins!";
                                winner = true;
                            }
                        }
                        if (winner) {
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, msg);
			    }
                            shutdown(msg);
                        } 
                    }

		    if(END_CONDITION_ALL_INFANTRY_DEAD) {
			checkAllInfantryDead();
		    }
		    if(END_CONDITION_ALL_GOALS_HELD) {
                        if((stepCount % 100) == 0)
			    checkAllGoalsHeld();
		    }

                    if (experimentMode) {
                        if(RUNNING_TIME_LIMIT_STEPS > 0 && env.getStep() > RUNNING_TIME_LIMIT_STEPS) {
			    deleteLockfile();
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "Step limit for simulation ("+RUNNING_TIME_LIMIT_STEPS+") has been exceeded, shutting down.");
			    }
                            shutdown("Step limit of "+RUNNING_TIME_LIMIT_STEPS+" steps since start of simulation has been reached (step="+env.getStep()+")");
                        }
                        if(END_CONDITION_ALL_OPFOR_DEAD && env.allOpforDead()) {
			    deleteLockfile();
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "BLUEFOR WINS, all OPFOR are dead.");
			    }
                            shutdown("All OPFOR are dead.");
                        }
                        if(END_CONDITION_ALL_BLUEFOR_DEAD && env.allBlueforDead()) {
			    deleteLockfile();
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "OPFOR WINS, all BLUEFOR are dead.");
			    }
                            shutdown("All BLUEFOR are dead.");
                        }
                        if(END_CONDITION_ALL_HMMMVS_DEAD && env.allHmmmvsDead()) {
			    deleteLockfile();
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "OPFOR WINS, all BLUEFOR HMMMVs are dead.");
			    }
			    shutdown("All HMMMVs are dead.");
                        }
			if(END_CONDITION_ALL_EMITTERS_DETECTED && env.allEmittersDetected()) {
			    deleteLockfile();
			    if(interactiveMode) {
				JOptionPane.showMessageDialog(null, "All emitters have been detected.");
			    }
			    shutdown("All emitters have been detected");
			}
//                         if(env.scenarioFinished()) {
//                             shutdown("Scenario is finished - there no longer exists any nondead hmmmvs that are not at their goal.");
//                         }
                    }

                    long timeAfterProc = System.currentTimeMillis();
		    timeElapsedProc = timeAfterProc - timeBeforeStep;
		    processingTimeLastNSteps += timeElapsedProc;

		    long plannedSleepTime = 0;
		    long timeSlept = 0;
		    if(STEP_TIME_FIXED) {
			long timeNextStep = timeBeforeStep+FIXED_MS_PER_STEP;
			while(true) {
			    long now = System.currentTimeMillis();
			    plannedSleepTime = (timeNextStep - now);
			    if(plannedSleepTime <= 0)
				break;
			    if((plannedSleepTime+leftOverSleepTime) <= MIN_SLEEP_RESOLUTION) {
				leftOverSleepTime += plannedSleepTime;
				break;
			    }
			    plannedSleepTime += leftOverSleepTime;
			    leftOverSleepTime = 0;
			    long timeStartSleep = System.currentTimeMillis();
			    try {sleep(plannedSleepTime);} catch (InterruptedException e) {}
			    timeSlept = System.currentTimeMillis() - timeStartSleep;
			    leftOverSleepTime = plannedSleepTime - timeSlept;
			    //			    Machinetta.Debugger.debug("Step="+stepCount+" tried to sleep for "+plannedSleepTime+", actually slept for "+timeSlept+", leftOverSleepTime="+leftOverSleepTime, 3, this);
			}
		    } else {
			plannedSleepTime = UPDATE_RATE - timeElapsedProc;
			if(plannedSleepTime > 0) {
			    try {
				sleep(plannedSleepTime);
			    } catch (InterruptedException e) {}
			}
		    }
		    timeAfterStep = System.currentTimeMillis();
		    actualSleepTimeThisStep = timeAfterStep - timeAfterProc;
		    //		    Machinetta.Debugger.debug("Step="+stepCount+" actualSleepTimeThisStep="+actualSleepTimeThisStep+", leftover="+leftOverSleepTime,3,this);
		    totalTimeThisStep = timeAfterStep - timeBeforeStep;
		    totalTimeLastNSteps += totalTimeThisStep;
                    if((updateRateCounter++ % UPDATE_RATE_PRINT) == 0) {
			double avgProcessingTimePerStep = processingTimeLastNSteps/((double)UPDATE_RATE_PRINT);
			double avgTimePerStep = totalTimeLastNSteps/((double)UPDATE_RATE_PRINT);
			Machinetta.Debugger.debug("STEPTIMESUMMARY: Step="+stepCount
						  +" totalTimeThisStep="+totalTimeThisStep
						  +", totalTimeLastNSteps="+totalTimeLastNSteps
						  +", avgTimePerStep="+avgTimePerStep
						  +", processingTimeLastNSteps="+processingTimeLastNSteps
						  +", avgProcessingTimePerStep="+avgProcessingTimePerStep
						  +", actualSleepTimeThisStep="+actualSleepTimeThisStep
						  +", plannedSleepTime="+plannedSleepTime
						  +", UPDATE_RATE="+UPDATE_RATE
						  +", requestedStepsPerSecond="+(1000.0/UPDATE_RATE)
						  +", FIXED_MS_PER_STEP="+FIXED_MS_PER_STEP
						  +", STEP_TIME_FIXED_ON="+STEP_TIME_FIXED,3,this);
			if(STEP_TIME_FIXED && (avgProcessingTimePerStep > FIXED_MS_PER_STEP)) {
			    Machinetta.Debugger.debug("WARNING Step time is supposed to be fixed at "+FIXED_MS_PER_STEP+" and average actual step time is "+(avgProcessingTimePerStep - FIXED_MS_PER_STEP) +" greater than that.", 4, this);
			}
			if(null != mainFrameGUI) {
			    mainFrameGUI.setActualSpeedLabel(1000.0/avgTimePerStep);
			}
			processingTimeLastNSteps = 0;
			totalTimeLastNSteps = 0;
                    }
                }
		else {
		    try {sleep(100);} catch (InterruptedException e) {}
		    pausedCount++;
		    if(pausedCount % 100 == 0) {
			Machinetta.Debugger.debug("Simulation paused for "+pausedCount+" steps...",1,this);
		    }
		}
            }
        }
    };
    
    public static final long serialVersionUID = 1L;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        boolean stepTimeFixed = false;
	long fixedMsPerStep = 25;
        int repaintInterval = 1000/15;
	int numTrucks = -1;
        boolean showGui = false;
        boolean snapshotGui = false;
	String switchHost = null;
	String switchGroup = null;
        
        if (args.length > 0) {
            Env.CONFIG_FILE = args[0];
        }
        for(int loopi = 1; loopi < args.length; loopi++) {
	    if(args[loopi].equalsIgnoreCase("--switchhost") && ((loopi+1) < args.length)) {
                switchHost = args[++loopi];
	    } else if(args[loopi].equalsIgnoreCase("--switchgroup") && ((loopi+1) < args.length)) {
                switchGroup = args[++loopi];
	    } else if(args[loopi].equalsIgnoreCase("--numtrucks") && ((loopi+1) < args.length)) {
                numTrucks = Integer.parseInt(args[++loopi]);
		Env.numTrucks = numTrucks;
	    } else if(args[loopi].equalsIgnoreCase("--showgui")) {
                showGui = true;
            } else if(args[loopi].equalsIgnoreCase("--experimentmode")) {
                experimentMode = true;
            } else if(args[loopi].equalsIgnoreCase("--snapshot")) {
                snapshotGui = true;
            } else if(args[loopi].equalsIgnoreCase("--runfour")) {
                stepTimeFixed = true;
		fixedMsPerStep = 250;
            } else if(args[loopi].equalsIgnoreCase("--runfourty")) {
                stepTimeFixed = true;
		fixedMsPerStep = 25;
            } else if(args[loopi].equalsIgnoreCase("--steptimefixed")) {
                stepTimeFixed = true;
            } else if(args[loopi].equalsIgnoreCase("--fixedmsperstep") && ((loopi+1) < args.length)) {
                stepTimeFixed = true;
                fixedMsPerStep = Integer.parseInt(args[++loopi]);
            } else if(args[loopi].equalsIgnoreCase("--repaintinterval") && ((loopi+1) < args.length)) {
                repaintInterval = Integer.parseInt(args[++loopi]);
            } else {
                System.err.println("Unable to parse option args["+loopi+"]="+args[loopi]);
                System.err.println("Usage: MainFrame [environment_config_file] [--repaintinterval nnn] [--showgui] [--snapshot] [--steptimefixed] [--fixedmsperstep nnn] [--runfour] [--runfourty]");
                System.err.println("        repaint interval is in milliseconds.");
                System.err.println("        snapshot forces showgui to be true.");
                System.err.println("        steptimefixed tells the simulator make each step take the same");
		System.err.println("            amount of time.  Default fixed time per step is 25ms, which");
		System.err.println("            equals 40steps/second .");
		System.err.println("        fixedmsperstep sets the fixed step time and also enables steptimefixed.");
		System.err.println("        runfour sets steptimefixed to true and fixedmsperstep to 250, i.e. 4/s");
		System.err.println("        runfourty sets steptimefixed to true and fixedmsperstep to 25, i.2. 40/s");
		
                System.exit(1);
            }
        }
        new MainFrame(stepTimeFixed, fixedMsPerStep, repaintInterval, showGui, snapshotGui, switchHost, switchGroup);
    }
    
}
