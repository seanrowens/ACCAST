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
 * L3Create2.java
 *
 * Created on February 7, 2006, 5:24 PM
 *
 * SEE INLINE COMMENTS regarding various final static globals.
 *
 */
package AirSim.Configs;

import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.TDOACoordCommand;
import AirSim.Machinetta.UAVRI;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.Match.MatchCondition;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.Associates;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.ProxyState;
import java.awt.Rectangle;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import java.util.HashMap;

/**
 *
 * @author pscerri
 */
public class MAAFCreate {

    public static final String SEP = File.separator;
    public static int noHumvees = 2;
    public static int noUAVs = 1;
    //
    // START CONFIGURING HERE
    //
    public static boolean operBinaryBayesFilterOn = false;
    public static boolean operBinaryBayesFilterPanelOn = false;
    public static boolean sanjayaOneKmGridLinesOn = false;
    public static boolean uavBinaryBayesFilterPanelOn = false;
    public static boolean uavBinaryBayesFilterPanelFirstFourOn = false;
    public static boolean uavEntropyPanelOn = false;
    public static boolean uavRRTPathPanelOn = false;
    public static boolean uavRRTTreePanelOn = false;
    public static int uavEODistanceTolerance = 100;
    public static int uavEOIR_EO_CAP_PERCENT = 25;
    public static int uavEOIR_IR_CAP_PERCENT = 25;
    public static String loc = ".";
    public static String locs = "./";
    public static boolean uavPlacementRandom = false;
    public static int mapWidthMeters = 50000;
    public static int mapHeightMeters = 50000;
    public static double uavMaxSpeedMetersPerSec = 41.666;
    public static double uavMaxTurnRateDegPerStep = 3;
    public static double uavAtLocationDistance = 50;
    public static double uavMoveFinishedHoldRadius = 50;
    public static double RRTPreferredPathMeters = 5000.0;
    public static double RRTMaxPath = 2500.0;
    public static double RRTBranchRangeX = 500.0;
    public static double RRTBranchRangeY = 500.0;
    public static double RRTMaxZChange = 1.0;
    public static double RRTMinPathLength = 500.0;
    public static double PathPlannerAtWaypointTolerance = 200.0;
    public static double PathPlannerReplanDistFromEnd = 1000.0;
    public static boolean PATHPLANNER_SMOOTH_GRAD_DESC_ON = false;
    public static boolean PATHPLANNER_REDIRECT_PATH_TO_TARGET = false;
    public static double PathConflictDist = 1000.0;
    public static double PathConflictZDist = 100.0;
    public static boolean uavPathDeconflictionOn = true;
    public static boolean operPathDeconflictionOn = true;
    public static int PathPlannerTTL = 5;
    public static int noOpforInfantry = 0;
    public static int noBlueforInfantry = 0;
    public static int noCivilians = 0;
    public static int noRSSIUAVs = 0;
    public static int noEOIRUAVs = 0;
    public static int noArmoredUAVs = 0;
    public static double uavRefuelThreshold = 2000;
    public static int noDetectors = 0;
    public static int noGeolocators = 0;
    public static int noOperators = 1;
    public static int noTrafficControllers = 0;
    public static int noUGSs = 0;
    public static int BBFSensorMinRange = 5000;
    public static int BBFSensorMaxRange = 10000;
    public static double BBFGridScaleFactor = 50.0;
    public static boolean bbfRandomSharingOn = true;
    public static double bbfRandomSharingProb = 0.95;
    public static int bbfRandomSharingTtl = 5;
    public static boolean bbfKLDSharingOn = false;
    public static boolean bbfRemoteKLDSharingOn = false;
    public static boolean OtherVehicleCostmapOn = true;
    public static double OtherVehicleCostMapConflict = 1000;
    public static double OtherVehicleCostMapZConflict = 100;
    public static double OtherVehicleCostMapAvoid = 3000;
    public static int SA9Cost = 1;
    public static int diffusionInterval = 5000;
    public static double diffusionRate = 0.001;
    // Used to place emitters, i.e. SA9s.
    public static int uavProxyMemoryMegabytes = 400;
    public TeamBelief team = null;
    public boolean useAssociates = true;
    public int noAssociates = 3;
    public static boolean UAV_DYNAMIC_FLY_ZONES = false;
    public static boolean OPERATOR_CLUSTERING_ON = false;
    public static String[] EnvFileAdditionalLines = null;
    public static String[] UAVCfgAdditionalLines = null;
    public static int RRTCONFIG_NO_EXPANSIONS = 2000;
    public static int RRTCONFIG_BRANCHES_PER_EXPANSION = 3;
    public static double RRTCONFIG_MAX_THETA_CHANGE = 3;
    public static double RRTCONFIG_MAX_PSI_CHANGE = 1;
    public static double RRTCONFIG_MAX_BRANCH_LENGTH = 25.0;
    public static double RRTCONFIG_MIN_BRANCH_LENGTH = -25.0;
    public static int RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = 50;
    public static int RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = 50;
    public static String[] rssiUavLocations = null;
    // Where do we write our output logs?  If OUTPUT_TO_CUR_DIR is
    // true, then write to currentdir/output.  If false then build a
    // path using OUTPUT_DIR_PREFIX and 'loc'. (The 'loc' variable is
    // specified as a command line param.)
    public static String OUTPUT_DIR = "output";
    public static String OUTPUT_DIRS = "output" + SEP;
    public static boolean OUTPUT_TO_CUR_DIR = false;
    public static String OUTPUT_DIR_PREFIX;
    public static String OUTPUT_DIR_PREFIXS;

    // If true, the number and location of the emitters is added to
    // various debugging GUIs on the UAV proxies.  This information is
    // used only to display crosses on the GUI at each emitter
    // location, for human viewers, and is not made available by the
    // UAV 'intelligence'.
    public final static boolean ADD_EMITTER_LOCS_TO_UAV_CFG = true;
    public final static String SANJAYA_UPDATE_RATE_COMMENT =
            "# The update rate is the number of ms that sanjay sleeps between each\n" + "# 'step' of the simulator.  By default the simulator 'steps' ten times\n" + "# per second, so an update rate of 50 means the simulator simulates\n" + "# second of time every .5 seconds or so.\n";
    public static int sanjayaUpdateRate = 100;
    public final static String SANJAYA_ASSET_COLLISION_COMMENT =
            "# Sanjaya has code that checks for collision between SmallUAVs,\n" + "# where 'collision' is defined as 'coming within some range n of\n" + "# each other'.  These flags control whether or not this code runs\n" + "# and what it uses for range.  When we use smaller maps (1km x\n" + "# 1km) and slower UAVs, collision range should accordingly be\n" + "# reduced.\n";
    public static boolean sanjayaAssetCollisionDetectionOn = true;
    public static double sanjayaAssetCollisionRangeMeters = 2000;
    public static double sanjayaAssetCollisionMinZDistMeters = 100;
    public final static String COSTMAPS_COMMENT =
            "# In general, only one of the following costmaps should be on.\n" + "# The RSSI UAVs use a Binary Bayes Filter to locate targets.\n" + "# They generate an entropy map from the filter to use as a\n" + "# costmap in planning their paths to cover regions where\n" + "# uncertainty still exists.  If the Binary Bayes Filter is not\n" + "# set on, one of the other random gaussian costmaps below is used\n" + "# instead.\n";
    public final static String USE_XPLANE_CAMERA_COMMENT =
            "# Turn on sending of uav locations to xplane via UDP.\n";
    public static boolean USE_XPLANE_CAMERA = false;
    public static String USE_XPLANE_CAMERA_HOST = "zeta.cimds.ri.cmu.edu";
    public final static String END_CONDITION_COMMENT =
            "# These end conditions are checked every step in the simulator,\n" + "# and if any them are true, the simulator exits.  This is to\n" + "# facilitate batch experiments.  CAPTURE_THE_FLAG checks for \n" + "# OPFOR ground vehicles inside a BLUEFOR 'flag zone' in the lower\n" + "# left 5km of the map, and for BLUEFOR ground vehicles inside\n" + "# the OPFOR 'flag zone' in the upper right 5km of the map.\n" + "# ALL_EMITTERS_DETECTED checks that all assets that implement\n" + "# the Emitter interface have been 'detected', i.e. by a SmallAUV\n" + "# receiving a CAMERA_COMMAND while within 1km of the emitter.\n";
    public static boolean END_CONDITION_CAPTURE_THE_FLAG = false;
    public static boolean END_CONDITION_ALL_HMMMVS_DEAD = false;
    public static boolean END_CONDITION_ALL_OPFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_BLUEFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_EMITTERS_DETECTED = false;
    public static boolean uavBinaryBayesFilterOn = true;
    public static boolean uavRandomEntireMapCostmapOn = false;
    public static boolean uavRandomSmallMovesCostmapOn = false;
    public static double uavRandomSmallMovesRange = 5000;
    public static boolean uavRandomClustersCostmapOn = false;
    public static double uavRandomClustersRange = 3000;
    public static int uavRandomClustersCount = 3;
    public final static String COSTMAPS_RANDOM_GUASSIANS_COMMENT =
            "# The random gaussian params are used by all of the random costmaps.\n" + "# The amplitude multiplier is multiplied by a random number\n" + "# from 0 to 1.0.  The divisor multiplier is multiplied by a\n" + "# random integer from 5 to 14.\n";
    public static double uavRandomGuassiaAmplitudeMultiplier = 500;
    public static double uavRandomGuassianDivisorMultiplier = 100;

