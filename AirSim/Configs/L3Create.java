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
 * L3Create.java
 *
 * Created on February 7, 2006, 5:24 PM
 *
 * SEE INLINE COMMENTS regarding various final static globals.
 *
 */

package AirSim.Configs;

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
public class L3Create {
    public static final String SEP = File.separator;
    //
    // START CONFIGURING HERE
    //
    
    //
    // SET _ONE_ and _ONLY_ONE_ OF THE configureFor VARIABLES to true.
    //
    
    // If this is true, will change all settings for 50km x 50km map for TDOA (Time Difference Of Arrival)
    public static boolean configureFor50by50TDOADemo = true;
    
    // If this is true, will change all settings for 50km x 50km map for multi frequency RSSI
    public static boolean configureFor50by50MultiFreqDemo = false;
    
    // If this is true, will change all settings for 50km x 50km map for profiling
    public static boolean configureFor50by50profiling = false;
    
    // If this is true, will change all settings for 1km x 1km map, else 50km x 5km
    public static boolean configureFor1by1 = false;
    
    // If this is true, will change all settings for 5km x 5km map, else 50km x 5km
    public static boolean configureFor5by5 = false;

    // experiments for infotech paper
    public static boolean configureFor50by50InfoTech = false;
    
    public static boolean configureForTestingSean = false;
    
    // Demo to accast Mon Apr 16 23:04:40 EDT 2007
    public static boolean configureFor50by50AccastDemo = false;
    
    // Multifreq config for development work
    public static boolean configureForMultiFreq = false;
    
    public static boolean configureForDeconflictionExperiments = false;
    
    // If this is true, will change all settings for 1km x 1km map, else 50km x 5km
    public static boolean configureFor1by1Sean = false;
    public static boolean configureFor1by1Gascola = false;
    public static boolean configureFor50by50and12UAVsSean = false;
    public static boolean configureFor1by1MultiEmitterSean = false;
    
    // If this is true, will change all settings to 50km x 5km, deconfliction
    public static boolean configureForDeconfliction = false;
    
    public static String CTDB_BASE_DIR = "/usr1/grd/sanjaya_maps_1_0";
    public static String CTDB_DEFAULT = "sanjaya001_0_0_50000_50000_050";
    public static String CTDB_AUSTIN5 = "austin_0_0_5000_5000_005";
    public static String CTDB_AUSTIN50 = "austin_0_0_50000_50000_050";
    public static String CTDB_GASCOLA = "gascola_0_0_5000_5000_005";
    public static String CTDB_FILE = CTDB_DEFAULT;
    public static String getCtdbBaseName() { return CTDB_BASE_DIR + SEP + CTDB_FILE; } 
   
    public final static String MASTER_DISPLAY_MASTER = ":0.0";
    
    public static HashMap<String, UserConfig> perUserMap = new HashMap<String, UserConfig>();
    public static void createUserConfigs() {

	UserConfig uc;

	uc = new UserConfig();
	uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
	uc.CTDB_FILE = CTDB_FILE;
	uc.TERRAIN_COSTMAP_LOCATION = "/afs/cs.cmu.edu/user/owens/TerrainCM";
	String BASEDIR_OWENS = "/afs/cs.cmu.edu/user/owens/camra/src/";
	uc.MASTER_CLASSPATH = BASEDIR_OWENS+":"+BASEDIR_OWENS+"Util-src:"+BASEDIR_OWENS+"Gui-src:"+BASEDIR_OWENS+"Machinetta-src:"+BASEDIR_OWENS+"AirSim-src:"+BASEDIR_OWENS+"jar/swing-layout-1.0.jar";
	uc.SLAVE_CLASSPATH = BASEDIR_OWENS+"Util-src:"+BASEDIR_OWENS+"Gui-src:"+BASEDIR_OWENS+"Machinetta-src:"+BASEDIR_OWENS+"AirSim-src:"+BASEDIR_OWENS+"jar/swing-layout-1.0.jar";
	uc.MASTER_MACHINE = "mavrodafni";
	String[] OWENS_SLAVE_MACHINES = {"tsaousi", "zalovitiko", "volitsa", "athiri"};
	uc.SLAVE_MACHINES = OWENS_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
	uc.SLAVE_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
	//	uc.MASTER_DISPLAY = "iota.wv.cs.cmu.edu:0";
	//	uc.SLAVE_DISPLAY = "iota.wv.cs.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = "";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
	uc.JAVA = "java -server";
	perUserMap.put("owens", uc);
	
	uc = new UserConfig();
	uc.CTDB_BASE_DIR = "/projects/sanjaya/sanjaya_maps_1_0";
	uc.CTDB_FILE = CTDB_AUSTIN5;
	uc.TERRAIN_COSTMAP_LOCATION = "/afs/cs.cmu.edu/user/owens/TerrainCM";
	String BASEDIR_IWARFIEL = "/projects/sanjaya/";
	uc.MASTER_CLASSPATH = BASEDIR_IWARFIEL+":"+BASEDIR_IWARFIEL+"Util-src:"+BASEDIR_IWARFIEL+"Gui-src:"+BASEDIR_IWARFIEL+"Machinetta-src:"+BASEDIR_IWARFIEL+"AirSim-src:"+BASEDIR_IWARFIEL+"swing-layout-1.0.jar";
	uc.SLAVE_CLASSPATH = BASEDIR_IWARFIEL+"Util-src:"+BASEDIR_IWARFIEL+"Gui-src:"+BASEDIR_IWARFIEL+"Machinetta-src:"+BASEDIR_IWARFIEL+"AirSim-src:"+BASEDIR_IWARFIEL+"lib/swing-layout-1.0.jar:"+BASEDIR_IWARFIEL+"lib/log4j-1.2.15.jar";
	uc.MASTER_MACHINE = "abingdon";
	String[] IWARFIEL_SLAVE_MACHINES = {"groucho", "harpo", "chico", "zeppo", "gummo"};
	uc.SLAVE_MACHINES = IWARFIEL_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
	uc.SLAVE_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = "";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/projects/sanjaya/logs";
	uc.JAVA = "java -server";
	perUserMap.put("iwarfiel", uc);

	uc = new UserConfig();
	uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
	uc.CTDB_FILE = CTDB_FILE;
	uc.TERRAIN_COSTMAP_LOCATION = "/usr1/grd/TerrainCM";
	String BASEDIR_SJO = "/home/sjo/src/";
	uc.MASTER_CLASSPATH = BASEDIR_SJO+":"+BASEDIR_SJO+"Util-src:"+BASEDIR_SJO+"Gui-src:"+BASEDIR_SJO+"Machinetta-src:"+BASEDIR_SJO+"AirSim-src:"+BASEDIR_SJO+"jar/log4j-1.2.15.jar:"+BASEDIR_SJO+"jar/swing-layout-1.0.jar";
	uc.SLAVE_CLASSPATH = BASEDIR_SJO+"Util-src:"+BASEDIR_SJO+"Gui-src:"+BASEDIR_SJO+"Machinetta-src:"+BASEDIR_SJO+"AirSim-src:"+BASEDIR_SJO+"jar/log4j-1.2.15.jar:"+BASEDIR_SJO+"jar/swing-layout-1.0.jar";
	uc.MASTER_MACHINE = "mavrodafni";
	String[] SJO_SLAVE_MACHINES = {"asyrtiko", "dafni", "lagorthi", "tsaousi", "zalovitiko", "volitsa", "tsipouro","xynomavro","raki","debina","robola"};
	uc.SLAVE_MACHINES = SJO_SLAVE_MACHINES;
 	uc.MASTER_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
 	uc.SLAVE_DISPLAY = "zorba.cimds.ri.cmu.edu:0";
	// uc.MASTER_DISPLAY = "iota.wv.cs.cmu.edu:0";
	// uc.SLAVE_DISPLAY = "iota.wv.cs.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = BASEDIR_SJO+"rsync.sh";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
	uc.JAVA = "java -server";
	perUserMap.put("sjo", uc);
	
	uc = new UserConfig();
	uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
	uc.CTDB_FILE = CTDB_FILE;
	uc.TERRAIN_COSTMAP_LOCATION = "/usr1/L3-MACHINETTA/TerrainCM";
	String BASEDIR_RGLINTON = "/usr1/L3-MACHINETTA/";
	uc.MASTER_CLASSPATH = BASEDIR_RGLINTON+":"+BASEDIR_RGLINTON+"Util-src:"+BASEDIR_RGLINTON+"Gui-src:"+BASEDIR_RGLINTON+"Machinetta-src:"+BASEDIR_RGLINTON+"AirSim-src:"+BASEDIR_RGLINTON+"swing-layout-1.0.jar";
	uc.SLAVE_CLASSPATH = BASEDIR_RGLINTON+"Util-src:"+BASEDIR_RGLINTON+"Gui-src:"+BASEDIR_RGLINTON+"Machinetta-src:"+BASEDIR_RGLINTON+"AirSim-src:"+BASEDIR_RGLINTON+"swing-layout-1.0.jar";
	uc.MASTER_MACHINE = "batiki";
	String[] RGLINTON_SLAVE_MACHINES = {"batiki"};
	uc.SLAVE_MACHINES = RGLINTON_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = ":0";
	uc.SLAVE_DISPLAY = ":0";
	uc.SYNCHRONIZATION_SCRIPT = "";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
	uc.JAVA = "java -server";
	perUserMap.put("rglinton", uc);
	
	uc = new UserConfig();
	uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
	uc.CTDB_FILE = CTDB_FILE;
	uc.TERRAIN_COSTMAP_LOCATION = "/home/dscerri/Code/TerrainCM";
	uc.MASTER_CLASSPATH = "/usr0/pscerri/Code/swing-layout-1.0.jar:/usr0/pscerri/Code/AirSim/build/classes:/usr0/pscerri/Code/Util/build/classes/:/usr0/pscerri/Code/Machinetta/build/classes:/usr0/pscerri/Code/SanjayaGUI/build/classes";
	uc.SLAVE_CLASSPATH = "/home/pscerri/Code/swing-layout-1.0.jar:/home/pscerri/Code/classes";
	uc.MASTER_MACHINE = "tsipouro";
	String[] PSCERRI_SLAVE_MACHINES = {"zeta", "iota", "kappa"};
	uc.SLAVE_MACHINES = PSCERRI_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = "tsipouro.cimds.ri.cmu.edu:0";
	uc.SLAVE_DISPLAY = "tsipouro.cimds.ri.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = "/usr0/pscerri/Code/scripts/syncall.sh";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
	uc.JAVA = "java -server";
	perUserMap.put("pscerri", uc);
	
	uc = new UserConfig();
	uc.CTDB_BASE_DIR = CTDB_BASE_DIR;
	uc.CTDB_FILE = CTDB_FILE;
	uc.TERRAIN_COSTMAP_LOCATION = "/home/junyounk/srcs/TerrainCM";
	uc.MASTER_CLASSPATH = "/home/junyounk/netbeans-5.5.1/platform6/modules/ext/swing-layout-1.0.jar:/home/junyounk/srcs/build/classes/";
	uc.SLAVE_CLASSPATH = "/home/junyounk/netbeans-5.5.1/platform6/modules/ext/swing-layout-1.0.jar:/home/junyounk/srcs/build/classes";
	uc.MASTER_MACHINE = "zalovitiko";
	String[] JUNYOUNK_SLAVE_MACHINES = {"robola", "raki", "debina"};
	uc.SLAVE_MACHINES = JUNYOUNK_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = ":0";
	uc.SLAVE_DISPLAY = "zalovitiko.cimds.ri.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = "/home/junyounk/test/syncall.sh";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "/usr1/logs";
	uc.JAVA = "java -server";
	perUserMap.put("junyounk", uc);
	
	uc = new UserConfig();
	uc.CTDB_BASE_DIR = "sanjaya_maps_1_0";
	uc.CTDB_FILE = "sanjaya001_0_0_50000_50000_050";
	uc.TERRAIN_COSTMAP_LOCATION = "/home/dscerri/Code/TerrainCM";
	uc.MASTER_CLASSPATH = "/home/dscerri/Code/swing-layout-1.0.jar:/home/dscerri/Code/AirSim/build/classes:/home/dscerri/Code/Util/build/classes/:/home/dscerri/Code/Machinetta/build/classes:/home/dscerri/Code/SanjayaGUI/build/classes";
	uc.SLAVE_CLASSPATH = "/home/dscerri/Code/swing-layout-1.0.jar:/home/dscerri/Code/classes";
	uc.MASTER_MACHINE = "tau";
	String[] DSCERRI_SLAVE_MACHINES = {"xynomavro", "zalovitiko", "volitsa", "athiri"};
	uc.SLAVE_MACHINES = DSCERRI_SLAVE_MACHINES;
	uc.MASTER_DISPLAY = "tau.cimds.ri.cmu.edu:0";
	uc.SLAVE_DISPLAY = "tau.cimds.ri.cmu.edu:0";
	uc.SYNCHRONIZATION_SCRIPT = "";
	uc.OUTPUT_TO_CUR_DIR = false;
	uc.OUTPUT_DIR_PREFIX = "";
	uc.JAVA = "java -server";
	perUserMap.put("dscerri", uc);
	
	//	uc.MASTER_DISPLAY_DEMO = "raki.cimds.ri.cmu.edu:0";
	//	uc.SLAVE_DISPLAY_DEMO = "raki.cimds.ri.cmu.edu:0";

    }
    
