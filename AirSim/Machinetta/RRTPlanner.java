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
import AirSim.Machinetta.GA.*;

import AirSim.Machinetta.CostMaps.ParticleFilterCostMap;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import AirSim.Machinetta.CostMaps.Dynamic;
import AirSim.Machinetta.CostMaps.RandomGaussiansCostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.PathDisplayPanel;
import AirSim.Machinetta.CostMaps.OtherVehicleCostMap;
import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.UAVRI;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.ConfigReader;
import Machinetta.Configuration;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.RAPBelief;
import Gui.*;
//import com.sun.org.apache.xerces.internal.impl.dtd.models.CMStateSet;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.Point;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

// /* DEBUG RRT graphing support
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.*;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class RRTPlanner {

    private static int learningMode = 1; // 0: GA learning, 1: running with confs.
    private static int isDynamicConf = 0; // 0: static, 1: dynamic

    private static ArrayList<GAConf> configs = null;
    private static GAEnv env = null;
    private static Vehicle vehicle = null;
    private static boolean isGoal = false;
    private static GAConf currConfig = null;

    public final static boolean SYNCHRONOUS_REPAINT_ON_SETDATA = false;
    public static boolean SNAPSHOT_RRT_TREE_PANEL = false;
    private static boolean STANDALONE_TEST = false;

    // This coefficient is used to alter the expected speed of the
    // UAV.  Even though the UAV speed might be set to say 141.6 m/s,
    // in practice it runs slower than that, because of limitations on
    // turning, etc, when following a path, the autopilot, etc.  It
    // would be better if we'd just model the uav better, but for now
    // we fudge it with this.
    public final static double SPEED_MODIFIER = .999;

    //    public final static String TERRAIN_COST_MAP_FILE = "/usr0/pscerri/Code/TerrainCM";
    public final static String TERRAIN_COST_MAP_FILE = "/afs/cs.cmu.edu/user/owens/TerrainCM";

    private static PriorityQueue<Node> pq = null;
    private static ArrayList<Node> list = null;

    // For keeping away from other vehicles
    private static String proxyIdString = null;
    private static ProxyID proxyId = null;

    private static boolean configured = false;

    public static Random rand = new Random();

    /* =================== Path parameters */
    // Search parameters
    public static int EXPANSIONS = 5000;
    public final static int NUM_NODES_PER_RUN = 10;
    public final static double PROB_PICK_FIRST_IN_QUEUE = 1.0;
    public static int NUM_EXPANSIONS = 3;

    private static double HALF_RRT_BRANCH_RANGE_X_METERS = 0;
    private static double HALF_RRT_BRANCH_RANGE_Y_METERS = 0;
    private static double HALF_RRT_BRANCH_RANGE_HYPOT_METERS = 0;
    private static double NEAR_PREVIOUS_LIMIT = 2500.0; // same as l3com
    public final static double NEAR_PREVIOUS_PENALTY_MULT = 100.0; // same as l3com
    public final static double SMOOTH_PATH_A_CONST = 1.3;
    public final static double SMOOTH_PATH_B_CONST = 10.0;
    public final static double SMOOTH_PATH_C_CONST = 1.0;

    public final static double DEFAULT_PLAN_MAX_DIST_NONE = -1;
    public final static double DEFAULT_PLAN_MAX_DIST_SHORT_PATH = 500; // same as l3com
    public final static double DEFAULT_PLAN_MAX_DIST_LONG_PATH = 1000; // same as l3com
    public final static double DEFAULT_PLAN_MAX_DIST_LONGER_PATH = 1500; // same as l3com

    public final static double HEIGHT_CHANGE_PENALTY_FACTOR = 10.0;

    public static double MOMENTUM_REWARD = 1000.0;

    public static boolean SMART_EXPANSIONS = true;

    public static double MIN_PATH_LENGTH = 15000.0;

    // Cost parameters
    public static double EXPANSION_COST_FACTOR = 8.0;
    public static double TOO_DEEP_TOO_FAST_PENALTY = 3000.0;

    /* =================== Path parameters */
    /* =================== Debugging Parameters */
    private static JFrame rrtTreeFrame = null;
    public static RrtTreePanel rrtTreePanel = null;

    private static JFrame rrtPathFrame = null;
    private static PathDisplayPanel rrtPathPanel = null;

    // Use these variables for environment size and graph scaling factor
    public static int mapSize = 50000, mapStep = 100;

    // initial location
    private static int initx = 400; // size / 2;
    private static int inity = 31250; //size / 2;
    private static int initz = 100;

    private static boolean rrtPathPanelOn = true;

    public static boolean getRrtPathPanelOn() {
        return rrtPathPanelOn;
    }

    public static void setRrtPathPanelOn(boolean value) {
        rrtPathPanelOn = value;
    }
    private static int rrtPathPanelX = 10;

    public static int getRrtPathPanelX() {
        return rrtPathPanelX;
    }

    public static void setRrtPathPanelX(int value) {
        rrtPathPanelX = value;
    }
    private static int rrtPathPanelY = 10;

    public static int getRrtPathPanelY() {
        return rrtPathPanelY;
    }

    public static void setRrtPathPanelY(int value) {
        rrtPathPanelY = value;
    }

    private static boolean rrtTreePanelOn = true;

    public static boolean getRrtTreePanelOn() {
        return rrtTreePanelOn;
    }

    public static void setRrtTreePanelOn(boolean value) {
        rrtTreePanelOn = value;
    }
    private static int rrtTreePanelX = 510;

    public static int getRrtTreePanelX() {
        return rrtTreePanelX;
    }

    public static void setRrtTreePanelX(int value) {
        rrtTreePanelX = value;
    }
    private static int rrtTreePanelY = 10;

    public static int getRrtTreePanelY() {
        return rrtTreePanelY;
    }

    public static void setRrtTreePanelY(int value) {
        rrtTreePanelY = value;
    }

    private static ArrayList<Point> emitters = new ArrayList<Point>();

    public static int depthToPaint = 30;

    /* =================== End Debugging */
    public static void main(String argv[]) {
        Configuration config = new Configuration(argv[0]);

        STANDALONE_TEST = true;

        UAVRI.readConfigs();

        GAConf rrtConfig = new GAConf();
        if (UAVRI.MAP_WIDTH_METERS <= 1000) {
            rrtConfig.NO_EXPANSIONS = 4749;
            rrtConfig.BRANCHES_PER_EXPANSION = 3;
            rrtConfig.MAX_THETA_CHANGE = 71.73;
            rrtConfig.MAX_PSI_CHANGE = 5.34;
            rrtConfig.MAX_BRANCH_LENGTH = .06;
            rrtConfig.MIN_BRANCH_LENGTH = .01;
            //rrtConfig.RRT_BRANCH_RANGE_X_METERS = 113;
            //rrtConfig.RRT_BRANCH_RANGE_Y_METERS = 113;
        } else {
            rrtConfig.NO_EXPANSIONS = 5000;
            rrtConfig.BRANCHES_PER_EXPANSION = 3;
            rrtConfig.MAX_THETA_CHANGE = 90;
            rrtConfig.MAX_PSI_CHANGE = 1;
            rrtConfig.MAX_BRANCH_LENGTH = 50.0 / 1000.0;
            rrtConfig.MIN_BRANCH_LENGTH = 0;
            //rrtConfig.RRT_BRANCH_RANGE_X_METERS = 5000;
            //rrtConfig.RRT_BRANCH_RANGE_Y_METERS = 5000;
        }
        ArrayList<GAConf> rrtConfigList = new ArrayList<GAConf>();
        rrtConfigList.add(rrtConfig);
        RRTPlanner.setConfigs(rrtConfigList);
        GAEnv gaenv = new GAEnv();
        gaenv.xsize = UAVRI.MAP_WIDTH_METERS;
        gaenv.ysize = UAVRI.MAP_HEIGHT_METERS;
        gaenv.zsize = 2000;
        RRTPlanner.setEnv(gaenv);
        Vehicle gavehicle = new Vehicle("UAV",
                UAVRI.UAV_SPEED_METERS_PER_SEC,
                UAVRI.UAV_MAX_TURN_RATE_DEG, 10);
        RRTPlanner.setVehicle(gavehicle);

        UAVRI.RANDOM_ENTIRE_MAP_COSTMAP_ON = true;
        UAVRI.RANDOM_SMALL_MOVES_COSTMAP_ON = false;
        UAVRI.RANDOM_CLUSTERS_COSTMAP_ON = false;

        configure();
        rrtPathPanelOn = true;
        rrtTreePanelOn = true;

        SimpleStaticCostMap edgeCM = new SimpleStaticCostMap();
        ArrayList<CostMap> cms = new ArrayList<CostMap>();

        // plan the path
        Path3D path = null;

        initx = (int) (UAVRI.MAP_WIDTH_METERS) / 2;
        inity = (int) (UAVRI.MAP_HEIGHT_METERS) / 2;

        initz = 200;

        MixGaussiansCostMap randomGaussianCM2 = null;
        randomGaussianCM2 = new MixGaussiansCostMap();
        cms.add(randomGaussianCM2);

        double scaleFactor = UAVRI.MAP_WIDTH_METERS / 1000;

        SimpleStaticCostMap rectCM = new SimpleStaticCostMap();
        rectCM.addCostRect(new Rectangle(300, 300, 400, 400), -60);
        rectCM.addCostRect(new Rectangle(400, 400, 200, 200), -70);
        rectCM.addCostRect(new Rectangle(450, 450, 100, 100), -100);
        rectCM.addCostRect(new Rectangle(475, 475, 50, 50), -125);
        rectCM.addCostRect(new Rectangle(490, 490, 20, 20), -150);

        cms.add(rectCM);

        initx = rand.nextInt((int) UAVRI.MAP_WIDTH_METERS);
        inity = rand.nextInt((int) UAVRI.MAP_HEIGHT_METERS);


        /* This "just" repeats a bunch of times. */
        Machinetta.Debugger.debug(1, "Entering for loop to call plan.");
        double shortCount = 0;
        for (int loopi = 0; loopi < 1000; loopi++) {

            if ((loopi % 20) == 0) {
                initx = rand.nextInt((int) UAVRI.MAP_WIDTH_METERS);
                inity = rand.nextInt((int) UAVRI.MAP_HEIGHT_METERS);
                initz = 1839;

            }

            Machinetta.Debugger.debug(1, "Calling plan.");
            long startTime = System.currentTimeMillis();
            path = plan(initx, inity, initz, System.currentTimeMillis(), cms, UAVRI.RRT_MAX_PATH_METERS, UAVRI.UAV_SPEED_METERS_PER_SEC);
            long endTime = System.currentTimeMillis();
            Machinetta.Debugger.debug("plan elapsed time=" + (endTime - startTime), 1, "RRTPlanner");

            long duration = path.getDuration();
            double length = path.getLength();
            int size = path.size();
            Machinetta.Debugger.debug("path " + loopi + " size=" + size + ", duration=" + duration + ", length=" + length + ", path = " + path, 1, "RRTPlanner");

            boolean isShort = false;
            if (duration < 5000) {
                Machinetta.Debugger.debug("SHORT PATH duration = " + duration, 1, "RRTPlanner");
                isShort = true;
            }
            if (length < UAVRI.PATHPLANNER_MIN_PATH_LENGTH) {
                Machinetta.Debugger.debug("SHORT PATH length = " + length, 1, "RRTPlanner");
                isShort = true;
            }
            if (size < 3) {
                Machinetta.Debugger.debug("SHORT PATH size = " + size, 1, "RRTPlanner");
                isShort = true;
            }
            if (isShort) {
                shortCount++;
            }
            Machinetta.Debugger.debug("SHORTPERCENT for " + loopi + " paths is " + (shortCount / (loopi + 1)) * 100, 1, "RRTPlanner");

            // Machinetta.Debugger.debug("Initial point "+initx+", "+inity+", length " + path.getRemainingDist(initx, inity, initz) + ", path planned="+path, 1, "RRTPlanner");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            };

            Waypoint end = path.getEndPoint();
            initx = (int) end.x;
            inity = (int) end.y;
        }

        // Machinetta.Debugger.debug("Plan length: " + path.getRemainingDist(initx, inity, initz) + " is: " + path, 1, "RRTPlanner");
    }

    private static long calcTimeMs(double dist, double speedMetersPerSec) {
        return (long) (dist / ((speedMetersPerSec / 1000) * SPEED_MODIFIER));
    }

    public static void recalculateTimes(double currX, double currY, double currZ, Path3D path, long startTime, double speedMetersPerSec) {
        Waypoint[] wps = path.getWaypointsAry();
        double dist;
        double lastx = currX;
        double lasty = currY;
        double lastz = currZ;
        long lastTime = startTime;
        for (int loopi = 0; loopi < wps.length; loopi++) {
            dist = dist(lastx, lasty, lastz, wps[loopi].x, wps[loopi].y, wps[loopi].z);
            wps[loopi].time = lastTime + calcTimeMs(dist, speedMetersPerSec);
            lastx = wps[loopi].x;
            lasty = wps[loopi].y;
            lastz = wps[loopi].z;
            lastTime = wps[loopi].time;
        }
    }

    /**
     * Search node
     */
    static class Node implements Comparable {

        static int count = 0;

        int x, y, z;
        long timeMs;
        int depth = 0;
        private int expansions = 0;
        private int treeExpansions;

        Node previous = null;

        double cost = 0.0;
        double cachedCompCost = 0.0;

        private double theta = 0.0;
        private double psi = 0.0;

        // This is the total distance of this path, to this node
        double dist = 0.0;

        private static double DIST_COST_EXPONENTIAL = 1.02;
        // shorter paths
        //	private final static double DIST_COST_EXPONENTIAL = 1.08;
        private static double DIST_COST_EXPONENT = 2.0;

        public static double[] EXPANSION_COST_TABLE = null;

        // public static void reinitializeExpansionCostTable()
        static {
            EXPANSION_COST_TABLE = new double[10000];
            for (int loopi = 0; loopi < EXPANSION_COST_TABLE.length; loopi++) {
                EXPANSION_COST_TABLE[loopi] = Math.pow(EXPANSION_COST_FACTOR, 1 * (double) (loopi));
                Machinetta.Debugger.debug("Set : " + loopi + " to " + EXPANSION_COST_TABLE[loopi], 0, "RRTPlanner.Node");
            }
        }

        public Node() {
            count++;
        }

        void calcTime(double speedMetersPerSec) {
            timeMs = 0;
            if (-1 == speedMetersPerSec) {
                return;
            }
            if (null == previous) {
                return;
            }
            double distThisNode = dist - previous.dist;

            timeMs = previous.timeMs + RRTPlanner.calcTimeMs(distThisNode, speedMetersPerSec);
        }

        public int compareTo(Object o) {
            try {
                double otherValue = 0, thisValue = 0;

                Node other = (Node) o;

                // Basic node cost
                otherValue = 1.5 * (other.cost / (1.0 + other.depth / 2.0));
                thisValue = 1.5 * (cost / (1.0 + depth / 2.0));

                /*otherValue = other.cost;
                 thisValue = cost;
                 */
                // Cost for expansions of this node
                try {
                    // otherValue += EXPANSION_COST_TABLE[other.getExpansions() + other.getTreeExpansions()/5];
                    otherValue = ((Node) o).cost + EXPANSION_COST_TABLE[((Node) o).getExpansions()];
                } catch (ArrayIndexOutOfBoundsException e) {
                    otherValue = Double.MAX_VALUE;
                }
                try {
                    // thisValue += EXPANSION_COST_TABLE[getExpansions() + getTreeExpansions()/5];
                    thisValue = cost + EXPANSION_COST_TABLE[getExpansions()];
                    // System.out.println("Expansion cost: " + EXPANSION_COST_TABLE[getExpansions()] + " for " + getExpansions());
                } catch (ArrayIndexOutOfBoundsException e) {
                    otherValue = Double.MAX_VALUE;
                }

                //System.out.println("For " + ((Node)o).compCost() + " and " + ((Node)o).expansions + " and " + dist + " get " + otherValue);
                // If you are onto something good, go with it
                if (other.previous != null && previous != null) {
                    double otherImprove = other.cost / other.previous.cost;
                    if (other.cost < 0) {
                        if (otherImprove > 1.1) {
                            otherValue -= MOMENTUM_REWARD;
                        } else if (otherValue < 0.9) {
                            otherValue += MOMENTUM_REWARD;
                        }
                    } else {
                        if (otherImprove > 1.1) {
                            otherValue += MOMENTUM_REWARD;
                        } else if (otherValue < 0.9) {
                            otherValue -= MOMENTUM_REWARD;
                        }
                    }

                    double improve = cost / previous.cost;
                    if (cost < 0) {
                        if (improve > 1.1) {
                            thisValue -= MOMENTUM_REWARD;
                        } else if (improve < 0.9) {
                            thisValue += MOMENTUM_REWARD;
                        }
                    } else {
                        if (improve > 1.1) {
                            thisValue += MOMENTUM_REWARD;
                        } else if (improve < 0.9) {
                            thisValue -= MOMENTUM_REWARD;
                        }
                    }
                }

                // Penalty for going too deep too early
                if (Node.count < 3000) {
                    if (Math.pow(4.0, depth) > Node.count) {
                        thisValue += TOO_DEEP_TOO_FAST_PENALTY;
                    }
                    if (Math.pow(3.0, other.depth) > Node.count) {
                        otherValue += TOO_DEEP_TOO_FAST_PENALTY;
                    }
                }

                // Return
                if (otherValue > thisValue) {
                    return -1;
                } else if (otherValue < thisValue) {
                    return 1;
                } else {
                    return 0;
                }

            } catch (ClassCastException e) {
                return 0;
            }
        }

        public int getExpansions() {
            return expansions;
        }

        public void incExpansions(int inc) {
            if (previous != null) {
                incTreeExpansions(inc);
            }
            expansions++;
        }

        public int getTreeExpansions() {
            return treeExpansions;
        }

        public void incTreeExpansions(int inc) {
            treeExpansions += inc;
            if (previous != null) {
                previous.incTreeExpansions(inc);
            }
        }

    }

    /**
     * Helper function for proper "plan"
     */
    static public Path3D plan(double currX, double currY, double currZ, long t,
            ArrayList<CostMap> costMaps) {

        return plan(currX, currY, currZ, t, costMaps, DEFAULT_PLAN_MAX_DIST_NONE, -1);
    }

    private static Node init = null;

    private static double degreeToRad(double degree) {
        return degree / 360.0 * 2.0 * Math.PI;
    }

    public static void setConfigs(ArrayList<GAConf> c) {
        configs = c;
        currConfig = configs.get(0);

    }

    public static void setEnv(GAEnv e) {
        env = e;
    }

    public static void setVehicle(Vehicle v) {
        vehicle = v;
    }

    private static void generateConfSet() {
        //@once we find some configuraion value...

        //@NOTE: set the general environment parameters. we can 
        env = new GAEnv();
        env.xsize = UAVRI.MAP_WIDTH_METERS;
        env.ysize = UAVRI.MAP_HEIGHT_METERS;
        env.zsize = 2000;

        //@NOTE: in this part, you need to learn the parameters first, and set the best solutions here.
        //For test, I just set 5 random configurations.
        configs = new ArrayList<GAConf>();
        configs.add(new GAConf());
        configs.add(new GAConf());
        configs.add(new GAConf());
        configs.add(new GAConf());
        configs.add(new GAConf());

        //@NOTE: we can set any vehicle types here. (or several vehicles, too.)
        vehicle = new Vehicle();

        // end
    }

    static private Path3D getBestPath(double currX, double currY, double currZ, long pathStartTime,
            ArrayList<CostMap> costMaps, double maxDist, double speed) {

        long t0 = System.currentTimeMillis();

        boolean useZ = true;
        if (currZ == 0) {
            // Assume this means it can never go in the air, might do something smarter ..
            useZ = false;
        }

        if (learningMode == 1) {
            lookupProxyId();
        }

        for (CostMap cm : costMaps) {
            if (cm instanceof AirSim.Machinetta.CostMaps.Dynamic) {
                ((Dynamic) cm).rebuild();
            }
        }

        Node.count = 0;
        init = new Node();
        Node best = init;
        init.x = (int) currX;
        init.y = (int) currY;
        init.z = (int) currZ;
        init.theta = 0.0;
        init.psi = 0.0;
        init.timeMs = pathStartTime;

        pq = new PriorityQueue<Node>(1000);
        pq.add(init);

        // int [] depthCounts = new int[100];
        int maxDepth = 0;

        // ANOTHER PAUL CHEAT
        System.out.println("PAUL CHEATING AND OVERRIDING PARAMS IN RRTPlanner");
        currConfig.MIN_BRANCH_LENGTH = 0.025;
        currConfig.MAX_BRANCH_LENGTH = 0.05;
        currConfig.NO_EXPANSIONS = 5000;
        UAVRI.RRT_PREFERRED_PATH_METERS = 10000.0;
        UAVRI.RRT_MAX_PATH_METERS = 20000.0;
        SMART_EXPANSIONS = true;
        useZ = false;

        //System.out.println("Setup : " + currConfig.MAX_BRANCH_LENGTH + " " + currConfig.MIN_BRANCH_LENGTH + " " + currConfig.NO_EXPANSIONS + " " + UAVRI.RRT_PREFERRED_PATH_METERS);

        for (int loopi = 0; loopi < currConfig.NO_EXPANSIONS; loopi++) {
            Thread.yield();

            Node start = null;
            // Pick a node to expand
            start = pq.poll();

            //System.out.println("Expanding node " + start.cost + "( " + start.dist + ") and " + start.expansions + " when best is " + best.cost + " (" + best.dist + ")  ");
            // modify NUM_EXPAN using GAConf.
            for (int loopk = 0; loopk < currConfig.BRANCHES_PER_EXPANSION; loopk++) {
                Node expand = new Node();

                double theta = 0.0;
                double psi = 0.0;

                double distX = env.xsize * (rand.nextDouble() * (currConfig.MAX_BRANCH_LENGTH - currConfig.MIN_BRANCH_LENGTH) + currConfig.MIN_BRANCH_LENGTH);
                double distY = env.ysize * (rand.nextDouble() * (currConfig.MAX_BRANCH_LENGTH - currConfig.MIN_BRANCH_LENGTH) + currConfig.MIN_BRANCH_LENGTH);

                // To avoid exceptions calling rand.nextInt().
                if (distX <= 1) {
                    distX = 1;
                }
                if (distY <= 1) {
                    distY = 1;
                }

                // Expand
                if (start.previous == null || !SMART_EXPANSIONS) {
                    expand.x = start.x + (rand.nextInt((int) (2.0 * distX)) - (int) (distX));
                    if (expand.x < 0) {
                        expand.x = 0;
                    } else if (expand.x >= mapSize) {                        
                        expand.x = mapSize - 1;
                    }
                    expand.y = start.y + (rand.nextInt((int) (2.0 * distY)) - (int) (distY));
                    if (expand.y < 0) {
                        expand.y = 0;
                    } else if (expand.y >= mapSize) {
                        expand.y = mapSize - 1;
                    }

                    //@NOTE: not really good.
                    expand.theta = start.theta;
                    expand.psi = start.psi;

                } else {
                    double dx = start.x - start.previous.x;
                    double dy = start.y - start.previous.y;
                    double dz = start.z - start.previous.z;

                    //@NOTE: need to modify or add more? (considering various types of vehicles)
                    if (vehicle.type != "Aircraft") {
                        theta = Math.atan2(dy, dx) + (rand.nextDouble() * degreeToRad(currConfig.MAX_THETA_CHANGE)) - (degreeToRad(currConfig.MAX_THETA_CHANGE / 2.0));
                        psi = Math.atan2(dz, dx) + (rand.nextDouble() * degreeToRad(currConfig.MAX_PSI_CHANGE)) - (degreeToRad(currConfig.MAX_PSI_CHANGE / 2.0));
                    } else {
                        theta = start.theta + (degreeToRad(vehicle.MAX_THETA_CHANGE) * rand.nextDouble());
                        psi = start.psi + (degreeToRad(vehicle.MAX_PSI_CHANGE) * rand.nextDouble());
                    }

                    // is there any other ways to consider speed of vehicle?
                    // just use one of the random dist variables as an edge length (distX in this case
                    expand.x = start.x + (int) (distX * Math.cos((theta)));
                    expand.y = start.y + (int) (distX * Math.sin((theta)));

                    expand.theta = theta;
                    expand.psi = psi;
                }

                // or if(vehicle.type == "Aircraft")
                if (useZ) {
                    double distZ = env.zsize * (rand.nextDouble() * (currConfig.MAX_BRANCH_LENGTH - currConfig.MIN_BRANCH_LENGTH) + currConfig.MIN_BRANCH_LENGTH);
                    expand.z = start.z + (int) (distZ * Math.cos(psi));

                    if (expand.z < 0) {
                        expand.z = 0;
                    }
                } else {
                    expand.z = start.z;
                }

                double expansionDistance = dist(start, expand);
                if (expansionDistance < 2.0 * distX / 5) {
                    loopk--;
                    Node.count--;                    
                    continue;
                }

                expand.dist = start.dist + expansionDistance;
                expand.previous = start;
                expand.depth = start.depth + 1;
                expand.calcTime(speed);
                start.incExpansions(1);

                /* if (expand.depth < depthCounts.length) {
                 depthCounts[expand.depth]++;
                 } */
                if (expand.depth > maxDepth) {
                    maxDepth = expand.depth;
                }

                //
                // Calculate the new cost
                //
                // Cost of previous node
                expand.cost = start.cost;
                //System.out.print(expand.cost + "-1\t");
                // Penalty for being near any previous nodes on this path
                double nearCost = NEAR_PREVIOUS_PENALTY_MULT * costNear(expand);
                expand.cost += nearCost;
                //System.out.print(expand.cost + "-2\t");
                boolean nearPrev = nearCost > 10.0;

                double hypoDist = Math.sqrt((distX * distX) + (distY * distY));

                // Cost of any terrain covered
                for (CostMap cm : costMaps) {

                    double c = cm.getCost(start.x, start.y, start.z, start.timeMs, expand.x, expand.y, expand.z, expand.timeMs) * (expansionDistance / hypoDist);

                    // if c>0 then it is a cost, if c<0 then it is a
                    // bonus, but the path doesn't get the bonus if an
                    // earlier point on the path was 'near' this
                    // point.
                    if (c > 0 || !nearPrev) {
                        expand.cost += c;
                    }
                    
                    // System.out.print(expand.cost + "-3\t");
                }

                // Bonus/penalty for straight/jagged movement
                if (expand.previous.previous != null) {
                    Node startPrev = expand.previous.previous;

                    // We're trying to penalize excess changes in
                    // direction
                    //
                    // This is calculating the change in
                    // x velocity, and change in y velocity from this
                    // step vs the previous step, adding the two
                    // together and then raising the sum to some
                    // power.
                    double distStartStartPrev = dist(start, startPrev);
                    double distExpandStart = dist(expand, start);
                    double dx1 = (start.x - startPrev.x) / distStartStartPrev;
                    double dy1 = (start.y - startPrev.y) / distStartStartPrev;
                    double dz1 = (start.z - startPrev.z) / distStartStartPrev;
                    double dx2 = (expand.x - start.x) / distExpandStart;
                    double dy2 = (expand.y - start.y) / distExpandStart;
                    double dz2 = (expand.z - start.z) / distExpandStart;

                    double penalty = Math.pow(SMOOTH_PATH_A_CONST, SMOOTH_PATH_B_CONST * (Math.abs(dx1 - dx2) + Math.abs(dy1 - dy2) + Math.abs(dz1 - dz2))) - SMOOTH_PATH_C_CONST;
                // expand.cost += penalty;

                    // if (loopk == 0 && loopi % 10 == 0) System.out.println("Penalty for " + (Math.abs(dx1-dx2) + Math.abs(dy1-dy2)) + " was " + penalty + " in cost of " + expand.cost + ", prev " + start.cost);
                }

                // There is assumed to be an inherent cost for going either up or down
                expand.cost += HEIGHT_CHANGE_PENALTY_FACTOR * Math.abs(expand.z - start.z);
                // System.out.print(expand.cost + "-4\t");
                // The idea here is to really punish nodes that are too long
                // and promote length otherwise
                if (expand.dist > UAVRI.RRT_PREFERRED_PATH_METERS) {
                    expand.cost += Math.pow(2.0, ((expand.dist - UAVRI.RRT_PREFERRED_PATH_METERS) / 100.0));                    
                } else {
                    // expand.cost += -Math.pow(1.02, expand.dist/1000.0);
                    // expand.cost += 0.05 * (1.0 - expand.dist/UAVRI.RRT_PREFERRED_PATH_METERS);//*(expand.dist - start.dist);
                    // System.out.println("Penalty: " + (0.05 * (1.0 - expand.dist/UAVRI.RRT_PREFERRED_PATH_METERS))); // *(expand.dist - start.dist)));
                }
                // System.out.println(expand.cost + "-5");
                /*
                if (expand.cost < start.cost)
                    System.out.println("IMPORTANT " + start.cost + "\t->\t" + expand.cost);
                else 
                    //System.out.print(".");
                */
                
                pq.offer(expand);

                // See if it is the best
                if (expand.dist > MIN_PATH_LENGTH && (expand.cost < best.cost || best.dist < MIN_PATH_LENGTH)) {
                    best = expand;
                }
            }

            pq.offer(start);
        }

        Path3D path = new Path3D();

        if (learningMode == 1) {
            lookupProxyId();
            path.setAssetID(proxyId);
        }

        // Set the path
        Node n = best;
        path.setScore(-n.cost);

        double totalDist = best.dist;
        do {
            // if maxDist is set to < 0 then that means we don't care
            // about maxDist, if maxDist >= 0 then we want to truncate
            // the path to maxDist.
            if ((maxDist < 0) || (totalDist < maxDist)) {
                path.addPoint(new Waypoint(n.x, n.y, n.z, n.timeMs), 0);
            } else {
                if (null != n.previous) {
                    totalDist -= dist(n, n.previous);
                }
            }
            n = n.previous;
        } while (n != null);

        // The first point on the path should correspond to the last point on the previous path, so we
        // actually dump it
        if (path.getSize() > 1) {
            path.removeFirst();
        }

        long t1 = System.currentTimeMillis();
        //System.out.println("Elapsed time: " + (t1-t0)/1000.0 + "sec");                

        return path;
    }

    /**
     *
     * Here is the main RRT planner
     *
     */
    private static boolean printParams = true;

    static public Path3D plan(double currX, double currY, double currZ, long pathStartTime,
            ArrayList<CostMap> costMaps, double maxDist, double speed) {

        Path3D bestPath = new Path3D();
        bestPath.setScore(-999999999.0);

        if (!configured) {
            configure();
        }

        if (learningMode == 1) {
            generateConfSet();
        }

        if (isDynamicConf == 1) {

            if (!configured) {
                configure();
            }

            for (int l = 0; l < configs.size(); l++) {
                currConfig = configs.get(l);

                Path3D path = getBestPath(currX, currY, currZ, pathStartTime, costMaps, maxDist, speed);

                // HERE is where we used to update the BinaryBayesDisplay -
                // rebuilding the BBFCM entropy grid is expensive.  (Or it
                // used to be, it's been speeded up a fair bit but it's still
                // kinda expensive, on the order of 50 or 60ms) We don't want
                // to rebuild it every time we update the filter, just when
                // we're going to use the entropy map to plan a path.  So,
                // when we plan a path we want to rebuild entropy and costs
                // and then update the display...
                // @todo Once debugging is over, make sure the PQ is cleared
                //System.out.println("path score: " + path.getScore());
                if (path.getScore() > bestPath.getScore()) {
                    bestPath = path;
                }

            } //end of for
        } else {
            currConfig = configs.get(0);

            Path3D path = getBestPath(currX, currY, currZ, pathStartTime, costMaps, maxDist, speed);

            // HERE is where we used to update the BinaryBayesDisplay -
            // rebuilding the BBFCM entropy grid is expensive.  (Or it
            // used to be, it's been speeded up a fair bit but it's still
            // kinda expensive, on the order of 50 or 60ms) We don't want
            // to rebuild it every time we update the filter, just when
            // we're going to use the entropy map to plan a path.  So,
            // when we plan a path we want to rebuild entropy and costs
            // and then update the display...
            // @todo Once debugging is over, make sure the PQ is cleared
            //System.out.println("path score: " + path.getScore());
            bestPath = path;
        }

        updateRRTPanels(currX, currY, costMaps, bestPath, pq);

        return bestPath;
    }

    /* Simple Euclidean distance */
    static double dist(Node a, Node b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z) * (a.z - b.z));
    }

    static double dist(double startx, double starty, double startz, double endx, double endy, double endz) {
        return Math.sqrt((startx - endx) * (startx - endx) + (starty - endy) * (starty - endy) + (startz - endz) * (startz - endz));
    }

    /**
     * The concept behind this function is that if the distance between the
     * current node and previous ones is close to that of the path distance then
     * the path can't be turning on itself too much.
     */
    private static double costNear(Node e) {

        Node prev = e.previous;
        if (prev == null || prev.previous == null) {
            return 0.0;
        }

        prev = prev.previous;
        double smallestRatio = 1.0;
        double culmDist = 0.0, crowsDist = 0.0;

        while (prev != null) {

            double pathDist = e.dist - prev.dist;
            double crowsFlyDist = dist(e, prev);

            double ratio = crowsFlyDist / pathDist;

            smallestRatio = Math.min(ratio, smallestRatio);

            prev = prev.previous;
        }

        return 1.0 - smallestRatio;
    }

    /**
     * This is just for testing.
     */
    private void addOtherVehiclePaths(OtherVehicleCostMap otherVehicleCostMap, ArrayList<CostMap> cms) {
        Path3D path = null;
        int counter = 0;
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);
        int initx = rand.nextInt(50000);
        int inity = rand.nextInt(50000);
        int initz = 1000;
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 10000, -1);
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);
        initx = rand.nextInt(50000);
        inity = rand.nextInt(50000);
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 10000, -1);
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);
        initx = rand.nextInt(50000);
        inity = rand.nextInt(50000);
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 10000, -1);
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);
        initx = rand.nextInt(50000);
        inity = rand.nextInt(50000);
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 10000, -1);
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);
        initx = rand.nextInt(50000);
        inity = rand.nextInt(50000);
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 10000, -1);
        path.setAssetID(new NamedProxyID("path" + counter++));
        otherVehicleCostMap.addPath(path);

        initx = 25000;
        inity = 25000;
        path = plan(initx, inity, initz, System.currentTimeMillis(), cms, 40000, -1);
    }

    /**
     * This is the old version, not used any more. *
     */
    /*
     private static double costNearOld(Node e) {
     // Penalty for being near any previous nodes on this path
     // System.out.print("Impact of getting near: " + e.cost + " ");
     Node prev = e.previous;
     // But ignore distance to most recent node to allow detailed planning
     if (prev != null) prev = prev.previous;
        
     double cost = 0.0;
        
     boolean nearPrev = false;
     while (prev != null) {
     double dist = dist(prev, e);
     if (dist <= NEAR_PREVIOUS_LIMIT) {
     cost += NEAR_PREVIOUS_PENALTY_MULT *(NEAR_PREVIOUS_LIMIT - dist);
     nearPrev = true;
     }
     prev = prev.previous;
     }
        
     return cost;
     }*/
    private static void configure() {
        if (learningMode == 1) {
            lookupProxyId();
        }

        mapSize = UAVRI.MAP_WIDTH_METERS;
        //        mapStep = 2;
        mapStep = (int) Math.max(1.0, (UAVRI.BBF_GRID_SCALE_FACTOR * 2.0));
        Machinetta.Debugger.debug("mapStep set to : " + mapStep + " UAVRI.BBF_GRID_SCALE_FACTOR " + UAVRI.BBF_GRID_SCALE_FACTOR, 1, "RRTPlanner");

        HALF_RRT_BRANCH_RANGE_X_METERS = UAVRI.RRT_BRANCH_RANGE_X_METERS / 2;
        HALF_RRT_BRANCH_RANGE_Y_METERS = UAVRI.RRT_BRANCH_RANGE_Y_METERS / 2;
        HALF_RRT_BRANCH_RANGE_HYPOT_METERS = Math.sqrt((HALF_RRT_BRANCH_RANGE_X_METERS * HALF_RRT_BRANCH_RANGE_X_METERS) + (HALF_RRT_BRANCH_RANGE_Y_METERS * HALF_RRT_BRANCH_RANGE_Y_METERS));
//dbr   NEAR_PREVIOUS_LIMIT = UAVRI.RRT_BRANCH_RANGE_Y_METERS/10;
        NEAR_PREVIOUS_LIMIT = UAVRI.RRT_BRANCH_RANGE_Y_METERS / 2;

        UAVRI.BBF_GRID_SCALE_FACTOR = ConfigReader.getConfigDouble("BBF_GRID_SCALE_FACTOR", 200.0, false);
        EXPANSION_COST_FACTOR = ConfigReader.getConfigDouble("EXPANSION_COST_FACTOR", 8.0, false);
        EXPANSIONS = ConfigReader.getConfigInt("EXPANSIONS", 5000, false);
        NUM_EXPANSIONS = ConfigReader.getConfigInt("NUM_EXPANSIONS", 3, false);
        MOMENTUM_REWARD = ConfigReader.getConfigDouble("MOMENTUM_REWARD", 1000.0, false);
        MIN_PATH_LENGTH = ConfigReader.getConfigDouble("MIN_PATH_LENGTH", 100.0, false);
        SMART_EXPANSIONS = ConfigReader.getConfigBoolean("SMART_EXPANSIONS", true, false);

        rrtTreePanelOn = ConfigReader.getConfigBoolean("RRTTREE_PANEL_ON", rrtTreePanelOn, false);
        rrtTreePanelX = ConfigReader.getConfigInt("RRTTREE_PANEL_X", rrtTreePanelX, false);
        rrtTreePanelY = ConfigReader.getConfigInt("RRTTREE_PANEL_Y", rrtTreePanelY, false);

        Machinetta.Debugger.debug("rrtTreePanelOn=" + rrtTreePanelOn, 1, "RRTPlanner");

        rrtPathPanelOn = ConfigReader.getConfigBoolean("RRTPATH_PANEL_ON", rrtPathPanelOn, false);
        rrtPathPanelX = ConfigReader.getConfigInt("RRTPATH_PANEL_X", rrtPathPanelX, false);
        rrtPathPanelY = ConfigReader.getConfigInt("RRTPATH_PANEL_Y", rrtPathPanelY, false);
        Machinetta.Debugger.debug("rrtPathPanelOn=" + rrtPathPanelOn, 1, "RRTPlanner");

        int emitterCount = ConfigReader.getConfigInt("EMITTER_COUNT", 0, false);

        for (int loopi = 0; loopi < emitterCount; loopi++) {
            String fieldName = "EMITTER_" + loopi;
            String emitterCoords = ConfigReader.getConfigString(fieldName, null, false);
            if (null == emitterCoords) {
                continue;
            }
            if (emitterCoords.equalsIgnoreCase("RANDOM")) {
                continue;
            }
            double[] coords = StringUtils.parseDoubleList(2, emitterCoords);
            if (null == coords) {
                continue;
            }
            if (coords.length < 2) {
                continue;
            }
            emitters.add(new Point((int) coords[0], (int) coords[1]));
        }
        configured = true;

    }

    public static void lookupProxyId() {
        if (null == proxyId) {
            if (STANDALONE_TEST) {
                proxyId = new NamedProxyID("STANDALONE_TEST");
                proxyIdString = "STANDALONE_TEST";
            } else {
                ProxyState state = new ProxyState();
                RAPBelief selfBel = state.getSelf();
                proxyId = selfBel.getProxyID();
                proxyIdString = proxyId.toString();
            }
        }
    }

    public static void updateRRTPanels(double currX, double currY, ArrayList<CostMap> costMaps, Path3D path, PriorityQueue<Node> treeNodes) {
        if (!configured) {
            configure();
        }

        if (rrtPathPanelOn) {

            Machinetta.Debugger.debug("Showing RrtTreePanel", 0, "RRTPlanner");
            if (null == rrtPathPanel) {
                rrtPathPanel = new PathDisplayPanel(UAVRI.MAP_WIDTH_METERS, UAVRI.MAP_HEIGHT_METERS, costMaps, path);
                rrtPathFrame = new JFrame("PathDisplayPanel: " + proxyIdString);
                rrtPathFrame.setLocation(rrtPathPanelX, rrtPathPanelY);
                rrtPathFrame.getContentPane().setLayout(new BorderLayout());
                rrtPathFrame.getContentPane().add(rrtPathPanel, BorderLayout.CENTER);
                rrtPathFrame.pack();
                //                rrtPathFrame.setSize((mapSize/mapStep), (mapSize/mapStep));
                Machinetta.Debugger.debug("Setting size: " + (mapSize / mapStep) + " based on " + mapStep + " " + mapSize, 1, "RRTPlanner");
                rrtPathFrame.setSize(400, 400);
                rrtPathFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        if (STANDALONE_TEST) {
                            System.exit(0);
                        }
                    }
                });

                rrtPathFrame.setVisible(true);
            } else {
                rrtPathPanel.setData(currX, currY, costMaps, path);
                rrtPathPanel.repaint();
            }
        }
        if (rrtTreePanelOn) {

            if (null == rrtTreePanel) {
                //DEBUG Display the RRT
                rrtTreePanel = new RrtTreePanel(treeNodes);
                rrtTreeFrame = new JFrame("RrtTreePanel: " + proxyIdString);
                rrtTreeFrame.setLocation(rrtTreePanelX, rrtTreePanelY);
                rrtTreeFrame.getContentPane().setLayout(new BorderLayout());
                rrtTreeFrame.getContentPane().add(rrtTreePanel, BorderLayout.CENTER);
                rrtTreeFrame.pack();
                //                rrtTreeFrame.setSize((mapSize/mapStep), (mapSize/mapStep));
                rrtTreeFrame.setSize(400, 400);
                rrtTreeFrame.setVisible(true);
            } else {
                rrtTreePanel.setData(treeNodes);
                rrtTreePanel.repaint();
            }
        }
    }

    // /* DEBUG:  Draw an image of the RRT tree discovered during path search
    public static class RrtTreePanel extends JPanel {

        String filename = "/afs/cs.cmu.edu/user/owens/RRT/rrtsnapshot";

        int nodeSize = 4, half = 2;
        Node[] nodes = null;
        boolean newData = false;

        public RrtTreePanel(PriorityQueue<Node> pq) {
            if (null != pq) {
                nodes = pq.toArray(new Node[1]);
            }
        }

        public void setData(PriorityQueue<Node> pq) {
            if (null != pq) {
                final Node[] newNodes = pq.toArray(new Node[1]);
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            nodes = newNodes;
                            newData = true;
                            if (SYNCHRONOUS_REPAINT_ON_SETDATA) {
                                paintImmediately(null);
                            }
                        }
                    }
                    );
                } catch (InterruptedException e) {
                    System.err.println("Update of DebugRRT panel data was interrupted e=" + e);
                    e.printStackTrace();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    System.err.println("e=" + e);
                    e.printStackTrace();
                }
            }
        }

        //	Color lineColor = new Color(0,0,255,16);
        Color lineColor = Color.blue;
        BufferedImage imgTranslucent = null;

        public void paint(Graphics g) {
            try {
                long startTime = System.currentTimeMillis();
                Graphics2D g2 = (Graphics2D) g;

                g2.setColor(Color.white);
                g2.clearRect(0, 0, (int) getSize().getWidth(), (int) getSize().getHeight());

                g2.setStroke(new BasicStroke(1));

                double dx = ((double) getWidth()) / ((double) mapSize);
                double dy = ((double) getHeight()) / ((double) mapSize);
                int height = getHeight();

                int improved = 0, worsened = 0, deepest = 0;

                g2.setColor(Color.blue);

                // System.out.println("Number of nodes: " + nodes.length + " created: " + Node.count);
                for (int loopi = 0; loopi < nodes.length; loopi++) {
                    Node node = nodes[loopi];
                    if (node.depth > depthToPaint) {
                        continue;
                    }
                    // System.out.println("Drawing node");
                    g2.fillOval((int) (dx * node.x) - half, (int) (height - (dy * node.y)) - half, nodeSize, nodeSize);

                    if (node.previous != null) {

// 		    // CODE TO SET COLOR BASED ON GOING UP OR DOWN IN Z AXIS
// 		    //
//                  if (node.z == node.previous.z) imgG.setColor(Color.blue);
//                  else if (node.z > node.previous.z) imgG.setColor(Color.green);
//                  else imgG.setColor(Color.red);
                        /*
                         if (node.z == node.previous.z) g2.setColor(Color.blue);
                         else if (node.z > node.previous.z) g2.setColor(Color.green);
                         else g2.setColor(Color.red);
                         */
                        /*
                         if (node.cost < node.previous.cost) {
                         g2.setColor(Color.green);
                         improved++;
                         } else {
                         g2.setColor(Color.RED);
                         worsened++;
                         }
                         */
                        /*
                         if (deepest < node.depth) deepest = node.depth;
                         g2.setColor(new Color(0, 0, Math.min(255, node.depth * 2)));
                         */
                        g2.drawLine((int) (dx * node.x), (int) (height - (dy * node.y)),
                                (int) (dx * node.previous.x), (int) (height - (dy * node.previous.y)));
                    }
                }
            } catch (Exception e) {
                Machinetta.Debugger.debug(1, "Exception trying to repaint RRTPlanner.RRTTreePanel, ignoring it.  e=" + e);
                // e.printStackTrace();
            }
        }
    }
}