    static String BASEDIR_OWENS = "/afs/cs.cmu.edu/user/owens/camra/src/";
    public static String MASTER_CLASSPATH = BASEDIR_OWENS+":"+BASEDIR_OWENS+"Util-src:"+BASEDIR_OWENS+"Gui-src:"+BASEDIR_OWENS+"Machinetta-src:"+BASEDIR_OWENS+"AirSim-src:"+BASEDIR_OWENS+"jar/swing-layout-1.0.jar";
    // SLAVE_CLASSPATH is used to specify the classpath when running
    // on one of the slave machines, i.e. the various proxies.
    public static String SLAVE_CLASSPATH;
    // The master system, on which we run the Sanjaya wargame, the
    // SimOperator, and the UDP Switch.
    public static String MASTER_MACHINE;
    public static String[] SLAVE_MACHINES = {"localhost"};
    // If non null, sets the xwindows display for master machines scripts
    public static String MASTER_DISPLAY;
    // If non null, sets the xwindows display for slave machines scripts
    public static String SLAVE_DISPLAY;
    // path+fullname of a script to run on the master system to
    // synchronize any code changes out to the slave systems.
    public static String SYNCHRONIZATION_SCRIPT;
    public static String JAVA;
    public static double mapLowerLeftLat = -1;
    public static double mapLowerLeftLon = -1;
    public static String CTDB_BASE_DIR = "/usr1/grd/sanjaya_maps_1_0";
    public static String CTDB_DEFAULT = "sanjaya001_0_0_50000_50000_050";
    public static String CTDB_FILE = CTDB_DEFAULT;

    public static String getCtdbBaseName() {
        return CTDB_BASE_DIR + SEP + CTDB_FILE;
    }
    
    public static String TERRAIN_COSTMAP_LOCATION = "/afs/cs.cmu.edu/user/owens/TerrainCM";
    //
    // STOP CONFIGURING HERE
    //
    Random rand = new Random();

    private static double mps_to_kph(double metersPerSecond) {
        return ((metersPerSecond * 3600) / 1000);
    }

    /**
     * Writes all the cfg and xml file for a L3Comms type scenario
     */
    public MAAFCreate() {
        if (!OUTPUT_TO_CUR_DIR) {
            File locDir = new File(loc);
            OUTPUT_DIR = OUTPUT_DIR_PREFIXS + locDir.getName();
            OUTPUT_DIRS = OUTPUT_DIR + SEP;
            Machinetta.Debugger.debug("Setting log output dir to " + OUTPUT_DIR, 1, this);
        }

        Vector teamMembers = new Vector();

        // For now, add all proxy ids to the team
        for (int i = 0; i < noUAVs; i++) {
            Machinetta.Debugger.debug("Adding UAV" + i + " to team", 1, this);
            teamMembers.add(new NamedProxyID("UAV" + i));
        }

        for (int i = 0; i < noOperators; i++) {
            Machinetta.Debugger.debug("Adding Operator" + i + " to team", 1, this);
            teamMembers.add(new NamedProxyID("Operator" + i));
        }

        for (int i = 0; i < noHumvees; i++) {
            Machinetta.Debugger.debug("Adding H" + i + " to team", 1, this);
            teamMembers.add(new NamedProxyID("H" + i));
        }

        Machinetta.Debugger.debug("Creating TeamBelief with " + teamMembers.size() + " members", 1, this);

        // For testing Dynamic Teaming
        // teamMembers.clear();
        // teamMembers.add(new NamedProxyID("Operator0"));
        team = new TeamBelief("TeamAll", teamMembers, new Hashtable());

        // Now create specific files
        for (int i = 0; i < noUAVs; i++) {
            createUAVCfgFile("UAV" + i);
            makeUAVBeliefs("UAV" + i, i);
        }

        for (int i = 0; i < noOperators; i++) {
            createOperatorCfgFile("Operator" + i);
            makeOperatorBeliefs("Operator" + i);
        }

        for (int i = 0; i < noHumvees; i++) {
            createHVCfgFile("H" + i);
            makeHVBeliefs("H" + i);
        }

        Machinetta.Debugger.debug("Making run script", 1, this);
        makeRunScript();
        Machinetta.Debugger.debug("Making Env file", 1, this);
        makeEnvFile();
        Machinetta.Debugger.debug("Done with l3create constructor.", 1, this);

        makeKillScript();
        makeUptimeScript();
        makeUptimesScript();
        makeDeleteLogsScript();
        makeTopScript();
        makeTopsScript();
        makeAllKillScript();
        makeFetchLogsScript();

    }

    public static void main(String argv[]) {

        // First args is always the directory we'll be writing all the
        // config and script files into;
        loc = argv[0];
        locs = loc + SEP;

        mapHeightMeters = 50000;
        mapWidthMeters = 50000;

        sanjayaAssetCollisionMinZDistMeters = 100;
        sanjayaAssetCollisionRangeMeters = 1000;

        // For scripts		
        String BASE_DIR = "/Users/pscerri/NetBeansProjects";
        SLAVE_CLASSPATH = MASTER_CLASSPATH;
        MASTER_MACHINE = "mavrodafni";
        MASTER_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
        SLAVE_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
        SYNCHRONIZATION_SCRIPT = "";
        OUTPUT_TO_CUR_DIR = false;
        OUTPUT_DIR_PREFIX = "/usr1/logs";
        OUTPUT_DIR_PREFIXS = "/usr1/logs/";
        JAVA = "java -server";

        new MAAFCreate();
    }

    /**
     * This will (obviously) only work on linux/unix (and obviously, the classpath needs to change per user.)
     */
    public void makeRunScript() {
        if (SLAVE_MACHINES == null || SLAVE_MACHINES.length == 0) {
            makeSingleMachineRunScript();
        } else {

            // Make scripts for all machines
            try {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "run.sh"));

                out.writeBytes("#!/bin/tcsh\n\n");
                out.writeBytes("setenv CLASSPATH " + MASTER_CLASSPATH + "\n\n");

                //out.writeBytes("echo Synchronizing files on slaves \n");
                //out.writeBytes(SYNCHRONIZATION_SCRIPT + "\n");

                out.writeBytes("\n");
                if (null != MASTER_DISPLAY) {
                    out.writeBytes("setenv DISPLAY " + MASTER_DISPLAY + "\n");
                }
                out.writeBytes("\n");
                out.writeBytes("mkdir -p " + OUTPUT_DIR + "\n");
                out.writeBytes("\n");

		out.writeBytes(JAVA+"  AirSim.Configs.TPTFactory\n");
                out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
                out.writeBytes("sleep 1\n\n");
                out.writeBytes(JAVA + "  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& " + OUTPUT_DIRS + "SimOut &\n\n");
                // out.writeBytes(JAVA+"  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt >& "+OUTPUT_DIRS+"SimOut &\n\n");
                out.writeBytes("sleep 1\n\n");

                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("\n\necho Starting processes on " + SLAVE_MACHINES[i] + "\n");
                    out.writeBytes("# ssh " + SLAVE_MACHINES[i] + " chmod +x " + locs + SLAVE_MACHINES[i] + ".sh\n");
                    if (SLAVE_MACHINES[i].equalsIgnoreCase("localhost")) {
                        out.writeBytes(locs + SLAVE_MACHINES[i] + ".sh &\n");
                    } else {
                        out.writeBytes("ssh " + SLAVE_MACHINES[i] + " " + locs + SLAVE_MACHINES[i] + ".sh &\n");
                    }
                }

