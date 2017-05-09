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
package Gui;

// @author      Sean R. Owens
// @version     $Id: BackgroundConfig.java,v 1.3 2007/12/14 04:05:20 junyounk Exp $ 



import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

// Just a collection of flags and such to specify what kind of
// background a panel wants to use.  These are set/changed by radio
// buttons, etc, and then the instance is passed to Background to
// create a new background image.
public class BackgroundConfig extends Observable {
    // 
    public final static int CONTOUR_MENU_NONE = 0;
    public final static int CONTOUR_MENU_25 = 1;
    public final static int CONTOUR_MENU_50 = 2;
    public final static int CONTOUR_MENU_100 = 3;
    public final static int CONTOUR_MENU_250 = 4;
    public final static int CONTOUR_MENU_500 = 5;
    public final static int CONTOUR_MENU_1000 = 6;

    // Multiples of 25 meters... contour drawing actually works by
    // figuring out contours every 25 meters, then storing them, and
    // later on skipping over some of them if we're not drawing every
    // 25 meters.
    public final static int CONTOUR_MULT_NONE = 0;
    public final static int CONTOUR_MULT_25 = 1;
    public final static int CONTOUR_MULT_50 = 2;
    public final static int CONTOUR_MULT_100 = 4;
    public final static int CONTOUR_MULT_250 = 10;
    public final static int CONTOUR_MULT_500 = 20;
    public final static int CONTOUR_MULT_1000 = 40;

    public boolean gridLinesOneKm = false;
    public boolean gridLinesTenKm = true;
    public boolean soilTypes = true;
    public int contourMultiples = CONTOUR_MENU_100;
    public boolean obstacles = false;
    public boolean configSpace = false;
    public boolean elevation = false;
    public boolean voronoiGraph = false;
    public boolean showUnits = true;
    public boolean showNai = true;
    public boolean showMinefields = true;
    public boolean showCheckpoints = true;
    public boolean showObservationPosts = false;
    public boolean showClearings = false;
    public boolean showCorridors = false;
    public boolean showAAPrimary = true;
    public boolean showAASecondary = false;
    public boolean showCandidateEngagementAreas = false;
    public boolean showInferencedEngagementAreas = true;
    public boolean showRoads = true;
    public boolean showSatImages = false;
    public boolean showRailways = true;
    public boolean showMapObjectTypes = false;
    public boolean showMapObjectNames = false;
    public boolean showTraces = true;

    public ViewPort viewPort = null;

    public BackgroundConfig(ViewPort viewPort) {
	this.viewPort = viewPort;
    }

    public void changed() {
	setChanged();
	notifyObservers(null);
    }

    public void changed(int command) {
	Debug.debug(".BackgroundConfig: changed called with command="+command);
	setChanged();
	notifyObservers(new Integer(command));
    }
}
