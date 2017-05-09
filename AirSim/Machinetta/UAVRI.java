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
 * RAPInterface.java
 *
 * Created on June 17, 2005, 9:51 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.GA.*;
import AirSim.Environment.Assets.Sensors.DirRFSensorReading;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Assets.Sensors.RSSISensorReading;
import AirSim.Machinetta.Beliefs.DirectionalRFReading;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.RSSIReading;
import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.CostMaps.BinaryBayesFilterCostMap;
import AirSim.Machinetta.CostMaps.BinaryBayesFilter;
import AirSim.Machinetta.CostMaps.BBFTabPanel;
import AirSim.Machinetta.CostMaps.BeliefCostMap;
import AirSim.Machinetta.CostMaps.DirectionalRFFilter;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap3D;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.RandomGaussiansCostMap;
import AirSim.Machinetta.Messages.EOIRSensorReadingAP;
import AirSim.Machinetta.Messages.GeoLocateDataAP;
import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.SearchSensorReadingAP;
import AirSim.Machinetta.SimTime;
import AirSim.SARSensorReading;
import Machinetta.ConfigReader;
import Machinetta.Communication.*;
import Machinetta.Coordination.MAC.BeliefShareRequirement;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.OutputMessages.RoleCancelMessage;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.Debugger;
import Gui.LatLonUtil;

import java.util.StringTokenizer;
import java.net.*;
import java.awt.Rectangle;
import java.util.Random;
import javax.swing.JFrame;
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
import java.awt.geom.Point2D;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

/**
 *
 * @author pscerri
 */
public class UAVRI extends Machinetta.RAPInterface.RAPInterfaceImplementation implements PonderInterface {
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------

    public static int MAP_WIDTH_METERS = 50000;
    public static int MAP_HEIGHT_METERS = 50000;

    // Used in AirSim/Machinetta/CostMaps/MixGaussiansCostMap as the
    // default divisor for Gaussian costmaps, i.e. when calculating a
    // gaussian's contribution to cost, we add;
    // gaussian.amplitude/(dist_from_point_squared/(divisor*divisor)).
    // to the running total.  The size of the map may affect what this
    // should be set too.
    public static int MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR = 500;

    // Speed in meters/second of the UAV - used for path planning so
    // we can known times.  Can get from SmallUAV constructor that
    // calls setSpeed.  (41.6666 is 150kph, 4.1666 is 15kph)
    public static double UAV_SPEED_METERS_PER_SEC= 4.1666;;
    public static double UAV_MAX_TURN_RATE_DEG = 3;

    // The maximum amount that a vehicle may change its z-coordinate in one RRT branch
    public static double UAV_MAX_Z_CHANGE = 1.0;

    // Used in RRTPlanner to control generation of path
    public static double RRT_PREFERRED_PATH_METERS = 500.0;

    // Used in RRTPlanner as hard limit length of paths returned.
    public static double RRT_MAX_PATH_METERS = 1000.0;

    // Used in RRTPlanner to control distance of each branch of the RRT tree
    public static double RRT_BRANCH_RANGE_X_METERS = 50.0;
    public static double RRT_BRANCH_RANGE_Y_METERS = 50.0;

    // Minimum distance inside of which a RAP is considered to be 'at'
    // the waypoint.  If this is set too small, then you see the UAVs
    // 'dance' because they haven't received the next waypoint before
    // they've passed the current one.  This was defaulted to 200m on
    // the 50Kx50K map.
    public static double PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS = 200.0;

    //Minimum Path length for path planner
    public static double PATHPLANNER_MIN_PATH_LENGTH = 200.0;
    
    // When a uav is less than this distance from the end of its path,
    // start planning the next path.
    public static double PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH = 1000.0;

    // Planned path TTL - when the UAV generates a planned path, this
    // is the TTL associated with the information sent out - generally
    // this should be set to about log(teamsize), more or less.
    public static int PATHPLANNER_PLANNED_PATH_TTL = 1;

    // Apply gradient descent path smoothing to paths after they are
    // planned.
    public static boolean PATHPLANNER_SMOOTH_GRAD_DESC_ON = false;
    
    // For use with roles that have a target point - take the path
    // returned by RRTPlanner and redirect it so its closest point
    // leads directly to the target.  This is a bit of a hack.
    public static boolean PATHPLANNER_REDIRECT_PATH_TO_TARGET = false;

    // making it 5000 to encourage some conflicts actually happening,
    // so we can detect them.
    public static double PATHDB_PATH_CONFLICT_MIN_DIST_METERS = 1000;
    public static double PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS = 100;

    // This turns on path deconfliction, i.e. the creation of a PathDB
    // instance.
    public static boolean PATH_DECONFLICTION_ON = false;

    // Used in AA, for attack capability calculation - if distance (in
    // meters) from target is > AA_MAX_DIST_CAP_METERS, then capability to
    // fill role is 0.
    public static int AA_MAX_DIST_CAP_METERS = 10000;

    // The size of the 'cost' square placed around SA9's that are
    // detected, in order to avoid being shot down.
    public static int SA9_COST_SQUARE_METERS = 10000;

    // Controls the range that each RSSI sensor reading has, in the
    // BinaryBayesFilter, i.e. how far away from the reading location do we
    // update our binary bayes probabilities.
    //    public static int BBF_SENSOR_RANGE_METERS = 5000;
    public static int BBF_SENSOR_MAX_RANGE_METERS = 200*50;
    public static int BBF_SENSOR_MIN_RANGE_METERS = 100*50;

    // the scaling factor to go from map coords to indices into the
    // bayes filter probability array - and also to figure out what
    // size array to use, based on map size.
    public static double BBF_GRID_SCALE_FACTOR = 100.0;

    // The prior value for the occupancyMap array in the binary bayes
    // filter.  
    public static double BBF_UNIFORM_PRIOR = 0.001;

    // These control the manner in which the
    // BinaryBayesianFilterCostMap decides which RSSI readings to
    // share with its teammates - it either uses random sharing
    // (i.e. share some random percentage to the time) or it uses
    // KLDivergence to set the TTL on all (local or nonlocal) RSSI
    // readings it gets, i.e. it repropagates RSSI Readings from other
    // UAVs as well as tis own.
    public static boolean BBF_RANDOM_SHARING_ON = false;
    public static double BBF_RANDOM_SHARING_PROB = .5;
    public static int BBF_RANDOM_SHARING_TTL = 5;
    public static boolean BBF_KLD_SHARING_ON = false;

    public static double BBF_RSSI_ALPHA = 10000.0;
    
    public static double BBF_DIFFUSE_DELTA = 0.001;
    public static long BBF_DIFFUSE_INTERVAL_MS = 100;
    
    // Should we track remotely sensed readings in a separate bayesian
    // filter, and use that to decide sharing?  (If this is true,
    // BBF_RANDOM_SHARING_ON and BBF_KLD_SHARING_ON should be false.)
    public static boolean BBF_REMOTE_KLD_SHARING_ON = false;

    // These are parameters for the emitter signal computations 
    public static double EMITTER_SIGNAL_STRENGTH_COEFFICIENT = 239400;
    public static double EMITTER_SIGNAL_STRENGTH_INCREMENT = -70; 
    public static double EMITTER_NOISE_MODEL_STD =  1.0; 

    // if this is true, then pathdb isn't going to
    // have much work to do... so leave it false when testing pathdb.
    public static boolean OTHER_VEHICLE_COSTMAP_ON = false;

    // The range to other vehicle paths for the OtherVehicleCostMap.  Costs
    // are scaled from 0.0 or less (at or beyond range) up to 1.0, for a
    // point being near another vehicles path, and then scaled up by 100.0.
    public static double OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS = 5000;

    // This is kind of an override value - any closer than this and we
    // return a very high cost, to sorta say 'you're going to collide
    // and die if you get this close'
    public static double OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS = 1000;
    public static double OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS = 100;
    
