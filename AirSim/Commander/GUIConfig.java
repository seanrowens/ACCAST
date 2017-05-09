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
 * GUIConfig.java
 *
 * Created on March 20, 2006, 9:34 AM
 *
 */

package AirSim.Commander;

import Machinetta.Debugger;
import Machinetta.Configuration;
import Machinetta.ConfigReader;

import Gui.BackgroundConfig;

public class GUIConfig {

    public static String CTDB_BASE_NAME;
    public static String ROAD_FILE_NAME;
    public static int REPAINT_INTERVAL_MS = 1000/1;
    public static boolean SHOW_GUI = true;

    public static boolean SET_VIEWPORT = false;
    public static double GUI_VIEWPORT_X = 0;
    public static double GUI_VIEWPORT_Y = 0;
    public static double GUI_VIEWPORT_WIDTH = 0;
    public static double GUI_VIEWPORT_HEIGHT = 0;
    public static boolean GUI_SOIL_TYPES = false;
    public static boolean GUI_SHOW_TRACES = true;
    public static int GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_100;
    public static boolean GUI_GRID_LINES_ONE_KM = true;
    public static boolean GUI_SHOW_MAP_OBJECT_NAMES = true;

    public static void getConfigFields() {
        if(null == Configuration.allMap) {
            Debugger.debug("Configuration.allMap is null, can't read config options.", 5, "GUIConfig");
        }

	CTDB_BASE_NAME = ConfigReader.getConfig("CTDB_BASE_NAME", CTDB_BASE_NAME, true);
	ROAD_FILE_NAME = ConfigReader.getConfig("ROAD_FILE_NAME", ROAD_FILE_NAME, true);
	REPAINT_INTERVAL_MS = ConfigReader.getConfig("REPAINT_INTERVAL_MS", REPAINT_INTERVAL_MS, true);
	SHOW_GUI = ConfigReader.getConfig("SHOW_GUI", SHOW_GUI, true);
	if(Configuration.allMap.containsKey("GUI_VIEWPORT_X")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_Y")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_WIDTH")
	   || Configuration.allMap.containsKey("GUI_VIEWPORT_HEIGHT")) {
	    SET_VIEWPORT = true;
	    GUI_VIEWPORT_X = ConfigReader.getConfig("GUI_VIEWPORT_X", GUI_VIEWPORT_X, true);
	    GUI_VIEWPORT_Y = ConfigReader.getConfig("GUI_VIEWPORT_Y", GUI_VIEWPORT_Y, true);
	    GUI_VIEWPORT_WIDTH = ConfigReader.getConfig("GUI_VIEWPORT_WIDTH", GUI_VIEWPORT_WIDTH, true);
	    GUI_VIEWPORT_HEIGHT = ConfigReader.getConfig("GUI_VIEWPORT_HEIGHT", GUI_VIEWPORT_HEIGHT, true);
	}
	GUI_SOIL_TYPES = ConfigReader.getConfig("GUI_SOIL_TYPES", GUI_SOIL_TYPES, true);
	GUI_SHOW_TRACES = ConfigReader.getConfig("GUI_SHOW_TRACES", GUI_SHOW_TRACES, true);
	GUI_GRID_LINES_ONE_KM = ConfigReader.getConfig("GUI_GRID_LINES_ONE_KM", GUI_GRID_LINES_ONE_KM, true);
	GUI_SHOW_MAP_OBJECT_NAMES = ConfigReader.getConfig("GUI_SHOW_MAP_OBJECT_NAMES", GUI_SHOW_MAP_OBJECT_NAMES, true);
	if(Configuration.allMap.containsKey("GUI_CONTOUR_MULTIPLES")) {
	    int multiple = ConfigReader.getConfig("GUI_CONTOUR_MULTIPLES", 0, true);
	    if(multiple <= 0)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_NONE;
	    else if(multiple == 25)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_25;
	    else if(multiple == 50)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_50;
	    else if(multiple == 100)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_100;
	    else if(multiple == 250)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_250;
	    else if(multiple == 500)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_500;
	    else if(multiple == 1000)
		GUI_CONTOUR_MULTIPLES = BackgroundConfig.CONTOUR_MULT_1000;
	}
    }

}
