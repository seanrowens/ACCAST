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
 * RRTPlanner.java
 *
 * Created on March 15, 2006, 7:04 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.CostMaps.PathDisplayPanel;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.CostMaps.TiledCostMap;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.LinkedList;
import javax.swing.JFrame;

// /* DEBUG RRT graphing support
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import javax.swing.JPanel;


/**
 *
 * @author pscerri
 */
public class RRTPlanner3 {
    //    public final static String TERRAIN_COST_MAP_FILE = "/usr0/pscerri/Code/TerrainCM";
    public final static String TERRAIN_COST_MAP_FILE = "/afs/cs.cmu.edu/user/owens/TerrainCM";
    public final static int PRINT_EVERY_N_RUNS = 100;
    public final static int PLAN_RUNS = 100;
    public final static int NUM_NODES_PER_RUN = 10;
    public final static double PROB_PICK_FIRST_IN_QUEUE = 0.6;
    public final static int NUM_EXPANSIONS = 2;
    public final static int X_EXPANSION_RANGE = 5000;
    public final static int HALF_X_EXPANSION_RANGE = 2500;
    public final static int Y_EXPANSION_RANGE = 5000;
    public final static int HALF_Y_EXPANSION_RANGE = 2500;
    public final static int EXPAND_ALL_WHEN_SMALLER_THAN = 10;
    public final static double NEAR_PREVIOUS_LIMIT = 500.0;
    public final static double NEAR_PREVIOUS_PENALTY_MULT = 20.0;
    public final static double SMOOTH_PATH_A_CONST = 1.3;
    public final static double SMOOTH_PATH_B_CONST = 10.0;
    public final static double SMOOTH_PATH_C_CONST = 1.0;
    
    public final static boolean REMOVE_FROM_LIST = false;
    public final static boolean SPREAD_EARLY = true;
    
    
    public static Random rand = new Random();
    
    // Use these variables for environment size and graph scaling factor
    static public int size = 50000, step = 100;
    
    // initial location
    static int initx = size / 2;
    static int inity = size / 2;
    static int initz = 0;
    
    static PriorityQueue<Node> pq = null;
    static ArrayList<Node> list = null;
    
    static double pow1Counter = 0;
    static double pow3Counter = 0;
    static double sqrtCounter = 0;