                out.writeBytes("\necho Starting Operators\n");
                for (int i = 0; i < noOperators; i++) {
                    out.writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "Operator" + i + ".cfg >& " + OUTPUT_DIRS + "Operator" + i + ".out &\n");
                    out.writeBytes("sleep 1\n");
                }

                if (noTrafficControllers > 0) {
                    out.writeBytes("\necho Starting TrafficControllers\n");
                    for (int i = 0; i < noTrafficControllers; i++) {
                        out.writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "TrafficController" + i + ".cfg >& " + OUTPUT_DIRS + "TrafficController" + i + ".out &\n");
                        out.writeBytes("sleep 1\n");
                    }
                }

                out.flush();
                out.close();

                // The switch, the simulator and the operator all run on the master machine
                DataOutputStream[] slaveScripts = new DataOutputStream[SLAVE_MACHINES.length];
                for (int i = 0; i < slaveScripts.length; i++) {
                    slaveScripts[i] = new DataOutputStream(new FileOutputStream(locs + SLAVE_MACHINES[i] + ".sh"));

                    slaveScripts[i].writeBytes("#!/bin/tcsh\n\n");
                    slaveScripts[i].writeBytes("setenv CLASSPATH " + SLAVE_CLASSPATH + "\n\n");
                    slaveScripts[i].writeBytes("\n");
                    if (null != SLAVE_DISPLAY) {
                        slaveScripts[i].writeBytes("setenv DISPLAY " + SLAVE_DISPLAY + "\n");
                    }
                    slaveScripts[i].writeBytes("\n");
                    slaveScripts[i].writeBytes("mkdir -p " + OUTPUT_DIR + "\n");
                    slaveScripts[i].writeBytes("\n");
                }
                int outIndex = 0;

                for (int i = 0; i < noUAVs; i++) {
                    Machinetta.Debugger.debug("Adding regular uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);

                    slaveScripts[outIndex].writeBytes("\necho Starting UAV " + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "UAV" + i + ".cfg >& " + OUTPUT_DIRS + "UAV" + i + ".out &\n");
                    //                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "UAV"+i+".cfg >& "+OUTPUT_DIRS + "UAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noRSSIUAVs; i++) {
                    Machinetta.Debugger.debug("Adding RSSI uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting RSSIUAV " + i + " on $HOST\n");
                    String outString;
                    outString = JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "RSSI-UAV" + i + ".cfg >& " + OUTPUT_DIRS + "RSSI-UAV" + i + ".out &\n";
                    Machinetta.Debugger.debug("Writing to file " + SLAVE_MACHINES[outIndex] + ": \"" + outString + "\"", 1, this);
                    slaveScripts[outIndex].writeBytes(outString);
                    //                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "RSSI-UAV"+i+".cfg >& "+OUTPUT_DIRS + "RSSI-UAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noEOIRUAVs; i++) {
                    Machinetta.Debugger.debug("Adding EOIR uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting EOIRUAV " + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "EOIR-UAV" + i + ".cfg >& " + OUTPUT_DIRS + "EOIR-UAV" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noArmoredUAVs; i++) {
                    Machinetta.Debugger.debug("Adding Armored uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting SUAV " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "AUAV" + i + ".cfg >& " + OUTPUT_DIRS + "AUAV" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noUGSs; i++) {
                    Machinetta.Debugger.debug("Adding UGS " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting UGS " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "UGS" + i + ".cfg >& " + OUTPUT_DIRS + "UGS" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noHumvees; i++) {
                    Machinetta.Debugger.debug("Adding Humvee " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting HV " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "H" + i + ".cfg >& " + OUTPUT_DIRS + "H" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noDetectors; i++) {
                    Machinetta.Debugger.debug("Adding detector " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting Detector " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "Detector" + i + ".cfg >& " + OUTPUT_DIRS + "Detector" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noGeolocators; i++) {
                    Machinetta.Debugger.debug("Adding geolocator " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting Geolocator " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA + "  -Xmx400m Machinetta.Proxy " + locs + "Geolocator" + i + ".cfg >& " + OUTPUT_DIRS + "Geolocator" + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                // @TODO: Add code to script here to run top periodically... somehow.
                //                 for (int i = 0; i < slaveScripts.length; i++) {
                //                     slaveScripts[i].flush();
                //                     slaveScripts[i].close();
                //                 }

                for (int i = 0; i < slaveScripts.length; i++) {
                    slaveScripts[i].flush();
                    slaveScripts[i].close();
                }
            } catch (Exception e) {
                System.out.println("Failed to write script file: " + e);
                e.printStackTrace();
            }


        }
    }

    private void makeSingleMachineRunScript() {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "run.sh"));

            out.writeBytes("#!/bin/tcsh\n\n");
            out.writeBytes("setenv CLASSPATH " + MASTER_CLASSPATH + "\n\n");

            out.writeBytes("\n");
            if (null != MASTER_DISPLAY) {
                out.writeBytes("setenv DISPLAY " + MASTER_DISPLAY + "\n");
            }
            out.writeBytes("\n");
            out.writeBytes("mkdir -p " + OUTPUT_DIR + "\n");
            out.writeBytes("\n");
            out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
            out.writeBytes("sleep 1\n\n");
            out.writeBytes(JAVA + "  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& " + OUTPUT_DIRS + "SimOut &\n\n");
            out.writeBytes("sleep 1\n\n");

            // For now, the same script runs all the agents.
            if (noUAVs > 0) {
                out.writeBytes("\necho Starting UAVs\n");
            }
            for (int i = 0; i < noUAVs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "UAV" + i + ".cfg >& " + OUTPUT_DIRS + "UAV" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noRSSIUAVs > 0) {
                out.writeBytes("\necho Starting RSSI UAVs\n");
            }
            for (int i = 0; i < noRSSIUAVs; i++) {
                out.writeBytes("\necho Starting RSSIUAV " + i + " on $HOST\n");
                out.writeBytes(JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "RSSI-UAV" + i + ".cfg >& " + OUTPUT_DIRS + "RSSI-UAV" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noEOIRUAVs > 0) {
                out.writeBytes("\necho Starting EOIR UAVs\n");
            }
            for (int i = 0; i < noEOIRUAVs; i++) {
                out.writeBytes("\necho Starting EOIRUAV " + i + " on $HOST\n");
                out.writeBytes(JAVA + "  -Xmx" + uavProxyMemoryMegabytes + "m Machinetta.Proxy " + locs + "EOIR-UAV" + i + ".cfg >& " + OUTPUT_DIRS + "EOIR-UAV" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noArmoredUAVs > 0) {
                out.writeBytes("\necho Starting AUAVs\n");
            }
            for (int i = 0; i < noArmoredUAVs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "AUAV" + i + ".cfg >& " + OUTPUT_DIRS + "AUAV" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noUGSs > 0) {
                out.writeBytes("\necho Starting UGSs\n");
            }
            for (int i = 0; i < noUGSs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "UGS" + i + ".cfg >& " + OUTPUT_DIRS + "UGS" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noHumvees > 0) {
                out.writeBytes("\necho Starting Humvees\n");
            }
            for (int i = 0; i < noHumvees; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "H" + i + ".cfg >& " + OUTPUT_DIRS + "H" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noDetectors > 0) {
                out.writeBytes("\necho Starting Detectors\n");
            }
            for (int i = 0; i < noDetectors; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "Detector" + i + ".cfg >& " + OUTPUT_DIRS + "Detector" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noGeolocators > 0) {
                out.writeBytes("\necho Starting Geolocators\n");
            }
            for (int i = 0; i < noGeolocators; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + "Geolocator" + i + ".cfg >& " + OUTPUT_DIRS + "Geolocator" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noOperators > 0) {
                out.writeBytes("\necho Starting Operators\n");
            }
            for (int i = 0; i < noOperators; i++) {
                out.writeBytes(JAVA + "  -Xmx256M Machinetta.Proxy " + locs + "Operator" + i + ".cfg >& " + OUTPUT_DIRS + "Operator" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noTrafficControllers > 0) {
                out.writeBytes("\necho Starting TrafficControllers\n");
            }
            for (int i = 0; i < noTrafficControllers; i++) {
                out.writeBytes(JAVA + "  -Xmx128M Machinetta.Proxy " + locs + "TrafficController" + i + ".cfg >& " + OUTPUT_DIRS + "TrafficController" + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public void makeUptimeScript() {
        String filename = locs + "uptime.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("UPTIME=`uptime`\n");
            out.writeBytes("echo $HOSTNAME $UPTIME\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeTopScript() {
        String filename = locs + "top.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("echo $HOSTNAME\n");
            out.writeBytes("top -b -n 1 -u $USER\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeKillScript() {
        String filename = locs + "kill.sh";
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/tcsh\n");
            out.writeBytes("\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill >& /dev/null\n");
            out.writeBytes("sleep 1\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill -9 >& /dev/null\n");
            out.writeBytes("\n");
            out.writeBytes("echo \"Remaining java processes on \" $HOST\n");
            out.writeBytes("echo \"________________________________________\"\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |fgrep -v fgrep\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeAllKillScript() {
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "allkill.sh"));

            out.writeBytes("#!/bin/tcsh\n\n");

            out.writeBytes("setenv CONFIG_DIR " + loc + "\n");
            out.writeBytes("echo \"Killing things on $HOSTNAME\"\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill >& /dev/null\n");
            out.writeBytes("sleep 1\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill -9 >& /dev/null\n");
            out.writeBytes("\n");
            out.writeBytes("echo \"Remaining java processes\"\n");
            out.writeBytes("echo \"________________________________________\"\n");
            out.writeBytes("ps auxwwww|head -1\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |fgrep -v fgrep\n");
            out.writeBytes("\n");
            if (SLAVE_MACHINES != null) {
                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("echo \"Killing things on " + SLAVE_MACHINES[i] + "\"\n");
                    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " $CONFIG_DIR/kill.sh\n");
                    out.writeBytes("\n");
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file: " + e);
            e.printStackTrace();
        }
    }

    public void makeUptimesScript() {
        // Make scripts for all machines
        String filename = locs + "uptimes.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("export CONFIG_DIR=" + loc + "\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/uptime.sh\n");
            out.writeBytes("\n");
            if (SLAVE_MACHINES != null) {
                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " $CONFIG_DIR/uptime.sh &\n");
                    out.writeBytes("\n");
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeDeleteLogsScript() {
        // Make scripts for all machines
        String filename = locs + "deletelogs.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            if (SLAVE_MACHINES != null) {
                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " rm -rf " + OUTPUT_DIR + "\n");
                    out.writeBytes("\n");
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeTopsScript() {
        // Make scripts for all machines
        String filename = locs + "tops.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("export CONFIG_DIR=" + loc + "\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/top.sh\n");
            out.writeBytes("\n");
            if (SLAVE_MACHINES != null) {
                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " $CONFIG_DIR/top.sh &\n");
                    out.writeBytes("\n");
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '" + filename + "' : " + e);
            e.printStackTrace();
        }
    }

    public void makeFetchLogsScript() {
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "fetchlogs.sh"));

            out.writeBytes("#!/bin/bash\n\n");

            if (SLAVE_MACHINES != null) {
                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("export LOGDIR=$1\n");
                    out.writeBytes("echo \"Fetching logs from " + SLAVE_MACHINES[i] + "\"\n");
                    out.writeBytes("scp -r " + SLAVE_MACHINES[i] + ":$LOGDIR $LOGDIR\n");
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file: " + e);
            e.printStackTrace();
        }

    }

    public void makeEnvFile() {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "Env.txt"));

            //out.writeBytes("M2TANK R-M2-9 20344 20275 OPFOR\n");

            out.writeBytes("GUI_ON true\n");

            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes(SANJAYA_UPDATE_RATE_COMMENT);
            out.writeBytes("UPDATE_RATE " + sanjayaUpdateRate + "\n\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");

            out.writeBytes(USE_XPLANE_CAMERA_COMMENT);
            out.writeBytes("USE_XPLANE_CAMERA " + USE_XPLANE_CAMERA + "\n");
            out.writeBytes("USE_XPLANE_CAMERA_HOST " + USE_XPLANE_CAMERA_HOST + "\n");
            out.writeBytes("\n");

            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM " + sanjayaOneKmGridLinesOn + "\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n");

            out.writeBytes("\n");
            out.writeBytes(SANJAYA_ASSET_COLLISION_COMMENT);
            out.writeBytes("ASSET_COLLISION_DETECTION_ON " + sanjayaAssetCollisionDetectionOn + "\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_RANGE_METERS " + sanjayaAssetCollisionRangeMeters + "\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_MIN_Z_DIST_METERS " + sanjayaAssetCollisionMinZDistMeters + "\n");

            out.writeBytes("\n");
            out.writeBytes(END_CONDITION_COMMENT);
            out.writeBytes("END_CONDITION_CAPTURE_THE_FLAG " + END_CONDITION_CAPTURE_THE_FLAG + "\n");
            out.writeBytes("END_CONDITION_ALL_HMMMVS_DEAD " + END_CONDITION_ALL_HMMMVS_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_OPFOR_DEAD " + END_CONDITION_ALL_OPFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_BLUEFOR_DEAD " + END_CONDITION_ALL_BLUEFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_EMITTERS_DETECTED " + END_CONDITION_ALL_EMITTERS_DETECTED + "\n");
            out.writeBytes("\n");

            // out.writeBytes("UGS UGS0 5000 5000 BLUEFOR\n");

            // out.writeBytes("HUMMER H0 4700 4925 BLUEFOR\n");

            // out.writeBytes("TRUCK MULTI 5 OPFOR\n");

            out.writeBytes("ASSET_CONFIG AT_LOCATION_DISTANCE " + uavAtLocationDistance + "\n");
            out.writeBytes("ASSET_CONFIG MOVE_FINISHED_HOLD_RADIUS " + uavMoveFinishedHoldRadius + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_SPEED_KPH " + mps_to_kph(uavMaxSpeedMetersPerSec) + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_TURN_RATE_DEG " + uavMaxTurnRateDegPerStep + "\n");

            out.writeBytes("ASSET_CONFIG RSSI_MAP_SCALE " + BBFGridScaleFactor + "\n");

            out.writeBytes("CTDB_BASE_NAME " + getCtdbBaseName() + "\n");

            for (int i = 0; i < noUAVs; i++) {
                if (uavPlacementRandom) {
                    out.writeBytes("SMALL_UAV UAV" + i + " RANDOM " + uavRefuelThreshold + " BLUEFOR\n");
                } else {
                    out.writeBytes("SMALL_UAV UAV" + i + " " + (13119 + i * 2 * PathConflictDist) + " 34805 1000 " + uavRefuelThreshold + " BLUEFOR\n");
                }

                out.writeBytes("SENSOR TDOA UAV" + i + "\n");
		out.writeBytes("PROXY UAV" + i + " ON\n");
            }

            for (int i = 0; i < noRSSIUAVs; i++) {
                if (uavPlacementRandom) {
                    out.writeBytes("SMALL_UAV RSSI-UAV" + i + " RANDOM " + uavRefuelThreshold + " BLUEFOR\n");
                } else if (null != rssiUavLocations) {
                    out.writeBytes("SMALL_UAV RSSI-UAV" + i + " " + rssiUavLocations[i] + "  " + uavRefuelThreshold + " BLUEFOR\n");
                } else {
                    out.writeBytes("SMALL_UAV RSSI-UAV" + i + " " + (119 + i * 2 * PathConflictDist) + " 48 " + (i * 100) + "  " + uavRefuelThreshold + " BLUEFOR\n");
                }
                out.writeBytes("SENSOR RSSI RSSI-UAV" + i + "\n");
            }

            for (int i = 0; i < noEOIRUAVs; i++) {
                if (uavPlacementRandom) {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + i + " RANDOM " + uavRefuelThreshold + " BLUEFOR\n");
                } else {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + i + " " + (151 + i * 2 * PathConflictDist) + " 34 100  " + uavRefuelThreshold + " BLUEFOR\n");
                }
                out.writeBytes("SENSOR EOIR EOIR-UAV" + i + "\n");
            }

            for (int i = 0; i < noArmoredUAVs; i++) {
                if (uavPlacementRandom) {
                    out.writeBytes("SMALL_UAV AUAV" + i + " RANDOM " + uavRefuelThreshold + " BLUEFOR\n");
                } else {
                    out.writeBytes("SMALL_UAV AUAV" + i + " " + (16119 + i * 2 * PathConflictDist) + " 38805 1000  " + uavRefuelThreshold + " BLUEFOR\n");
                }
                out.writeBytes("SENSOR SAR AUAV" + i + "\n");
            }

            for (int i = 0; i < noHumvees; i++) {

                    out.writeBytes("HMMWV H" + i + " " + (16119 + i * 500) + " 37805 0 BLUEFOR\n");
                    out.writeBytes("PROXY H" + i + " ON\n");

            }

            int infX = 25000;
            int infY = 25000;
            for (int i = 0; i < noOpforInfantry; i++) {
                out.writeBytes("INFANTRY DI-OP" + i + " " + infX + " " + infY + " 0 OPFOR\n");
                infX += 5;
            }

            infX = 25000;
            infY = 25100;
            for (int i = 0; i < noBlueforInfantry; i++) {
                out.writeBytes("INFANTRY DI-BLUE" + i + " " + infX + " " + infY + " 0  BLUEFOR\n");
                infX += 5;
            }

            if (null != EnvFileAdditionalLines) {
                Machinetta.Debugger.debug(1, "Have " + EnvFileAdditionalLines.length + " additional lines for Env.txt, writing.");
                for (int loopi = 0; loopi < EnvFileAdditionalLines.length; loopi++) {
                    out.writeBytes(EnvFileAdditionalLines[loopi]);
                    out.writeBytes("\n");
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public ProxyState _makeBeliefs() {
        ProxyState state = new ProxyState();
        state.removeAll();

        state.addBelief(team);

        return state;
    }

    public Associates makeAssociates(ProxyID self) {
        Associates ass = new Associates();
        // Possible number of associates is entire team minus ourselves
        int maxPossibleAssociates = team.members.size() - 1;
        //	Machinetta.Debugger.debug("Maximum possible number of associates is "+maxPossibleAssociates+", noAssociates is "+noAssociates,1,this);
        if (noAssociates > maxPossibleAssociates) {
            Machinetta.Debugger.debug("Maximum possible number of associates is " + maxPossibleAssociates + ", noAssociates is too large (" + noAssociates + "), lowering it to the max possible number.", 1, this);
            noAssociates = maxPossibleAssociates;
        }
        // System.out.println("Creating associates for : " + self);
        for (int i = 0; i < noAssociates; i++) {
            ProxyID other = null;
            do {
                other = team.members.get(rand.nextInt(team.members.size()));
            // System.out.println("Considering " + other);
            } while (self.equals(other) || ass.isAssociate(other));
            // System.out.println("Adding: " + other);
            ass.addAssociate(other);
        }
        return ass;
    }

    /**
     *
     * UAV initially knows about itself and the team.
     */
    public ProxyState _makeUAVBeliefs(String name, RAPBelief self) {
        ProxyState state = _makeBeliefs();

        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        return state;
    }

    public void makeUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeUAVBeliefs " + name, 1, this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVTDOACaps);

        ProxyState state = _makeUAVBeliefs(name, self);
        state.addBelief(self);

        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        // scanRole.params.put("Area", new Rectangle(10000*uavNo, 10000*uavNo, 20000, 20000));
        scanRole.params.put("Area", new AirSim.Environment.Area(0, 0, 50000, 50000));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);

        writeBeliefs(locs + name + ".blf", state);
    }

    public void makeEOIRUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeEOIRUAVBeliefs " + name, 1, this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVEOIRCaps);

        ProxyState state = _makeUAVBeliefs(name, self);

        state.addBelief(self);

        writeBeliefs(locs + name + ".blf", state);
    }

    public void makeRSSIUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs " + name, 1, this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVRSSICaps);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs ProxyState " + name, 1, this);

        ProxyState state = _makeUAVBeliefs(name, self);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs self " + name, 1, this);

        state.addBelief(self);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs writing " + name, 1, this);

        writeBeliefs(locs + name + ".blf", state);
        Machinetta.Debugger.debug("Done makeRSSIUAVBeliefs " + name, 1, this);
    }

    /**
     *
     * Armored UAV initially knows about itself and the team.
     */
    public void makeAUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeAUAVBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, AUAVCaps);
        state.addBelief(self);

        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        // Tell them about the detectors
        for (int i = 0; i < noDetectors; i++) {
            RAPBelief db = new RAPBelief(new NamedProxyID("Detector" + i), false, detectorCaps);
            state.addBelief(db);
        }

        // Tell them about the emitters
        for (int i = 0; i < noGeolocators; i++) {
            RAPBelief rb = new RAPBelief(new NamedProxyID("Geolocator" + i), false, geoLocateCaps);
            state.addBelief(rb);
        }

        // Tell them the plan for getting sensor readings checked
        state.addBelief(makeCheckSensorReadingTPT());

        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        scanRole.params.put("Area", new Rectangle(40000, 40000, 10000, 10000));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);

        writeBeliefs(locs + name + ".blf", state);
    }

    /**
     *
     * USG initially knows about itself and the team.
     *
     * It knows no plans
     */
    public void makeUGSBeliefs(String name) {
        Machinetta.Debugger.debug("makeUGSBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UGSCaps);
        state.addBelief(self);

        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        writeBeliefs(locs + name + ".blf", state);
    }

    /**
     *
     * Humvees initially knows about itself and the team.
     *
     * It knows no plans
     */
    public void makeHVBeliefs(String name) {

        Machinetta.Debugger.debug("makeHMMWVBeliefs ProxyState " + name, 1, this);
        // NOTE: _makeBeliefs() removes all other beliefs.  So don't
        // add anything before this.
        ProxyState state = _makeBeliefs();

        Machinetta.Debugger.debug("makeHMMWVBeliefs " + name, 1, this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, HVCaps);

        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        // Give them a role
        // Give them an initial move role
        BasicRole scanRole = new BasicRole(TaskType.move);
        scanRole.params = new Hashtable();
        // scanRole.params.put("Area", new Rectangle(10000*uavNo, 10000*uavNo, 20000, 20000));
        scanRole.params.put("Location", new Vector3D(0, 0, 500));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);

        Machinetta.Debugger.debug("makeHMMWVBeliefs self " + name, 1, this);
        state.addBelief(self);

        Machinetta.Debugger.debug("makeHMMWVBeliefs writing " + name, 1, this);
        writeBeliefs(locs + name + ".blf", state);
        Machinetta.Debugger.debug("Done makeHMMWVBeliefs " + name, 1, this);
    }

    /**
     *
     * Detector initially knows about itself and the team.
     *
     * It also knows the Geo-locate team plan template.
     */
    public void makeDetectorBeliefs(String name) {
        Machinetta.Debugger.debug("makeDetectorBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, detectorCaps);
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        state.addBelief(makeGeolocateTPT());

        writeBeliefs(locs + name + ".blf", state);
    }

    /**
     *
     * Geolocator initially knows about itself and the team.
     */
    public void makeGeolocatorBeliefs(String name) {
        Machinetta.Debugger.debug("makeGeolocatorBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, geoLocateCaps);
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        writeBeliefs(locs + name + ".blf", state);
    }

    /**
     *
     * Operator initially knows about itself and the team.
     */
    public void makeOperatorBeliefs(String name) {
        Machinetta.Debugger.debug("makeOperatorBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, operatorCaps);
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        // This is really only here to give something for FVRI to
        // print out.
        state.addBelief(makeGeolocateTPT());

        writeBeliefs(locs + name + ".blf", state);
    }

    /**
     *
     * TrafficController initially knows about itself and the team.
     */
    public void makeTrafficControllerBeliefs(String name) {
        Machinetta.Debugger.debug("makeTrafficControllerBeliefs " + name, 1, this);
        ProxyState state = _makeBeliefs();

        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, trafficControllerCaps);
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        writeBeliefs(locs + name + ".blf", state);
    }

    private void writeBeliefs(String fileName, ProxyState state) {
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
            BeliefID[] beliefs = state.getAllBeliefs();
            for (int i = 0; i < beliefs.length; i++) {
                Belief b = state.getBelief(beliefs[i]);
                if (b != null) {
                    os.writeObject(b);
                }
            }
            os.flush();
            os.close();
        } catch (FileNotFoundException ex) {
            Machinetta.Debugger.debug("Problem writing beliefs: " + ex, 5, this);
            ex.printStackTrace();
        } catch (IOException ex) {
            Machinetta.Debugger.debug("Problem writing beliefs: " + ex, 5, this);
            ex.printStackTrace();
        }
    }

    /**
     * Defines the plan template for Geolocate missions.
     *
     * @todo Define the plan template
     */
    public TeamPlanTemplate makeGeolocateTPT() {
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();

        //----------------------------------------------------------
        // No params for now

        //----------------------------------------------------------
        // Precondition is that there is an un"located" GeoLocateRequest
        Vector<String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("located");

        Vector<String> outKeys = new Vector<String>();
        outKeys.add("id");

        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest false", // String to be matched
                inKeys,
                outKeys,
                "request");  // This is the key to the belief in the role's param string
        Vector first = new Vector();
        first.add(cond);
        preconditions.add(first);

        //----------------------------------------------------------
        // Post condition is that location is known
        Vector<String> inKeysPost = new Vector<String>();
        inKeysPost.add("class");
        inKeysPost.add("located");

        Vector<String> outKeysPost = new Vector<String>();

        MatchCondition condPost = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest true", // String to be matched
                inKeysPost,
                outKeysPost,
                "request");  // This is the key to the belief in the role's param string
        Vector firstPost = new Vector();
        firstPost.add(condPost);
        postconditions.add(firstPost);

        //----------------------------------------------------------
        // Create the roles

        // One Geolocate Compute Role
        BasicRole computeRole = new BasicRole(TaskType.geolocateCompute);
        computeRole.infoSharing = new Vector<DirectedInformationRequirement>();
        try {
            computeRole.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.GeoLocateResult"), new NamedProxyID("Operator0")));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        roles.add(computeRole);

        //
        //
        //
        // Problem here is that role id for geolocate roles are changed after they are instantiated, hence this can't hook up!
        //
        // Does not appear to be a problem, see TeamPlanTemplate L:235        
        //
        //
        //


        // Geolocate sense roles
        for (int no = 0; no < 3; no++) {
            BasicRole basic = new BasicRole(TaskType.geolocateSense);
            basic.params = new Hashtable();
            basic.params.put("Label", "Role" + no);
            basic.params.put("ScanDataLabel", "TMAGLData" + no);

            basic.infoSharing = new Vector<DirectedInformationRequirement>();
            try {
                // Sets up requirement to send sensor data to geolocator
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.TMAScanResult"), computeRole.getID(), new BeliefNameID("TMAGLData" + no)));
                // Sets up the requirement to send location information to geolocator
                basic.infoSharing.add(new DirectedInformationRequirement(AirSim.Machinetta.Beliefs.UAVLocation.class, computeRole.getID()));
                // Sets up the requirement to send coordination commands out to geolocate sensing UAVs
                computeRole.infoSharing.add(new DirectedInformationRequirement(TDOACoordCommand.class, basic.getID()));
                Machinetta.Debugger.debug(1, "Added: " + TDOACoordCommand.class + " -> " + basic.getID());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            roles.add(basic);
        }

        //----------------------------------------------------------
        // No constraints for now

        // Finally, create the template
        TeamPlanTemplate tpt = new TeamPlanTemplate("Geolocate", team.getID(), params, preconditions, postconditions, roles, constraints);

        return tpt;
    }

    /**
     * Defines the plan template for discrimating signals
     *
     * @todo Define the plan template
     */
    public TeamPlanTemplate makeCheckSensorReadingTPT() {
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Vector preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();

        //----------------------------------------------------------
        // No params for now

        //----------------------------------------------------------
        // Precondition is that there is an unevaluated TMA sensor reading
        // (In a roundabout way this says that the field isDiscriminated of an
        // object of class AirSim.Machinetta.Beliefs.TMAScanResult must be false
        // for this template to be instantiated
        Vector<String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("isDiscriminated");
        inKeys.add("isGeolocateData");

        Vector<String> outKeys = new Vector<String>();
        outKeys.add("id");

        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.TMAScanResult false false", // String to be matched
                inKeys,
                outKeys,
                "sensorReading");
        Vector first = new Vector();
        first.add(cond);
        preconditions.add(first);
        //----------------------------------------------------------
        // No post conditions for now

        //----------------------------------------------------------
        // Create the roles

        // Role for providing the data, will be assigned to the UAV creating the plan
        BasicRole sensor = new BasicRole(TaskType.provideScanData);
        roles.add(sensor);

        // Role for processing the data
        BasicRole detector = new BasicRole(TaskType.emitterDiscriminate);
        roles.add(detector);

        sensor.infoSharing = new Vector<DirectedInformationRequirement>();

        // Tell proxy with sensor role to send the plan parameters to the proxy
        // with detector role
        // Constructor signature is bad and will change !
        sensor.infoSharing.add(new DirectedInformationRequirement("sensorReading", detector.getID()));

        //----------------------------------------------------------
        // No constraints for now

        // Finally, create the template
        TeamPlanTemplate tpt = new TeamPlanTemplate("CheckSensorReading", team.getID(), params, preconditions, postconditions, roles, constraints);

        return tpt;
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic UAV.
     */
    java.util.Hashtable<String, CapabilityBelief> UAVTDOACaps = null;

    {
        UAVTDOACaps = new Hashtable<String, CapabilityBelief>();
        UAVTDOACaps.put("scan", new CapabilityBelief("scan", 5));
        UAVTDOACaps.put("provideScanData", new CapabilityBelief("provideScanData", 0));
        UAVTDOACaps.put("geolocateSense", new CapabilityBelief("geolocateSense", 85));
        UAVTDOACaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    java.util.Hashtable<String, CapabilityBelief> UAVEOIRCaps = null;

    {
        UAVEOIRCaps = new Hashtable<String, CapabilityBelief>();
        UAVEOIRCaps.put("scan", new CapabilityBelief("scan", 5));
        UAVEOIRCaps.put("EOImage", new CapabilityBelief("EOImage", uavEOIR_EO_CAP_PERCENT));
        UAVEOIRCaps.put("IRImage", new CapabilityBelief("IRImage", uavEOIR_IR_CAP_PERCENT));
        UAVEOIRCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    java.util.Hashtable<String, CapabilityBelief> UAVRSSICaps = null;

    {
        UAVRSSICaps = new Hashtable<String, CapabilityBelief>();
        UAVRSSICaps.put("scan", new CapabilityBelief("scan", 5));
        UAVRSSICaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic AUAV.
     */
    java.util.Hashtable<String, CapabilityBelief> AUAVCaps = null;

    {
        AUAVCaps = new Hashtable<String, CapabilityBelief>();
        AUAVCaps.put("scan", new CapabilityBelief("scan", 5));
        AUAVCaps.put("provideScanData", new CapabilityBelief("provideScanData", 0));
        AUAVCaps.put("geolocateSense", new CapabilityBelief("geolocateSense", 85));
        AUAVCaps.put("attackFromAir", new CapabilityBelief("attackFromAir", 5));
        AUAVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic Detector.
     */
    java.util.Hashtable<String, CapabilityBelief> detectorCaps = null;

    {
        detectorCaps = new Hashtable<String, CapabilityBelief>();
        detectorCaps.put("emitterDiscriminate", new CapabilityBelief("emitterDiscriminate", 85));
        detectorCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic Humvee
     */
    java.util.Hashtable<String, CapabilityBelief> HVCaps = null;

    {
        HVCaps = new Hashtable<String, CapabilityBelief>();
        HVCaps.put("move", new CapabilityBelief("move", 85));
        HVCaps.put("hold", new CapabilityBelief("hold", 100));

        HVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic Geolocate agent.
     */
    java.util.Hashtable<String, CapabilityBelief> geoLocateCaps = null;

    {
        geoLocateCaps = new Hashtable<String, CapabilityBelief>();
        geoLocateCaps.put("geolocateCompute", new CapabilityBelief("geolocateCompute", 85));
        geoLocateCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic Geolocate agent.
     */
    java.util.Hashtable<String, CapabilityBelief> operatorCaps = null;

    {
        operatorCaps = new Hashtable<String, CapabilityBelief>();
        operatorCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a generic traffic controller agent.
     */
    java.util.Hashtable<String, CapabilityBelief> trafficControllerCaps = null;

    {
        trafficControllerCaps = new Hashtable<String, CapabilityBelief>();
        trafficControllerCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }
    /**
     * This hashtable contains a specification of the capabilities
     * of a Unattended Ground Sensor.  Since it just produces information
     * it has no capabilities.
     */
    java.util.Hashtable<String, CapabilityBelief> UGSCaps = null;

    {
        UGSCaps = new Hashtable<String, CapabilityBelief>();
        UGSCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * For creating config files for UAVs
     */
    private DataOutputStream _createUAVCfgFile(String name) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");

            out.writeBytes("TERRAIN_COSTMAP_LOCATION "+TERRAIN_COSTMAP_LOCATION+"\n");
            
            out.writeBytes("TIME_BETWEEN_FILTER_PANEL_UPDATES_MS 100\n");
            out.writeBytes("# New RRT params from Paul\n");
            out.writeBytes("EXPANSION_COST_FACTOR 8.0\n");
            out.writeBytes("EXPANSIONS 5000\n");
            out.writeBytes("NUM_EXPANSIONS 3\n");
            out.writeBytes("MOMENTUM_REWARD 1000.0\n");
            out.writeBytes("MIN_PATH_LENGTH " + RRTMinPathLength + "\n");
            out.writeBytes("# RRTCONFIG params\n");
            out.writeBytes("RRTCONFIG_NO_EXPANSIONS " + RRTCONFIG_NO_EXPANSIONS + "\n");
            out.writeBytes("RRTCONFIG_BRANCHES_PER_EXPANSION " + RRTCONFIG_BRANCHES_PER_EXPANSION + "\n");
            out.writeBytes("RRTCONFIG_MAX_THETA_CHANGE " + RRTCONFIG_MAX_THETA_CHANGE + "\n");
            out.writeBytes("RRTCONFIG_MAX_PSI_CHANGE " + RRTCONFIG_MAX_PSI_CHANGE + "\n");
            out.writeBytes("RRTCONFIG_MAX_BRANCH_LENGTH " + RRTCONFIG_MAX_BRANCH_LENGTH + "\n");
            out.writeBytes("RRTCONFIG_MIN_BRANCH_LENGTH " + RRTCONFIG_MIN_BRANCH_LENGTH + "\n");
            out.writeBytes("RRTCONFIG_RRT_BRANCH_RANGE_X_METERS " + RRTCONFIG_RRT_BRANCH_RANGE_X_METERS + "\n");
            out.writeBytes("RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS " + RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS + "\n");
            out.writeBytes("\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Used in AirSim/Machinetta/CostMaps/MixGaussiansCostMap as the\n");
            out.writeBytes("# default divisor for Gaussian costmaps, i.e. when calculating a\n");
            out.writeBytes("# gaussian's contribution to cost, we add\n");
            out.writeBytes("# gaussian.amplitude/(dist_from_point_squared/(divisor*divisor)).\n");
            out.writeBytes("# to the running total.  The size of the map may affect what this\n");
            out.writeBytes("# should be set too.\n");
            out.writeBytes("MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR 500\n");
            out.writeBytes("\n");
            out.writeBytes("# Speed in meters/second of the UAV - used for path planning so\n");
            out.writeBytes("# we can known times.  Can get from SmallUAV constructor that\n");
            out.writeBytes("# calls setSpeed.  (41.6666 is 150kph, 4.1666 is 15kph)\n");
            out.writeBytes("UAV_SPEED_METERS_PER_SEC " + uavMaxSpeedMetersPerSec + "\n");
            out.writeBytes("UAV_MAX_TURN_RATE_DEG " + uavMaxTurnRateDegPerStep + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The maximum amount that a vehicle may change its z-coordinate in one RRT branch\n");
            out.writeBytes("UAV_MAX_Z_CHANGE " + RRTMaxZChange + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Used in RRTPlanner to control generation of paths\n");
            out.writeBytes("RRT_PREFERRED_PATH_METERS " + RRTPreferredPathMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Used in RRTPlanner as hard limit length of paths returned.\n");
            out.writeBytes("RRT_MAX_PATH_METERS " + RRTMaxPath + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Used in RRTPlanner to control distance of each branch of the RRT tree\n");
            out.writeBytes("RRT_BRANCH_RANGE_X_METERS " + RRTBranchRangeX + "\n");
            out.writeBytes("RRT_BRANCH_RANGE_Y_METERS " + RRTBranchRangeY + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Minimum distance inside of which a RAP is considered to be 'at'\n");
            out.writeBytes("# the waypoint.  If this is set too small, then you see the UAVs\n");
            out.writeBytes("# 'dance' because they haven't received the next waypoint before\n");
            out.writeBytes("# they've passed the current one.  This was defaulted to 200m on\n");
            out.writeBytes("# the 50Kx50K map.\n");
            out.writeBytes("PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS " + PathPlannerAtWaypointTolerance + "\n");
            out.writeBytes("\n");
            out.writeBytes("# When a uav is less than this distance from the end of its path,\n");
            out.writeBytes("# start planning the next path.\n");
            out.writeBytes("PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH " + PathPlannerReplanDistFromEnd + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Use gradient descent smoothing on paths returned from RRTPlanner.\n");
            out.writeBytes("PATHPLANNER_SMOOTH_GRAD_DESC_ON " + PATHPLANNER_SMOOTH_GRAD_DESC_ON + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Redirect the path directly to the goal point.\n");
            out.writeBytes("PATHPLANNER_REDIRECT_PATH_TO_TARGET " + PATHPLANNER_REDIRECT_PATH_TO_TARGET + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Planned path TTL - when the UAV generates a planned path, this\n");
            out.writeBytes("# is the TTL associated with the information sent out - generally\n");
            out.writeBytes("# this should be set to about log(teamsize), more or less.\n");
            out.writeBytes("PATHPLANNER_PLANNED_PATH_TTL " + PathPlannerTTL + "\n");
            out.writeBytes("\n");
            out.writeBytes("# making it 5000 to encourage some conflicts actually happening,\n");
            out.writeBytes("# so we can detect them.\n");
            out.writeBytes("PATHDB_PATH_CONFLICT_MIN_DIST_METERS " + PathConflictDist + "\n");
            out.writeBytes("PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS " + PathConflictZDist + "\n");
            out.writeBytes("\n");
            out.writeBytes("# This turns on path deconfliction, i.e. the creation of a PathDB\n");
            out.writeBytes("# instance.\n");
            out.writeBytes("PATH_DECONFLICTION_ON " + uavPathDeconflictionOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Used in AA, for attack capability calculation - if distance (in\n");
            out.writeBytes("# meters) from target is > AA_MAX_DIST_CAP_METERS, then capability to\n");
            out.writeBytes("# fill role is 0.\n");
            out.writeBytes("AA_MAX_DIST_CAP_METERS 10000\n");
            out.writeBytes("\n");
            out.writeBytes("# The size of the 'cost' square placed around SA9's that are\n");
            out.writeBytes("# detected, in order to avoid being shot down.\n");
            out.writeBytes("SA9_COST_SQUARE_METERS " + SA9Cost + "\n");
            out.writeBytes("\n");
            out.writeBytes("# if this is true, then pathdb isn't going to\n");
            out.writeBytes("# have much work to do... so leave it false when testing pathdb.\n");
            out.writeBytes("OTHER_VEHICLE_COSTMAP_ON " + OtherVehicleCostmapOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The range to other vehicle paths for the OtherVehicleCostMap.  Costs\n");
            out.writeBytes("# are scaled from 0.0 or less (at or beyond range) up to 1.0, for a\n");
            out.writeBytes("# point being near another vehicles path, and then scaled up by 100.0.\n");
            out.writeBytes("OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS " + OtherVehicleCostMapAvoid + "\n");
            out.writeBytes("\n");
            out.writeBytes("# This is kind of an override value - any closer than this and we\n");
            out.writeBytes("# return a very high cost, to sorta say 'you're going to collide\n");
            out.writeBytes("# and die if you get this close'\n");
            out.writeBytes("OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS " + OtherVehicleCostMapConflict + "\n");
            out.writeBytes("OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS " + OtherVehicleCostMapZConflict + "\n");
            out.writeBytes("\n");
            if (noOperators > 0) {
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON true\n");
            } else {
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON false\n");
            }
            if (noTrafficControllers > 0) {
                out.writeBytes("SIM_TRAFFIC_CONTROLLER_DIRECTED_INFO_REQ_ON true\n");
            } else {
                out.writeBytes("SIM_TRAFFIC_CONTROLLER_DIRECTED_INFO_REQ_ON false\n");
            }
            if (UAV_DYNAMIC_FLY_ZONES) {
                out.writeBytes("DYNAMIC_FLY_ZONES true\n");
            } else {
                out.writeBytes("DYNAMIC_FLY_ZONES false\n");
            }

            out.writeBytes("ENTROPY_PANEL_ON " + uavEntropyPanelOn + "\n");
            out.writeBytes("ENTROPY_PANEL_X 740\n");
            out.writeBytes("ENTROPY_PANEL_Y 20\n");
            out.writeBytes("RRTPATH_PANEL_ON " + uavRRTPathPanelOn + "\n");
            out.writeBytes("RRTPATH_PANEL_X 0\n");
            out.writeBytes("RRTPATH_PANEL_Y 540\n");
            out.writeBytes("RRTTREE_PANEL_ON " + uavRRTTreePanelOn + "\n");
            out.writeBytes("RRTTREE_PANEL_X 780\n");
            out.writeBytes("RRTTREE_PANEL_Y 540\n");

            return out;

        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }

        return null;
    }

    public void createUAVCfgFile(String name) {
        Machinetta.Debugger.debug("createUAVCfgFile " + name, 1, this);
        DataOutputStream out = _createUAVCfgFile(name);

        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    int bayesPanelX = 0;
    int bayesPanelY = 20;

    public void createRSSIUAVCfgFile(String name) {
        Machinetta.Debugger.debug("createRSSIUAVCfgFile " + name, 1, this);
        DataOutputStream out = _createUAVCfgFile(name);

        try {
            out.writeBytes("BAYES_FILTER_PANEL_ON " + uavBinaryBayesFilterPanelOn + "\n");
            out.writeBytes("BAYES_FILTER_PANEL_X " + bayesPanelX + "\n");
            out.writeBytes("BAYES_FILTER_PANEL_Y " + bayesPanelY + "\n");
            bayesPanelX += 780;
            if (bayesPanelX >= 1200) {
                bayesPanelY += 524;
                bayesPanelX = 0;
            }

            out.writeBytes("\n");
            out.writeBytes(COSTMAPS_COMMENT);
            out.writeBytes("BINARY_BAYES_FILTER_ON " + uavBinaryBayesFilterOn + "\n");
            out.writeBytes("RANDOM_ENTIRE_MAP_COSTMAP_ON " + uavRandomEntireMapCostmapOn + "\n");
            out.writeBytes("RANDOM_SMALL_MOVES_COSTMAP_ON " + uavRandomSmallMovesCostmapOn + "\n");
            out.writeBytes("RANDOM_SMALL_MOVES_RANGE " + uavRandomSmallMovesRange + "\n");
            out.writeBytes("RANDOM_CLUSTERS_COSTMAP_ON " + uavRandomClustersCostmapOn + "\n");
            out.writeBytes("RANDOM_CLUSTERS_RANGE " + uavRandomClustersRange + "\n");
            out.writeBytes("RANDOM_CLUSTERS_COUNT " + uavRandomClustersCount + "\n");
            out.writeBytes(COSTMAPS_RANDOM_GUASSIANS_COMMENT);
            out.writeBytes("RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER " + uavRandomGuassiaAmplitudeMultiplier + "\n");
            out.writeBytes("RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER " + uavRandomGuassianDivisorMultiplier + "\n");
            out.writeBytes("\n");

            out.writeBytes("# Controls the range that each RSSI sensor reading has, in the\n");
            out.writeBytes("# BinaryBayesFilter, i.e. how far away from the reading location do we\n");
            out.writeBytes("# update our binary bayes probabilities.\n");
            out.writeBytes("BBF_SENSOR_MAX_RANGE_METERS " + BBFSensorMaxRange + "\n");
            out.writeBytes("BBF_SENSOR_MIN_RANGE_METERS " + BBFSensorMinRange + "\n");
            out.writeBytes("\n");
            out.writeBytes("# the scaling factor to go from map coords to indices into the\n");
            out.writeBytes("# bayes filter probability array - and also to figure out what\n");
            out.writeBytes("# size array to use, based on map size.\n");
            out.writeBytes("BBF_GRID_SCALE_FACTOR " + BBFGridScaleFactor + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The prior value for the occupancyMap array in the binary bayes\n");
            out.writeBytes("# filter.  \n");
            //out.writeBytes("BBF_UNIFORM_PRIOR " + Math.exp(0.001) + "\n");
            out.writeBytes("BBF_UNIFORM_PRIOR 0.01\n");
            out.writeBytes("\n");
            out.writeBytes("# to turn off diffusion, set DIFFUSE_DELTA to 0.  \n");
            out.writeBytes("BBF_DIFFUSE_INTERVAL_MS " + diffusionInterval + "\n");
            out.writeBytes("BBF_DIFFUSE_DELTA " + diffusionRate + "\n");
            out.writeBytes("\n");
            out.writeBytes("# These control the manner in which the\n");
            out.writeBytes("# BinaryBayesianFilterCostMap decides which RSSI readings to\n");
            out.writeBytes("# share with its teammates - it either uses random sharing\n");
            out.writeBytes("# (i.e. share some random percentage to the time) or it uses\n");
            out.writeBytes("# KLDivergence to set the TTL on all (local or nonlocal) RSSI\n");
            out.writeBytes("# readings it gets, i.e. it repropagates RSSI Readings from other\n");
            out.writeBytes("# UAVs as well as tis own.\n");
            out.writeBytes("BBF_RANDOM_SHARING_ON " + bbfRandomSharingOn + "\n");
            out.writeBytes("BBF_RANDOM_SHARING_PROB " + bbfRandomSharingProb + "\n");
            out.writeBytes("BBF_RANDOM_SHARING_TTL " + bbfRandomSharingTtl + "\n");
            out.writeBytes("BBF_KLD_SHARING_ON " + bbfKLDSharingOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Should we track remotely sensed readings in a separate bayesian\n");
            out.writeBytes("# filter, and use that to decide sharing?  (If this is true,\n");
            out.writeBytes("# BBF_RANDOM_SHARING_ON and BBF_KLD_SHARING_ON should be false.)\n");
            out.writeBytes("BBF_REMOTE_KLD_SHARING_ON " + bbfRemoteKLDSharingOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The alpha that is used to calculate expected signal strength in\n");
            out.writeBytes("# BinaryBayesFitler.\n");
            out.writeBytes("BBF_RSSI_ALPHA 10000.0\n");
            out.writeBytes("# These are parameters for the emitter signal computations \n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_COEFFICIENT 239400\n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_INCREMENT -70\n");
            out.writeBytes("EMITTER_NOISE_MODEL_STD  1.0\n");

            out.writeBytes("\n");
            if (null != UAVCfgAdditionalLines) {
                Machinetta.Debugger.debug(1, "Have " + UAVCfgAdditionalLines.length + " additional lines for UAV cfg, writing.");
                for (int loopi = 0; loopi < UAVCfgAdditionalLines.length; loopi++) {
                    out.writeBytes(UAVCfgAdditionalLines[loopi]);
                    out.writeBytes("\n");
                }
            }

        } catch (IOException e) {
            Machinetta.Debugger.debug("RSSI cfg failed: " + e, 1, this);
        }

        try {
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void createEOIRUAVCfgFile(String name) {
        Machinetta.Debugger.debug("createEOIRUAVCfgFile " + name, 1, this);
        DataOutputStream out = _createUAVCfgFile(name);


        try {
            out.writeBytes("EO_DISTANCE_TOLERANCE " + uavEODistanceTolerance + "\n");

            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void createHVCfgFile(String name) {
        Machinetta.Debugger.debug("createHVCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.HVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.HVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for UGSs
     */
    public void createUGSCfgFile(String name) {
        Machinetta.Debugger.debug("createUGSCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UGSAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UGSRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for Detector agents
     */
    public void createDetectorCfgFile(String name) {
        Machinetta.Debugger.debug("createDetectorCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.DetectorAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.DetectorRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for Geolocate agents
     */
    public void createGeolocatorCfgFile(String name) {
        Machinetta.Debugger.debug("createGeolocatorCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.GeoLocateAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.GeoLocateRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for Operators
     */
    public void createOperatorCfgFile(String name) {
        Machinetta.Debugger.debug("createOperatorCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");
            out.writeBytes("BAYES_FILTER_PANEL_ON " + operBinaryBayesFilterPanelOn + "\n");
            out.writeBytes("BAYES_FILTER_PANEL_X 0\n");
            out.writeBytes("BAYES_FILTER_PANEL_Y 20\n");
            out.writeBytes("\n");
            if (OPERATOR_CLUSTERING_ON) {
                out.writeBytes("CLUSTERING_ON true\n");
            } else {
                out.writeBytes("CLUSTERING_ON false\n");
            }
            out.writeBytes("\n");
            out.writeBytes("CTDB_BASE_NAME "+getCtdbBaseName()+"\n");

            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Commander.AA\n");
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.SimOperatorRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");

            out.writeBytes("# Controls the range that each RSSI sensor reading has, in the\n");
            out.writeBytes("PATHDB_PATH_CONFLICT_MIN_DIST_METERS " + PathConflictDist + "\n");
            out.writeBytes("PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS " + PathConflictZDist + "\n");
            out.writeBytes("\n");
            out.writeBytes("# This turns on path deconfliction, i.e. the creation of a PathDB\n");
            out.writeBytes("# instance.\n");
            out.writeBytes("PATH_DECONFLICTION_ON " + operPathDeconflictionOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# BinaryBayesFilter, i.e. how far away from the reading location do we\n");
            out.writeBytes("# update our binary bayes probabilities.\n");
            out.writeBytes("BINARY_BAYES_FILTER_ON " + operBinaryBayesFilterOn + "\n");
            out.writeBytes("BBF_SENSOR_MAX_RANGE_METERS " + BBFSensorMaxRange + "\n");
            out.writeBytes("BBF_SENSOR_MIN_RANGE_METERS " + BBFSensorMinRange + "\n");
            out.writeBytes("\n");
            out.writeBytes("# the scaling factor to go from map coords to indices into the\n");
            out.writeBytes("# bayes filter probability array - and also to figure out what\n");
            out.writeBytes("# size array to use, based on map size.\n");
            out.writeBytes("BBF_GRID_SCALE_FACTOR " + BBFGridScaleFactor + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The prior value for the occupancyMap array in the binary bayes\n");
            out.writeBytes("# filter.  \n");
            // out.writeBytes("BBF_UNIFORM_PRIOR " + Math.exp(0.001) + "\n");
            out.writeBytes("BBF_UNIFORM_PRIOR 0.01\n");
            out.writeBytes("\n");
            out.writeBytes("# to turn off diffusion, set DIFFUSE_DELTA to 0.  \n");
            out.writeBytes("BBF_DIFFUSE_INTERVAL_MS " + diffusionInterval + "\n");
            out.writeBytes("BBF_DIFFUSE_DELTA " + diffusionRate + "\n");
            out.writeBytes("\n");
            out.writeBytes("# These control the manner in which the\n");
            out.writeBytes("# BinaryBayesianFilterCostMap decides which RSSI readings to\n");
            out.writeBytes("# share with its teammates - it either uses random sharing\n");
            out.writeBytes("# (i.e. share some random percentage to the time) or it uses\n");
            out.writeBytes("# KLDivergence to set the TTL on all (local or nonlocal) RSSI\n");
            out.writeBytes("# readings it gets, i.e. it repropagates RSSI Readings from other\n");
            out.writeBytes("# UAVs as well as tis own.\n");
            out.writeBytes("BBF_RANDOM_SHARING_ON " + bbfRandomSharingOn + "\n");
            out.writeBytes("BBF_RANDOM_SHARING_PROB " + bbfRandomSharingProb + "\n");
            out.writeBytes("BBF_RANDOM_SHARING_TTL " + bbfRandomSharingTtl + "\n");
            out.writeBytes("BBF_KLD_SHARING_ON " + bbfKLDSharingOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# Should we track remotely sensed readings in a separate bayesian\n");
            out.writeBytes("# filter, and use that to decide sharing?  (If this is true,\n");
            out.writeBytes("# BBF_RANDOM_SHARING_ON and BBF_KLD_SHARING_ON should be false.)\n");
            out.writeBytes("BBF_REMOTE_KLD_SHARING_ON " + bbfRemoteKLDSharingOn + "\n");
            out.writeBytes("\n");
            out.writeBytes("# The alpha that is used to calculate expected signal strength in\n");
            out.writeBytes("# BinaryBayesFitler.\n");
            out.writeBytes("BBF_RSSI_ALPHA 10000.0\n");
            out.writeBytes("# These are parameters for the emitter signal computations \n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_COEFFICIENT 239400\n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_INCREMENT -70\n");
            out.writeBytes("EMITTER_NOISE_MODEL_STD  1.0\n");
            out.writeBytes("\n");

            out.writeBytes("LOCAL_BBGF_DISPLAY true");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for TrafficController
     */
    public void createTrafficControllerCfgFile(String name) {
        Machinetta.Debugger.debug("createTrafficControllerCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES false\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");

            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Commander.AA\n");
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.DynaZonesRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");

            out.writeBytes("\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
}
