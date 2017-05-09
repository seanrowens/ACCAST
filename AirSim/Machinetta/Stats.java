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

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.ProxyState;

public class Stats {
    // total times we called plan()
    static long pathsGenerated = 0;	
    synchronized static public long pathsGenerated(int val) { pathsGenerated += val; return pathsGenerated;}
    // total number of paths returned from plan() that were short and thrown away
    static long pathsGeneratedShort = 0;	
    synchronized static public long pathsGeneratedShort(int val) { pathsGeneratedShort += val; return pathsGeneratedShort;}
    // total number of good paths returned from plan() (not short)
    static long pathsGeneratedGood = 0;	
    synchronized static public long pathsGeneratedGood(int val) { pathsGeneratedGood += val; return pathsGeneratedGood;}
    // total number of plans we shared
    static long pathsShared = 0;	
    synchronized static public long pathsShared(int val) { pathsShared += val; return pathsShared;}
    // how many waypoints did we send?
    static long waypointsSent = 0;	
    synchronized static public long waypointsSent(int val) { waypointsSent += val; return waypointsSent;}
    // how many waypoints did we reach?
    static long waypointsReached = 0;	
    synchronized static public long waypointsReached(int val) { waypointsReached += val; return waypointsReached;}
    // How many paths did we reach the end of?
    static long pathsFinished = 0;	
    synchronized static public long pathsFinished(int val) { pathsFinished += val; return pathsFinished;}

    // total number of our shared paths that had conflicts
    static long conflictsFound = 0;	
    synchronized static public long conflictsFound(int val) { conflictsFound += val; return conflictsFound;}
    // number of our shared paths that had conflicts while still nextPath
    static long conflictsFoundNextPath = 0;	
    synchronized static public long conflictsFoundNextPath(int val) { conflictsFoundNextPath += val; return conflictsFoundNextPath;}
    // number of our shared paths that had conflicts while already currPath
    static long conflictsFoundCurrPath = 0;	
    synchronized static public long conflictsFoundCurrPath(int val) { conflictsFoundCurrPath += val; return conflictsFoundCurrPath;}

    // total times we received someone elses path to check for conflicts
    static long otherPathConflictChecked = 0;	
    synchronized static public long otherPathConflictChecked(int val) { otherPathConflictChecked += val; return otherPathConflictChecked;}
    // total times we compared someone elses path to a path in our db
    static long otherPathConflictComparison = 0;	
    synchronized static public long otherPathConflictComparison(int val) { otherPathConflictComparison += val; return otherPathConflictComparison;}
    // total number of conflicts we detected
    static long otherPathConflictDetected = 0;	
    synchronized static public long otherPathConflictDetected(int val) { otherPathConflictDetected += val; return otherPathConflictDetected;}

    synchronized static public void init() {
	pathsGenerated = 0;
	pathsGeneratedShort = 0;
	pathsGeneratedGood = 0;
	pathsShared = 0;
	waypointsSent = 0;
	waypointsReached = 0;
	pathsFinished = 0;
	conflictsFound = 0;
	conflictsFoundNextPath = 0;
	conflictsFoundCurrPath = 0;
	otherPathConflictChecked = 0;
	otherPathConflictComparison = 0;
	otherPathConflictDetected = 0;
        Machinetta.Debugger.debug("STATS: "+SimTime.getEstimatedTime()+"\t"+getHeader(),1,"Stats");
    }

    public Stats() {
	init();
    }

    private static String myIdString = null;
    synchronized static public String getStats() {
	if(myIdString == null) {
	    ProxyState state = new ProxyState();
	    ProxyID myid = state.getSelf().getProxyID();
	    if(myid != null)
		myIdString = myid.toString();
	}
	return myIdString+"\tPATHS:\t"+pathsGenerated+"\t"+pathsGeneratedShort+"\t"+pathsShared+"\tWP:\t"+waypointsSent+"\t"+waypointsReached+"\tCONF:\t"+conflictsFound+"\t"+conflictsFoundNextPath+"\t"+conflictsFoundCurrPath+"\tFIN:\t"+pathsFinished+"\tOTHER:\t"+otherPathConflictChecked+"\t"+otherPathConflictComparison+"\t"+otherPathConflictDetected;
    }
    synchronized static public String getHeader() {
	return "ID\t\tPaths Generated\tPaths Generated Short\tPaths Shared\t\tWaypoints Sent\tWaypoints Reached\t\tConflicts Found\tConflicts Found NextPath\tConflicts Found CurrPath\t\tPaths Finished\t\tOtherPath Conflict Checked\tOtherPath Conflict Comparison\tOtherPath Conflict Detected";
    }
}
