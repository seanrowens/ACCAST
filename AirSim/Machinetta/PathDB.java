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
 * PathDB
 *
 * Created on July 26, 2006, 5:29 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import AirSim.Machinetta.Path3D;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;

import java.awt.Rectangle;
import java.util.*;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.DecimalFormat;

/**
 *
 * @author owens
 */
public class PathDB implements  Runnable, StateChangeListener {
    private final static DecimalFormat fmt = new DecimalFormat("0.000000");
    private final static long INTERP_TIME_INCR = 1000;
    
    private double range;
    
    static private ProxyState state = new ProxyState();
    private BlockingQueue<PlannedPath> incomingPlannedPathQueue = new LinkedBlockingQueue<PlannedPath>();
    private Thread myThread = null;
    
    private ProxyID proxyid;
    private ProxyID getProxyID() {
        if(null == proxyid) {
            try {
                ProxyState state = new ProxyState();
                RAPBelief selfBel = state.getSelf();
                proxyid = selfBel.getProxyID();
            } catch(Exception e) {
                Machinetta.Debugger.debug("Ignoring exception, e="+e,1,this);
                e.printStackTrace();
            }
        }
        if(null == proxyid)
            return new NamedProxyID("PROXYID_NOT_YET_LOADED");
        return proxyid;
    }
    
    public static void main(String argv[]) {
        System.out.println("Testing");
        
        state.ready();
        
        PathDB pathDB = new PathDB(2000);
        pathDB.start();
        
        Path3D p = new Path3D();
        //p.addPoint(new Waypoint(0.0, 0.0, 100.0, 0));
        p.addPoint(new Waypoint(5000.0, 5000.0, 100.0, 5));
        p.addPoint(new Waypoint(10000.0, 0.0, 100.0, 10));
        p.setAssetID(new NamedProxyID("UAV"+1));
        PlannedPath path = new PlannedPath(p, new NamedProxyID("UAV"+1));
        state.addBelief(path);
        state.notifyListeners();
        
        Path3D p2 = new Path3D();
        p2.addPoint(new Waypoint(10000.0, 0.0, 100.0, 0));
        p2.addPoint(new Waypoint(0.0, 0.0, 100.0, 10));
        p2.setAssetID(new NamedProxyID("UAV"+2));
        PlannedPath path2 = new PlannedPath(p2, new NamedProxyID("UAV"+2));
        state.addBelief(path2);
        state.notifyListeners();
        
        Path3D p3 = new Path3D();
        p3.addPoint(new Waypoint(10000.0, 0.0, 100.0, 2));
        p3.addPoint(new Waypoint(0.0, 0.0, 100.0, 10));
        p3.setAssetID(new NamedProxyID("UAV"+3));
        PlannedPath path3 = new PlannedPath(p3, new NamedProxyID("UAV"+3));
        state.addBelief(path3);
        state.notifyListeners();
        
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
        
        System.exit(0);
    }
    
    private class PathInfo {
	Path3D origPath;
	Path3D interpPath;
	BeliefID beliefID;
	ProxyID owner;
	public PathInfo(Path3D orig, Path3D interp, BeliefID id, ProxyID owner ) {
	    origPath = orig;
	    interpPath = interp;
	    beliefID = id;
	    this.owner = owner;
	}
    }

    HashMap<BeliefID,PathInfo>pathMap = new HashMap<BeliefID,PathInfo>();
    
    /** Creates a new instance of PathDB
     * @param range maximum range between a point we're getting cost
     * for and a point on any path.  Costs will be scaled from 0 to
     * max range.
     **/
    public PathDB(double range) {
        this.range = range;

        myThread = new Thread(this);
    }
    
    public void start() {
        Machinetta.Debugger.debug("Starting PathDB thread",1,this);
        state.addChangeListener(this);
        myThread.start();
    }
    
    /**
     *
     * Called when something updates state
     *
     *
     * @param b array of BeliefIDs that have changed.
     */
    public void stateChanged(BeliefID[] b) {
        
        int numPlannedPaths = 0;
        for (BeliefID id: b) {
            Belief bel = state.getBelief(id);
            if (bel instanceof PlannedPath) {
                numPlannedPaths++;
                try {
                    incomingPlannedPathQueue.put((PlannedPath)bel);
                } catch (InterruptedException e) {
                    
                }
            }
        }
        if(numPlannedPaths > 0)
            Machinetta.Debugger.debug("Added "+numPlannedPaths+" PlannedPaths to incomingPlannedPathQueue.",0,this);
    }
    
