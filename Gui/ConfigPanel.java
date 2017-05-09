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
// @version     $Id: ConfigPanel.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class ConfigPanel extends JPanel implements ActionListener, Observer {
    private final static boolean SHOW_GRIDLINESONEKM = true;
    private final static boolean SHOW_GRIDLINESTENKM = true;
    private final static boolean SHOW_SOILTYPES = true;
    private final static boolean SHOW_CONTOURS = true;
    private final static boolean SHOW_OBSTACLES = true;
    private final static boolean SHOW_CONFIGSPACE = false;
    private final static boolean SHOW_ELEVATION = false;
    private final static boolean SHOW_VORONOIGRAPH = false;
    private final static boolean SHOW_SHOWMAPOBJECTTYPES = true;
    private final static boolean SHOW_SHOWMAPOBJECTNAMES = true;
    private final static boolean SHOW_SHOWTRACES = true;
    private final static boolean SHOW_SHOWUNITS = false;
    private final static boolean SHOW_SHOWNAI = false;
    private final static boolean SHOW_SHOWMINEFIELDS = false;
    private final static boolean SHOW_SHOWCHECKPOINTS = false;
    private final static boolean SHOW_SHOWOBSERVATIONPOSTS = false;
    private final static boolean SHOW_SHOWCLEARINGS = false;
    private final static boolean SHOW_SHOWCORRIDORS = false;
    private final static boolean SHOW_SHOWAAPRIMARY = false;
    private final static boolean SHOW_SHOWAASECONDARY = false;
    private final static boolean SHOW_SHOWCANDIDATEENGAGEMENTAREAS = false;
    private final static boolean SHOW_SHOWINFERENCEDENGAGEMENTAREAS = false;

    private final static boolean SHOW_SHOWROADS = false;
    private final static boolean SHOW_SHOWRAILWAYS = false;

    private DebugInterface debug = null;
    BackgroundConfig config = null;
    
    private FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 1, 1);

    private final static String GRIDLINESONEKM_TOOLTIP = "Click to toggle displaying grid lines every one kilometer";
    private final static String GRIDLINESTENKM_TOOLTIP = "Click to toggle displaying grid lines every one ten kilometers";
    private final static String SOILTYPES_TOOLTIP = "Click to toggle displaying soil types (i.e. sand, mud, packed dirt, roads, water, trees, etc.)";
    private final static String CONTOURS_TOOLTIP = "Click to select displaying of terrain contours for every 25/50/100/etc meters of elevation";
    private final static String OBSTACLES_TOOLTIP = "Click to toggle displaying obstacle terrain";
    private final static String CONFIGSPACE_TOOLTIP = "Click to toggle displaying configuration spaces";
    private final static String ELEVATION_TOOLTIP = "Click to toggle displaying elevations";
    private final static String VORONOIGRAPH_TOOLTIP = "Click to toggle displaying Voronoi graph";
    private final static String SHOWUNITS_TOOLTIP = "Click to toggle displaying of military/non militaryunits";
    private final static String SHOWNAI_TOOLTIP = "Click to toggle displaying Named Areas of Interest";
    private final static String SHOWMINEFIELDS_TOOLTIP = "Click to toggle displaying mine fields";
    private final static String SHOWCHECKPOINTS_TOOLTIP = "Click to toggle displaying check points";
    private final static String SHOWOBSERVATIONPOSTS_TOOLTIP = "Click to toggle displaying observation posts";
    private final static String SHOWCLEARINGS_TOOLTIP = "Click to toggle displaying clearings";
    private final static String SHOWCORRIDORS_TOOLTIP = "Click to toggle displaying corridors";
    private final static String SHOWAAPRIMARY_TOOLTIP = "Click to toggle displaying Primary Avenue of Approach";
    private final static String SHOWAASECONDARY_TOOLTIP = "Click to toggle displaying Secondary Avenue of Approach";
    private final static String SHOWCANDIDATEENGAGEMENTAREAS_TOOLTIP = "Click to toggle displaying Engagement Area candidates";
    private final static String SHOWINFERENCEDENGAGEMENTAREAS_TOOLTIP = "Click to toggle displaying inferenced Engagement Areas";
    private final static String SHOWMAPOBJECTTYPES_TOOLTIP = "Click to toggle displaying 'types' next to units";
    private final static String SHOWMAPOBJECTNAMES_TOOLTIP = "Click to toggle displaying 'names' next to units";
    private final static String SHOWTRACES_TOOLTIP = "Click to toggle displaying 'traces' for units, i.e. trails of past movement";

    JToggleButton gridLinesOneKm = null;
    JToggleButton gridLinesTenKm = null;
    JToggleButton soilTypes = null;
    JComboBox contours = null;
    JToggleButton obstacles = null;
    JToggleButton configSpace = null;
    JToggleButton elevation = null;
    JToggleButton voronoiGraph = null;
    JToggleButton showUnits = null;
    JToggleButton showNai = null;
    JToggleButton showMinefields = null;
    JToggleButton showCheckpoints = null;
    JToggleButton showObservationPosts = null;
    JToggleButton showClearings = null;
    JToggleButton showCorridors = null;
    JToggleButton showAAPrimary = null;
    JToggleButton showAASecondary = null;
    JToggleButton showCandidateEngagementAreas = null;
    JToggleButton showInferencedEngagementAreas = null;
    JToggleButton showMapObjectTypes = null;
    JToggleButton showMapObjectNames = null;
    JToggleButton showTraces = null;
    
    public ConfigPanel(BackgroundConfig config) {
	debug = new DebugFacade(this);
	this.config = config;

	this.setLayout(layout);

	if(SHOW_GRIDLINESONEKM) {
	    gridLinesOneKm = new JToggleButton("One Km Grid",config.gridLinesOneKm);
	    gridLinesOneKm.addActionListener(this);
	    gridLinesOneKm.setToolTipText(GRIDLINESONEKM_TOOLTIP);
	    add(gridLinesOneKm);
	}

	if(SHOW_GRIDLINESTENKM) {
	    gridLinesTenKm = new JToggleButton("Ten Km Grid",config.gridLinesTenKm);
	    gridLinesTenKm.addActionListener(this);
	    gridLinesTenKm.setToolTipText(GRIDLINESTENKM_TOOLTIP);
	    add(gridLinesTenKm);
	}

	if(SHOW_SOILTYPES) {
	    soilTypes = new JToggleButton("Soil",config.soilTypes);
	    soilTypes.addActionListener(this);
	    soilTypes.setToolTipText(SOILTYPES_TOOLTIP);
	    add(soilTypes);
	}

	if(SHOW_CONTOURS) {
	    add(new JLabel("Contours:"));
	    contours = new JComboBox();
	    contours.addItem("None");
	    contours.addItem("25 meters");
	    contours.addItem("50 meters");
	    contours.addItem("100 meters");
	    contours.addItem("250 meters");
	    contours.addItem("500 meters");
	    contours.addItem("1000 meters");
	    contours.addActionListener(this);
	    contours.setToolTipText(CONTOURS_TOOLTIP);
	    contours.setSelectedIndex(config.contourMultiples);
	    add(contours);
	}

	if(SHOW_OBSTACLES) {
	    obstacles = new JToggleButton("Obstacles",config.obstacles);
	    obstacles.addActionListener(this);
	    obstacles.setToolTipText(OBSTACLES_TOOLTIP);
	    add(obstacles);
	}

	if(SHOW_CONFIGSPACE) {
	    configSpace = new JToggleButton("ConfigSpace",config.configSpace);
	    configSpace.addActionListener(this);
	    configSpace.setToolTipText(CONFIGSPACE_TOOLTIP);
	    add(configSpace);
	}

	if(SHOW_ELEVATION) {
	    // 	elevation = new JToggleButton("Elevation",config.elevation);
	    //  elevation.addActionListener(this);
	    //  elevation.setToolTipText(ELEVATION_TOOLTIP);
	    // 	add(elevation);
	}

	if(SHOW_VORONOIGRAPH) {
	    // 	voronoiGraph = new JToggleButton("VoronoiGraph",config.voronoiGraph);
	    //  voronoiGraph.addActionListener(this);
	    //  voronoiGraph.setToolTipText(VORONOIGRAPH_TOOLTIP);
	    // 	add(voronoiGraph);
	}

	if(SHOW_SHOWMAPOBJECTTYPES) {
	    showMapObjectTypes = new JToggleButton("Types",config.showMapObjectTypes);
	    showMapObjectTypes.addActionListener(this);
	    showMapObjectTypes.setToolTipText(SHOWMAPOBJECTTYPES_TOOLTIP);
	    add(showMapObjectTypes);
	}

	if(SHOW_SHOWMAPOBJECTNAMES) {
	    showMapObjectNames = new JToggleButton("Names",config.showMapObjectNames);
	    showMapObjectNames.addActionListener(this);
	    showMapObjectNames.setToolTipText(SHOWMAPOBJECTNAMES_TOOLTIP);
	    add(showMapObjectNames);
	}

	if(SHOW_SHOWTRACES) {
	    showTraces = new JToggleButton("Traces",config.showTraces);
	    showTraces.addActionListener(this);
	    showTraces.setToolTipText(SHOWTRACES_TOOLTIP);
	    add(showTraces);
	}

	if(SHOW_SHOWUNITS) {
	    showUnits = new JToggleButton("Units",config.showUnits);
	    showUnits.addActionListener(this);
	    showUnits.setToolTipText(SHOWUNITS_TOOLTIP);
	    add(showUnits);
	}

	if(SHOW_SHOWNAI) {
	    showNai = new JToggleButton("NAIs",config.showNai);
	    showNai.addActionListener(this);
	    showNai.setToolTipText(SHOWNAI_TOOLTIP);
	    add(showNai);
	}

	if(SHOW_SHOWMINEFIELDS) {
	    showMinefields = new JToggleButton("Minefields",config.showMinefields);
	    showMinefields.addActionListener(this);
	    showMinefields.setToolTipText(SHOWMINEFIELDS_TOOLTIP);
	    add(showMinefields);
	}

	if(SHOW_SHOWCHECKPOINTS) {
	    showCheckpoints = new JToggleButton("Checkpoints",config.showCheckpoints);
	    showCheckpoints.addActionListener(this);
	    showCheckpoints.setToolTipText(SHOWCHECKPOINTS_TOOLTIP);
	    add(showCheckpoints);
	}

	if(SHOW_SHOWOBSERVATIONPOSTS) {
	    showObservationPosts = new JToggleButton("Obs Posts",config.showObservationPosts);
	    showObservationPosts.addActionListener(this);
	    showObservationPosts.setToolTipText(SHOWOBSERVATIONPOSTS_TOOLTIP);
	    add(showObservationPosts);
	}

	if(SHOW_SHOWCLEARINGS) {
	    showClearings = new JToggleButton("Clearings",config.showClearings);
	    showClearings.addActionListener(this);
	    showClearings.setToolTipText(SHOWCLEARINGS_TOOLTIP);
	    add(showClearings);
	}

	if(SHOW_SHOWCORRIDORS) {
	    showCorridors = new JToggleButton("Corridors",config.showCorridors);
	    showCorridors.addActionListener(this);
	    showCorridors.setToolTipText(SHOWCORRIDORS_TOOLTIP);
	    add(showCorridors);
	}

	if(SHOW_SHOWAAPRIMARY) {
	    showAAPrimary = new JToggleButton("Primary AA",config.showAAPrimary);
	    showAAPrimary.addActionListener(this);
	    showAAPrimary.setToolTipText(SHOWAAPRIMARY_TOOLTIP);
	    add(showAAPrimary);
	}

	if(SHOW_SHOWAASECONDARY) {
	    showAASecondary = new JToggleButton("Secondary AA",config.showAASecondary);
	    showAASecondary.addActionListener(this);
	    showAASecondary.setToolTipText(SHOWAASECONDARY_TOOLTIP);
	    add(showAASecondary);
	}

	if(SHOW_SHOWCANDIDATEENGAGEMENTAREAS) {
	    showCandidateEngagementAreas = new JToggleButton("EA Cand.",config.showCandidateEngagementAreas);
	    showCandidateEngagementAreas.addActionListener(this);
	    showCandidateEngagementAreas.setToolTipText(SHOWCANDIDATEENGAGEMENTAREAS_TOOLTIP);
	    add(showCandidateEngagementAreas);
	}

	if(SHOW_SHOWINFERENCEDENGAGEMENTAREAS) {
	    showInferencedEngagementAreas = new JToggleButton("EAs",config.showInferencedEngagementAreas);
	    showInferencedEngagementAreas.addActionListener(this);
	    showInferencedEngagementAreas.setToolTipText(SHOWINFERENCEDENGAGEMENTAREAS_TOOLTIP);
	    add(showInferencedEngagementAreas);
	}

	debug.info("preferred size of config panel is "+getPreferredSize().toString());
	debug.info("minimum size of config panel is "+getMinimumSize().toString());
	//	setMinimumSize(new Dimension(1000,100));
	config.addObserver(this);
    }

    public void update(Observable  o, Object  arg) {
	debug.debug("update: o="+o+", arg="+arg);
	if(o == config) {
	    debug.debug("update: setting button settings from BackgroundConfig");
	    EventQueue.invokeLater(new Runnable() { 
		    public void run() { 
			if(null != gridLinesOneKm)
			    gridLinesOneKm.setSelected(config.gridLinesOneKm);
			if(null != gridLinesTenKm)
			    gridLinesTenKm.setSelected(config.gridLinesTenKm);
			if(null != soilTypes)
			    soilTypes.setSelected(config.soilTypes);
 			if(null != contours)
 			    contours.setSelectedIndex(config.contourMultiples);
			if(null != obstacles)
			    obstacles.setSelected(config.obstacles);
			if(null != configSpace)
			    configSpace.setSelected(config.configSpace);
			if(null != elevation) 
			    elevation.setSelected(config.elevation);
			if(null != voronoiGraph)
			    voronoiGraph.setSelected(config.voronoiGraph);
			if(null != showUnits)
			    showUnits.setSelected(config.showUnits);
			if(null != showNai)
			    showNai.setSelected(config.showNai);
			if(null != showMinefields)
			    showMinefields.setSelected(config.showMinefields);
			if(null != showCheckpoints)
			    showCheckpoints.setSelected(config.showCheckpoints);
			if(null != showObservationPosts)
			    showObservationPosts.setSelected(config.showObservationPosts);
			if(null != showClearings)
			    showClearings.setSelected(config.showClearings);
			if(null != showCorridors)
			    showCorridors.setSelected(config.showCorridors);
			if(null != showAAPrimary)
			    showAAPrimary.setSelected(config.showAAPrimary);
			if(null != showAASecondary)
			    showAASecondary.setSelected(config.showAASecondary);
			if(null != showCandidateEngagementAreas)
			    showCandidateEngagementAreas.setSelected(config.showCandidateEngagementAreas);
			if(null != showInferencedEngagementAreas)
			    showInferencedEngagementAreas.setSelected(config.showInferencedEngagementAreas);
			if(null != showMapObjectTypes)
			    showMapObjectTypes.setSelected(config.showMapObjectTypes);
			if(null != showMapObjectNames)
			    showMapObjectNames.setSelected(config.showMapObjectNames);
			if(null != showTraces)
			    showTraces.setSelected(config.showTraces);
		    }
		}
				   );
	    repaint();
	}
    }

    // @todo: (SRO Tue Apr 18 21:07:27 EDT 2006) why did I do
    // this?  I think this leftover from some crappy way i was
    // doing buttons before, using observer.  I can probably get
    // rid of this.
    public void updateOld(Observable  o, Object  arg) {
	// VERY IMPORTANT: Do NOT call config.changed in this method.
	// This method is called by config.changed(), so if we call
	// config.changed() in here then we get infinite recursion and
	// this is a bad thing.

	if(null != gridLinesOneKm)
	    config.gridLinesOneKm = gridLinesOneKm.isSelected();
	if(null != gridLinesTenKm)
	    config.gridLinesTenKm = gridLinesTenKm.isSelected();
	if(null != soilTypes)
	    config.soilTypes = soilTypes.isSelected();
	if(null != contours)
	    config.contourMultiples = contours.getSelectedIndex();
	if(null != obstacles)
	    config.obstacles = obstacles.isSelected();
	if(null != configSpace)
	    config.configSpace = configSpace.isSelected();
	if(null != elevation) 
	    config.elevation = elevation.isSelected();
	if(null != voronoiGraph)
	    config.voronoiGraph = voronoiGraph.isSelected();
	if(null != showUnits)
	    config.showUnits = showUnits.isSelected();
	if(null != showNai)
	    config.showNai = showNai.isSelected();
	if(null != showMinefields)
	    config.showMinefields = showMinefields.isSelected();
	if(null != showCheckpoints)
	    config.showCheckpoints = showCheckpoints.isSelected();
	if(null != showObservationPosts)
	    config.showObservationPosts = showObservationPosts.isSelected();
	if(null != showClearings)
	    config.showClearings = showClearings.isSelected();
	if(null != showCorridors)
	    config.showCorridors = showCorridors.isSelected();
	if(null != showAAPrimary)
	    config.showAAPrimary = showAAPrimary.isSelected();
	if(null != showAASecondary)
	    config.showAASecondary = showAASecondary.isSelected();
	if(null != showCandidateEngagementAreas)
	    config.showCandidateEngagementAreas = showCandidateEngagementAreas.isSelected();
	if(null != showInferencedEngagementAreas)
	    config.showInferencedEngagementAreas = showInferencedEngagementAreas.isSelected();
	if(null != showMapObjectTypes)
	    config.showMapObjectTypes = showMapObjectTypes.isSelected();
	if(null != showMapObjectNames)
	    config.showMapObjectNames = showMapObjectNames.isSelected();
	if(null != showTraces)
	    config.showTraces = showTraces.isSelected();
	repaint();
    }

    public void actionPerformed(ActionEvent e) {
	debug.debug("actionPerformed: e="+e);
	Object source = e.getSource();
	if(source == gridLinesOneKm) {
	    if(config.gridLinesOneKm != gridLinesOneKm.isSelected()) {
		config.gridLinesOneKm = gridLinesOneKm.isSelected();
		config.changed();
	    }
	}
	else if(source == gridLinesTenKm) {
	    if(config.gridLinesTenKm != gridLinesTenKm.isSelected()) {
		config.gridLinesTenKm = gridLinesTenKm.isSelected();
		config.changed();
	    }
	}
	else if(source == soilTypes) {
	    if(config.soilTypes != soilTypes.isSelected()) {
		config.soilTypes = soilTypes.isSelected();
		config.changed();
	    }
	}
	else if(source == contours) {
	    if(config.contourMultiples != contours.getSelectedIndex()) {
		config.contourMultiples = contours.getSelectedIndex();
		config.changed();
	    }
	}
	else if(source == obstacles) {
	    if(config.obstacles != obstacles.isSelected()) {
		config.obstacles = obstacles.isSelected();
		config.changed();
	    }
	}
	else if(source == configSpace) {
	    if(config.configSpace != configSpace.isSelected()) {
		config.configSpace = configSpace.isSelected();
		config.changed();
	    }
	}
	else if(source == elevation) {
	    if(config.elevation != elevation.isSelected()) {
		config.elevation = elevation.isSelected();
		config.changed();
	    }
	}
	else if(source == voronoiGraph) {
	    if(config.voronoiGraph != voronoiGraph.isSelected()) {
		config.voronoiGraph = voronoiGraph.isSelected();
		config.changed();
	    }
	}
	else if(source == showUnits) {
	    if(config.showUnits != showUnits.isSelected()) {
		config.showUnits = showUnits.isSelected();
		config.changed();
	    }
	}
	else if(source == showNai) {
	    if(config.showNai != showNai.isSelected()) {
		config.showNai = showNai.isSelected();
		config.changed();
	    }
	}
	else if(source == showMinefields) {
	    if(config.showMinefields != showMinefields.isSelected()) {
		config.showMinefields = showMinefields.isSelected();
		config.changed();
	    }
	}
	else if(source == showCheckpoints) {
	    if(config.showCheckpoints != showCheckpoints.isSelected()) {
		config.showCheckpoints = showCheckpoints.isSelected();
		config.changed();
	    }
	}
	else if(source == showObservationPosts) {
	    if(config.showObservationPosts != showObservationPosts.isSelected()) {
		config.showObservationPosts = showObservationPosts.isSelected();
		config.changed();
	    }
	}
	else if(source == showClearings) {
	    if(config.showClearings != showClearings.isSelected()) {
		config.showClearings = showClearings.isSelected();
		config.changed();
	    }
	}
	else if(source == showCorridors) {
	    if(config.showCorridors != showCorridors.isSelected()) {
		config.showCorridors = showCorridors.isSelected();
		config.changed();
	    }
	}
	else if(source == showAAPrimary) {
	    if(config.showAAPrimary != showAAPrimary.isSelected()) {
		config.showAAPrimary = showAAPrimary.isSelected();
		config.changed();
	    }
	}
	else if(source == showAASecondary) {
	    if(config.showAASecondary != showAASecondary.isSelected()) {
		config.showAASecondary = showAASecondary.isSelected();
		config.changed();
	    }
	}
	else if(source == showCandidateEngagementAreas) {
	    if(config.showCandidateEngagementAreas != showCandidateEngagementAreas.isSelected()) {
		config.showCandidateEngagementAreas = showCandidateEngagementAreas.isSelected();
		config.changed();
	    }
	}
	else if(source == showInferencedEngagementAreas) {
	    if(config.showInferencedEngagementAreas != showInferencedEngagementAreas.isSelected()) {
		config.showInferencedEngagementAreas = showInferencedEngagementAreas.isSelected();
		config.changed();
	    }
	}
	else if(source == showMapObjectTypes) {
	    if(config.showMapObjectTypes != showMapObjectTypes.isSelected()) {
		config.showMapObjectTypes = showMapObjectTypes.isSelected();
		config.changed();
	    }
	}
	else if(source == showMapObjectNames) {
	    if(config.showMapObjectNames != showMapObjectNames.isSelected()) {
		config.showMapObjectNames = showMapObjectNames.isSelected();
		config.changed();
	    }
	}
	else if(source == showTraces) {
	    if(config.showTraces != showTraces.isSelected()) {
		config.showTraces = showTraces.isSelected();
		config.changed();
	    }
	}
	else {
	    debug.error("Got an action event from an unknown source.");
	}
    }


    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	//	debug.info("preferred size="+getPreferredSize().toString());
	//	debug.info("minimum size="+getMinimumSize().toString());
    }
    
}