    public static boolean BINARY_BAYES_FILTER_ON = false;
    public static boolean BINARY_BAYES_FILTER_BELIEF_COSTMAP_ON = false;
    public static boolean RANDOM_ENTIRE_MAP_COSTMAP_ON = false;
    public static boolean RANDOM_SMALL_MOVES_COSTMAP_ON = false;
    public static double RANDOM_SMALL_MOVES_RANGE;
    public static boolean RANDOM_CLUSTERS_COSTMAP_ON = false;
    public static double RANDOM_CLUSTERS_RANGE;
    public static int RANDOM_CLUSTERS_COUNT;
    public static double RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER;
    public static double RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER;

    // @TODO This assumes a 50mk x 50km map.
    public static boolean CONSTRAIN_MAP_TO_TEN_KM_ON = false;

    // If nonzero, adds a MixGaussian costmap with this many randomly
    // placed gaussians.
    public static int RANDOM_GAUSSIAN_COSTMAP = 0;

    /**
     * The distance a UAV can be from proscribed point to take EO image.
     */
    public static int EO_DISTANCE_TOLERANCE = 100;
    
    // If true, add DirectedInformationRequirement to send
    // UAVLocation, ImageData, and PlannedPath to Operator0
    public static boolean SIM_OPERATOR_DIRECTED_INFO_REQ_ON = true;

    // If true, add BeliefShareRequirement to share VehicleBeliefs
    // randomly.
    public static boolean VEHICLE_BELIEF_SHARE_REQ_ON = false;

    // Dynamic fly/no fly zones.
    public static boolean DYNAMIC_FLY_ZONES = false;

    // debug/demo panels for bbf filter
    public static boolean BAYES_FILTER_PANEL_ON = false;
    public static int BAYES_FILTER_PANEL_X = 0;
    public static int BAYES_FILTER_PANEL_Y = 0;

    public static boolean ENTROPY_PANEL_ON = false;
    public static int ENTROPY_PANEL_X = 0;
    public static int ENTROPY_PANEL_Y = 0;

    public static String VIRTUAL_COCKPIT_IP_ADDRESS = null;

    public static double MAP_LOWER_LEFT_LAT = -1000;
    public static double MAP_LOWER_LEFT_LON = -1000;
    public static String FLY_ZONE_POLY = null;

    public static int PROCERUS_UAV_ID = NavData.INVALID_UAV_ADDRESS;

    public static boolean USE_VIRTUAL_COCKPIT = false;

    public static String VC_BRIDGE_SERVER_IP_ADDRESS = null;
    public static int VC_BRIDGE_SERVER_PORT = -1;

    public static int RRTCONFIG_NO_EXPANSIONS = 2000;
    public static int RRTCONFIG_BRANCHES_PER_EXPANSION = 3;
    public static double RRTCONFIG_MAX_THETA_CHANGE = 3;
    public static double RRTCONFIG_MAX_PSI_CHANGE = 1;
    public static double RRTCONFIG_MAX_BRANCH_LENGTH = 25.0;
    public static double RRTCONFIG_MIN_BRANCH_LENGTH = -25.0;
    public static int RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = 50;
    public static int RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = 50;

    // ----------------------------------------------------------------------
    // END PARAMETERS LOADED FROM CONFIG FILE
    // ----------------------------------------------------------------------
    private Point[] flyZonePointAry = null;
    

    private Machinetta.State.BeliefType.ProxyID proxyID = null;
    private String proxyIDString;
    
    private SmallUAVModel uavModel = null;
    private MiniWorldState miniWorldState = null;
    private Autopilot autopilot = null;
    private PathPlanner planner = null;
    private UAVLocation location = null;
    
    String currentGLID = null;
    BeliefID currentGLBID = null;
    
    private JFrame bbfBeliefFrame = null;
    private JFrame bbfEntropyFrame = null;
    private BBFTabPanel bbfBeliefTabPanel = null;
    private BBFTabPanel bbfEntropyTabPanel = null;

    private BinaryBayesFilterCostMap bbfCM = null;
    private BeliefCostMap bbfBeliefCostMap = null;
    private SimpleStaticCostMap edgeCostMap = null;
    private MixGaussiansCostMap gaussianCM = null;
    private MixGaussiansCostMap staticRandomGaussianCM = null;
    private RandomGaussiansCostMap randomCM = null;
    
    private PathDB pathDB = null;

    private Random rand = new Random();

