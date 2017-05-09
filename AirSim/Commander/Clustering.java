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
import AirSim.Machinetta.CostMaps.BBFMan;
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
public class Clustering implements Runnable {

    private final static boolean TASK_PICTURES_AUTOMATICALLY = true;

    private final static long SLEEP_BETWEEN_CLUSTER_ATTEMPT_MS = 6000;
    private final static double TARGET_UPDATES_SINCE_LAST_SEEN_LIMIT = 10;
    private final static double TARGET_UPDATES_SINCE_LAST_MOVE_LIMIT = 3;
    private static double TARGET_MOVE_LIMIT_METERS = 50;
    private static double TARGET_MOVE_LIMIT_SQD_METERS = TARGET_MOVE_LIMIT_METERS * TARGET_MOVE_LIMIT_METERS;

    private BinaryBayesFilterCostMap bbfCM = null;
    private SimOperatorGUI gui = null;
    int gridSize = 0;

    private Thread myThread;
    private double gridScaleFactor=0;
    private double sensorMaxRangeMeters=0;

    private ClusterUtils clusterUtils;
    private ArrayList<ClusterTarget> targetList = new ArrayList<ClusterTarget>();
    private ArrayList<Clust> clustList = null;

    /** Creates new form Clustering */
    public Clustering(BinaryBayesFilterCostMap bbfCM, SimOperatorGUI gui,int gridSize) {

	this.bbfCM = bbfCM;
	this.gui = gui;
	this.gridSize = gridSize;

        RRTPlanner.mapStep = (int)Math.max(1.0, (UAVRI.BBF_GRID_SCALE_FACTOR*2.0));
	myThread = new Thread(this);

	gridScaleFactor = UAVRI.BBF_GRID_SCALE_FACTOR;
	sensorMaxRangeMeters = UAVRI.BBF_SENSOR_MAX_RANGE_METERS;
	TARGET_MOVE_LIMIT_METERS = TARGET_MOVE_LIMIT_METERS*gridScaleFactor;
	TARGET_MOVE_LIMIT_SQD_METERS = TARGET_MOVE_LIMIT_METERS * TARGET_MOVE_LIMIT_METERS;
	clusterUtils = new ClusterUtils(gridScaleFactor,sensorMaxRangeMeters,gridSize);
    }

    public void start() {
	myThread.start();
    }

    public void run() {
	double[][] beliefs = new double[gridSize][gridSize];
	DoubleGrid entropyGrid = new DoubleGrid(gridSize,gridSize, (int)gridScaleFactor);;

	BBFMan bbfMan=bbfCM.getBBFMan();
	if(null == bbfMan) {
	    Debugger.debug( 4, "CLUSTERING.run: bbfMan from bbfCostMap is null.  Can't do clustering.");
	}

	Debugger.debug( 1,"Started Clustering thread");
	int clusteringCounter = 0;
                
	while (true) {
	    clusteringCounter++;
	    Debugger.debug( 1,"CLUSTERING:"+clusteringCounter+": Starting clustering attempt");
	    Integer[] channels = bbfMan.getFilterChannels();
	    for(int loopi = 0; loopi < channels.length; loopi++) {
		if(null == channels[loopi]) {
		    Debugger.debug( 3,"CLUSTERING.run: channel for index "+loopi+" is null");
		    continue;
		}
		BinaryBayesFilter bbf = bbfMan.getFilter(channels[loopi]);
		bbf.copyBeliefs(beliefs);
		bbf.getEntropyGrid(entropyGrid);
		performClustering(channels[loopi],clusteringCounter,beliefs,entropyGrid);
	    }

	    clustList = new ArrayList<Clust>();
	    ListIterator<ClusterTarget> iter = targetList.listIterator();
	    while(iter.hasNext()) {
		ClusterTarget target = iter.next();
		clustList.add(target.cluster);
	    }
	    
	    bbfMan.setClustList(clustList);
	    Debugger.debug( 1,"CLUSTERING:"+clusteringCounter+": Finished clustering attempt");
	    try {
		myThread.sleep (SLEEP_BETWEEN_CLUSTER_ATTEMPT_MS);
	    } catch (InterruptedException e) {}
	}
    }