    public static void setUserConfig(String username) {
        UserConfig uc = perUserMap.get(username);
        if(null == uc) {
            System.err.println("Unknown USER '"+username+"', unable to configure.  Add user configurations for this user to createUserConfigs");
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
    
    public final static String BASEDIR_SEAN="/afs/cs.cmu.edu/user/owens/camra/src/";
    public final static String MASTER_CLASSPATH_SEAN=BASEDIR_SEAN+":"+BASEDIR_SEAN+"Util-src:"+BASEDIR_SEAN+"Gui-src:"+BASEDIR_SEAN+"Machinetta-src:"+BASEDIR_SEAN+"AirSim-src:"+BASEDIR_SEAN+"swing-layout-1.0.jar";
    public final static String MASTER_CLASSPATH_PAUL="/usr0/pscerri/Code/swing-layout-1.0.jar:/usr0/pscerri/Code/AirSim/build/classes:/usr0/pscerri/Code/Util/build/classes/:/usr0/pscerri/Code/Machinetta/build/classes:/usr0/pscerri/Code/SanjayaGUI/build/classes";
    public final static String MASTER_CLASSPATH_DAVE="/home/dscerri/Code/swing-layout-1.0.jar:/home/dscerri/Code/AirSim/build/classes:/home/dscerri/Code/Util/build/classes/:/home/dscerri/Code/Machinetta/build/classes:/home/dscerri/Code/SanjayaGUI/build/classes";
    
    public static String MASTER_CLASSPATH;
    
    // SLAVE_CLASSPATH is used to specify the classpath when running
    // on one of the slave machines, i.e. the various proxies.
    public static String SLAVE_CLASSPATH;
    
    // The master system, on which we run the Sanjaya wargame, the
    // SimOperator, and the UDP Switch.
    public static String MASTER_MACHINE;
    
    // The slave systems on which we run the various proxies.
    // What other cpus can we use?   tsipouro, zorba?  Theta (paul), tau (dave)?
    public final static String [] SLAVE_MACHINES_MANY = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "iota","zeta", "kappa","tau", "athiri", "robola", "hydra", "batiki", "debina","raki"};
    public final static String [] SLAVE_MACHINES_MANY2 = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "iota","zeta", "athiri", "robola", "hydra", "batiki", "debina"};
    public final static String [] SLAVE_MACHINES_MANY3 = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "iota", "kappa","athiri", "robola", "hydra", "batiki", "debina"};
    public final static String [] SLAVE_MACHINES_MANY4 = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "iota","athiri", "robola", "hydra", "batiki", "debina"};
    public final static String [] SLAVE_MACHINES_MANY5 = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "athiri", "robola", "hydra", "debina","raki"};
    public final static String [] SLAVE_MACHINES_MANY6 = {"xynomavro", "zalovitiko", "tsaousi", "volitsa", "robola", "hydra", "debina","raki"};
    public final static String [] SLAVE_MACHINES_MANY7 = {"volitsa", "athiri", "robola", "hydra", "debina","raki"};
    
    public final static String [] SLAVE_MACHINES_SEAN = {"xynomavro", "zalovitiko", "volitsa", "athiri"};
    public final static String [] SLAVE_MACHINES_FEW1 = {"robola","raki"};
    public final static String [] SLAVE_MACHINES_PAUL = {"zeta", "iota", "kappa"};
    public final static String [] SLAVE_MACHINES_DAVE = {"zalovitiko", "volitsa", "athiri"};
    
    public static String [] SLAVE_MACHINES;
    
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
    public static String OUTPUT_DIR="output";
    public static String OUTPUT_DIRS="output"+SEP;
    public static boolean OUTPUT_TO_CUR_DIR = false;
    public static String OUTPUT_DIR_PREFIX;
    public static String OUTPUT_DIR_PREFIXS;
    
    // Where is the java binary? what flags do we want?
    public final static String JAVA_SEAN="java -server";
    public final static String JAVA_PAUL="java -server";
    public final static String JAVA_DAVE="java -server";
    
    public static String JAVA;
    
    //Only for Dave, for running master on Windows. Should be false for everyone else
    public final static boolean CONFIG_FOR_WIN_DAVE=false;
    
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
    
    public final static String SANJAYA_UPDATE_RATE_COMMENT=
            "# The update rate is the number of ms that sanjay sleeps between each\n"
            +"# 'step' of the simulator.  By default the simulator 'steps' ten times\n"
            +"# per second, so an update rate of 50 means the simulator simulates\n"
            +"# second of time every .5 seconds or so.\n";
    public static int sanjayaUpdateRate = 100;
    
    public final static String SANJAYA_ASSET_COLLISION_COMMENT=
            "# Sanjaya has code that checks for collision between SmallUAVs,\n"
            +"# where 'collision' is defined as 'coming within some range n of\n"
            +"# each other'.  These flags control whether or not this code runs\n"
            +"# and what it uses for range.  When we use smaller maps (1km x\n"
            +"# 1km) and slower UAVs, collision range should accordingly be\n"
            +"# reduced.\n";
    public static boolean sanjayaAssetCollisionDetectionOn = true;
    public static double sanjayaAssetCollisionRangeMeters = 2000;
    public static double sanjayaAssetCollisionMinZDistMeters = 100;
    
    public final static String COSTMAPS_COMMENT=
            "# In general, only one of the following costmaps should be on.\n"
            +"# The RSSI UAVs use a Binary Bayes Filter to locate targets.\n"
            +"# They generate an entropy map from the filter to use as a\n"
            +"# costmap in planning their paths to cover regions where\n"
            +"# uncertainty still exists.  If the Binary Bayes Filter is not\n"
            +"# set on, one of the other random gaussian costmaps below is used\n"
            +"# instead.\n";
    
    public final static String USE_XPLANE_CAMERA_COMMENT =
            "# Turn on sending of uav locations to xplane via UDP.\n";
    public static boolean USE_XPLANE_CAMERA = false;
    public static String USE_XPLANE_CAMERA_HOST = "zeta.cimds.ri.cmu.edu";
    
    public final static String END_CONDITION_COMMENT=
            "# These end conditions are checked every step in the simulator,\n"
            +"# and if any them are true, the simulator exits.  This is to\n"
            +"# facilitate batch experiments.  CAPTURE_THE_FLAG checks for \n"
            +"# OPFOR ground vehicles inside a BLUEFOR 'flag zone' in the lower\n"
            +"# left 5km of the map, and for BLUEFOR ground vehicles inside\n"
            +"# the OPFOR 'flag zone' in the upper right 5km of the map.\n"
            +"# ALL_EMITTERS_DETECTED checks that all assets that implement\n"
            +"# the Emitter interface have been 'detected', i.e. by a SmallAUV\n"
            +"# receiving a CAMERA_COMMAND while within 1km of the emitter.\n";
    public static boolean END_CONDITION_CAPTURE_THE_FLAG = false;
    public static boolean END_CONDITION_ALL_HMMMVS_DEAD = false;
    public static boolean END_CONDITION_ALL_OPFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_BLUEFOR_DEAD = false;
    public static boolean END_CONDITION_ALL_EMITTERS_DETECTED = false;
    
    public static boolean uavBinaryBayesFilterOn= true;
    public static boolean uavRandomEntireMapCostmapOn = false;
    public static boolean uavRandomSmallMovesCostmapOn = false;
    public static double uavRandomSmallMovesRange = 5000;
    public static boolean uavRandomClustersCostmapOn = false;
    public static double uavRandomClustersRange = 3000;
    public static int uavRandomClustersCount = 3;
    public final static String COSTMAPS_RANDOM_GUASSIANS_COMMENT=
            "# The random gaussian params are used by all of the random costmaps.\n"
            +"# The amplitude multiplier is multiplied by a random number\n"
            +"# from 0 to 1.0.  The divisor multiplier is multiplied by a\n"
            +"# random integer from 5 to 14.\n";
    public static double uavRandomGuassiaAmplitudeMultiplier = 500;
    public static double uavRandomGuassianDivisorMultiplier = 100;
    
    public static boolean operBinaryBayesFilterOn = false;
    public static boolean operBinaryBayesFilterPanelOn = false;
    public static boolean operOneKmGridLinesOn = false;
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
    public static int noOpforInfantry = 10;
    public static int noBlueforInfantry = 10;
    public static int noUAVs = 0;
    public static int noCivilians = 0;
    public static int noRSSIUAVs = 4;
    public static int noEOIRUAVs = 0;
    public static int noArmoredUAVs = 0;
    public static double uavRefuelThreshold = 2000;
    public static int noDetectors = 0;
    public static int noGeolocators = 0;
    public static int noOperators = 1;
    public static int noTrafficControllers = 1;
    public static int noUGSs = 0;
    public static int noHumvees = 0;
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
        public String [] SLAVE_MACHINES;
        public String MASTER_DISPLAY;
        public String SLAVE_DISPLAY;
        public String SYNCHRONIZATION_SCRIPT;
        public boolean OUTPUT_TO_CUR_DIR;
        public String OUTPUT_DIR_PREFIX;
        public String JAVA;
    }
    
    Random rand = new Random();
    
    private static double mps_to_kph(double metersPerSecond) {
        return ((metersPerSecond * 3600)/1000);
    }
    
    /**
     * Writes all the cfg and xml file for a L3Comms type scenario
     */
    public L3Create() {
        if(!OUTPUT_TO_CUR_DIR) {
            File locDir = new File(loc);
            OUTPUT_DIR = OUTPUT_DIR_PREFIXS+locDir.getName();
            OUTPUT_DIRS = OUTPUT_DIR+SEP;
            Machinetta.Debugger.debug("Setting log output dir to "+OUTPUT_DIR,1,this);
        }
        
        Vector teamMembers = new Vector();
        
        // For now, add all proxy ids to the team
        for (int i = 0; i < noUAVs; i++) {
            Machinetta.Debugger.debug("Adding UAV"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("UAV"+i));
        }
        
        for (int i = 0; i < noArmoredUAVs; i++) {
            Machinetta.Debugger.debug("Adding AUAV"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("AUAV"+i));
        }
        
        for (int i = 0; i < noEOIRUAVs; i++) {
            Machinetta.Debugger.debug("Adding EOIR-UAV"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("EOIR-UAV"+i));
        }
        
        for (int i = 0; i < noRSSIUAVs; i++) {
            Machinetta.Debugger.debug("Adding RSSI-UAV"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("RSSI-UAV"+i));
        }
        
        for (int i = 0; i < noDetectors; i++) {
            Machinetta.Debugger.debug("Adding Detector"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("Detector"+i));
        }
        
        for (int i = 0; i < noGeolocators; i++) {
            Machinetta.Debugger.debug("Adding Geolocator"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("Geolocator"+i));
        }
        
        for (int i = 0; i < noOperators; i++) {
            Machinetta.Debugger.debug("Adding Operator"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("Operator"+i));
        }
        
        for (int i = 0; i < noTrafficControllers; i++) {
            Machinetta.Debugger.debug("Adding TrafficController"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("TrafficController"+i));
        }
        
        for (int i = 0; i < noUGSs; i++) {
            Machinetta.Debugger.debug("Adding UGS"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("UGS"+i));
        }
        
        for (int i = 0; i < noHumvees; i++) {
            Machinetta.Debugger.debug("Adding H"+i+" to team",1,this);
            teamMembers.add(new NamedProxyID("H"+i));
        }
        
        Machinetta.Debugger.debug("Creating TeamBelief with "+teamMembers.size()+" members",1,this);
        
        // For testing Dynamic Teaming
        // teamMembers.clear();
        // teamMembers.add(new NamedProxyID("Operator0"));
        team = new TeamBelief("TeamAll", teamMembers, new Hashtable());
        
        // Now create specific files
        for (int i = 0; i < noUAVs; i++) {
            createUAVCfgFile("UAV"+i);
            makeUAVBeliefs("UAV"+i, i);
        }
        
        for (int i = 0; i < noEOIRUAVs; i++) {
            createEOIRUAVCfgFile("EOIR-UAV"+i);
            makeEOIRUAVBeliefs("EOIR-UAV"+i, i);
        }
        
        for (int i = 0; i < noRSSIUAVs; i++) {
            if(uavBinaryBayesFilterPanelFirstFourOn) {
                if(i < 4)
                    uavBinaryBayesFilterPanelOn = true;
                else
                    uavBinaryBayesFilterPanelOn = false;
                
            }
            createRSSIUAVCfgFile("RSSI-UAV"+i);
            makeRSSIUAVBeliefs("RSSI-UAV"+i, i);
        }
        
        for (int i = 0; i < noArmoredUAVs; i++) {
            createUAVCfgFile("AUAV"+i);
            makeAUAVBeliefs("AUAV"+i, i);
        }
        
        for (int i = 0; i < noDetectors; i++) {
            createDetectorCfgFile("Detector"+i);
            makeDetectorBeliefs("Detector"+i);
        }
        
        for (int i = 0; i < noGeolocators; i++) {
            createGeolocatorCfgFile("Geolocator"+i);
            makeGeolocatorBeliefs("Geolocator"+i);
        }
        
        for (int i = 0; i < noOperators; i++) {
            createOperatorCfgFile("Operator"+i);
            makeOperatorBeliefs("Operator"+i);
        }
        
        for (int i = 0; i < noTrafficControllers; i++) {
            createTrafficControllerCfgFile("TrafficController"+i);
            makeTrafficControllerBeliefs("TrafficController"+i);
        }
        
        for (int i = 0; i < noUGSs; i++) {
            createUGSCfgFile("UGS"+i);
            makeUGSBeliefs("UGS"+i);
        }
        
        for (int i = 0; i < noHumvees; i++) {
            createHVCfgFile("H"+i);
            makeHVBeliefs("H"+i);
        }
        
        Machinetta.Debugger.debug("Making run script",1,this);
        makeRunScript();
        Machinetta.Debugger.debug("Making Env file",1,this);
        makeEnvFile();
        Machinetta.Debugger.debug("Done with l3create constructor.",1,this);
        
        makeKillScript();
        makeUptimeScript();
        makeUptimesScript();
        makeDeleteLogsScript();
        makeTopScript();
        makeTopsScript();
        makeAllKillScript();
        makeFetchLogsScript();
        
    }
    
    public static void setConfigurationFlag(String flagName) {
        configureFor50by50TDOADemo = false;
        configureFor50by50MultiFreqDemo = false;
        configureFor50by50AccastDemo = false;
        configureFor50by50InfoTech = false;
        configureFor1by1 = false;
	configureFor5by5 = false;
        configureForDeconflictionExperiments = false;
        configureFor1by1Sean = false;
        configureFor1by1Gascola = false;
        configureFor50by50and12UAVsSean = false;
        configureFor1by1MultiEmitterSean = false;
        configureForDeconfliction = false;
        configureForTestingSean = false;
        
        if(flagName.equalsIgnoreCase("configureFor50by50AccastDemo"))
            configureFor50by50AccastDemo = true;
        else if(flagName.equalsIgnoreCase("configureFor50by50TDOADemo"))
            configureFor50by50TDOADemo = true;
        else if(flagName.equalsIgnoreCase("configureFor50by50MultiFreqDemo"))
            configureFor50by50MultiFreqDemo = true;
        else if(flagName.equalsIgnoreCase("configureFor50by50InfoTech"))
            configureFor50by50InfoTech = true;
        else if(flagName.equalsIgnoreCase("configureFor1by1"))
            configureFor1by1 = true;
	else if(flagName.equalsIgnoreCase("configureFor5by5"))
	    configureFor5by5 = true;
        else if(flagName.equalsIgnoreCase("configureForDeconflictionExperiments"))
            configureForDeconflictionExperiments = true;
        else if(flagName.equalsIgnoreCase("configureFor1by1Sean"))
            configureFor1by1Sean = true;
        else if(flagName.equalsIgnoreCase("configureFor1by1Gascola"))
            configureFor1by1Gascola = true;
        else if(flagName.equalsIgnoreCase("configureFor50by50and12UAVsSean"))
            configureFor50by50and12UAVsSean = true;
        else if(flagName.equalsIgnoreCase("configureFor1by1MultiEmitterSean"))
            configureFor1by1MultiEmitterSean = true;
        else if(flagName.equalsIgnoreCase("configureForDeconfliction"))
            configureForDeconfliction = true;
        else if(flagName.equalsIgnoreCase("configureForTestingSean"))
            configureForTestingSean = true;
        else if(flagName.equalsIgnoreCase("configureForMultiFreq"))
            configureForMultiFreq = true;
        else if(flagName.equalsIgnoreCase("configureFor50by50profiling"))
            configureFor50by50profiling = true;
        else {
            System.err.println("unknown configuration='"+flagName+"'");
            throw new RuntimeException("unknown configuration='"+flagName+"'");
        }
    }
    
    public static void confMap50by50() {
        mapHeightMeters = 50000;
        mapWidthMeters = 50000;
        emitterLocations = bigMapEmitterLocations;
        sanjayaAssetCollisionMinZDistMeters = 100;
        sanjayaAssetCollisionRangeMeters = 1000;
    }
    
    public static void confMap1by1() {
        mapWidthMeters = 1000;
        mapHeightMeters = 1000;
        emitterLocations = smallMapEmitterLocations;
        sanjayaAssetCollisionMinZDistMeters = 100;
        sanjayaAssetCollisionRangeMeters = 10;
    }
    
    public static void confMap5by5() {
	mapWidthMeters = 5000;
	mapHeightMeters = 5000;
	emitterLocations = smallMapEmitterLocations;
	sanjayaAssetCollisionMinZDistMeters = 100;
	sanjayaAssetCollisionRangeMeters = 10;
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
	RRTPreferredPathMeters = 5000.0;
	RRTMaxPath = 5000.0;
	RRTBranchRangeX = 1000.0;
	RRTBranchRangeY = 1000.0;
    }

    public static void confBBF50by50() {
        BBFSensorMinRange = 50*50;
        BBFSensorMaxRange = 100*50;
        BBFGridScaleFactor = 50.0;
    }
    
    public static void confBBF1by1() {
        BBFSensorMinRange = 50;
        BBFSensorMaxRange = 100;
        BBFGridScaleFactor = 1.0;
    }
    
    public static void confBBF5by5() {
	BBFSensorMinRange = 250;
	BBFSensorMaxRange = 500;
	BBFGridScaleFactor = 5.0;
    }
    
    public static void confPathPlanner50by50() {
        PathConflictDist = 2000.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 1000.0;
        PathPlannerTTL = 5;
    }
    
    public static void confMapSanjaya001() {
	CTDB_FILE = CTDB_DEFAULT;
	mapLowerLeftLat = mapLowerLeftLatSanjaya001;
	mapLowerLeftLon = mapLowerLeftLonSanjaya001;
    }

    public static void confMapGascola() {
	CTDB_FILE = CTDB_GASCOLA;
	mapLowerLeftLat = 40.4563444444;
	mapLowerLeftLon = -79.789486111;
    }

    public static void confMapAustinAccast50() {
	CTDB_FILE = CTDB_AUSTIN50;
	mapLowerLeftLat = mapLowerLeftLatAustinAccast50;
	mapLowerLeftLon = mapLowerLeftLonAustinAccast50;
    }

    public static void configureFor1by1(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();

        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = true;
        uavPlacementRandom = true;
        noRSSIUAVs = argNoRSSIUAVs;
        randomEmitterLocations = true;
        emitterCount=argNoEmitters;
        noEOIRUAVs = 1;
        
        uavBinaryBayesFilterOn = true;
        uavBinaryBayesFilterPanelOn = false;
        operBinaryBayesFilterOn = true;
        operBinaryBayesFilterPanelOn = true;
        bbfKLDSharingOn = true;
        
        confMap1by1();
        operOneKmGridLinesOn = true;
        sanjayaOneKmGridLinesOn = true;
        uavMaxSpeedMetersPerSec = 4.1666;
        uavMaxTurnRateDegPerStep = 30;
        uavAtLocationDistance = 50;
        uavMoveFinishedHoldRadius = 50;
        uavEODistanceTolerance = 50;
        
        confRRT1by1();
        PathPlannerAtWaypointTolerance = 50.0;
        PathPlannerReplanDistFromEnd = 100.0;
        PathPlannerTTL = 5;
        confBBF1by1();
        PathConflictDist = 100.0;
        OtherVehicleCostMapAvoid = 200;
        OtherVehicleCostMapConflict = 100;
        uavPathDeconflictionOn = false;
        operPathDeconflictionOn = false;
        sanjayaAssetCollisionRangeMeters = 100;
        sanjayaAssetCollisionMinZDistMeters = 100;
    }
    
    public static void configureFor5by5(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	USE_XPLANE_CAMERA = true;
	UAV_DYNAMIC_FLY_ZONES = false;
	OPERATOR_CLUSTERING_ON = true;
	noTrafficControllers = 0;
	uavPlacementRandom = true;
	noRSSIUAVs = argNoRSSIUAVs;
	randomEmitterLocations = true;
	emitterCount=argNoEmitters;
	//	noEOIRUAVs = 1;

	uavBinaryBayesFilterOn = true;
	uavBinaryBayesFilterPanelOn = false;
	operBinaryBayesFilterOn = true;
	operBinaryBayesFilterPanelOn = true;
	bbfKLDSharingOn = true;

	confMap5by5();
	operOneKmGridLinesOn = true;
	sanjayaOneKmGridLinesOn = true;
	uavMaxSpeedMetersPerSec = 41.666;
	uavMaxTurnRateDegPerStep = 30;
	uavAtLocationDistance = 50;
	uavMoveFinishedHoldRadius = 50;
	uavEODistanceTolerance = 50;

	confRRT5by5();
	PathPlannerAtWaypointTolerance = 50.0;
	PathPlannerReplanDistFromEnd = 100.0;
	PathPlannerTTL = 5;
	confBBF5by5();
	PathConflictDist = 100.0;
	OtherVehicleCostMapAvoid = 200;
	OtherVehicleCostMapConflict = 100;
	uavPathDeconflictionOn = false;
	operPathDeconflictionOn = false;
	sanjayaAssetCollisionRangeMeters = 100;
	sanjayaAssetCollisionMinZDistMeters = 100;
    }

    public static void configureFor50by50InfoTech(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();

        emitterCount=argNoEmitters;
        //            BBFSensorMaxRange = 10000;
        //            BBFSensorMinRange = 5000;
        PATHPLANNER_SMOOTH_GRAD_DESC_ON = false;
        PATHPLANNER_REDIRECT_PATH_TO_TARGET = false;
        END_CONDITION_CAPTURE_THE_FLAG = false;
        END_CONDITION_ALL_HMMMVS_DEAD = false;
        END_CONDITION_ALL_OPFOR_DEAD = false;
        END_CONDITION_ALL_BLUEFOR_DEAD = false;
        END_CONDITION_ALL_EMITTERS_DETECTED = true;
        BBFSensorMaxRange = 5000;
        BBFSensorMinRange = 2500;
        uavEOIR_EO_CAP_PERCENT = 25;
        uavEOIR_IR_CAP_PERCENT = 25;
        randomEmitterLocations = true;
        emitterLocations = null;
        uavEODistanceTolerance = 1000;
        noEOIRUAVs = 1;
        operBinaryBayesFilterOn = true;
        operBinaryBayesFilterPanelOn = true;
        bbfRandomSharingOn = false;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        bbfRemoteKLDSharingOn = false;
        bbfKLDSharingOn = true;
        
        noRSSIUAVs = argNoRSSIUAVs;
        PathPlannerTTL = argPathPlannerTTL;
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        BBFGridScaleFactor = 50.0;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        diffusionInterval = 5000/50;
        diffusionRate = 0.0001/50;
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavBinaryBayesFilterOn = true;
        uavPlacementRandom = true;
        uavMaxSpeedMetersPerSec = 41.66;
    }
    
    public static void configureForTestingSean(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        //            BBFSensorMaxRange = 10000;
        //            BBFSensorMinRange = 5000;
        END_CONDITION_CAPTURE_THE_FLAG = false;
        END_CONDITION_ALL_HMMMVS_DEAD = false;
        END_CONDITION_ALL_OPFOR_DEAD = false;
        END_CONDITION_ALL_BLUEFOR_DEAD = false;
        END_CONDITION_ALL_EMITTERS_DETECTED = true;
        PATHPLANNER_SMOOTH_GRAD_DESC_ON = false;
        PATHPLANNER_REDIRECT_PATH_TO_TARGET = false;
        BBFSensorMaxRange = 5000;
        BBFSensorMinRange = 2500;
        uavEOIR_EO_CAP_PERCENT = 25;
        uavEOIR_IR_CAP_PERCENT = 25;
        randomEmitterLocations = true;
        emitterCount=argNoEmitters;
        emitterLocations = null;
        uavEODistanceTolerance = 1000;
        noEOIRUAVs = 1;
        operBinaryBayesFilterOn = true;
        operBinaryBayesFilterPanelOn = true;
        bbfRandomSharingOn = false;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        bbfRemoteKLDSharingOn = false;
        bbfKLDSharingOn = true;
        
        noRSSIUAVs = argNoRSSIUAVs;
        PathPlannerTTL = argPathPlannerTTL;
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        BBFGridScaleFactor = 50.0;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        diffusionInterval = 5000/50;
        diffusionRate = 0.0001/50;
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavBinaryBayesFilterOn = true;
        uavPlacementRandom = true;
        uavMaxSpeedMetersPerSec = 41.66;
    }
    
    public static void configureFor50by50AccastDemo(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapAustinAccast50();
	noTrafficControllers = 0;
        emitterCount=argNoEmitters;
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = false;	// @TODO Really it should be on... and we should create a separate config for zone controller
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        noEOIRUAVs = 1;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        bbfRandomSharingOn = true;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        uavBinaryBayesFilterOn = false;
        
        noRSSIUAVs = argNoRSSIUAVs;
        noCivilians = argNoCivilians;
        PathPlannerTTL = argPathPlannerTTL;
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        BBFGridScaleFactor = 50.0;
        BBFSensorMaxRange = 100*50;
        BBFSensorMinRange = 50*50;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavPlacementRandom = true;
    }
    
    public static void configureForMultiFreq(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        UAV_DYNAMIC_FLY_ZONES = false;
        emitterChannels = true;
        noTrafficControllers = 0;
        noEOIRUAVs = 0;
        OPERATOR_CLUSTERING_ON = false;	// @TODO Really it should be on... and we should create a separate config for zone controller
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        operBinaryBayesFilterOn = true;
        operBinaryBayesFilterPanelOn = true;
        bbfRandomSharingOn = true;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        uavBinaryBayesFilterOn = true;
        
        noRSSIUAVs = argNoRSSIUAVs;
        PathPlannerTTL = argPathPlannerTTL;
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        BBFGridScaleFactor = 50.0;
        BBFSensorMaxRange = 100*50;
        BBFSensorMinRange = 50*50;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavPlacementRandom = true;
        
    }
    
    public static void configureFor50by50profiling(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        uavProxyMemoryMegabytes = 128;
        uavRandomSmallMovesCostmapOn = true;
        emitterCount=argNoEmitters;
        UAV_DYNAMIC_FLY_ZONES = false;
        emitterChannels = true;
        noTrafficControllers = 0;
        noEOIRUAVs = 0;
        OPERATOR_CLUSTERING_ON = false;
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        bbfRandomSharingOn = false;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        uavBinaryBayesFilterOn = false;
        
        noRSSIUAVs = argNoRSSIUAVs;
        PathPlannerTTL = argPathPlannerTTL;
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        uavPathDeconflictionOn = argDeconflictOn;
        confBBF50by50();
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavPlacementRandom = true;
    }
    
    public static void configureFor1by1Sean(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        uavBinaryBayesFilterPanelOn = true;
        confMap1by1();
        emitterCount=1;
        uavMaxSpeedMetersPerSec = 4.1666;
        confRRT1by1();
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 100.0;
        PathPlannerTTL = 5;
        confBBF1by1();
        OtherVehicleCostMapAvoid = 300;
        OtherVehicleCostMapConflict = 10;
        PathConflictDist = 10.0;
        diffusionInterval = 5000/25;
        diffusionRate = 0.0001/25;
        uavPathDeconflictionOn = true;
        sanjayaUpdateRate = 25;
        operPathDeconflictionOn = false;
        OtherVehicleCostmapOn = true;
        uavPathDeconflictionOn = true;
        uavBinaryBayesFilterOn = true;
    }
    
    static String gascolaUavLocations[] = {"450 600 500", "460 600 750", "470 600 1000"};
    public static void configureFor1by1Gascola(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	// At gascola everything runs on a single laptop, no slave machines
	SLAVE_MACHINES = null;
	rssiUavLocations = gascolaUavLocations;
	uavPlacementRandom = false;
	confMapGascola();
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = false;
        noRSSIUAVs = argNoRSSIUAVs;
        randomEmitterLocations = true;
        emitterCount=argNoEmitters;
        noEOIRUAVs = 0;
        noTrafficControllers = 0;
        
        uavBinaryBayesFilterOn = true;
        uavBinaryBayesFilterPanelOn = false;
	uavRRTPathPanelOn = true;
	uavRRTTreePanelOn = false;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        bbfKLDSharingOn = true;
        
        confMap1by1();
        operOneKmGridLinesOn = true;
        sanjayaOneKmGridLinesOn = true;
        uavMaxSpeedMetersPerSec = 4.1666;
        uavMaxTurnRateDegPerStep = 30;
        uavAtLocationDistance = 50;
        uavMoveFinishedHoldRadius = 50;
        uavEODistanceTolerance = 50;
        
        confRRT1by1();
        PathPlannerAtWaypointTolerance = 50.0;
        PathPlannerReplanDistFromEnd = 100.0;
        PathPlannerTTL = 5;
        confBBF1by1();
        PathConflictDist = 10.0;
	PathConflictZDist = 10.0;
        OtherVehicleCostMapAvoid = 20;
        OtherVehicleCostMapConflict = 10;
        uavPathDeconflictionOn = false;
        operPathDeconflictionOn = false;
        sanjayaAssetCollisionRangeMeters = 10;
        sanjayaAssetCollisionMinZDistMeters = 10;
	MASTER_DISPLAY="zorba.cimds.ri.cmu.edu:0";
	SLAVE_DISPLAY="zorba.cimds.ri.cmu.edu:0";

	EnvFileAdditionalLines = new String[8];
	EnvFileAdditionalLines[0] = "# PAVEMENT PAV01 40.46293427461115 -79.78496723292629 40.46280106320477 -79.78496507250132 40.46267970363885 -79.78504155173751 40.4625452571847 -79.78496875740203 40.46249467969226 -79.7850046069226 40.46231568431917 -79.78516858758221 40.46218397347801 -79.78528544368258 40.46191544018694 -79.78522626579195 40.46168313062685 -79.78532912449188 40.46118551665584 -79.78567205517069 40.46084268480557 -79.78564781986816 40.46062380285112 -79.78551606854022 40.46026242091882 -79.7854397519632 40.45988894682643 -79.78546724048323 40.4596541759173 -79.7855324517094 40.45920288988555 -79.78532178742765 40.45839611484225 -79.78427524004815 40.45806058565161 -79.78330658825811 40.45780485601028 -79.78271933155038 40.4576307888408 -79.78220979791277 40.46031779276992 -79.78008683277645 40.46081947101542 -79.78085270678484 40.46203717847606 -79.78286610788851 40.46235786284777 -79.78324198933066 40.46236839528719 -79.78323584189234 40.46240977088341 -79.78338108156916 40.46258170713863 -79.78348521824658 40.46284464497738 -79.78366845868902 40.46277485427414 -79.78392311501024 40.46297377183123 -79.78397525206248 40.46316654149312 -79.78392854329771 40.46323010456828 -79.78403790802643 40.46306670574931 -79.78418463248914 40.46290521958137 -79.7843565498325 40.46291262741784 -79.78463679705739 40.46295017513234 -79.78485228616576 40.46293427461115 -79.78496723292629  ";
	EnvFileAdditionalLines[1] = "";
	EnvFileAdditionalLines[2] = "# PAVEMENT PAV02 40.46219253540039 -79.78369903564453 40.46220016479492 -79.7836685180664 40.46220779418945 -79.78360748291016 40.46217727661133 -79.78348541259766 40.4621696472168 -79.78343963623047 40.46213912963867 -79.78327178955078 40.46213912963867 -79.7832260131836 40.46213912963867 -79.78321075439453 40.46213150024414 -79.7831802368164 40.46210861206055 -79.7831802368164 40.462039947509766 -79.78316497802734 40.46195602416992 -79.78314971923828 40.46188735961914 -79.7831039428711 40.461788177490234 -79.7830581665039 40.46175003051758 -79.78304290771484 40.46171951293945 -79.78301239013672 40.46158981323242 -79.78302764892578 40.46152877807617 -79.78307342529297 40.46152114868164 -79.78307342529297 40.46152114868164 -79.78307342529297 40.4614143371582 -79.78316497802734 40.46139144897461 -79.7831802368164 40.46134567260742 -79.78321075439453 40.46132278442383 -79.7832260131836 40.46128463745117 -79.78324127197266 40.46120071411133 -79.78328704833984 40.461185455322266 -79.78328704833984 40.461177825927734 -79.78328704833984 40.4611701965332 -79.78331756591797 40.461124420166016 -79.7833480834961 40.46107864379883 -79.78337860107422 40.4610710144043 -79.78337860107422 40.46072769165039 -79.78314971923828 40.46071243286133 -79.78313446044922 40.46065902709961 -79.7831802368164 40.46066665649414 -79.78324127197266 40.46067428588867 -79.7833023071289 40.46067428588867 -79.78333282470703 40.4606819152832 -79.78336334228516 40.4606819152832 -79.78337860107422 40.46066665649414 -79.7834243774414 40.46066665649414 -79.78353118896484 40.46065902709961 -79.78356170654297 40.46065902709961 -79.78357696533203 40.46066665649414 -79.7835922241211 40.46066665649414 -79.78362274169922 40.46067428588867 -79.78365325927734 40.46066665649414 -79.78369903564453 40.46066665649414 -79.78372955322266 40.460636138916016 -79.78394317626953 40.46065902709961 -79.78404998779297 40.46071243286133 -79.78409576416016 40.46073532104492 -79.78412628173828 40.46078109741211 -79.78417205810547 40.460819244384766 -79.78418731689453 40.4608268737793 -79.78418731689453 40.46084976196289 -79.78421783447266 40.46089553833008 -79.78423309326172 40.46091079711914 -79.78421783447266 40.461002349853516 -79.78417205810547 40.4610710144043 -79.78421783447266 40.461116790771484 -79.78424835205078 40.46113967895508 -79.78426361083984 40.461177825927734 -79.78429412841797 40.46121597290039 -79.78430938720703 40.461238861083984 -79.7843246459961 40.46126174926758 -79.78433990478516 40.4613151550293 -79.78435516357422 40.46138381958008 -79.78437042236328 40.46152114868164 -79.78433990478516 40.46156692504883 -79.78433990478516 40.46158218383789 -79.78433990478516 40.46162033081055 -79.78433990478516 40.46169662475586 -79.7843246459961 40.461727142333984 -79.78430938720703 40.46176528930664 -79.78435516357422 40.4617805480957 -79.78438568115234 40.4617805480957 -79.7844009399414 40.46186447143555 -79.78450775146484 40.461917877197266 -79.7845687866211 40.46194076538086 -79.78459930419922 40.461971282958984 -79.7846450805664 40.46199417114258 -79.78467559814453 40.46201705932617 -79.78470611572266 40.462093353271484 -79.7848129272461 40.46211624145508 -79.78482818603516 40.46218490600586 -79.78490447998047 40.46220016479492 -79.78491973876953 40.462223052978516 -79.78491973876953 40.46224594116211 -79.7849349975586 40.46225357055664 -79.7849349975586 40.4622917175293 -79.7849349975586 40.462337493896484 -79.78491973876953 40.46236038208008 -79.7848892211914 40.46238327026367 -79.78484344482422 40.46245193481445 -79.7848129272461 40.46247482299805 -79.78478240966797 40.46249008178711 -79.7847671508789 40.46249771118164 -79.78467559814453 40.462459564208984 -79.78467559814453 40.46243667602539 -79.78466033935547 40.4624137878418 -79.78466033935547 40.4623908996582 -79.7846450805664 40.46236038208008 -79.78461456298828  ";
	EnvFileAdditionalLines[3] = "";
	EnvFileAdditionalLines[4] = "PAVEMENT FLYZONE1VERYSML 40.46279603211546 -79.78469695537027 40.46254636235219 -79.78488485106374 40.4623783677449 -79.78499572046293 40.46235510171693 -79.7850033644317 40.46220361333166 -79.78501481523976 40.46206105707484 -79.78503034569168 40.46180083749636 -79.78506807741448 40.46177765811589 -79.78506100106955 40.46169052569126 -79.78504763419855 40.46159693501739 -79.78500959469375 40.46157362047662 -79.78499464862401 40.46150207286768 -79.78489647929584 40.46140987733746 -79.78478882678137 40.46132394396478 -79.78473444340514 40.46123164598507 -79.78458384733091 40.46126029220957 -79.78443743666209 40.46126916985446 -79.78436476621626 40.46128089013337 -79.78422585934116 40.46128088989041 -79.78421043613997 40.46129845206325 -79.78404061265356 40.46132780534169 -79.78387877059275 40.46134561368325 -79.78378677695379 40.46134567800626 -79.78376378755803 40.46134610829267 -79.7836102496574 40.46135230289836 -79.78349475746585 40.46139981420744 -79.78336354931911 40.46157154170208 -79.78315922530547 40.46167175935084 -79.78318465935332 40.4617587080368 -79.78325319660138 40.46181349161743 -79.783325143905 40.46186611221332 -79.78336375510811 40.46199424281985 -79.78342552309522 40.46213889624954 -79.78353657978664 40.46220796594454 -79.78357905045536 40.46228624002399 -79.78362561304209 40.46242801574664 -79.78373053214946 40.46250965649955 -79.78379750325179 40.46259935183484 -79.78389769140534 40.46267181841112 -79.78401446928595 40.46274562207017 -79.78414371465134 40.46275116450948 -79.78416654771041 40.4628356625897 -79.78433591957013 40.46281736416128 -79.78459108370562 40.46279603211546 -79.78469695537027";
	EnvFileAdditionalLines[5] = "PAVEMENT FLYZONE2MEDSML  40.46283708098864 -79.78434126531359 40.4628008839805 -79.78469092950475 40.46255795829242 -79.78487491586751 40.46237747233286 -79.78499205539045 40.46215101881769 -79.7850448635994 40.46180533556516 -79.78507440821977 40.46159507004462 -79.78500239703432 40.46142303341402 -79.78479367173614 40.46102742558937 -79.78444242407616 40.46070429815747 -79.78414844714371 40.4604194565081 -79.78385161712288 40.4599526973772 -79.78350898156978 40.45972628626072 -79.78319750186742 40.45958904416687 -79.78295625194249 40.45946273702058 -79.78259616978014 40.45951400126155 -79.78229744578485 40.46010131182004 -79.78173897526366 40.46058852379877 -79.78200737807379 40.4606076130878 -79.78205043168197 40.46121746604285 -79.78266553178627 40.46158490459624 -79.78308499349313 40.46201407634975 -79.78343170652798 40.46241486549756 -79.78367501820279 40.46269513476651 -79.78399123454314 40.46283708098864 -79.78434126531359 ";
	EnvFileAdditionalLines[6] = "PAVEMENT FLZONE3MED 40.46281319734936 -79.78458200313472 40.46280136126733 -79.7847036281803 40.46240284126982 -79.7849842287842 40.4621670056263 -79.78518713465138 40.461988737113 -79.78524851389317 40.46192680821815 -79.78527573836342 40.46149246845146 -79.78554371088228 40.46112112391075 -79.78556638991601 40.46081241819547 -79.78552462506286 40.46037953169981 -79.78552720775546 40.45984023393353 -79.78552096893613 40.4596190859162 -79.78545995775876 40.45936717285235 -79.78513705850956 40.45923937312308 -79.78481267284253 40.45912968136484 -79.78451620958516 40.45903980896183 -79.7841476592308 40.45899639573276 -79.78356469913032 40.45898729129589 -79.78330406293313 40.4590532756129 -79.78304162271427 40.45914010942893 -79.78280003145598 40.45927572815611 -79.78257952418815 40.45948909746819 -79.78230504583196 40.45960121199513 -79.78222103622568 40.4598879508401 -79.78197783551779 40.46003150958677 -79.78180862417895 40.46024019764814 -79.78179884940916 40.46058653714383 -79.78202549222033 40.46089893962178 -79.78235901500419 40.46104919781532 -79.78250822457027 40.46116417084566 -79.78264906366881 40.46127924120095 -79.78273256104042 40.46149752917525 -79.78295341709087 40.46155948687564 -79.78306316045263 40.46182308138743 -79.78330553379965 40.46221348119982 -79.78355900146252 40.46264014606519 -79.78393204035463 40.46284250188642 -79.78432314268623 40.46281319734936 -79.78458200313472 ";
	EnvFileAdditionalLines[7] = "PAVEMENT FLYZONE4MEDLRG 40.46294903608363 -79.78437310192093 40.46316694874945 -79.78492165549911 40.46316301325797 -79.78544732534797 40.46315699465336 -79.78622138593008 40.46313403865669 -79.78684196013347 40.46289097352111 -79.78731874191774 40.46237953589252 -79.78759112757956 40.46159503384615 -79.78765998176451 40.46151518822161 -79.7876575833107 40.4607846523039 -79.78763329383023 40.46029316967611 -79.78763768370646 40.45987865461549 -79.78764739411484 40.45925214826188 -79.78766557581356 40.45921982488578 -79.78766665887042 40.45855931479241 -79.78742677350904 40.45849481026931 -79.78740667870129 40.45777470587225 -79.78698475884393 40.4572301409619 -79.78648348123824 40.45718976247321 -79.78599979844393 40.45717825918491 -79.78538791488518 40.4572825790843 -79.78492185544931 40.45732124163756 -79.7844321585152 40.45768788497309 -79.78389673398098 40.45802333872388 -79.78353133902998 40.45805568385757 -79.78350978210442 40.45841391053247 -79.78325030101841 40.45877616766932 -79.78294674991392 40.45905723165338 -79.78270858852854 40.45926808922668 -79.78252290703657 40.45945380728649 -79.78233378668219 40.45964972447142 -79.7822214542712 40.45986567648243 -79.78201469660512 40.46013576735414 -79.781777230362 40.46031354251344 -79.78190655345968 40.46046600391706 -79.78198143364247 40.46064208957021 -79.7821258242014 40.46090573795121 -79.78234429109841 40.46124919600171 -79.78264785822894 40.46144403157603 -79.78288432506831 40.46180007270734 -79.78322618883729 40.4620450047973 -79.7834578022112 40.46244320637219 -79.78370046227821 40.46266273062063 -79.78396216203321 40.46280592564656 -79.78418297798019 40.46294903608363 -79.78437310192093 ";

	UAVCfgAdditionalLines = new String[15];
	UAVCfgAdditionalLines[0] = "# FLY_ZONE_POLY 40.46293427461115 -79.78496723292629 40.46280106320477 -79.78496507250132 40.46267970363885 -79.78504155173751 40.4625452571847 -79.78496875740203 40.46249467969226 -79.7850046069226 40.46231568431917 -79.78516858758221 40.46218397347801 -79.78528544368258 40.46191544018694 -79.78522626579195 40.46168313062685 -79.78532912449188 40.46118551665584 -79.78567205517069 40.46084268480557 -79.78564781986816 40.46062380285112 -79.78551606854022 40.46026242091882 -79.7854397519632 40.45988894682643 -79.78546724048323 40.4596541759173 -79.7855324517094 40.45920288988555 -79.78532178742765 40.45839611484225 -79.78427524004815 40.45806058565161 -79.78330658825811 40.45780485601028 -79.78271933155038 40.4576307888408 -79.78220979791277 40.46031779276992 -79.78008683277645 40.46081947101542 -79.78085270678484 40.46203717847606 -79.78286610788851 40.46235786284777 -79.78324198933066 40.46236839528719 -79.78323584189234 40.46240977088341 -79.78338108156916 40.46258170713863 -79.78348521824658 40.46284464497738 -79.78366845868902 40.46277485427414 -79.78392311501024 40.46297377183123 -79.78397525206248 40.46316654149312 -79.78392854329771 40.46323010456828 -79.78403790802643 40.46306670574931 -79.78418463248914 40.46290521958137 -79.7843565498325 40.46291262741784 -79.78463679705739 40.46295017513234 -79.78485228616576 40.46293427461115 -79.78496723292629  ";
	UAVCfgAdditionalLines[1] = "";
	UAVCfgAdditionalLines[2] = "FLY_ZONE_POLY 40.46219253540039 -79.78369903564453 40.46220016479492 -79.7836685180664 40.46220779418945 -79.78360748291016 40.46217727661133 -79.78348541259766 40.4621696472168 -79.78343963623047 40.46213912963867 -79.78327178955078 40.46213912963867 -79.7832260131836 40.46213912963867 -79.78321075439453 40.46213150024414 -79.7831802368164 40.46210861206055 -79.7831802368164 40.462039947509766 -79.78316497802734 40.46195602416992 -79.78314971923828 40.46188735961914 -79.7831039428711 40.461788177490234 -79.7830581665039 40.46175003051758 -79.78304290771484 40.46171951293945 -79.78301239013672 40.46158981323242 -79.78302764892578 40.46152877807617 -79.78307342529297 40.46152114868164 -79.78307342529297 40.46152114868164 -79.78307342529297 40.4614143371582 -79.78316497802734 40.46139144897461 -79.7831802368164 40.46134567260742 -79.78321075439453 40.46132278442383 -79.7832260131836 40.46128463745117 -79.78324127197266 40.46120071411133 -79.78328704833984 40.461185455322266 -79.78328704833984 40.461177825927734 -79.78328704833984 40.4611701965332 -79.78331756591797 40.461124420166016 -79.7833480834961 40.46107864379883 -79.78337860107422 40.4610710144043 -79.78337860107422 40.46072769165039 -79.78314971923828 40.46071243286133 -79.78313446044922 40.46065902709961 -79.7831802368164 40.46066665649414 -79.78324127197266 40.46067428588867 -79.7833023071289 40.46067428588867 -79.78333282470703 40.4606819152832 -79.78336334228516 40.4606819152832 -79.78337860107422 40.46066665649414 -79.7834243774414 40.46066665649414 -79.78353118896484 40.46065902709961 -79.78356170654297 40.46065902709961 -79.78357696533203 40.46066665649414 -79.7835922241211 40.46066665649414 -79.78362274169922 40.46067428588867 -79.78365325927734 40.46066665649414 -79.78369903564453 40.46066665649414 -79.78372955322266 40.460636138916016 -79.78394317626953 40.46065902709961 -79.78404998779297 40.46071243286133 -79.78409576416016 40.46073532104492 -79.78412628173828 40.46078109741211 -79.78417205810547 40.460819244384766 -79.78418731689453 40.4608268737793 -79.78418731689453 40.46084976196289 -79.78421783447266 40.46089553833008 -79.78423309326172 40.46091079711914 -79.78421783447266 40.461002349853516 -79.78417205810547 40.4610710144043 -79.78421783447266 40.461116790771484 -79.78424835205078 40.46113967895508 -79.78426361083984 40.461177825927734 -79.78429412841797 40.46121597290039 -79.78430938720703 40.461238861083984 -79.7843246459961 40.46126174926758 -79.78433990478516 40.4613151550293 -79.78435516357422 40.46138381958008 -79.78437042236328 40.46152114868164 -79.78433990478516 40.46156692504883 -79.78433990478516 40.46158218383789 -79.78433990478516 40.46162033081055 -79.78433990478516 40.46169662475586 -79.7843246459961 40.461727142333984 -79.78430938720703 40.46176528930664 -79.78435516357422 40.4617805480957 -79.78438568115234 40.4617805480957 -79.7844009399414 40.46186447143555 -79.78450775146484 40.461917877197266 -79.7845687866211 40.46194076538086 -79.78459930419922 40.461971282958984 -79.7846450805664 40.46199417114258 -79.78467559814453 40.46201705932617 -79.78470611572266 40.462093353271484 -79.7848129272461 40.46211624145508 -79.78482818603516 40.46218490600586 -79.78490447998047 40.46220016479492 -79.78491973876953 40.462223052978516 -79.78491973876953 40.46224594116211 -79.7849349975586 40.46225357055664 -79.7849349975586 40.4622917175293 -79.7849349975586 40.462337493896484 -79.78491973876953 40.46236038208008 -79.7848892211914 40.46238327026367 -79.78484344482422 40.46245193481445 -79.7848129272461 40.46247482299805 -79.78478240966797 40.46249008178711 -79.7847671508789 40.46249771118164 -79.78467559814453 40.462459564208984 -79.78467559814453 40.46243667602539 -79.78466033935547 40.4624137878418 -79.78466033935547 40.4623908996582 -79.7846450805664 40.46236038208008 -79.78461456298828  ";
	UAVCfgAdditionalLines[3] = "";
	UAVCfgAdditionalLines[4] = "# FLYZONE1VERYSML";
	UAVCfgAdditionalLines[5] = "#FLY_ZONE_POLY 40.46279603211546 -79.78469695537027 40.46254636235219 -79.78488485106374 40.4623783677449 -79.78499572046293 40.46235510171693 -79.7850033644317 40.46220361333166 -79.78501481523976 40.46206105707484 -79.78503034569168 40.46180083749636 -79.78506807741448 40.46177765811589 -79.78506100106955 40.46169052569126 -79.78504763419855 40.46159693501739 -79.78500959469375 40.46157362047662 -79.78499464862401 40.46150207286768 -79.78489647929584 40.46140987733746 -79.78478882678137 40.46132394396478 -79.78473444340514 40.46123164598507 -79.78458384733091 40.46126029220957 -79.78443743666209 40.46126916985446 -79.78436476621626 40.46128089013337 -79.78422585934116 40.46128088989041 -79.78421043613997 40.46129845206325 -79.78404061265356 40.46132780534169 -79.78387877059275 40.46134561368325 -79.78378677695379 40.46134567800626 -79.78376378755803 40.46134610829267 -79.7836102496574 40.46135230289836 -79.78349475746585 40.46139981420744 -79.78336354931911 40.46157154170208 -79.78315922530547 40.46167175935084 -79.78318465935332 40.4617587080368 -79.78325319660138 40.46181349161743 -79.783325143905 40.46186611221332 -79.78336375510811 40.46199424281985 -79.78342552309522 40.46213889624954 -79.78353657978664 40.46220796594454 -79.78357905045536 40.46228624002399 -79.78362561304209 40.46242801574664 -79.78373053214946 40.46250965649955 -79.78379750325179 40.46259935183484 -79.78389769140534 40.46267181841112 -79.78401446928595 40.46274562207017 -79.78414371465134 40.46275116450948 -79.78416654771041 40.4628356625897 -79.78433591957013 40.46281736416128 -79.78459108370562 40.46279603211546 -79.78469695537027 ";
	UAVCfgAdditionalLines[6] = "";
	UAVCfgAdditionalLines[7] = "# FLYZONE2MEDSML";
	UAVCfgAdditionalLines[8] = "# FLY_ZONE_POLY 40.46283708098864 -79.78434126531359 40.4628008839805 -79.78469092950475 40.46255795829242 -79.78487491586751 40.46237747233286 -79.78499205539045 40.46215101881769 -79.7850448635994 40.46180533556516 -79.78507440821977 40.46159507004462 -79.78500239703432 40.46142303341402 -79.78479367173614 40.46102742558937 -79.78444242407616 40.46070429815747 -79.78414844714371 40.4604194565081 -79.78385161712288 40.4599526973772 -79.78350898156978 40.45972628626072 -79.78319750186742 40.45958904416687 -79.78295625194249 40.45946273702058 -79.78259616978014 40.45951400126155 -79.78229744578485 40.46010131182004 -79.78173897526366 40.46058852379877 -79.78200737807379 40.4606076130878 -79.78205043168197 40.46121746604285 -79.78266553178627 40.46158490459624 -79.78308499349313 40.46201407634975 -79.78343170652798 40.46241486549756 -79.78367501820279 40.46269513476651 -79.78399123454314 40.46283708098864 -79.78434126531359 ";
	UAVCfgAdditionalLines[9] = "";
	UAVCfgAdditionalLines[10] = "# FLZONE3MED";
	UAVCfgAdditionalLines[11] = "# FLY_ZONE_POLY 40.46281319734936 -79.78458200313472 40.46280136126733 -79.7847036281803 40.46240284126982 -79.7849842287842 40.4621670056263 -79.78518713465138 40.461988737113 -79.78524851389317 40.46192680821815 -79.78527573836342 40.46149246845146 -79.78554371088228 40.46112112391075 -79.78556638991601 40.46081241819547 -79.78552462506286 40.46037953169981 -79.78552720775546 40.45984023393353 -79.78552096893613 40.4596190859162 -79.78545995775876 40.45936717285235 -79.78513705850956 40.45923937312308 -79.78481267284253 40.45912968136484 -79.78451620958516 40.45903980896183 -79.7841476592308 40.45899639573276 -79.78356469913032 40.45898729129589 -79.78330406293313 40.4590532756129 -79.78304162271427 40.45914010942893 -79.78280003145598 40.45927572815611 -79.78257952418815 40.45948909746819 -79.78230504583196 40.45960121199513 -79.78222103622568 40.4598879508401 -79.78197783551779 40.46003150958677 -79.78180862417895 40.46024019764814 -79.78179884940916 40.46058653714383 -79.78202549222033 40.46089893962178 -79.78235901500419 40.46104919781532 -79.78250822457027 40.46116417084566 -79.78264906366881 40.46127924120095 -79.78273256104042 40.46149752917525 -79.78295341709087 40.46155948687564 -79.78306316045263 40.46182308138743 -79.78330553379965 40.46221348119982 -79.78355900146252 40.46264014606519 -79.78393204035463 40.46284250188642 -79.78432314268623 40.46281319734936 -79.78458200313472 ";
	UAVCfgAdditionalLines[12] = "";
	UAVCfgAdditionalLines[13] = "# FLYZONE4MEDLRG";
	UAVCfgAdditionalLines[14] = "# FLY_ZONE_POLY 40.46294903608363 -79.78437310192093 40.46316694874945 -79.78492165549911 40.46316301325797 -79.78544732534797 40.46315699465336 -79.78622138593008 40.46313403865669 -79.78684196013347 40.46289097352111 -79.78731874191774 40.46237953589252 -79.78759112757956 40.46159503384615 -79.78765998176451 40.46151518822161 -79.7876575833107 40.4607846523039 -79.78763329383023 40.46029316967611 -79.78763768370646 40.45987865461549 -79.78764739411484 40.45925214826188 -79.78766557581356 40.45921982488578 -79.78766665887042 40.45855931479241 -79.78742677350904 40.45849481026931 -79.78740667870129 40.45777470587225 -79.78698475884393 40.4572301409619 -79.78648348123824 40.45718976247321 -79.78599979844393 40.45717825918491 -79.78538791488518 40.4572825790843 -79.78492185544931 40.45732124163756 -79.7844321585152 40.45768788497309 -79.78389673398098 40.45802333872388 -79.78353133902998 40.45805568385757 -79.78350978210442 40.45841391053247 -79.78325030101841 40.45877616766932 -79.78294674991392 40.45905723165338 -79.78270858852854 40.45926808922668 -79.78252290703657 40.45945380728649 -79.78233378668219 40.45964972447142 -79.7822214542712 40.45986567648243 -79.78201469660512 40.46013576735414 -79.781777230362 40.46031354251344 -79.78190655345968 40.46046600391706 -79.78198143364247 40.46064208957021 -79.7821258242014 40.46090573795121 -79.78234429109841 40.46124919600171 -79.78264785822894 40.46144403157603 -79.78288432506831 40.46180007270734 -79.78322618883729 40.4620450047973 -79.7834578022112 40.46244320637219 -79.78370046227821 40.46266273062063 -79.78396216203321 40.46280592564656 -79.78418297798019 40.46294903608363 -79.78437310192093 ";

    }
    
    public static void configureFor1by1MultiEmitterSean(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        confMap1by1();
        uavMaxSpeedMetersPerSec = 4.1666;
        confRRT1by1();
        PathPlannerAtWaypointTolerance = 50.0;
        PathPlannerReplanDistFromEnd = 100.0;
        PathPlannerTTL = 5;
        confBBF1by1();
        OtherVehicleCostMapAvoid = 300;
        OtherVehicleCostMapConflict = 10;
        PathConflictDist = 10.0;
        diffusionInterval = 5000/50;
        diffusionRate = 0.0001/50;
        uavPathDeconflictionOn = false;
        sanjayaUpdateRate = 25;
    }
    
    public static void configureFor50by50and12UAVsSean(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        uavPlacementRandom = true;
        confMap50by50();
        uavMaxSpeedMetersPerSec = 141.66;
        RRTPreferredPathMeters = 15000.0;
        RRTMaxPath = 25000.0;
        RRTBranchRangeX = 1000.0;
        RRTBranchRangeY = 1000.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 500.0;
        PathPlannerTTL = 5;
        confBBF50by50();
        OtherVehicleCostMapAvoid = 2000;
        OtherVehicleCostMapConflict = 2000;
        PathConflictDist = 2000.0;
        sanjayaAssetCollisionRangeMeters = 2000;
        sanjayaAssetCollisionMinZDistMeters = 100;
        diffusionInterval = 5000/50;
        diffusionRate = 0.0001/50;
        uavPathDeconflictionOn = false;
        sanjayaUpdateRate = 25;
        noRSSIUAVs = 12;
    }
    
    public static void configureForDeconfliction(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        // Configuration for deconfliction
        confMap50by50();
        uavMaxSpeedMetersPerSec = 141.666;
        RRTPreferredPathMeters = 15000.0;
        RRTMaxPath = 25000.0;
        RRTBranchRangeX = 1000.0;
        RRTBranchRangeY = 1000.0;
        PathPlannerAtWaypointTolerance = 200.0;
        PathPlannerReplanDistFromEnd = 500.0;
        PathPlannerTTL = 5;
        PathConflictDist = 2000.0;
        confBBF50by50();
        OtherVehicleCostMapAvoid = 2000;
        OtherVehicleCostMapConflict = 1000;
        sanjayaAssetCollisionDetectionOn = true;
        sanjayaAssetCollisionRangeMeters = 2000;
        sanjayaAssetCollisionMinZDistMeters = 100;
        noRSSIUAVs = 10;
        uavPlacementRandom = true;
        uavBinaryBayesFilterOn = false;
        uavRandomEntireMapCostmapOn = true;
    }
    
    public static void configureFor50by50MultiFreqDemo(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        emitterCount=argNoEmitters;
        noRSSIUAVs = argNoRSSIUAVs;
        PathPlannerTTL = argPathPlannerTTL;
        uavPathDeconflictionOn = argDeconflictOn;
        
        emitterChannels = true;
        
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = true;
        noTrafficControllers = 0;
        noEOIRUAVs = 3;
        
        USE_XPLANE_CAMERA = true;
        USE_XPLANE_CAMERA_HOST = "zeta.cimds.ri.cmu.edu";
        
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        operBinaryBayesFilterOn = true;
        operBinaryBayesFilterPanelOn = true;
        bbfRandomSharingOn = true;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        uavBinaryBayesFilterOn = true;
        
        OtherVehicleCostmapOn = true;
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        BBFGridScaleFactor = 50.0;
        BBFSensorMaxRange = 100*50;
        BBFSensorMinRange = 50*50;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        confRRT50by50();
        
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavPlacementRandom = true;
        
    }
    
    public static void configureFor50by50TDOADemo(int argNoRSSIUAVs, int argPathPlannerTTL, boolean argOvcmOn, boolean argDeconflictOn, int argNoEmitters, int argNoCivilians) {
	confMapSanjaya001();
        // Based on multifreqdemo, wiht the following changes;
        emitterCount=argNoEmitters;
        noUAVs = argNoRSSIUAVs;
        noRSSIUAVs = 0;
        PathPlannerTTL = argPathPlannerTTL;
        uavPathDeconflictionOn = argDeconflictOn;
        emitterChannels = true;
        noDetectors = 1;
        noGeolocators = 1;
        noTrafficControllers = 0;
        noEOIRUAVs = 0;
        operBinaryBayesFilterOn = false;
        operBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterOn = false;
        OtherVehicleCostmapOn = argOvcmOn;
        
        confRRT50by50();
        RRTMaxPath = 71000;
        
        // and then the rest of the multifreqdemo settings
        UAV_DYNAMIC_FLY_ZONES = false;
        OPERATOR_CLUSTERING_ON = false;
        
        USE_XPLANE_CAMERA = true;
        USE_XPLANE_CAMERA_HOST = "zeta.cimds.ri.cmu.edu";
        
        uavMaxSpeedMetersPerSec = 41.66;
        uavEODistanceTolerance = 1000;
        bbfRandomSharingOn = true;
        bbfRandomSharingProb = .95;
        bbfRandomSharingTtl = 5;
        diffusionInterval = 5000/50;
        diffusionRate = 0;
        
        uavBinaryBayesFilterPanelOn = false;
        uavBinaryBayesFilterPanelFirstFourOn = false;
        BBFGridScaleFactor = 50.0;
        BBFSensorMaxRange = 100*50;
        BBFSensorMinRange = 50*50;
        OtherVehicleCostMapAvoid = 3000;
        OtherVehicleCostMapConflict = 2000;
        confPathPlanner50by50();
        
        confMap50by50();
        operPathDeconflictionOn = false;
        sanjayaUpdateRate = 50;
        uavPlacementRandom = true;
        
    }
    
    public static void main(String argv[]) {
        int argNoRSSIUAVs = noRSSIUAVs;
        int argPathPlannerTTL=0;
        boolean argOvcmOn = false;
        boolean argDeconflictOn = false;
	int argNoEmitters = emitterCount;
        int argNoCivilians = noCivilians;
        
	int argNoUAVs = noUAVs;
	int argNoArmoredUAVs = noArmoredUAVs;
	int argNoEOIRUAVs = noEOIRUAVs;
	int argNoUGSs = noUGSs;
	int argNoOperators = noOperators;
	int argNoOpforInfantry = noOpforInfantry;
	int argNoBlueforInfantry = noBlueforInfantry;
	int argNoHumvees = noHumvees;

	// @TODO: add params and corresponding proxies for Intelligent
	// mines, C130s and FARRPs.

	// First args is always the directory we'll be writing all the
	// config and script files into;
        loc = argv[0];
        locs = loc + SEP;

	// Check for deprecated command line args;
	// Args [ num_rssi_uavs [ OtherVehicleCostmapsOn [ DeconflictionOn [ numEmitters [ numCivilians ]]]]] 

	int argProcessingStart = 1;
	int argLength = 5;
	if (argv.length < argLength) argLength = 5;
        for(int loopi = 1; loopi < argLength; loopi++) {
	    System.err.println("Checking for deprecated args arg["+loopi+"] ="+argv[loopi]);
	    if(argv[loopi].startsWith("--")) {
		System.err.println("Not using deprecated cmd line args.");
		break;
	    }

	    if(Character.isDigit(argv[loopi].charAt(0))) {
		argProcessingStart++;
		if(loopi == 1) {
		    argNoRSSIUAVs = Integer.parseInt(argv[1]);
		    argPathPlannerTTL = (int)(2*(Math.log(argNoRSSIUAVs))+.5);
		    System.err.println("Deprecated command line args, please use long form (i.e. --rssiuav 1)");
		    argProcessingStart++;
		}
		if(loopi == 2) {
		    int foo = Integer.parseInt(argv[2]);
		    argOvcmOn = (foo == 1) ? true : false;
		    System.err.println("Deprecated command line args, please use long form (i.e. --ovcm)");
		    argProcessingStart++;
		}
		if(loopi == 3) {
		    int foo = Integer.parseInt(argv[3]);
		    argDeconflictOn = (foo == 1) ? true : false;
		    System.err.println("Deprecated command line args, please use long form (i.e. --deconflict)");
		    argProcessingStart++;
		}
		if(loopi == 4) {
		    argNoEmitters = Integer.parseInt(argv[4]);
		    System.err.println("Deprecated command line args, please use long form (i.e. --emitters 1)");
		    argProcessingStart++;
		}
		if(loopi == 5) {
		    argNoCivilians = Integer.parseInt(argv[4]);
		    System.err.println("Deprecated command line args, please use long form (i.e. --civilians 4)");
		    argProcessingStart++;
		}
	    }
        }

	for(int loopi = argProcessingStart; loopi < argv.length; loopi++) {
	    if(argv[loopi].equalsIgnoreCase("--configuration") && ((loopi+1) < argv.length)) {
		setConfigurationFlag(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--uav") && ((loopi+1) < argv.length)) {
		argNoUAVs = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--auav") && ((loopi+1) < argv.length)) {
		argNoArmoredUAVs = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--eoiruav") && ((loopi+1) < argv.length)) {
		argNoEOIRUAVs = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--rssiuav") && ((loopi+1) < argv.length)) {
		argNoRSSIUAVs = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--ugs") && ((loopi+1) < argv.length)) {
		argNoUGSs = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--emitters") && ((loopi+1) < argv.length)) {
		argNoEmitters = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--civilians") && ((loopi+1) < argv.length)) {
		argNoCivilians = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--operators") && ((loopi+1) < argv.length)) {
		argNoOperators = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--opforinfantry") && ((loopi+1) < argv.length)) {
		argNoOpforInfantry = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--blueforinfantry") && ((loopi+1) < argv.length)) {
		argNoBlueforInfantry = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--humvees") && ((loopi+1) < argv.length)) {
		argNoHumvees = Integer.parseInt(argv[++loopi]);
	    }
	    else if(argv[loopi].equalsIgnoreCase("--ovcm")) {
		argOvcmOn = true;
	    }
	    else if(argv[loopi].equalsIgnoreCase("--deconflict")) {
		argDeconflictOn = true;
	    }
	    else {
		System.err.println("ERROR: UNKNOWN ARG argv["+loopi+"]= '" + argv[loopi]+"'");
	    }
	}

        int configureCount = 0;
        if(configureFor50by50MultiFreqDemo) configureCount++;
        if(configureFor50by50TDOADemo) configureCount++;
        if(configureFor50by50AccastDemo) configureCount++;
        if(configureForMultiFreq) configureCount++;
        if(configureFor50by50profiling) configureCount++;
        if(configureFor50by50InfoTech) configureCount++;
        if(configureFor1by1) configureCount++;
	if(configureFor5by5) configureCount++;
        if(configureForDeconflictionExperiments) configureCount++;
        if(configureFor1by1Sean) configureCount++;
        if(configureFor1by1Gascola) configureCount++;
        if(configureFor50by50and12UAVsSean) configureCount++;
        if(configureFor1by1MultiEmitterSean) configureCount++;
        if(configureForDeconfliction) configureCount++;
        if(configureForTestingSean) configureCount++;
        if(configureCount > 1) {
            Machinetta.Debugger.debug("More than one of the configureFor flags at the top of L3Create are set to true - only one may be set to true at a time.  Please check for more than one set to true, and recompile L3Create.",5,"L3Create");
	    Machinetta.Debugger.debug("according to params, argNoRSSIUAVs="+argNoRSSIUAVs+", argOvcmOn = "+argOvcmOn+", argDeconflictOn="+argDeconflictOn+", argNoEmitters= "+argNoEmitters,5,"L3Create");
            System.exit(1);
        }
	if(configureCount < 1) {
            Machinetta.Debugger.debug("None of the configureFor flags at the top of L3Create are set to true - one (and ONLY one) must be set to true.  Please choose one and set it to true, and recompile L3Create.",5,"L3Create");
	    System.exit(1);
	}
        
        String username = System.getProperty("user.name");
        createUserConfigs();
        setUserConfig(username);
        
        System.err.println("according to params, argNoRSSIUAVs="+argNoRSSIUAVs+", argOvcmOn = "+argOvcmOn+", argDeconflictOn="+argDeconflictOn+", argNoEmitters= "+argNoEmitters);
        
        if (configureFor1by1) {
            configureFor1by1(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
	} else if (configureFor5by5) {
	    configureFor5by5(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor50by50InfoTech) {
            configureFor50by50InfoTech(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureForTestingSean) {
            configureForTestingSean(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor50by50AccastDemo) {
            configureFor50by50AccastDemo(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureForMultiFreq) {
            configureForMultiFreq(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor50by50profiling) {
            configureFor50by50profiling(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor1by1Sean) {
            configureFor1by1Sean(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor1by1Gascola) {
            configureFor1by1Gascola(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if (configureFor1by1MultiEmitterSean) {
            configureFor1by1MultiEmitterSean(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if(configureFor50by50and12UAVsSean) {
            configureFor50by50and12UAVsSean(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if(configureForDeconfliction) {
            configureForDeconfliction(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if(configureFor50by50MultiFreqDemo) {
            configureFor50by50MultiFreqDemo(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if(configureFor50by50TDOADemo) {
            configureFor50by50TDOADemo(argNoRSSIUAVs, argPathPlannerTTL, argOvcmOn, argDeconflictOn, argNoEmitters, argNoCivilians);
        } else if(configureForDeconflictionExperiments) {
            
            boolean configureOvcmOffDeconflictOff = false;
            boolean configureOvcmOnDeconflictOff = false;
            boolean configureOvcmOnDeconflictOn = false;
            
            if(!argOvcmOn && !argDeconflictOn) {
                configureOvcmOffDeconflictOff = true;
            } else if(argOvcmOn && !argDeconflictOn) {
                configureOvcmOnDeconflictOff = true;
            } else if(argOvcmOn && argDeconflictOn) {
                configureOvcmOnDeconflictOn = true;
            }
            noRSSIUAVs = argNoRSSIUAVs;
            System.err.println("according to params, argNoRSSIUAVs="+argNoRSSIUAVs+", argOvcmOn = "+argOvcmOn+", argDeconflictOn="+argDeconflictOn);
            System.err.println("configureOvcmOffDeconflictOff="+configureOvcmOffDeconflictOff+", configureOvcmOnDeconflictOff="+configureOvcmOnDeconflictOff+", configureOvcmOnDeconflictOn="+configureOvcmOnDeconflictOn);
            
            if(configureOvcmOffDeconflictOff || configureOvcmOnDeconflictOff || configureOvcmOnDeconflictOn) {
                // Configuration for deconfliction
                confMap50by50();
                emitterCount = 0;
                uavMaxSpeedMetersPerSec = 41.666;
                
                confPathPlanner50by50();
                confRRT50by50();
                confBBF50by50();
                uavPlacementRandom = true;
                MASTER_DISPLAY=MASTER_DISPLAY_MASTER;
                //                SLAVE_DISPLAY=SLAVE_DISPLAY_SEAN;
                uavBinaryBayesFilterOn = false;
                noUAVs = 0;
                
                if(argNoRSSIUAVs <= 10) {
                    sanjayaUpdateRate = 50;
                } else if(argNoRSSIUAVs <= 20) {
                    sanjayaUpdateRate = 50;
                } else if(argNoRSSIUAVs <= 30) {
                    sanjayaUpdateRate = 100;
                } else if(argNoRSSIUAVs <= 40) {
                    sanjayaUpdateRate = 100;
                } else {
                    sanjayaUpdateRate = 150;
                }
                
                sanjayaAssetCollisionDetectionOn = true;
                OtherVehicleCostMapAvoid = 3000;
                OtherVehicleCostMapConflict = 2000;
                
                uavRandomEntireMapCostmapOn = true;
                uavRandomSmallMovesCostmapOn = false;
                uavRandomSmallMovesRange = 5000;
                uavRandomClustersCostmapOn = false;
                uavRandomClustersRange = 7000;
                uavRandomClustersCount = 5;
                uavRandomGuassiaAmplitudeMultiplier = 500;
                uavRandomGuassianDivisorMultiplier = 100;
                operPathDeconflictionOn = false;
                uavProxyMemoryMegabytes = 200;
                
                PathPlannerTTL = (int)(3*(Math.log(argNoRSSIUAVs))+.5)+1;
                
                if(configureOvcmOffDeconflictOff) {
                    OtherVehicleCostmapOn = false;
                    uavPathDeconflictionOn = false;
                } else if(configureOvcmOnDeconflictOff) {
                    OtherVehicleCostmapOn = true;
                    uavPathDeconflictionOn = false;
                } else if(configureOvcmOnDeconflictOn) {
                    OtherVehicleCostmapOn = true;
                    uavPathDeconflictionOn = true;
                }
            }
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
	noOpforInfantry = argNoOpforInfantry;
	noBlueforInfantry = argNoBlueforInfantry;
	noHumvees = argNoHumvees;

        new L3Create();
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
                out.writeBytes("setenv CLASSPATH "+MASTER_CLASSPATH+"\n\n");
                
                out.writeBytes("echo Synchronizing files on slaves \n");
                out.writeBytes(SYNCHRONIZATION_SCRIPT+"\n");
                
                out.writeBytes("\n");
                if(null != MASTER_DISPLAY)
                    out.writeBytes("setenv DISPLAY "+MASTER_DISPLAY+"\n");
                out.writeBytes("\n");
                out.writeBytes("mkdir -p "+OUTPUT_DIR+"\n");
                out.writeBytes("\n");
                
                out.writeBytes(JAVA+"  Machinetta.Communication.UDPSwitch >& "+OUTPUT_DIRS+"SwitchOut &\n\n");
                out.writeBytes("sleep 1\n\n");
                out.writeBytes(JAVA+"  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& "+OUTPUT_DIRS+"SimOut &\n\n");
                // out.writeBytes(JAVA+"  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt >& "+OUTPUT_DIRS+"SimOut &\n\n");
                out.writeBytes("sleep 1\n\n");
                
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("\n\necho Starting processes on " + SLAVE_MACHINES[i] +"\n");
		    out.writeBytes("# ssh " + SLAVE_MACHINES[i] + " chmod +x " + locs + SLAVE_MACHINES[i] + ".sh\n");
		    out.writeBytes("ssh " + SLAVE_MACHINES[i] + " " + locs + SLAVE_MACHINES[i] + ".sh &\n");
		}
                
                out.writeBytes("\necho Starting Operators\n");
                for (int i = 0; i < noOperators; i++) {
                    out.writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "Operator"+i+".cfg >& "+OUTPUT_DIRS + "Operator"+i+".out &\n");
                    out.writeBytes("sleep 1\n");
                }
                
                if(noTrafficControllers > 0) {
                    out.writeBytes("\necho Starting TrafficControllers\n");
                    for (int i = 0; i < noTrafficControllers; i++) {
                        out.writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "TrafficController"+i+".cfg >& "+OUTPUT_DIRS + "TrafficController"+i+".out &\n");
                        out.writeBytes("sleep 1\n");
                    }
                }
                
                out.flush();
                out.close();
                
                // The switch, the simulator and the operator all run on the master machine
                DataOutputStream [] slaveScripts = new DataOutputStream[SLAVE_MACHINES.length];
                for (int i = 0; i < slaveScripts.length; i++) {
                    slaveScripts[i] = new DataOutputStream(new FileOutputStream(locs + SLAVE_MACHINES[i] + ".sh"));
                    
                    slaveScripts[i].writeBytes("#!/bin/tcsh\n\n");
                    slaveScripts[i].writeBytes("setenv CLASSPATH "+SLAVE_CLASSPATH+"\n\n");
                    slaveScripts[i].writeBytes("\n");
                    if(null != SLAVE_DISPLAY)
                        slaveScripts[i].writeBytes("setenv DISPLAY "+SLAVE_DISPLAY+"\n");
                    slaveScripts[i].writeBytes("\n");
                    slaveScripts[i].writeBytes("mkdir -p "+OUTPUT_DIR+"\n");
                    slaveScripts[i].writeBytes("\n");
                }
                int outIndex = 0;
                
                for (int i = 0; i < noUAVs; i++) {
                    Machinetta.Debugger.debug("Adding regular uav "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    
                    slaveScripts[outIndex].writeBytes("\necho Starting UAV " + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "UAV"+i+".cfg >& "+OUTPUT_DIRS + "UAV"+i+".out &\n");
                    //                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "UAV"+i+".cfg >& "+OUTPUT_DIRS + "UAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noRSSIUAVs; i++) {
                    Machinetta.Debugger.debug("Adding RSSI uav "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting RSSIUAV " + i + " on $HOST\n");
                    String outString;
                    if(CONFIG_FOR_WIN_DAVE) {
                        outString = JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " +  "/home/dscerri/Code/RSSI-UAV"+i+".cfg >& "+OUTPUT_DIR + "/RSSI-UAV"+i+".out &\n";
                    } else {
                        outString = JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "RSSI-UAV"+i+".cfg >& "+OUTPUT_DIRS + "RSSI-UAV"+i+".out &\n";
                    }
                    Machinetta.Debugger.debug("Writing to file "+SLAVE_MACHINES[outIndex]+": \""+outString+"\"",1,this);
                    slaveScripts[outIndex].writeBytes(outString);
                    //                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "RSSI-UAV"+i+".cfg >& "+OUTPUT_DIRS + "RSSI-UAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noEOIRUAVs; i++) {
                    Machinetta.Debugger.debug("Adding EOIR uav "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting EOIRUAV " + i + " on $HOST\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "EOIR-UAV"+i+".cfg >& "+OUTPUT_DIRS + "EOIR-UAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noArmoredUAVs; i++) {
                    Machinetta.Debugger.debug("Adding Armored uav "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting SUAV " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "AUAV"+i+".cfg >& "+OUTPUT_DIRS + "AUAV"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noUGSs; i++) {
                    Machinetta.Debugger.debug("Adding UGS "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting UGS " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "UGS"+i+".cfg >& "+OUTPUT_DIRS +"UGS"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noHumvees; i++) {
                    Machinetta.Debugger.debug("Adding Humvee "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting HV " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "H"+i+".cfg >& "+OUTPUT_DIRS + "H"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noDetectors; i++) {
                    Machinetta.Debugger.debug("Adding detector "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting Detector " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "Detector"+i+".cfg >& "+OUTPUT_DIRS + "Detector"+i+".out &\n");
                    slaveScripts[outIndex].writeBytes("sleep 1\n");
                    outIndex = (++outIndex) % slaveScripts.length;
                }
                
                for (int i = 0; i < noGeolocators; i++) {
                    Machinetta.Debugger.debug("Adding geolocator "+i+" to script for slave "+SLAVE_MACHINES[outIndex],1,this);
                    slaveScripts[outIndex].writeBytes("\necho Starting Geolocator " + i + "\n");
                    slaveScripts[outIndex].writeBytes(JAVA+"  -Xmx400m Machinetta.Proxy " + locs + "Geolocator"+i+".cfg >& "+OUTPUT_DIRS + "Geolocator"+i+".out &\n");
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
            out.writeBytes("setenv CLASSPATH "+MASTER_CLASSPATH+"\n\n");
            
	    out.writeBytes("\n");
	    if(null != MASTER_DISPLAY)
		out.writeBytes("setenv DISPLAY "+MASTER_DISPLAY+"\n");
	    out.writeBytes("\n");
	    out.writeBytes("mkdir -p "+OUTPUT_DIR+"\n");
	    out.writeBytes("\n");
            out.writeBytes(JAVA+"  Machinetta.Communication.UDPSwitch >& "+OUTPUT_DIRS+"SwitchOut &\n\n");
            out.writeBytes("sleep 1\n\n");
            out.writeBytes(JAVA+"  -Xmx800m AirSim.Environment.GUI.MainFrame " + locs + "Env.txt --showgui >& "+OUTPUT_DIRS+"SimOut &\n\n");
            out.writeBytes("sleep 1\n\n");
            
            // For now, the same script runs all the agents.
	    if(noUAVs > 0) 
		out.writeBytes("\necho Starting UAVs\n");
            for (int i = 0; i < noUAVs; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "UAV"+i+".cfg >& "+OUTPUT_DIRS + "UAV"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noRSSIUAVs > 0) 
		out.writeBytes("\necho Starting RSSI UAVs\n");
	    for (int i = 0; i < noRSSIUAVs; i++) {
		out.writeBytes("\necho Starting RSSIUAV " + i + " on $HOST\n");
		out.writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "RSSI-UAV"+i+".cfg >& "+OUTPUT_DIRS + "RSSI-UAV"+i+".out &\n");
		out.writeBytes("sleep 1\n");
	    }
                
	    if(noEOIRUAVs > 0) 
		out.writeBytes("\necho Starting EOIR UAVs\n");
	    for (int i = 0; i < noEOIRUAVs; i++) {
		out.writeBytes("\necho Starting EOIRUAV " + i + " on $HOST\n");
		out.writeBytes(JAVA+"  -Xmx"+uavProxyMemoryMegabytes+"m Machinetta.Proxy " + locs + "EOIR-UAV"+i+".cfg >& "+OUTPUT_DIRS + "EOIR-UAV"+i+".out &\n");
		out.writeBytes("sleep 1\n");
	    }
           
	    if(noArmoredUAVs > 0)
		out.writeBytes("\necho Starting AUAVs\n");
            for (int i = 0; i < noArmoredUAVs; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "AUAV"+i+".cfg >& "+OUTPUT_DIRS + "AUAV"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noUGSs > 0) 
		out.writeBytes("\necho Starting UGSs\n");
            for (int i = 0; i < noUGSs; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "UGS"+i+".cfg >& "+OUTPUT_DIRS + "UGS"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noHumvees > 0) 
		out.writeBytes("\necho Starting Humvees\n");
            for (int i = 0; i < noHumvees; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "H"+i+".cfg >& "+OUTPUT_DIRS + "H"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noDetectors > 0) 
		out.writeBytes("\necho Starting Detectors\n");
            for (int i = 0; i < noDetectors; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "Detector"+i+".cfg >& "+OUTPUT_DIRS + "Detector"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noGeolocators > 0) 
		out.writeBytes("\necho Starting Geolocators\n");
            for (int i = 0; i < noGeolocators; i++) {
                out.writeBytes(JAVA+"  Machinetta.Proxy " + locs + "Geolocator"+i+".cfg >& "+OUTPUT_DIRS + "Geolocator"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noOperators > 0) 
		out.writeBytes("\necho Starting Operators\n");
            for (int i = 0; i < noOperators; i++) {
                out.writeBytes(JAVA+"  -Xmx256M Machinetta.Proxy " + locs + "Operator"+i+".cfg >& "+OUTPUT_DIRS + "Operator"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
	    if(noTrafficControllers > 0) 
		out.writeBytes("\necho Starting TrafficControllers\n");
            for (int i = 0; i < noTrafficControllers; i++) {
                out.writeBytes(JAVA+"  -Xmx128M Machinetta.Proxy " + locs + "TrafficController"+i+".cfg >& "+OUTPUT_DIRS + "TrafficController"+i+".out &\n");
                out.writeBytes("sleep 1\n");
            }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
    
    
    public void makeUptimeScript() {
        String filename = locs+"uptime.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("UPTIME=`uptime`\n");
            out.writeBytes("echo $HOSTNAME $UPTIME\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    public void makeTopScript() {
        String filename = locs+"top.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("echo $HOSTNAME\n");
            out.writeBytes("top -b -n 1 -u $USER\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    public void makeKillScript() {
        String filename = locs+"kill.sh";
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
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    public void makeAllKillScript() {
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "allkill.sh"));
            
            out.writeBytes("#!/bin/tcsh\n\n");
            
            out.writeBytes("setenv CONFIG_DIR "+loc+"\n");
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
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("echo \"Killing things on "+SLAVE_MACHINES[i]+"\"\n");
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/kill.sh\n");
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
        String filename = locs+"uptimes.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/uptime.sh\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/uptime.sh &\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    public void makeDeleteLogsScript() {
        // Make scripts for all machines
        String filename = locs+"deletelogs.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" rm -rf "+OUTPUT_DIR+"\n");
		    out.writeBytes("\n");
		}
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    public void makeTopsScript() {
        // Make scripts for all machines
        String filename = locs+"tops.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/top.sh\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/top.sh &\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    
    public void makeFetchLogsScript() {
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "fetchlogs.sh"));
            
            out.writeBytes("#!/bin/bash\n\n");
            
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("export LOGDIR=$1\n");
		    out.writeBytes("echo \"Fetching logs from "+SLAVE_MACHINES[i]+"\"\n");
		    out.writeBytes("scp -r "+SLAVE_MACHINES[i]+":$LOGDIR $LOGDIR\n");
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
            
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
            out.writeBytes(SANJAYA_UPDATE_RATE_COMMENT);
            out.writeBytes("UPDATE_RATE "+sanjayaUpdateRate+"\n\n");
            
            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS "+mapWidthMeters+"\n");
            out.writeBytes("MAP_HEIGHT_METERS "+mapHeightMeters+"\n");
            out.writeBytes("\n");
            
            out.writeBytes(USE_XPLANE_CAMERA_COMMENT);
            out.writeBytes("USE_XPLANE_CAMERA "+USE_XPLANE_CAMERA+"\n");
            out.writeBytes("USE_XPLANE_CAMERA_HOST "+USE_XPLANE_CAMERA_HOST+"\n");
            out.writeBytes("\n");
            
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM "+sanjayaOneKmGridLinesOn+"\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n");
            
            out.writeBytes("\n");
            out.writeBytes(SANJAYA_ASSET_COLLISION_COMMENT);
            out.writeBytes("ASSET_COLLISION_DETECTION_ON "+sanjayaAssetCollisionDetectionOn+"\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_RANGE_METERS "+sanjayaAssetCollisionRangeMeters+"\n");
            out.writeBytes("ASSET_COLLISION_DETECTION_MIN_Z_DIST_METERS "+sanjayaAssetCollisionMinZDistMeters+"\n");
            
            out.writeBytes("\n");
            out.writeBytes(END_CONDITION_COMMENT);
            out.writeBytes("END_CONDITION_CAPTURE_THE_FLAG "+END_CONDITION_CAPTURE_THE_FLAG+"\n");
            out.writeBytes("END_CONDITION_ALL_HMMMVS_DEAD "+END_CONDITION_ALL_HMMMVS_DEAD+"\n");
            out.writeBytes("END_CONDITION_ALL_OPFOR_DEAD "+END_CONDITION_ALL_OPFOR_DEAD+"\n");
            out.writeBytes("END_CONDITION_ALL_BLUEFOR_DEAD "+END_CONDITION_ALL_BLUEFOR_DEAD+"\n");
            out.writeBytes("END_CONDITION_ALL_EMITTERS_DETECTED "+END_CONDITION_ALL_EMITTERS_DETECTED+"\n");
            out.writeBytes("\n");
            
            
            // NOTE: If you change the emitters here you should change
            // the emitters in _createUAVCfgFile to match it.
            writeSA9Locations(out);
            
            // out.writeBytes("UGS UGS0 5000 5000 BLUEFOR\n");
            
            // out.writeBytes("HUMMER H0 4700 4925 BLUEFOR\n");
            
            // out.writeBytes("TRUCK MULTI 5 OPFOR\n");
            out.writeBytes("CTDB_BASE_NAME "+getCtdbBaseName()+"\n");
            
            out.writeBytes("ASSET_CONFIG AT_LOCATION_DISTANCE "+uavAtLocationDistance+"\n");
            out.writeBytes("ASSET_CONFIG MOVE_FINISHED_HOLD_RADIUS "+uavMoveFinishedHoldRadius+"\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_SPEED_KPH " + mps_to_kph(uavMaxSpeedMetersPerSec) + "\n");
            out.writeBytes("ASSET_CONFIG SMALL_UAV_MAX_TURN_RATE_DEG "+uavMaxTurnRateDegPerStep+"\n");
            
            out.writeBytes("ASSET_CONFIG RSSI_MAP_SCALE " + BBFGridScaleFactor + "\n");
            
            
            
            for (int i = 0; i < noUAVs; i++) {
                if(uavPlacementRandom)
                    out.writeBytes("SMALL_UAV UAV"+i+ " RANDOM "+uavRefuelThreshold+" BLUEFOR\n");
                else
                    out.writeBytes("SMALL_UAV UAV"+i+ " " + (13119 + i * 2 * PathConflictDist) + " 34805 1000 "+uavRefuelThreshold+" BLUEFOR\n");
                
                out.writeBytes("SENSOR TDOA UAV"+i+"\n");
            }
            
            for (int i = 0; i < noRSSIUAVs; i++) {
                if(uavPlacementRandom)
                    out.writeBytes("SMALL_UAV RSSI-UAV"+i+ " RANDOM "+uavRefuelThreshold+" BLUEFOR\n");
                else if(null != rssiUavLocations) 
                    out.writeBytes("SMALL_UAV RSSI-UAV"+i+ " " + rssiUavLocations[i] +"  "+uavRefuelThreshold+" BLUEFOR\n");
		else 
                    out.writeBytes("SMALL_UAV RSSI-UAV"+i+ " " + (119 + i * 2 * PathConflictDist) + " 48 "+(i*100)+"  "+uavRefuelThreshold+" BLUEFOR\n");
                out.writeBytes("SENSOR RSSI RSSI-UAV"+i+"\n");
            }
            
            for (int i = 0; i < noEOIRUAVs; i++) {
                if(uavPlacementRandom)
                    out.writeBytes("SMALL_UAV EOIR-UAV"+i+ " RANDOM "+uavRefuelThreshold+" BLUEFOR\n");
                else
                    out.writeBytes("SMALL_UAV EOIR-UAV"+i+ " " + (151 + i * 2 * PathConflictDist) + " 34 100  "+uavRefuelThreshold+" BLUEFOR\n");
                out.writeBytes("SENSOR EOIR EOIR-UAV"+i+"\n");
            }
            
            for (int i = 0; i < noArmoredUAVs; i++) {
                if(uavPlacementRandom)
                    out.writeBytes("SMALL_UAV AUAV"+i+ " RANDOM "+uavRefuelThreshold+" BLUEFOR\n");
                else
                    out.writeBytes("SMALL_UAV AUAV"+i+ " " + (16119 + i * 2 * PathConflictDist) + " 38805 1000  "+uavRefuelThreshold+" BLUEFOR\n");
                out.writeBytes("SENSOR SAR AUAV"+i+"\n");
            }
            
            for (int i = 0; i < noHumvees; i++) {
                if(uavPlacementRandom)
                    out.writeBytes("HMMWV H"+i+ " " + (16119 + i * 500) + " 37805 0 BLUEFOR\n");
            }

	    int infX = 25000;
	    int infY = 25000;
            for (int i = 0; i < noOpforInfantry; i++) {
		out.writeBytes("INFANTRY DI-OP"+i+" "+infX+" "+infY+" 0 OPFOR\n");
		infX += 5;
            }

	    infX = 25000;
	    infY = 25100;
            for (int i = 0; i < noBlueforInfantry; i++) {
		out.writeBytes("INFANTRY DI-BLUE"+i+" "+infX+" "+infY+" 0  BLUEFOR\n");
		infX += 5;
            }
	    
	    if(null != EnvFileAdditionalLines) {
		Machinetta.Debugger.debug(1,"Have "+EnvFileAdditionalLines.length+" additional lines for Env.txt, writing.");
		for(int loopi = 0; loopi <EnvFileAdditionalLines.length; loopi++) {
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
        if(noAssociates > maxPossibleAssociates) {
            Machinetta.Debugger.debug("Maximum possible number of associates is "+maxPossibleAssociates+", noAssociates is too large ("+noAssociates+"), lowering it to the max possible number.",1,this);
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
        Machinetta.Debugger.debug("makeUAVBeliefs "+name,1,this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVTDOACaps);
        
        ProxyState state = _makeUAVBeliefs(name, self);
        state.addBelief(self);
        
        // Tell them about the detectors
        for (int i = 0; i < noDetectors; i++) {
            RAPBelief db = new RAPBelief(new NamedProxyID("Detector"+i), false, detectorCaps);
            state.addBelief(db);
        }
        
        // Tell them about the geolocators
        for (int i = 0; i < noGeolocators; i++) {
            RAPBelief rb = new RAPBelief(new NamedProxyID("Geolocator"+i), false, geoLocateCaps);
            state.addBelief(rb);
        }
        
        // Tell them the plan for getting sensor readings checked
        state.addBelief(makeCheckSensorReadingTPT());
        
        // Give them an initial scan role
        BasicRole scanRole = new BasicRole(TaskType.scan);
        scanRole.params = new Hashtable();
        // scanRole.params.put("Area", new Rectangle(10000*uavNo, 10000*uavNo, 20000, 20000));
        scanRole.params.put("Area", new Rectangle(0,0,50000,50000));
        scanRole.setResponsible(self);
        scanRole.constrainedWait = false;
        state.addBelief(scanRole);
        
        writeBeliefs(locs + name + ".blf", state);
    }
    
    public void makeEOIRUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeEOIRUAVBeliefs "+name,1,this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVEOIRCaps);
        
        ProxyState state = _makeUAVBeliefs(name, self);
        
        state.addBelief(self);
        
        writeBeliefs(locs + name + ".blf", state);
    }
    
    public void makeRSSIUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs "+name,1,this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, UAVRSSICaps);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs ProxyState "+name,1,this);
        
        ProxyState state = _makeUAVBeliefs(name, self);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs self "+name,1,this);
        
        state.addBelief(self);
        Machinetta.Debugger.debug("makeRSSIUAVBeliefs writing "+name,1,this);
        
        writeBeliefs(locs + name + ".blf", state);
        Machinetta.Debugger.debug("Done makeRSSIUAVBeliefs "+name,1,this);
    }
    
    /**
     *
     * Armored UAV initially knows about itself and the team.
     */
    public void makeAUAVBeliefs(String name, int uavNo) {
        Machinetta.Debugger.debug("makeAUAVBeliefs "+name,1,this);
        ProxyState state = _makeBeliefs();
        
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, AUAVCaps);
        state.addBelief(self);
        
        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }
        
        // Tell them about the detectors
        for (int i = 0; i < noDetectors; i++) {
            RAPBelief db = new RAPBelief(new NamedProxyID("Detector"+i), false, detectorCaps);
            state.addBelief(db);
        }
        
        // Tell them about the emitters
        for (int i = 0; i < noGeolocators; i++) {
            RAPBelief rb = new RAPBelief(new NamedProxyID("Geolocator"+i), false, geoLocateCaps);
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
        Machinetta.Debugger.debug("makeUGSBeliefs "+name,1,this);
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

        Machinetta.Debugger.debug("makeHMMWVBeliefs ProxyState "+name,1,this);
	// NOTE: _makeBeliefs() removes all other beliefs.  So don't
	// add anything before this.
        ProxyState state = _makeBeliefs();
        
        Machinetta.Debugger.debug("makeHMMWVBeliefs "+name,1,this);
        RAPBelief self = new RAPBelief(new NamedProxyID(name), true, HVCaps);

        if (useAssociates) {
            state.addBelief(makeAssociates(self.getProxyID()));
        }

        Machinetta.Debugger.debug("makeHMMWVBeliefs self "+name,1,this);
        state.addBelief(self);
        
        Machinetta.Debugger.debug("makeHMMWVBeliefs writing "+name,1,this);
        writeBeliefs(locs + name + ".blf", state);
        Machinetta.Debugger.debug("Done makeHMMWVBeliefs "+name,1,this);
    }
    
    /**
     *
     * Detector initially knows about itself and the team.
     *
     * It also knows the Geo-locate team plan template.
     */
    public void makeDetectorBeliefs(String name) {
        Machinetta.Debugger.debug("makeDetectorBeliefs "+name,1,this);
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
        Machinetta.Debugger.debug("makeGeolocatorBeliefs "+name,1,this);
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
        Machinetta.Debugger.debug("makeOperatorBeliefs "+name,1,this);
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
        Machinetta.Debugger.debug("makeTrafficControllerBeliefs "+name,1,this);
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
            BeliefID [] beliefs = state.getAllBeliefs();
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
        Hashtable <String, Object> params = new Hashtable <String, Object>();
        Vector  preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();
        
        //----------------------------------------------------------
        // No params for now
        
        //----------------------------------------------------------
        // Precondition is that there is an un"located" GeoLocateRequest
        Vector <String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("located");
        
        Vector <String> outKeys = new Vector<String>();
        outKeys.add("id");
        
        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest false",    // String to be matched
                inKeys,
                outKeys,
                "request");  // This is the key to the belief in the role's param string
        Vector first = new Vector();
        first.add(cond);
        preconditions.add(first);
        
        //----------------------------------------------------------
        // Post condition is that location is known
        Vector <String> inKeysPost = new Vector<String>();
        inKeysPost.add("class");
        inKeysPost.add("located");
        
        Vector <String> outKeysPost = new Vector<String>();
        
        MatchCondition condPost = new MatchCondition(
                "AirSim.Machinetta.Beliefs.GeoLocateRequest true",    // String to be matched
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
            basic.params.put("Label", "Role"+no);
            basic.params.put("ScanDataLabel", "TMAGLData"+no);
            
            basic.infoSharing = new Vector<DirectedInformationRequirement>();
            try {
                // Sets up requirement to send sensor data to geolocator
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.TMAScanResult"), computeRole.getID(), new BeliefNameID("TMAGLData"+no)));
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
        Hashtable <String, Object> params = new Hashtable <String, Object>();
        Vector  preconditions = new Vector(),
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
        Vector <String> inKeys = new Vector<String>();
        inKeys.add("class");
        inKeys.add("isDiscriminated");
        inKeys.add("isGeolocateData");
        
        Vector <String> outKeys = new Vector<String>();
        outKeys.add("id");
        
        MatchCondition cond = new MatchCondition(
                "AirSim.Machinetta.Beliefs.TMAScanResult false false",    // String to be matched
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
    
    private void writeEmitterLocations(DataOutputStream out) throws java.io.IOException {
        out.writeBytes("EMITTER_COUNT "+emitterCount+"\n");
        for(int loopi = 0; loopi < emitterCount; loopi++)
            if(randomEmitterLocations)
                out.writeBytes("EMITTER_"+loopi+" RANDOM\n");
            else
                out.writeBytes("EMITTER_"+loopi+" "+emitterLocations[loopi]+"\n");
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
        for(int loopi = 0; loopi < emitterCount; loopi++) {
            if(!emitterChannels) {
                if(randomEmitterLocations)
                    out.writeBytes("SA9 SA9-"+(loopi+1)+" RANDOM OPFOR\n");
                else
                    out.writeBytes("SA9 SA9-"+(loopi+1)+" "+emitterLocations[loopi]+" OPFOR\n");
            } else {
                if(randomEmitterLocations)
                    out.writeBytes("SA9 SA9-"+(loopi+1)+" RANDOM CHANNEL"+channel+" OPFOR\n");
                else
                    out.writeBytes("SA9 SA9-"+(loopi+1)+" "+emitterLocations[loopi]+" CHANNEL"+channel+" OPFOR\n");
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
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVAA\n");
            // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Machinetta.UAVRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DYNAMIC_TEAMING true\n");
            if(CONFIG_FOR_WIN_DAVE) {
                out.writeBytes("DEFAULT_BELIEFS_FILE /home/dscerri/Code/" + name + ".blf\n");
            } else {
                out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            }
            
            out.writeBytes("TERRAIN_COSTMAP_LOCATION "+TERRAIN_COSTMAP_LOCATION+"\n");
            out.writeBytes("TIME_BETWEEN_FILTER_PANEL_UPDATES_MS 100\n");
            out.writeBytes("# New RRT params from Paul\n");
            out.writeBytes("EXPANSION_COST_FACTOR 8.0\n");
            out.writeBytes("EXPANSIONS 5000\n");
            out.writeBytes("NUM_EXPANSIONS 3\n");
            out.writeBytes("MOMENTUM_REWARD 1000.0\n");
            out.writeBytes("MIN_PATH_LENGTH "+RRTMinPathLength+"\n");
	    out.writeBytes("# RRTCONFIG params\n");
	    out.writeBytes("RRTCONFIG_NO_EXPANSIONS "+RRTCONFIG_NO_EXPANSIONS+"\n");
	    out.writeBytes("RRTCONFIG_BRANCHES_PER_EXPANSION "+RRTCONFIG_BRANCHES_PER_EXPANSION+"\n");
	    out.writeBytes("RRTCONFIG_MAX_THETA_CHANGE "+RRTCONFIG_MAX_THETA_CHANGE+"\n");
	    out.writeBytes("RRTCONFIG_MAX_PSI_CHANGE "+RRTCONFIG_MAX_PSI_CHANGE+"\n");
	    out.writeBytes("RRTCONFIG_MAX_BRANCH_LENGTH "+RRTCONFIG_MAX_BRANCH_LENGTH+"\n");
	    out.writeBytes("RRTCONFIG_MIN_BRANCH_LENGTH "+RRTCONFIG_MIN_BRANCH_LENGTH+"\n");
	    out.writeBytes("RRTCONFIG_RRT_BRANCH_RANGE_X_METERS "+RRTCONFIG_RRT_BRANCH_RANGE_X_METERS+"\n");
	    out.writeBytes("RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS "+RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS+"\n");
	    out.writeBytes("\n");

            
            // NOTE: If you change the emitters here you should change
            // the SA9s in makeEnvFile to match.
            if(ADD_EMITTER_LOCS_TO_UAV_CFG) {
                writeEmitterLocations(out);
            }
            
            out.writeBytes("\n");
            out.writeBytes("MAP_WIDTH_METERS "+mapWidthMeters+"\n");
            out.writeBytes("MAP_HEIGHT_METERS "+mapHeightMeters+"\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT "+mapLowerLeftLat+"\n");
            out.writeBytes("MAP_LOWER_LEFT_LON "+mapLowerLeftLon+"\n");
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
            out.writeBytes("UAV_MAX_TURN_RATE_DEG "+uavMaxTurnRateDegPerStep+"\n");
            out.writeBytes("\n");
            out.writeBytes("# The maximum amount that a vehicle may change its z-coordinate in one RRT branch\n");
            out.writeBytes("UAV_MAX_Z_CHANGE "+RRTMaxZChange+"\n");
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
            out.writeBytes("OTHER_VEHICLE_COSTMAP_ON "+OtherVehicleCostmapOn+"\n");
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
            if(noOperators > 0)
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON true\n");
            else
                out.writeBytes("SIM_OPERATOR_DIRECTED_INFO_REQ_ON false\n");
            if(noTrafficControllers > 0)
                out.writeBytes("SIM_TRAFFIC_CONTROLLER_DIRECTED_INFO_REQ_ON true\n");
            else
                out.writeBytes("SIM_TRAFFIC_CONTROLLER_DIRECTED_INFO_REQ_ON false\n");
            if(UAV_DYNAMIC_FLY_ZONES)
                out.writeBytes("DYNAMIC_FLY_ZONES true\n");
            else
                out.writeBytes("DYNAMIC_FLY_ZONES false\n");
            
            out.writeBytes("ENTROPY_PANEL_ON "+uavEntropyPanelOn+"\n");
            out.writeBytes("ENTROPY_PANEL_X 740\n");
            out.writeBytes("ENTROPY_PANEL_Y 20\n");
            out.writeBytes("RRTPATH_PANEL_ON "+uavRRTPathPanelOn+"\n");
            out.writeBytes("RRTPATH_PANEL_X 0\n");
            out.writeBytes("RRTPATH_PANEL_Y 540\n");
            out.writeBytes("RRTTREE_PANEL_ON "+uavRRTTreePanelOn+"\n");
            out.writeBytes("RRTTREE_PANEL_X 780\n");
            out.writeBytes("RRTTREE_PANEL_Y 540\n");
            
            return out;
            
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
        
        return null;
    }
    
    public void createUAVCfgFile(String name) {
        Machinetta.Debugger.debug("createUAVCfgFile "+name,1,this);
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
        Machinetta.Debugger.debug("createRSSIUAVCfgFile "+name,1,this);
        DataOutputStream out = _createUAVCfgFile(name);
        
        try {
            out.writeBytes("BAYES_FILTER_PANEL_ON "+uavBinaryBayesFilterPanelOn+"\n");
            out.writeBytes("BAYES_FILTER_PANEL_X "+bayesPanelX+"\n");
            out.writeBytes("BAYES_FILTER_PANEL_Y "+bayesPanelY+"\n");
            bayesPanelX += 780;
            if(bayesPanelX >= 1200) {
                bayesPanelY += 524;
                bayesPanelX = 0;
            }
            
            out.writeBytes("\n");
            out.writeBytes(COSTMAPS_COMMENT);
            out.writeBytes("BINARY_BAYES_FILTER_ON "+uavBinaryBayesFilterOn+"\n");
            out.writeBytes("RANDOM_ENTIRE_MAP_COSTMAP_ON "+uavRandomEntireMapCostmapOn+"\n");
            out.writeBytes("RANDOM_SMALL_MOVES_COSTMAP_ON "+uavRandomSmallMovesCostmapOn+"\n");
            out.writeBytes("RANDOM_SMALL_MOVES_RANGE "+uavRandomSmallMovesRange+"\n");
            out.writeBytes("RANDOM_CLUSTERS_COSTMAP_ON "+uavRandomClustersCostmapOn+"\n");
            out.writeBytes("RANDOM_CLUSTERS_RANGE "+uavRandomClustersRange+"\n");
            out.writeBytes("RANDOM_CLUSTERS_COUNT "+uavRandomClustersCount+"\n");
            out.writeBytes(COSTMAPS_RANDOM_GUASSIANS_COMMENT);
            out.writeBytes("RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER "+uavRandomGuassiaAmplitudeMultiplier+"\n");
            out.writeBytes("RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER "+uavRandomGuassianDivisorMultiplier+"\n");
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
            out.writeBytes("BBF_RANDOM_SHARING_ON "+bbfRandomSharingOn+"\n");
            out.writeBytes("BBF_RANDOM_SHARING_PROB "+bbfRandomSharingProb+"\n");
            out.writeBytes("BBF_RANDOM_SHARING_TTL "+bbfRandomSharingTtl+"\n");
            out.writeBytes("BBF_KLD_SHARING_ON "+bbfKLDSharingOn+"\n");
            out.writeBytes("\n");
            out.writeBytes("# Should we track remotely sensed readings in a separate bayesian\n");
            out.writeBytes("# filter, and use that to decide sharing?  (If this is true,\n");
            out.writeBytes("# BBF_RANDOM_SHARING_ON and BBF_KLD_SHARING_ON should be false.)\n");
            out.writeBytes("BBF_REMOTE_KLD_SHARING_ON "+bbfRemoteKLDSharingOn+"\n");
            out.writeBytes("\n");
            out.writeBytes("# The alpha that is used to calculate expected signal strength in\n");
            out.writeBytes("# BinaryBayesFitler.\n");
            out.writeBytes("BBF_RSSI_ALPHA 10000.0\n");
            out.writeBytes("# These are parameters for the emitter signal computations \n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_COEFFICIENT 239400\n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_INCREMENT -70\n");
            out.writeBytes("EMITTER_NOISE_MODEL_STD  1.0\n");
            
            out.writeBytes("\n");
	    if(null != UAVCfgAdditionalLines) {
		Machinetta.Debugger.debug(1,"Have "+UAVCfgAdditionalLines.length+" additional lines for UAV cfg, writing.");
		for(int loopi = 0; loopi <UAVCfgAdditionalLines.length; loopi++) {
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
        Machinetta.Debugger.debug("createEOIRUAVCfgFile "+name,1,this);
        DataOutputStream out = _createUAVCfgFile(name);
        
        
        try {
            out.writeBytes("EO_DISTANCE_TOLERANCE "+uavEODistanceTolerance+"\n");
            
            out.flush();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void createHVCfgFile(String name) {
        Machinetta.Debugger.debug("createHVCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
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
        Machinetta.Debugger.debug("createUGSCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
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
        Machinetta.Debugger.debug("createDetectorCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
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
        Machinetta.Debugger.debug("createGeolocatorCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
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
        Machinetta.Debugger.debug("createOperatorCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES true\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM "+operOneKmGridLinesOn+"\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n\n");
            
            out.writeBytes("DYNAMIC_TEAMING true\n");
            
            out.writeBytes("\n");
            out.writeBytes("BAYES_FILTER_PANEL_ON "+operBinaryBayesFilterPanelOn+"\n");
            out.writeBytes("BAYES_FILTER_PANEL_X 0\n");
            out.writeBytes("BAYES_FILTER_PANEL_Y 20\n");
            out.writeBytes("\n");
            if(OPERATOR_CLUSTERING_ON)
                out.writeBytes("CLUSTERING_ON true\n");
            else
                out.writeBytes("CLUSTERING_ON false\n");
            out.writeBytes("\n");
            
            out.writeBytes("MAP_WIDTH_METERS "+mapWidthMeters+"\n");
            out.writeBytes("MAP_HEIGHT_METERS "+mapHeightMeters+"\n");
            out.writeBytes("\n");
            out.writeBytes("MAP_LOWER_LEFT_LAT "+mapLowerLeftLat+"\n");
            out.writeBytes("MAP_LOWER_LEFT_LON "+mapLowerLeftLon+"\n");
            
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Commander.AA\n");
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
	    // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.SimOperatorRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + locs + name + ".blf\n");
            //            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.writeBytes("CTDB_BASE_NAME "+getCtdbBaseName()+"\n");
            
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
            out.writeBytes("BINARY_BAYES_FILTER_ON "+operBinaryBayesFilterOn+"\n");
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
            out.writeBytes("BBF_RANDOM_SHARING_ON "+bbfRandomSharingOn+"\n");
            out.writeBytes("BBF_RANDOM_SHARING_PROB "+bbfRandomSharingProb+"\n");
            out.writeBytes("BBF_RANDOM_SHARING_TTL "+bbfRandomSharingTtl+"\n");
            out.writeBytes("BBF_KLD_SHARING_ON "+bbfKLDSharingOn+"\n");
            out.writeBytes("\n");
            out.writeBytes("# Should we track remotely sensed readings in a separate bayesian\n");
            out.writeBytes("# filter, and use that to decide sharing?  (If this is true,\n");
            out.writeBytes("# BBF_RANDOM_SHARING_ON and BBF_KLD_SHARING_ON should be false.)\n");
            out.writeBytes("BBF_REMOTE_KLD_SHARING_ON "+bbfRemoteKLDSharingOn+"\n");
            out.writeBytes("\n");
            out.writeBytes("# The alpha that is used to calculate expected signal strength in\n");
            out.writeBytes("# BinaryBayesFitler.\n");
            out.writeBytes("BBF_RSSI_ALPHA 10000.0\n");
            out.writeBytes("# These are parameters for the emitter signal computations \n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_COEFFICIENT 239400\n");
            out.writeBytes("EMITTER_SIGNAL_STRENGTH_INCREMENT -70\n");
            out.writeBytes("EMITTER_NOISE_MODEL_STD  1.0\n");
            out.writeBytes("\n");
            
            if(ADD_EMITTER_LOCS_TO_UAV_CFG) {
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
     * For creating config files for TrafficController
     */
    public void createTrafficControllerCfgFile(String name) {
        Machinetta.Debugger.debug("createTrafficControllerCfgFile "+name,1,this);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + name + ".cfg"));
            out.writeBytes("GUI_VIEWPORT_X -100\n");
            out.writeBytes("GUI_VIEWPORT_Y -100\n");
            out.writeBytes("GUI_VIEWPORT_WIDTH " + (mapWidthMeters + 200) + "\n");
            out.writeBytes("GUI_VIEWPORT_HEIGHT " + (mapHeightMeters + 200) + "\n");
            out.writeBytes("GUI_SOIL_TYPES false\n");
            out.writeBytes("GUI_SHOW_TRACES false\n");
            out.writeBytes("GUI_GRID_LINES_ONE_KM "+operOneKmGridLinesOn+"\n");
            out.writeBytes("GUI_SHOW_MAP_OBJECT_NAMES true\n");
            out.writeBytes("GUI_CONTOUR_MULTIPLES 0\n\n");
            
            out.writeBytes("DYNAMIC_TEAMING true\n");
            
            out.writeBytes("\n");
            
            out.writeBytes("MAP_WIDTH_METERS "+mapWidthMeters+"\n");
            out.writeBytes("MAP_HEIGHT_METERS "+mapHeightMeters+"\n");
            
            out.writeBytes("AA_IMPLEMENTATION_TYPE AirSim.Commander.AA\n");
            out.writeBytes("UDP_SWITCH_IP_STRING " + MASTER_MACHINE +"\n");
	    // Eventually want to change this to SCALAR_BINARY
            out.writeBytes("ROLE_ALLOCATION_ALGORITHM BINARY_BINARY\n");
            out.writeBytes("COMMS_IMPLEMENTATION_TYPE Machinetta.Communication.UDPComms\n");
            out.writeBytes("RAP_INTERFACE_IMPLEMENTATION_TYPE AirSim.Commander.DynaZonesRI\n");
            out.writeBytes("COORD_IMPLEMENTATION_TYPE Machinetta.Coordination.MACoordination\n");
            out.writeBytes("BELIEF_CLASS_FILE_LOCATIONS AirSim.Machinetta.\n");
            out.writeBytes("DEBUG_LEVEL 1\n");
            out.writeBytes("DEFAULT_BELIEFS_FILE " + name + ".blf\n");
            out.writeBytes("CTDB_BASE_NAME "+getCtdbBaseName()+"\n");
            
            out.writeBytes("\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write config file: " + e);
        }
    }
}