    public static void readConfigs() {
	MAP_WIDTH_METERS = ConfigReader.getConfig("MAP_WIDTH_METERS", MAP_WIDTH_METERS, true);
	MAP_HEIGHT_METERS = ConfigReader.getConfig("MAP_HEIGHT_METERS", MAP_HEIGHT_METERS, true);
	MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR = ConfigReader.getConfig("MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR", MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR, true);
	UAV_SPEED_METERS_PER_SEC = ConfigReader.getConfig("UAV_SPEED_METERS_PER_SEC", UAV_SPEED_METERS_PER_SEC, true);
	UAV_MAX_TURN_RATE_DEG = ConfigReader.getConfig("UAV_MAX_TURN_RATE_DEG", UAV_MAX_TURN_RATE_DEG, true);
	UAV_MAX_Z_CHANGE = ConfigReader.getConfig("UAV_MAX_Z_CHANGE", UAV_MAX_Z_CHANGE, true);
	RRT_PREFERRED_PATH_METERS = ConfigReader.getConfig("RRT_PREFERRED_PATH_METERS", RRT_PREFERRED_PATH_METERS, true);
	RRT_MAX_PATH_METERS = ConfigReader.getConfig("RRT_MAX_PATH_METERS", RRT_MAX_PATH_METERS, true);
	RRT_BRANCH_RANGE_X_METERS = ConfigReader.getConfig("RRT_BRANCH_RANGE_X_METERS", RRT_BRANCH_RANGE_X_METERS, true);
	RRT_BRANCH_RANGE_Y_METERS = ConfigReader.getConfig("RRT_BRANCH_RANGE_Y_METERS", RRT_BRANCH_RANGE_Y_METERS, true);
	PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS = ConfigReader.getConfig("PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS", PATHPLANNER_AT_WAYPOINT_TOLERANCE_METERS, true);
	PATHPLANNER_MIN_PATH_LENGTH = ConfigReader.getConfig("MIN_PATH_LENGTH", PATHPLANNER_MIN_PATH_LENGTH, true);

        PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH = ConfigReader.getConfig("PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH", PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH, true);
	PATHPLANNER_PLANNED_PATH_TTL = ConfigReader.getConfig("PATHPLANNER_PLANNED_PATH_TTL", PATHPLANNER_PLANNED_PATH_TTL, true);
	PATHPLANNER_SMOOTH_GRAD_DESC_ON = ConfigReader.getConfig("PATHPLANNER_SMOOTH_GRAD_DESC_ON", PATHPLANNER_SMOOTH_GRAD_DESC_ON, true);
	PATHPLANNER_REDIRECT_PATH_TO_TARGET = ConfigReader.getConfig("PATHPLANNER_REDIRECT_PATH_TO_TARGET", PATHPLANNER_REDIRECT_PATH_TO_TARGET, true);
	PATHDB_PATH_CONFLICT_MIN_DIST_METERS = ConfigReader.getConfig("PATHDB_PATH_CONFLICT_MIN_DIST_METERS", PATHDB_PATH_CONFLICT_MIN_DIST_METERS, true);
	PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS = ConfigReader.getConfig("PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS", PATHDB_PATH_CONFLICT_MIN_Z_DIST_METERS, true);
	PATH_DECONFLICTION_ON = ConfigReader.getConfig("PATH_DECONFLICTION_ON", PATH_DECONFLICTION_ON, true);
	AA_MAX_DIST_CAP_METERS = ConfigReader.getConfig("AA_MAX_DIST_CAP_METERS", AA_MAX_DIST_CAP_METERS, true);
	SA9_COST_SQUARE_METERS = ConfigReader.getConfig("SA9_COST_SQUARE_METERS", SA9_COST_SQUARE_METERS, true);
	//	BBF_SENSOR_RANGE_METERS = ConfigReader.getConfig("BBF_SENSOR_RANGE_METERS", BBF_SENSOR_RANGE_METERS, true);
	BBF_SENSOR_MAX_RANGE_METERS = ConfigReader.getConfig("BBF_SENSOR_MAX_RANGE_METERS", BBF_SENSOR_MAX_RANGE_METERS, true);
	BBF_SENSOR_MIN_RANGE_METERS = ConfigReader.getConfig("BBF_SENSOR_MIN_RANGE_METERS", BBF_SENSOR_MIN_RANGE_METERS, true);
	PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH = ConfigReader.getConfig("PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH", PATHPLANNER_REPLAN_DIST_FROM_END_OF_PATH, true);
        

	BBF_GRID_SCALE_FACTOR = ConfigReader.getConfig("BBF_GRID_SCALE_FACTOR", BBF_GRID_SCALE_FACTOR, true);
	BBF_UNIFORM_PRIOR = ConfigReader.getConfig("BBF_UNIFORM_PRIOR", BBF_UNIFORM_PRIOR, true);
	BBF_RANDOM_SHARING_ON = ConfigReader.getConfig("BBF_RANDOM_SHARING_ON", BBF_RANDOM_SHARING_ON, true);
	BBF_RANDOM_SHARING_PROB = ConfigReader.getConfig("BBF_RANDOM_SHARING_PROB", BBF_RANDOM_SHARING_PROB, true);
	BBF_RANDOM_SHARING_TTL = ConfigReader.getConfig("BBF_RANDOM_SHARING_TTL", BBF_RANDOM_SHARING_TTL, true);
	BBF_KLD_SHARING_ON = ConfigReader.getConfig("BBF_KLD_SHARING_ON", BBF_KLD_SHARING_ON, true);
	BBF_REMOTE_KLD_SHARING_ON = ConfigReader.getConfig("BBF_REMOTE_KLD_SHARING_ON", BBF_REMOTE_KLD_SHARING_ON, true);
        BBF_DIFFUSE_INTERVAL_MS = ConfigReader.getConfig("BBF_DIFFUSE_INTERVAL_MS", BBF_DIFFUSE_INTERVAL_MS, true);
        BBF_DIFFUSE_DELTA = ConfigReader.getConfig("BBF_DIFFUSE_DELTA", BBF_DIFFUSE_DELTA, true);
	EMITTER_SIGNAL_STRENGTH_COEFFICIENT = ConfigReader.getConfig("EMITTER_SIGNAL_STRENGTH_COEFFICIENT", EMITTER_SIGNAL_STRENGTH_COEFFICIENT, true);
	EMITTER_SIGNAL_STRENGTH_INCREMENT = ConfigReader.getConfig("EMITTER_SIGNAL_STRENGTH_INCREMENT", EMITTER_SIGNAL_STRENGTH_INCREMENT, true);
	EMITTER_NOISE_MODEL_STD = ConfigReader.getConfig("EMITTER_NOISE_MODEL_STD", EMITTER_NOISE_MODEL_STD, true);
	OTHER_VEHICLE_COSTMAP_ON = ConfigReader.getConfig("OTHER_VEHICLE_COSTMAP_ON", OTHER_VEHICLE_COSTMAP_ON, true);
	OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS = ConfigReader.getConfig("OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS", OTHER_VEHICLE_COSTMAP_AVOID_RANGE_METERS, true);
	OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS = ConfigReader.getConfig("OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS", OTHER_VEHICLE_COSTMAP_CONFLICT_RANGE_METERS, true);
	OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS = ConfigReader.getConfig("OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS", OTHER_VEHICLE_COSTMAP_CONFLICT_Z_RANGE_METERS, true);
	BINARY_BAYES_FILTER_ON = ConfigReader.getConfig("BINARY_BAYES_FILTER_ON", BINARY_BAYES_FILTER_ON, true);
	BINARY_BAYES_FILTER_BELIEF_COSTMAP_ON = ConfigReader.getConfig("BINARY_BAYES_FILTER_BELIEF_COSTMAP_ON", BINARY_BAYES_FILTER_BELIEF_COSTMAP_ON, true);
	RANDOM_ENTIRE_MAP_COSTMAP_ON = ConfigReader.getConfig("RANDOM_ENTIRE_MAP_COSTMAP_ON", RANDOM_ENTIRE_MAP_COSTMAP_ON, true);
	RANDOM_SMALL_MOVES_COSTMAP_ON = ConfigReader.getConfig("RANDOM_SMALL_MOVES_COSTMAP_ON", RANDOM_SMALL_MOVES_COSTMAP_ON, true);
	RANDOM_SMALL_MOVES_RANGE = ConfigReader.getConfig("RANDOM_SMALL_MOVES_RANGE", RANDOM_SMALL_MOVES_RANGE, true);
	RANDOM_CLUSTERS_COSTMAP_ON = ConfigReader.getConfig("RANDOM_CLUSTERS_COSTMAP_ON", RANDOM_CLUSTERS_COSTMAP_ON, true);
	RANDOM_CLUSTERS_RANGE = ConfigReader.getConfig("RANDOM_CLUSTERS_RANGE", RANDOM_CLUSTERS_RANGE, true);
	RANDOM_CLUSTERS_COUNT = ConfigReader.getConfig("RANDOM_CLUSTERS_COUNT", RANDOM_CLUSTERS_COUNT, true);
	RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER = ConfigReader.getConfig("RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER", RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER, true);
	RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER = ConfigReader.getConfig("RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER", RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER, true);

	CONSTRAIN_MAP_TO_TEN_KM_ON = ConfigReader.getConfig("CONSTRAIN_MAP_TO_TEN_KM_ON", CONSTRAIN_MAP_TO_TEN_KM_ON, true);
	RANDOM_GAUSSIAN_COSTMAP = ConfigReader.getConfig("RANDOM_GAUSSIAN_COSTMAP", RANDOM_GAUSSIAN_COSTMAP, true);
        EO_DISTANCE_TOLERANCE = ConfigReader.getConfig("EO_DISTANCE_TOLERANCE", EO_DISTANCE_TOLERANCE, true);
	SIM_OPERATOR_DIRECTED_INFO_REQ_ON = ConfigReader.getConfig("SIM_OPERATOR_DIRECTED_INFO_REQ_ON", SIM_OPERATOR_DIRECTED_INFO_REQ_ON, true);
	VEHICLE_BELIEF_SHARE_REQ_ON = ConfigReader.getConfig("VEHICLE_BELIEF_SHARE_REQ_ON", VEHICLE_BELIEF_SHARE_REQ_ON, true);
	DYNAMIC_FLY_ZONES = ConfigReader.getConfig("DYNAMIC_FLY_ZONES", DYNAMIC_FLY_ZONES, true);


        BAYES_FILTER_PANEL_ON = ConfigReader.getConfigBoolean("BAYES_FILTER_PANEL_ON", BAYES_FILTER_PANEL_ON, false);
        BAYES_FILTER_PANEL_X = ConfigReader.getConfigInt("BAYES_FILTER_PANEL_X", BAYES_FILTER_PANEL_X, false);
        BAYES_FILTER_PANEL_Y = ConfigReader.getConfigInt("BAYES_FILTER_PANEL_Y", BAYES_FILTER_PANEL_Y, false);
        Machinetta.Debugger.debug(1, "BAYES_FILTER_PANEL_ON="+BAYES_FILTER_PANEL_ON);

        ENTROPY_PANEL_ON = ConfigReader.getConfigBoolean("ENTROPY_PANEL_ON", ENTROPY_PANEL_ON, false);
        ENTROPY_PANEL_X = ConfigReader.getConfigInt("ENTROPY_PANEL_X", ENTROPY_PANEL_X, false);
        ENTROPY_PANEL_Y = ConfigReader.getConfigInt("ENTROPY_PANEL_Y", ENTROPY_PANEL_Y, false);
        Machinetta.Debugger.debug(1, "ENTROPY_PANEL_ON="+ENTROPY_PANEL_ON);

	VIRTUAL_COCKPIT_IP_ADDRESS = ConfigReader.getConfig("VIRTUAL_COCKPIT_IP_ADDRESS", VIRTUAL_COCKPIT_IP_ADDRESS, false);
        Machinetta.Debugger.debug(1, "VIRTUAL_COCKPIT_IP_ADDRESS="+VIRTUAL_COCKPIT_IP_ADDRESS);
	
	MAP_LOWER_LEFT_LAT = ConfigReader.getConfig("MAP_LOWER_LEFT_LAT", MAP_LOWER_LEFT_LAT, false);
	MAP_LOWER_LEFT_LON = ConfigReader.getConfig("MAP_LOWER_LEFT_LON", MAP_LOWER_LEFT_LON, false);
	FLY_ZONE_POLY = ConfigReader.getConfig("FLY_ZONE_POLY", FLY_ZONE_POLY, false);
        Machinetta.Debugger.debug(1, "FLY_ZONE_POLY="+FLY_ZONE_POLY);

	PROCERUS_UAV_ID = ConfigReader.getConfigInt("PROCERUS_UAV_ID", PROCERUS_UAV_ID, false);
        Machinetta.Debugger.debug(1, "PROCERUS_UAV_ID="+PROCERUS_UAV_ID);

	USE_VIRTUAL_COCKPIT = ConfigReader.getConfig("USE_VIRTUAL_COCKPIT", USE_VIRTUAL_COCKPIT, false);
	VC_BRIDGE_SERVER_IP_ADDRESS = ConfigReader.getConfig("VC_BRIDGE_SERVER_IP_ADDRESS", VC_BRIDGE_SERVER_IP_ADDRESS, false);
	VC_BRIDGE_SERVER_PORT = ConfigReader.getConfig("VC_BRIDGE_SERVER_PORT", VC_BRIDGE_SERVER_PORT, false);

	RRTCONFIG_NO_EXPANSIONS = ConfigReader.getConfig("RRTCONFIG_NO_EXPANSIONS", RRTCONFIG_NO_EXPANSIONS, false);	
	RRTCONFIG_BRANCHES_PER_EXPANSION = ConfigReader.getConfig("RRTCONFIG_BRANCHES_PER_EXPANSION", RRTCONFIG_BRANCHES_PER_EXPANSION, false);	
	RRTCONFIG_MAX_THETA_CHANGE = ConfigReader.getConfig("RRTCONFIG_MAX_THETA_CHANGE", RRTCONFIG_MAX_THETA_CHANGE, false);	
	RRTCONFIG_MAX_PSI_CHANGE = ConfigReader.getConfig("RRTCONFIG_MAX_PSI_CHANGE", RRTCONFIG_MAX_PSI_CHANGE, false);	
	RRTCONFIG_MAX_BRANCH_LENGTH = ConfigReader.getConfig("RRTCONFIG_MAX_BRANCH_LENGTH", RRTCONFIG_MAX_BRANCH_LENGTH, false);	
	RRTCONFIG_MIN_BRANCH_LENGTH = ConfigReader.getConfig("RRTCONFIG_MIN_BRANCH_LENGTH", RRTCONFIG_MIN_BRANCH_LENGTH, false);	
	RRTCONFIG_RRT_BRANCH_RANGE_X_METERS = ConfigReader.getConfig("RRTCONFIG_RRT_BRANCH_RANGE_X_METERS", RRTCONFIG_RRT_BRANCH_RANGE_X_METERS, false);	
	RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS = ConfigReader.getConfig("RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS", RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS, false);	

        Machinetta.Debugger.debug(1, "USE_VIRTUAL_COCKPIT="+USE_VIRTUAL_COCKPIT);
        Machinetta.Debugger.debug(1, "VC_BRIDGE_SERVER_IP_ADDRESS="+VC_BRIDGE_SERVER_IP_ADDRESS);
        Machinetta.Debugger.debug(1, "VC_BRIDGE_SERVER_PORT="+VC_BRIDGE_SERVER_PORT);
    }