    public static void main(String argv[]) {

        int planRuns = RRTPlanner3.PLAN_RUNS;

	if(argv.length > 0) {
	    planRuns = Integer.parseInt(argv[0]);
	    System.err.println("planRuns set to "+planRuns);
	}


        // initialize costmaps
        // FIMXE ctor init
        ArrayList<CostMap> cms = new ArrayList<CostMap>();
        
        // /* DEBUG add the test blocks to the costmap; range is to +/-51
        SimpleStaticCostMap m1 = new SimpleStaticCostMap();
        m1.addCostRect(new Rectangle(5000, 40000, 5000, 5000), -10.0);
        m1.addCostRect(new Rectangle(15000, 5000, 5000, 5000), -25.0);
        m1.addCostRect(new Rectangle(25000, 40000, 5000, 5000), -40.0);
        m1.addCostRect(new Rectangle(35000, 5000, 5000, 5000), -500.0);
        
        m1.addCostRect(new Rectangle(5000, 5000, 5000, 5000), 10.0);
        m1.addCostRect(new Rectangle(15000, 40000, 5000, 5000), 25.0);
        m1.addCostRect(new Rectangle(25000, 5000, 5000, 5000), 40.0);
        m1.addCostRect(new Rectangle(35000, 40000, 5000, 5000), 50.0);
        cms.add(m1);
        // FIXME */
        
        
        // add the terrain map
        // FIXME ctor/method
        try {
            TiledCostMap terrainCM = null;
            if (TERRAIN_COST_MAP_FILE != null) {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(TERRAIN_COST_MAP_FILE));
                terrainCM = (TiledCostMap)is.readObject();
                cms.add(terrainCM);
            } else {
                Machinetta.Debugger.debug("No terrain cost map location provided.", 3, "RRTPlanner3");
            }
        } catch (IOException ex) {
            Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, "RRTPlanner3");
        } catch (ClassNotFoundException ex) {
            Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, "RRTPlanner3");
        }
        
        
        // FIMXE
        // add the sweep map
        // cms.add(sweepCM);
        
        
        // plan the path
        Path3D path = plan(initx, inity, initz, System.currentTimeMillis(), cms, planRuns);
        
        
        // Display the costmap with path overlay
        PathDisplayPanel panel = new PathDisplayPanel(UAVRI.MAP_WIDTH_METERS,UAVRI.MAP_HEIGHT_METERS,cms, path);
        JFrame display = new JFrame("RRTPlanner3 Plan");
	display.setLocation(600,530);
        display.getContentPane().setLayout(new BorderLayout());
        display.getContentPane().add(panel, BorderLayout.CENTER);
        display.pack();
        display.setSize((size/step), (size/step));
        display.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        display.setVisible(true);
    }
    
    
    /** Creates a new instance of RRTPlanner3 */
    public RRTPlanner3() {
        
    }
    
    static class Node implements Comparable {
        
        int x, y, z;
        int expansions = 0;
        
        Node previous = null;
        
        double cost = 0.0;
        
        // This is the total distance of this path, to this node
        double dist = 0.0;

	private final static double ACONST1 = 1.01;
	private final static double BCONST1 = 2.0;
	static double EXPANSION_COST_TABLE[];
	static {
	    EXPANSION_COST_TABLE = new double[10000];
	    for(int loopi = 0; loopi < EXPANSION_COST_TABLE.length; loopi++) {
		EXPANSION_COST_TABLE[loopi] = Math.pow(ACONST1, BCONST1*(double)(loopi));
	    }
	}

        // Distances cause exponentially higher cost
        double compCost() {
            double ret = cost;
            //System.out.print("cost: w/o dist " + ret);
	    ret += Math.pow(ACONST1, dist/100.0);
	    pow1Counter++;
            //System.out.println("  w/ dist (" + dist + ")" + ret );
	    cachedCompCost = ret;
            return ret;
        }

	double cachedCompCost = 0.0;
        double cachedCompCost() { return cachedCompCost; }
        
        public int compareTo(Object o) {
            try {
// 		double otherValue = ((Node)o).compCost() + Math.pow(ACONST1, BCONST1*(double)(((Node)o).expansions));
// 		double thisValue = compCost() + Math.pow(ACONST1, BCONST1*(double)(expansions));
		double otherValue = ((Node)o).cachedCompCost() + EXPANSION_COST_TABLE[((Node)o).expansions];
		double thisValue = cachedCompCost() + EXPANSION_COST_TABLE[expansions];
                //System.out.println("For " + ((Node)o).compCost() + " and " + ((Node)o).expansions + " and " + dist + " get " + otherValue);
                
                if (otherValue > thisValue) return -1;
                else if (otherValue < thisValue) return 1;
                else return 0;
                
            } catch (ClassCastException e) {
                return 0;
            }
        }
    }
    
    static public Path3D plan(double currX, double currY, double currZ, long t,
            ArrayList<CostMap> costMaps) {
	return plan(currX, currY, currZ, t, costMaps, PLAN_RUNS);
    }
    /**
     *
     * Here is the main RRT planner
     *
     */
    static public Path3D plan(double currX, double currY, double currZ, long t,
            ArrayList<CostMap> costMaps, int planRuns) {
        
	int printEveryNRuns = PRINT_EVERY_N_RUNS;
	if(planRuns <= 10)
	    printEveryNRuns = 1;
	else if(planRuns <= 100)
	    printEveryNRuns = 10;
	else if(planRuns <= 1000)
	    printEveryNRuns = 100;

        pq = new PriorityQueue<Node>(1000);        
        Node init = new Node(), best = init;
        init.x = (int)currX;
        init.y = (int)currY;
        init.z = (int)currZ;
	Vector3D initV = new Vector3D(init.x, init.y, 0);
        pq.add(init);
        
        list = new ArrayList<Node>(1000);
        list.add(init);       
        
        ArrayList<Node> saveNodesList = new ArrayList<Node>(1000);

	int runCounter = 0;
	long startTime = System.currentTimeMillis();
	long lastPrintTime = System.currentTimeMillis();

        for (int loopi = 0; loopi < planRuns; loopi++) {
	    runCounter++;
	    if(0 == (runCounter % printEveryNRuns)) {
		long printTime = System.currentTimeMillis();
		long elapsedSincePrint = printTime - lastPrintTime;
		long elapsedSinceStart = printTime - startTime;
		long estimateRemaining = elapsedSincePrint * ((PLAN_RUNS - loopi)/printEveryNRuns);
                Machinetta.Debugger.debug("On run "+runCounter+", num nodes="+list.size()+", "+elapsedSincePrint+" since last print, "+elapsedSinceStart+" since start, est remaining="+estimateRemaining+", sqrts="+sqrtCounter+", pow counters = "+pow1Counter+", "+pow3Counter, 1, "RRTPlanner3");
	    }

            saveNodesList.clear();

	    int numNodesToExpand = NUM_NODES_PER_RUN;
	    boolean expandAll = false;
 	    if(pq.size() < EXPAND_ALL_WHEN_SMALLER_THAN) {
		expandAll = true;		
 		numNodesToExpand = list.size();
 	    }
            for (int loopj = 0; loopj < numNodesToExpand; loopj++) {

		if(list.size() <= 0)
		    continue;

		Node s = null;
		// Pick a node to expand, either a random one or the "best"
		if(expandAll) {
		    s = list.get(loopj);
		}
		else if(list.size() == 0 || rand.nextDouble() < PROB_PICK_FIRST_IN_QUEUE) {
		    s = pq.poll();
		}
		else {
		    s = list.get(rand.nextInt(list.size()));
		}
		if(null == s) 
		    continue;
		if(REMOVE_FROM_LIST)
		    list.remove(s);

            
		// System.out.println("Expanding node " + s.cost + " and " + s.expansions + " when best is " + best.cost);
            
		for (int loopk = 0; loopk < NUM_EXPANSIONS; loopk++) {
                
		    // Expand
		    Node e = new Node();
		    int randomx = (rand.nextInt(X_EXPANSION_RANGE) - HALF_X_EXPANSION_RANGE);
		    int randomy = (rand.nextInt(Y_EXPANSION_RANGE) - HALF_Y_EXPANSION_RANGE);
		    if(SPREAD_EARLY) {
			if(expandAll) {
			    Vector3D curV = new Vector3D(s.x,s.y,0);
			    Vector3D toVector = initV.toVector(curV);
			    toVector.normalize2d();
			    randomx += X_EXPANSION_RANGE*.3*toVector.x;
			    randomy += Y_EXPANSION_RANGE*.3*toVector.y;
// 			    int dx = s.x - init.x;
// 			    int dy = s.y - init.y;
			
// 			    if(((dx < 0) && (randomx > 0))
// 			       || ((dx > 0) && (randomx < 0)))
// 				randomx *= -1;
// 			    if(((dy < 0) && (randomy > 0))
// 			       || ((dy > 0) && (randomy < 0)))
// 				randomy *= -1;
			}
		    }

		    e.x = s.x + randomx;
		    if (e.x < 0) e.x = 0;
		    else if (e.x >= size) e.x = size - 1;
		    e.y = s.y + randomy;
		    if (e.y < 0) e.y = 0;
		    else if (e.y >= size) e.y = size - 1;
                
		    e.z = s.z;
		    e.dist = s.dist + dist(e, s);

		    e.previous = s;
                
		    s.expansions++;
                
		    //
		    // Calculate the new cost
		    //
		    // Cost of previous node
		    e.cost = s.cost;
                
		    // Penalty for being near any previous nodes on this path
		    // System.out.print("Impact of getting near: " + e.cost + " ");
		    Node prev = e.previous;
		    // But ignore distance to most recent node to allow detailed planning
		    if (prev != null) prev = prev.previous;
                
		    boolean nearPrev = false;
		    while (prev != null) {
			double dist = dist(prev, e);
			if (dist <= NEAR_PREVIOUS_LIMIT) {
			    e.cost += NEAR_PREVIOUS_PENALTY_MULT *(NEAR_PREVIOUS_LIMIT - dist);
			    nearPrev = true;
			}                    
			prev = prev.previous;
		    }
		    // System.out.println(" " + e.cost);
                
		    // Cost of any terrain covered
		    for (CostMap cm: costMaps) {
			// The 0s in this call are the time - we're ignoring time atm
			double c = cm.getCost(s.x, s.y, s.z, 0, e.x, e.y, e.z, 0);
			// if c>0 then it is a cost, if c<0 then it is a
			// bonus, but the path doesn't get the bonus if an
			// earlier point on the path was 'near' this
			// point.
			if (c > 0 || !nearPrev) e.cost += c;                    
		    }
                
		    // Bonus/penalty for straight/jagged movement
		    if (e.previous.previous != null) {
			Node sp = e.previous.previous;
                    
			// We're trying to penalize excess changes in
			// direction
			//
			// This mess of code is calculating the change in
			// x velocity, and change in y velocity from this
			// step vs the previous step, adding the two
			// together and then raising the sum to some
			// power.
			double dx1 = (s.x - sp.x)/dist(s,sp);
			double dy1 = (s.y - sp.y)/dist(s,sp);
			double dx2 = (e.x - s.x)/dist(e,s);
			double dy2 = (e.y - s.y)/dist(e,s);
                    
			double penalty = Math.pow(SMOOTH_PATH_A_CONST, SMOOTH_PATH_B_CONST*(Math.abs(dx1-dx2) + Math.abs(dy1-dy2))) - SMOOTH_PATH_C_CONST;
			pow3Counter++;
			e.cost += penalty; 
                    
			// System.out.println("Penalty for " + (Math.abs(dx1-dx2) + Math.abs(dy1-dy2)) + " was " + penalty);
		    }
		    e.compCost();
		    saveNodesList.add(e);
                
		    // See if it is the best
		    if (e.cachedCompCost() < best.cachedCompCost()) best = e;                
		}
		// Save original node for return to queue.
		if(REMOVE_FROM_LIST)
		    saveNodesList.add(s);
	    }
	    for (int loopk = 0; loopk < saveNodesList.size(); loopk++) {
		// Add it to the queue
		pq.add(saveNodesList.get(loopk));
		list.add(saveNodesList.get(loopk));
	    }
        }
            
        
        //DEBUG Display the RRT
        DebugRRTPanel panel = new DebugRRTPanel();
        JFrame display = new JFrame("RRTPlanner3 RRT");
	display.setLocation(5,530);
        display.getContentPane().setLayout(new BorderLayout());
        display.getContentPane().add(panel, BorderLayout.CENTER);
        display.pack();
        display.setSize((size/step), (size/step));
        display.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        display.setVisible(true);
        
        Path3D path = new Path3D();
        
        // Set the path
        Node n = best;
        do {
            path.addPoint(new Waypoint(n.x, n.y, n.z), 0);
            n = n.previous;
        } while (n != null);
        
        // Once debugging is over, make sure the PQ is cleared
        
        return path;
    }
    
    static double dist(Node a, Node b) {
	sqrtCounter++;
        return Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) + (a.z-b.z)*(a.z-b.z));
    }
    
    
    // /* DEBUG:  Draw an image of the RRT tree discovered during path search
    public static class DebugRRTPanel extends JPanel {
        
        /** Creates a new instance of DebugRRTPanel */
        public DebugRRTPanel() {}
        
        int nodeSize = 4, half = 2;
        
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            
            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(1));
            
            double dx = ((double)getWidth())/((double)size);
            double dy = ((double)getHeight())/((double)size);
            
            for (Node node: pq) {
                g2.fillOval((int)(dx * node.x) - half, (int)(dy * node.y) - half, nodeSize, nodeSize);
                if (node.previous != null) {
                    g2.drawLine((int)(dx * node.x), (int)(dy * node.y), (int)(dx * node.previous.x), (int)(dy * node.previous.y));
                }
            }
        }
    }
    // */
    
    
}
