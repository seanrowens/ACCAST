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
 * SimConfiguration.java
 *
 * Created on March 3, 2008, 4:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Commander;
import Machinetta.ConfigReader;
import Machinetta.Configuration;
import Machinetta.Debugger;
import AirSim.Machinetta.UAVRI;
import Gui.BackgroundConfig;

/**
 *
 * 
 * This class contains the parameters for operating SimUser.
 * @author junyounk
 */
public class SimConfiguration {
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS LOADED FROM CONFIG FILE (GENERAL ENVIRONMENT)
    // ----------------------------------------------------------------------    
    public static String CTDB_BASE_NAME;
    public static String ROAD_FILE_NAME;
    public static int REPAINT_INTERVAL_MS = 1000/1;
    public static boolean SHOW_GUI = false;
    public static boolean SHOW_SIMUSER_GUI = false;
    
    // Be aware that at low values (5, 10, 20, etc) the Thread.sleep()
    // method is unreliable.  Also be aware that this reaction delay
    // is in real, wall clock milliseconds.  If the simulation is
    // running much faster than real time then this will be
    // significantly faster.  (i.e. if a simulator step represents
    // 1/10 of a simulated second, and if the simulator is running at
    // 5 ms per step, hence simulating 20 times reality, then the
    // reaction delay will be similarly exaggerated - a delay of 100ms
    // will equate to approximately 2000ms (the simulator sleep()
    // sufferes from the same reliability issues) delay in the
    // simulator.
    public static long REACTION_DELAY_MS = 0;

    private static long WAIT_BEFORE_MOVE_PLAN_MS = 20000;
    public static double DROPPED_PLAN_PROB = 0.0;

    public static int MAP_WIDTH_METERS = 50000;
    public static int MAP_HEIGHT_METERS = 50000;

    // the scaling factor to go from map coords to indices into the
    // bayes filter probability array - and also to figure out what
    // size array to use, based on map size.
    public static double BBF_GRID_SCALE_FACTOR = 100.0;

    public static boolean BINARY_BAYES_FILTER_ON = false;

    public static boolean BAYES_FILTER_PANEL_ON=false;
    public static int BAYES_FILTER_PANEL_X=0;
    public static int BAYES_FILTER_PANEL_Y=20;

    public static boolean ENTROPY_PANEL_ON = false;
    public static int ENTROPY_PANEL_X = 0;
    public static int ENTROPY_PANEL_Y = 0;

    public static boolean CLUSTERING_ON=false;
    
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS (GENERAL RULES)
    // ----------------------------------------------------------------------   
    private static double timing = 0.5;
    //what kind of type can we have?
    /**
     * User types indicate the user's intention and aptitude for the simulation.
     * This field is designed to give options to users.
     * @NOTE: of course, we can give actual number btw 0(defensive) and 1(offensive) rather than the descretized value.
     * This totally depends on the policy, and even though we use some levels for this field, that will be translated into
     * appropriate number.
     */
    public static enum UserType {Offensive, Mediate, Defensive}
    /**
     * Task types indicate the importance of each task. (we can give some valus btw 0(skippable) and 1(very important; critical)
     * i.e. Inevitable: 0.9, Mediate: 0.5, Skippable; 0.1 => then multiply this value to the goal ratio to control the flow
     * with the desired value at each step.
     */
    public static enum TaskType {Critical, Mediate, Skippable}
    // ==> We need to make a general and good formulation w/ UserType & TaskType.
    
    public static int numDIBlues = 5;
    public static int numC130s = 4;
    public static int numAUGVs = 4;
    public static int numAUAVs = 4;
    public static int numIMs = 3;
    public static int numEOIRUAVs = 1;
    public static int numHVs = 2;    
    
    //something else.
    
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS (SPECIFIC PARAMETERS FOR EACH TASK OR PLAN)
    // ----------------------------------------------------------------------   
    private static double spreadOutRadius = 0.5;
    //something else.
    public static int SEE_STATE_DIST = 250;
    public static int FIRE_DIST = 300;
    public static int SENSE_RADIUS = 400;
    public static int IDENTICAL_RADIUS = 5;
    
