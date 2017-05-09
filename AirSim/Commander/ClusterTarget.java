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
 * Clustering.java
 *
 * Created on March 20, 2006, 9:44 AM
 */

package AirSim.Commander;


import Gui.*;
import AirSim.Environment.Area;
import AirSim.Machinetta.CostMaps.Clust;
import AirSim.Machinetta.CostMaps.BinaryBayesDisplay;
import AirSim.Machinetta.CostMaps.BinaryBayesFilter;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import AirSim.Machinetta.CostMaps.ClusterUtils;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.Point2D;
import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.RRTPlanner;
import Machinetta.ConfigReader;
import Machinetta.Configuration;

import Machinetta.Debugger;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.Belief;
import java.awt.geom.Ellipse2D;
import java.util.*;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Toolkit;
import java.awt.Rectangle;
import java.awt.Point;
import javax.swing.*;
import java.awt.Dimension;

/**
 *
 * @author  owens
 */
public class ClusterTarget   {

    public boolean newTarget = true;
    public Clust cluster;
    public String key1;
    public String key2;
    public MapObject mo1;
    public MapObject mo2;
    public double lastMoveDistMeters = 0.0;
    public int updates=0;
    public int updatesSinceLastSeen=0;
    public int updatesSinceLastMove = 0;
    public boolean tasked = false;
    public Clust newCluster = null;
    public double newClusterDistSqd = 0.0;

    private void makekeys() {
	key1="Cluster:"+cluster.getKey();
	key2="Cluster:"+cluster.getKey()+" ";
    }
    public void removeFromMap(MapDB mapDB) {
	if(null != mo1)
	    mapDB.remove(mo1);
	if(null != mo2)
	    mapDB.remove(mo2);
    }
    private void makeMapObjects(MapDB mapDB) {
	removeFromMap(mapDB);
	mo1 = new MapObject(key1, MapObject.TYPE_MAP_GRAPHIC, cluster.x, cluster.y, 0, cluster.memberDistMean,0);
	mapDB.add(mo1);
	// @TODO: Change size of circle to be a config parameter - or perhaps alter based on mapscale?
	mo2 = new MapObject(key2, MapObject.TYPE_MAP_GRAPHIC, cluster.x, cluster.y, 0, cluster.memberDistMax+(100*UAVRI.BBF_GRID_SCALE_FACTOR),0);
	mapDB.add(mo2);
    }
    public ClusterTarget(Clust clust, MapDB mapDB) {
	cluster = clust;
	updates = 1;
	makekeys();
	makeMapObjects(mapDB);
    }
    public String update(MapDB mapDB, double moveLimitSqdMeters) {
	if(newTarget) {
	    newTarget = false;
	    Machinetta.Debugger.debug("        updated: NEW TARGET : "+toString(), 1, this);
	}
	else if((null != newCluster) 
		&& (newClusterDistSqd < moveLimitSqdMeters)) {
	    updatesSinceLastSeen = 0;
	    updates++;
	    lastMoveDistMeters = Math.sqrt(newClusterDistSqd);
	    cluster = newCluster;
	    newCluster = null;
	    newClusterDistSqd = 0;
	    if(lastMoveDistMeters > 0) {
		updatesSinceLastMove = 0;
		makekeys();
		makeMapObjects(mapDB);
		return "MOVED";
	    }
	    else {
		updatesSinceLastMove++;
		return "NOT MOVED";
	    }
	}
	else {
	    updatesSinceLastSeen++;
	    return "NO CLUSTER";
	}

	return null;
    }
    public String toString() {
	return key1+" updates "+updates+" sinceLastMove "+updatesSinceLastMove+" sinceLastSeen "+updatesSinceLastSeen+" tasked "+tasked+" cluster "+cluster.toString();
    }
}
