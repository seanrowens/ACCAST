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
 * BeliefCostMap.java
 *
 * Created on April 28, 2006, 9:51 AM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.SimTime;
import AirSim.Machinetta.BeliefShare;
import Machinetta.ConfigReader;
import Gui.StringUtils;
import AirSim.Machinetta.Beliefs.RSSIReading;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
// import AirSim.Machinetta.RSSIReading;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Gui.IntGrid;
import Gui.DoubleGrid;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.DecimalFormat;

/**
 *
 * @author owens
 */
public class BeliefCostMap implements CostMap, Dynamic, StateChangeListener {
    private final static DecimalFormat fmt = new DecimalFormat("0.000000");
    private final static double HIGH_BELIEF_THRESHOLD = .8;
    private double mapWidth;
    private double mapHeight;
    private double mapScale;

    // The size of the internal grid of probabilities for the binary
    // bayes filter.
    private int binaryBayesFilterGridSize;

    private BinaryBayesFilterCostMap bbfCM = null;
    //    public BinaryBayesFilter getBinaryBayesFilter() { return bbfCM; }

    private ArrayList<Point> emitters;

    private double timeStamp = 0.0;
    private double entropy = 0.0;

    private static Random rand = new Random();

    private double[][] beliefs;
    private double[][] beliefs2;
    
    public BeliefCostMap(double mapWidth, double mapHeight, BinaryBayesFilterCostMap bbfCM) {
	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	this.bbfCM = bbfCM;

	mapScale = UAVRI.BBF_GRID_SCALE_FACTOR;
	binaryBayesFilterGridSize = (int)(mapWidth/mapScale);
	beliefs = new double[binaryBayesFilterGridSize][binaryBayesFilterGridSize];
	beliefs2 = new double[binaryBayesFilterGridSize][binaryBayesFilterGridSize];

        Machinetta.Debugger.debug("Set mapWidth,mapHeight to "+mapWidth+", "+mapHeight,1,this);
        state.addChangeListener(this);
    }
    
    /**
     * Simple implementation
     */
    
    public boolean withinGridBounds(int gridx, int gridy){
        boolean validLocation = true;
        if(gridx>=binaryBayesFilterGridSize) validLocation = false;
        if(gridx<0) validLocation = false;
        if(gridy>=binaryBayesFilterGridSize) validLocation = false;
        if(gridy<0) validLocation = false;
        return validLocation;
    }
    
    Boolean rebuildBeliefs = true;

    private void rebuildBeliefs() {
        long timeStart = System.currentTimeMillis();
	bbfCM.copyBeliefs(beliefs);
        for(int loopx = 0; loopx < beliefs.length; loopx++) {
            for(int loopy  = 0; loopy  < beliefs[0].length; loopy++) {
		if(beliefs[loopx][loopy] > .9) {
		    int startx = loopx - 40;
		    int endx = loopx - 40;
		    int starty = loopy - 40;
		    int endy = loopy - 40;
		    if(startx < 0) startx = 0;
		    if(endx >= beliefs.length) endx = beliefs.length -1;
		    if(starty < 0) starty = 0;
		    if(endy >= beliefs[0].length) endy= beliefs[0].length -1;

		    for(int loopi = startx; loopi <= endx; loopi++) {
			for(int loopj = starty; loopj <= endy; loopj++) {
			    beliefs2[loopi][loopj] = 1.0;
			}
		    }
		}
		else {
		    beliefs2[loopx][loopy] = beliefs[loopx][loopy];
		}
	    }
	}	
//         for(int loopx = 100; loopx < 200; loopx++) {
//             for(int loopy  = 100; loopy  < 300; loopy++) {
// 		beliefs2[loopx][loopy] = 1.0;
// 	    }
// 	}
        long timeEnd = System.currentTimeMillis();
        long timeElapsed = (timeEnd - timeStart);
        Machinetta.Debugger.debug("rebuildBeliefs: elapsed="+timeElapsed,1,this);
    }    
    
    public void rebuild() {
	synchronized(rebuildBeliefs) {
	    if(rebuildBeliefs) {
		rebuildBeliefs();
		rebuildBeliefs = false;
	    }
	}
    }
    
    static private ProxyState state = new ProxyState();
    
    /**
     * Return the cost if a vehicle moves from (x1,y1,z1) at t1 to (x2,y2,z2) at t2
     */
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        
        // Paul reversed these
//         y1 = mapHeight - y1;
//         y2 = mapHeight - y2;
        
        double belief = 0.0;
        double cost = 0.0;
        
        if (x1 > x2) {
            double temp = x2;
            x2 = x1;
            x1 = temp;
        }
        
        if (y1 > y2) {
            double temp = y2;
            y2 = y1;
            y1 = temp;
        }
        
 	int gridx1 = (int)(x1/mapScale);
 	int gridy1 = (int)(y1/mapScale);
 	int gridx2 = (int)(x2/mapScale);
 	int gridy2 = (int)(y2/mapScale);
	if(!withinGridBounds(gridx1, gridy1) || !withinGridBounds(gridx2, gridy2))
	    return Double.MAX_VALUE;

        double steps = 10;
        double ldx = (x2-x1)/steps;
        double ldy = (y2-y1)/steps;
        
        for (int i = 0; i < steps; i++) {
            x1 += ldx; y1 += ldy;
            int x = (int)Math.floor(x1);
            int y = (int)Math.floor(y1);
	    int gridx = (int)(x/mapScale);
	    int gridy = (int)(y/mapScale);
	    //belief += beliefs[gridx][gridy];
	    if(beliefs2[gridx][gridy] > HIGH_BELIEF_THRESHOLD) {
		belief += 10.0;
	    }
        }

	// Cost for a line segment should be between 0 and 50.
	// Negative cost is a reward, so reward should be between 0
	// and -50.
	// 
	// Any single belief sample is between 0.0 and 1.0.
	//
	// 'steps' is 10
	// 
        // the summed belief for a line segment is thus between 0 and
        // (steps * 1.0), i.e. 0 and 10;
	//
	// The higher the belief, the more we want to explore that area.
	//
	// We want belief=0.0 to result in cost = 50.0, and belief =
	// 10.0 to result in cost = -50.0;
	//
	// So...
	double span = (1.0 * steps);
	double halfSpan = span/2;
	double costScaleFactor = 100.0/steps;
	//	cost =  (belief - halfSpan) * costScaleFactor;
	// Change so cost is 0 to -100
	cost =  belief * costScaleFactor;
        
        // Paul inverted this
        return -cost;
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1) at t1 to (x2,y2) at t2
     */
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        // This is where the belief would need to be used to calculate a cost
        return getCost(x1,y1,0,t1,x2,y2,0,t2);
    }
    
    /**
     *
     * For dynamic cost maps, allows them to update.
     */
    public void timeElapsed(long t) {
        // Do nothing
    }
    
    /**
     * Which Rectangles are good to be in at time t.
     */
    public ArrayList<Rectangle> getGoodAreas(long t) {
        return null;
    }
    
    /**
     * Which Rectangles are bad to be in at time t.
     */
    public ArrayList<Rectangle> getBadAreas(long t) {
        return null;
    }
    
    /**
     *
     * Called when something updates state
     *
     *
     * @param b array of BeliefIDs that have changed.
     */
    public void stateChanged(BeliefID[] b) {
        for (BeliefID id: b) {
            Belief bel = state.getBelief(id);
            if (bel instanceof RSSIReading) {
		synchronized(rebuildBeliefs) {
		    rebuildBeliefs = true;
		    return;
		}
            }
        }
    }
}