    // ----------------------------------------------------------------------
    // BEGIN PARAMETERS (Mainly for World State Mgr)
    // ----------------------------------------------------------------------
    public final static long SENSOR_READING_EXPIRATION_LIMIT_MS = 1000 * 30;
    public static long lastTimeExpiredObjects = System.currentTimeMillis();
    public final static long RUN_SENSOR_READING_EXPIRE_EVERY_N_MS = 1000;
    public final static double EXPIRATION_CONFIDENCE_LIMIT = .6;
    
    public static boolean GUI_ON = false;
    public static double PLAN_CREATION_DROP_PROBABILITY = -1;
    public static double PLAN_CREATION_REACTION_TIME_MS = 0;
    public static boolean PLAN_CREATION_AUAV_ATTACK_DI = false;
    public static boolean PLAN_CREATION_AUGV_ATTACK_DI = false;
    public static boolean PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = false;
        
    public static int NUM_AUAVS=0;
    public static int NUM_EOIRUAVs=0;
    public static int NUM_AUGVs=0;

    public static boolean SMALL_ACCAST_SCENARIO = true;

    /** 
     * Creates a new instance of SimConfiguration
     */
    public SimConfiguration() {
    }
    
    /**
     * Gets actual parameter values from the config files
     */
    public static void getConfigFields() {
   
        if(null == Configuration.allMap) {
            //Debugger.debug("Configuration.allMap is null, can't read config options.", 5, this);
            System.out.println("Configuration.allMap is null, can't read config options.");
        }

	UAVRI.readConfigs();
	
	MAP_WIDTH_METERS = ConfigReader.getConfig("MAP_WIDTH_METERS", MAP_WIDTH_METERS, true);
	MAP_HEIGHT_METERS = ConfigReader.getConfig("MAP_HEIGHT_METERS", MAP_HEIGHT_METERS, true);
	BBF_GRID_SCALE_FACTOR = ConfigReader.getConfig("BBF_GRID_SCALE_FACTOR", BBF_GRID_SCALE_FACTOR, true);

	CLUSTERING_ON = ConfigReader.getConfig("CLUSTERING_ON", CLUSTERING_ON, true);
	BINARY_BAYES_FILTER_ON = ConfigReader.getConfig("BINARY_BAYES_FILTER_ON", BINARY_BAYES_FILTER_ON, true);

        BAYES_FILTER_PANEL_ON = ConfigReader.getConfig("BAYES_FILTER_PANEL_ON", BAYES_FILTER_PANEL_ON, false);
        BAYES_FILTER_PANEL_X = ConfigReader.getConfig("BAYES_FILTER_PANEL_X", BAYES_FILTER_PANEL_X, false);
        BAYES_FILTER_PANEL_Y = ConfigReader.getConfig("BAYES_FILTER_PANEL_Y", BAYES_FILTER_PANEL_Y, false);

        ENTROPY_PANEL_ON = ConfigReader.getConfigBoolean("ENTROPY_PANEL_ON", ENTROPY_PANEL_ON, false);
        ENTROPY_PANEL_X = ConfigReader.getConfigInt("ENTROPY_PANEL_X", ENTROPY_PANEL_X, false);
        ENTROPY_PANEL_Y = ConfigReader.getConfigInt("ENTROPY_PANEL_Y", ENTROPY_PANEL_Y, false);

	CTDB_BASE_NAME = ConfigReader.getConfig("CTDB_BASE_NAME", CTDB_BASE_NAME, true);
	ROAD_FILE_NAME = ConfigReader.getConfig("ROAD_FILE_NAME", ROAD_FILE_NAME, true);
	REPAINT_INTERVAL_MS = ConfigReader.getConfig("REPAINT_INTERVAL_MS", REPAINT_INTERVAL_MS, true);
	SHOW_GUI = ConfigReader.getConfig("SHOW_GUI", SHOW_GUI, true);	
	REACTION_DELAY_MS = ConfigReader.getConfig("REACTION_DELAY_MS", REACTION_DELAY_MS, true);
	WAIT_BEFORE_MOVE_PLAN_MS = ConfigReader.getConfig("WAIT_BEFORE_MOVE_PLAN_MS", WAIT_BEFORE_MOVE_PLAN_MS, true);
	DROPPED_PLAN_PROB = ConfigReader.getConfig("DROPPED_PLAN_PROB", DROPPED_PLAN_PROB, true);
	if(Configuration.allMap.containsKey("GUI_VIEWPORT_X")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_Y")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_WIDTH")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_HEIGHT")) {
	    SimOperatorGUI.setViewPort = true;
	    SimOperatorGUI.viewPortX = ConfigReader.getConfig("GUI_VIEWPORT_X", SimOperatorGUI.viewPortX, true);
	    SimOperatorGUI.viewPortY = ConfigReader.getConfig("GUI_VIEWPORT_Y", SimOperatorGUI.viewPortY, true);
	    SimOperatorGUI.viewPortWidth = ConfigReader.getConfig("GUI_VIEWPORT_WIDTH", SimOperatorGUI.viewPortWidth, true);
	    SimOperatorGUI.viewPortHeight = ConfigReader.getConfig("GUI_VIEWPORT_HEIGHT", SimOperatorGUI.viewPortHeight, true);
	}
	SimOperatorGUI.soilTypes = ConfigReader.getConfig("GUI_SOIL_TYPES", SimOperatorGUI.soilTypes, true);
	SimOperatorGUI.showTraces = ConfigReader.getConfig("GUI_SHOW_TRACES", SimOperatorGUI.showTraces, true);
	SimOperatorGUI.gridLinesOneKm = ConfigReader.getConfig("GUI_GRID_LINES_ONE_KM", SimOperatorGUI.gridLinesOneKm, true);
	SimOperatorGUI.showMapObjectNames = ConfigReader.getConfig("GUI_SHOW_MAP_OBJECT_NAMES", SimOperatorGUI.showMapObjectNames, true);
	if(Configuration.allMap.containsKey("GUI_CONTOUR_MULTIPLES")) {
	    int multiple = ConfigReader.getConfig("GUI_CONTOUR_MULTIPLES", 0, true);
	    if(multiple <= 0)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_NONE;
	    else if(multiple == 25)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_25;
	    else if(multiple == 50)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_50;
	    else if(multiple == 100)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
	    else if(multiple == 250)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_250;
	    else if(multiple == 500)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_500;
	    else if(multiple == 1000)
		SimOperatorGUI.contourMultiples = BackgroundConfig.CONTOUR_MULT_1000;
	}

