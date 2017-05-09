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
 * ACCASTCreate.java
 *
 * Created on February 7, 2006, 5:24 PM
 *
 * SEE INLINE COMMENTS regarding various final static globals.
 *
 */
package AirSim.Configs;

import AirSim.UnitIDs;
import AirSim.Environment.Area;
import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Environment.Assets.Tasks.TaskType;
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
import Machinetta.State.BeliefType.TeamBelief.Constraints.ANDConstraint;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.Constraints.SequenceConstraint;
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
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class ACCASTCreate1 {

    public static final String SEP = File.separator;

    //
    // START CONFIGURING HERE
    //
    //
    // SET _ONE_ and _ONLY_ONE_ OF THE configureFor VARIABLES to true.
    //
    // Demo to accast Mon Apr 16 23:04:40 EDT 2007
    public static boolean configureFor5by5AccastDemo = true;
    public static boolean configureFor5by5AccastDemoFortBenning = false;

    public static String CTDB_BASE_DIR = "../../grd/";
    public static String CTDB_DEFAULT = "sanjaya001_0_0_50000_50000_050";
    public static String CTDB_AUSTIN5 = "austin_0_0_5000_5000_005";
    public static String CTDB_AUSTIN50 = "austin_0_0_50000_50000_050";
    public static String CTDB_GASCOLA = "gascola_0_0_5000_5000_005";
    public static String CTDB_FILE = CTDB_AUSTIN5;

    public static String getCtdbBaseName() {
        return CTDB_BASE_DIR + SEP + CTDB_FILE;
    }

    public final static String MASTER_DISPLAY_MASTER = ":0.0";

    public static HashMap<String, UserConfig> perUserMap = new HashMap<String, UserConfig>();

    public static void createUserConfigs() {

        UserConfig uc;

        uc = new UserConfig();
        uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
        uc.CTDB_FILE = CTDB_FILE;
        uc.TERRAIN_COSTMAP_LOCATION = "/usr1/grd/TerrainCM";
        String BASEDIR_SANJAYA = "/home/sanjaya/src/";
        uc.MASTER_CLASSPATH = locs + "sanjaya.jar";
        uc.SLAVE_CLASSPATH = locs + "sanjaya.jar";
// 	String[] SANJAYA_SLAVE_MACHINES = {"kappa","zeta"};
// 	uc.MASTER_MACHINE = "iota";
        String[] SANJAYA_SLAVE_MACHINES = {"128.2.181.150", "128.2.181.147"};
        uc.MASTER_MACHINE = "128.2.181.149";
        uc.SLAVE_MACHINES = SANJAYA_SLAVE_MACHINES;
        uc.MASTER_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
        uc.SLAVE_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
//	uc.MASTER_DISPLAY = ":0";
//	uc.SLAVE_DISPLAY = ":0";
        uc.SYNCHRONIZATION_SCRIPT = "/home/sanjaya/sync.sh";
        uc.OUTPUT_TO_CUR_DIR = false;
        uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
        uc.JAVA = "java -server";
        //	uc.JAVA = "java -server -Dcom.sun.management.jmxremote=true";
        perUserMap.put("sanjaya", uc);

        uc = new UserConfig();
        uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
        uc.CTDB_FILE = CTDB_FILE;
        uc.TERRAIN_COSTMAP_LOCATION = "../../TerrainCM";
        uc.MASTER_CLASSPATH = "../../AirSim/dist/AirSim.jar::../../AirSim/dist/lib/Gui.jar:../../AirSim/dist/lib/Machinetta.jar:../../AirSim/dist/lib/Util.jar:../../AirSim/dist/lib/log4j-1.2.15.jar:../../AirSim/dist/lib/swing-layout-1.0.jar";
        // uc.MASTER_CLASSPATH = "/Users/pscerri/NetBeansProjects/NRECAirSim/dist/AirSim.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/Gui.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/Machinetta.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/Util.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/log4j-1.2.15.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/swing-layout-1.0.jar";
        // uc.MASTER_CLASSPATH = "/Users/pscerri/NetBeansProjects/NRECAirSim/dist/NRECAirSim.jar::/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/log4j-1.2.15.jar:/Users/pscerri/NetBeansProjects/NRECAirSim/dist/lib/swing-layout-1.0.jar";
        uc.SLAVE_CLASSPATH = "/home/pscerri/Code/swing-layout-1.0.jar:/home/pscerri/Code/classes";
        uc.MASTER_MACHINE = "localhost";
        String[] PSCERRI_SLAVE_MACHINES = {};
        uc.SLAVE_MACHINES = PSCERRI_SLAVE_MACHINES;
        uc.MASTER_DISPLAY = null;
        uc.SLAVE_DISPLAY = "localhost:0";
        uc.SYNCHRONIZATION_SCRIPT = "/usr0/pscerri/Code/scripts/syncall.sh";
        uc.OUTPUT_TO_CUR_DIR = false;
        uc.OUTPUT_DIR_PREFIX = "../../logs";
        uc.JAVA = "java -server";
        perUserMap.put("darpacode", uc);

    }

    public static void setUserConfig(String username) {
        UserConfig uc = perUserMap.get(username);
        if (null == uc) {
            System.err.println("Unknown USER '" + username + "', unable to configure.  Add user configurations for this user to createUserConfigs");
            System.exit(1);
        }

        CTDB_BASE_DIR = uc.CTDB_BASE_DIR;
        CTDB_FILE = uc.CTDB_FILE;
        TERRAIN_COSTMAP_LOCATION = uc.TERRAIN_COSTMAP_LOCATION;
        MASTER_CLASSPATH = uc.MASTER_CLASSPATH;
        SLAVE_CLASSPATH = uc.SLAVE_CLASSPATH;
        MASTER_MACHINE = uc.MASTER_MACHINE;
        SLAVE_MACHINES = uc.SLAVE_MACHINES;
        MASTER_DISPLAY = uc.MASTER_DISPLAY;
        SLAVE_DISPLAY = uc.SLAVE_DISPLAY;
        SYNCHRONIZATION_SCRIPT = uc.SYNCHRONIZATION_SCRIPT;
        OUTPUT_TO_CUR_DIR = uc.OUTPUT_TO_CUR_DIR;
        OUTPUT_DIR_PREFIX = uc.OUTPUT_DIR_PREFIX;
        OUTPUT_DIR_PREFIXS = uc.OUTPUT_DIR_PREFIX + SEP;
        JAVA = uc.JAVA;
    }

    // The path and full filename of the terrain cost map.  (@TODO:
    // Need to check where the code is to create these cost maps from
    // terrain databases, and document that here.)
    public static String TERRAIN_COSTMAP_LOCATION;

    // MASTER_CLASSPATH is used to specify the classpath when running
    // on the master machine, which typically includes the Sanjaya
    // simulator, the SimOperator, and the UDP switch.
    public final static String BASEDIR_SEAN = "/afs/cs.cmu.edu/user/owens/camra/src/";
    public final static String MASTER_CLASSPATH_SEAN = BASEDIR_SEAN + ":" + BASEDIR_SEAN + "Util-src:" + BASEDIR_SEAN + "Gui-src:" + BASEDIR_SEAN + "Machinetta-src:" + BASEDIR_SEAN + "AirSim-src:" + BASEDIR_SEAN + "swing-layout-1.0.jar";
    public final static String MASTER_CLASSPATH_PAUL = "/usr0/pscerri/Code/swing-layout-1.0.jar:/usr0/pscerri/Code/AirSim/build/classes:/usr0/pscerri/Code/Util/build/classes/:/usr0/pscerri/Code/Machinetta/build/classes:/usr0/pscerri/Code/SanjayaGUI/build/classes";
    public final static String MASTER_CLASSPATH_DAVE = "/home/dscerri/Code/swing-layout-1.0.jar:/home/dscerri/Code/AirSim/build/classes:/home/dscerri/Code/Util/build/classes/:/home/dscerri/Code/Machinetta/build/classes:/home/dscerri/Code/SanjayaGUI/build/classes";

    public static String MASTER_CLASSPATH;

    // SLAVE_CLASSPATH is used to specify the classpath when running
    // on one of the slave machines, i.e. the various proxies.
    public static String SLAVE_CLASSPATH;

    // The master system, on which we run the Sanjaya wargame, the
    // SimOperator, and the UDP Switch.
    public static String MASTER_MACHINE;

    // The slave systems on which we run the various proxies.
    public static String[] SLAVE_MACHINES;

    // If non null, sets the xwindows display for master machines scripts
    public static String MASTER_DISPLAY;

    // If non null, sets the xwindows display for slave machines scripts
    public static String SLAVE_DISPLAY;

    // path+fullname of a script to run on the master system to
    // synchronize any code changes out to the slave systems.
    public static String SYNCHRONIZATION_SCRIPT;

    // Where do we write our output logs?  If OUTPUT_TO_CUR_DIR is
    // true, then write to currentdir/output.  If false then build a
    // path using OUTPUT_DIR_PREFIX and 'loc'. (The 'loc' variable is
    // specified as a command line param.)
    public static String OUTPUT_DIR = "output";
    public static String OUTPUT_DIRS = "output" + SEP;
    public static boolean OUTPUT_TO_CUR_DIR = false;
    public static String OUTPUT_DIR_PREFIX;
    public static String OUTPUT_DIR_PREFIXS;

    // Where is the java binary? what flags do we want?
    public final static String JAVA_SEAN = "java -server";
    public final static String JAVA_PAUL = "java -server";
    public final static String JAVA_DAVE = "java -server";

    public static String JAVA;

    //Only for Dave, for running master on Windows. Should be false for everyone else
    public final static boolean CONFIG_FOR_WIN_DAVE = false;

    // Emitters.
    public static boolean randomEmitterLocations = false;
    public static int emitterCount;
    public static String[] bigMapEmitterLocations = {"25532 25440", "40134 10325", "5342 29343"};
    public static String[] smallMapEmitterLocations = {"500 500", "802 206", "106 586"};
    public static String[] emitterLocations = bigMapEmitterLocations;
    public static boolean emitterChannels = false;

    // If true, the number and location of the emitters is added to
    // various debugging GUIs on the UAV proxies.  This information is
    // used only to display crosses on the GUI at each emitter
    // location, for human viewers, and is not made available by the
    // UAV 'intelligence'.
    public final static boolean ADD_EMITTER_LOCS_TO_UAV_CFG = true;

    public final static String SANJAYA_UPDATE_RATE_COMMENT
            = "# The update rate is the number of ms that sanjay sleeps between each\n"
            + "# 'step' of the simulator.  By default the simulator 'steps' ten times\n"
            + "# per second, so an update rate of 50 means the simulator simulates\n"
            + "# second of time every .5 seconds or so.\n";
    public static int sanjayaUpdateRate = 100;

    public final static String SANJAYA_ASSET_COLLISION_COMMENT
            = "# Sanjaya has code that checks for collision between SmallUAVs,\n"
            + "# where 'collision' is defined as 'coming within some range n of\n"
            + "# each other'.  These flags control whether or not this code runs\n"
            + "# and what it uses for range.  When we use smaller maps (1km x\n"
            + "# 1km) and slower UAVs, collision range should accordingly be\n"
            + "# reduced.\n";
    public static boolean sanjayaAssetCollisionDetectionOn = true;
    public static double sanjayaAssetCollisionRangeMeters = 2000;
    public static double sanjayaAssetCollisionMinZDistMeters = 100;

    public final static String COSTMAPS_COMMENT
            = "# In general, only one of the following costmaps should be on.\n"
            + "# The RSSI UAVs use a Binary Bayes Filter to locate targets.\n"
            + "# They generate an entropy map from the filter to use as a\n"
            + "# costmap in planning their paths to cover regions where\n"
            + "# uncertainty still exists.  If the Binary Bayes Filter is not\n"
            + "# set on, one of the other random gaussian costmaps below is used\n"
            + "# instead.\n";

    public final static String USE_XPLANE_CAMERA_COMMENT
            = "# Turn on sending of uav locations to xplane via UDP.\n";
    public static boolean USE_XPLANE_CAMERA = false;
    public static String USE_XPLANE_CAMERA_HOST = "128.2.181.147";

    public final static String END_CONDITION_COMMENT
            = "# These end conditions are checked every step in the simulator,\n"
            + "# and if any them are true, the simulator exits.  This is to\n"
            + "# facilitate batch experiments.  CAPTURE_THE_FLAG checks for \n"
            + "# OPFOR ground vehicles inside a BLUEFOR 'flag zone' in the lower\n"
            + "# left 5km of the map, and for BLUEFOR ground vehicles inside\n"
            + "# the OPFOR 'flag zone' in the upper right 5km of the map.\n"
            + "# ALL_EMITTERS_DETECTED checks that all assets that implement\n"
            + "# the Emitter interface have been 'detected', i.e. by a SmallAUV\n"
            + "# receiving a CAMERA_COMMAND while within 1km of the emitter.\n";
    public static boolean END_CONDITION_CAPTURE_THE_FLAG = false;
    public static boolean END_CONDITION_ALL_HMMMVS_DEAD = false;
    public static boolean END_CONDITION_ALL_OPFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_BLUEFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_EMITTERS_DETECTED = false;
    public static boolean END_CONDITION_ALL_INFANTRY_DEAD = true;
    public static boolean END_CONDITION_ALL_GOALS_HELD = true;

    public static boolean uavBinaryBayesFilterOn = true;
    public static boolean uavRandomEntireMapCostmapOn = false;
    public static boolean uavRandomSmallMovesCostmapOn = false;
    public static double uavRandomSmallMovesRange = 5000;
    public static boolean uavRandomClustersCostmapOn = false;
    public static double uavRandomClustersRange = 3000;
    public static int uavRandomClustersCount = 3;
    public final static String COSTMAPS_RANDOM_GUASSIANS_COMMENT
            = "# The random gaussian params are used by all of the random costmaps.\n"
            + "# The amplitude multiplier is multiplied by a random number\n"
            + "# from 0 to 1.0.  The divisor multiplier is multiplied by a\n"
            + "# random integer from 5 to 14.\n";
    public static double uavRandomGuassiaAmplitudeMultiplier = 500;
    public static double uavRandomGuassianDivisorMultiplier = 100;

    public static boolean operBinaryBayesFilterOn = false;
    public static boolean operBinaryBayesFilterPanelOn = false;
    public static boolean operOneKmGridLinesOn = false;
    public static boolean sanjayaOneKmGridLinesOn = true;
    public static boolean uavBinaryBayesFilterPanelOn = false;
    public static boolean uavBinaryBayesFilterPanelFirstFourOn = false;
    public static boolean uavEntropyPanelOn = false;
    public static boolean uavRRTPathPanelOn = false;
    public static boolean uavRRTTreePanelOn = true;

    public static int uavEODistanceTolerance = 100;
    public static int uavEOIR_EO_CAP_PERCENT = 25;
    public static int uavEOIR_IR_CAP_PERCENT = 25;

    public static String loc = ".";
    public static String locs = "./";
    public static boolean uavPlacementRandom = false;
    public static int mapWidthMeters = 50000;
    public static int mapHeightMeters = 50000;
    public static double mapLowerLeftLatGascola = 40.4563444444;
    public static double mapLowerLeftLonGascola = -79.789486111;
    public static double mapLowerLeftLatPittsburgh1 = 40.2159371876;
    public static double mapLowerLeftLonPittsburgh1 = -80.2824944544;
    public static double mapLowerLeftLatSanjaya001 = 40.138055555555555554;
    public static double mapLowerLeftLonSanjaya001 = -80.287499999999999999;
    public static double mapLowerLeftLatAustinAccast5 = 30.4096071468;
    public static double mapLowerLeftLonAustinAccast5 = -97.4604290251;
    public static double mapLowerLeftLatAustinAccast50 = 30.2067464686;
    public static double mapLowerLeftLonAustinAccast50 = -97.6947652517;
    public static double mapLowerLeftLat = -1;
    public static double mapLowerLeftLon = -1;
    public static double uavMaxSpeedMetersPerSec = 41.666;
    public static double uavMaxTurnRateDegPerStep = 30;
    public static double uavAtLocationDistance = 150;
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
    public static int noUAVs = 0;
    public static int noCivilians = 0;
    public static int noRSSIUAVs = 0;
    public static int noEOIRUAVs = 0;
    public static int noArmoredUAVs = 4;
    public static double uavRefuelThreshold = 2000;
    public static int noOperators = 1;
    public static int noSimUsers = 0;
    public static int noFalconView = 0;
    public static int noUGSs = 0;
    public static int noHumvees = 0;

    public static int noBlueforDIs = 0;
    public static int noIntelligentMines = 0;
    public static int noAUGVs = 0;
    public static int noC130s = 0;
    public static String switchRawPacketLogFilename = null;

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

    public static int simMemoryMegabytes = 800;
    public static int defaultProxyMemoryMegabytes = 400;
    public static int operProxyMemoryMegabytes = 256;
    public static int falconViewProxyMemoryMegabytes = 256;
    public static int simUserProxyMemoryMegabytes = 256;
    public static int uavProxyMemoryMegabytes = 400;
    public static int augvProxyMemoryMegabytes = 400;
    public static int diProxyMemoryMegabytes = 200;
    public static int humveeProxyMemoryMegabytes = 200;
    public static int imProxyMemoryMegabytes = 100;
    public static int ugsProxyMemoryMegabytes = 100;
    public static int c130ProxyMemoryMegabytes = 200;

    public static String simXmx = "-Xmx" + simMemoryMegabytes + "m";
    public static String defaultProxyXmx = "-Xmx" + defaultProxyMemoryMegabytes + "m";
    public static String operProxyXmx = "-Xmx" + operProxyMemoryMegabytes + "m";
    public static String falconViewProxyXmx = "-Xmx" + falconViewProxyMemoryMegabytes + "m";
    public static String simUserProxyXmx = "-Xmx" + simUserProxyMemoryMegabytes + "m";
    public static String uavProxyXmx = "-Xmx" + uavProxyMemoryMegabytes + "m";
    public static String augvProxyXmx = "-Xmx" + augvProxyMemoryMegabytes + "m";
    public static String diProxyXmx = "-Xmx" + diProxyMemoryMegabytes + "m";
    public static String humveeProxyXmx = "-Xmx" + humveeProxyMemoryMegabytes + "m";
    public static String imProxyXmx = "-Xmx" + imProxyMemoryMegabytes + "m";
    public static String ugsProxyXmx = "-Xmx" + ugsProxyMemoryMegabytes + "m";
    public static String c130ProxyXmx = "-Xmx" + c130ProxyMemoryMegabytes + "m";

    public TeamBelief team = null;

    public boolean useAssociates = true;
    public int noAssociates = 3;

    public static boolean UAV_DYNAMIC_FLY_ZONES = true;

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

    public static double blueforDIDamage = .2;

    public static String[] rssiUavLocations = null;

    public static boolean GUI_ON = true;
    public static double PLAN_CREATION_DROP_PROBABILITY = -1;
    public static double PLAN_CREATION_REACTION_TIME_MS = 0;
    public static boolean PLAN_CREATION_AUAV_ATTACK_DI = false;
    public static boolean PLAN_CREATION_AUGV_ATTACK_DI = false;
    public static boolean PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = false;

    public static int DEBUG_LEVEL = 1;

    public static boolean USE_JAMVM = false;

    public static boolean SMALL_ACCAST_SCENARIO = true;
    //
    // STOP CONFIGURING HERE
    //

    private static class UserConfig {

        public String CTDB_BASE_DIR;
        public String CTDB_FILE;
        public String TERRAIN_COSTMAP_LOCATION;
        public String MASTER_CLASSPATH;
        public String SLAVE_CLASSPATH;
        public String MASTER_MACHINE;
        public String[] SLAVE_MACHINES;
        public String MASTER_DISPLAY;
        public String SLAVE_DISPLAY;
        public String SYNCHRONIZATION_SCRIPT;
        public boolean OUTPUT_TO_CUR_DIR;
        public String OUTPUT_DIR_PREFIX;
        public String JAVA;
    }

    Random rand = new Random();

    // This is used to share capabilities - we generate this once
    // early on, then when we generate individual proxy beliefs files,
    // we add the RAPBeliefs from this list (minus the proxy's own
    // RAPBelief) to the ProxyState.
    ArrayList<RAPBelief> teamRAPBeliefs = new ArrayList<RAPBelief>();

    private static double mps_to_kph(double metersPerSecond) {
        return ((metersPerSecond * 3600) / 1000);
    }

    /**
     * Writes all the cfg and xml file for an ACCAST type scenario
     */
    public ACCASTCreate1() {
        if (USE_JAMVM) {
            confJamVM();
        }

        if (!OUTPUT_TO_CUR_DIR) {
            File locDir = new File(loc);
            OUTPUT_DIR = OUTPUT_DIR_PREFIXS + locDir.getName();

            OUTPUT_DIRS = OUTPUT_DIR + SEP;
            Machinetta.Debugger.debug("Setting log output dir to " + OUTPUT_DIR, 1, this);
        }

        Vector teamMembers = new Vector();

        // For now, add all proxy ids to the team
        for (int i = 0; i < noUAVs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.UAV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.UAV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, UAVSARCaps));
        }

        for (int i = 0; i < noEOIRUAVs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.EOIRUAV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.EOIRUAV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, UAVEOIRCaps));
        }

        for (int i = 0; i < noRSSIUAVs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.RSSIUAV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.RSSIUAV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, UAVRSSICaps));
        }

        for (int i = 0; i < noArmoredUAVs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.AUAV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.AUAV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, AUAVCaps));
        }

        for (int i = 0; i < noOperators; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.OPER + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.OPER + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, operatorCaps));
        }

        for (int i = 0; i < noFalconView; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.FV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.FV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, falconViewCaps));
        }

        for (int i = 0; i < noSimUsers; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.SIM_USER + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.SIM_USER + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, simUserCaps));
        }

        for (int i = 0; i < noUGSs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.UGS + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.UGS + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, UGSCaps));
        }

        for (int i = 0; i < noHumvees; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.HUMVEE + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.HUMVEE + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, HVCaps));
        }

        for (int i = 0; i < noBlueforDIs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.DIBLUE + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.DIBLUE + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, DIBLUECaps));
        }

        for (int i = 0; i < noIntelligentMines; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.IM + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.IM + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, IMCaps));
        }

        for (int i = 0; i < noAUGVs; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.AUGV + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.AUGV + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, AUGVCaps));
        }

        for (int i = 0; i < noC130s; i++) {
            Machinetta.Debugger.debug("Adding " + UnitIDs.C130 + i + " to team", 1, this);
            NamedProxyID pid = new NamedProxyID(UnitIDs.C130 + i);
            teamMembers.add(pid);
            teamRAPBeliefs.add(new RAPBelief(pid, false, C130Caps));
        }

        Machinetta.Debugger.debug("Creating TeamBelief with " + teamMembers.size() + " members", 1, this);

        // For testing Dynamic Teaming
        // teamMembers.clear();
        // teamMembers.add(new NamedProxyID("Operator0"));
        team = new TeamBelief("TeamAll", teamMembers, new Hashtable());

        // Now create specific files
        for (int i = 0; i < noUAVs; i++) {
            createUAVCfgFile(UnitIDs.UAV + i);
            makeUAVBeliefs(UnitIDs.UAV + i, i);
        }

        for (int i = 0; i < noEOIRUAVs; i++) {
            createEOIRUAVCfgFile(UnitIDs.EOIRUAV + i);
            makeEOIRUAVBeliefs(UnitIDs.EOIRUAV + i, i);
        }

        for (int i = 0; i < noRSSIUAVs; i++) {
            if (uavBinaryBayesFilterPanelFirstFourOn) {
                if (i < 4) {
                    uavBinaryBayesFilterPanelOn = true;
                } else {
                    uavBinaryBayesFilterPanelOn = false;
                }

            }
            createRSSIUAVCfgFile(UnitIDs.RSSIUAV + i);
            makeRSSIUAVBeliefs(UnitIDs.RSSIUAV + i, i);
        }

        for (int i = 0; i < noArmoredUAVs; i++) {
            createUAVCfgFile(UnitIDs.AUAV + i);
            makeAUAVBeliefs(UnitIDs.AUAV + i, i);
        }

        for (int i = 0; i < noOperators; i++) {
            createOperatorCfgFile(UnitIDs.OPER + i);
            makeOperatorBeliefs(UnitIDs.OPER + i);
        }

        for (int i = 0; i < noFalconView; i++) {
            createFalconViewCfgFile(UnitIDs.FV + i);
            makeFalconViewBeliefs(UnitIDs.FV + i);
        }

        for (int i = 0; i < noSimUsers; i++) {
            createSimUserCfgFile(UnitIDs.SIM_USER + i);
            makeSimUserBeliefs(UnitIDs.SIM_USER + i);
        }

        for (int i = 0; i < noUGSs; i++) {
            createUGSCfgFile(UnitIDs.UGS + i);
            makeUGSBeliefs(UnitIDs.UGS + i);
        }

        for (int i = 0; i < noHumvees; i++) {
            createHVCfgFile(UnitIDs.HUMVEE + i);
            makeHVBeliefs(UnitIDs.HUMVEE + i);
        }

        for (int i = 0; i < noBlueforDIs; i++) {
            createBlueforDICfgFile(UnitIDs.DIBLUE + i);
            makeBlueforDIBeliefs(UnitIDs.DIBLUE + i);
        }

        for (int i = 0; i < noIntelligentMines; i++) {
            createIMCfgFile(UnitIDs.IM + i);
            makeIMBeliefs(UnitIDs.IM + i);
        }

        for (int i = 0; i < noAUGVs; i++) {
            createAUGVCfgFile(UnitIDs.AUGV + i);
            makeAUGVBeliefs(UnitIDs.AUGV + i);
        }

        for (int i = 0; i < noC130s; i++) {
            createC130CfgFile(UnitIDs.C130 + i);
            makeC130Beliefs(UnitIDs.C130 + i);
        }

        Machinetta.Debugger.debug(1, "Making run script");
        makeRunScript();
        Machinetta.Debugger.debug(1, "Making Env file");
        if (SMALL_ACCAST_SCENARIO) {
            makeEnvFileACCASTSmall();
        } else {
            makeEnvFileACCAST();
        }

        Machinetta.Debugger.debug(1, "Making varius linux scripts");
        LinuxStaticScripts.makeKillScript(loc);
        LinuxStaticScripts.makeUptimeScript(loc);
        LinuxStaticScripts.makeTopScript(loc);
        LinuxStaticScripts.makeTopForeverAllScript(loc);
        LinuxStaticScripts.makeMemScript(loc);
        LinuxStaticScripts.makeMemForeverAllScript(loc);

        LinuxSlaveScripts.makeAllKillScript(loc, SLAVE_MACHINES);
        LinuxSlaveScripts.makeTopsScript(loc, SLAVE_MACHINES);
        LinuxSlaveScripts.makeDeleteLogsScript(loc, SLAVE_MACHINES, OUTPUT_DIRS);
        LinuxSlaveScripts.makeFetchLogsScript(loc, SLAVE_MACHINES, OUTPUT_DIRS);
        LinuxSlaveScripts.makeUptimesScript(loc, SLAVE_MACHINES);
        LinuxSlaveScripts.makeMemAllScript(loc, SLAVE_MACHINES);
        Machinetta.Debugger.debug(1, "Done with ACCASTCreate constructor.");
    }

    public static void setConfigurationFlag(String flagName) {
        configureFor5by5AccastDemo = false;
        configureFor5by5AccastDemoFortBenning = false;

        if (flagName.equalsIgnoreCase("configureFor5by5AccastDemo")) {
            configureFor5by5AccastDemo = true;
        }
        if (flagName.equalsIgnoreCase("configureFor5by5AccastDemoFortBenning")) {
            configureFor5by5AccastDemoFortBenning = true;
        } else {
            System.err.println("unknown configuration='" + flagName + "'");
            throw new RuntimeException("unknown configuration='" + flagName + "'");
        }
    }

    // Configure everything to use JamVM and no -Xmx options
    public static void confJamVM() {
        JAVA = "/usr1/java/jamvm/bin/jamvm";
    }

    public static void confNoXmx() {
        simXmx = "";
        defaultProxyXmx = "";
        operProxyXmx = "";
        falconViewProxyXmx = "";
        simUserProxyXmx = "";
        uavProxyXmx = "";
        augvProxyXmx = "";
        diProxyXmx = "";
        humveeProxyXmx = "";
        imProxyXmx = "";
        ugsProxyXmx = "";
        c130ProxyXmx = "";
    }

    public static void confMap50by50() {
        mapHeightMeters = 50000;
        mapWidthMeters = 50000;
        emitterLocations = bigMapEmitterLocations;
        sanjayaAssetCollisionMinZDistMeters = 100;
        sanjayaAssetCollisionRangeMeters = 1000;
    }

    public static void confMap5by5() {
        mapHeightMeters = 5000;
        mapWidthMeters = 5000;
        emitterLocations = bigMapEmitterLocations;

        sanjayaAssetCollisionDetectionOn = false;
        sanjayaAssetCollisionMinZDistMeters = 1;
        sanjayaAssetCollisionRangeMeters = 1;
    }

    public static void confRRT1by1() {
//         RRTPreferredPathMeters = 1000.0;
//         RRTMaxPath = 1000.0;
//         RRTBranchRangeX = 200.0;
//         RRTBranchRangeY = 200.0;
        RRTPreferredPathMeters = 500.0;
        RRTMaxPath = 500.0;
        RRTBranchRangeX = 50.0;
        RRTBranchRangeY = 50.0;

        // from GA;
        RRTCONFIG_NO_EXPANSIONS = 4749;
        RRTCONFIG_BRANCHES_PER_EXPANSION = 3;
        RRTCONFIG_MAX_THETA_CHANGE = 71.73;
        RRTCONFIG_MAX_PSI_CHANGE = 5.34;
        RRTCONFIG_MAX_BRANCH_LENGTH = .06;
        RRTCONFIG_MIN_BRANCH_LENGTH = .01;
        RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = 113;
        RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = 113;
    }

    public static void confRRT5by5() {
        RRTPreferredPathMeters = 1000.0;
        RRTMaxPath = 1000.0;
        RRTBranchRangeX = 250.0;
        RRTBranchRangeY = 250.0;

        // From GA
        RRTCONFIG_NO_EXPANSIONS = 1000;
        RRTCONFIG_BRANCHES_PER_EXPANSION = 3;
        RRTCONFIG_MAX_THETA_CHANGE = 90;
        RRTCONFIG_MAX_PSI_CHANGE = 1;
        RRTCONFIG_MAX_BRANCH_LENGTH = .05;
        RRTCONFIG_MIN_BRANCH_LENGTH = 0;
        RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = 252;
        RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = 252;
    }

    public static void confRRT50by50() {
        RRTBranchRangeX = 5000.0;
        RRTBranchRangeY = 5000.0;
        RRTMaxZChange = 5.0;      // was 1.0 with a branch size of 1000 - should therefore now be 5.0?
        RRTMinPathLength = 15000;
        RRTMaxPath = 25000.0;
        RRTPreferredPathMeters = 25000.0;
        PathConflictZDist = 150.0;

        // From GA
        RRTCONFIG_NO_EXPANSIONS = 5000;
        RRTCONFIG_BRANCHES_PER_EXPANSION = 3;
        RRTCONFIG_MAX_THETA_CHANGE = 90;
        RRTCONFIG_MAX_PSI_CHANGE = 1;
        RRTCONFIG_MAX_BRANCH_LENGTH = .05;
        RRTCONFIG_MIN_BRANCH_LENGTH = 0;
        RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = 5000;
        RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = 5000;
    }

    public static void confPathPlanner1by1() {
        PathConflictDist = 200.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 500.0;
        PathPlannerTTL = 5;
    }

    public static void confPathPlanner5by5() {
        PathConflictDist = 200.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 500.0;
        PathPlannerTTL = 5;
    }

    public static void confPathPlanner50by50() {
        PathConflictDist = 2000.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 1000.0;
        PathPlannerTTL = 5;
    }

    public static void confMapAustinAccast50() {
        CTDB_FILE = CTDB_AUSTIN50;
        mapLowerLeftLat = mapLowerLeftLatAustinAccast50;
        mapLowerLeftLon = mapLowerLeftLonAustinAccast50;
    }

    public static void confMapAustinAccast5() {
        CTDB_FILE = CTDB_AUSTIN5;
        mapLowerLeftLat = mapLowerLeftLatAustinAccast5;
        mapLowerLeftLon = mapLowerLeftLonAustinAccast5;
    }

    public static void configureFor5by5AccastDemo(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {

        System.out.println("\n\n\nJUST FYI, THIS IS THE ONE WE ARE USING: configureFor5by5AccastDemo DeconflictPath?" + argDeconflictOn + " OtherVehicle?" + argOvcmOn + "  \n\n\n\n");
        confMapAustinAccast5();
        emitterCount = argNoEmitters;
        
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = false;	// @TODO Really it should be on... and we should create a separate config for zone controller
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        noEOIRUAVs = 1;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterOn = false;

        noRSSIUAVs = argNoRSSIUAVs;
        noCivilians = argNoCivilians;
        PathPlannerTTL = argPathPlannerTTL;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        OtherVehicleCostmapOn = argOvcmOn;
        OtherVehicleCostMapAvoid = 300;
        OtherVehicleCostMapConflict = 200;
        confMap5by5();
        confPathPlanner5by5();
        confRRT5by5();

        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 100;
        uavPlacementRandom = true;

        END_CONDITION_ALL_INFANTRY_DEAD = true;
        END_CONDITION_ALL_GOALS_HELD = true;
    }

    public static void configureFor5by5AccastDemoFortBenning(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
        if (USE_JAMVM) {
            confJamVM();
        }
        confNoXmx();
        confMapAustinAccast5();
        emitterCount = argNoEmitters;
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = false;	// @TODO Really it should be on... and we should create a separate config for zone controller
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        noEOIRUAVs = 1;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterOn = false;

        noRSSIUAVs = argNoRSSIUAVs;
        noCivilians = argNoCivilians;
        PathPlannerTTL = argPathPlannerTTL;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        OtherVehicleCostmapOn = argOvcmOn;
        OtherVehicleCostMapAvoid = 300;
        OtherVehicleCostMapConflict = 200;
        confMap5by5();
        //        confMap50by50();
        confPathPlanner5by5();
        confRRT5by5();
//         confPathPlanner50by50();
//         confRRT50by50();

        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 100;
        uavPlacementRandom = true;

        END_CONDITION_ALL_INFANTRY_DEAD = true;
        END_CONDITION_ALL_GOALS_HELD = true;
    }

    public static void main(String argv[]) {
        int argNoRSSIUAVs = noRSSIUAVs;
        int argPathPlannerTTL = 0;
        // Next two need to be set to avoid other a/c
        boolean argOvcmOn = true;
        boolean argDeconflictOn = true;
        int argNoEmitters = emitterCount;
        int argNoCivilians = noCivilians;

        int argNoUAVs = noUAVs;
        int argNoArmoredUAVs = noArmoredUAVs;
        int argNoEOIRUAVs = noEOIRUAVs;
        int argNoUGSs = noUGSs;
        int argNoOperators = noOperators;
        int argNoFalconView = noFalconView;
        int argNoSimUsers = noSimUsers;
        int argNoOpforInfantry = noOpforInfantry;
        int argNoBlueforInfantry = noBlueforInfantry;
        int argNoHumvees = noHumvees;
        int argNoBlueforDIs = noBlueforDIs;
        int argNoIntelligentMines = noIntelligentMines;
        int argNoAUGVs = noAUGVs;
        int argNoC130s = noC130s;
        String argSwitchRawPacketLogFilename = switchRawPacketLogFilename;
        boolean argGuiOn = true;
        double ARG_PLAN_CREATION_DROP_PROBABILITY = -1;
        double ARG_PLAN_CREATION_REACTION_TIME_MS = 0;
        boolean ARG_PLAN_CREATION_AUAV_ATTACK_DI = false;
        boolean ARG_PLAN_CREATION_AUGV_ATTACK_DI = false;
        boolean ARG_PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = false;
        boolean ARG_USE_JAMVM = false;

	// First args is always the directory we'll be writing all the
        // config and script files into;
        // basically the config name
        loc = argv[0];
        //locs = loc + SEP;
        // Setup configs to run with relative paths (all config files will be in the config directory with the run script
        locs = "." + SEP;

	// @TODO: Add params for BuildXml
        int argProcessingStart = 1;
        for (int loopi = argProcessingStart; loopi < argv.length; loopi++) {
            if (argv[loopi].equalsIgnoreCase("--configuration") && ((loopi + 1) < argv.length)) {
                setConfigurationFlag(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--uav") && ((loopi + 1) < argv.length)) {
                argNoUAVs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--auav") && ((loopi + 1) < argv.length)) {
                argNoArmoredUAVs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--eoiruav") && ((loopi + 1) < argv.length)) {
                argNoEOIRUAVs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--rssiuav") && ((loopi + 1) < argv.length)) {
                argNoRSSIUAVs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--ugs") && ((loopi + 1) < argv.length)) {
                argNoUGSs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--emitters") && ((loopi + 1) < argv.length)) {
                argNoEmitters = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--civilians") && ((loopi + 1) < argv.length)) {
                argNoCivilians = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--operators") && ((loopi + 1) < argv.length)) {
                argNoOperators = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--falconview") && ((loopi + 1) < argv.length)) {
                argNoFalconView = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--simusers") && ((loopi + 1) < argv.length)) {
                argNoSimUsers = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--opforinfantry") && ((loopi + 1) < argv.length)) {
                argNoOpforInfantry = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--blueforinfantry") && ((loopi + 1) < argv.length)) {
                argNoBlueforInfantry = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--humvees") && ((loopi + 1) < argv.length)) {
                argNoHumvees = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--bluefordi") && ((loopi + 1) < argv.length)) {
                argNoBlueforDIs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--intelligentmines") && ((loopi + 1) < argv.length)) {
                argNoIntelligentMines = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--augvs") && ((loopi + 1) < argv.length)) {
                argNoAUGVs = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--c130s") && ((loopi + 1) < argv.length)) {
                argNoC130s = Integer.parseInt(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--switchpacketlog") && ((loopi + 1) < argv.length)) {
                argSwitchRawPacketLogFilename = argv[++loopi];
            } else if (argv[loopi].equalsIgnoreCase("--ovcm")) {
                argOvcmOn = true;
            } else if (argv[loopi].equalsIgnoreCase("--deconflict")) {
                argDeconflictOn = true;
            } else if (argv[loopi].equalsIgnoreCase("--bluefordidamage") && ((loopi + 1) < argv.length)) {
                blueforDIDamage = Double.parseDouble(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--guion") && ((loopi + 1) < argv.length)) {
                argGuiOn = Boolean.parseBoolean(argv[++loopi]);
            } else if (argv[loopi].equalsIgnoreCase("--PLAN_CREATION_AUAV_ATTACK_DI") && ((loopi + 1) < argv.length)) {
                ARG_PLAN_CREATION_AUAV_ATTACK_DI = Boolean.parseBoolean(argv[++loopi]);
                System.out.println("    ARG_PLAN_CREATION_AUAV_ATTACK_DI set to " + ARG_PLAN_CREATION_AUAV_ATTACK_DI);
            } else if (argv[loopi].equalsIgnoreCase("--PLAN_CREATION_AUGV_ATTACK_DI") && ((loopi + 1) < argv.length)) {
                ARG_PLAN_CREATION_AUGV_ATTACK_DI = Boolean.parseBoolean(argv[++loopi]);
                System.out.println("    ARG_PLAN_CREATION_AUGV_ATTACK_DI set to " + ARG_PLAN_CREATION_AUGV_ATTACK_DI);
            } else if (argv[loopi].equalsIgnoreCase("--PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI") && ((loopi + 1) < argv.length)) {
                ARG_PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = Boolean.parseBoolean(argv[++loopi]);
                System.out.println("    ARG_PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI set to " + ARG_PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI);
            } else if (argv[loopi].equalsIgnoreCase("--PLAN_CREATION_DROP_PROBABILITY") && ((loopi + 1) < argv.length)) {
                ARG_PLAN_CREATION_DROP_PROBABILITY = Double.parseDouble(argv[++loopi]);
                System.out.println("    ARG_PLAN_CREATION_DROP_PROBABILITY set to " + ARG_PLAN_CREATION_DROP_PROBABILITY);
            } else if (argv[loopi].equalsIgnoreCase("--PLAN_CREATION_REACTION_TIME_MS") && ((loopi + 1) < argv.length)) {
                ARG_PLAN_CREATION_REACTION_TIME_MS = Double.parseDouble(argv[++loopi]);
                System.out.println("    ARG_PLAN_CREATION_REACTION_TIME_MS set to " + ARG_PLAN_CREATION_REACTION_TIME_MS);
            } else if (argv[loopi].equalsIgnoreCase("--USE_JAMVM") && ((loopi + 1) < argv.length)) {
                ARG_USE_JAMVM = Boolean.parseBoolean(argv[++loopi]);
                System.out.println("    ARG_USE_JAMVM set to " + ARG_USE_JAMVM);
            } else {
                System.err.println("ERROR: UNKNOWN ARG argv[" + loopi + "]= '" + argv[loopi] + "'");
            }
        }

        int configureCount = 0;
        if (configureFor5by5AccastDemo) {
            configureCount++;
        }
        if (configureFor5by5AccastDemoFortBenning) {
            configureCount++;
        }
        if (configureCount > 1) {
            Machinetta.Debugger.debug("More than one of the configureFor flags at the top of ACCASTCreate are set to true - only one may be set to true at a time.  Please check for more than one set to true, and recompile ACCASTCreate.", 5, "ACCASTCreate");
            Machinetta.Debugger.debug("according to params, argNoRSSIUAVs=" + argNoRSSIUAVs + ", argOvcmOn = " + argOvcmOn + ", argDeconflictOn=" + argDeconflictOn + ", argNoEmitters= " + argNoEmitters, 5, "ACCASTCreate");
            System.exit(1);
        }
        if (configureCount < 1) {
            Machinetta.Debugger.debug("None of the configureFor flags at the top of ACCASTCreate are set to true - one (and ONLY one) must be set to true.  Please choose one and set it to true, and recompile ACCASTCreate.", 5, "ACCASTCreate");
            System.exit(1);
        }

        String username = "darpacode";
        createUserConfigs();
        setUserConfig(username);

        System.err.println("according to params, argNoRSSIUAVs=" + argNoRSSIUAVs + ", argOvcmOn = " + argOvcmOn + ", argDeconflictOn=" + argDeconflictOn + ", argNoEmitters= " + argNoEmitters);

        if (configureFor5by5AccastDemo) {
            configureFor5by5AccastDemo(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor5by5AccastDemoFortBenning) {
            configureFor5by5AccastDemoFortBenning(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        }

	// these come last because, being command line params, they
        // override anything in the configurations above
        // (i.e. configureFor1by1Gascola, etc.)
        noUAVs = argNoUAVs;
        noArmoredUAVs = argNoArmoredUAVs;
        noEOIRUAVs = argNoEOIRUAVs;
        noRSSIUAVs = argNoRSSIUAVs;
        noUGSs = argNoUGSs;
        noOperators = argNoOperators;
        noFalconView = argNoFalconView;
        noSimUsers = argNoSimUsers;
        noOpforInfantry = argNoOpforInfantry;
        noBlueforInfantry = argNoBlueforInfantry;
        noHumvees = argNoHumvees;
        noBlueforDIs = argNoBlueforDIs;
        noIntelligentMines = argNoIntelligentMines;
        noAUGVs = argNoAUGVs;
        noC130s = argNoC130s;
        switchRawPacketLogFilename = argSwitchRawPacketLogFilename;
        GUI_ON = argGuiOn;
        PLAN_CREATION_DROP_PROBABILITY = ARG_PLAN_CREATION_DROP_PROBABILITY;
        PLAN_CREATION_REACTION_TIME_MS = ARG_PLAN_CREATION_REACTION_TIME_MS;
        PLAN_CREATION_AUAV_ATTACK_DI = ARG_PLAN_CREATION_AUAV_ATTACK_DI;
        PLAN_CREATION_AUGV_ATTACK_DI = ARG_PLAN_CREATION_AUGV_ATTACK_DI;
        PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = ARG_PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI;
        USE_JAMVM = ARG_USE_JAMVM;
        new ACCASTCreate1();
    }

    /**
     * This will (obviously) only work on linux/unix (and obviously, the
     * classpath needs to be changed per user.)
     */
    public void makeRunScript() {
        if (SLAVE_MACHINES == null || SLAVE_MACHINES.length == 0) {
            makeSingleMachineRunScript();
        } else {

            // Make scripts for all machines
            try {
                DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + "run.sh"));

                out.writeBytes("#!/bin/tcsh\n\n");
                out.writeBytes("setenv CLASSPATH " + MASTER_CLASSPATH + "\n\n");

                /*
                 out.writeBytes("echo Synchronizing files on slaves \n");
                 out.writeBytes(SYNCHRONIZATION_SCRIPT+"\n");
                
                 out.writeBytes("\n");
                 if(null != MASTER_DISPLAY)
                 out.writeBytes("setenv DISPLAY "+MASTER_DISPLAY+"\n");
                 out.writeBytes("\n");
                 out.writeBytes("mkdir -p "+OUTPUT_DIR+"\n");
                 out.writeBytes("\n");
                 */
                if (null != switchRawPacketLogFilename) {
                    out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch --logtofile " + OUTPUT_DIRS + switchRawPacketLogFilename + " >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
                } else {
                    out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
                }
                out.writeBytes("sleep 1\n\n");
                out.writeBytes(JAVA + " " + simXmx + " AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& " + OUTPUT_DIRS + "SimOut &\n\n");
                // out.writeBytes(JAVA+" "+simXmx+" AirSim.Environment.GUI.MainFrame " + locs + "Env.txt >& "+OUTPUT_DIRS+"SimOut &\n\n");
                out.writeBytes("sleep 1\n\n");

                for (int i = 0; i < SLAVE_MACHINES.length; i++) {
                    out.writeBytes("\n\necho Starting processes on " + SLAVE_MACHINES[i] + "\n");
                    out.writeBytes("# ssh " + SLAVE_MACHINES[i] + " chmod +x " + locs + "slave_" + SLAVE_MACHINES[i] + ".sh\n");
                    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " " + locs + "slave_" + SLAVE_MACHINES[i] + ".sh &\n");
                }

                out.writeBytes("\necho Starting Operators\n");
                for (int i = 0; i < noOperators; i++) {
                    out.writeBytes(JAVA + " " + operProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.OPER + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.OPER + i + ".out &\n");
                    out.writeBytes("sleep 1\n");
                }

                out.writeBytes("\necho Starting FalconView interfaces\n");
                for (int i = 0; i < noFalconView; i++) {
                    out.writeBytes(JAVA + " " + falconViewProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.FV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.FV + i + ".out &\n");
                    out.writeBytes("sleep 1\n");
                }

                out.writeBytes("\necho Starting SimUsers\n");
                for (int i = 0; i < noSimUsers; i++) {
                    out.writeBytes(JAVA + " " + simUserProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.SIM_USER + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.SIM_USER + i + ".out &\n");
                    out.writeBytes("sleep 1\n");
                }

                out.writeBytes("./memforeverall.sh >& " + OUTPUT_DIRS + "memtrack.out &\n");
                out.writeBytes("./topforeverall.sh >& " + OUTPUT_DIRS + "toptrack.out &\n");

                out.flush();
                out.close();

                // The switch, the simulator, the operator, and the
                // FalconView interface all run on the master machine
                DataOutputStream[] slaveScripts = new DataOutputStream[SLAVE_MACHINES.length];
                for (int i = 0; i < slaveScripts.length; i++) {
                    slaveScripts[i] = new DataOutputStream(new FileOutputStream(locs + "slave_" + SLAVE_MACHINES[i] + ".sh"));

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

                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.UAV + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.UAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.UAV + i + ".out &\n");
                    //                    slaveScripts[outIndex].writeBytes(JAVA+" "+uavProxyXmx+" Machinetta.Proxy " + locs + UnitIDs.UAV+i+".cfg >& "+OUTPUT_DIRS + UnitIDs.UAV+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noRSSIUAVs; i++) {
                    Machinetta.Debugger.debug("Adding RSSI uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.RSSIUAV + i + " on $HOST\n");
                    String outString;
                    if (CONFIG_FOR_WIN_DAVE) {
                        outString = JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + "/home/dscerri/Code/" + UnitIDs.RSSIUAV + i + ".cfg >& " + OUTPUT_DIR + "/" + UnitIDs.RSSIUAV + i + ".out &\n";
                    } else {
                        outString = JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.RSSIUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.RSSIUAV + i + ".out &\n";
                    }
                    Machinetta.Debugger.debug("Writing to file " + "slave_" + SLAVE_MACHINES[outIndex] + ": \"" + outString + "\"", 1, this);
                    slaveScripts[outIndex].writeBytes(outString);
                    //                    slaveScripts[outIndex].writeBytes(JAVA+" "+uavProxyXmx+" Machinetta.Proxy " + locs + UnitIDs.RSSIUAV+i+".cfg >& "+OUTPUT_DIRS + UnitIDs.RSSIUAV+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noEOIRUAVs; i++) {
                    Machinetta.Debugger.debug("Adding EOIR uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.EOIRUAV + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.EOIRUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.EOIRUAV + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noArmoredUAVs; i++) {
                    Machinetta.Debugger.debug("Adding Armored uav " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.AUAV + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.AUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.AUAV + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noBlueforDIs; i++) {
                    Machinetta.Debugger.debug("Adding blueforDI " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.DIBLUE + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + diProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.DIBLUE + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.DIBLUE + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noAUGVs; i++) {
                    Machinetta.Debugger.debug("Adding AUGV " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.AUGV + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + augvProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.AUGV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.AUGV + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noHumvees; i++) {
                    Machinetta.Debugger.debug("Adding Humvee " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.HUMVEE + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + humveeProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.HUMVEE + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.HUMVEE + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noC130s; i++) {
                    Machinetta.Debugger.debug("Adding C130 " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.C130 + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + c130ProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.C130 + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.C130 + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noIntelligentMines; i++) {
                    Machinetta.Debugger.debug("Adding IM " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.IM + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + imProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.IM + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.IM + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

                for (int i = 0; i < noUGSs; i++) {
                    Machinetta.Debugger.debug("Adding UGS " + i + " to script for slave " + SLAVE_MACHINES[outIndex], 1, this);
                    slaveScripts[outIndex].writeBytes("\necho Starting " + UnitIDs.UGS + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA + " " + ugsProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.UGS + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.UGS + i + ".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }

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
            String filename = loc + SEP + "run.sh";
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));

            out.writeBytes("#!/bin/tcsh\n\n");
            out.writeBytes("setenv CLASSPATH " + MASTER_CLASSPATH + "\n\n");

            out.writeBytes("\n");
            if (null != MASTER_DISPLAY) {
                out.writeBytes("setenv DISPLAY " + MASTER_DISPLAY + "\n");
            }
            out.writeBytes("\n");
            out.writeBytes("mkdir -p " + OUTPUT_DIR + "\n");
            out.writeBytes("\n");
            if (null != switchRawPacketLogFilename) {
                out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch --logtofile " + switchRawPacketLogFilename + " >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
            } else {
                out.writeBytes(JAVA + "  Machinetta.Communication.UDPSwitch >& " + OUTPUT_DIRS + "SwitchOut &\n\n");
            }
            out.writeBytes("sleep 1\n\n");
            out.writeBytes(JAVA + " " + simXmx + " AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& " + OUTPUT_DIRS + "SimOut &\n\n");
            out.writeBytes("sleep 1\n\n");

            // For now, the same script runs all the agents.
            if (noUAVs > 0) {
                out.writeBytes("\necho Starting UAVs\n");
            }
            for (int i = 0; i < noUAVs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.UAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.UAV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noRSSIUAVs > 0) {
                out.writeBytes("\necho Starting RSSI UAVs\n");
            }
            for (int i = 0; i < noRSSIUAVs; i++) {
                out.writeBytes("\necho Starting RSSIUAV " + i + " on $HOST\n");
                out.writeBytes(JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.RSSIUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.RSSIUAV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noEOIRUAVs > 0) {
                out.writeBytes("\necho Starting EOIR UAVs\n");
            }
            for (int i = 0; i < noEOIRUAVs; i++) {
                out.writeBytes("\necho Starting EOIRUAV " + i + " on $HOST\n");
                out.writeBytes(JAVA + " " + uavProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.EOIRUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.EOIRUAV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noArmoredUAVs > 0) {
                out.writeBytes("\necho Starting AUAVs\n");
            }
            for (int i = 0; i < noArmoredUAVs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.AUAV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.AUAV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noUGSs > 0) {
                out.writeBytes("\necho Starting UGSs\n");
            }
            for (int i = 0; i < noUGSs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.UGS + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.UGS + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noHumvees > 0) {
                out.writeBytes("\necho Starting Humvees\n");
            }
            for (int i = 0; i < noHumvees; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.HUMVEE + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.HUMVEE + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noBlueforDIs > 0) {
                out.writeBytes("\necho Starting Bluefor DI\n");
            }
            for (int i = 0; i < noBlueforDIs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.DIBLUE + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.DIBLUE + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noIntelligentMines > 0) {
                out.writeBytes("\necho Starting IM\n");
            }
            for (int i = 0; i < noIntelligentMines; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.IM + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.IM + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noAUGVs > 0) {
                out.writeBytes("\necho Starting AUGV\n");
            }
            for (int i = 0; i < noAUGVs; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.AUGV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.AUGV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noC130s > 0) {
                out.writeBytes("\necho Starting Bluefor C130\n");
            }
            for (int i = 0; i < noC130s; i++) {
                out.writeBytes(JAVA + "  Machinetta.Proxy " + locs + UnitIDs.C130 + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.C130 + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noOperators > 0) {
                out.writeBytes("\necho Starting Operators\n");
            }
            for (int i = 0; i < noOperators; i++) {
                out.writeBytes(JAVA + " " + operProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.OPER + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.OPER + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noFalconView > 0) {
                out.writeBytes("\necho Starting FalconView\n");
            }
            for (int i = 0; i < noFalconView; i++) {
                out.writeBytes(JAVA + " " + falconViewProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.FV + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.FV + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            if (noSimUsers > 0) {
                out.writeBytes("\necho Starting SimUsers\n");
            }
            for (int i = 0; i < noSimUsers; i++) {
                out.writeBytes(JAVA + " " + simUserProxyXmx + " Machinetta.Proxy " + locs + UnitIDs.SIM_USER + i + ".cfg >& " + OUTPUT_DIRS + UnitIDs.SIM_USER + i + ".out &\n");
                out.writeBytes("sleep 1\n");
            }

            out.flush();
            out.close();
            Runtime.getRuntime().exec("chmod 744 " + filename);
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    // @TODO: Kind of a nasty hack.  Too much effort to 'generalize'
    // everything...
    public void makeEnvFileACCAST() {

        boolean shortenScenario = true;
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + "Env.txt"));

            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes(SANJAYA_UPDATE_RATE_COMMENT);
            out.writeBytes("UPDATE_RATE " + sanjayaUpdateRate + "\n\n");

            out.writeBytes("GUI_ON " + GUI_ON + "\n");

            out.writeBytes("DAMAGE_MODE Cumulative\n\n");
            out.writeBytes("DAMAGE_MODE_BLUEFOR_DI " + blueforDIDamage + " \n\n");

            out.writeBytes("MAP_WIDTH_METERS " + 5000 + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + 5000 + "\n");
//             out.writeBytes("MAP_WIDTH_METERS "+mapWidthMeters+"\n");
//             out.writeBytes("MAP_HEIGHT_METERS "+mapHeightMeters+"\n");
            out.writeBytes("\n");

            out.writeBytes(USE_XPLANE_CAMERA_COMMENT);
            out.writeBytes("USE_XPLANE_CAMERA " + USE_XPLANE_CAMERA + "\n");
            out.writeBytes("USE_XPLANE_CAMERA_HOST " + USE_XPLANE_CAMERA_HOST + "\n");
            out.writeBytes("\n");
            out.writeBytes("GUI_MARK_XPLANE_ASSET true\n");
            out.writeBytes("\n");

            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
//             out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
//             out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + 5200 + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + 5200 + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES false\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM " + sanjayaOneKmGridLinesOn + "\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n");

            out.writeBytes("\n");
            out.writeBytes(SANJAYA_ASSET_COLLISION_COMMENT);
            out.writeBytes("ASSET_COLLISION_DETECTION_ON " + sanjayaAssetCollisionDetectionOn + "\n");
//             out.writeBytes("ASSET_COLLISION_DETECTION_RANGE_METERS "+sanjayaAssetCollisionRangeMeters+"\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_RANGE_METERS " + 100 + "\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_MIN_Z_DIST_METERS " + sanjayaAssetCollisionMinZDistMeters + "\n");

            out.writeBytes("\n");
            out.writeBytes(END_CONDITION_COMMENT);
            out.writeBytes("END_CONDITION_CAPTURE_THE_FLAG " + END_CONDITION_CAPTURE_THE_FLAG + "\n");
            out.writeBytes("END_CONDITION_ALL_HMMMVS_DEAD " + END_CONDITION_ALL_HMMMVS_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_OPFOR_DEAD " + END_CONDITION_ALL_OPFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_BLUEFOR_DEAD " + END_CONDITION_ALL_BLUEFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_EMITTERS_DETECTED " + END_CONDITION_ALL_EMITTERS_DETECTED + "\n");
            out.writeBytes("END_CONDITION_ALL_INFANTRY_DEAD " + END_CONDITION_ALL_INFANTRY_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_GOALS_HELD " + END_CONDITION_ALL_GOALS_HELD + "\n");

            out.writeBytes("END_CONDITION_GOAL 500 2250 0\n");
            out.writeBytes("END_CONDITION_GOAL 2500 2250 0\n");
            out.writeBytes("END_CONDITION_GOAL 1500 1500 0\n");
            out.writeBytes("END_CONDITION_GOAL 1950 1250 0\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");

            out.writeBytes("\n");

            // NOTE: If you change the emitters here you should change
            // the emitters in _createUAVCfgFile to match it.
            // writeSA9Locations(out);
            // out.writeBytes("UGS UGS0 5000 5000 BLUEFOR\n");
            // out.writeBytes("HUMMER H0 4700 4925 BLUEFOR\n");
            // out.writeBytes("TRUCK MULTI 5 OPFOR\n");
            //            out.writeBytes("CTDB_BASE_NAME "+CTDB_BASE_NAME+"\n");
            out.writeBytes("CTDB_BASE_NAME " + getCtdbBaseName() + "\n");

            out.writeBytes("ASSET_CONFIG AT_LOCATION_DISTANCE " + uavAtLocationDistance + "\n");
            out.writeBytes("ASSET_CONFIG MOVE_FINISHED_HOLD_RADIUS " + uavMoveFinishedHoldRadius + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_SPEED_KPH " + mps_to_kph(uavMaxSpeedMetersPerSec) + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_TURN_RATE_DEG " + uavMaxTurnRateDegPerStep + "\n");

            //            out.writeBytes("ASSET_CONFIG RSSI_MAP_SCALE " + BBFGridScaleFactor + "\n");            
            out.writeBytes("ASSET_CONFIG RSSI_MAP_SCALE " + 5.0 + "\n");

            out.writeBytes("\n");
            out.writeBytes("TERMINAL TM-0 1500 1500 1000 400\n");
            out.writeBytes("\n");
            out.writeBytes("######################################################################\n");
            out.writeBytes("# OPPOSING FORCES\n");
            out.writeBytes("######################################################################\n");
            out.writeBytes("\n");
            out.writeBytes("SA9 SA9-0 300 600 OPFOR\n");
            out.writeBytes("SA9 SA9-1 1500 600 OPFOR\n");
            out.writeBytes("SA9 SA9-2 2700 600 OPFOR\n");
            out.writeBytes("SA9 SA9-3 300 2500 OPFOR\n");
            out.writeBytes("SA9 SA9-4 2700 2500 OPFOR\n");
            out.writeBytes("\n");
            /*
             out.writeBytes("INFANTRY DI-OP0 400 700 OPFOR ADA0\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP1 1500 700 OPFOR ADA1\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP2 2600 700 OPFOR ADA2\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP3 400 2400 OPFOR ADA3\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP4 2600 2400 OPFOR ADA4\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP5 1600 700 OPFOR GATE6\n");
             out.writeBytes("INFANTRY DI-OP6 1700 700 OPFOR GATE6\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("A10 A10-OP0 1900 1200 0 OPFOR C2\n");
             out.writeBytes("A10 A10-OP1 1950 1200 0 OPFOR C2\n");
             out.writeBytes("A10 A10-OP2 2000 1200 0 OPFOR C2\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C00 1290 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C01 1295 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C02 1300 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C03 1305 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C04 1310 1100 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C05 1890 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C06 1895 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C07 1900 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C08 1905 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C09 1910 1100 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C10 290 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C11 295 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C12 300 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C13 305 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C14 310 1900 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C15 1490 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C16 1495 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C17 1500 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C18 1505 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C19 1510 1800 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("TASK DI-OP0 DEFEND 390 700 false true\n");
             out.writeBytes("TASK DI-OP1 DEFEND 1290 700 false true\n");
             out.writeBytes("TASK DI-OP2 DEFEND 2190 700 false true\n");
             out.writeBytes("TASK DI-OP3 DEFEND 390 2400 false true\n");
             out.writeBytes("TASK DI-OP4 DEFEND 2190 2400 false true\n");
             out.writeBytes("TASK DI-OP5 DEFEND 1450 700 false true\n");
             out.writeBytes("TASK DI-OP6 DEFEND 1540 700 false true\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             */
            out.writeBytes("######################################################################$\n");
            out.writeBytes("# BLUE FORCES\n");
            out.writeBytes("######################################################################$\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            if (shortenScenario) {
                for (int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
                    out.writeBytes("SMALL_UAV AUAV" + loopAuavs + " 3500 3000 500 34000 BLUEFOR\n");
                    out.writeBytes("SENSOR SAR AUAV" + loopAuavs + "\n");
                }
                for (int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + loopEoiruavs + " 3500 3000 500 18000 BLUEFOR\n");
                    out.writeBytes("SENSOR EOIR EOIR-UAV" + loopEoiruavs + "\n");
                }
            } else {
                for (int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
                    out.writeBytes("SMALL_UAV AUAV" + loopAuavs + " 4500 3500 500 34000 BLUEFOR\n");
                    out.writeBytes("SENSOR SAR AUAV" + loopAuavs + "\n");
                }
                for (int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + loopEoiruavs + " 4500 3500 500 18000 BLUEFOR\n");
                    out.writeBytes("SENSOR EOIR EOIR-UAV" + loopEoiruavs + "\n");
                }
            }
            out.writeBytes("\n");
            out.writeBytes("FAARP FAARP 4600 3600 BLUEFOR\n");
            out.writeBytes("\n");
	    // Note: currrently SA9's have a SENSE_RADIUS of 3km and a
            // FIRE_DIST of 2.5km, so we can't start the C130's so
            // close that they can be shot on the ground.

// 	    if(shortenScenario) {
// 		out.writeBytes("C130 C130-0 5000 5000 1000 BLUEFOR AIRLIFT0 0\n");
// 		out.writeBytes("C130 C130-1 4000 1000 1000 BLUEFOR INTMINE1 180\n");
// 		out.writeBytes("C130 C130-2 4000 2000 1000 BLUEFOR INTMINE2 180\n");
// 		out.writeBytes("C130 C130-3 4000 3000 1000 BLUEFOR INTMINE3 180\n");
// 	    }
// 	    else {
            out.writeBytes("C130 C130-0 6000 5000 1000 BLUEFOR AIRLIFT0 180\n");
            out.writeBytes("C130 C130-1 6000 1000 1000 BLUEFOR INTMINE1 180\n");
            out.writeBytes("C130 C130-2 6000 2000 1000 BLUEFOR INTMINE2 180\n");
            out.writeBytes("C130 C130-3 6000 3000 1000 BLUEFOR INTMINE3 180\n");
// 	    }
            out.writeBytes("\n");
            out.writeBytes("# initial UGV assault (before SA9's taken out)\n");
            if (shortenScenario) {
                out.writeBytes("AUGV AUGV0 -500 1500 BLUEFOR ASSAULT1\n");
                out.writeBytes("AUGV AUGV1 3500 1500 BLUEFOR ASSAULT1\n");
            } else {
                out.writeBytes("AUGV AUGV0 -1000 3000 BLUEFOR ASSAULT1\n");
                out.writeBytes("AUGV AUGV1 4000 -100 BLUEFOR ASSAULT1\n");
            }
            out.writeBytes("SENSOR LADAR AUGV0\n");
            out.writeBytes("SENSOR LADAR AUGV1\n");
            out.writeBytes("\n");
            out.writeBytes("# Troop convoy for front gate\n");
            if (shortenScenario) {
                out.writeBytes("HUMMER H0 3300 250 BLUEFOR ASSAULT2\n");
                out.writeBytes("HUMMER H1 3350 250 BLUEFOR ASSAULT2\n");
                out.writeBytes("AUGV AUGV2 3250 260 BLUEFOR ASSAULT2\n");
                out.writeBytes("AUGV AUGV3 3250 240 BLUEFOR ASSAULT2\n");
            } else {
                out.writeBytes("HUMMER H0 4000 250 BLUEFOR ASSAULT2\n");
                out.writeBytes("HUMMER H1 4050 250 BLUEFOR ASSAULT2\n");
                out.writeBytes("AUGV AUGV2 3800 260 BLUEFOR ASSAULT2\n");
                out.writeBytes("AUGV AUGV3 3800 240 BLUEFOR ASSAULT2\n");
            }
            out.writeBytes("SENSOR MARKONEEYEBALL H0\n");
            out.writeBytes("SENSOR MARKONEEYEBALL H1\n");
            out.writeBytes("SENSOR LADAR AUGV2\n");
            out.writeBytes("SENSOR LADAR AUGV3\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "0 6000 4800 BLUEFOR AIRLIFT00\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "0\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "1 6000 4800 BLUEFOR AIRLIFT00\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "1\n");
            out.writeBytes("\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "2 6000 4800 BLUEFOR AIRLIFT01\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "2\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "3 6000 4800 BLUEFOR AIRLIFT01\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "3\n");
            out.writeBytes("\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "4 6000 4800 BLUEFOR AIRLIFT02\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "4\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "5 6000 4800 BLUEFOR AIRLIFT02\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "5\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("INTELMINE IM0 5000 800 BLUEFOR INTMINE1\n");
            out.writeBytes("\n");
            out.writeBytes("INTELMINE IM1 5000 1800 BLUEFOR INTMINE2\n");
            out.writeBytes("\n");
            out.writeBytes("INTELMINE IM2 5000 2800 BLUEFOR INTMINE3\n");
            out.writeBytes("\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "6 4000 250 BLUEFOR ASSAULT21\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "6\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "7 4000 250 BLUEFOR ASSAULT21\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "7\n");
            out.writeBytes("\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "8 4000 250 BLUEFOR ASSAULT22\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "8\n");
            out.writeBytes("INFANTRY " + UnitIDs.DIBLUE + "9 4000 250 BLUEFOR ASSAULT22\n");
            out.writeBytes("SENSOR MARKONEEYEBALL " + UnitIDs.DIBLUE + "9\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT DI-BLUE0 C130-0\n");
            out.writeBytes("MOUNT DI-BLUE1 C130-0\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT DI-BLUE2 C130-0\n");
            out.writeBytes("MOUNT DI-BLUE3 C130-0\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT DI-BLUE4 C130-0\n");
            out.writeBytes("MOUNT DI-BLUE5 C130-0\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT DI-BLUE6 H0\n");
            out.writeBytes("MOUNT DI-BLUE7 H0\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT DI-BLUE8 H1\n");
            out.writeBytes("MOUNT DI-BLUE9 H1\n");
            out.writeBytes("\n");
            out.writeBytes("MOUNT IM0 C130-1\n");
            out.writeBytes("MOUNT IM1 C130-2\n");
            out.writeBytes("MOUNT IM2 C130-3\n");
            out.writeBytes("\n");
            out.writeBytes("TASK H0 HOLD\n");
            out.writeBytes("TASK H1 HOLD\n");
            out.writeBytes("TASK AUGV0 HOLD\n");
            out.writeBytes("TASK AUGV1 HOLD\n");
            out.writeBytes("TASK AUGV2 HOLD\n");
            out.writeBytes("TASK AUGV3 HOLD\n");
            out.writeBytes("\n");
            out.writeBytes("TASK C130-0 LAND\n");
            out.writeBytes("TASK C130-1 LAND\n");
            out.writeBytes("TASK C130-2 LAND\n");
            out.writeBytes("TASK C130-3 LAND\n");
            out.writeBytes("\n");
            out.writeBytes("# UAV's move around the airfield\n");
            out.writeBytes("# TASK AUAV0 RANDOMMOVE 1500 1500 3000 3000\n");
            out.writeBytes("# TASK AUAV1 RANDOMMOVE 1500 1500 3000 3000\n");
            out.writeBytes("# TASK AUAV2 RANDOMMOVE 1500 1500 3000 3000\n");
            out.writeBytes("# TASK AUAV3 RANDOMMOVE 1500 1500 3000 3000\n");
            out.writeBytes("# TASK EOIR-UAV0 RANDOMMOVE 1500 1500 2000 2000\n");
            out.writeBytes("\n");
            for (int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
                out.writeBytes("PROXY EOIR-UAV" + loopEoiruavs + " ON\n");
            }
            for (int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
                out.writeBytes("PROXY AUAV" + loopAuavs + " ON\n");
            }
            out.writeBytes("PROXY C130-0 ON\n");
            out.writeBytes("PROXY C130-1 ON\n");
            out.writeBytes("PROXY C130-2 ON\n");
            out.writeBytes("PROXY C130-3 ON\n");
            out.writeBytes("\n");
            out.writeBytes("PROXY IM0 ON\n");
            out.writeBytes("PROXY IM1 ON\n");
            out.writeBytes("PROXY IM2 ON\n");

            out.writeBytes("PROXY AUGV0 ON\n");
            out.writeBytes("PROXY AUGV1 ON\n");
            out.writeBytes("PROXY AUGV2 ON\n");
            out.writeBytes("PROXY AUGV3 ON\n");
            out.writeBytes("PROXY H0 ON\n");
            out.writeBytes("PROXY H1 ON\n");
            out.writeBytes("\n");
            out.writeBytes("PROXY DI-BLUE0 ON\n");
            out.writeBytes("PROXY DI-BLUE1 ON\n");
            out.writeBytes("PROXY DI-BLUE2 ON\n");
            out.writeBytes("PROXY DI-BLUE3 ON\n");
            out.writeBytes("PROXY DI-BLUE4 ON\n");
            out.writeBytes("PROXY DI-BLUE5 ON\n");
            out.writeBytes("PROXY DI-BLUE6 ON\n");
            out.writeBytes("PROXY DI-BLUE7 ON\n");
            out.writeBytes("PROXY DI-BLUE8 ON\n");
            out.writeBytes("PROXY DI-BLUE9 ON\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("# Taxiway\n");
            out.writeBytes("PAVEMENT PAV01  30.42823572 -097.44638464 30.42790706 -097.44638521 30.42776885 -097.44622133 30.42790468 -097.44605544 30.42823333 -097.44605486 30.42824858 -097.44605436 30.42857484 -097.44572401 30.42856821 -097.44454177 30.42823726 -097.44420853 30.42822374 -097.44420764 30.42789517 -097.44420417 30.42775674 -097.44403934 30.42789181 -097.44387462 30.42822038 -097.44387808 30.42823487 -097.44387875 30.42856104 -097.44355244 30.42855457 -097.44238423 30.42822401 -097.44206596 30.42819471 -097.44206770 30.42786653 -097.44207921 30.42772847 -097.44198257 30.42772750 -097.44184524 30.42786414 -097.44174943 30.42819232 -097.44173793 30.42822259 -097.44173596 30.42854890 -097.44149897 30.42854703 -097.44127349 30.42853667 -097.43868647 30.42853304 -097.43760596 30.42885949 -097.43726753 30.42992656 -097.43724006 30.43005167 -097.43723628 30.43021669 -097.43723232 30.43037916 -097.43702211 30.43037916 -097.43702211 30.43078724 -097.43738775 30.43038495 -097.43796558 30.43038495 -097.43796558 30.43022200 -097.43756142 30.43006012 -097.43756566 30.42993695 -097.43756899 30.42918974 -097.43758889 30.42886232 -097.43792754 30.42886332 -097.43798955 30.42900450 -097.43819291 30.42943528 -097.43800912 30.42989454 -097.43808181 30.43006159 -097.43821092 30.43016497 -097.43829046 30.43027692 -097.43837607 30.43038606 -097.43805468 30.43038606 -097.43805468 30.43079456 -097.43882585 30.43039159 -097.43928536 30.43039159 -097.43928536 30.43017779 -097.43871422 30.42996507 -097.43855148 30.42986169 -097.43847193 30.42975310 -097.43838756 30.42947130 -097.43833816 30.42908136 -097.43856574 30.42886628 -097.43908930 30.42887425 -097.44077425 30.42920397 -097.44109773 30.42994764 -097.44108468 30.43007200 -097.44108207 30.43023605 -097.44107834 30.43039848 -097.44066834 30.43039848 -097.44066834 30.43080606 -097.44123590 30.43040453 -097.44181255 30.43040453 -097.44181255 30.43024060 -097.44140862 30.43007850 -097.44141191 30.42995414 -097.44141451 30.42920539 -097.44142773 30.42887851 -097.44165782 30.42888016 -097.44188235 30.42889361 -097.44404796 30.42890328 -097.44572248 30.42923366 -097.44604882 30.43005629 -097.44604063 30.43020456 -097.44603954 30.43031681 -097.44603772 30.43042647 -097.44562986 30.43042647 -097.44562986 30.43083547 -097.44619911 30.43043349 -097.44677385 30.43043349 -097.44677385 30.43032039 -097.44636823 30.43020911 -097.44636982 30.43006085 -097.44637092 30.42923507 -097.44637883 30.42890925 -097.44671108 30.42891831 -097.44836967 30.42893032 -097.45041815 30.42926071 -097.45074450 30.43005144 -097.45073564 30.43020359 -097.45073365 30.43033033 -097.45073250 30.43045427 -097.45032436 30.43045427 -097.45032436 30.43086305 -097.45089265 30.43046108 -097.45146739 30.43046108 -097.45146739 30.43033369 -097.45106205 30.43020814 -097.45106393 30.43005599 -097.45106592 30.42926288 -097.45107332 30.42893727 -097.45140653 30.42896598 -097.45622030 30.42929693 -097.45655354 30.42998671 -097.45655996 30.43017024 -097.45656076 30.43033052 -097.45656291 30.43048778 -097.45615712 30.43048778 -097.45615712 30.43089668 -097.45673040 30.43049480 -097.45730111 30.43049480 -097.45730111 30.43033074 -097.45689219 30.43016852 -097.45689048 30.42998499 -097.45688968 30.42929834 -097.45688354 30.42897120 -097.45721008 30.42897373 -097.45790429 30.42898578 -097.46053410 30.42898662 -097.46077287 30.42924739 -097.46116147 30.42969696 -097.46124543 30.42992231 -097.46113015 30.42996116 -097.46108102 30.43006177 -097.46095337 30.43030776 -097.46064356 30.43050963 -097.45997847 30.43050963 -097.45997847 30.43091802 -097.46039424 30.43051775 -097.46131799 30.43051775 -097.46131799 30.43041757 -097.46103489 30.43032019 -097.46115878 30.43021957 -097.46128643 30.43015038 -097.46137269 30.42977912 -097.46156583 30.42923144 -097.46153162 30.42898843 -097.46172127 30.42900090 -097.46475661 30.42933027 -097.46507415 30.43006458 -097.46504720 30.43020764 -097.46504128 30.43037341 -097.46503614 30.43053615 -097.46462306 30.43053615 -097.46462306 30.43094502 -097.46518731 30.43054178 -097.46555250 30.43054178 -097.46555250 30.43038088 -097.46536575 30.43022041 -097.46537167 30.43007736 -097.46537759 30.42900373 -097.46541661 30.42867435 -097.46509907 30.42865810 -097.46110675 30.42865832 -097.46086372 30.42838540 -097.46043575 30.42804609 -097.46020384 30.42777391 -097.46001868 30.42804624 -097.45980520 30.42831842 -097.45999036 30.42838435 -097.46003639 30.42865512 -097.45989155 30.42864780 -097.45840023 30.42833925 -097.45807085 30.42803309 -097.45807125 30.42775917 -097.45804714 30.42775624 -097.45777271 30.42802973 -097.45774170 30.42833784 -097.45774085 30.42864355 -097.45741022 30.42864330 -097.45720947 30.42833130 -097.45688290 30.42801974 -097.45688655 30.42774591 -097.45685840 30.42761514 -097.45658035 30.42793268 -097.45655825 30.42826068 -097.45655482 30.42831172 -097.45655408 30.42863732 -097.45622087 30.42860798 -097.45138215 30.42860559 -097.45105237 30.42860461 -097.45077749 30.42860222 -097.45044771 30.42859282 -097.44886752 30.42826190 -097.44854330 30.42811797 -097.44854541 30.42778944 -097.44855097 30.42792374 -097.44821876 30.42825660 -097.44821420 30.42858684 -097.44787892 30.42858006 -097.44671379 30.42824999 -097.44638436 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV02  30.42852892 -097.45132811 30.42853167 -097.45193894 30.42825839 -097.45193978 30.42825467 -097.45132918 30.42825369 -097.45105430 30.42824644 -097.45105396 30.42824137 -097.45132924 30.42824481 -097.45195195 30.42797056 -097.45195302 30.42796733 -097.45133125 30.42795402 -097.45105621 30.42794365 -097.45133070 30.42794678 -097.45195650 30.42767349 -097.45195734 30.42767059 -097.45133249 30.42766863 -097.45105784 30.42766452 -097.45105778 30.42766139 -097.45133261 30.42766377 -097.45195958 30.42739049 -097.45196043 30.42738811 -097.45133345 30.42738713 -097.45105857 30.42738107 -097.45105896 30.42737600 -097.45133424 30.42737952 -097.45197501 30.42710623 -097.45197585 30.42710272 -097.45133508 30.42710174 -097.45106020 30.42709546 -097.45105964 30.42709039 -097.45133491 30.42709412 -097.45197664 30.42682084 -097.45197748 30.42681711 -097.45133575 30.42681613 -097.45106087 30.42680888 -097.45106054 30.42679992 -097.45133671 30.42679882 -097.45198859 30.42652510 -097.45198752 30.42652663 -097.45133755 30.42652760 -097.45106222 30.42651841 -097.45106234 30.42650825 -097.45133778 30.42650707 -097.45199369 30.42623334 -097.45199263 30.42623497 -097.45133862 30.42622069 -097.45106380 30.42620620 -097.45133824 30.42620863 -097.45200536 30.42593535 -097.45200620 30.42593194 -097.45106420 30.42615103 -097.45078871 30.42637111 -097.45078810 30.42666374 -097.45078681 30.42695130 -097.45078569 30.42723691 -097.45078501 30.42752230 -097.45078338 30.42780380 -097.45078265 30.42810033 -097.45078046 30.42838886 -097.45077911 30.42849722 -097.45077819 30.42860222 -097.45044771 30.42860222 -097.45044771 30.42876931 -097.45091417 30.42860798 -097.45138215 30.42860798 -097.45138215 30.42856666 -097.45105232 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV03  30.42790804 -097.44652254 30.42791111 -097.44688630 30.42792276 -097.44808143 30.42825660 -097.44821420 30.42825660 -097.44821420 30.42811797 -097.44854541 30.42811797 -097.44854541 30.42778847 -097.44841365 30.42777577 -097.44709426 30.42763811 -097.44695937 30.42675851 -097.44697368 30.42620425 -097.44697312 30.42564806 -097.44697301 30.42463103 -097.44696885 30.42463125 -097.44683225 30.42564730 -097.44683664 30.42599770 -097.44683707 30.42613385 -097.44669921 30.42613056 -097.44619695 30.42599245 -097.44606015 30.42463687 -097.44606688 30.42463589 -097.44592955 30.42599148 -097.44592282 30.42612763 -097.44578496 30.42612307 -097.44531713 30.42598496 -097.44518033 30.42464949 -097.44518543 30.42464851 -097.44504810 30.42598399 -097.44504300 30.42612014 -097.44490514 30.42611711 -097.44449720 30.42597901 -097.44436040 30.42464676 -097.44436174 30.42464579 -097.44422442 30.42597803 -097.44422308 30.42611418 -097.44408521 30.42611110 -097.44374355 30.42597375 -097.44360558 30.42464990 -097.44359895 30.42465012 -097.44346235 30.42597255 -097.44346730 30.42610795 -097.44333061 30.42610462 -097.44301009 30.42596652 -097.44287329 30.42463794 -097.44287278 30.42463794 -097.44273523 30.42596554 -097.44273596 30.42610169 -097.44259810 30.42609819 -097.44223244 30.42623359 -097.44209575 30.42656074 -097.44209754 30.42669452 -097.44198934 30.42669214 -097.44188145 30.42668060 -097.44169837 30.42653587 -097.44155507 30.42586807 -097.44152852 30.42587337 -097.44139175 30.42653176 -097.44141746 30.42666826 -097.44128555 30.42666865 -097.44086577 30.42666882 -097.44077336 30.42653228 -097.44067436 30.42585904 -097.44064203 30.42586607 -097.44050386 30.42653227 -097.44053680 30.42666899 -097.44040584 30.42666864 -097.44020913 30.42666906 -097.43990481 30.42653211 -097.43975971 30.42585659 -097.43972189 30.42586383 -097.43958467 30.42653113 -097.43962239 30.42666816 -097.43954155 30.42680465 -097.43946285 30.42729037 -097.43949931 30.42772445 -097.43952059 30.42786117 -097.43966473 30.42786304 -097.44027174 30.42786334 -097.44089418 30.42786413 -097.44161188 30.42819232 -097.44173793 30.42819232 -097.44173793 30.42819471 -097.44206770 30.42819471 -097.44206770 30.42786737 -097.44212721 30.42786821 -097.44217521 30.42787686 -097.44280275 30.42788428 -097.44342052 30.42788651 -097.44354552 30.42788986 -097.44373752 30.42822038 -097.44387808 30.42822038 -097.44387808 30.42822374 -097.44420764 30.42822374 -097.44420764 30.42789535 -097.44424931 30.42789673 -097.44429518 30.42790763 -097.44510995 30.42790382 -097.44597631 30.42790462 -097.44601529 30.42823333 -097.44605486 30.42823333 -097.44605486 30.42823572 -097.44638464 30.42823572 -097.44638464 30.42776312 -097.44450174 30.42762479 -097.44436399 30.42717241 -097.44436100 30.42638964 -097.44435998 30.42625424 -097.44449666 30.42625727 -097.44490461 30.42639537 -097.44504141 30.42718428 -097.44503799 30.42763331 -097.44503975 30.42776774 -097.44490329 30.42775674 -097.44403934 30.42775290 -097.44375207 30.42761382 -097.44361550 30.42716232 -097.44361632 30.42638406 -097.44360824 30.42624845 -097.44374397 30.42625131 -097.44408468 30.42638866 -097.44422265 30.42717359 -097.44422417 30.42762262 -097.44422593 30.42775776 -097.44413248 30.42680571 -097.43970257 30.42680605 -097.44000572 30.42694277 -097.44014986 30.42728675 -097.44016683 30.42758975 -097.44018825 30.42772615 -097.44006037 30.42772521 -097.43979451 30.42758848 -097.43965037 30.42728291 -097.43963558 30.42694107 -097.43961008 30.42680518 -097.43965149 30.42776885 -097.44622133 30.42776818 -097.44613414 30.42763105 -097.44605033 30.42718196 -097.44606164 30.42640308 -097.44605973 30.42626768 -097.44619641 30.42627097 -097.44669868 30.42640908 -097.44683547 30.42675731 -097.44683540 30.42763713 -097.44682204 30.42777284 -097.44668227 30.42626475 -097.44578443 30.42640211 -097.44592240 30.42718076 -097.44592336 30.42763104 -097.44591278 30.42776750 -097.44577184 30.42776970 -097.44531550 30.42763332 -097.44517730 30.42718504 -097.44517436 30.42639635 -097.44517873 30.42626020 -097.44531659 30.42623882 -097.44259757 30.42637692 -097.44273437 30.42715351 -097.44273078 30.42760276 -097.44273349 30.42773718 -097.44259704 30.42773130 -097.44217670 30.42773037 -097.44207952 30.42759219 -097.44197788 30.42696615 -097.44195474 30.42683398 -097.44208767 30.42683349 -097.44209883 30.42670122 -097.44223579 30.42637213 -097.44223446 30.42623673 -097.44237114 30.42681656 -097.44167501 30.42696106 -097.44181736 30.42759122 -097.44184056 30.42772749 -097.44170769 30.42772616 -097.44109854 30.42758912 -097.44095748 30.42725027 -097.44094937 30.42694288 -097.44094001 30.42680585 -097.44107406 30.42680524 -097.44149288 30.42761090 -097.44347862 30.42774770 -097.44345005 30.42774737 -097.44342201 30.42774303 -097.44300835 30.42760471 -097.44287060 30.42715426 -097.44286716 30.42637790 -097.44287169 30.42624175 -097.44300955 30.42624552 -097.44333199 30.42638309 -097.44347091 30.42716350 -097.44347950 30.42680565 -097.44061626 30.42680623 -097.44070750 30.42694287 -097.44080246 30.42725340 -097.44081210 30.42758912 -097.44081993 30.42772615 -097.44068588 30.42772617 -097.44047302 30.42758975 -097.44032580 30.42727928 -097.44030310 30.42694278 -097.44028741 30.42680606 -097.44041837 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV04  30.42758915 -097.45654318 30.42756023 -097.45623158 30.42755651 -097.45586496 30.42782935 -097.45586221 30.42783315 -097.45614046 30.42788493 -097.45648895 30.42826068 -097.45655482 30.42826068 -097.45655482 30.42833130 -097.45688290 30.42833130 -097.45688290 30.42802267 -097.45716098 30.42802680 -097.45746727 30.42833784 -097.45774085 30.42833784 -097.45774085 30.42833925 -097.45807085 30.42833925 -097.45807085 30.42803516 -097.45831978 30.42803700 -097.45856735 30.42804428 -097.45953055 30.42831842 -097.45999036 30.42831842 -097.45999036 30.42804609 -097.46020384 30.42804609 -097.46020384 30.42777195 -097.45974403 30.42776568 -097.45898040 30.42772793 -097.45870597 30.42754728 -097.45893945 30.42731603 -097.45931313 30.42708359 -097.45916699 30.42739392 -097.45866561 30.42765028 -097.45843173 30.42776036 -097.45823864 30.42748602 -097.45805297 30.42740470 -097.45805568 30.42698788 -097.45829549 30.42663930 -097.45886192 30.42640610 -097.45871696 30.42666453 -097.45829684 30.42653513 -097.45806466 30.42589210 -097.45806553 30.42589112 -097.45779065 30.42663354 -097.45778993 30.42688745 -097.45751444 30.42688361 -097.45747115 30.42659074 -097.45719638 30.42588076 -097.45719563 30.42587999 -097.45692171 30.42657018 -097.45692100 30.42683897 -097.45685192 30.42660107 -097.45663776 30.42584216 -097.45616336 30.42598702 -097.45593011 30.42659404 -097.45631006 30.42682528 -097.45618037 30.42682323 -097.45582541 30.42709651 -097.45582457 30.42709934 -097.45629381 30.42735773 -097.45657452 30.42712829 -097.45704919 30.42716105 -097.45751051 30.42745454 -097.45777911 30.42748309 -097.45777854 30.42775332 -097.45749828 30.42774884 -097.45713284 30.42747206 -097.45685235 30.42738651 -097.45685001 30.42712096 -097.45694646 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV05  30.42558035 -097.45851201 30.42554227 -097.45575166 30.42574999 -097.45563543 30.42572353 -097.45509935 30.42655947 -097.45503789 30.42657508 -097.45540277 30.42661300 -097.45550044 30.42667627 -097.45555712 30.42676112 -097.45560079 30.42849399 -097.45552904 30.42875915 -097.45528007 30.42876891 -097.45585916 30.42850854 -097.45570742 30.42818036 -097.45571892 30.42803064 -097.45581574 30.42804528 -097.45637161 30.42819663 -097.45646820 30.42852813 -097.45644889 30.42872672 -097.45631971 30.42872974 -097.45710919 30.42853584 -097.45700134 30.42819706 -097.45701128 30.42805442 -097.45709442 30.42805961 -097.45758720 30.42821903 -097.45766988 30.42853651 -097.45766084 30.42873034 -097.45755987 30.42875237 -097.45828974 30.42854179 -097.45820281 30.42822319 -097.45822917 30.42806873 -097.45833210 30.42810076 -097.46024544 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV06  30.42955044 -097.46136059 30.42953961 -097.46179281 30.42937374 -097.46177087 30.42915015 -097.46170301 30.42886588 -097.46149654 30.42877862 -097.46095442 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV07  30.42868889 -097.45195887 30.42582926 -097.45203471 30.42582325 -097.44937339 30.42619878 -097.44938508 30.42622686 -097.45046497 30.42635061 -097.45058400 30.42647685 -097.45064722 30.42659196 -097.45065780 30.42669957 -097.45065805 30.42670022 -097.45005749 30.42795704 -097.45004444 30.42796336 -097.45051990 30.42809183 -097.45051735 30.42823540 -097.45047818 30.42835195 -097.45041513 30.42844938 -097.45033139 30.42854096 -097.45021788 30.42867269 -097.44994549 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV08  30.43051787 -097.44205948 30.42994506 -097.44125094 30.43054769 -097.44049834 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV09  30.42859694 -097.43746570 30.42966801 -097.43751763 30.42914746 -097.43767194 30.42902758 -097.43771167 30.42893369 -097.43778857 30.42867721 -097.43815502 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV10  30.42947104 -097.43780909 30.42948321 -097.43807034 30.42876258 -097.43854696 30.42883157 -097.43810934 30.42913584 -097.43790555 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV11  30.43051893 -097.43812441 30.43032708 -097.43779921 30.43024268 -097.43770424 30.43012660 -097.43764067 30.42979649 -097.43749298 30.43057753 -097.43741106 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV12  30.42889259 -097.46531677 30.42893738 -097.46433954 30.42909257 -097.46482983 30.42919117 -097.46495968 30.42929943 -097.46501601 30.42980120 -097.46514415 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV13  30.42991917 -097.46516711 30.43028037 -097.46500139 30.43041275 -097.46492264 30.43047981 -097.46481378 30.43060428 -097.46454106 30.43068406 -097.46518832 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV14  30.42872228 -097.44063061 30.42925904 -097.44123864 30.42871578 -097.44191126 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV15  30.43057002 -097.44694816 30.43004279 -097.44620841 30.43058361 -097.44554740 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV16  30.43004364 -097.45089708 30.43063755 -097.45007120 30.43063585 -097.45166098 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV17  30.42998234 -097.45674069 30.43068002 -097.45581856 30.43070136 -097.45762793 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV18  30.43075019 -097.46574536 30.43051362 -097.46551483 30.43048840 -097.46545438 30.43044023 -097.46541430 30.43038603 -097.46538364 30.43032378 -097.46536688 30.43027010 -097.46535618 30.43029774 -097.46523635 30.43070057 -097.46525981 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV19  30.42559922 -097.44204542 30.42556976 -097.44236146 30.42546788 -097.44233278 30.42546219 -097.44198870 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV20  30.42815415 -097.44176782 30.42773756 -097.44176460 30.42742350 -097.44146461 30.42741170 -097.43914198 30.42791484 -097.43912522 30.42794620 -097.44141723 30.42808036 -097.44156799 30.42821877 -097.44161736 \n");
            out.writeBytes("# Apron \n");
            out.writeBytes("PAVEMENT PAV21  30.43096051 -097.46691370 30.43053882 -097.46693274 30.43035482 -097.43563011 30.43079563 -097.43563175 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV22  30.42801680 -097.44875050 30.42576299 -097.44879291 30.42575163 -097.44718204 30.42410232 -097.44721642 30.42410546 -097.44222562 30.42545217 -097.44216270 30.42534431 -097.43920661 30.42790176 -097.43912623 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV23  30.42795908 -097.44875780 30.42790032 -097.44081034 30.42794271 -097.44080758 30.42794939 -097.44126689 30.42796194 -097.44132122 30.42797426 -097.44137460 30.42801897 -097.44138637 30.42804709 -097.44141502 30.42807894 -097.44142875 30.42813923 -097.44141483 30.42820633 -097.44139934 30.42827246 -097.44138407 30.42833955 -097.44136858 30.42839401 -097.44135601 30.42844441 -097.44133032 30.42848317 -097.44128523 30.42853203 -097.44125286 30.42855066 -097.44117828 30.42857490 -097.44110139 30.42856707 -097.44253597 30.42852301 -097.44249594 30.42848542 -097.44242630 30.42843168 -097.44237545 30.42839367 -097.44233503 30.42836071 -097.44228541 30.42831138 -097.44225362 30.42816113 -097.44224614 30.42810829 -097.44223023 30.42805173 -097.44222923 30.42799298 -097.44224982 30.42794193 -097.44230378 30.42796024 -097.44337688 30.42798049 -097.44346458 30.42800074 -097.44355228 30.42806491 -097.44364389 30.42814666 -097.44369630 30.42819717 -097.44369770 30.42827690 -097.44367929 30.42835630 -097.44365493 30.42841659 -097.44364101 30.42845785 -097.44360237 30.42849464 -097.44357982 30.42852835 -097.44354392 30.42856059 -097.44346620 30.42859580 -097.44334361 30.42860020 -097.44478239 30.42854438 -097.44469587 30.42848766 -097.44457443 30.42844904 -097.44443374 30.42842310 -097.44438350 30.42840529 -097.44433741 30.42838551 -097.44431387 30.42834475 -097.44428814 30.42808664 -097.44427041 30.42805941 -097.44427670 30.42800044 -097.44429634 30.42797222 -097.44432494 30.42798045 -097.44588232 30.42802065 -097.44590115 30.42806360 -097.44590529 30.42812389 -097.44589137 30.42817308 -097.44588704 30.42823337 -097.44587312 30.42832145 -097.44585982 30.42839382 -097.44583608 30.42845411 -097.44582216 30.42848879 -097.44578604 30.42852031 -097.44577174 30.42853521 -097.44571207 30.42855484 -097.44561517 30.42855591 -097.44555769 30.42860631 -097.44553200 30.42858700 -097.44685476 30.42856479 -097.44678960 30.42855225 -097.44673527 30.42853466 -097.44669013 30.42850015 -097.44663384 30.42849245 -097.44660048 30.42843695 -097.44654200 30.42841564 -097.44651178 30.42836652 -097.44648095 30.42828492 -097.44646465 30.42822133 -097.44646427 30.42818267 -097.44645211 30.42810666 -097.44645560 30.42803859 -097.44647132 30.42799238 -097.44654624 30.42800383 -097.44815308 30.42809851 -097.44813724 30.42816714 -097.44812842 30.42823424 -097.44811293 30.42829453 -097.44809901 30.42836844 -097.44808195 30.42843553 -097.44806646 30.42849296 -097.44804015 30.42852280 -097.44798306 30.42854662 -097.44793539 30.42856657 -097.44786653 30.42860289 -097.44777983 30.42859182 -097.44908060 30.42856785 -097.44900782 30.42853772 -097.44890835 30.42852055 -097.44883400 30.42849812 -097.44876789 30.42847161 -097.44871076 30.42843887 -097.44866209 30.42841909 -097.44863854 30.42837657 -097.44860519 30.42832373 -097.44858927 30.42822324 -097.44858436 30.42810989 -097.44858141 30.42806905 -097.44859084 30.42800876 -097.44860476 30.42801530 -097.44875285 \n");

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

    public void makeEnvFileACCASTSmall() {

        boolean shortenScenario = true;
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + "Env.txt"));

            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes(SANJAYA_UPDATE_RATE_COMMENT);
            out.writeBytes("UPDATE_RATE " + sanjayaUpdateRate + "\n\n");

            out.writeBytes("GUI_ON " + GUI_ON + "\n");

            out.writeBytes("DAMAGE_MODE Cumulative\n\n");
            out.writeBytes("DAMAGE_MODE_BLUEFOR_DI " + blueforDIDamage + " \n\n");

            out.writeBytes("MAP_WIDTH_METERS " + 5000 + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + 5000 + "\n");
            out.writeBytes("\n");

            out.writeBytes(USE_XPLANE_CAMERA_COMMENT);
            out.writeBytes("USE_XPLANE_CAMERA " + USE_XPLANE_CAMERA + "\n");
            out.writeBytes("USE_XPLANE_CAMERA_HOST " + USE_XPLANE_CAMERA_HOST + "\n");
            out.writeBytes("\n");
            out.writeBytes("GUI_MARK_XPLANE_ASSET true\n");
            out.writeBytes("\n");

            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + 5200 + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + 5200 + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES false\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM " + sanjayaOneKmGridLinesOn + "\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n");

            out.writeBytes("\n");
            out.writeBytes(SANJAYA_ASSET_COLLISION_COMMENT);
            out.writeBytes("ASSET_COLLISION_DETECTION_ON " + sanjayaAssetCollisionDetectionOn + "\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_RANGE_METERS " + 100 + "\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_MIN_Z_DIST_METERS " + sanjayaAssetCollisionMinZDistMeters + "\n");

            out.writeBytes("\n");
            out.writeBytes(END_CONDITION_COMMENT);
            out.writeBytes("END_CONDITION_CAPTURE_THE_FLAG " + END_CONDITION_CAPTURE_THE_FLAG + "\n");
            out.writeBytes("END_CONDITION_ALL_HMMMVS_DEAD " + END_CONDITION_ALL_HMMMVS_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_OPFOR_DEAD " + END_CONDITION_ALL_OPFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_BLUEFOR_DEAD " + END_CONDITION_ALL_BLUEFOR_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_EMITTERS_DETECTED " + END_CONDITION_ALL_EMITTERS_DETECTED + "\n");
            out.writeBytes("END_CONDITION_ALL_INFANTRY_DEAD " + END_CONDITION_ALL_INFANTRY_DEAD + "\n");
            out.writeBytes("END_CONDITION_ALL_GOALS_HELD " + END_CONDITION_ALL_GOALS_HELD + "\n");

            out.writeBytes("END_CONDITION_GOAL 500 2250 0\n");
            out.writeBytes("END_CONDITION_GOAL 2500 2250 0\n");
            out.writeBytes("END_CONDITION_GOAL 1500 1500 0\n");
            out.writeBytes("END_CONDITION_GOAL 1950 1250 0\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");

            out.writeBytes("\n");

            // NOTE: If you change the emitters here you should change
            // the emitters in _createUAVCfgFile to match it.
            writeSA9Locations(out);

            out.writeBytes("CTDB_BASE_NAME " + getCtdbBaseName() + "\n");

            out.writeBytes("ASSET_CONFIG AT_LOCATION_DISTANCE " + uavAtLocationDistance + "\n");
            out.writeBytes("ASSET_CONFIG MOVE_FINISHED_HOLD_RADIUS " + uavMoveFinishedHoldRadius + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_SPEED_KPH " + mps_to_kph(uavMaxSpeedMetersPerSec) + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_TURN_RATE_DEG " + uavMaxTurnRateDegPerStep + "\n");
            out.writeBytes("ASSET_CONFIG RSSI_MAP_SCALE " + 5.0 + "\n");

            out.writeBytes("\n");
            out.writeBytes("TERMINAL TM-0 1500 1500 1000 400\n");
            out.writeBytes("\n");
            out.writeBytes("######################################################################\n");
            out.writeBytes("# OPPOSING FORCES\n");
            out.writeBytes("######################################################################\n");
            out.writeBytes("\n");
            out.writeBytes("SA9 SA9-0 300 600 OPFOR\n");
            out.writeBytes("SA9 SA9-1 1500 600 OPFOR\n");
            out.writeBytes("SA9 SA9-2 2700 600 OPFOR\n");
            out.writeBytes("SA9 SA9-3 300 2500 OPFOR\n");
            out.writeBytes("SA9 SA9-4 2700 2500 OPFOR\n");
            out.writeBytes("\n");
            /*
             out.writeBytes("INFANTRY DI-OP0 400 700 OPFOR ADA0\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP1 1500 700 OPFOR ADA1\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP2 2600 700 OPFOR ADA2\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP3 400 2400 OPFOR ADA3\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP4 2600 2400 OPFOR ADA4\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY DI-OP5 1600 700 OPFOR GATE6\n");
             out.writeBytes("INFANTRY DI-OP6 1700 700 OPFOR GATE6\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("A10 A10-OP0 1900 1200 0 OPFOR C2\n");
             out.writeBytes("A10 A10-OP1 1950 1200 0 OPFOR C2\n");
             out.writeBytes("A10 A10-OP2 2000 1200 0 OPFOR C2\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C00 1290 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C01 1295 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C02 1300 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C03 1305 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C04 1310 1100 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C05 1890 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C06 1895 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C07 1900 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C08 1905 1100 NEUTRAL\n");
             out.writeBytes("CIVILIAN C09 1910 1100 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C10 290 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C11 295 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C12 300 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C13 305 1900 NEUTRAL\n");
             out.writeBytes("CIVILIAN C14 310 1900 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("CIVILIAN C15 1490 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C16 1495 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C17 1500 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C18 1505 1800 NEUTRAL\n");
             out.writeBytes("CIVILIAN C19 1510 1800 NEUTRAL\n");
             out.writeBytes("\n");
             out.writeBytes("TASK DI-OP0 DEFEND 390 700 false true\n");
             out.writeBytes("TASK DI-OP1 DEFEND 1290 700 false true\n");
             out.writeBytes("TASK DI-OP2 DEFEND 2190 700 false true\n");
             out.writeBytes("TASK DI-OP3 DEFEND 390 2400 false true\n");
             out.writeBytes("TASK DI-OP4 DEFEND 2190 2400 false true\n");
             out.writeBytes("TASK DI-OP5 DEFEND 1450 700 false true\n");
             out.writeBytes("TASK DI-OP6 DEFEND 1540 700 false true\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             */
            out.writeBytes("######################################################################$\n");
            out.writeBytes("# BLUE FORCES\n");
            out.writeBytes("######################################################################$\n");

            out.writeBytes("\n");
            out.writeBytes("\n");
            if (shortenScenario) {
                for (int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
                    out.writeBytes("SMALL_UAV AUAV" + loopAuavs + " 4500 4500 500 34000 BLUEFOR\n");
                    out.writeBytes("SENSOR SAR AUAV" + loopAuavs + "\n");
                }
                for (int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + loopEoiruavs + " 3500 3000 500 18000 BLUEFOR\n");
                    out.writeBytes("SENSOR EOIR EOIR-UAV" + loopEoiruavs + "\n");
                }
            } else {
                for (int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
                    out.writeBytes("SMALL_UAV AUAV" + loopAuavs + " 4500 3500 500 34000 BLUEFOR\n");
                    out.writeBytes("SENSOR SAR AUAV" + loopAuavs + "\n");
                }
                for (int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
                    out.writeBytes("SMALL_UAV EOIR-UAV" + loopEoiruavs + " 4500 3500 500 18000 BLUEFOR\n");
                    out.writeBytes("SENSOR EOIR EOIR-UAV" + loopEoiruavs + "\n");
                }
            }

            /*
             out.writeBytes("\n");
             out.writeBytes("FAARP FAARP 4600 3600 BLUEFOR\n");
             out.writeBytes("\n");

             out.writeBytes("C130 C130-0 6000 5000 1000 BLUEFOR AIRLIFT0 180\n");

             out.writeBytes("\n");
             out.writeBytes("# initial UGV assault (before SA9's taken out)\n");
             if(shortenScenario) {
             out.writeBytes("AUGV AUGV0 -500 1500 BLUEFOR ASSAULT1\n");
             out.writeBytes("AUGV AUGV1 3500 1500 BLUEFOR ASSAULT1\n");
             } 
             else {
             out.writeBytes("AUGV AUGV0 -1000 3000 BLUEFOR ASSAULT1\n");
             out.writeBytes("AUGV AUGV1 4000 -100 BLUEFOR ASSAULT1\n");
             }
             out.writeBytes("SENSOR LADAR AUGV0\n");
             out.writeBytes("SENSOR LADAR AUGV1\n");
             out.writeBytes("\n");
             out.writeBytes("# Troop convoy for front gate\n");
             if(shortenScenario) {
             out.writeBytes("HUMMER H0 3300 250 BLUEFOR ASSAULT2\n");
             out.writeBytes("HUMMER H1 3350 250 BLUEFOR ASSAULT2\n");
             out.writeBytes("AUGV AUGV2 3250 260 BLUEFOR ASSAULT2\n");
             out.writeBytes("AUGV AUGV3 3250 240 BLUEFOR ASSAULT2\n");
             }
             else {
             out.writeBytes("HUMMER H0 4000 250 BLUEFOR ASSAULT2\n");
             out.writeBytes("HUMMER H1 4050 250 BLUEFOR ASSAULT2\n");
             out.writeBytes("AUGV AUGV2 3800 260 BLUEFOR ASSAULT2\n");
             out.writeBytes("AUGV AUGV3 3800 240 BLUEFOR ASSAULT2\n");
             }
             out.writeBytes("SENSOR MARKONEEYEBALL H0\n");
             out.writeBytes("SENSOR MARKONEEYEBALL H1\n");
             out.writeBytes("SENSOR LADAR AUGV2\n");
             out.writeBytes("SENSOR LADAR AUGV3\n");
             out.writeBytes("\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"0 6000 4800 BLUEFOR AIRLIFT00\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"0\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"1 6000 4800 BLUEFOR AIRLIFT00\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"1\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"2 6000 4800 BLUEFOR AIRLIFT01\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"2\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"3 6000 4800 BLUEFOR AIRLIFT01\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"3\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"4 6000 4800 BLUEFOR AIRLIFT02\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"4\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"5 6000 4800 BLUEFOR AIRLIFT02\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"5\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"6 4000 250 BLUEFOR ASSAULT21\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"6\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"7 4000 250 BLUEFOR ASSAULT21\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"7\n");
             out.writeBytes("\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"8 4000 250 BLUEFOR ASSAULT22\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"8\n");
             out.writeBytes("INFANTRY "+UnitIDs.DIBLUE+"9 4000 250 BLUEFOR ASSAULT22\n");
             out.writeBytes("SENSOR MARKONEEYEBALL "+UnitIDs.DIBLUE+"9\n");
             out.writeBytes("\n");
             out.writeBytes("MOUNT DI-BLUE0 C130-0\n");
             out.writeBytes("MOUNT DI-BLUE1 C130-0\n");
             out.writeBytes("\n");
             out.writeBytes("MOUNT DI-BLUE2 C130-0\n");
             out.writeBytes("MOUNT DI-BLUE3 C130-0\n");
             out.writeBytes("\n");
             out.writeBytes("MOUNT DI-BLUE4 C130-0\n");
             out.writeBytes("MOUNT DI-BLUE5 C130-0\n");
             out.writeBytes("\n");
             out.writeBytes("MOUNT DI-BLUE6 H0\n");
             out.writeBytes("MOUNT DI-BLUE7 H0\n");
             out.writeBytes("\n");
             out.writeBytes("MOUNT DI-BLUE8 H1\n");
             out.writeBytes("MOUNT DI-BLUE9 H1\n");
             out.writeBytes("\n");
             out.writeBytes("TASK H0 HOLD\n");
             out.writeBytes("TASK H1 HOLD\n");
             out.writeBytes("TASK AUGV0 HOLD\n");
             out.writeBytes("TASK AUGV1 HOLD\n");
             out.writeBytes("TASK AUGV2 HOLD\n");
             out.writeBytes("TASK AUGV3 HOLD\n");
             out.writeBytes("\n");
             out.writeBytes("TASK C130-0 LAND\n");
             out.writeBytes("\n");
            
             for(int loopEoiruavs = 0; loopEoiruavs < noEOIRUAVs; loopEoiruavs++) {
             out.writeBytes("PROXY EOIR-UAV"+loopEoiruavs+" ON\n");
             }
             for(int loopAuavs = 0; loopAuavs < noArmoredUAVs; loopAuavs++) {
             out.writeBytes("PROXY AUAV"+loopAuavs+" ON\n");
             }
            
             //out.writeBytes("PROXY C130-0 ON\n");
             out.writeBytes("\n");

             out.writeBytes("PROXY AUGV0 ON\n");
             out.writeBytes("PROXY AUGV1 ON\n");
             out.writeBytes("PROXY AUGV2 ON\n");
             out.writeBytes("PROXY AUGV3 ON\n");
	    
             out.writeBytes("PROXY H0 ON\n");
             out.writeBytes("PROXY H1 ON\n");
             out.writeBytes("\n");
             out.writeBytes("PROXY DI-BLUE0 ON\n");
             out.writeBytes("PROXY DI-BLUE1 ON\n");
             out.writeBytes("PROXY DI-BLUE2 ON\n");
             out.writeBytes("PROXY DI-BLUE3 ON\n");
             out.writeBytes("PROXY DI-BLUE4 ON\n");
             out.writeBytes("PROXY DI-BLUE5 ON\n");
             out.writeBytes("PROXY DI-BLUE6 ON\n");
             out.writeBytes("PROXY DI-BLUE7 ON\n");
             out.writeBytes("PROXY DI-BLUE8 ON\n");
             out.writeBytes("PROXY DI-BLUE9 ON\n");
             */
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("\n");
            out.writeBytes("# Taxiway\n");
            out.writeBytes("PAVEMENT PAV01  30.42823572 -097.44638464 30.42790706 -097.44638521 30.42776885 -097.44622133 30.42790468 -097.44605544 30.42823333 -097.44605486 30.42824858 -097.44605436 30.42857484 -097.44572401 30.42856821 -097.44454177 30.42823726 -097.44420853 30.42822374 -097.44420764 30.42789517 -097.44420417 30.42775674 -097.44403934 30.42789181 -097.44387462 30.42822038 -097.44387808 30.42823487 -097.44387875 30.42856104 -097.44355244 30.42855457 -097.44238423 30.42822401 -097.44206596 30.42819471 -097.44206770 30.42786653 -097.44207921 30.42772847 -097.44198257 30.42772750 -097.44184524 30.42786414 -097.44174943 30.42819232 -097.44173793 30.42822259 -097.44173596 30.42854890 -097.44149897 30.42854703 -097.44127349 30.42853667 -097.43868647 30.42853304 -097.43760596 30.42885949 -097.43726753 30.42992656 -097.43724006 30.43005167 -097.43723628 30.43021669 -097.43723232 30.43037916 -097.43702211 30.43037916 -097.43702211 30.43078724 -097.43738775 30.43038495 -097.43796558 30.43038495 -097.43796558 30.43022200 -097.43756142 30.43006012 -097.43756566 30.42993695 -097.43756899 30.42918974 -097.43758889 30.42886232 -097.43792754 30.42886332 -097.43798955 30.42900450 -097.43819291 30.42943528 -097.43800912 30.42989454 -097.43808181 30.43006159 -097.43821092 30.43016497 -097.43829046 30.43027692 -097.43837607 30.43038606 -097.43805468 30.43038606 -097.43805468 30.43079456 -097.43882585 30.43039159 -097.43928536 30.43039159 -097.43928536 30.43017779 -097.43871422 30.42996507 -097.43855148 30.42986169 -097.43847193 30.42975310 -097.43838756 30.42947130 -097.43833816 30.42908136 -097.43856574 30.42886628 -097.43908930 30.42887425 -097.44077425 30.42920397 -097.44109773 30.42994764 -097.44108468 30.43007200 -097.44108207 30.43023605 -097.44107834 30.43039848 -097.44066834 30.43039848 -097.44066834 30.43080606 -097.44123590 30.43040453 -097.44181255 30.43040453 -097.44181255 30.43024060 -097.44140862 30.43007850 -097.44141191 30.42995414 -097.44141451 30.42920539 -097.44142773 30.42887851 -097.44165782 30.42888016 -097.44188235 30.42889361 -097.44404796 30.42890328 -097.44572248 30.42923366 -097.44604882 30.43005629 -097.44604063 30.43020456 -097.44603954 30.43031681 -097.44603772 30.43042647 -097.44562986 30.43042647 -097.44562986 30.43083547 -097.44619911 30.43043349 -097.44677385 30.43043349 -097.44677385 30.43032039 -097.44636823 30.43020911 -097.44636982 30.43006085 -097.44637092 30.42923507 -097.44637883 30.42890925 -097.44671108 30.42891831 -097.44836967 30.42893032 -097.45041815 30.42926071 -097.45074450 30.43005144 -097.45073564 30.43020359 -097.45073365 30.43033033 -097.45073250 30.43045427 -097.45032436 30.43045427 -097.45032436 30.43086305 -097.45089265 30.43046108 -097.45146739 30.43046108 -097.45146739 30.43033369 -097.45106205 30.43020814 -097.45106393 30.43005599 -097.45106592 30.42926288 -097.45107332 30.42893727 -097.45140653 30.42896598 -097.45622030 30.42929693 -097.45655354 30.42998671 -097.45655996 30.43017024 -097.45656076 30.43033052 -097.45656291 30.43048778 -097.45615712 30.43048778 -097.45615712 30.43089668 -097.45673040 30.43049480 -097.45730111 30.43049480 -097.45730111 30.43033074 -097.45689219 30.43016852 -097.45689048 30.42998499 -097.45688968 30.42929834 -097.45688354 30.42897120 -097.45721008 30.42897373 -097.45790429 30.42898578 -097.46053410 30.42898662 -097.46077287 30.42924739 -097.46116147 30.42969696 -097.46124543 30.42992231 -097.46113015 30.42996116 -097.46108102 30.43006177 -097.46095337 30.43030776 -097.46064356 30.43050963 -097.45997847 30.43050963 -097.45997847 30.43091802 -097.46039424 30.43051775 -097.46131799 30.43051775 -097.46131799 30.43041757 -097.46103489 30.43032019 -097.46115878 30.43021957 -097.46128643 30.43015038 -097.46137269 30.42977912 -097.46156583 30.42923144 -097.46153162 30.42898843 -097.46172127 30.42900090 -097.46475661 30.42933027 -097.46507415 30.43006458 -097.46504720 30.43020764 -097.46504128 30.43037341 -097.46503614 30.43053615 -097.46462306 30.43053615 -097.46462306 30.43094502 -097.46518731 30.43054178 -097.46555250 30.43054178 -097.46555250 30.43038088 -097.46536575 30.43022041 -097.46537167 30.43007736 -097.46537759 30.42900373 -097.46541661 30.42867435 -097.46509907 30.42865810 -097.46110675 30.42865832 -097.46086372 30.42838540 -097.46043575 30.42804609 -097.46020384 30.42777391 -097.46001868 30.42804624 -097.45980520 30.42831842 -097.45999036 30.42838435 -097.46003639 30.42865512 -097.45989155 30.42864780 -097.45840023 30.42833925 -097.45807085 30.42803309 -097.45807125 30.42775917 -097.45804714 30.42775624 -097.45777271 30.42802973 -097.45774170 30.42833784 -097.45774085 30.42864355 -097.45741022 30.42864330 -097.45720947 30.42833130 -097.45688290 30.42801974 -097.45688655 30.42774591 -097.45685840 30.42761514 -097.45658035 30.42793268 -097.45655825 30.42826068 -097.45655482 30.42831172 -097.45655408 30.42863732 -097.45622087 30.42860798 -097.45138215 30.42860559 -097.45105237 30.42860461 -097.45077749 30.42860222 -097.45044771 30.42859282 -097.44886752 30.42826190 -097.44854330 30.42811797 -097.44854541 30.42778944 -097.44855097 30.42792374 -097.44821876 30.42825660 -097.44821420 30.42858684 -097.44787892 30.42858006 -097.44671379 30.42824999 -097.44638436 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV02  30.42852892 -097.45132811 30.42853167 -097.45193894 30.42825839 -097.45193978 30.42825467 -097.45132918 30.42825369 -097.45105430 30.42824644 -097.45105396 30.42824137 -097.45132924 30.42824481 -097.45195195 30.42797056 -097.45195302 30.42796733 -097.45133125 30.42795402 -097.45105621 30.42794365 -097.45133070 30.42794678 -097.45195650 30.42767349 -097.45195734 30.42767059 -097.45133249 30.42766863 -097.45105784 30.42766452 -097.45105778 30.42766139 -097.45133261 30.42766377 -097.45195958 30.42739049 -097.45196043 30.42738811 -097.45133345 30.42738713 -097.45105857 30.42738107 -097.45105896 30.42737600 -097.45133424 30.42737952 -097.45197501 30.42710623 -097.45197585 30.42710272 -097.45133508 30.42710174 -097.45106020 30.42709546 -097.45105964 30.42709039 -097.45133491 30.42709412 -097.45197664 30.42682084 -097.45197748 30.42681711 -097.45133575 30.42681613 -097.45106087 30.42680888 -097.45106054 30.42679992 -097.45133671 30.42679882 -097.45198859 30.42652510 -097.45198752 30.42652663 -097.45133755 30.42652760 -097.45106222 30.42651841 -097.45106234 30.42650825 -097.45133778 30.42650707 -097.45199369 30.42623334 -097.45199263 30.42623497 -097.45133862 30.42622069 -097.45106380 30.42620620 -097.45133824 30.42620863 -097.45200536 30.42593535 -097.45200620 30.42593194 -097.45106420 30.42615103 -097.45078871 30.42637111 -097.45078810 30.42666374 -097.45078681 30.42695130 -097.45078569 30.42723691 -097.45078501 30.42752230 -097.45078338 30.42780380 -097.45078265 30.42810033 -097.45078046 30.42838886 -097.45077911 30.42849722 -097.45077819 30.42860222 -097.45044771 30.42860222 -097.45044771 30.42876931 -097.45091417 30.42860798 -097.45138215 30.42860798 -097.45138215 30.42856666 -097.45105232 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV03  30.42790804 -097.44652254 30.42791111 -097.44688630 30.42792276 -097.44808143 30.42825660 -097.44821420 30.42825660 -097.44821420 30.42811797 -097.44854541 30.42811797 -097.44854541 30.42778847 -097.44841365 30.42777577 -097.44709426 30.42763811 -097.44695937 30.42675851 -097.44697368 30.42620425 -097.44697312 30.42564806 -097.44697301 30.42463103 -097.44696885 30.42463125 -097.44683225 30.42564730 -097.44683664 30.42599770 -097.44683707 30.42613385 -097.44669921 30.42613056 -097.44619695 30.42599245 -097.44606015 30.42463687 -097.44606688 30.42463589 -097.44592955 30.42599148 -097.44592282 30.42612763 -097.44578496 30.42612307 -097.44531713 30.42598496 -097.44518033 30.42464949 -097.44518543 30.42464851 -097.44504810 30.42598399 -097.44504300 30.42612014 -097.44490514 30.42611711 -097.44449720 30.42597901 -097.44436040 30.42464676 -097.44436174 30.42464579 -097.44422442 30.42597803 -097.44422308 30.42611418 -097.44408521 30.42611110 -097.44374355 30.42597375 -097.44360558 30.42464990 -097.44359895 30.42465012 -097.44346235 30.42597255 -097.44346730 30.42610795 -097.44333061 30.42610462 -097.44301009 30.42596652 -097.44287329 30.42463794 -097.44287278 30.42463794 -097.44273523 30.42596554 -097.44273596 30.42610169 -097.44259810 30.42609819 -097.44223244 30.42623359 -097.44209575 30.42656074 -097.44209754 30.42669452 -097.44198934 30.42669214 -097.44188145 30.42668060 -097.44169837 30.42653587 -097.44155507 30.42586807 -097.44152852 30.42587337 -097.44139175 30.42653176 -097.44141746 30.42666826 -097.44128555 30.42666865 -097.44086577 30.42666882 -097.44077336 30.42653228 -097.44067436 30.42585904 -097.44064203 30.42586607 -097.44050386 30.42653227 -097.44053680 30.42666899 -097.44040584 30.42666864 -097.44020913 30.42666906 -097.43990481 30.42653211 -097.43975971 30.42585659 -097.43972189 30.42586383 -097.43958467 30.42653113 -097.43962239 30.42666816 -097.43954155 30.42680465 -097.43946285 30.42729037 -097.43949931 30.42772445 -097.43952059 30.42786117 -097.43966473 30.42786304 -097.44027174 30.42786334 -097.44089418 30.42786413 -097.44161188 30.42819232 -097.44173793 30.42819232 -097.44173793 30.42819471 -097.44206770 30.42819471 -097.44206770 30.42786737 -097.44212721 30.42786821 -097.44217521 30.42787686 -097.44280275 30.42788428 -097.44342052 30.42788651 -097.44354552 30.42788986 -097.44373752 30.42822038 -097.44387808 30.42822038 -097.44387808 30.42822374 -097.44420764 30.42822374 -097.44420764 30.42789535 -097.44424931 30.42789673 -097.44429518 30.42790763 -097.44510995 30.42790382 -097.44597631 30.42790462 -097.44601529 30.42823333 -097.44605486 30.42823333 -097.44605486 30.42823572 -097.44638464 30.42823572 -097.44638464 30.42776312 -097.44450174 30.42762479 -097.44436399 30.42717241 -097.44436100 30.42638964 -097.44435998 30.42625424 -097.44449666 30.42625727 -097.44490461 30.42639537 -097.44504141 30.42718428 -097.44503799 30.42763331 -097.44503975 30.42776774 -097.44490329 30.42775674 -097.44403934 30.42775290 -097.44375207 30.42761382 -097.44361550 30.42716232 -097.44361632 30.42638406 -097.44360824 30.42624845 -097.44374397 30.42625131 -097.44408468 30.42638866 -097.44422265 30.42717359 -097.44422417 30.42762262 -097.44422593 30.42775776 -097.44413248 30.42680571 -097.43970257 30.42680605 -097.44000572 30.42694277 -097.44014986 30.42728675 -097.44016683 30.42758975 -097.44018825 30.42772615 -097.44006037 30.42772521 -097.43979451 30.42758848 -097.43965037 30.42728291 -097.43963558 30.42694107 -097.43961008 30.42680518 -097.43965149 30.42776885 -097.44622133 30.42776818 -097.44613414 30.42763105 -097.44605033 30.42718196 -097.44606164 30.42640308 -097.44605973 30.42626768 -097.44619641 30.42627097 -097.44669868 30.42640908 -097.44683547 30.42675731 -097.44683540 30.42763713 -097.44682204 30.42777284 -097.44668227 30.42626475 -097.44578443 30.42640211 -097.44592240 30.42718076 -097.44592336 30.42763104 -097.44591278 30.42776750 -097.44577184 30.42776970 -097.44531550 30.42763332 -097.44517730 30.42718504 -097.44517436 30.42639635 -097.44517873 30.42626020 -097.44531659 30.42623882 -097.44259757 30.42637692 -097.44273437 30.42715351 -097.44273078 30.42760276 -097.44273349 30.42773718 -097.44259704 30.42773130 -097.44217670 30.42773037 -097.44207952 30.42759219 -097.44197788 30.42696615 -097.44195474 30.42683398 -097.44208767 30.42683349 -097.44209883 30.42670122 -097.44223579 30.42637213 -097.44223446 30.42623673 -097.44237114 30.42681656 -097.44167501 30.42696106 -097.44181736 30.42759122 -097.44184056 30.42772749 -097.44170769 30.42772616 -097.44109854 30.42758912 -097.44095748 30.42725027 -097.44094937 30.42694288 -097.44094001 30.42680585 -097.44107406 30.42680524 -097.44149288 30.42761090 -097.44347862 30.42774770 -097.44345005 30.42774737 -097.44342201 30.42774303 -097.44300835 30.42760471 -097.44287060 30.42715426 -097.44286716 30.42637790 -097.44287169 30.42624175 -097.44300955 30.42624552 -097.44333199 30.42638309 -097.44347091 30.42716350 -097.44347950 30.42680565 -097.44061626 30.42680623 -097.44070750 30.42694287 -097.44080246 30.42725340 -097.44081210 30.42758912 -097.44081993 30.42772615 -097.44068588 30.42772617 -097.44047302 30.42758975 -097.44032580 30.42727928 -097.44030310 30.42694278 -097.44028741 30.42680606 -097.44041837 \n");
            out.writeBytes("# Apron path\n");
            out.writeBytes("PAVEMENT PAV04  30.42758915 -097.45654318 30.42756023 -097.45623158 30.42755651 -097.45586496 30.42782935 -097.45586221 30.42783315 -097.45614046 30.42788493 -097.45648895 30.42826068 -097.45655482 30.42826068 -097.45655482 30.42833130 -097.45688290 30.42833130 -097.45688290 30.42802267 -097.45716098 30.42802680 -097.45746727 30.42833784 -097.45774085 30.42833784 -097.45774085 30.42833925 -097.45807085 30.42833925 -097.45807085 30.42803516 -097.45831978 30.42803700 -097.45856735 30.42804428 -097.45953055 30.42831842 -097.45999036 30.42831842 -097.45999036 30.42804609 -097.46020384 30.42804609 -097.46020384 30.42777195 -097.45974403 30.42776568 -097.45898040 30.42772793 -097.45870597 30.42754728 -097.45893945 30.42731603 -097.45931313 30.42708359 -097.45916699 30.42739392 -097.45866561 30.42765028 -097.45843173 30.42776036 -097.45823864 30.42748602 -097.45805297 30.42740470 -097.45805568 30.42698788 -097.45829549 30.42663930 -097.45886192 30.42640610 -097.45871696 30.42666453 -097.45829684 30.42653513 -097.45806466 30.42589210 -097.45806553 30.42589112 -097.45779065 30.42663354 -097.45778993 30.42688745 -097.45751444 30.42688361 -097.45747115 30.42659074 -097.45719638 30.42588076 -097.45719563 30.42587999 -097.45692171 30.42657018 -097.45692100 30.42683897 -097.45685192 30.42660107 -097.45663776 30.42584216 -097.45616336 30.42598702 -097.45593011 30.42659404 -097.45631006 30.42682528 -097.45618037 30.42682323 -097.45582541 30.42709651 -097.45582457 30.42709934 -097.45629381 30.42735773 -097.45657452 30.42712829 -097.45704919 30.42716105 -097.45751051 30.42745454 -097.45777911 30.42748309 -097.45777854 30.42775332 -097.45749828 30.42774884 -097.45713284 30.42747206 -097.45685235 30.42738651 -097.45685001 30.42712096 -097.45694646 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV05  30.42558035 -097.45851201 30.42554227 -097.45575166 30.42574999 -097.45563543 30.42572353 -097.45509935 30.42655947 -097.45503789 30.42657508 -097.45540277 30.42661300 -097.45550044 30.42667627 -097.45555712 30.42676112 -097.45560079 30.42849399 -097.45552904 30.42875915 -097.45528007 30.42876891 -097.45585916 30.42850854 -097.45570742 30.42818036 -097.45571892 30.42803064 -097.45581574 30.42804528 -097.45637161 30.42819663 -097.45646820 30.42852813 -097.45644889 30.42872672 -097.45631971 30.42872974 -097.45710919 30.42853584 -097.45700134 30.42819706 -097.45701128 30.42805442 -097.45709442 30.42805961 -097.45758720 30.42821903 -097.45766988 30.42853651 -097.45766084 30.42873034 -097.45755987 30.42875237 -097.45828974 30.42854179 -097.45820281 30.42822319 -097.45822917 30.42806873 -097.45833210 30.42810076 -097.46024544 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV06  30.42955044 -097.46136059 30.42953961 -097.46179281 30.42937374 -097.46177087 30.42915015 -097.46170301 30.42886588 -097.46149654 30.42877862 -097.46095442 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV07  30.42868889 -097.45195887 30.42582926 -097.45203471 30.42582325 -097.44937339 30.42619878 -097.44938508 30.42622686 -097.45046497 30.42635061 -097.45058400 30.42647685 -097.45064722 30.42659196 -097.45065780 30.42669957 -097.45065805 30.42670022 -097.45005749 30.42795704 -097.45004444 30.42796336 -097.45051990 30.42809183 -097.45051735 30.42823540 -097.45047818 30.42835195 -097.45041513 30.42844938 -097.45033139 30.42854096 -097.45021788 30.42867269 -097.44994549 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV08  30.43051787 -097.44205948 30.42994506 -097.44125094 30.43054769 -097.44049834 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV09  30.42859694 -097.43746570 30.42966801 -097.43751763 30.42914746 -097.43767194 30.42902758 -097.43771167 30.42893369 -097.43778857 30.42867721 -097.43815502 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV10  30.42947104 -097.43780909 30.42948321 -097.43807034 30.42876258 -097.43854696 30.42883157 -097.43810934 30.42913584 -097.43790555 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV11  30.43051893 -097.43812441 30.43032708 -097.43779921 30.43024268 -097.43770424 30.43012660 -097.43764067 30.42979649 -097.43749298 30.43057753 -097.43741106 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV12  30.42889259 -097.46531677 30.42893738 -097.46433954 30.42909257 -097.46482983 30.42919117 -097.46495968 30.42929943 -097.46501601 30.42980120 -097.46514415 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV13  30.42991917 -097.46516711 30.43028037 -097.46500139 30.43041275 -097.46492264 30.43047981 -097.46481378 30.43060428 -097.46454106 30.43068406 -097.46518832 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV14  30.42872228 -097.44063061 30.42925904 -097.44123864 30.42871578 -097.44191126 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV15  30.43057002 -097.44694816 30.43004279 -097.44620841 30.43058361 -097.44554740 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV16  30.43004364 -097.45089708 30.43063755 -097.45007120 30.43063585 -097.45166098 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV17  30.42998234 -097.45674069 30.43068002 -097.45581856 30.43070136 -097.45762793 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV18  30.43075019 -097.46574536 30.43051362 -097.46551483 30.43048840 -097.46545438 30.43044023 -097.46541430 30.43038603 -097.46538364 30.43032378 -097.46536688 30.43027010 -097.46535618 30.43029774 -097.46523635 30.43070057 -097.46525981 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV19  30.42559922 -097.44204542 30.42556976 -097.44236146 30.42546788 -097.44233278 30.42546219 -097.44198870 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV20  30.42815415 -097.44176782 30.42773756 -097.44176460 30.42742350 -097.44146461 30.42741170 -097.43914198 30.42791484 -097.43912522 30.42794620 -097.44141723 30.42808036 -097.44156799 30.42821877 -097.44161736 \n");
            out.writeBytes("# Apron \n");
            out.writeBytes("PAVEMENT PAV21  30.43096051 -097.46691370 30.43053882 -097.46693274 30.43035482 -097.43563011 30.43079563 -097.43563175 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV22  30.42801680 -097.44875050 30.42576299 -097.44879291 30.42575163 -097.44718204 30.42410232 -097.44721642 30.42410546 -097.44222562 30.42545217 -097.44216270 30.42534431 -097.43920661 30.42790176 -097.43912623 \n");
            out.writeBytes("# Apron\n");
            out.writeBytes("PAVEMENT PAV23  30.42795908 -097.44875780 30.42790032 -097.44081034 30.42794271 -097.44080758 30.42794939 -097.44126689 30.42796194 -097.44132122 30.42797426 -097.44137460 30.42801897 -097.44138637 30.42804709 -097.44141502 30.42807894 -097.44142875 30.42813923 -097.44141483 30.42820633 -097.44139934 30.42827246 -097.44138407 30.42833955 -097.44136858 30.42839401 -097.44135601 30.42844441 -097.44133032 30.42848317 -097.44128523 30.42853203 -097.44125286 30.42855066 -097.44117828 30.42857490 -097.44110139 30.42856707 -097.44253597 30.42852301 -097.44249594 30.42848542 -097.44242630 30.42843168 -097.44237545 30.42839367 -097.44233503 30.42836071 -097.44228541 30.42831138 -097.44225362 30.42816113 -097.44224614 30.42810829 -097.44223023 30.42805173 -097.44222923 30.42799298 -097.44224982 30.42794193 -097.44230378 30.42796024 -097.44337688 30.42798049 -097.44346458 30.42800074 -097.44355228 30.42806491 -097.44364389 30.42814666 -097.44369630 30.42819717 -097.44369770 30.42827690 -097.44367929 30.42835630 -097.44365493 30.42841659 -097.44364101 30.42845785 -097.44360237 30.42849464 -097.44357982 30.42852835 -097.44354392 30.42856059 -097.44346620 30.42859580 -097.44334361 30.42860020 -097.44478239 30.42854438 -097.44469587 30.42848766 -097.44457443 30.42844904 -097.44443374 30.42842310 -097.44438350 30.42840529 -097.44433741 30.42838551 -097.44431387 30.42834475 -097.44428814 30.42808664 -097.44427041 30.42805941 -097.44427670 30.42800044 -097.44429634 30.42797222 -097.44432494 30.42798045 -097.44588232 30.42802065 -097.44590115 30.42806360 -097.44590529 30.42812389 -097.44589137 30.42817308 -097.44588704 30.42823337 -097.44587312 30.42832145 -097.44585982 30.42839382 -097.44583608 30.42845411 -097.44582216 30.42848879 -097.44578604 30.42852031 -097.44577174 30.42853521 -097.44571207 30.42855484 -097.44561517 30.42855591 -097.44555769 30.42860631 -097.44553200 30.42858700 -097.44685476 30.42856479 -097.44678960 30.42855225 -097.44673527 30.42853466 -097.44669013 30.42850015 -097.44663384 30.42849245 -097.44660048 30.42843695 -097.44654200 30.42841564 -097.44651178 30.42836652 -097.44648095 30.42828492 -097.44646465 30.42822133 -097.44646427 30.42818267 -097.44645211 30.42810666 -097.44645560 30.42803859 -097.44647132 30.42799238 -097.44654624 30.42800383 -097.44815308 30.42809851 -097.44813724 30.42816714 -097.44812842 30.42823424 -097.44811293 30.42829453 -097.44809901 30.42836844 -097.44808195 30.42843553 -097.44806646 30.42849296 -097.44804015 30.42852280 -097.44798306 30.42854662 -097.44793539 30.42856657 -097.44786653 30.42860289 -097.44777983 30.42859182 -097.44908060 30.42856785 -097.44900782 30.42853772 -097.44890835 30.42852055 -097.44883400 30.42849812 -097.44876789 30.42847161 -097.44871076 30.42843887 -097.44866209 30.42841909 -097.44863854 30.42837657 -097.44860519 30.42832373 -097.44858927 30.42822324 -097.44858436 30.42810989 -097.44858141 30.42806905 -097.44859084 30.42800876 -097.44860476 30.42801530 -097.44875285 \n");

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

    // @TODO: Possibly this should only be called for
    // SimUser/Operator/FVWI, instead of (as of now) for each and
    // every proxy.  It was really only created so that FVWI can use
    // it to get a menu of proxies with appropriate role capabilities
    // for plan creation.
    public void makeOtherThanSelfRAPBeliefs(ProxyState state, ProxyID self) {
        if (teamRAPBeliefs != null) {
            for (RAPBelief rapBelief : teamRAPBeliefs) {
                if (!rapBelief.getProxyID().equals(self)) {
                    state.addBelief(rapBelief);
                }
            }
        }
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

    public ProxyState _makeUAVBeliefs(String name, RAPBelief self) {
        ProxyState state = _makeBeliefs();
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        return state;
    }

    public void makeUAVBeliefs(String name, int uavNo) {
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVSARCaps);
        ProxyState state = _makeUAVBeliefs(name, self);
        state.addBelief(self);

        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        scanRole.params.put("Area", new Area(10000 * uavNo, 10000 * uavNo, 20000, 20000));
        // scanRole.params.put("Area", new Area(0,0,7000,5000));        
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);

        writeBeliefs(loc + SEP + name + ".blf", state);
    }

    public void makeCommonBeliefs(String name, Hashtable<String, CapabilityBelief> caps) {
	// NOTE: _makeBeliefs() removes all other beliefs.  So don't
        // add anything before this.
        ProxyState state = _makeBeliefs();
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, caps);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        state.addBelief(self);
        Machinetta.Debugger.debug(1, "        Created self for proxyid " + state.getSelf().getProxyID());;
        writeBeliefs(loc + SEP + name + ".blf", state);
    }

    public void makeCommonBeliefsWithOtherRAPBeliefs(String name, Hashtable<String, CapabilityBelief> caps) {
	// NOTE: _makeBeliefs() removes all other beliefs.  So don't
        // add anything before this.
        ProxyState state = _makeBeliefs();
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, caps);
        makeOtherThanSelfRAPBeliefs(state, self.getProxyID());
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        state.addBelief(self);
        Machinetta.Debugger.debug(1, "        Created self for proxyid " + state.getSelf().getProxyID());;
        writeBeliefs(loc + SEP + name + ".blf", state);
    }

    public void makeEOIRUAVBeliefs(String name, int uavNo) {
        makeCommonBeliefs(name, UAVEOIRCaps);
    }

    public void makeRSSIUAVBeliefs(String name, int uavNo) {
        makeCommonBeliefs(name, UAVRSSICaps);
    }

    /**
     *
     * These are the beliefs for CODE
     *
     * @param name
     * @param uavNo
     */
    public void makeAUAVBeliefs(String name, int uavNo) {
        ProxyState state = _makeBeliefs();
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, AUAVCaps);
        //	makeOtherThanSelfRAPBeliefs(state,self.getProxyID());	
        state.addBelief(self);
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        // scanRole.params.put("Area", new Area(0, 0, 7000, 5000));
        scanRole.params.put("Area", new Area(1250 * uavNo, 0, 1250 * (uavNo + 1), 5000));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);

        // Give them the plan for geolocating targets
        state.addBelief(makeGeolocateTPT());

        writeBeliefs(loc + SEP + name + ".blf", state);
    }

    public void makeUGSBeliefs(String name) {
        makeCommonBeliefs(name, UGSCaps);
    }

    public void makeHVBeliefs(String name) {
        makeCommonBeliefs(name, HVCaps);
    }

    public void makeBlueforDIBeliefs(String name) {
        makeCommonBeliefs(name, DIBLUECaps);
    }

    public void makeIMBeliefs(String name) {
        makeCommonBeliefs(name, IMCaps);
    }

    public void makeAUGVBeliefs(String name) {
        makeCommonBeliefs(name, AUGVCaps);
    }

    public void makeC130Beliefs(String name) {
        makeCommonBeliefs(name, C130Caps);
    }

    public void makeOperatorBeliefs(String name) {
        makeCommonBeliefs(name, operatorCaps);
    }

    public void makeFalconViewBeliefs(String name) {
        makeCommonBeliefsWithOtherRAPBeliefs(name, falconViewCaps);
    }

    public void makeSimUserBeliefs(String name) {
        makeCommonBeliefsWithOtherRAPBeliefs(name, simUserCaps);
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
     * This hashtable contains a specification of the capabilities of a generic
     * UAV.
     */
    java.util.Hashtable<String, CapabilityBelief> UAVSARCaps = null;

    {
        UAVSARCaps = new Hashtable<String, CapabilityBelief>();
        UAVSARCaps.put("scan", new CapabilityBelief("scan", 5));
        UAVSARCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    java.util.Hashtable<String, CapabilityBelief> UAVEOIRCaps = null;

    {
        UAVEOIRCaps = new Hashtable<String, CapabilityBelief>();
        UAVEOIRCaps.put("scan", new CapabilityBelief("scan", 5));
        UAVEOIRCaps.put("EOImage", new CapabilityBelief("EOImage", uavEOIR_EO_CAP_PERCENT));
        UAVEOIRCaps.put("IRImage", new CapabilityBelief("IRImage", uavEOIR_IR_CAP_PERCENT));
        UAVEOIRCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
        UAVEOIRCaps.put("patrol", new CapabilityBelief("patrol", 70));
        UAVEOIRCaps.put("intelSurveilRecon", new CapabilityBelief("intelSurveilRecon", 70));
    }

    java.util.Hashtable<String, CapabilityBelief> UAVRSSICaps = null;

    {
        UAVRSSICaps = new Hashtable<String, CapabilityBelief>();
        UAVRSSICaps.put("scan", new CapabilityBelief("scan", 5));
        UAVRSSICaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * AUAV.
     */
    java.util.Hashtable<String, CapabilityBelief> AUAVCaps = null;

    {
        AUAVCaps = new Hashtable<String, CapabilityBelief>();
        AUAVCaps.put("scan", new CapabilityBelief("scan", 5));
        AUAVCaps.put("provideScanData", new CapabilityBelief("provideScanData", 0));
        AUAVCaps.put("attackFromAir", new CapabilityBelief("attackFromAir", 20));
        AUAVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
        AUAVCaps.put("intelSurveilRecon", new CapabilityBelief("intelSurveilRecon", 70));
        AUAVCaps.put("patrol", new CapabilityBelief("patrol", 70));
        AUAVCaps.put("attackFromAirOrGround", new CapabilityBelief("attackFromAirOrGround", 20));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * Humvee
     */
    java.util.Hashtable<String, CapabilityBelief> HVCaps = null;

    {
        HVCaps = new Hashtable<String, CapabilityBelief>();
        HVCaps.put("move", new CapabilityBelief("move", 85));
        HVCaps.put("hold", new CapabilityBelief("hold", 100));

        HVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * BlueforDI
     */
    java.util.Hashtable<String, CapabilityBelief> DIBLUECaps = null;

    {
        DIBLUECaps = new Hashtable<String, CapabilityBelief>();

        //@TODO: need to fix the number for each CapabilityBelief
        //       reporting sensed data?, how about defend? (does DI actually defend both air and ground?
        //       anything else?
        DIBLUECaps.put("move", new CapabilityBelief("move", 85));
        DIBLUECaps.put("hold", new CapabilityBelief("hold", 100));
        DIBLUECaps.put("patrol", new CapabilityBelief("patrol", 80));
        DIBLUECaps.put("attack", new CapabilityBelief("attack", 100));
        DIBLUECaps.put("mount", new CapabilityBelief("mount", 100));
        DIBLUECaps.put("dismount", new CapabilityBelief("dismount", 100));
        DIBLUECaps.put("defendAir", new CapabilityBelief("defendAir", 90));
        DIBLUECaps.put("defendGround", new CapabilityBelief("defendGround", 90));
        DIBLUECaps.put("flee", new CapabilityBelief("flee", 100));
        DIBLUECaps.put("retreat", new CapabilityBelief("retreat", 100));
        DIBLUECaps.put("randomMove", new CapabilityBelief("randomMove", 85));

        DIBLUECaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * Intelligent Mine
     */
    java.util.Hashtable<String, CapabilityBelief> IMCaps = null;

    {
        //@TODO: need to fix the number for each CapabilityBelief
        //       reporting sensed data?, anything else?, spreadOut
        IMCaps = new Hashtable<String, CapabilityBelief>();
        IMCaps.put("move", new CapabilityBelief("move", 85));
        IMCaps.put("hold", new CapabilityBelief("hold", 100));
        IMCaps.put("detonate", new CapabilityBelief("detonate", 100));
        IMCaps.put("activate", new CapabilityBelief("activate", 100));
        IMCaps.put("deactivate", new CapabilityBelief("deactivate", 100));
        IMCaps.put("mount", new CapabilityBelief("mount", 100));
        IMCaps.put("dismount", new CapabilityBelief("dismount", 100));

        IMCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * AUGV
     */
    java.util.Hashtable<String, CapabilityBelief> AUGVCaps = null;

    {
        //@TODO: need to fix the number for each CapabilityBelief
        //       reporting sensed data?, how about defend? (does AUGV actually defend both air and ground?
        //       anything else?
        AUGVCaps = new Hashtable<String, CapabilityBelief>();
        AUGVCaps.put("move", new CapabilityBelief("move", 85));
        AUGVCaps.put("hold", new CapabilityBelief("hold", 100));
        AUGVCaps.put("patrol", new CapabilityBelief("patrol", 10));
        AUGVCaps.put("attack", new CapabilityBelief("attack", 10));
        AUGVCaps.put("UGVAttack", new CapabilityBelief("UGVAttack", 90));
        AUGVCaps.put("mount", new CapabilityBelief("mount", 100));
        AUGVCaps.put("dismount", new CapabilityBelief("dismount", 100));
        AUGVCaps.put("defendAir", new CapabilityBelief("defendAir", 90));
        AUGVCaps.put("defendGround", new CapabilityBelief("defendGround", 90));
        AUGVCaps.put("randomMove", new CapabilityBelief("randomMove", 85));

        AUGVCaps.put("attackFromAirOrGround", new CapabilityBelief("attackFromAirOrGround", 20));
        AUGVCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * C130
     */
    java.util.Hashtable<String, CapabilityBelief> C130Caps = null;

    {
        //@TODO: need to fix the number for each CapabilityBelief
        //       anything else?
        C130Caps = new Hashtable<String, CapabilityBelief>();
        C130Caps.put("move", new CapabilityBelief("move", 85));
        C130Caps.put("hold", new CapabilityBelief("hold", 100));
        C130Caps.put("deploy", new CapabilityBelief("deploy", 100));
        C130Caps.put("transport", new CapabilityBelief("transport", 100));
        C130Caps.put("land", new CapabilityBelief("land", 100));
        C130Caps.put("launch", new CapabilityBelief("launch", 100));
        C130Caps.put("airdrop", new CapabilityBelief("airdrop", 100));

        C130Caps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * Operator agent.
     */
    java.util.Hashtable<String, CapabilityBelief> operatorCaps = null;

    {
        operatorCaps = new Hashtable<String, CapabilityBelief>();
        operatorCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * FalconView interface agent.
     */
    java.util.Hashtable<String, CapabilityBelief> falconViewCaps = null;

    {
        falconViewCaps = new Hashtable<String, CapabilityBelief>();
        falconViewCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a generic
     * SimUser agent.
     */
    java.util.Hashtable<String, CapabilityBelief> simUserCaps = null;

    {
        simUserCaps = new Hashtable<String, CapabilityBelief>();
        simUserCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    /**
     * This hashtable contains a specification of the capabilities of a
     * Unattended Ground Sensor. Since it just produces information it has no
     * capabilities.
     */
    java.util.Hashtable<String, CapabilityBelief> UGSCaps = null;

    {
        UGSCaps = new Hashtable<String, CapabilityBelief>();
        UGSCaps.put("RoleAllocation", new CapabilityBelief("RoleAllocation", 0));
    }

    private void writeEmitterLocations(DataOutputStream out) throws java.io.IOException {
        out.writeBytes("EMITTER_COUNT " + emitterCount + "\n");
        for (int loopi = 0; loopi < emitterCount; loopi++) {
            if (randomEmitterLocations) {
                out.writeBytes("EMITTER_" + loopi + " RANDOM\n");
            } else {
                out.writeBytes("EMITTER_" + loopi + " " + emitterLocations[loopi] + "\n");
            }
        }
        out.writeBytes("# EMITTER_1 35144 25275\n");
        out.writeBytes("# EMITTER_2 10344 10275\n");
        out.writeBytes("# EMITTER_3 13344 34275\n");
        // out.writeBytes("EMITTER_5 5144 5275\n");
        // out.writeBytes("EMITTER_6 15344 15275\n");
        // out.writeBytes("EMITTER_7 20344 20275\n");
        // out.writeBytes("EMITTER_8 30344 30275\n");
        // out.writeBytes("EMITTER_9 35344 35275\n");
        // out.writeBytes("EMITTER_10 40344 40275\n");
        // out.writeBytes("EMITTER_11 45344 45275\n");
    }

    private void writeSA9Locations(DataOutputStream out) throws java.io.IOException {
        int channel = 1;

        for (int loopi = 0; loopi < emitterCount; loopi++) {
            if (!emitterChannels) {
                if (randomEmitterLocations) {
                    out.writeBytes("SA9 SA9-" + (loopi + 1) + " RANDOM OPFOR\n");
                } else {
                    out.writeBytes("SA9 SA9-" + (loopi + 1) + " " + emitterLocations[loopi] + " OPFOR\n");
                }
            } else {
                if (randomEmitterLocations) {
                    out.writeBytes("SA9 SA9-" + (loopi + 1) + " RANDOM CHANNEL" + channel + " OPFOR\n");
                } else {
                    out.writeBytes("SA9 SA9-" + (loopi + 1) + " " + emitterLocations[loopi] + " CHANNEL" + channel + " OPFOR\n");
                }
                channel++;
            }
        }
        // out.writeBytes("# SA9 SA9-1 35144 25275 OPFOR\n");
        // out.writeBytes("# SA9 SA9-2 10344 10275 OPFOR\n");
        // out.writeBytes("# SA9 SA9-4 13344 34275 OPFOR\n");
        // out.writeBytes("SA9 SA9-5 5144 5275 OPFOR\n");
        // out.writeBytes("SA9 SA9-6 15344 15275 OPFOR\n");
        // out.writeBytes("SA9 SA9-7 20344 20275 OPFOR\n");
        // out.writeBytes("SA9 SA9-8 30344 30275 OPFOR\n");
        // out.writeBytes("SA9 SA9-9 35344 35275 OPFOR\n");
        // out.writeBytes("SA9 SA9-10 40344 40275 OPFOR\n");
        // out.writeBytes("SA9 SA9-11 45344 45275 OPFOR\n");
    }

    /**
     * For creating config files for UAVs
     */
    private DataOutputStream _createUAVCfgFile(String name) {
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DYNAMIC_TEAMING true\n");
            if (CONFIG_FOR_WIN_DAVE) {
                out.writeBytes("DEFAULT_BELIEFS_FILE /home/dscerri/Code/" + name + ".blf\n");
            } else {
                out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            }

            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + TERRAIN_COSTMAP_LOCATION + "\n");
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

	    // NOTE: The reason we write the emitter locations to our
            // UAV configs is so that we can mark on the GUI where the
            // emitters are and easily compare that (visually) with
            // where our detection code thinks they are.
            if (ADD_EMITTER_LOCS_TO_UAV_CFG) {
                writeEmitterLocations(out);
            }

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
            out.writeBytes("AA_MAX_DIST_CAP_METERS 5000\n");
            out.writeBytes("\n");
            out.writeBytes("# The size of the 'cost' square placed around SA9's that are\n");
            out.writeBytes("# detected, in order to avoid being shot down.\n");
            out.writeBytes("SA9_COST_SQUARE_METERS " + SA9Cost + "\n");
            out.writeBytes("\n");
            out.writeBytes("# if this is true, then pathdb isn't going to\n");
            out.writeBytes("# have much work to do... so leave it false when testing pathdb.\n");
            Machinetta.Debugger.debug(1, "OTHER_VEHICLE_COSTMAP_ON " + OtherVehicleCostmapOn);
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
            if ((noOperators > 0) || (noFalconView > 0) || (noSimUsers > 0)) {
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON true\n");
            } else {
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON false\n");
            }
            if (noFalconView > 0) {
                out.writeBytes("FALCONVIEW_DIRECTED_INFO_REQ_ON true\n");
            } else {
                out.writeBytes("FALCONVIEW_DIRECTED_INFO_REQ_ON false\n");
            }
            if (noSimUsers > 0) {
                out.writeBytes("SIM_SIMUSER_DIRECTED_INFO_REQ_ON true\n");
            } else {
                out.writeBytes("SIM_SIMUSER_DIRECTED_INFO_REQ_ON false\n");
            }
            out.writeBytes("SIM_TRAFFIC_CONTROLLER_DIRECTED_INFO_REQ_ON false\n");
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
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.HVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.HVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public void createBlueforDICfgFile(String name) {
        Machinetta.Debugger.debug("createBlueforDICfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.BasicAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.DIRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public void createIMCfgFile(String name) {
        Machinetta.Debugger.debug("createIMCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.BasicAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.IMRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public void createAUGVCfgFile(String name) {
        Machinetta.Debugger.debug("createAUGVCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.BasicAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.AUGVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

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
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
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
//             if(noOperators > 0)
//                 out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON true\n");
//             else
//                 out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON false\n");
//             if(noFalconView > 0)
//                 out.writeBytes("FALCONVIEW_DIRECTED_INFO_REQ_ON true\n");
//             else
//                 out.writeBytes("FALCONVIEW_DIRECTED_INFO_REQ_ON false\n");
//             if(noSimUsers > 0)
//                 out.writeBytes("SIM_SIMUSER_DIRECTED_INFO_REQ_ON true\n");
//             else
//                 out.writeBytes("SIM_SIMUSER_DIRECTED_INFO_REQ_ON false\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    public void createC130CfgFile(String name) {
        Machinetta.Debugger.debug("createC130CfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");

            out.writeBytes("AA_IMPLEMENTATION_TYPE  AirSim.Machinetta.BasicAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.C130RI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            out.writeBytes("TERRAIN_COSTMAP_LOCATION " + locs + "TerrainCM\n");

            out.writeBytes("DYNAMIC_TEAMING true\n");

            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");

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
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE + "\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UGSAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UGSRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS " + mapWidthMeters + "\n");
            out.writeBytes("MAP_HEIGHT_METERS " + mapHeightMeters + "\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT " + mapLowerLeftLat + "\n");
            out.writeBytes("MAP_LOWER_LEFT_LON " + mapLowerLeftLon + "\n");
            out.writeBytes("\n");
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
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM " + operOneKmGridLinesOn + "\n");
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
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            //            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.writeBytes("CTDB_BASE_NAME " + getCtdbBaseName() + "\n");

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

            if (ADD_EMITTER_LOCS_TO_UAV_CFG) {
                writeEmitterLocations(out);
            }

            out.writeBytes("LOCAL_BBGF_DISPLAY true");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for Operators
     */
    public void createFalconViewCfgFile(String name) {
        Machinetta.Debugger.debug("createFalconViewCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));

            out.writeBytes("DYNAMIC_TEAMING true\n");

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
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE com.lmco.atl.accast.interop.FalconViewWrapperInterface\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");

	    // @TODO: Does falconview interface need anything specific
            // to it?  add here
            out.writeBytes("ACCAST_SERVER_PORT 8888\n");

            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * For creating config files for SimUsers
     */
    public void createSimUserCfgFile(String name) {
        Machinetta.Debugger.debug("createSimUserCfgFile " + name, 1, this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(loc + SEP + name + ".cfg"));

            out.writeBytes("DAMAGE_MODE CUMULATIVE\n");

            out.writeBytes("GUI_ON " + GUI_ON + "\n");
            out.writeBytes("PLAN_CREATION_DROP_PROBABILITY " + PLAN_CREATION_DROP_PROBABILITY + "\n");
            out.writeBytes("PLAN_CREATION_REACTION_TIME_MS " + PLAN_CREATION_REACTION_TIME_MS + "\n");
            out.writeBytes("PLAN_CREATION_AUAV_ATTACK_DI " + PLAN_CREATION_AUAV_ATTACK_DI + "\n");
            out.writeBytes("PLAN_CREATION_AUGV_ATTACK_DI " + PLAN_CREATION_AUGV_ATTACK_DI + "\n");
            out.writeBytes("PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI " + PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI + "\n");

            out.writeBytes("GUI_ON " + GUI_ON + "\n");
            out.writeBytes("GUI_ON " + GUI_ON + "\n");
            out.writeBytes("GUI_ON " + GUI_ON + "\n");

	    // @TODO: I highly suspect most of these config params
            // aren't being used by SimUser and should be deleted
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM " + operOneKmGridLinesOn + "\n");
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
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.SimUserRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL " + DEBUG_LEVEL + "\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            //            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.writeBytes("CTDB_BASE_NAME " + getCtdbBaseName() + "\n");

            out.writeBytes("\n");
            out.writeBytes("NUM_AUAVS " + noArmoredUAVs + "\n");
            out.writeBytes("NUM_EOIRUAVS " + noEOIRUAVs + "\n");
            out.writeBytes("NUM_AUGVS " + noAUGVs + "\n");
            out.writeBytes("\n");

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
            out.writeBytes("SMALL_ACCAST_SCENARIO " + SMALL_ACCAST_SCENARIO + "\n");
            out.writeBytes("\n");

            if (ADD_EMITTER_LOCS_TO_UAV_CFG) {
                writeEmitterLocations(out);
            }

            out.writeBytes("LOCAL_BBGF_DISPLAY true");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }

    /**
     * Defines the plan template for Geolocate missions.
     *
     * Modified from L3Create
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
        // ISR role
        BasicRole basic = new BasicRole(TaskType.intelSurveilRecon);
        basic.params = new Hashtable();
        basic.params.put("Label", "FixRole");
        roles.add(basic);

        //----------------------------------------------------------
        // No constraints for now        
        
        // Finally, create the template
        TeamPlanTemplate tpt = new TeamPlanTemplate("Geolocate", team.getID(), params, preconditions, postconditions, roles, constraints);

        return tpt;
    }
}
