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
 * BinaryBayesFilterCostMap.java
 *
 * Created on April 28, 2006, 9:51 AM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.SimTime;
import AirSim.Machinetta.BeliefShare;
import AirSim.Machinetta.MiniWorldState;
import Machinetta.ConfigReader;
import Gui.StringUtils;
import AirSim.Environment.Assets.Sensors.EmitterModel;
import AirSim.Environment.Assets.Emitter;
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

// @TODO: What we really need to do is factor this out into a "grid
// cost map" and then pass in some kind of "grid source" - i.e. the
// BinaryBayesFilter.

/**
 *
 * @author pscerri
 */
public class BinaryBayesFilterCostMap implements Runnable, CostMap, Dynamic, StateChangeListener {
    private final static DecimalFormat fmt = new DecimalFormat("0.000000");

    private double mapWidth;
    private double mapHeight;
    private MiniWorldState miniWorldState;
    private BBFMan bbfMan = null;
    public BBFMan getBBFMan() { return bbfMan; }

    private BBFTabPanel beliefPanel = null;
    private BBFTabPanel entropyPanel = null;
    private static Random rand = new Random();

    private BlockingQueue<RSSIReading> incomingReadingQueue = new LinkedBlockingQueue<RSSIReading>();
    private Thread myThread = null;

    /** Creates a new instance of BinaryBayesFilter */
    public BinaryBayesFilterCostMap(double mapWidth, double mapHeight, MiniWorldState miniWorldState, BBFTabPanel beliefPanel, BBFTabPanel entropyPanel, BinaryBayesFilter binaryBayesFilter) {
	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	this.miniWorldState = miniWorldState;
	this.beliefPanel = beliefPanel;
	this.entropyPanel = entropyPanel;
        Machinetta.Debugger.debug("Set mapWidth,mapHeight to "+mapWidth+", "+mapHeight,1,this);

	ArrayList<Point> emitters = new ArrayList<Point>();
	int emitterCount = ConfigReader.getConfigInt("EMITTER_COUNT", 0, false);
	for(int loopi = 0; loopi < emitterCount; loopi++) {
	    String fieldName = "EMITTER_"+loopi;
	    String emitterCoords = ConfigReader.getConfigString(fieldName, null, false);
	    if(null == emitterCoords)
		continue;
	    if(emitterCoords.equalsIgnoreCase("RANDOM"))
		continue;

	    double[] coords = StringUtils.parseDoubleList(2,emitterCoords);
	    if(null == coords)
		continue;
	    if(coords.length < 2)
		continue;
	    emitters.add(new Point((int)coords[0],(int)coords[1]));
	}
	miniWorldState.setEmitters(emitters);
        Machinetta.Debugger.debug("Found "+emitters.size()+" emitters in .cfg file.",1,this);

	bbfMan = new BBFMan(mapWidth, mapHeight, miniWorldState, emitters, beliefPanel, entropyPanel, binaryBayesFilter);

	int gridSize = (int)(mapWidth/UAVRI.BBF_GRID_SCALE_FACTOR);
	entropyGrid = new DoubleGrid(gridSize,gridSize, (int)UAVRI.BBF_GRID_SCALE_FACTOR);
	myThread = new Thread(this);
        state.addChangeListener(this);
    }

    public BinaryBayesFilterCostMap(double mapWidth, double mapHeight, MiniWorldState miniWorldState, BBFTabPanel beliefPanel, BBFTabPanel entropyPanel) {
	this(mapWidth,mapHeight,miniWorldState, beliefPanel, entropyPanel, null);
    }
    
    public void start() {
        Machinetta.Debugger.debug("Starting BinaryBayesFilterCostMap thread",1,this);
	myThread.start();
    }
    
    /**
     * Simple implementation
     */
    