    private void performClustering(int channel, int clusteringCounter, double[][] beliefs, DoubleGrid entropyGrid) {

	String dbgPref = "CLUSTERING: channel "+channel+": ";
	ArrayList<Clust> clusterList = clusterUtils.checkClusters(channel, entropyGrid, beliefs);

	if(null != clusterList) {
	    // Create list of !failed Clust objects
	    ArrayList<Clust> goodClustList = new ArrayList<Clust>();
	    for(int loopi = 0; loopi < clusterList.size(); loopi++) {
		Clust cluster = clusterList.get(loopi);
		if(!cluster.failed) {
		    goodClustList.add(cluster);
//		    Debugger.debug( 1,dbgPref+"cluster "+cluster.getKey()+" good");
		}
		else {
//		    Debugger.debug( 1,dbgPref+"cluster "+cluster.getKey()+" failed");
		}
	    }
	    // Throw away old list just to make sure I don't screw something up in the code below.
	    clusterList = null;
	    if(goodClustList.size() == 0)
		Debugger.debug( 1,dbgPref+"goodClustList all clusters failed");
			    
	    // Try to correlate the !failed Clust objects with the
	    // TargetCluster objects we have from prior clustering
	    // runs.
	    //
	    // This is trickier than it sounds.  Basically we find the
	    // closest Clust/TargetCluster pair in both sets, then we
	    // remove both of them from each set and update the
	    // TargetCluster based on the closest Clust.
	    ArrayList<ClusterTarget> targetListCopy = new ArrayList<ClusterTarget>(targetList);
	    while((targetListCopy.size() != 0) && (goodClustList.size() != 0)) {
		ClusterTarget bestTarget = null;
		Clust bestCluster = null;
		double bestDistSqd = Double.MAX_VALUE;

		// Find closest pair of !failed Clust object and
		// TargetCluster object
		for(int loopi = 0; loopi < targetList.size(); loopi++) {
		    ClusterTarget target = targetList.get(loopi);
		    for(int loopj = 0; loopj < goodClustList.size(); loopj++) {
			Clust cluster = goodClustList.get(loopj);
			double dSqd = cluster.distSqd(target.cluster);
			if(dSqd < bestDistSqd) {
			    bestDistSqd = dSqd;
			    bestTarget = target;
			    bestCluster = cluster;
			}
		    }
		}

		// We assume that when the closest pair are farther
		// apart than some amount, then all we have left of
		// the !failed clusters are new clusters that we
		// haven't seen before and hence don't correlate to
		// anything in TargetCluster list.  So we break out of
		// the loop.
		if(bestDistSqd > TARGET_MOVE_LIMIT_SQD_METERS)
		    break;
		
		// Update the TargetCluster with the corresponding
		// Clust.
		if(null != bestTarget) {
		    bestTarget.newCluster = bestCluster;
		    bestTarget.newClusterDistSqd = bestDistSqd;
		    targetListCopy.remove(bestTarget);
		    goodClustList.remove(bestCluster);
		}
	    }
	    targetListCopy = null;
			    
	    // Any clusters remaining in goodClustList are NEW
	    // targets, so create them and add them to targetList.
	    for(int loopj = 0; loopj < goodClustList.size(); loopj++) {

		Clust cluster = goodClustList.get(loopj);
		ClusterTarget newTarget = new ClusterTarget(cluster, gui.getMapDB());
		targetList.add(newTarget);
		Debugger.debug( 1,dbgPref+"Created new target "+newTarget);
	    }

	    // Now finalize the cluster updates for the old targets
	    for(int loopi = 0; loopi < targetList.size(); loopi++) {
		ClusterTarget target = targetList.get(loopi);
		String updateStatus = target.update(gui.getMapDB(),TARGET_MOVE_LIMIT_SQD_METERS);
		Debugger.debug( 1,dbgPref+"Update target "+target.key1+" status "+updateStatus+" "+target.toString());
	    }

	}
	else {
	    Debugger.debug( 1,dbgPref+"null clusterList");
	}

	// Remove any old targets that haven't been seen in a
	// while... but not if they're already tasked!
	ListIterator<ClusterTarget> iter = targetList.listIterator();
	while(iter.hasNext()) {
	    ClusterTarget target = iter.next();
	    if(!target.tasked) {
		if(target.updatesSinceLastSeen > TARGET_UPDATES_SINCE_LAST_SEEN_LIMIT) {
		    Debugger.debug( 1,dbgPref+"removing target "+target.key1+" since updatesSinceLastSeen="+target.updatesSinceLastSeen+" > limit of "+TARGET_UPDATES_SINCE_LAST_SEEN_LIMIT);
		    target.removeFromMap(gui.getMapDB());
		    iter.remove();
		}
		else if(target.updatesSinceLastMove > TARGET_UPDATES_SINCE_LAST_MOVE_LIMIT) {
		    Debugger.debug( 1,dbgPref+"Tasking target "+target.key1+" since updatesSinceLastMove="+target.updatesSinceLastSeen+" > limit of "+TARGET_UPDATES_SINCE_LAST_MOVE_LIMIT );
		    if(TASK_PICTURES_AUTOMATICALLY) {
			Debugger.debug( 0,dbgPref+"Tasking automatically for "+target.key1 );
			gui.task(target);			// Task it!
		    }
		}
	    }
	}
    }
}