    public void expireOldPaths(long simTime) {
        long time = SimTime.getEstimatedTime();
        synchronized(pathMap) {
            Iterator<PathInfo> iter = pathMap.values().iterator();
            while(iter.hasNext()) {
                PathInfo pathInfo = iter.next();
                if(pathInfo.origPath.isDone(time)) {
                    iter.remove();
                    Machinetta.Debugger.debug("Removed expired path "+pathInfo.origPath,1,this);
                }
            }
        }
        
//         Path3D paths[] = new Path3D[1];
//         synchronized(pathMap) {
//             paths = pathMap.values().toArray(paths);
//         }
//         if(null == paths)
//             return;
//         if(paths.length <= 0)
//             return;
//         for(int loopi = 0; loopi < paths.length; loopi++ ) {
//             if(null == paths[loopi])
//                 continue;
//             paths[loopi].expireOldPathSegments(simTime);
//         }
    }
    
//     public Path3D checkConflictsOld(Path3D path, BeliefID bid) {
// 	Stats.otherPathConflictChecked(1);
// 	StringBuffer checkedAgainst = new StringBuffer();
//         Waypoint[] pathAry = path.getWaypointsAry();
        
//         Path3D paths[] = new Path3D[1];
//         synchronized(pathMap) {
//             paths = pathMap.values().toArray(paths);
//         }
//         if(null == paths)
//             return null;
//         if(paths.length <= 0)
//             return null;
//         for(int loopi = 0; loopi < paths.length; loopi++ ) {
//             if(null == paths[loopi])
//                 continue;
// 	    ProxyID curPathAssetID = paths[loopi].getAssetID();
// 	    BeliefID curPathID = pathIDMap.get(curPathAssetID);
//             if(curPathAssetID.equals(path.getAssetID())) {
// 		checkedAgainst.append("Skipped "+curPathID+", ");
//                 continue;
//             }
//             Waypoint[] otherPathAry = paths[loopi].getWaypointsAry();
//             if(otherPathAry.length <= 0)
//                 continue;
//             if(otherPathAry.length == 1) {
//                 if(otherPathAry[0] == null)
//                     continue;
//             }
// 	    checkedAgainst.append(curPathID+", ");	    

// 	    Stats.otherPathConflictComparison(1);
            
//             // @todo This "range" value is tricky in the case of 3D flight, because if one a/c is range directly above the other, it is
//             // probably very safe.  A smarter number is required.
//             if(Path3D.conflicts(pathAry, otherPathAry,range,100)) {
// 		Stats.otherPathConflictDetected(1);
// 		Machinetta.Debugger.debug("        simtime "+SimTime.getEstimatedTime()+" CONFLICT with path "+curPathID+", checked new path "+bid.toString()+" from "+path.getAssetID()+" against: "+checkedAgainst.toString(),1,this);
//                 return paths[loopi];
// 	    }
//         }

// 	Machinetta.Debugger.debug("        simtime "+SimTime.getEstimatedTime()+" no conflict, checked new path "+bid.toString()+" from "+path.getAssetID()+" against: "+checkedAgainst.toString(),1,this);

//         return null;
//     }

    public PathInfo checkConflicts(PathInfo newPathInfo) {
	Stats.otherPathConflictChecked(1);
	StringBuffer checkedAgainst = new StringBuffer();
        Waypoint[] newPathAry = newPathInfo.interpPath.getWaypointsAry();
        
        PathInfo pathInfos[] = new PathInfo[1];
        synchronized(pathMap) {
            pathInfos = pathMap.values().toArray(pathInfos);
        }
        if(null == pathInfos) {
	    Machinetta.Debugger.debug("        can't check "+newPathInfo.beliefID+" for conflicts - we don't have any paths!",1,this);
            return null;
	}
        if(pathInfos.length <= 0) {
	    Machinetta.Debugger.debug("        can't check "+newPathInfo.beliefID+" for conflicts - we don't have any paths!",1,this);
            return null;
	}
        for(int loopi = 0; loopi < pathInfos.length; loopi++ ) {
            if(null == pathInfos[loopi])
                continue;
	    ProxyID otherPathAssetID = pathInfos[loopi].owner;
	    BeliefID otherPathBeliefID = pathInfos[loopi].beliefID;
            if(otherPathAssetID.equals(newPathInfo.owner)) {
		checkedAgainst.append("Skipped "+otherPathBeliefID+", ");
                continue;
            }
            Waypoint[] otherPathAry = pathInfos[loopi].interpPath.getWaypointsAry();
            if(otherPathAry.length <= 0)
                continue;
            if(otherPathAry.length == 1) {
                if(otherPathAry[0] == null)
                    continue;
            }
	    checkedAgainst.append(otherPathBeliefID+", ");	    

	    Stats.otherPathConflictComparison(1);
            
            // @todo This "range" value is tricky in the case of 3D flight, because if one a/c is range directly above the other, it is
            // probably very safe.  A smarter number is required.
            if(Path3D.conflicts(newPathAry, otherPathAry,range,150)) {
		Stats.otherPathConflictDetected(1);
		Machinetta.Debugger.debug("        simtime "+SimTime.getEstimatedTime()+" CONFLICT with path "+otherPathBeliefID+", checked new path "+newPathInfo.beliefID+" from "+newPathInfo.owner+" against: "+checkedAgainst.toString(),1,this);
                return pathInfos[loopi];
	    }
        }

	Machinetta.Debugger.debug("        simtime "+SimTime.getEstimatedTime()+" no conflict, checked new path "+newPathInfo.beliefID+" from "+newPathInfo.owner+" against: "+checkedAgainst.toString(),1,this);

        return null;
    }
    
    
    /** These don't appear to be used, I will delete later.  Paul
     * public void addPath(Path3D path) {
     * Machinetta.Debugger.debug("Adding path for "+path.getAssetID(),1,this);
     * pathMap.put(path.getAssetID(),path);
     * }
     *
     * public void removePath(Path3D path) {
     * Machinetta.Debugger.debug("Removing path for "+path.getAssetID(),1,this);
     * pathMap.remove(path.getAssetID());
     * }
     */
    