    // @todo: Ponder interface hack, this code will probably go away soon.
    public static boolean PONDER_ON = false;
    PonderMonitor ponderMonitor = null;
    int ponderXlimit = 50000;
    SimpleStaticCostMap ponderCostMap = null;
    public void ponder(String line) {
        Machinetta.Debugger.debug(1, "Received from ponder: "+line);
	try {
	    ponderXlimit = Integer.parseInt(line);
	    planner.removeCostMap(ponderCostMap);
	    ponderCostMap = new SimpleStaticCostMap();
	    ponderCostMap.addCostRect(new Rectangle(0, 0, ponderXlimit, 50000), -1000.0);
	    int costWidth= 50000 - ponderXlimit;
	    int range1 = ponderXlimit + 1*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(ponderXlimit, 0, range1, 50000), 100.0);
	    int range2 = ponderXlimit + 2*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(range1, 0, range2, 50000), 200.0);
	    int range3 = ponderXlimit + 3*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(range2, 0, range3, 50000), 300.0);
	    int range4 = ponderXlimit + costWidth;
	    ponderCostMap.addCostRect(new Rectangle(range3, 0, range4, 50000), 400.0);
	    planner.addCostMap(ponderCostMap);
	    planner.forceReplan("UAVRI adding ponder costmaps");
	}
	catch(Exception e) {
	    Machinetta.Debugger.debug(1, "Exception processing ponder input, ignoring, e="+e);
	    e.printStackTrace();
	}
    }

    private void exitBadFlyZone() {
	Machinetta.Debugger.debug(5, "BIG PROBLEM: We have a fly zone (in lat/lon) but don't have the necessary origin point lat/lon to convert it to local coords for planning.");
	Machinetta.Debugger.debug(5, "BIG PROBLEM: If FLY_ZONE_POLY in .cfg is non null and we are unable to parse it then we exit.");
	Machinetta.Debugger.debug(5, "BIG PROBLEM: Better to exit than sending presumably REAL UAVs off into who knows where.");
	System.exit(1);
    }
    // This should be done in some better way.... somehow.
    private void parseFlyZonePoly() {
	if(null == FLY_ZONE_POLY)
	    return;
	if((-1000 == MAP_LOWER_LEFT_LAT) || (-1000 == MAP_LOWER_LEFT_LON))
	    exitBadFlyZone();
	
	try {
	    StringTokenizer tok = new StringTokenizer(FLY_ZONE_POLY);
	    if(false) {
		ArrayList<Point2D.Double> latLonList = new ArrayList<Point2D.Double>();
		StringBuffer flyzoneLatlonCoords = new StringBuffer("");
		while(tok.hasMoreTokens()) {
		    double lat;
		    double lon;
		    lat = Double.parseDouble(tok.nextToken());
		    lon = Double.parseDouble(tok.nextToken());
		    latLonList.add(new Point2D.Double(lat,lon));
		    flyzoneLatlonCoords.append("("+lat+","+lon+") ");
		}
		Machinetta.Debugger.debug(1, "Flyzone lat/lon pairs = "+ flyzoneLatlonCoords.toString());


		boolean first = true;
		int x=Integer.MAX_VALUE;
		int y=Integer.MAX_VALUE;
		StringBuffer flyzoneLocalCoords = new StringBuffer("");
		ArrayList<Point> pointList = new ArrayList<Point>();
		for(int loopi = 0; loopi < latLonList.size(); loopi++) {
		    double localx = LatLonUtil.lonToLocalXMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, latLonList.get(loopi).x);
		    double localy = LatLonUtil.latToLocalYMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, latLonList.get(loopi).y);
		    if(first) {
			x = (int)localx;
			y = (int)localy;
			first = false;
		    }
		    Machinetta.Debugger.debug(1, "Flyzone converted lat/lon "+latLonList.get(loopi).x+","+latLonList.get(loopi).y+" to "+localx+","+localy);
	    
		    pointList.add(new Point((int)localx,(int)localy));
		    flyzoneLocalCoords.append("("+(int)localx+","+(int)localy+") ");
		}
		Machinetta.Debugger.debug(1, "Creating Flyzone at "+x+", "+y +" = "+ flyzoneLocalCoords.toString());
		flyZonePointAry = pointList.toArray(new Point[1]);
	    }
	    else {
		boolean first = true;
		int x=Integer.MAX_VALUE;
		int y=Integer.MAX_VALUE;
		StringBuffer pavementLocalCoords = new StringBuffer("");
		Machinetta.Debugger.debug(1, "        Origin lat/lon = "+MAP_LOWER_LEFT_LAT+","+MAP_LOWER_LEFT_LON);
		ArrayList<Point> pointList = new ArrayList<Point>();
		while(tok.hasMoreTokens()) {
		    double lat;
		    double lon;
		    lat = Double.parseDouble(tok.nextToken());
		    lon = Double.parseDouble(tok.nextToken());

		    double localx = LatLonUtil.lonToLocalXMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, lon);
		    double localy = LatLonUtil.latToLocalYMeters(MAP_LOWER_LEFT_LAT, MAP_LOWER_LEFT_LON, lat);
		    if(first) {
			x = (int)localx;
			y = (int)localy;
			first = false;
		    }
		    pointList.add(new Point((int)localx,(int)localy));
		    pavementLocalCoords.append("("+(int)localx+","+(int)localy+") ");
		}
                    
                Machinetta.Debugger.debug(1, "        Creating FLY_ZONE_POLY at "+x+", "+y +" = "+ pavementLocalCoords.toString());
		flyZonePointAry = pointList.toArray(new Point[1]);
	    }
	}
	catch(Exception e) {
	    Machinetta.Debugger.debug(5, "BIG PROBLEM: Exception parsing fly zone, e="+e);
	    e.printStackTrace();
	    exitBadFlyZone();
	}
    }

    /** Creates a new instance of RAPInterface */
    public UAVRI() {
        Machinetta.Debugger.debug(1, "Creating RAPInterface");

        Machinetta.Debugger.debug(1, "reading config");
	readConfigs();

	GAConf rrtConfig = new GAConf();
	rrtConfig.NO_EXPANSIONS = RRTCONFIG_NO_EXPANSIONS;
	rrtConfig.BRANCHES_PER_EXPANSION = RRTCONFIG_BRANCHES_PER_EXPANSION;
	rrtConfig.MAX_THETA_CHANGE = RRTCONFIG_MAX_THETA_CHANGE;
	rrtConfig.MAX_PSI_CHANGE = RRTCONFIG_MAX_PSI_CHANGE;
	rrtConfig.MAX_BRANCH_LENGTH = RRTCONFIG_MAX_BRANCH_LENGTH;
	rrtConfig.MIN_BRANCH_LENGTH = RRTCONFIG_MIN_BRANCH_LENGTH;
//	rrtConfig.RRT_BRANCH_RANGE_X_METERS = RRTCONFIG_RRT_BRANCH_RANGE_X_METERS;
//	rrtConfig.RRT_BRANCH_RANGE_Y_METERS = RRTCONFIG_RRT_BRANCH_RANGE_Y_METERS;
	ArrayList<GAConf> rrtConfigList = new ArrayList<GAConf>();
	rrtConfigList.add(rrtConfig);
	RRTPlanner.setConfigs(rrtConfigList);
	GAEnv gaenv = new GAEnv();
	gaenv.xsize = MAP_WIDTH_METERS;
	gaenv.ysize = MAP_HEIGHT_METERS;
	gaenv.zsize = 2000;
	RRTPlanner.setEnv(gaenv);
	Vehicle gavehicle = new Vehicle("UAV",UAV_SPEED_METERS_PER_SEC,UAV_MAX_TURN_RATE_DEG,10);
	RRTPlanner.setVehicle(gavehicle);

	if(null != FLY_ZONE_POLY) {
	    parseFlyZonePoly();
	}
	// @todo: should get these elsewhere.
        Machinetta.Debugger.debug(1, "creating SmallUAVModel");
	uavModel = new SmallUAVModel("me", UAV_SPEED_METERS_PER_SEC, 30, .1, .4);
	
        Machinetta.Debugger.debug(1, "creating MiniWorldState");
	miniWorldState = new MiniWorldState();

	if(PATH_DECONFLICTION_ON) {
	    Machinetta.Debugger.debug(1, "creating PathDB");
	    pathDB = new PathDB(PATHDB_PATH_CONFLICT_MIN_DIST_METERS);
	    pathDB.start();
	}
        if(BINARY_BAYES_FILTER_ON) {
	    Machinetta.Debugger.debug(1, "creating BBF costmap");
	    buildBBFFrames();
            bbfCM = new BinaryBayesFilterCostMap(MAP_WIDTH_METERS, MAP_HEIGHT_METERS, miniWorldState, bbfBeliefTabPanel, bbfEntropyTabPanel);
	    bbfCM.start();
        }
	else if(RANDOM_ENTIRE_MAP_COSTMAP_ON 
		  || RANDOM_SMALL_MOVES_COSTMAP_ON 
		  || RANDOM_CLUSTERS_COSTMAP_ON) {
	    Machinetta.Debugger.debug(1, "creating random costmaps");
	    int botLeftX = 0;
	    int botLeftY = 0;
	    int rangeX = MAP_WIDTH_METERS;
	    int rangeY = MAP_HEIGHT_METERS;
	    if(CONSTRAIN_MAP_TO_TEN_KM_ON) {
		botLeftX = 20000;
		botLeftY = 20000;
		rangeX = 10000;
		rangeY = 10000;
	    }
	    randomCM = new RandomGaussiansCostMap(botLeftX, botLeftY, rangeX, rangeY, true, 
						  RANDOM_GAUSSIAN_AMPLITUDE_MULTIPLIER, 
						  RANDOM_GAUSSIAN_DIVISOR_MULTIPLIER,
						  RANDOM_SMALL_MOVES_COSTMAP_ON, 
						  RANDOM_SMALL_MOVES_RANGE,
						  RANDOM_CLUSTERS_COSTMAP_ON, 
						  RANDOM_CLUSTERS_RANGE, 
						  RANDOM_CLUSTERS_COUNT);
        }
	
	Machinetta.Debugger.debug(1, "creating pathplanner");
	autopilot = new Autopilot(this,uavModel,miniWorldState);
        planner = new PathPlanner(this,DYNAMIC_FLY_ZONES, autopilot);
        planner.setRequireApproval(true);
        
	if((null != FLY_ZONE_POLY) && (null == flyZonePointAry))
	    exitBadFlyZone();
	if(null != flyZonePointAry) {
	    StringBuffer flyZonePointBuf = new StringBuffer("");
	    for(int loopi = 0; loopi < flyZonePointAry.length; loopi++) {
		flyZonePointBuf.append("("+flyZonePointAry[loopi].x+","+flyZonePointAry[loopi].y+"), ");
	    }
	    Machinetta.Debugger.debug(1, "Adding costmap for flyzone="+flyZonePointBuf.toString());
	    SimpleStaticCostMap3D flyZoneCostMap = new SimpleStaticCostMap3D();
	    flyZoneCostMap.addCostPoly(flyZonePointAry, 1000000000000.0, true);
	    planner.addCostMap(flyZoneCostMap);
	}

//         // The only number that matters here is the z (because planner is keeping UAV at current height)
// 	Machinetta.Debugger.debug(1, "updating location to pathplanner.");
//         autopilot.updateLocation(0.0, 0.0, 1000.0);
// 	Machinetta.Debugger.debug(1, "Done updating location to pathplanner.");
        
	if(RANDOM_GAUSSIAN_COSTMAP > 0) {
	    Machinetta.Debugger.debug(1, "creating "+RANDOM_GAUSSIAN_COSTMAP+" random gaussian costmaps");
	    int botLeftX = 0;
	    int botLeftY = 0;
	    int rangeX = MAP_WIDTH_METERS;
	    int rangeY = MAP_HEIGHT_METERS;
	    if(CONSTRAIN_MAP_TO_TEN_KM_ON) {
		botLeftX = 20000;
		botLeftY = 20000;
		rangeX = 10000;
		rangeY = 10000;
	    }
	    staticRandomGaussianCM = new MixGaussiansCostMap();
	    
	    for(int loopi = 0; loopi < RANDOM_GAUSSIAN_COSTMAP; loopi++) {
		int x = botLeftX + rand.nextInt(rangeX);
		int y = botLeftY + rand.nextInt(rangeY);
		staticRandomGaussianCM.addGaussian(x, y, -200.0,400);
	    }
	    planner.addCostMap(staticRandomGaussianCM);
	}

        if(BINARY_BAYES_FILTER_ON) {
            planner.addCostMap(bbfCM);
        } else if(RANDOM_ENTIRE_MAP_COSTMAP_ON 
		  || RANDOM_SMALL_MOVES_COSTMAP_ON 
		  || RANDOM_CLUSTERS_COSTMAP_ON) {
	    planner.addCostMap(randomCM);
        } else {
            // planner.addCostMap(gaussianCM);
        }
        
	if(BINARY_BAYES_FILTER_ON && BINARY_BAYES_FILTER_BELIEF_COSTMAP_ON) {
	    Machinetta.Debugger.debug(1, "creating bbf belief costmap");
	    bbfBeliefCostMap = new BeliefCostMap(MAP_WIDTH_METERS, MAP_HEIGHT_METERS,bbfCM);
	    planner.addCostMap(bbfBeliefCostMap);
	}

        //
        // Create the DirectionalRFFilter
        //
        // Sean: please make this parameterizable
        //
	Machinetta.Debugger.debug(1, "creating directional RF filter");
        new DirectionalRFFilter();
        
        
        // Edges ...        
	Machinetta.Debugger.debug(1, "creating edge costmaps.");
        SimpleStaticCostMap edgeCostMap = new SimpleStaticCostMap();
	if(CONSTRAIN_MAP_TO_TEN_KM_ON) {
	    edgeCostMap.addCostRect(new Rectangle(-50, 0, 20000, MAP_HEIGHT_METERS), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(MAP_WIDTH_METERS - 20000, 0, MAP_WIDTH_METERS+50, MAP_HEIGHT_METERS), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(0, -50, MAP_WIDTH_METERS, 20000), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(0, MAP_HEIGHT_METERS -20000, MAP_WIDTH_METERS, MAP_HEIGHT_METERS+50), 10000.0);             
	}
	else {            
	    edgeCostMap.addCostRect(new Rectangle(-50, 0, 50, MAP_HEIGHT_METERS), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(MAP_WIDTH_METERS - 50, 0, MAP_WIDTH_METERS+50, MAP_HEIGHT_METERS), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(0, -50, MAP_WIDTH_METERS, 50), 10000.0);
	    edgeCostMap.addCostRect(new Rectangle(0, MAP_HEIGHT_METERS -50, MAP_WIDTH_METERS, MAP_HEIGHT_METERS+50), 10000.0);             
	}
	planner.addCostMap(edgeCostMap);
        
	if(PONDER_ON) {
	    Machinetta.Debugger.debug(1, "doing ponder crap");
	    ponderMonitor = new PonderMonitor("robola.cimds.ri.cmu.edu", 15555, this);
	    ponderMonitor.start();
	    ponderCostMap = new SimpleStaticCostMap();
	    ponderCostMap.addCostRect(new Rectangle(0, 0, ponderXlimit, 50000), -1000.0);
	    int costWidth= 50000 - ponderXlimit;
	    int range1 = ponderXlimit + 1*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(ponderXlimit, 0, range1, 50000), 100.0);
	    int range2 = ponderXlimit + 2*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(range1, 0, range2, 50000), 200.0);
	    int range3 = ponderXlimit + 3*(costWidth/4);
	    ponderCostMap.addCostRect(new Rectangle(range2, 0, range3, 50000), 300.0);
	    int range4 = ponderXlimit + costWidth;
	    ponderCostMap.addCostRect(new Rectangle(range3, 0, range4, 50000), 400.0);
	    planner.addCostMap(ponderCostMap);
	}

        /*
        try {
            TiledCostMap terrainCM = null;
            if (TERRAIN_COST_MAP_FILE != null) {
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(TERRAIN_COST_MAP_FILE));
                terrainCM = (TiledCostMap)is.readObject();
                cms.add(terrainCM);
            } else {
                Machinetta.Debugger.debug("No terrain cost map location provided.", 3, "RRTPlanner");
            }
        } catch (IOException ex) {
            Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, "RRTPlanner");
        } catch (ClassNotFoundException ex) {
            Machinetta.Debugger.debug("Failed to get Terrain cost map:" + ex, 3, "RRTPlanner");
        }
         */
        
        // @todo Move this to the default role the UAVs take on
        try {
            // SRO Tue Aug  1 18:47:48 EDT 2006
            //
            // replaced this;
            //
            //	    InformationAgentFactory.addBeliefShareRequirement(
            //	      new BeliefShareRequirement(Class.forName("AirSim.Machinetta.Beliefs.UAVLocation"), 5));
            //
            // with directed;
            
	    if(SIM_OPERATOR_DIRECTED_INFO_REQ_ON) {
		Machinetta.Debugger.debug(1, "Adding operator directed info requirements");
		NamedProxyID oper = new NamedProxyID("Operator0");
		DirectedInformationRequirement dir;
		Class dirClass;
		
		dirClass = Class.forName("AirSim.Machinetta.Beliefs.ImageData");
		dir = new DirectedInformationRequirement(dirClass, oper);
		InformationAgentFactory.addDirectedInformationRequirement(dir);

		dirClass = Class.forName("AirSim.Machinetta.PlannedPath");
		dir = new DirectedInformationRequirement(dirClass, oper);
		InformationAgentFactory.addDirectedInformationRequirement(dir);

		dirClass = Class.forName("AirSim.Machinetta.Beliefs.AssetStateBelief");
		dir = new DirectedInformationRequirement(dirClass, oper);
		InformationAgentFactory.addDirectedInformationRequirement(dir);

		dirClass = Class.forName("AirSim.Machinetta.Beliefs.VehicleBelief");
		dir = new DirectedInformationRequirement(dirClass, oper);
		InformationAgentFactory.addDirectedInformationRequirement(dir);

		dirClass = Class.forName("AirSim.Machinetta.Beliefs.TMAScanResult");
		dir = new DirectedInformationRequirement(dirClass, oper);
		InformationAgentFactory.addDirectedInformationRequirement(dir);


		if(DYNAMIC_FLY_ZONES) {
		    Machinetta.Debugger.debug(1, "Adding Dynamic fly zone DIRs");
		    NamedProxyID tc = new NamedProxyID("TrafficController0");

		    // Make every UAV send their locations to the TrafficController
		    dirClass = Class.forName("AirSim.Machinetta.Beliefs.UAVLocation");
		    dir = new DirectedInformationRequirement(dirClass, tc);
		    InformationAgentFactory.addDirectedInformationRequirement(dir);

		    // Make every UAV send their path to the TrafficController
		    dirClass = Class.forName("AirSim.Machinetta.PlannedPath");
		    dir = new DirectedInformationRequirement(dirClass, tc);
		    InformationAgentFactory.addDirectedInformationRequirement(dir);

		    // make every UAV send their fly zone requests to TrafficController.
		    dirClass = Class.forName("AirSim.Machinetta.Beliefs.FlyZone");
		    dir = new DirectedInformationRequirement(dirClass, tc);
		    InformationAgentFactory.addDirectedInformationRequirement(dir);
		}


	    }

	    if(VEHICLE_BELIEF_SHARE_REQ_ON) {
		InformationAgentFactory.addBeliefShareRequirement(new BeliefShareRequirement(Class.forName("AirSim.Machinetta.Beliefs.VehicleBelief"), 20));
	    }

        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug(3, "Could not find class : " + e);
        }
        
        Machinetta.Debugger.debug(1, "Done creating RAPInterface");
    }
    
    private void buildBBFFrames() {
        if(BAYES_FILTER_PANEL_ON) {
	    bbfBeliefFrame = new JFrame("Bayes Filter Beliefs "+proxyIDString);
	    bbfBeliefTabPanel = new BBFTabPanel(miniWorldState);
	    bbfBeliefFrame.setLocation(BAYES_FILTER_PANEL_X,BAYES_FILTER_PANEL_Y);
	    bbfBeliefFrame.getContentPane().setLayout(new BorderLayout());
	    bbfBeliefFrame.getContentPane().add(bbfBeliefTabPanel, BorderLayout.CENTER);
	    bbfBeliefFrame.pack();
	    bbfBeliefFrame.setSize((int)(MAP_WIDTH_METERS/(BBF_GRID_SCALE_FACTOR*2)), (int)(MAP_HEIGHT_METERS/(BBF_GRID_SCALE_FACTOR*2)));
	    bbfBeliefFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfBeliefFrame.setVisible(true);
	} 
        
        if(ENTROPY_PANEL_ON) {
	    bbfEntropyFrame = new JFrame("Entropy "+proxyIDString);
	    bbfEntropyTabPanel = new BBFTabPanel(miniWorldState);
	    bbfEntropyFrame.setLocation(ENTROPY_PANEL_X,ENTROPY_PANEL_Y);
	    bbfEntropyFrame.getContentPane().setLayout(new BorderLayout());
	    bbfEntropyFrame.getContentPane().add(bbfEntropyTabPanel, BorderLayout.CENTER);
	    bbfEntropyFrame.pack();
	    bbfEntropyFrame.setSize((int)(MAP_WIDTH_METERS/(BBF_GRID_SCALE_FACTOR*2)), (int)(MAP_HEIGHT_METERS/(BBF_GRID_SCALE_FACTOR*2)));
	    bbfEntropyFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
	    bbfEntropyFrame.setVisible(true);
	}
    }

    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        if (proxyID == null) {
            Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
            proxyID = state.getSelf().getProxyID();
	    backend.setProxyID(proxyID);
        }
        
        if (msg instanceof NewRoleMessage) {
            // UAVRI Intercepts these messages, so it can do some of the work for the agent.
            Machinetta.Debugger.debug(1, "UAVRI intercepted: " + msg);
            BasicRole role = (BasicRole)((NewRoleMessage)msg).getRole();
            if(TaskType.EOImage == role.type) {
                if(BINARY_BAYES_FILTER_ON) {
                    planner.removeCostMap(bbfCM);
		} else if(RANDOM_ENTIRE_MAP_COSTMAP_ON 
		      || RANDOM_SMALL_MOVES_COSTMAP_ON 
		      || RANDOM_CLUSTERS_COSTMAP_ON) {
                    planner.removeCostMap(randomCM);
                }

            }
            planner.addRole(role);
        } else if (msg instanceof RoleCancelMessage) {
            Machinetta.Debugger.debug(1, "UAVRI intercepted: " + msg);
            BasicRole role = (BasicRole)((RoleCancelMessage)msg).getRole();
            planner.removeRole(role);
        } else {
            send(msg, proxyID);
        }
    }
    
    /**
     * This simply starts the planner off.
     *
     * Unforunately, it can start the planner off too soon, hence the wait.
     */
    public void run() {
        do {
            try {
                sleep(100);
            } catch (InterruptedException e) {}
        } while(state.getSelf() == null);
        
        try {
            sleep(2000);
        } catch (InterruptedException e) {}
        planner.start();
    }
    
    /**
     * Not precisely sure what this is going to do yet.
     */
    public Machinetta.RAPInterface.InputMessages.InputMessage[] getMessages() {
        Machinetta.Debugger.debug(3, "Unimplemented getMessages() being called ... ");
        return null;
    }
    
    int sent = 0, recv = 0;
    boolean firstLocationInfo = true;
    
    protected UDPCommon backend = new UDPCommon() {
        public boolean processMessage(DatagramPacket packet) {
            if(!UDPMessage.isRAPInput(packet.getData())) {
		// Leaving this debug in because with the new comms
		// lib fixes this really shouldn't ever happen.
		Debugger.debug(1,"Not consuming non RAPInput message, ID="+UDPMessage.getID(packet.getData())+" summary="+UDPMessage.summary(packet.getData()));
		return false;
	    }

	    //            Machinetta.Debugger.debug("UAVRI processing message: " + packet, 0, this);
            
            // This first if statement is basically just checking that the beliefs
            // from the XML file are properly loaded.
            if (proxyIDString == null) {
                Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
                while (state.getSelf() == null) {
                    Machinetta.Debugger.debug(3, "WARNING No self in beliefs!  Sleeping for 100ms and then trying again");
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (state.getSelf() == null) {
                    Machinetta.Debugger.debug(5, "No self in beliefs!! - exiting");
                    System.exit(-1);
                }
                proxyID = state.getSelf().getProxyID();
		proxyIDString = proxyID.toString();
            }
            
            // Now process the message
            if (proxyIDString != null) {
                if (UDPMessage.isRAPInput(packet.getData(), proxyIDString)) {
                    UDPMessage msg = UDPMessage.getMessage(packet.getData());
		    //                    Machinetta.Debugger.debug("Sending " + ++recv + "th message to proxy from RAP" + msg, 0, this);
                    Machinetta.RAPInterface.InputMessages.InputMessage iMsg = msg.getInputMessage();
                    if (iMsg instanceof RPMessage) {
                        RPMessage rpMsg = (RPMessage)iMsg;
                        switch (rpMsg.type) {
                            
                            case NAVIGATION_DATA:
                                NavigationDataAP nMsg = (NavigationDataAP)iMsg;
			
				AssetStateBelief asb = new AssetStateBelief(proxyID, nMsg);
				//				Machinetta.Debugger.debug(1,"processMessage: NAVIGATION_DATA msg resulting in AssetStateBelief = "+asb);
				// set locallySensed true in order to trigger any DIRs.
				asb.setLocallySensed(true);
				state.addBelief(asb);
				state.notifyListeners();

				//				Debugger.debug(1,"Sending "+asb);
	    
				state.getSelf().miscInfo.put("AssetState", asb);
				state.addBelief(state.getSelf());

				// Update our local record of time.
				SimTime.updateTime(nMsg.time);

                                // Tell planner about UAVs current state.
				//                                Machinetta.Debugger.debug("processMessage: Received Navigation Data from UAV: " + nMsg.latitude + ", " + nMsg.longtitude, 0, this);

				//                                autopilot.updateLocation(nMsg.longtitude,nMsg.latitude, nMsg.altitude);
				autopilot.updateLocation(nMsg.xMeters,nMsg.yMeters, nMsg.zMeters);
				miniWorldState.setLocation(nMsg.xMeters, nMsg.yMeters, nMsg.zMeters);
                                
                                if (firstLocationInfo) {
				    Stats.init();

				    // NOTE: Since we now check for
				    // 'first nav data' in Autopilot,
				    // no longer need to forceReplan
				    // here.
				    //
 				    // Machinetta.Debugger.debug("processMessage: Very first Navigation Data from UAV that we have received, forcing replan : " + nMsg.longtitude + ", " + nMsg.latitude, 1, this);
				    // planner.forceReplan();
                                    firstLocationInfo = false;
                                }
                                
				uavModel.updateNav(nMsg.xMeters,nMsg.yMeters,nMsg.zMeters,nMsg.headingDegrees,nMsg.verticalVelocity,nMsg.time);
// 				// @TODO: Should get rid of
// 				// UAVLocation, it has been replaced
// 				// by AssetStateBelief.
//                                 UAVLocation loc = new UAVLocation(proxyID,
// 								  nMsg.xMeters, 
// 								  nMsg.yMeters, 
// 								  nMsg.zMeters,
// 								  nMsg.headingDegrees, 
// 								  nMsg.groundSpeed);
                                
//                                 location = loc;
//                                 loc.setLocallySensed(true);
//                                 state.addBelief(loc);
//                                 state.notifyListeners();
				
                                break;
                                
                            case TERMINAL_PROBLEM:
                                Machinetta.Debugger.debug(1, "processMessage: Terminal Problem Message Received from UAV");
                                // @todo Implement
                                break;
                                
                            case SEARCH_SENSOR_READING:
                                TMAScanResult tmasr = new TMAScanResult();
                                Machinetta.Debugger.debug(1, "processMessage: SEARCH_SENSOR_READING="+tmasr);
                                tmasr.setData((SearchSensorReadingAP)rpMsg);
                                tmasr.setLocallySensed(true);
                                state.addBelief(tmasr);
                                state.notifyListeners();
                                break;
                                
                                
                            case GEOLOCATE_DATA:
				Machinetta.Debugger.debug(1, "processMessage: GEOLOCATE_DATA msg="+rpMsg);
                                
                                if (currentGLID != null) {
                                    tmasr = new TMAScanResult();
                                    tmasr.setData((GeoLocateDataAP)rpMsg);
                                    tmasr.setLocallySensed(true);
                                    tmasr.id = new BeliefNameID(currentGLID);
                                    tmasr.isGeolocateData = true;
                                    tmasr.requestID = currentGLBID;
                                    Machinetta.Debugger.debug(1, "Received Geolocate Data from UAV: " + tmasr);
                                    
                                    state.addBelief(tmasr);
                                    state.notifyListeners();
                                    
                                    currentGLID = null;
                                }
                                break;
                                
                            case SAR_SENSOR:
				Machinetta.Debugger.debug(1, "processMessage: SAR_SENSOR msg="+rpMsg);
                                
                                SARSensorReading ssr = (SARSensorReading)rpMsg.params.get(0);
                                //VehicleBelief vb = new VehicleBelief(ssr.getMostLikely(), ssr.x, ssr.y, ssr.getHighestProb());
				Asset.Types type = ssr.getMostLikelyExClutter();
				double confidence = ssr.getProb(type);
                                VehicleBelief vb = new VehicleBelief(ssr.time, type, confidence, ssr.x, ssr.y, ssr.z, ssr.state);
				vb.setHeading(ssr.heading);
				vb.setForceId(ssr.forceId);
				if(null == vb.getForceId()) {
				    vb.setForceId(ForceId.UNKNOWN);
				}
				vb.setSensor(proxyID);
                                Machinetta.Debugger.debug(1, "Received SAR_SENSOR: " + vb);
                                vb.setLocallySensed(true);
                                state.addBelief(vb);
                                state.notifyListeners();
                                // since we've sensed a target, request that it be geolocated
                                // this should really only be done if the sensed target
                                // reaches a threshold probability that it is a SCUD (SA-9)
                                // Also this should only happen if the sensor reading cannot
                                // be associated with an existing, previously sensed target
                                // (only done once when initially sensed
                                GeoLocateRequest glr = new GeoLocateRequest();
                                // Lat/Lon seem to be used interchangably with y/x in the code
                                // only seems to be used for the label anyways
                                int x = vb.getX();
                                int y = vb.getY();
                                // combine the x/y location into a single number to use as the id
                                // this associates a unique id preventing the creation of multiple plans
                                // round the int locations to nearest 10, shift x (decimalwise) and concatenate
                                if( x > 999999 | y > 999999 )
                                {
                                    Machinetta.Debugger.debug( Machinetta.Debugger.DL_SERIOUS_PROBLEM, "GeoLocateRequest specified outside of expected bounds." );
                                }
                                glr.emitterID = ( ( ( ( x + 5 ) / 10 ) * 10 * 1000000L ) + ( ( y + 5 ) / 10 ) * 10 );
                                glr.longtitude = x;
                                glr.latitude = y;
                                glr.setLocallySensed(true);
                                state.addBelief(glr);
                                state.notifyListeners();
                                break;
                                
                            case EOIR_SENSOR_READING:
                                EOIRSensorReadingAP eoMsg = (EOIRSensorReadingAP)rpMsg;
                                Machinetta.Debugger.debug(1, "processMessage: EOIR_SENSOR_READING="+eoMsg);
                                ImageData id = new ImageData();
                                id.data = eoMsg.imgData;
                                id.loc = location.asVector();
                                id.type = ImageData.ImageTypes.EO;
                                id.setLocallySensed(true);
				id.assetType = eoMsg.type;
				id.forceId = eoMsg.forceId;
				id.state = eoMsg.state;
                                state.addBelief(id);
                                state.notifyListeners();
                                break;
                                
                            case RSSI_SENSOR_READING:
                                RSSISensorReading rssiSR = (RSSISensorReading)rpMsg.params.get(0);
                                Machinetta.Debugger.debug(0, "processMessage: RSSIAP="+rssiSR);
				for(int loopi = 0; loopi < rssiSR.channels.length;loopi++) {
				    RSSIReading reading = new RSSIReading();
				    reading.x = rssiSR.x;
				    reading.y = rssiSR.y;
				    reading.z = rssiSR.z;
				    reading.channel = rssiSR.channels[loopi];
				    reading.time = rssiSR.time;
				    reading.strength = rssiSR.strengths[loopi];
				    reading.sensor = state.getSelf().getProxyID();
				    reading.setLocallySensed(true);
				    //				    Machinetta.Debugger.debug("processMessage: Created RSSIReading="+reading, 1, this);
				    state.addBelief(reading);
				}
				state.notifyListeners();
                                break;
                                
                            case DIRECTIONAL_RF_SENSOR_READING:                               
                                DirRFSensorReading dirSR = (DirRFSensorReading)rpMsg.params.get(0);
                                Machinetta.Debugger.debug(1, "processMessage: DirRFSensorReading " + dirSR);
                                DirectionalRFReading dReading = new DirectionalRFReading();
                                dReading.x = dirSR.x;
                                dReading.y = dirSR.y;
                                dReading.z = dirSR.z;
                                dReading.values = new double[dirSR.values.length];
                                System.arraycopy(dirSR.values, 0, dReading.values, 0, dirSR.values.length);
                                dReading.sensor = state.getSelf().getProxyID();
                                dReading.setLocallySensed(true);
                                state.addBelief(dReading);
                                state.notifyListeners();
                                break;
                                
                            default:
                                Machinetta.Debugger.debug(3, "processMessage: Proxy to RAP message of type " + rpMsg.type + " not handled");
                        }
                    } else {
                        Machinetta.Debugger.debug(1, "processMessage: Ignoring message of type : " + iMsg.getClass());
                    }
                    
                    return true;
                }
		else {
		    // Leaving this debug in because with the new comms
		    // lib fixes this really shouldn't ever happen.
		    Debugger.debug(1,"Not consuming non RAPInput message, ID="+UDPMessage.getID(packet.getData())+" summary="+UDPMessage.summary(packet.getData()));

		}
//  		else {
// 		    if(packet.getData()[0] == 2)
// 			Machinetta.Debugger.debug("processMessage: Ignoring RP packet that is not for us.",1,this);
// 		    else
// 			Machinetta.Debugger.debug("processMessage: Ignoring non RP packet.",1,this);
//  		}
            } else {
		// Leaving this debug in because with the new comms
		// lib fixes this really shouldn't ever happen.
		Debugger.debug(1,"NULL ProxyIDString - Not consuming non RAPInput message, ID="+UDPMessage.getID(packet.getData())+" summary="+UDPMessage.summary(packet.getData()));
                Machinetta.Debugger.debug(0, "processMessage: Sending " + ++recv + "th message to proxy from RAP, but no proxyID");
            }
            return false;
        }
    };
    {
	backend.setOnlyRP(true);
    }
    
    // This function sends messages to the UAV agent.
    public synchronized void send(Object o, Machinetta.State.BeliefType.ProxyID id) {
        Machinetta.Debugger.debug(0, "Sending " + ++sent + "th message to UAV");
        synchronized(backend) {
            UDPMessage uMsg = new UDPMessage(id, new NamedProxyID(UDPMessage.SIM_NAME), (OutputMessage)o);
            backend.sendMessage(uMsg);
        }
    }
    
}