	GUI_ON = ConfigReader.getConfig("GUI_ON", GUI_ON, true);
	PLAN_CREATION_DROP_PROBABILITY = ConfigReader.getConfig("PLAN_CREATION_DROP_PROBABILITY", PLAN_CREATION_DROP_PROBABILITY, true);
	PLAN_CREATION_REACTION_TIME_MS = ConfigReader.getConfig("PLAN_CREATION_REACTION_TIME_MS", PLAN_CREATION_REACTION_TIME_MS, true);
	PLAN_CREATION_AUAV_ATTACK_DI = ConfigReader.getConfig("PLAN_CREATION_AUAV_ATTACK_DI", PLAN_CREATION_AUAV_ATTACK_DI, true);
	PLAN_CREATION_AUGV_ATTACK_DI = ConfigReader.getConfig("PLAN_CREATION_AUGV_ATTACK_DI", PLAN_CREATION_AUGV_ATTACK_DI, true);
	PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI = ConfigReader.getConfig("PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI", PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI, true);
	NUM_AUAVS = ConfigReader.getConfig("NUM_AUAVS",NUM_AUAVS, true);
	NUM_EOIRUAVs = ConfigReader.getConfig("NUM_EOIRUAVS", NUM_EOIRUAVs, true);
	NUM_AUGVs = ConfigReader.getConfig("NUM_AUGVS", NUM_AUGVs, true);
	SMALL_ACCAST_SCENARIO = ConfigReader.getConfig("SMALL_ACCAST_SCENARIO", SMALL_ACCAST_SCENARIO, true);
    }
        
}