    public boolean withinBounds(int x, int y){
        boolean validLocation = true;
        if(x>=mapWidth) validLocation = false;
        if(x<0) validLocation = false;
        if(y>=mapHeight) validLocation = false;
        if(y<0) validLocation = false;
        return validLocation;
    }
    
    DoubleGrid entropyGrid = null;
    Boolean rebuildEntropyGrid = true;
    
    public double getEntropyValue(int localx, int localy) {
	int gridx = (int)(localx/UAVRI.BBF_GRID_SCALE_FACTOR);
	int gridy = (int)(localy/UAVRI.BBF_GRID_SCALE_FACTOR);
	return entropyGrid.getValue(gridx,gridy); 
    }
    public DoubleGrid getEntropyGrid() { return entropyGrid; }

    public void getEntropyGrid(DoubleGrid eGrid) { bbfMan.getEntropyGrid(eGrid); }

    public double[][] getBeliefs() { return bbfMan.getBeliefs(); }

    public int getSize() { return bbfMan.getSize(); } 

    public void copyBeliefs(double[][] copy) {
	bbfMan.copyBeliefs(copy);
    }

    private void populateEntropyGrid(int gridSize) {
        Machinetta.Debugger.debug("populateEntropyGrid: gridSize="+gridSize, 0, this);
        long timeStart = System.currentTimeMillis();

	if(null == entropyGrid)
	    entropyGrid = new DoubleGrid(gridSize,gridSize, (int)UAVRI.BBF_GRID_SCALE_FACTOR);
	bbfMan.getEntropyGrid(entropyGrid);

        long timeEnd = System.currentTimeMillis();
        long timeElapsed = (timeEnd - timeStart);
        
        Machinetta.Debugger.debug("populateEntropyGrid: GridSize="+gridSize+", elapsed="+timeElapsed,1,this);
    }    
    
    private void printEntropyGrid() {
        double entropy=0.0;
	StringBuffer buf = new StringBuffer();
	double gridSize = entropyGrid.getWidth();
	for(double loopi = 0; loopi < gridSize; loopi += (gridSize/100)) {
	    for(double loopj = 0; loopj < gridSize; loopj += (gridSize/100)) {
		entropy = entropyGrid.getValue((int)loopi,(int)loopj);
		if(0.0 == entropy) {
		    buf.append(" ");
		}
		else if(entropy >= .0 && entropy < .1) {
		    buf.append("0");
		}
		else if(entropy >= .1 && entropy < .2) {
		    buf.append("1");
		}
		else if(entropy >= .2 && entropy < .3) {
		    buf.append("2");
		}
		else if(entropy >= .3 && entropy < .4) {
		    buf.append("3");
		}
		else if(entropy >= .4 && entropy < .5) {
		    buf.append("4");
		}
		else if(entropy >= .5 && entropy < .6) {
		    buf.append("5");
		}
		else if(entropy >= .6 && entropy < .7) {
		    buf.append("6");
		}
		else if(entropy >= .7 && entropy < .8) {
		    buf.append("7");
		}
		else if(entropy >= .8 && entropy < .9) {
		    buf.append("8");
		}
		else if(entropy >= .9 && entropy < .999) {
		    buf.append("9");
		}
		else if(entropy >= .999) {
		    buf.append("@");
		}
	    }
	    buf.append("\n");
	}
	Machinetta.Debugger.debug("Grid2=\n"+buf.toString(),1,this);
    }

    public void rebuild() {
	synchronized(rebuildEntropyGrid) {
	    if(rebuildEntropyGrid) {
		populateEntropyGrid((int)(mapWidth/UAVRI.BBF_GRID_SCALE_FACTOR));
		rebuildEntropyGrid = false;
	    }
	}
    }
    
    
    static private ProxyState state = new ProxyState();
    
    /**
     * Return the cost if a vehicle moves from (x1,y1,z1) at t1 to (x2,y2,z2) at t2
     */
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        
        // Paul reversed these
        y1 = UAVRI.MAP_HEIGHT_METERS - y1;
        y2 = UAVRI.MAP_HEIGHT_METERS - y2;
        