    public void run() {
        LinkedList<PlannedPath> beliefList = new LinkedList<PlannedPath>();
        
        while(true) {
            try {
                
                beliefList.clear();
                try {
                    PlannedPath newPlannedPath = incomingPlannedPathQueue.take();
                    beliefList.add(newPlannedPath);
                    incomingPlannedPathQueue.drainTo(beliefList);
                } catch (InterruptedException e) {
                    
                }
                
                Machinetta.Debugger.debug("We see "+beliefList.size()+" plannedpaths, pathMap.size() = "+pathMap.size(), -1, this);
                int numBeliefs = beliefList.size();
                long startTime = System.currentTimeMillis();
                
                long simTime = SimTime.getEstimatedTime();
		expireOldPaths(simTime);
                
                for (PlannedPath plannedPath: beliefList) {
                    if(plannedPath.conflicted != null) {
			Machinetta.Debugger.debug("Not checking for conflicts for conflict report id "+plannedPath.getID()+" for original path "+plannedPath.originalPlannedPathID, 1, this);
			pathMap.remove(plannedPath.originalPlannedPathID);
                        continue;
		    }
                    Machinetta.Debugger.debug("Checking for conflict for path "+plannedPath.getID()+" owned by "+plannedPath.owner+" among "+pathMap.size()+" paths in pathMap", 1, this);
                    

		    Path3D newPath = plannedPath.path;

		    long pathStartTime = newPath.getTimeAtStart();
		    long pathEndTime = newPath.getTimeAtEnd();

		    long startInter = (pathStartTime /INTERP_TIME_INCR)*INTERP_TIME_INCR;
		    long endInter = (pathEndTime /INTERP_TIME_INCR)*INTERP_TIME_INCR+INTERP_TIME_INCR;
		    Path3D iNewPath = Path3D.interpolatePath(newPath,startInter,endInter,INTERP_TIME_INCR);
		    PathInfo pathInfo = new PathInfo(newPath, iNewPath, plannedPath.getID(), plannedPath.owner);

		    PathInfo conflictingPath = null;
		    //		    Machinetta.Debugger.debug("CONFLICT_CHECK_CASE_4 : full blown deconfliction",1,this);
		    conflictingPath = checkConflicts(pathInfo);
                    if(null != conflictingPath) {
                        // Found a conflict, send some kind of message back
                        Machinetta.Debugger.debug("Simtime="+SimTime.getEstimatedTime()+" FOUND CONFLICT between new path "+pathInfo.beliefID+" and known path "+conflictingPath.beliefID+" for asset "+conflictingPath.owner+",  new path="+pathInfo.origPath+", known path="+conflictingPath.origPath,1,this);
                        
                        // Machinetta.Debugger.debug("Doing nothing, in test mode", 1, this);
                        
                        PlannedPath conflictBelief = new PlannedPath(plannedPath.path, plannedPath.owner);
                        conflictBelief.originalPlannedPathID = plannedPath.getID();
                        conflictBelief.conflicted = conflictingPath.origPath;
                        conflictBelief.conflictDetectedBy = getProxyID();
                        // We want this conflict message to trip the
                        // DirectedInformationRequirement that was set up
                        // in UAVRI, but that won't happen if it isn't
                        // locallySensed.
                        //
                        //		    conflictBelief.setLocallySensed(true);
                        InformationAgent agent = new InformationAgent(conflictBelief, conflictBelief.owner);
                        MACoordination.addAgent(agent);
                        // Let it act
                        agent.stateChanged();
                        // this line was in the code I copied from
                        // PathPlanner that sent out the beleif in the
                        // first place
                        //
                        state.addBelief(conflictBelief);
                        Machinetta.Debugger.debug("Finished sending InformationAgent with plannedPath conflict (id="+conflictBelief.getID()+") back to "+plannedPath.owner.toString(), 1, this);
                        
                    } else {
                        // no conflict, add to our local path db.
                        Machinetta.Debugger.debug("No conflict found for path id "+plannedPath.getID()+", path="+plannedPath.path,1,this);
                        pathMap.put(pathInfo.beliefID,pathInfo);
                    }
                }
                
                long endTime = System.currentTimeMillis();
                long elapsed = endTime - startTime;
                double avg = elapsed/numBeliefs;
                Machinetta.Debugger.debug("Time to process "+numBeliefs+" plannedpaths = "+elapsed+" avg per plannedPath="+fmt.format(avg),1,this);
            } catch (Exception e) {
                Machinetta.Debugger.debug("EXCEPTION occurred while checking for conflicts, ignoring, e="+e,5,this);
                e.printStackTrace();
            }
            
        }
    }
    
}