        double entropy = 0.0;
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
        
 	int gridx1 = entropyGrid.toGridX(x1);
 	int gridy1 = entropyGrid.toGridY(y1);
 	int gridx2 = entropyGrid.toGridX(x2);
 	int gridy2 = entropyGrid.toGridY(y2);
	if(!entropyGrid.insideGrid(gridx1, gridy1)
	   || !entropyGrid.insideGrid(gridx2, gridy2))
	    return Double.MAX_VALUE;

        double steps = 10;
        double ldx = (x2-x1)/steps;
        double ldy = (y2-y1)/steps;
        
        for (int i = 0; i < steps; i++) {
            x1 += ldx; y1 += ldy;
            int x = (int)Math.floor(x1);
            int y = (int)Math.floor(y1);
	    int gridx = entropyGrid.toGridX(x);
	    int gridy = entropyGrid.toGridY(y);
	    entropy += entropyGrid.getValue(gridx, gridy);
        }

	// Cost for a line segment should be between 0 and 50.
	// Negative cost is a reward, so reward should be between 0
	// and -50.
	// 
	// Any single entropy sample is between 0.0 and 1.0.
	//
	// 'steps' is 10
	// 
        // the summed entropy for a line segment is thus between 0 and
        // (steps * 1.0), i.e. 0 and 10;
	//
	// The higher the entropy, the more we want to explore that area.
	//
	// We want entropy=0.0 to result in cost = 50.0, and entropy =
	// 10.0 to result in cost = -50.0;
	//
	// So...
	double span = (1.0 * steps);
	double halfSpan = span/2;
	double costScaleFactor = 100.0/steps;
	//	cost =  (entropy - halfSpan) * costScaleFactor;
	// Change so cost is 0 to -100
	cost =  entropy * costScaleFactor;
        
        // Paul inverted this
        return -cost;
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1) at t1 to (x2,y2) at t2
     */
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        // This is where the entropy would need to be used to calculate a cost
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

	int numReadings = 0;
        for (BeliefID id: b) {
            Belief bel = state.getBelief(id);
            if (bel instanceof RSSIReading) {
		numReadings++;
		// TODO: Wed Jul 26 14:47:30 EDT 2006 SRO
		//
		// Might change this to use offer(obj, timeout)
		// instead.
		try {
		    incomingReadingQueue.put((RSSIReading)bel);
		}
		catch (InterruptedException e) {
		    
		}
            }
        }
	if(numReadings > 0)
	    Machinetta.Debugger.debug("Added "+numReadings+" RSSIReadings to incomingReadingQueue.",0,this);
    }

    public void run() {
	LinkedList<RSSIReading> beliefList = new LinkedList<RSSIReading>();

	while(true) {
	    beliefList.clear();
	    try {
		RSSIReading newReading = incomingReadingQueue.take();
		beliefList.add(newReading);
		incomingReadingQueue.drainTo(beliefList);	    
	    }
	    catch (InterruptedException e) {
		    
	    }

	    if(beliefList.size() > 10) 
	    Machinetta.Debugger.debug("simtime="+SimTime.getEstimatedTime()+" we see "+beliefList.size()+" readings", 1, this);
	    int numBeliefs = beliefList.size();
	    long startTime = System.currentTimeMillis();

	    boolean added = bbfMan.update(beliefList);

	    if (added) {
		synchronized(rebuildEntropyGrid) {
		    rebuildEntropyGrid = true;
		}
	    }
	    long endTime = System.currentTimeMillis();
	    long elapsed = endTime - startTime;
	    double avg = elapsed/numBeliefs;
	    Machinetta.Debugger.debug("Time to process "+numBeliefs+" readings = "+elapsed+" avg per reading="+fmt.format(avg),0,this);

	}
    }
}
